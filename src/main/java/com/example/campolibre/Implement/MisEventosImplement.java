package com.example.campolibre.Implement;

import com.example.campolibre.DTO.MisEventosDTO;
import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Entity.MisEventos;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.EventoRepository;
import com.example.campolibre.Repository.MisEventosRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Service.MisEventosService;
import com.example.campolibre.Service.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

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
        if (usuarioTieneEventoGuardado(idUsuario, idEvento)) {
            throw new CustomException("Este evento ya está en tu lista de 'Mis Eventos'.");
        }
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new CustomException("Usuario (Consumidor) no encontrado."));
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new CustomException("Evento no encontrado."));

        MisEventos misEventos = new MisEventos();
        misEventos.setUsuario(usuario);
        misEventos.setEvento(evento);
        MisEventos nuevaRelacion = misEventosRepository.save(misEventos);

        String nombrePatrocinador = evento.getPatrocinador() != null ? evento.getPatrocinador().getNombre() : "Patrocinador Oficial";

        emailService.enviarConfirmacionGuardadoEvento(
                usuario.getEmail(),
                usuario.getNombre(),
                evento.getNombre(),
                evento.getUbicacion(),
                evento.getFecha_evento().format(DateTimeFormatter.ISO_LOCAL_DATE),
                evento.getHora_evento().format(DateTimeFormatter.ISO_LOCAL_TIME),
                nombrePatrocinador
        );

        return modelMapper.map(nuevaRelacion, MisEventosDTO.class);
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
        List<MisEventos> eventos = misEventosRepository.findByUsuarioId(idUsuario);
        return eventos.stream()
                .map(me -> {
                    MisEventosDTO dto = modelMapper.map(me, MisEventosDTO.class);

                    // Agregar datos de visualización del evento
                    dto.setNombreEvento(me.getEvento().getNombre());
                    dto.setUbicacionEvento(me.getEvento().getUbicacion());
                    dto.setFechaEvento(me.getEvento().getFecha_evento());
                    dto.setImagenEvento(me.getEvento().getImagen_evento());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean usuarioTieneEventoGuardado(Long idUsuario, Long idEvento) {
        MisEventos misEventos = misEventosRepository.findByUsuarioIdAndEventoId(idUsuario, idEvento);
        return misEventos != null;
    }
}