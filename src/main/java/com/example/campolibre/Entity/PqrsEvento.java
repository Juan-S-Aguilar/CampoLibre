package com.example.campolibre.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pqrs_eventos")
@Data
public class PqrsEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pqrs_evento", nullable = false, unique = true)
    private Long id_pqrs_evento;

    @ManyToOne
    @JoinColumn(name = "id_pqrs", nullable = false)
    private Pqrs pqrs;

    @ManyToOne
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;
}