package com.example.campolibre.DTO;

import com.example.campolibre.Enum.EstadoCupo;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InscripcionProveedorDTO {
    private Long id_inscripcion;
    private Long id_proveedor;
    private Long id_evento;
    private EstadoCupo estadoCupo;
    private Double costoPagado;
    private Long id_pago_evento;
    private LocalDateTime fechaInscripcion;
    private String codigoConfirmacion;
    // Campos de visualización (para el frontend)
    private String nombreProveedor;
    private String nombreEvento;
    private LocalDate fechaEvento;
    private String ubicacionEvento;
    private String imagenEvento;
}