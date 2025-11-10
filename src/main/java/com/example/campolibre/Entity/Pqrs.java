package com.example.campolibre.Entity;

import com.example.campolibre.Enum.EstadoPqrs;
import com.example.campolibre.Enum.TipoPqrs;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "pqrs")
@Data
public class Pqrs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pqrs", nullable = false, unique = true)
    private Long id_pqrs;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoPqrs tipo;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fecha_envio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPqrs estado = EstadoPqrs.PENDIENTE;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fecha_respuesta;

    @Column(name = "respuesta", length = 1000)
    private String respuesta;

    @ManyToOne
    @JoinColumn(name = "id_emisor", nullable = false)
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "id_receptor")
    private Usuario receptor;

    @PrePersist
    protected void onCreate() {
        fecha_envio = LocalDateTime.now();
    }
}