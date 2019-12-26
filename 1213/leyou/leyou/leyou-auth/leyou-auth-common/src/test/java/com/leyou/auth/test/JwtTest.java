package com.leyou.auth.test;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "D:\\heima30\\rsa\\rsa.pub";

    private static final String priKeyPath = "D:\\heima30\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(22L, "jack1"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjIsInVzZXJuYW1lIjoiamFjazEiLCJleHAiOjE1NzE5NzM0ODJ9.Xp-KxXUA28bIBShyLfsoK0ai_6GIftIYv10rocmQjV6lI0iMKgnNoIPLFKK0LwWz5cpJQSBkN3uibWTPrF2nhdd3k3bS8MY49jHzlgP33B0YJ31-mjaC4TjqbTZb9-le3iMhyPtdDbRNZQqtIiTQpSsskPaaOqaLfkpWHzTdASY";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}