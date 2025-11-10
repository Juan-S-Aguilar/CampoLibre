package com.example.campolibre.Service;

import com.example.campolibre.DTO.PqrsEventoDTO;
import java.util.List;

public interface PqrsEventoService {
    PqrsEventoDTO crearPqrsEvento(PqrsEventoDTO pqrsEventoDTO);
    List<PqrsEventoDTO> obtenerPqrsPorEvento(Long idEvento);
    PqrsEventoDTO obtenerPorPqrsId(Long idPqrs);
}
