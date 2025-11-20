package com.example.campolibre.Enum;

public enum EstadoCupo {
    PENDIENTE, // Cupo reservado, esperando confirmación de pago
    CONFIRMADO,     // Pago realizado y cupo asegurado
    CANCELADO       // Inscripción cancelada o pago fallido
}