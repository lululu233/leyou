package com.leyou.gateway.filter;

import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component //注入到spring 容器
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private FilterProperties filterProperties;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public String filterType() {
        return "pre";//前置路由 进入网关前
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {//返回 true 执行run  false 不执行

        //获取白名单
        List<String> allowPaths = this.filterProperties.getAllowPaths();

        //获取request对象
        //初始化运行上下文
        RequestContext context = RequestContext.getCurrentContext();

        //获取reques对象
        HttpServletRequest request = context.getRequest();
        
        //获取请求url
        String url = request.getRequestURL().toString();

        for (String allowPath : allowPaths){

            if(StringUtils.contains(url, allowPath)){
                return  false; //url 包含在 allowpath 则 不执行run
            }
        }
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        //初始化运行上下文
        RequestContext context = RequestContext.getCurrentContext();

        //获取reques对象
        HttpServletRequest request = context.getRequest();
        String cookieName = this.jwtProperties.getCookieName();
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());

        /*if(StringUtils.isBlank(token)){
            context.setSendZuulResponse(false);//不转发请求
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());

        }*/

        try {
            JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());//解析token
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
