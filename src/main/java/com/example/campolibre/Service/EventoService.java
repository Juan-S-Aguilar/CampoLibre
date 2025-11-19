package com.example.campolibre.Service;

import com.example.campolibre.DTO.EventoDTO;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.TipoEvento;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

import com.example.campolibre.DTO.EventoCreacionDTO;

public interface EventoService {

    List<EventoDTO> obtenerTodosLosEventos();
    List<EventoDTO> obtenerEventosPublicados();
    List<EventoDTO> obtenerEventosBorrador();
    List<EventoDTO> obtenerEventosPorCreador(Long idCreador);
    List<EventoDTO> obtenerEventosPorTipo(TipoEvento tipoEvento);
    List<EventoDTO> obtenerEventosProximos(LocalDate fecha);

    void cambiarEstadoEvento(Long id, EstadoEvento estado);
    void eliminarEvento(Long id);

    EventoDTO crearEvento(EventoCreacionDTO eventoCreacionDTO, Long idAdmin, MultipartFile imagen);
    EventoDTO obtenerEventoPorId(Long id);
    EventoDTO actualizarEvento(Long id, EventoCreacionDTO eventoCreacionDTO, MultipartFile imagen);

    // Agregar método para publicar evento (cambiar de BORRADOR a PUBLICADO)
    void publicarEvento(Long idEvento);

    // Agregar método para obtener eventos con cupos disponibles
    List<EventoDTO> obtenerEventosConCuposDisponibles();

    // Agregar método para buscar eventos por patrocinador
    List<EventoDTO> obtenerEventosPorPatrocinador(Long idPatrocinador);

    // 🌟 NUEVO MÉTODO 🌟
    // Objetivo: Obtener la lista completa de eventos creados por el administrador.
    List<EventoDTO> obtenerTodosLosEventosCreadosPorAdministrador(Long idAdmin);



}