package com.example.campolibre.Entity;

import com.example.campolibre.Enum.EstadoCupo; // Tendremos que crear este Enum
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscripciones_proveedor",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_evento"}))
@Data
public class InscripcionProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_inscripcion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario proveedor; // El usuario que es proveedor

    @ManyToOne
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cupo", nullable = false)
    private EstadoCupo estadoCupo; // PENDIENTE_PAGO, CONFIRMADO, CANCELADO

    @Column(name = "costo_pagado", nullable = false)
    private Double costoPagado; // El valor con el que se confirmó

    @OneToOne
    @JoinColumn(name = "id_pago")
    private Pago pago; // FK a la entidad Pago para trazabilidad financiera

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    @PrePersist
    protected void onCreate() {
        fechaInscripcion = LocalDateTime.now();
    }
}