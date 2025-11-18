package com.example.campolibre.Entity;

import com.example.campolibre.Enum.EstadoCupo;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private Usuario proveedor;

    @ManyToOne
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cupo", nullable = false)
    private EstadoCupo estadoCupo = EstadoCupo.PENDIENTE;

    @Column(name = "costo_pagado", nullable = false)
    private Double costoPagado;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    @OneToOne
    @JoinColumn(name = "id_pago_evento")
    private PagoEvento pagoEvento;

    @Column(name = "codigo_confirmacion", unique = true, length = 20)
    private String codigoConfirmacion;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @PrePersist
    protected void onCreate() {
        fechaInscripcion = LocalDateTime.now();
        generarCodigoConfirmacion();
    }

    private void generarCodigoConfirmacion() {
        if (codigoConfirmacion == null) {
            String yearMonth = LocalDateTime.now().getYear() +
                    String.format("%02d", LocalDateTime.now().getMonthValue());
            String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.codigoConfirmacion = String.format("EVT-%s-%s", yearMonth, uuid);
        }
    }

    // ✅ MÉTODO HELPER PARA CONFIRMAR LA INSCRIPCIÓN (recomendado)
    public void confirmarInscripcion() {
        this.estadoCupo = EstadoCupo.CONFIRMADO;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    // ✅ MÉTODO HELPER PARA CANCELAR LA INSCRIPCIÓN (recomendado)
    public void cancelarInscripcion() {
        this.estadoCupo = EstadoCupo.CANCELADO;
    }

    // ✅ MÉTODO HELPER PARA VERIFICAR SI ESTÁ CONFIRMADA (recomendado)
    public boolean estaConfirmada() {
        return this.estadoCupo == EstadoCupo.CONFIRMADO;
    }

    // ✅ MÉTODO HELPER PARA VERIFICAR SI ESTÁ PENDIENTE (recomendado)
    public boolean estaPendiente() {
        return this.estadoCupo == EstadoCupo.PENDIENTE;
    }
}