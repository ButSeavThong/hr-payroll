package com.thong.utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        //  Serve uploaded images at /uploads/**
//        registry.addResourceHandler("/uploads/**")
//                .addResourceLocations("file:./uploads/");
//    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "https://hr-payroll-xi.vercel.app"
                )
                .allowedMethods("GET", "POST","PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }


}
