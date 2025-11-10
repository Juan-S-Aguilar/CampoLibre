package com.example.campolibre.DTO;

import com.example.campolibre.Enum.EstadoPedido;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PedidoDTO {
    private Long id_pedido;
    private String numero_pedido;
    private Long id_usuario;
    private String nombre_usuario;
    private EstadoPedido estado;
    private Double total;
    private LocalDateTime fecha_pedido;
    private LocalDateTime fecha_pago;

    // Datos de contacto y entrega
    private String nombre_contacto;
    private String telefono_contacto;
    private String direccion_entrega;
    private String ciudad;
    private String notas_adicionales;

    private List<ItemPedidoDTO> items = new ArrayList<>();
    private PagoDTO pago;
}