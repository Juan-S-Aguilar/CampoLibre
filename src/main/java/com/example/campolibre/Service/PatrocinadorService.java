package com.example.campolibre.Service;

import com.example.campolibre.DTO.PatrocinadorDTO; // Tendrás que crear este DTO
import java.util.List;

public interface PatrocinadorService {
    PatrocinadorDTO crearPatrocinador(PatrocinadorDTO patrocinadorDTO);
    PatrocinadorDTO obtenerPatrocinadorPorId(Long id);
    List<PatrocinadorDTO> obtenerTodosLosPatrocinadores();
    PatrocinadorDTO actualizarPatrocinador(Long id, PatrocinadorDTO patrocinadorDTO);
    void eliminarPatrocinador(Long id);

    // Agregar método para obtener solo patrocinadores activos
    List<PatrocinadorDTO> obtenerPatrocinadoresActivos();

    // Agregar método para archivar/activar patrocinador (en lugar de eliminar)
    PatrocinadorDTO cambiarEstadoPatrocinador(Long id, Boolean activo);
}