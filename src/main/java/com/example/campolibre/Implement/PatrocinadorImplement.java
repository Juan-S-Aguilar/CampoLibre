package com.example.campolibre.Implement;

import com.example.campolibre.DTO.PatrocinadorDTO;
import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Entity.Patrocinador;
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
                                 EventoRepository eventoRepository
                                 ) {
        this.patrocinadorRepository = patrocinadorRepository;
        this.modelMapper = modelMapper;
        this.eventoRepository = eventoRepository;
    }

    @Override
    public PatrocinadorDTO crearPatrocinador(PatrocinadorDTO patrocinadorDTO) {
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
    public PatrocinadorDTO actualizarPatrocinador(Long id, PatrocinadorDTO patrocinadorDTO) {
        Patrocinador patrocinadorExistente = patrocinadorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Patrocinador no encontrado"));

        patrocinadorExistente.setNombre(patrocinadorDTO.getNombre());
        patrocinadorExistente.setDescripcion(patrocinadorDTO.getDescripcion());
        patrocinadorExistente.setLogoUrl(patrocinadorDTO.getLogoUrl());
        patrocinadorExistente.setContactoEmail(patrocinadorDTO.getContactoEmail()); // ✅ AGREGAR

        Patrocinador actualizado = patrocinadorRepository.save(patrocinadorExistente);
        return modelMapper.map(actualizado, PatrocinadorDTO.class);
    }

    @Override
    public void eliminarPatrocinador(Long id) {
        if (!patrocinadorRepository.existsById(id)) {
            throw new CustomException("Patrocinador no encontrado");
        }

        // Verificar si tiene eventos asociados
        List<Evento> eventosAsociados = eventoRepository.findByPatrocinadorId(id);
        if (!eventosAsociados.isEmpty()) {
            throw new CustomException("No se puede eliminar el patrocinador porque tiene " +
                    eventosAsociados.size() +
                    " evento(s) asociado(s). Debe reasignar o eliminar los eventos primero.");
        }

        patrocinadorRepository.deleteById(id);
    }
}