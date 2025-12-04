//package com.bik.flower_shop.config;
//
//import com.bik.flower_shop.interceptor.TokenInterceptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.*;
//
//@Configuration
//public class WebMvcConfig implements WebMvcConfigurer {
//
//    @Autowired
//    private TokenInterceptor tokenInterceptor;
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/admin/**")
//                // 允许带凭证时，不能使用 "*"
//                // 可以指定前端域名，或者使用 allowedOriginPatterns 支持通配符
//                .allowedOriginPatterns("http://localhost:3000", "http://127.0.0.1:3000")
//                .allowedMethods("*")
//                .allowedHeaders("*") // 允许 token 在 header 中传递
//                .exposedHeaders("token")
//                .allowCredentials(true)
//                .maxAge(3600);
//    }
//
////    @Override
////    public void addInterceptors(InterceptorRegistry registry) {
////        registry.addInterceptor(tokenInterceptor)
////                .addPathPatterns("/admin/**")
////                .excludePathPatterns(
////                        "/admin/login",
////                        "/admin/manager/*/getinfo",
////                        "/admin/public/**"
////                );
////    }
//}
