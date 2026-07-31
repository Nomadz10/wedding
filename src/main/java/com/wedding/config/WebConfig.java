package com.wedding.config;

import com.wedding.service.FileStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final FileStorageService fileStorage;

    public WebConfig(AdminAuthInterceptor adminAuthInterceptor, FileStorageService fileStorage) {
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.fileStorage = fileStorage;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor).addPathPatterns("/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded event photos from disk.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileStorage.getUploadDir().toUri().toString());
    }
}
