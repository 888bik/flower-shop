package com.bik.flower_shop.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.io.File;

/**
 * @author bik
 */
@Configuration
public class MultipartConfig {

    @Value("${spring.servlet.multipart.location:C:/code/projects/flower_shop/temp}")
    private String multipartLocation;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // 确保目录存在
        File dir = new File(multipartLocation);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(multipartLocation);
        factory.setMaxFileSize(DataSize.ofMegabytes(50));
        factory.setMaxRequestSize(DataSize.ofMegabytes(200));
        return factory.createMultipartConfig();
    }
}
