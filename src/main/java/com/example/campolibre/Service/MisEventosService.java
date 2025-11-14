package com.example.campolibre.Service;

import com.example.campolibre.DTO.MisEventosDTO;
import java.util.List;

public interface MisEventosService {

    /**
     * Guarda la intención de asistencia de un Consumidor.
     * Esto dispara el correo de confirmación de detalles del evento.
     * @param idUsuario ID del Consumidor.
     * @param idEvento ID del Evento.
     * @return MisEventosDTO con los datos de la relación guardada.
     */
    MisEventosDTO guardarIntencionAsistencia(Long idUsuario, Long idEvento);

    /**
     * Elimina el evento de la lista "Mis Eventos" del Consumidor.
     * @param idUsuario ID del Consumidor.
     * @param idEvento ID del Evento.
     */
    void removerIntencionAsistencia(Long idUsuario, Long idEvento);

    /**
     * Obtiene la lista de Eventos guardados por un Consumidor.
     * @param idUsuario ID del Consumidor.
     * @return Lista de MisEventosDTO.
     */
    List<MisEventosDTO> obtenerEventosGuardadosDeUsuario(Long idUsuario);

    /**
     * Verifica si un Consumidor ya tiene un Evento guardado en su lista.
     * @param idUsuario ID del Consumidor.
     * @param idEvento ID del Evento.
     * @return true si ya está guardado.
     */
    boolean usuarioTieneEventoGuardado(Long idUsuario, Long idEvento);
}