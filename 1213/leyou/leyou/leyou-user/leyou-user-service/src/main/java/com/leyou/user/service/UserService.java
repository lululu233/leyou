package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;

import com.leyou.user.mapper.UserMapper;

import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-05 15:55
 **/
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String KEY_PREFIX = "user:verify";

    //校验接口
    public Boolean checkUser(String data, Integer type) {
        User record = new User();
        if(type == 1){
            //用户名
            record.setUsername(data);
        }else if (type == 2){
            //手机
            record.setPhone(data);
        }else {
            return null;
        }
        return this.userMapper.selectCount(record) == 0;//查询符合记录有多少条没有 则失败
    }

    public void sendCode(String phone) {
        String key = KEY_PREFIX + phone;
        // 1、生成验证码
        String code = NumberUtils.generateCode(6);
        // 2、存入redis
        this.redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        // 3、发送消息
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        this.amqpTemplate.convertAndSend("leyou.sms.exchange", "verifycode.sms", msg);
    }


    public void register(User user, String code) {

        //查询redis中的验证码
        String redisCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        //1.校验验证码

        if (!StringUtils.equals(code, redisCode)){
            return;
        }
        //2. 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        //3. 加盐加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));

        //4.新增用户
        user.setId(null);
        user.setCreated(new Date());

        this.userMapper.insertSelective(user);


    }

    //用户查询
    public User queryUser(String username, String password) {

        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);

        //判断user 是否为空
        if (user == null){
            return null;
        }

        //获取盐 对用户 输入的密码加盐 加密
        password = CodecUtils.md5Hex(password, user.getSalt());

        //和数据库中的密码 比较
        if (StringUtils.equals(password, user.getPassword())){
            return  user;
        }
        return null;
    }
}
