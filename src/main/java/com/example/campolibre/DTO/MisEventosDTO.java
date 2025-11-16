package com.example.campolibre.DTO;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MisEventosDTO {
    private Long id_mis_eventos;
    private Long id_usuario;
    private Long id_evento;
    private LocalDateTime fecha_guardado;
    private Boolean notificado; // ← AGREGAR (si agregaste el campo en la Entity)

    // Campos de visualización
    private String nombreEvento;
    private String ubicacionEvento;
    private LocalDate fechaEvento;
    private LocalTime horaEvento; // ← AGREGAR (útil para frontend)
    private String imagenEvento;
    private String nombrePatrocinador; // ← AGREGAR (mostrar quién patrocina)
}