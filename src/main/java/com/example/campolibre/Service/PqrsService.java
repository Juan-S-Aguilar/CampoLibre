package com.example.campolibre.Service;

import com.example.campolibre.DTO.PqrsDTO;
import com.example.campolibre.Entity.Pqrs;
import com.example.campolibre.Enum.EstadoPqrs;
import com.example.campolibre.Enum.TipoPqrs;

import java.time.LocalDateTime;
import java.util.List;

public interface PqrsService {
    PqrsDTO crearPqrs(PqrsDTO pqrsDTO);
    PqrsDTO obtenerPqrsPorId(Long id);
    List<PqrsDTO> obtenerTodasLasPqrs();
    List<PqrsDTO> obtenerPqrsPendientes();
    List<PqrsDTO> obtenerPqrsPorEmisor(Long idEmisor);
    List<PqrsDTO> obtenerPqrsPorReceptor(Long idReceptor);
    List<PqrsDTO> obtenerPqrsPorTipo(TipoPqrs tipo);
    PqrsDTO responderPqrs(Long id, String respuesta, Long idReceptor);
    void cambiarEstadoPqrs(Long id, EstadoPqrs estado);
    List<PqrsDTO> obtenerPqrsVisibles(Long idUsuario, boolean esAdmin);
    boolean puedeResponder(Long idPqrs, Long idUsuario, boolean esAdmin);
    List<PqrsDTO> obtenerPqrsPendientesAdmin();
    List<Pqrs> buscarPqrsConFiltros(LocalDateTime fechaDesde, LocalDateTime fechaHasta,
                                    TipoPqrs tipo, EstadoPqrs estado);
}