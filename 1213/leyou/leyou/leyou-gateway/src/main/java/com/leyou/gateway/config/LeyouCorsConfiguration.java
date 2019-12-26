package com.leyou.gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 配置 解决跨域问题的拦截器 cors 复杂请求 springmvc 自带的 cors 拦截器
 */

@Configuration /*声明自定义过滤器标签*/
public class LeyouCorsConfiguration {
    @Bean
    public CorsFilter corsFilter(){
        //初始cors 配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //配置 可以跨域访问的域名 需要配置所有域名 需要配置 * 则不能允许携带cookie
        corsConfiguration.addAllowedOrigin("http://manage.leyou.com");
        corsConfiguration.addAllowedOrigin("http://www.leyou.com");
        corsConfiguration.addAllowedOrigin("http://order.leyou.com");
        //携带cookie
        corsConfiguration.setAllowCredentials(true);
        //所有 请求都可跨域 get post put delete ...
        corsConfiguration.addAllowedMethod("*");
        //允许携带任何头信息
        corsConfiguration.addAllowedHeader("*");

        //初始cors配置源
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
        //返回 corsFilter 实例 参数：cors 配置源对象
        return new CorsFilter(corsConfigurationSource);
    }
}
