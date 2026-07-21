package com.portal.gerenciamento.armazem.manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Libera todos os endpoints da nossa API
                .allowedOrigins("http://localhost:3000", "http://localhost:5173", "http://localhost:63342", "https://apiportalid.reportsinditex.site", "https://pdasid.pages.dev") // Portas padrão do React/Vite
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}