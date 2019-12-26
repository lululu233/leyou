package com.leyou.cart.interceptor;

import com.leyou.cart.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.soap.Addressing;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter{


    @Autowired
    private JwtProperties jwtProperties;//token

    //线程局部变量 一个请求对应一个线程
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取cookie 中的token
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        //解析token 获取用户信息
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());

        if (userInfo == null){
            return false;
        }
        //userInfo 放入线程局部变量中
        THREAD_LOCAL.set(userInfo);

        return true;
    }

    //获取线程局部变量
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        //清空线程局部变量 使用tomcat线程池会返还给tomcat  需要手动释放
        THREAD_LOCAL.remove();
    }
}
