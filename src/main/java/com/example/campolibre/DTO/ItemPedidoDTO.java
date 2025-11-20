package com.example.campolibre.DTO;

import com.example.campolibre.Enum.UnidadMedida;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ItemPedidoDTO {
    private Long id_item_pedido;
    private Long id_pedido;
    private Long id_producto;
    private String nombre_producto;
    private String imagen_producto;

    // ✅ NUEVO: Unidad de medida del producto
    private UnidadMedida unidadMedida;

    private Long id_tienda;
    private String nombre_tienda;
    private Integer cantidad;
    private Double precio_unitario;
    private Double subtotal;
}