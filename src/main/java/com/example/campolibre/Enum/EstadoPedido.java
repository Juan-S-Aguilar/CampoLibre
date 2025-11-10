package com.example.campolibre.Enum;

public enum EstadoPedido {
    PENDIENTE_PAGO,  // Pedido creado, esperando pago
    PAGADO,          // Pago confirmado y procesado
    CANCELADO        // Pedido cancelado (pago fallido o cancelación manual)
}