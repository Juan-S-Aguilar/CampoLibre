package com.example.campolibre.Enum;

public enum EstadoPqrs {
    PENDIENTE, // Pqrs recién creada
    EN_PROCESO, // (Opcional) Asignada a un área interna como Construcción
    RESPONDIDA, // El Proveedor envió la Respuesta 1. El Consumidor debe decidir si replica.
    EN_REPLICA, // El Consumidor ejerció su derecho a réplica.
    CERRADA_ACEPTADA, // El Consumidor aceptó la respuesta o el plazo expiró.
    CERRADA_DEFINITIVA // Cerrada forzosamente después de la Respuesta 2 a la réplica.
}