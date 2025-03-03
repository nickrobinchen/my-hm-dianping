package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.dto.UserJWTDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.MyJWTUtil;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


//    @Resource
//    private MyJWTUtil jwtUtil;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. 如果不符合要求，返回错误信息
            return Result.fail("手机号格式不正确");
        }

        // 3. 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4. 保存验证码到session

        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5. 发送短信验证码
        log.debug("发送短信验证码：{}", code);

        // 6. 返回成功信息
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1. 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式不正确");
        }
        // 2. redis校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.toString().equals(code)) {
            // 3. 不一致，返回错误信息
            return Result.fail("验证码错误");
        }

        // 4. 一致，根据手机号查询用户
        User user = query().eq("phone", phone).one();
        if (Objects.isNull(user)) {
            // 5. 用户不存在，创建用户
            user = createUserWithPhone(phone);
        }

        // 6. 保存用户信息到redis
        // 6.1 生成token
//        String token = UUID.randomUUID().toString(true);
        // 6.2 将User对象转Hash存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
//        UserJWTDTO userJWTDTO = BeanUtil.copyProperties(user, UserJWTDTO.class);
//        userJWTDTO.setExpireTime(java.time.LocalDateTime.now().plusHours(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // 6.3 保存到redis
//        String tokenKey = LOGIN_USER_KEY + token;


        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        String token = MyJWTUtil.getToken(userMap);


        //        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
//        // 6.4 expire
//        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 7. 返回成功信息
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
