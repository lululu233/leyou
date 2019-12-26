package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserClient userClient;

    public String accredit(String username, String password) {
        //1.根据用户名 密码 查询  调用 user-service 中的接口 查询
        User user = this.userClient.queryUser(username, password);

        //2.判断user 是否为 null
        if (user == null){
            return null;
        }

        try {
            //3. jwtUtils 生成jwt 类型 的 token
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            return  JwtUtils.generateToken(userInfo, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
