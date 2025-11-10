package com.example.campolibre.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos_compra")
@Data
public class CarritoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito", nullable = false, unique = true)
    private Long id_carrito;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrito> items = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fecha_creacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fecha_actualizacion;

    @PrePersist
    protected void onCreate() {
        fecha_creacion = LocalDateTime.now();
        fecha_actualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fecha_actualizacion = LocalDateTime.now();
    }

    // Método helper para calcular el total del carrito
    public Double calcularTotal() {
        return items.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();
    }

    // Método helper para obtener cantidad total de items
    public Integer getCantidadTotalItems() {
        return items.stream()
                .mapToInt(ItemCarrito::getCantidad)
                .sum();
    }
}