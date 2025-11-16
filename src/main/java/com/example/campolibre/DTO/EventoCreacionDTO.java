package com.example.campolibre.DTO;

import com.example.campolibre.Enum.TipoEvento;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class EventoCreacionDTO {

    // Datos básicos del evento
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private LocalDate fecha_evento;
    private LocalTime hora_evento;
    private TipoEvento tipo_evento;
    private String direccionCompleta;

    // Se necesita para formularios de EDICIÓN
    private Long id_evento;
    private String imagen_evento;
    private Integer cuposOcupados;

    // --- Nuevos campos de control (gestionados por el Administrador) ---

    // Relación con Patrocinador
    private Long id_patrocinador; // FK al Patrocinador

    // Control de Cupos
    private Integer cuposMaximosProveedor;
    private Double costoEspacio;

    // Términos y Condiciones
    private String terminosCondiciones;

    // ID del Administrador que lo crea (para trazabilidad en el Service)
    private Long id_admin_creador;
}