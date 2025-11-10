package com.example.campolibre.DTO;

import com.example.campolibre.Enum.EstadoPqrs;
import com.example.campolibre.Enum.TipoPqrs;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PqrsReporteItemDTO {
    private Long id_pqrs;
    private TipoPqrs tipo;
    private String descripcion;
    private LocalDateTime fecha_envio;
    private EstadoPqrs estado;
    private LocalDateTime fecha_respuesta;
    private String respuesta;
    private Long id_emisor;
    private Long id_receptor;
    private String asociacion; // "Tienda: Nombre" / "Evento: Nombre" / "Administración General"
}