package com.example.campolibre.DTO;

import com.example.campolibre.Enum.MetodoPago;
import lombok.*;

/**
 * DTO para recibir datos al confirmar un pedido
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConfirmarPedidoRequest {
    private String nombre_contacto;
    private String telefono_contacto;
    private String direccion_entrega;
    private String ciudad;
    private String notas_adicionales;
    private MetodoPago metodo_pago;
}