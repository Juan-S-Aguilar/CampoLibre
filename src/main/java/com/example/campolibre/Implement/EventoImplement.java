package com.example.campolibre.Implement;

import com.example.campolibre.DTO.EventoDTO;
import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.TipoEvento;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.EventoRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Service.EventoService;
import com.example.campolibre.Service.FileStorageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoImplement implements EventoService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;

    @Autowired
    public EventoImplement(EventoRepository eventoRepository, UsuarioRepository usuarioRepository,
                           FileStorageService fileStorageService, ModelMapper modelMapper) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.fileStorageService = fileStorageService;
        this.modelMapper = modelMapper;
    }

    @Override
    public EventoDTO crearEvento(EventoDTO eventoDTO, MultipartFile imagen) {
        Usuario creador = usuarioRepository.findById(eventoDTO.getCreado_por())
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        Evento evento = modelMapper.map(eventoDTO, Evento.class);
        evento.setCreado_por(creador);
        evento.setEstado(EstadoEvento.PENDIENTE);

        if (imagen != null && !imagen.isEmpty()) {
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "eventos");
            evento.setImagen_evento(rutaImagen);
        }

        Evento nuevoEvento = eventoRepository.save(evento);

        EventoDTO resultado = modelMapper.map(nuevoEvento, EventoDTO.class);
        resultado.setCreado_por(nuevoEvento.getCreado_por().getId_usuario());
        return resultado;
    }

    @Override
    public EventoDTO obtenerEventoPorId(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Evento no encontrado"));

        EventoDTO resultado = modelMapper.map(evento, EventoDTO.class);
        resultado.setCreado_por(evento.getCreado_por().getId_usuario());
        return resultado;
    }

    @Override
    public List<EventoDTO> obtenerTodosLosEventos() {
        List<Evento> eventos = eventoRepository.findAll();
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> obtenerEventosAprobados() {
        List<Evento> eventos = eventoRepository.findAllApproved();
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> obtenerEventosPendientes() {
        List<Evento> eventos = eventoRepository.findByEstado(EstadoEvento.PENDIENTE);
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> obtenerEventosPorCreador(Long idCreador) {
        List<Evento> eventos = eventoRepository.findByCreadorId(idCreador);
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> obtenerEventosPorTipo(TipoEvento tipoEvento) {
        List<Evento> eventos = eventoRepository.findByTipoEventoAndAprobado(tipoEvento);
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> obtenerEventosProximos(LocalDate fecha) {
        List<Evento> eventos = eventoRepository.findEventosProximos(fecha);
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventoDTO actualizarEvento(Long id, EventoDTO eventoDTO, MultipartFile imagen) {
        Evento eventoExistente = eventoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Evento no encontrado"));

        // Actualizar campos básicos
        eventoExistente.setNombre(eventoDTO.getNombre());
        eventoExistente.setDescripcion(eventoDTO.getDescripcion());
        eventoExistente.setUbicacion(eventoDTO.getUbicacion());
        eventoExistente.setFecha_evento(eventoDTO.getFecha_evento());
        eventoExistente.setHora_evento(eventoDTO.getHora_evento());
        eventoExistente.setTipo_evento(eventoDTO.getTipo_evento());

        // ✅ CORRECCIÓN: Manejar imagen desde MultipartFile O desde DTO
        if (imagen != null && !imagen.isEmpty()) {
            // Caso 1: Viene imagen nueva desde el form (usado en versiones antiguas)
            System.out.println("📸 [Service] Guardando imagen desde MultipartFile");
            if (eventoExistente.getImagen_evento() != null) {
                fileStorageService.eliminarArchivo(eventoExistente.getImagen_evento());
            }
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "eventos");
            eventoExistente.setImagen_evento(rutaImagen);
        } else if (eventoDTO.getImagen_evento() != null) {
            // ✅ Caso 2: La imagen ya fue guardada en el Controller
            System.out.println("📸 [Service] Usando imagen desde DTO: " + eventoDTO.getImagen_evento());
            eventoExistente.setImagen_evento(eventoDTO.getImagen_evento());
        }
        // Si ambos son null, mantiene la imagen existente (no hace nada)

        Evento eventoActualizado = eventoRepository.save(eventoExistente);

        EventoDTO resultado = modelMapper.map(eventoActualizado, EventoDTO.class);
        resultado.setCreado_por(eventoActualizado.getCreado_por().getId_usuario());
        return resultado;
    }

    @Override
    public void cambiarEstadoEvento(Long id, EstadoEvento estado) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Evento no encontrado"));
        evento.setEstado(estado);
        eventoRepository.save(evento);
    }

    @Override
    public void eliminarEvento(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Evento no encontrado"));
        evento.setEstado(EstadoEvento.ELIMINADO);
        eventoRepository.save(evento);
    }
}
