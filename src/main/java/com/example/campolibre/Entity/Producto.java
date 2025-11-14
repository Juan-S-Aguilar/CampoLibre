package com.example.campolibre.Entity;

import com.example.campolibre.Enum.CategoriaProducto;
import com.example.campolibre.Enum.SubcategoriaProducto;
import com.example.campolibre.Enum.UnidadMedida;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_producto;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CategoriaProducto categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private SubcategoriaProducto subcategoria;

    @Column(nullable = false)
    private Double cantidad; // Ejemplo: 1.5, 5, 10, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UnidadMedida unidadMedida;

    @Column(length = 500)
    private String imagen_producto;

    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO"; // ACTIVO, INACTIVO, ELIMINADO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tienda", nullable = false)
    private Tienda tienda;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha_creacion;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fecha_actualizacion;

    // Métodos de utilidad para obtener el id de la tienda
    @Transient
    public Long getId_tienda() {
        return tienda != null ? tienda.getId_tienda() : null;
    }

    /**
     * Devuelve la cantidad con su unidad de medida formateada
     * Ejemplo: "1.5 kg", "10 unidades"
     */
    @Transient
    public String getCantidadFormateada() {
        if (cantidad == null || unidadMedida == null) {
            return "N/A";
        }
        return unidadMedida.formatearConCantidad(cantidad);
    }

    /**
     * Verifica si el producto tiene stock disponible
     */
    @Transient
    public boolean tieneStock() {
        return stock != null && stock > 0;
    }

    /**
     * Verifica si el producto está activo
     */
    @Transient
    public boolean estaActivo() {
        return "ACTIVO".equals(estado);
    }
}