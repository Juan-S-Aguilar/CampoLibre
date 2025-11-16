package com.example.campolibre.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "mis_eventos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_evento"}))
@Data
public class MisEventos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mis_eventos", nullable = false, unique = true)
    private Long id_mis_eventos;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;

    @Column(name = "fecha_guardado", nullable = false)
    private LocalDateTime fecha_guardado;

    @Column(name = "notificado", nullable = false)
    private Boolean notificado = false; // Control de envío de email

    @PrePersist
    protected void onCreate() {
        fecha_guardado = LocalDateTime.now();
    }
}