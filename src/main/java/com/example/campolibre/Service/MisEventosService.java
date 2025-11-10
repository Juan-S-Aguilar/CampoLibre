package com.example.campolibre.Service;

import com.example.campolibre.DTO.MisEventosDTO;
import java.util.List;

public interface MisEventosService {

    // Método original
    MisEventosDTO confirmarAsistencia(MisEventosDTO misEventosDTO);

    // ✨ Nuevo método más simple para el Controller (Guarda la relación)
    // Usamos el nombre 'guardarAsistencia' para alinearnos con el Controller
    void guardarAsistencia(Long idUsuario, Long idEvento);

    List<MisEventosDTO> obtenerEventosDeUsuario(Long idUsuario);
    List<MisEventosDTO> obtenerAsistentesPorEvento(Long idEvento);
    void cancelarAsistencia(Long id);
    boolean usuarioConfirmoAsistencia(Long idUsuario, Long idEvento);
}