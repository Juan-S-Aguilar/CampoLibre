package com.example.campolibre.Implement;

import com.example.campolibre.Service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageImplement implements FileStorageService {

    @Value("${upload.path:uploads/}")
    private String uploadBasePath;

    @Override
    public String guardarArchivo(MultipartFile archivo, String carpeta) {
        try {
            // Validar que el archivo no esté vacío
            if (archivo == null || archivo.isEmpty()) {
                throw new RuntimeException("El archivo está vacío");
            }

            // Validar que sea una imagen
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("El archivo debe ser una imagen válida (JPG, PNG, GIF)");
            }

            // Validar tamaño (10MB máximo)
            if (archivo.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException("El archivo no debe superar los 10MB");
            }

            // Crear nombre único para el archivo
            String nombreOriginal = archivo.getOriginalFilename();
            String extension = "";
            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            }

            // Limpiar nombre de archivo (remover caracteres especiales)
            String nombreLimpio = nombreOriginal != null ?
                    nombreOriginal.replaceAll("[^a-zA-Z0-9._-]", "_") :
                    "imagen";

            String nombreArchivo = System.currentTimeMillis() + "_" + nombreLimpio;

            // Crear ruta completa del directorio
            Path directorioCompleto = Paths.get(uploadBasePath, carpeta);

            // Crear directorios si no existen
            if (!Files.exists(directorioCompleto)) {
                Files.createDirectories(directorioCompleto);
                System.out.println("📁 Directorio creado: " + directorioCompleto.toAbsolutePath());
            }

            // Guardar archivo
            Path rutaArchivo = directorioCompleto.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

            // Retornar ruta relativa para la BD (con / al inicio para web)
            String rutaRelativa = "/uploads/" + carpeta + "/" + nombreArchivo;
            System.out.println("✅ Archivo guardado: " + rutaArchivo.toAbsolutePath());
            System.out.println("🔗 Ruta para BD: " + rutaRelativa);

            return rutaRelativa;

        } catch (IOException e) {
            System.err.println("❌ Error al guardar archivo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminarArchivo(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return;
        }

        try {
            // Remover el primer "/" si existe para construir la ruta física
            String rutaLimpia = rutaArchivo.startsWith("/") ? rutaArchivo.substring(1) : rutaArchivo;

            // Si la ruta empieza con "uploads/", usar directamente
            // Si no, asumir que es relativa a uploadBasePath
            Path archivo = rutaLimpia.startsWith("uploads/") ?
                    Paths.get(rutaLimpia) :
                    Paths.get(uploadBasePath, rutaLimpia);

            if (Files.exists(archivo)) {
                Files.delete(archivo);
                System.out.println("🗑️ Archivo eliminado: " + archivo.toAbsolutePath());
            } else {
                System.out.println("⚠️ Archivo no encontrado para eliminar: " + archivo.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Error al eliminar archivo: " + e.getMessage());
            // No lanzar excepción, solo registrar el error
        }
    }

    @Override
    public String obtenerRutaCompleta(String nombreArchivo, String carpeta) {
        Path rutaCompleta = Paths.get(uploadBasePath, carpeta, nombreArchivo);
        return rutaCompleta.toAbsolutePath().toString();
    }

    /**
     * Método auxiliar para validar si un archivo es una imagen válida
     */
    public boolean esImagenValida(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return false;
        }

        String contentType = archivo.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}