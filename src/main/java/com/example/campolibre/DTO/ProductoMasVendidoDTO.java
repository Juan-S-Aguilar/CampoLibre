package com.example.campolibre.DTO;

import lombok.*;

/**
 * DTO para representar productos más vendidos de una tienda
 * Usado en reportes y dashboards de ventas
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductoMasVendidoDTO {
    private String nombreProducto;
    private Long cantidadVendida;
    private Double totalVentas; // Opcional: cantidad * precio promedio
}
