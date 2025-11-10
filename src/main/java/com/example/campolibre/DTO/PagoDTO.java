package com.example.campolibre.DTO;

import com.example.campolibre.Enum.EstadoPago;
import com.example.campolibre.Enum.MetodoPago;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PagoDTO {
    private Long id_pago;
    private Long id_pedido;
    private Double monto;
    private MetodoPago metodo_pago;
    private EstadoPago estado;
    private String numero_transaccion;
    private LocalDateTime fecha_pago;
    private String mensaje_error;
}