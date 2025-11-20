package com.example.campolibre.Service;

import com.example.campolibre.DTO.PagoEventoDTO;
import com.example.campolibre.DTO.PagoEventoCreacionDTO;
import com.example.campolibre.DTO.PagoEventoEstadisticasDTO;
import com.example.campolibre.Enum.EstadoPago;
import java.util.List;

public interface PagoEventoService {

    /**
     * Crea un nuevo pago para una inscripción de proveedor.
     * Estado inicial: PENDIENTE
     */
    PagoEventoDTO crearPago(PagoEventoCreacionDTO pagoCreacionDTO);

    /**
     * Procesa el pago (simulación o integración con pasarela real).
     * Cambia el estado a EXITOSO o FALLIDO.
     */
    PagoEventoDTO procesarPago(Long idPagoEvento);

    /**
     * Marca un pago como exitoso (llamado desde webhook de pasarela).
     */
    PagoEventoDTO marcarPagoExitoso(Long idPagoEvento);

    /**
     * Marca un pago como fallido (llamado desde webhook de pasarela).
     */
    PagoEventoDTO marcarPagoFallido(Long idPagoEvento, String mensajeError);

    /**
     * Obtiene un pago por su ID.
     */
    PagoEventoDTO obtenerPagoPorId(Long idPagoEvento);

    /**
     * Busca un pago por número de transacción (útil para webhooks).
     */
    PagoEventoDTO obtenerPagoPorNumeroTransaccion(String numeroTransaccion);

    /**
     * Obtiene todos los pagos de un proveedor.
     */
    List<PagoEventoDTO> obtenerPagosPorProveedor(Long idProveedor);

    /**
     * Obtiene todos los pagos de un evento (para reportes).
     */
    List<PagoEventoDTO> obtenerPagosPorEvento(Long idEvento);

    /**
     * Obtiene pagos por estado (PENDIENTE, EXITOSO, FALLIDO).
     */
    List<PagoEventoDTO> obtenerPagosPorEstado(EstadoPago estado);

    /**
     * Calcula el total recaudado de un evento (solo pagos exitosos).
     */
    Double calcularTotalRecaudadoPorEvento(Long idEvento);

    /**
     * Obtiene estadísticas de pagos de un evento.
     */
    PagoEventoEstadisticasDTO obtenerEstadisticasPagosEvento(Long idEvento);
}