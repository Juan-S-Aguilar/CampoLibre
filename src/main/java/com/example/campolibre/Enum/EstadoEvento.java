package com.example.campolibre.Enum;

public enum EstadoEvento {
    BORRADOR,      // En creación
    PUBLICADO,     // Visible para proveedores/consumidores
    EN_CURSO,      // Evento activo (día del evento)
    FINALIZADO,    // Evento terminado
    CANCELADO      // Evento cancelado
}