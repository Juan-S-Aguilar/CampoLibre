package com.example.campolibre.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsuarioRolDTO {
    private Long id_usuario_rol;
    private Long id_usuario;
    private Long id_rol;
}