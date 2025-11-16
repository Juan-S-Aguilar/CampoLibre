package com.example.campolibre.Implement;

import com.example.campolibre.DTO.PatrocinadorDTO;
import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Entity.Patrocinador;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.EventoRepository;
import com.example.campolibre.Repository.PatrocinadorRepository;
import com.example.campolibre.Service.PatrocinadorService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatrocinadorImplement implements PatrocinadorService {

    private final PatrocinadorRepository patrocinadorRepository;
    private final ModelMapper modelMapper;
    private final EventoRepository eventoRepository;

    @Autowired
    public PatrocinadorImplement(PatrocinadorRepository patrocinadorRepository,
                                 ModelMapper modelMapper,
                                 EventoRepository eventoRepository) {
        this.patrocinadorRepository = patrocinadorRepository;
        this.modelMapper = modelMapper;
        this.eventoRepository = eventoRepository;
    }

    @Override
    public PatrocinadorDTO crearPatrocinador(PatrocinadorDTO patrocinadorDTO) {
        // Validación: nombre único
        patrocinadorRepository.findByNombre(patrocinadorDTO.getNombre())
                .ifPresent(p -> {
                    throw new CustomException("Ya existe un patrocinador con ese nombre");
                });

        Patrocinador patrocinador = modelMapper.map(patrocinadorDTO, Patrocinador.class);
        Patrocinador nuevoPatrocinador = patrocinadorRepository.save(patrocinador);
        return modelMapper.map(nuevoPatrocinador, PatrocinadorDTO.class);
    }

    @Override
    public PatrocinadorDTO obtenerPatrocinadorPorId(Long id) {
        Patrocinador patrocinador = patrocinadorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Patrocinador no encontrado"));
        return modelMapper.map(patrocinador, PatrocinadorDTO.class);
    }

    @Override
    public List<PatrocinadorDTO> obtenerTodosLosPatrocinadores() {
        return patrocinadorRepository.findAll().stream()
                .map(patrocinador -> modelMapper.map(patrocinador, PatrocinadorDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<PatrocinadorDTO> obtenerPatrocinadoresActivos() {
        return patrocinadorRepository.findAllActivos().stream()
                .map(patrocinador -> modelMapper.map(patrocinador, PatrocinadorDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public PatrocinadorDTO actualizarPatrocinador(Long id, PatrocinadorDTO patrocinadorDTO) {
        Patrocinador patrocinadorExistente = patrocinadorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Patrocinador no encontrado"));

        // Validar nombre único (si cambió)
        if (!patrocinadorExistente.getNombre().equals(patrocinadorDTO.getNombre())) {
            patrocinadorRepository.findByNombre(patrocinadorDTO.getNombre())
                    .ifPresent(p -> {
                        throw new CustomException("Ya existe otro patrocinador con ese nombre");
                    });
        }

        patrocinadorExistente.setNombre(patrocinadorDTO.getNombre());
        patrocinadorExistente.setDescripcion(patrocinadorDTO.getDescripcion());
        patrocinadorExistente.setLogoUrl(patrocinadorDTO.getLogoUrl());
        patrocinadorExistente.setContactoEmail(patrocinadorDTO.getContactoEmail());
        patrocinadorExistente.setTelefonoContacto(patrocinadorDTO.getTelefonoContacto());
        patrocinadorExistente.setSitioWeb(patrocinadorDTO.getSitioWeb());
        patrocinadorExistente.setActivo(patrocinadorDTO.getActivo());

        Patrocinador actualizado = patrocinadorRepository.save(patrocinadorExistente);
        return modelMapper.map(actualizado, PatrocinadorDTO.class);
    }

    @Override
    public PatrocinadorDTO cambiarEstadoPatrocinador(Long id, Boolean activo) {
        Patrocinador patrocinador = patrocinadorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Patrocinador no encontrado"));

        patrocinador.setActivo(activo);
        Patrocinador actualizado = patrocinadorRepository.save(patrocinador);

        return modelMapper.map(actualizado, PatrocinadorDTO.class);
    }

    @Override
    public void eliminarPatrocinador(Long id) {
        Patrocinador patrocinador = patrocinadorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Patrocinador no encontrado"));

        // Verificar si tiene eventos ACTIVOS (no finalizados/cancelados)
        List<Evento> eventosActivos = eventoRepository.findByPatrocinadorId(id).stream()
                .filter(e -> e.getEstado() == EstadoEvento.BORRADOR ||
                        e.getEstado() == EstadoEvento.PUBLICADO ||
                        e.getEstado() == EstadoEvento.EN_CURSO)
                .collect(Collectors.toList());

        if (!eventosActivos.isEmpty()) {
            throw new CustomException("No se puede archivar el patrocinador porque tiene " +
                    eventosActivos.size() +
                    " evento(s) activo(s). Debe finalizar o cancelar los eventos primero.");
        }

        // Soft delete: marcar como inactivo
        patrocinador.setActivo(false);
        patrocinadorRepository.save(patrocinador);
    }
}