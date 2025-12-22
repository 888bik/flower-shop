package com.bik.flower_shop.config;

import com.bik.flower_shop.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * @author bik
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenInterceptor tokenInterceptor;


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 可以指定前端域名，或者使用 allowedOriginPatterns 支持通配符
                .allowedOriginPatterns("http://127.0.0.1:3000", "http://127.0.0.1:5173")
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("token")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/admin/login",
                        "/admin/goods/*",
                        "/mall/**",
                        "/user/login",
                        "/user/review/*",
                        "/user/register",
                        "/user/coupon/all",
                        "/api/user/register",
                        "/**/user/register",
                        "/error",
                        "/swagger-resources/**",
                        "/v2/api-docs/**",
                        "/webjars/**"
                )
                .order(1);
    }
}
