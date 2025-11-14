package com.example.campolibre.Implement;

import com.example.campolibre.DTO.AsistenciaConsumidorDTO;
import com.example.campolibre.Entity.AsistenciaConsumidor;
import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.AsistenciaConsumidorRepository;
import com.example.campolibre.Repository.EventoRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Service.AsistenciaConsumidorService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsistenciaConsumidorImplement implements AsistenciaConsumidorService {

    private final AsistenciaConsumidorRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public AsistenciaConsumidorImplement(AsistenciaConsumidorRepository asistenciaRepository,
                                         UsuarioRepository usuarioRepository,
                                         EventoRepository eventoRepository,
                                         ModelMapper modelMapper) {
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public AsistenciaConsumidorDTO registrarAsistencia(Long idConsumidor, Long idEvento) {
        // 1. Validar que no haya asistido ya
        if (yaAsistio(idConsumidor, idEvento)) {
            throw new CustomException("Este consumidor ya ha registrado asistencia a este evento.");
        }

        // 2. Obtener entidades
        Usuario consumidor = usuarioRepository.findById(idConsumidor)
                .orElseThrow(() -> new CustomException("Consumidor no encontrado."));
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new CustomException("Evento no encontrado."));

        // 3. Validar si el evento es hoy (Opcional, pero recomendado)
        if (!evento.getFecha_evento().isEqual(LocalDate.now())) {
            System.out.println("ADVERTENCIA: Registrando asistencia en un día diferente al del evento.");
            // throw new CustomException("La asistencia solo puede registrarse el día del evento.");
        }

        // 4. Crear el registro de asistencia
        AsistenciaConsumidor asistencia = new AsistenciaConsumidor();
        asistencia.setConsumidor(consumidor);
        asistencia.setEvento(evento);

        AsistenciaConsumidor nuevaAsistencia = asistenciaRepository.save(asistencia);

        // ✅ MEJORA: Usar método helper
        return mapearConDatosVisualizacion(nuevaAsistencia);
    }

    @Override
    public boolean yaAsistio(Long idConsumidor, Long idEvento) {
        return asistenciaRepository.findByConsumidorAndEventoId(idConsumidor, idEvento).isPresent();
    }

    @Override
    public List<AsistenciaConsumidorDTO> obtenerAsistentesPorEvento(Long idEvento) {
        return asistenciaRepository.findByEventoId(idEvento).stream()
                // ✅ MEJORA: Usar método helper
                .map(this::mapearConDatosVisualizacion)
                .collect(Collectors.toList());
    }

    @Override
    public Long contarAsistenciaPorEvento(Long idEvento) {
        return asistenciaRepository.countAsistenciaByEventoId(idEvento);
    }

    // ✅ Método helper para mapeo completo
    private AsistenciaConsumidorDTO mapearConDatosVisualizacion(AsistenciaConsumidor asistencia) {
        AsistenciaConsumidorDTO dto = modelMapper.map(asistencia, AsistenciaConsumidorDTO.class);

        if (asistencia.getConsumidor() != null) {
            dto.setNombreConsumidor(asistencia.getConsumidor().getNombre());
        }

        if (asistencia.getEvento() != null) {
            dto.setNombreEvento(asistencia.getEvento().getNombre());
        }

        return dto;
    }
}