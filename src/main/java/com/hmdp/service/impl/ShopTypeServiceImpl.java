package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        // 1. 从redis根据id查询商铺信息
        Set<String> list = stringRedisTemplate.opsForZSet().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        // 2. 判断是否存在
        if (list != null && !list.isEmpty()) {
            // 3. 如果存在，直接返回
            List<ShopType> shopTypeList = new ArrayList<>();
            for (String s : list) {
                ShopType shopType = JSONUtil.toBean(s, ShopType.class);
                shopTypeList.add(shopType);
            }
            return Result.ok(shopTypeList);
        }

        // 4. 如果不存在，从数据库查询

        List<ShopType> typeList = query().orderByAsc("sort").list();
        // 5. 不存在，返回404
        if (!typeList.isEmpty()) {
            for (ShopType shopType : typeList) {
                stringRedisTemplate.opsForZSet().add(CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopType), shopType.getSort());
            }
        }
        return Result.ok(typeList);

    }
}
