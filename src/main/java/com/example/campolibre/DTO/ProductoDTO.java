package com.example.campolibre.DTO;

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
    private Integer stockMinimo = 5;  // ✅ Valor por defecto en clase
    private SubcategoriaProducto subcategoria;
    private UnidadMedida unidadMedida;
    private Long id_tienda;
    private String imagen_producto;
    private LocalDateTime fecha_creacion;
    private String estado = "ACTIVO";  // ✅ Valor por defecto en clase

    // Campos calculados para la vista
    private Boolean tieneStockBajo;
    private Boolean sinStock;
}
