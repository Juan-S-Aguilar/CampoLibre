package com.example.campolibre.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AsistenciaConsumidorDTO {
    private Long id_asistencia;

    // IDs de las entidades relacionadas
    private Long id_consumidor;
    private Long id_evento;

    private LocalDateTime fechaRegistroAsistencia;

    // Campos de visualización (opcional)
    private String nombreEvento;
    private String nombreConsumidor;
}