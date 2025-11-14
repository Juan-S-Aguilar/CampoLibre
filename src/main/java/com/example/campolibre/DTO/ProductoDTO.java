package com.example.campolibre.DTO;

import com.example.campolibre.Enum.CategoriaProducto;
import com.example.campolibre.Enum.SubcategoriaProducto;
import com.example.campolibre.Enum.UnidadMedida;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductoDTO {
    private Long id_producto;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private CategoriaProducto categoria;
    private SubcategoriaProducto subcategoria;
    private Double cantidad;
    private UnidadMedida unidadMedida;
    private Long id_tienda;
    private String nombre_tienda;
    private String imagen_producto;
    private LocalDateTime fecha_creacion;
    private LocalDateTime fecha_actualizacion;
    private String estado = "ACTIVO";
    // Método auxiliar para validar que la subcategoría pertenezca a la categoría
    public boolean esSubcategoriaValida() {
        if (categoria == null || subcategoria == null) {
            return false;
        }
        return subcategoria.getCategoriaProducto() == categoria;
    }

    // Método auxiliar para obtener la cantidad formateada
    public String getCantidadFormateada() {
        if (cantidad == null || unidadMedida == null) {
            return "N/A";
        }
        return unidadMedida.formatearConCantidad(cantidad);
    }
}
