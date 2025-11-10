package com.example.campolibre.DTO;

import com.example.campolibre.Enum.CategoriaProducto;
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
    private Long id_tienda;
    private String imagen_producto;
    private LocalDateTime fecha_creacion;
    private String estado = "ACTIVO";
}