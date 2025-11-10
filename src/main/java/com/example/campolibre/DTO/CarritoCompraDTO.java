package com.example.campolibre.DTO;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CarritoCompraDTO {
    private Long id_carrito;
    private Long id_usuario;
    private List<ItemCarritoDTO> items = new ArrayList<>();
    private Double total;
    private Integer cantidad_total_items;
    private LocalDateTime fecha_creacion;
    private LocalDateTime fecha_actualizacion;
}