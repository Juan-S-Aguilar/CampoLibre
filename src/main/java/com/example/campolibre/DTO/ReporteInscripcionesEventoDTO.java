package com.example.campolibre.DTO;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteInscripcionesEventoDTO {

    // ===== INFORMACIÓN DEL EVENTO =====
    private Long eventoId;
    private String nombreEvento;
    private String ubicacionEvento;
    private LocalDate fechaEvento;
    private LocalTime horaEvento;
    private String nombrePatrocinador;

    // ===== ESTADÍSTICAS DE CUPOS =====
    private Integer cuposTotales;
    private Integer cuposOcupados;
    private Integer cuposDisponibles;

    // ===== ESTADÍSTICAS DE INSCRIPCIONES =====
    private Long inscripcionesConfirmadas;
    private Long inscripcionesPendientes;
    private Long inscripcionesCanceladas;

    // ===== ESTADÍSTICAS FINANCIERAS =====
    private Double totalRecaudado;
    private Double costoEspacio;

    // ===== LISTA DE PROVEEDORES INSCRITOS =====
    private List<InscripcionProveedorDTO> proveedoresInscritos;
}