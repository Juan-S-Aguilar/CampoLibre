package com.example.campolibre.Service;

import com.example.campolibre.DTO.AsistenciaConsumidorDTO; // Tendrás que crear este DTO
import java.util.List;

public interface AsistenciaConsumidorService {

    // Consumidor/Admin: Registra la asistencia (Check-in)
    AsistenciaConsumidorDTO registrarAsistencia(Long idConsumidor, Long idEvento);

    // Admin: Verifica si un consumidor ya asistió.
    boolean yaAsistio(Long idConsumidor, Long idEvento);

    // Admin: Reporte de asistentes por evento.
    List<AsistenciaConsumidorDTO> obtenerAsistentesPorEvento(Long idEvento);

    // Admin: Obtiene el número total de asistentes para reportes.
    Long contarAsistenciaPorEvento(Long idEvento);
}