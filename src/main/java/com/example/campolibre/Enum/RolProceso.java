package com.example.campolibre.Enum;

public enum RolProceso {
    CONSUMIDOR,  // Quien crea la PQRS o emite la réplica
    PROVEEDOR,   // Quien recibe la PQRS y da la respuesta formal
    CONSTRUCCION, // El rol interno que ejecuta la solución
    NINGUNO      // 💡 CORRECCIÓN: Indica que el proceso ha finalizado y nadie debe actuar.
}