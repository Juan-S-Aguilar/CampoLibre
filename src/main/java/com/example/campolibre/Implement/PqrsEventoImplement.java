package com.example.campolibre.Implement;

import com.example.campolibre.DTO.PqrsEventoDTO;
import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Entity.Pqrs;
import com.example.campolibre.Entity.PqrsEvento;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.EventoRepository;
import com.example.campolibre.Repository.PqrsEventoRepository;
import com.example.campolibre.Repository.PqrsRepository;
import com.example.campolibre.Service.PqrsEventoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PqrsEventoImplement implements PqrsEventoService {

    private final PqrsEventoRepository pqrsEventoRepository;
    private final PqrsRepository pqrsRepository;
    private final EventoRepository eventoRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public PqrsEventoImplement(PqrsEventoRepository pqrsEventoRepository, PqrsRepository pqrsRepository,
                               EventoRepository eventoRepository, ModelMapper modelMapper) {
        this.pqrsEventoRepository = pqrsEventoRepository;
        this.pqrsRepository = pqrsRepository;
        this.eventoRepository = eventoRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PqrsEventoDTO crearPqrsEvento(PqrsEventoDTO pqrsEventoDTO) {
        Pqrs pqrs = pqrsRepository.findById(pqrsEventoDTO.getId_pqrs())
                .orElseThrow(() -> new CustomException("PQRS no encontrada"));

        Evento evento = eventoRepository.findById(pqrsEventoDTO.getId_evento())
                .orElseThrow(() -> new CustomException("Evento no encontrado"));

        PqrsEvento pqrsEvento = new PqrsEvento();
        pqrsEvento.setPqrs(pqrs);
        pqrsEvento.setEvento(evento);

        PqrsEvento nuevaPqrsEvento = pqrsEventoRepository.save(pqrsEvento);

        PqrsEventoDTO resultado = new PqrsEventoDTO();
        resultado.setId_pqrs_evento(nuevaPqrsEvento.getId_pqrs_evento());
        resultado.setId_pqrs(nuevaPqrsEvento.getPqrs().getId_pqrs());
        resultado.setId_evento(nuevaPqrsEvento.getEvento().getId_evento());

        return resultado;
    }

    @Override
    public List<PqrsEventoDTO> obtenerPqrsPorEvento(Long idEvento) {
        List<PqrsEvento> pqrsEventos = pqrsEventoRepository.findByEventoId(idEvento);
        return pqrsEventos.stream()
                .map(pe -> {
                    PqrsEventoDTO dto = new PqrsEventoDTO();
                    dto.setId_pqrs_evento(pe.getId_pqrs_evento());
                    dto.setId_pqrs(pe.getPqrs().getId_pqrs());
                    dto.setId_evento(pe.getEvento().getId_evento());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PqrsEventoDTO obtenerPorPqrsId(Long idPqrs) {
        PqrsEvento pqrsEvento = pqrsEventoRepository.findByPqrsId(idPqrs);
        if (pqrsEvento == null) {
            return null;
        }

        PqrsEventoDTO resultado = new PqrsEventoDTO();
        resultado.setId_pqrs_evento(pqrsEvento.getId_pqrs_evento());
        resultado.setId_pqrs(pqrsEvento.getPqrs().getId_pqrs());
        resultado.setId_evento(pqrsEvento.getEvento().getId_evento());

        return resultado;
    }
}