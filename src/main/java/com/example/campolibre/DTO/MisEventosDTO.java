package com.example.campolibre.DTO;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class MisEventosDTO {
    private Long id_mis_eventos;
    private Long id_usuario;
    private Long id_evento;
    private LocalDateTime fecha_guardado;

    // Campos de visualización (opcional)
    private String nombreEvento;
    private String ubicacionEvento;
    private LocalDate fechaEvento;
    private String imagenEvento;
}