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

    // ----------------------------------------------------------------------
    // MÉTODOS DE TRAZABILIDAD ROBUSTA (Reemplazan a responderPqrs)
    // ----------------------------------------------------------------------

    /**
     * Registra la respuesta (oficial) del Proveedor (Respuesta 1 o Respuesta 2 a la réplica).
     * Mueve el estado a RESPONDIDA o CERRADA_DEFINITIVA.
     */
    PqrsDTO registrarRespuesta(Long idPqrs, String contenido, Long idReceptor);

    /**
     * Registra la réplica (apelación) del Consumidor.
     * Mueve el estado a EN_REPLICA.
     */
    PqrsDTO registrarReplica(Long idPqrs, String contenido, Long idEmisor);

    /**
     * Cierra la PQRS de forma explícita por el consumidor (aceptación)
     * o por el sistema (vencimiento/finalización del proceso).
     * Mueve el estado a CERRADA_ACEPTADA.
     */
    PqrsDTO cerrarPqrsPorConsumidor(Long idPqrs, Long idUsuario);

    // ----------------------------------------------------------------------
    // MÉTODOS ORIGINALES (Se mantienen)
    // ----------------------------------------------------------------------

    // Eliminado: PqrsDTO responderPqrs(Long id, String respuesta, Long idReceptor);
    // Ahora usar: registrarRespuesta()

    // Este método queda para otros cambios de estado, pero la lógica de respuesta/réplica usa los nuevos.
    void cambiarEstadoPqrs(Long id, EstadoPqrs estado);

    List<PqrsDTO> obtenerPqrsVisibles(Long idUsuario, boolean esAdmin);
    boolean puedeResponder(Long idPqrs, Long idUsuario, boolean esAdmin);
    List<PqrsDTO> obtenerPqrsPendientesAdmin();
    List<Pqrs> buscarPqrsConFiltros(LocalDateTime fechaDesde, LocalDateTime fechaHasta,
                                    TipoPqrs tipo, EstadoPqrs estado);
}