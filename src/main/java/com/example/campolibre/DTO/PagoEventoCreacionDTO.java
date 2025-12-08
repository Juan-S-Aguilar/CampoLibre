package com.example.campolibre.DTO;

import com.example.campolibre.Enum.EntidadBancaria;
import com.example.campolibre.Enum.MetodoPago;
import com.example.campolibre.Enum.TipoPersonaPSE;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoEventoCreacionDTO {
    private Long idInscripcion; // Inscripción a la que pertenece este pago
    private MetodoPago metodoPago; // TARJETA_CREDITO, PSE, etc.

    // ✅ Campos para PSE
    private EntidadBancaria entidadBancaria;
    private TipoPersonaPSE tipoPersonaPSE;
    private String numeroDocumentoPSE;

    // NOTA:
    // - El monto se obtiene automáticamente de la inscripción
    // - El estado inicial será PENDIENTE
    // - El número de transacción se genera automáticamente
    // - La fecha de pago se genera automáticamente
}