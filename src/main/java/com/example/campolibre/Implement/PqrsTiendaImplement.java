package com.example.campolibre.Implement;

import com.example.campolibre.DTO.PqrsTiendaDTO;
import com.example.campolibre.Entity.Pqrs;
import com.example.campolibre.Entity.PqrsTienda;
import com.example.campolibre.Entity.Tienda;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.PqrsRepository;
import com.example.campolibre.Repository.PqrsTiendaRepository;
import com.example.campolibre.Repository.TiendaRepository;
import com.example.campolibre.Service.PqrsTiendaService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PqrsTiendaImplement implements PqrsTiendaService {

    private final PqrsTiendaRepository pqrsTiendaRepository;
    private final PqrsRepository pqrsRepository;
    private final TiendaRepository tiendaRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public PqrsTiendaImplement(PqrsTiendaRepository pqrsTiendaRepository, PqrsRepository pqrsRepository,
                               TiendaRepository tiendaRepository, ModelMapper modelMapper) {
        this.pqrsTiendaRepository = pqrsTiendaRepository;
        this.pqrsRepository = pqrsRepository;
        this.tiendaRepository = tiendaRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PqrsTiendaDTO crearPqrsTienda(PqrsTiendaDTO pqrsTiendaDTO) {
        Pqrs pqrs = pqrsRepository.findById(pqrsTiendaDTO.getId_pqrs())
                .orElseThrow(() -> new CustomException("PQRS no encontrada"));

        Tienda tienda = tiendaRepository.findById(pqrsTiendaDTO.getId_tienda())
                .orElseThrow(() -> new CustomException("Tienda no encontrada"));

        PqrsTienda pqrsTienda = new PqrsTienda();
        pqrsTienda.setPqrs(pqrs);
        pqrsTienda.setTienda(tienda);

        PqrsTienda nuevaPqrsTienda = pqrsTiendaRepository.save(pqrsTienda);

        PqrsTiendaDTO resultado = new PqrsTiendaDTO();
        resultado.setId_pqrs_tienda(nuevaPqrsTienda.getId_pqrs_tienda());
        resultado.setId_pqrs(nuevaPqrsTienda.getPqrs().getId_pqrs());
        resultado.setId_tienda(nuevaPqrsTienda.getTienda().getId_tienda());

        return resultado;
    }

    @Override
    public List<PqrsTiendaDTO> obtenerPqrsPorTienda(Long idTienda) {
        List<PqrsTienda> pqrsTiendas = pqrsTiendaRepository.findByTiendaId(idTienda);
        return pqrsTiendas.stream()
                .map(pt -> {
                    PqrsTiendaDTO dto = new PqrsTiendaDTO();
                    dto.setId_pqrs_tienda(pt.getId_pqrs_tienda());
                    dto.setId_pqrs(pt.getPqrs().getId_pqrs());
                    dto.setId_tienda(pt.getTienda().getId_tienda());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PqrsTiendaDTO obtenerPorPqrsId(Long idPqrs) {
        PqrsTienda pqrsTienda = pqrsTiendaRepository.findByPqrsId(idPqrs);
        if (pqrsTienda == null) {
            return null;
        }

        PqrsTiendaDTO resultado = new PqrsTiendaDTO();
        resultado.setId_pqrs_tienda(pqrsTienda.getId_pqrs_tienda());
        resultado.setId_pqrs(pqrsTienda.getPqrs().getId_pqrs());
        resultado.setId_tienda(pqrsTienda.getTienda().getId_tienda());

        return resultado;
    }
}