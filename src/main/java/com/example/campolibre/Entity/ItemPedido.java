package com.example.campolibre.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "items_pedidos")
@Data
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item_pedido", nullable = false, unique = true)
    private Long id_item_pedido;

    @ManyToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "id_tienda", nullable = false)
    private Tienda tienda;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precio_unitario;

    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    @PrePersist
    @PreUpdate
    protected void calcularSubtotal() {
        if (cantidad != null && precio_unitario != null) {
            this.subtotal = cantidad * precio_unitario;
        }
    }

    // Constructor helper para crear desde ItemCarrito
    public static ItemPedido crearDesdeItemCarrito(ItemCarrito itemCarrito, Pedido pedido) {
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setPedido(pedido);
        itemPedido.setProducto(itemCarrito.getProducto());
        itemPedido.setTienda(itemCarrito.getProducto().getTienda());
        itemPedido.setCantidad(itemCarrito.getCantidad());
        itemPedido.setPrecio_unitario(itemCarrito.getPrecio_unitario());
        itemPedido.setSubtotal(itemCarrito.getSubtotal());
        return itemPedido;
    }
}