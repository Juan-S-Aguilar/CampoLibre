package com.example.campolibre.DTO;

import com.example.campolibre.Enum.NombreRol;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RolDTO {
    private Long id_rol;
    private NombreRol nombre_rol;
}