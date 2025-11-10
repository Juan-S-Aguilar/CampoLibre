package com.example.campolibre.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ Obtener ruta absoluta de la carpeta uploads
        Path uploadDir = Paths.get(uploadPath);
        String uploadPathAbsolute = uploadDir.toFile().getAbsolutePath();

        System.out.println("=".repeat(60));
        System.out.println("📂 CONFIGURACIÓN DE ARCHIVOS ESTÁTICOS");
        System.out.println("=".repeat(60));
        System.out.println("📁 Ruta uploads (relativa): " + uploadPath);
        System.out.println("📁 Ruta uploads (absoluta): " + uploadPathAbsolute);
        System.out.println("🌐 URL de acceso: http://localhost:8080/uploads/");
        System.out.println("=".repeat(60));

        // ✅ CRÍTICO: Usar file: en lugar de classpath:
        // Esto sirve archivos desde el sistema de archivos real, NO del JAR
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPathAbsolute + "/")
                .setCachePeriod(0); // Desactivar caché para desarrollo

        // Servir archivos estáticos de /static (CSS, JS, imágenes del tema)
        registry.addResourceHandler("/images/**", "/css/**", "/js/**")
                .addResourceLocations("classpath:/static/images/",
                        "classpath:/static/css/",
                        "classpath:/static/js/");
    }
}