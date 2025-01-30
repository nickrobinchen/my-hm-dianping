package com.hmdp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Shop;
import com.hmdp.entity.User;
import com.hmdp.service.IUserService;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;


//@SpringBootTest
class HmDianPingApplicationTests {
//    @Resource
//    private CacheClient cacheClient;
//    @Resource
//    private ShopServiceImpl shopService;
//    @Resource
//    private StringRedisTemplate stringRedisTemplate;
//    @Resource
//    private RedisIdWorker redisIdWorker;
//    @Resource
//    private IUserService userService;
//
//    private ExecutorService es = Executors.newFixedThreadPool(500);
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;

    @Test
    public void testBasicQueue() {
        return;
        //1.定义队列名称
//        String queueName = "simple.queue";
//        //2.定义message
//        String message = "Hello,Basic Queue!";
//        //发送message
//        for (int i = 0; i < 10; i++) {
//            rabbitTemplate.convertAndSend(queueName, message + i);
//        }
    }
    @Test
    void testLogins() {
        return;
//        //13688668889
//        List<String> tokens = new ArrayList<>();
//        BigInteger basePhone = new BigInteger("13688668889");
//        for (int i = 0; i < 1000; i++) {
//            BigInteger phone = basePhone.add(new BigInteger(String.valueOf(i)));
//
//            String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone.toString());
//
//            // 4. 一致，根据手机号查询用户
//            User user = userService.query().eq("phone", phone).one();
//            if (Objects.isNull(user)) {
//                // 5. 用户不存在，创建用户
//                System.out.println("not exist");
//            }
//
//            // 6. 保存用户信息到redis
//            // 6.1 生成token
//            String token = UUID.randomUUID().toString(true);
//            // 6.2 将User对象转Hash存储
//            UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
//            // 6.3 保存到redis
//            String tokenKey = LOGIN_USER_KEY + token;
//            Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
//            stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
//            // 6.4 expire
//            stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL * 2, TimeUnit.MINUTES);
//            tokens.add(token);
//        }
//        String filePath = "./tokens.txt";
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
//            // 写入标题（可选）
//            writer.write("Phone Number");
//            writer.newLine();
//
//            // 写入每个电话号码
//            for (String token : tokens) {
//                writer.write(token);
//                writer.newLine();
//            }
//
//            System.out.println("电话号码已保存到: " + filePath);
//        } catch (IOException e) {
//            System.err.println("保存失败: " + e.getMessage());
//        }
    }

//    @Test
//    void testIdWorker() throws InterruptedException {
//        return;
//        CountDownLatch latch = new CountDownLatch(300);
//        Runnable task = () -> {
//            for (int i = 0; i < 100; i++) {
//                long id = redisIdWorker.nextId("order");
//                System.out.println("id = " + id);
//            }
//            latch.countDown();
//        };
//        long begin = System.currentTimeMillis();
//        for (int i = 0; i < 300; i++) {
//            es.submit(task);
//        }
//        latch.await();
//        long end = System.currentTimeMillis();
//        System.out.println("time = " + (end - begin));
//    }
//
//    @Test
//    void testSaveShop() {
//
//        Shop shop = shopService.getById(1);
//
//        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY + 1L, shop, 10L, TimeUnit.SECONDS);
//    }


}
