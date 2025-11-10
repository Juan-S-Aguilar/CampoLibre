package com.example.campolibre.Service;

import com.example.campolibre.DTO.PagoDTO;
import com.example.campolibre.Enum.MetodoPago;

public interface PagoService {

    /**
     * Procesa el pago de un pedido (simulado)
     */
    PagoDTO procesarPago(Long idPedido, MetodoPago metodoPago);

    /**
     * Obtiene información de un pago por ID de pedido
     */
    PagoDTO obtenerPagoPorPedido(Long idPedido);

    /**
     * Obtiene información de un pago por número de transacción
     */
    PagoDTO obtenerPagoPorNumeroTransaccion(String numeroTransaccion);

    /**
     * Simula el procesamiento de pago externo
     * Retorna true si el pago fue exitoso, false si falló
     */
    boolean simularPasarelaPago(MetodoPago metodoPago, Double monto);
}