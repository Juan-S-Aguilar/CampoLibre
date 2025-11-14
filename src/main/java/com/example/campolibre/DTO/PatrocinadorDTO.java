package com.example.campolibre.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PatrocinadorDTO {
    private Long id_patrocinador;
    private String nombre;
    private String descripcion;
    private String logoUrl;
    private String contactoEmail;
    private LocalDateTime fechaCreacion;
}