package com.example.campolibre.DTO;

import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.TipoEvento;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventoDTO {
    private Long id_evento;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private LocalDate fecha_evento;
    private LocalTime hora_evento;
    private TipoEvento tipo_evento;
    private EstadoEvento estado = EstadoEvento.PENDIENTE;
    private Long creado_por;
    private String imagen_evento;
    private LocalDateTime fecha_creacion;
}