package com.example.campolibre.Entity;

import com.example.campolibre.Enum.EntidadBancaria;
import com.example.campolibre.Enum.EstadoPago;
import com.example.campolibre.Enum.MetodoPago;
import com.example.campolibre.Enum.TipoPersonaPSE;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagos_eventos")
@Data
public class PagoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago_evento", nullable = false, unique = true)
    private Long idPagoEvento;

    @OneToOne(mappedBy = "pagoEvento")
    private InscripcionProveedor inscripcionProveedor;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(name = "numero_transaccion", nullable = false, unique = true, length = 50)
    private String numeroTransaccion;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "mensaje_error", length = 500)
    private String mensajeError;

    // ✅ Campos para PSE
    @Enumerated(EnumType.STRING)
    @Column(name = "entidad_bancaria")
    private EntidadBancaria entidadBancaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_persona_pse")
    private TipoPersonaPSE tipoPersonaPSE;

    @Column(name = "numero_documento_pse", length = 20)
    private String numeroDocumentoPSE;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @PrePersist
    protected void onCreate() {
        fechaPago = LocalDateTime.now();
        generarNumeroTransaccion();
    }

    // Genera número único de transacción en formato PEVT-202501-UUID
    private void generarNumeroTransaccion() {
        if (numeroTransaccion == null) {
            String yearMonth = LocalDateTime.now().getYear() +
                    String.format("%02d", LocalDateTime.now().getMonthValue());
            String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.numeroTransaccion = String.format("PEVT-%s-%s", yearMonth, uuid);
        }
    }

    // Método para marcar pago como exitoso
    public void marcarExitoso() {
        this.estado = EstadoPago.EXITOSO;
    }

    // Método para marcar pago como fallido
    public void marcarFallido(String error) {
        this.estado = EstadoPago.FALLIDO;
        this.mensajeError = error;
    }
}