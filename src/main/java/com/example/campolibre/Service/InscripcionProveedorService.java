package com.example.campolibre.Service;

import com.example.campolibre.DTO.InscripcionProveedorDTO; // Tendrás que crear este DTO
import java.util.List;

public interface InscripcionProveedorService {

    // Proveedor: Solicita un cupo y se genera una inscripción en estado PENDIENTE_PAGO.
    InscripcionProveedorDTO solicitarCupo(Long idProveedor, Long idEvento);

    // Sistema de Pago/WebHook: Confirma el pago y cambia el estado del cupo.
    InscripcionProveedorDTO confirmarPago(Long idInscripcion, Long idPagoSistemaExterno);

    // Lógica de Negocio: Verifica si hay cupos disponibles.
    boolean hayCuposDisponibles(Long idEvento);

    // Proveedor: Ve sus eventos confirmados (Mis Eventos Confirmados)
    List<InscripcionProveedorDTO> obtenerEventosConfirmadosDeProveedor(Long idProveedor);

    // Administrador: Reporte de proveedores inscritos a un evento.
    List<InscripcionProveedorDTO> obtenerInscripcionesPorEvento(Long idEvento);

    // Lógica de Negocio: Obtener un cupo específico (para pago o verificación)
    InscripcionProveedorDTO obtenerInscripcionPorId(Long idInscripcion);

    boolean proveedorEstaInscrito(Long idProveedor, Long idEvento);
}