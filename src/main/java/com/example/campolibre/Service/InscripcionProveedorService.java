package com.example.campolibre.Service;

import com.example.campolibre.DTO.InscripcionProveedorDTO;
import com.example.campolibre.DTO.InscripcionProveedorCreacionDTO;
import java.util.List;

public interface InscripcionProveedorService {

    // Proveedor: Solicita un cupo (crea inscripción en PENDIENTE_PAGO)
    InscripcionProveedorDTO solicitarCupo(Long idProveedor, Long idEvento);

    // Sistema de Pago: Confirma el pago y cambia estado a CONFIRMADO
    // CAMBIO: Mejor recibir idPagoEvento en lugar de "idPagoSistemaExterno"
    InscripcionProveedorDTO confirmarPago(Long idInscripcion, Long idPagoEvento);

    // NUEVO: Cancelar inscripción (si pago falla o proveedor cancela)
    void cancelarInscripcion(Long idInscripcion, String motivo);

    // Verificar si hay cupos disponibles
    boolean hayCuposDisponibles(Long idEvento);

    // NUEVO: Obtener cupos disponibles (número exacto)
    Integer obtenerCuposDisponibles(Long idEvento);

    // Proveedor: Ver sus eventos confirmados
    List<InscripcionProveedorDTO> obtenerEventosConfirmadosDeProveedor(Long idProveedor);

    // NUEVO: Proveedor: Ver TODAS sus inscripciones (confirmadas, pendientes, canceladas)
    List<InscripcionProveedorDTO> obtenerTodasLasInscripcionesDeProveedor(Long idProveedor);

    // Administrador: Reporte de proveedores inscritos a un evento
    List<InscripcionProveedorDTO> obtenerInscripcionesPorEvento(Long idEvento);

    // Obtener inscripción específica
    InscripcionProveedorDTO obtenerInscripcionPorId(Long idInscripcion);

    // NUEVO: Buscar inscripción por código de confirmación
    InscripcionProveedorDTO obtenerInscripcionPorCodigo(String codigoConfirmacion);

    // Verificar si proveedor ya está inscrito
    boolean proveedorEstaInscrito(Long idProveedor, Long idEvento);
}