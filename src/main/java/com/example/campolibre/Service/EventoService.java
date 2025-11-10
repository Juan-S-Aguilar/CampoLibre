package com.example.campolibre.Service;

import com.example.campolibre.DTO.EventoDTO;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.TipoEvento;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

public interface EventoService {
    EventoDTO crearEvento(EventoDTO eventoDTO, MultipartFile imagen);
    EventoDTO obtenerEventoPorId(Long id);
    List<EventoDTO> obtenerTodosLosEventos();
    List<EventoDTO> obtenerEventosAprobados();
    List<EventoDTO> obtenerEventosPendientes();
    List<EventoDTO> obtenerEventosPorCreador(Long idCreador);
    List<EventoDTO> obtenerEventosPorTipo(TipoEvento tipoEvento);
    List<EventoDTO> obtenerEventosProximos(LocalDate fecha);
    EventoDTO actualizarEvento(Long id, EventoDTO eventoDTO, MultipartFile imagen);
    void cambiarEstadoEvento(Long id, EstadoEvento estado);
    void eliminarEvento(Long id);
}