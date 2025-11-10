package com.example.campolibre.DTO;

import com.example.campolibre.Enum.NombreRol;
import com.example.campolibre.Enum.TipoDocumento;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsuarioDTO {
    private Long id_usuario;
    private String nombre;
    private String email;
    private String contrasena;
    private String telefono;
    private String documento;
    private TipoDocumento tipo_documento;
    private LocalDateTime fecha_registro;
    private String estado = "ACTIVO";
    private NombreRol rolSeleccionado;
}