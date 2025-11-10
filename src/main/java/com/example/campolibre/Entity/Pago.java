package com.example.campolibre.Entity;

import com.example.campolibre.Enum.EstadoPago;
import com.example.campolibre.Enum.MetodoPago;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagos")
@Data
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago", nullable = false, unique = true)
    private Long id_pago;

    @OneToOne
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodo_pago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(name = "numero_transaccion", nullable = false, unique = true, length = 50)
    private String numero_transaccion;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fecha_pago;

    @Column(name = "mensaje_error", length = 500)
    private String mensaje_error;

    @PrePersist
    protected void onCreate() {
        fecha_pago = LocalDateTime.now();
        generarNumeroTransaccion();
    }

    // Genera número único de transacción en formato TRX-202501-UUID
    private void generarNumeroTransaccion() {
        if (numero_transaccion == null) {
            String yearMonth = LocalDateTime.now().getYear() +
                    String.format("%02d", LocalDateTime.now().getMonthValue());
            String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.numero_transaccion = String.format("TRX-%s-%s", yearMonth, uuid);
        }
    }

    // Método para marcar pago como exitoso
    public void marcarExitoso() {
        this.estado = EstadoPago.EXITOSO;
    }

    // Método para marcar pago como fallido
    public void marcarFallido(String error) {
        this.estado = EstadoPago.FALLIDO;
        this.mensaje_error = error;
    }
}