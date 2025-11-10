package com.example.campolibre.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MisEventosDTO {
    private Long id_mis_eventos;
    private Long id_usuario;
    private Long id_evento;
    private LocalDateTime fecha_guardado;
}