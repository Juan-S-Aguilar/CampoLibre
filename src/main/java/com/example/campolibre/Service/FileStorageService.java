package com.example.campolibre.Service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String guardarArchivo(MultipartFile archivo, String carpeta);
    void eliminarArchivo(String rutaArchivo);
    String obtenerRutaCompleta(String nombreArchivo, String carpeta);
}