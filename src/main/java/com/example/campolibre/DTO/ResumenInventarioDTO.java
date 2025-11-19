package com.example.campolibre.DTO;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResumenInventarioDTO {
    private Long idTienda;
    private String nombreTienda;
    private Integer totalProductos = 0;
    private Integer productosActivos = 0;
    private Integer productosConStockBajo = 0;
    private Integer productosSinStock = 0;
    private Integer productosInactivos = 0;
    private Double valorTotalInventario = 0.0;
    private List<ProductoDTO> productosAlerta = new ArrayList<>();
}