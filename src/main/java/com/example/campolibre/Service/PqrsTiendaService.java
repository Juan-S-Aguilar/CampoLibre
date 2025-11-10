package com.example.campolibre.Service;

import com.example.campolibre.DTO.PqrsTiendaDTO;
import java.util.List;

public interface PqrsTiendaService {
    PqrsTiendaDTO crearPqrsTienda(PqrsTiendaDTO pqrsTiendaDTO);
    List<PqrsTiendaDTO> obtenerPqrsPorTienda(Long idTienda);
    PqrsTiendaDTO obtenerPorPqrsId(Long idPqrs);
}