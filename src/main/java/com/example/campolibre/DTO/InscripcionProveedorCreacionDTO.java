package com.example.campolibre.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InscripcionProveedorCreacionDTO {
    private Long id_proveedor; // Usuario proveedor que se inscribe
    private Long id_evento;    // Evento al que se inscribe

    // NOTA:
    // - El costo se obtiene automáticamente del evento
    // - El estado inicial será PENDIENTE_PAGO
    // - La fecha de inscripción se genera automáticamente
}
