package com.hmdp.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisData;
import org.apache.tomcat.jni.Local;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        //缓存穿透
        //Shop shop = queryWithMutex(id);
        Shop shop = queryWithLogicalExpire(id);
        //缓存击穿
        if (shop == null) {
            return Result.fail("商铺不存在");
        }
        return Result.ok(shop);
    }

    public Shop queryWithLogicalExpire(Long id) {
        String key = CACHE_SHOP_KEY + id;
        // 1. 从redis根据id查询商铺信息
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(shopJson)) {
            return null;
        }

        RedisData<Shop> shopWithExpireTime = JSONUtil.toBean(shopJson, new TypeReference<RedisData<Shop>>() {
        }, false);
        Shop shop = shopWithExpireTime.getData();
        if (LocalDateTime.now().isBefore(shopWithExpireTime.getExpireTime())) { // 未过期
            return shop;
        }

        // 2. 如果过期，加锁，重新查询
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        if (!isLock) {
            return shop;
        }
        // double check
        shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(shopJson)) {
            return null;
        }
        shopWithExpireTime = JSONUtil.toBean(shopJson, new TypeReference<RedisData<Shop>>() {
        }, false);
        shop = shopWithExpireTime.getData();
        if (LocalDateTime.now().isBefore(shopWithExpireTime.getExpireTime())) { // 未过期
            unlock(lockKey);
            return shop;
        }
        CACHE_REBUILD_EXECUTOR.submit(()->{

            try {
                this.saveShop2Redis(id, 20L);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                unlock(lockKey);
            }
        });
        return shop;


    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public Shop queryWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
        // 1. 从redis根据id查询商铺信息
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3. 如果存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        if (shopJson != null) { // == ""
            return null;
        }

        // 4. 实现缓存重建
        // 4.1 尝试获取锁
        String lockKey = LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2 判断是否获取到锁
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(id);
                // 4.3 未获取到锁，等待重试
            }
            // 4. 如果不存在，从数据库查询
            shop = getById(id);
            Thread.sleep(200);
            // 5. 不存在，返回404
            if (shop == null) {
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            // 6. 存在，写入redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // 4.4 释放锁
            unlock(lockKey);
        }
        return shop;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    public void saveShop2Redis(Long id, Long expireSeconds) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Shop shop = getById(id);
        RedisData<Shop> redisData = new RedisData<>();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("商铺id不能为空");
        }
        // 1. 更新数据库
        updateById(shop);
        // 2. 删除redis缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }
}
