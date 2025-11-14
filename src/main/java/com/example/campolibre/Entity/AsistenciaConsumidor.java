package com.example.campolibre.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "asistencias_consumidor",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_evento"}))
@Data
public class AsistenciaConsumidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_asistencia;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario consumidor; // El usuario que es consumidor

    @ManyToOne
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;

    @Column(name = "fecha_registro_asistencia", nullable = false)
    private LocalDateTime fechaRegistroAsistencia; // Hora del check-in

    @PrePersist
    protected void onCreate() {
        fechaRegistroAsistencia = LocalDateTime.now();
    }
}