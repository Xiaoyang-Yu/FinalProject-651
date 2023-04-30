package com.example.partsmaintenance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 解决跨域问题
 * @author: Xiaoyang Yu
 * @create_at: 2023/4/19 18:26
 * @version: 1.0
 */
@Configuration
public class MyCorsConfig {

    @Bean
    public CorsFilter corsFilter(){
        //1.添加cors配置信息
        CorsConfiguration configuration = new CorsConfiguration();
        //允许的域 不要写*，否则cookie就无法使用了
        configuration.addAllowedOrigin("http://192.168.1.251:8080");
        //是否发送cookie信息
        configuration.setAllowCredentials(true);
        //允许请求的方式
        configuration.addAllowedMethod("*");
        //允许的头信息
        configuration.addAllowedHeader("*");
        //添加映射地址，我们拦截一切请求
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);

        //返回新的CorsFilter
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
