package com.example.campolibre.DTO;

import com.example.campolibre.Enum.EstadoPago;
import com.example.campolibre.Enum.MetodoPago;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoEventoDTO {
    private Long idPagoEvento;
    private Long idInscripcion;
    private Double monto;
    private MetodoPago metodoPago;
    private EstadoPago estado;
    private String numeroTransaccion;
    private LocalDateTime fechaPago;
    private String mensajeError;

    // Campos de visualización (para reportes y frontend)
    private String nombreProveedor;
    private String nombreEvento;
    private String codigoConfirmacion; // De la inscripción asociada
}