package com.example.campolibre.Implement;

import com.example.campolibre.DTO.MisEventosDTO;
import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Entity.MisEventos;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.EventoRepository;
import com.example.campolibre.Repository.MisEventosRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Service.MisEventosService;
import com.example.campolibre.Service.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MisEventosImplement implements MisEventosService {

    private final MisEventosRepository misEventosRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    @Autowired
    public MisEventosImplement(MisEventosRepository misEventosRepository,
                               UsuarioRepository usuarioRepository,
                               EventoRepository eventoRepository,
                               ModelMapper modelMapper,
                               EmailService emailService) {
        this.misEventosRepository = misEventosRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
        this.modelMapper = modelMapper;
        this.emailService = emailService;
    }

    @Override
    public MisEventosDTO guardarIntencionAsistencia(Long idUsuario, Long idEvento) {
        // Validar duplicado
        if (usuarioTieneEventoGuardado(idUsuario, idEvento)) {
            throw new CustomException("Este evento ya está en tu lista de 'Mis Eventos'.");
        }

        // Buscar usuario y evento
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new CustomException("Usuario (Consumidor) no encontrado."));

        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new CustomException("Evento no encontrado."));

        // Validar estado del evento
        if (evento.getEstado() != EstadoEvento.PUBLICADO) {
            throw new CustomException("Solo puedes guardar eventos publicados.");
        }

        // Validar que no sea un evento pasado
        if (evento.getFecha_evento().isBefore(LocalDate.now())) {
            throw new CustomException("No puedes guardar eventos que ya pasaron.");
        }

        // Crear relación
        MisEventos misEventos = new MisEventos();
        misEventos.setUsuario(usuario);
        misEventos.setEvento(evento);
        // Si agregaste el campo 'notificado'
        // misEventos.setNotificado(true);

        MisEventos nuevaRelacion = misEventosRepository.save(misEventos);

        // Enviar email de confirmación
        String nombrePatrocinador = evento.getPatrocinador() != null
                ? evento.getPatrocinador().getNombre()
                : "Patrocinador Oficial";

        try {
            emailService.enviarConfirmacionGuardadoEvento(
                    usuario.getEmail(),
                    usuario.getNombre(),
                    evento.getNombre(),
                    evento.getUbicacion(),
                    evento.getFecha_evento().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    evento.getHora_evento().format(DateTimeFormatter.ISO_LOCAL_TIME),
                    nombrePatrocinador
            );
        } catch (Exception e) {
            System.err.println("[MisEventosImplement] Error al enviar email: " + e.getMessage());
        }

        return convertirADTO(nuevaRelacion);
    }

    @Override
    public void removerIntencionAsistencia(Long idUsuario, Long idEvento) {
        MisEventos existente = misEventosRepository.findByUsuarioIdAndEventoId(idUsuario, idEvento);
        if (existente == null) {
            throw new CustomException("El evento no se encuentra en tu lista para ser removido.");
        }
        misEventosRepository.delete(existente);
    }

    @Override
    public List<MisEventosDTO> obtenerEventosGuardadosDeUsuario(Long idUsuario) {
        return misEventosRepository.findByUsuarioId(idUsuario).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean usuarioTieneEventoGuardado(Long idUsuario, Long idEvento) {
        return misEventosRepository.findByUsuarioIdAndEventoId(idUsuario, idEvento) != null;
    }

    // Método helper para mapeo
    private MisEventosDTO convertirADTO(MisEventos misEventos) {
        MisEventosDTO dto = modelMapper.map(misEventos, MisEventosDTO.class);

        // IDs básicos
        dto.setId_usuario(misEventos.getUsuario().getId_usuario());
        dto.setId_evento(misEventos.getEvento().getId_evento());

        // Datos de visualización del evento
        Evento evento = misEventos.getEvento();
        dto.setNombreEvento(evento.getNombre());
        dto.setUbicacionEvento(evento.getUbicacion());
        dto.setFechaEvento(evento.getFecha_evento());
        dto.setHoraEvento(evento.getHora_evento());
        dto.setImagenEvento(evento.getImagen_evento());

        // Nombre del patrocinador
        if (evento.getPatrocinador() != null) {
            dto.setNombrePatrocinador(evento.getPatrocinador().getNombre());
        }

        return dto;
    }
}