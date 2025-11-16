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
public class EventoDTO {
    private Long id_evento;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String direccionCompleta;
    private String ciudad;
    private LocalDate fecha_evento;
    private LocalTime hora_evento;
    private TipoEvento tipo_evento;
    private EstadoEvento estado;

    // Información del creador
    private Long id_creador;
    private String nombreCreador; // Nombre del admin

    private String imagen_evento;
    private LocalDateTime fecha_creacion;
    private LocalDateTime fechaPublicacion;

    // Información del patrocinador
    private Long id_patrocinador;
    private String nombrePatrocinador;
    private String logoPatrocinador; // Para mostrar logo en frontend

    // Control de cupos
    private Integer cuposMaximosProveedor;
    private Integer cuposOcupados;
    private Integer cuposDisponibles; // Calculado en el service
    private Double costoEspacio;

    private String terminosCondiciones;
}