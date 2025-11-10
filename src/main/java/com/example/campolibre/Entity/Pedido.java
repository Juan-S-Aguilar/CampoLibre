package com.example.campolibre.Entity;

import com.example.campolibre.Enum.EstadoPedido;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido", nullable = false, unique = true)
    private Long id_pedido;

    @Column(name = "numero_pedido", nullable = false, unique = true, length = 20)
    private String numero_pedido;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE_PAGO;

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fecha_pedido;

    @Column(name = "fecha_pago")
    private LocalDateTime fecha_pago;

    // Datos de contacto y entrega (Opción 2)
    @Column(name = "nombre_contacto", nullable = false, length = 150)
    private String nombre_contacto;

    @Column(name = "telefono_contacto", nullable = false, length = 20)
    private String telefono_contacto;

    @Column(name = "direccion_entrega", nullable = false, length = 300)
    private String direccion_entrega;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "notas_adicionales", length = 500)
    private String notas_adicionales;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> items = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private Pago pago;

    @PrePersist
    protected void onCreate() {
        fecha_pedido = LocalDateTime.now();
        generarNumeroPedido();
    }

    // Genera número de pedido en formato PED-2025-00001
    private void generarNumeroPedido() {
        if (numero_pedido == null) {
            int year = LocalDateTime.now().getYear();
            // El número secuencial se generará en el servicio para garantizar unicidad
            this.numero_pedido = String.format("PED-%d-TEMP", year);
        }
    }

    // Método helper para calcular total
    public void calcularTotal() {
        this.total = items.stream()
                .mapToDouble(ItemPedido::getSubtotal)
                .sum();
    }

    // Método para marcar como pagado
    public void marcarComoPagado() {
        this.estado = EstadoPedido.PAGADO;
        this.fecha_pago = LocalDateTime.now();
    }

    // Método para cancelar pedido
    public void cancelar() {
        this.estado = EstadoPedido.CANCELADO;
    }
}