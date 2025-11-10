package com.example.campolibre.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir archivos de uploads (imágenes subidas)
        Path uploadDir = Paths.get(uploadPath);
        String uploadPathAbsolute = uploadDir.toFile().getAbsolutePath();

        System.out.println("📂 Configurando ruta de uploads: " + uploadPathAbsolute);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPathAbsolute + "/");

        // Servir archivos estáticos de /static
        registry.addResourceHandler("/images/**", "/css/**", "/js/**")
                .addResourceLocations("classpath:/static/images/",
                        "classpath:/static/css/",
                        "classpath:/static/js/");
    }
}