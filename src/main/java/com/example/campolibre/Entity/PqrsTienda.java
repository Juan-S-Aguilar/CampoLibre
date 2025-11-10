package com.example.campolibre.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pqrs_tiendas")
@Data
public class PqrsTienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pqrs_tienda", nullable = false, unique = true)
    private Long id_pqrs_tienda;

    @ManyToOne
    @JoinColumn(name = "id_pqrs", nullable = false)
    private Pqrs pqrs;

    @ManyToOne
    @JoinColumn(name = "id_tienda", nullable = false)
    private Tienda tienda;
}