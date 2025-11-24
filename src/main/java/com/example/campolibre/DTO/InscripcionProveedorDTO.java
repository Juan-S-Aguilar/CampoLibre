package com.example.campolibre.DTO;

import com.example.campolibre.Enum.EstadoCupo;
import com.example.campolibre.Enum.EstadoPago;
import com.example.campolibre.Enum.MetodoPago;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InscripcionProveedorDTO {

    // ========== CAMPOS ORIGINALES (YA LOS TENÍAS) ==========
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

    // ========== NUEVOS CAMPOS PARA EL REPORTE ==========
    // Info adicional del proveedor
    private String emailProveedor;
    private String documentoProveedor;
    private String telefonoProveedor;

    // Info del pago
    private EstadoPago estadoPago;
    private MetodoPago metodoPago;
    private String numeroTransaccion;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaConfirmacion;
}