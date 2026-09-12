package com.wedding.config;

import com.wedding.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
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

        // Tell browsers never to serve a stale HTML page — always revalidate.
        // Versioned assets (/css, /images, /uploads) are excluded so they stay cached.
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
                return true;
            }
        }).addPathPatterns("/**")
          .excludePathPatterns("/css/**", "/images/**", "/uploads/**", "/js/**", "/webjars/**", "/qr.png");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded event photos from disk.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileStorage.getUploadDir().toUri().toString());
    }
}
