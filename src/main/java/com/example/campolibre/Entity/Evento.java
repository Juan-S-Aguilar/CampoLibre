package com.example.campolibre.Entity;

import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.TipoEvento;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "eventos")
@Data
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento", nullable = false, unique = true)
    private Long id_evento;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "ubicacion", nullable = false, length = 200)
    private String ubicacion;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDate fecha_evento;

    @Column(name = "hora_evento", nullable = false)
    private LocalTime hora_evento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    private TipoEvento tipo_evento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEvento estado = EstadoEvento.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creado_por;

    @Column(name = "imagen_evento", length = 255)
    private String imagen_evento;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fecha_creacion;

    @PrePersist
    protected void onCreate() {
        fecha_creacion = LocalDateTime.now();
    }
}