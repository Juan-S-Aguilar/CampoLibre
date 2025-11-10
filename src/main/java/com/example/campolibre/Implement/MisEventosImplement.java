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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MisEventosImplement implements MisEventosService {

    private final MisEventosRepository misEventosRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public MisEventosImplement(MisEventosRepository misEventosRepository, UsuarioRepository usuarioRepository,
                               EventoRepository eventoRepository, ModelMapper modelMapper) {
        this.misEventosRepository = misEventosRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
        this.modelMapper = modelMapper;
    }

    // El método original (confirmarAsistencia) puede ser usado por otros endpoints o API REST
    @Override
    public MisEventosDTO confirmarAsistencia(MisEventosDTO misEventosDTO) {
        Usuario usuario = usuarioRepository.findById(misEventosDTO.getId_usuario())
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        Evento evento = eventoRepository.findById(misEventosDTO.getId_evento())
                .orElseThrow(() -> new CustomException("Evento no encontrado"));

        // Verificar si ya confirmó asistencia (Lógica robusta, la mantenemos)
        MisEventos existente = misEventosRepository.findByUsuarioIdAndEventoId(
                misEventosDTO.getId_usuario(), misEventosDTO.getId_evento());

        if (existente != null) {
            throw new CustomException("Ya has confirmado asistencia a este evento");
        }

        MisEventos misEventos = new MisEventos();
        misEventos.setUsuario(usuario);
        misEventos.setEvento(evento);

        MisEventos nuevaConfirmacion = misEventosRepository.save(misEventos);

        MisEventosDTO resultado = new MisEventosDTO();
        resultado.setId_mis_eventos(nuevaConfirmacion.getId_mis_eventos());
        resultado.setId_usuario(nuevaConfirmacion.getUsuario().getId_usuario());
        resultado.setId_evento(nuevaConfirmacion.getEvento().getId_evento());
        resultado.setFecha_guardado(nuevaConfirmacion.getFecha_guardado());

        return resultado;
    }

    // ✨ NUEVO MÉTODO IMPLEMENTADO para usar en EventoController
    // Este método es más simple y se alinea con la llamada del Controller
    @Override
    public void guardarAsistencia(Long idUsuario, Long idEvento) {

        // 1. Verificar si ya confirmó asistencia
        if (usuarioConfirmoAsistencia(idUsuario, idEvento)) {
            // Si ya existe, lanzamos la excepción para notificar al Controller/Usuario
            throw new CustomException("Ya has guardado este evento en tu lista.");
        }

        // 2. Obtener Entidades (mismas verificaciones que el método original)
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new CustomException("Evento no encontrado"));

        // 3. Crear y guardar
        MisEventos misEventos = new MisEventos();
        misEventos.setUsuario(usuario);
        misEventos.setEvento(evento);

        misEventosRepository.save(misEventos);

        // No retorna nada (void), cumpliendo con la necesidad del Controller.
    }


    @Override
    public List<MisEventosDTO> obtenerEventosDeUsuario(Long idUsuario) {
        List<MisEventos> eventos = misEventosRepository.findByUsuarioId(idUsuario);
        return eventos.stream()
                .map(me -> {
                    MisEventosDTO dto = new MisEventosDTO();
                    dto.setId_mis_eventos(me.getId_mis_eventos());
                    dto.setId_usuario(me.getUsuario().getId_usuario());
                    dto.setId_evento(me.getEvento().getId_evento());
                    dto.setFecha_guardado(me.getFecha_guardado());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MisEventosDTO> obtenerAsistentesPorEvento(Long idEvento) {
        List<MisEventos> asistentes = misEventosRepository.findByEventoId(idEvento);
        return asistentes.stream()
                .map(me -> {
                    MisEventosDTO dto = new MisEventosDTO();
                    dto.setId_mis_eventos(me.getId_mis_eventos());
                    dto.setId_usuario(me.getUsuario().getId_usuario());
                    dto.setId_evento(me.getEvento().getId_evento());
                    dto.setFecha_guardado(me.getFecha_guardado());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void cancelarAsistencia(Long id) {
        MisEventos misEventos = misEventosRepository.findById(id)
                .orElseThrow(() -> new CustomException("Confirmación de asistencia no encontrada"));
        misEventosRepository.delete(misEventos);
    }

    @Override
    public boolean usuarioConfirmoAsistencia(Long idUsuario, Long idEvento) {
        MisEventos misEventos = misEventosRepository.findByUsuarioIdAndEventoId(idUsuario, idEvento);
        return misEventos != null;
    }
}