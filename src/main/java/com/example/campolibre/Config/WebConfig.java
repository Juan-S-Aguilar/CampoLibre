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

        // 1. Obtiene el Path del directorio "uploads/"
        // Usamos Path.of() para manejar rutas relativas y absolutas correctamente.
        Path uploadDir = Path.of(uploadPath);

        // 2. Convierte el Path a un URI/URL estándar (ej: file:///C:/ruta/a/uploads/)
        // Esto asegura que se usen barras diagonales (/) en lugar de las barras invertidas de Windows (\)
        String fileUrl = uploadDir.toUri().toASCIIString();

        System.out.println("📂 Configurando ruta de uploads: " + fileUrl);

        // 3. Mapea la ruta web (/uploads/**) a la ubicación física (file:///)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileUrl);


        // Servir archivos estáticos de /static (Tu configuración actual, se mantiene)
        registry.addResourceHandler("/images/**", "/css/**", "/js/**")
                .addResourceLocations("classpath:/static/images/",
                        "classpath:/static/css/",
                        "classpath:/static/js/");
    }
}