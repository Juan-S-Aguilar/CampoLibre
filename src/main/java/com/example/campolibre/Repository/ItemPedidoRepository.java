package com.example.campolibre.Repository;

import com.example.campolibre.Entity.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    @Query("SELECT ip FROM ItemPedido ip WHERE ip.pedido.id_pedido = :idPedido")
    List<ItemPedido> findByPedidoId(@Param("idPedido") Long idPedido);

    @Query("SELECT ip FROM ItemPedido ip WHERE ip.tienda.id_tienda = :idTienda")
    List<ItemPedido> findByTiendaId(@Param("idTienda") Long idTienda);

    // Total de ventas por tienda (solo pedidos pagados)
    @Query("SELECT SUM(ip.subtotal) FROM ItemPedido ip WHERE ip.tienda.id_tienda = :idTienda AND ip.pedido.estado = 'PAGADO'")
    Double calcularTotalVentasPorTienda(@Param("idTienda") Long idTienda);

    // Productos más vendidos de una tienda
    @Query("SELECT ip.producto.nombre, SUM(ip.cantidad) as total FROM ItemPedido ip " +
            "WHERE ip.tienda.id_tienda = :idTienda AND ip.pedido.estado = 'PAGADO' " +
            "GROUP BY ip.producto.id_producto, ip.producto.nombre " +
            "ORDER BY total DESC")
    List<Object[]> findProductosMasVendidosPorTienda(@Param("idTienda") Long idTienda);
}