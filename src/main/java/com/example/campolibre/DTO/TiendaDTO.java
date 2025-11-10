package com.example.campolibre.DTO;

import com.example.campolibre.Enum.EstadoTienda;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TiendaDTO {
    private Long id_tienda;
    private String nombre;
    private String descripcion;
    private EstadoTienda estado = EstadoTienda.ACTIVA;
    private String correo_tienda;
    private String telefono_tienda;
    private String ubicacion;
    private Long id_usuario;
    private String imagen_tienda;
    private LocalDateTime fecha_creacion;
}
