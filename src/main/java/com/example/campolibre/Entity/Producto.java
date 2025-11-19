package com.example.campolibre.Entity;

import com.example.campolibre.Enum.SubcategoriaProducto;
import com.example.campolibre.Enum.UnidadMedida;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto", nullable = false, unique = true)
    private Long id_producto;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "precio", nullable = false)
    private Double precio;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "subcategoria", nullable = false)
    private SubcategoriaProducto subcategoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedida unidadMedida;

    @ManyToOne
    @JoinColumn(name = "id_tienda", nullable = false)
    private Tienda tienda;

    @Column(name = "imagen_producto", length = 255)
    private String imagen_producto;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fecha_creacion;

    @Column(name = "estado", nullable = false)
    private String estado = "ACTIVO";

    @PrePersist
    protected void onCreate() {
        fecha_creacion = LocalDateTime.now();
    }

    // ========== MÉTODOS HELPER PARA INVENTARIO ==========

    /**
     * Verifica si el producto tiene stock bajo (por debajo del mínimo)
     */
    public boolean tieneStockBajo() {
        return stock > 0 && stock <= stockMinimo;
    }

    /**
     * Verifica si el producto está sin stock
     */
    public boolean sinStock() {
        return stock == 0;
    }

    /**
     * Desactiva el producto automáticamente por falta de stock
     */
    public void desactivarPorSinStock() {
        if (stock == 0) {
            this.estado = "SIN_STOCK";
        }
    }

    /**
     * Reactiva el producto con nuevo stock
     */
    public void reactivarConStock(Integer nuevoStock) {
        if (nuevoStock > 0) {
            this.stock = nuevoStock;
            this.estado = "ACTIVO";
        }
    }

    /**
     * Verifica si el producto está activo
     */
    public boolean estaActivo() {
        return "ACTIVO".equals(this.estado);
    }
}