package com.example.campolibre.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "items_carrito")
@Data
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item_carrito", nullable = false, unique = true)
    private Long id_item_carrito;

    @ManyToOne
    @JoinColumn(name = "id_carrito", nullable = false)
    private CarritoCompra carrito;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precio_unitario;

    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fecha_agregado;

    @PrePersist
    protected void onCreate() {
        fecha_agregado = LocalDateTime.now();
        calcularSubtotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calcularSubtotal();
    }

    // Método para calcular y actualizar el subtotal
    public void calcularSubtotal() {
        if (cantidad != null && precio_unitario != null) {
            this.subtotal = cantidad * precio_unitario;
        }
    }

    // Método para validar stock disponible
    public boolean stockDisponible() {
        return producto != null &&
                producto.getStock() != null &&
                producto.getStock() >= cantidad;
    }
}