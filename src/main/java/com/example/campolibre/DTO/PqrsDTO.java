package com.example.campolibre.DTO;

import com.example.campolibre.Enum.EstadoPqrs;
import com.example.campolibre.Enum.TipoPqrs;
import com.example.campolibre.Enum.RolProceso;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PqrsDTO {
    private Long id_pqrs;
    private TipoPqrs tipo;
    private String descripcion;
    private LocalDateTime fecha_envio;

    // Estado de la PQRS (PENDIENTE, RESPONDIDA, EN_REPLICA, CERRADA_DEFINITIVA, etc.)
    private EstadoPqrs estado = EstadoPqrs.PENDIENTE;

    // 💡 NUEVO CAMPO: Indica quién debe realizar la siguiente acción.
    private RolProceso pendienteDe;

    // ----------------------------------------------------
    // ✅ INFORMACIÓN DE LA ÚLTIMA INTERACCIÓN (Historial)
    // Estos campos serán poblados por el Service (mapToDto) buscando en PqrsRespuesta.

    // Contiene el contenido de la última respuesta o réplica. (Se mantiene)
    private String respuesta;

    // 💡 CAMBIO: Añadimos la fecha de la última respuesta.
    private LocalDateTime fecha_respuesta;

    // ----------------------------------------------------

    private Long id_emisor;
    private Long id_receptor;
    private Long id_tienda;
    private Long id_evento;

    // Campos de control de la vista (se mantienen)
    private Boolean puede_responder = false;
    private Boolean puede_replicar = false;
    private Boolean puede_cerrar = false;
}
