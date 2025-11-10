package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Pedido;
import com.example.campolibre.Enum.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p WHERE p.usuario.id_usuario = :idUsuario ORDER BY p.fecha_pedido DESC")
    List<Pedido> findByUsuarioId(@Param("idUsuario") Long idUsuario);

    @Query("SELECT p FROM Pedido p WHERE p.estado = :estado ORDER BY p.fecha_pedido DESC")
    List<Pedido> findByEstado(@Param("estado") EstadoPedido estado);

    @Query("SELECT p FROM Pedido p WHERE p.numero_pedido = :numeroPedido")
    Pedido findByNumeroPedido(@Param("numeroPedido") String numeroPedido);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.numero_pedido LIKE :patron")
    Long countByNumeroPedidoPattern(@Param("patron") String patron);

    // Pedidos que contienen productos de una tienda específica
    @Query("SELECT DISTINCT p FROM Pedido p JOIN p.items ip WHERE ip.tienda.id_tienda = :idTienda ORDER BY p.fecha_pedido DESC")
    List<Pedido> findPedidosConProductosDeTienda(@Param("idTienda") Long idTienda);

    // Pedidos pagados que contienen productos de una tienda
    @Query("SELECT DISTINCT p FROM Pedido p JOIN p.items ip WHERE ip.tienda.id_tienda = :idTienda AND p.estado = 'PAGADO' ORDER BY p.fecha_pedido DESC")
    List<Pedido> findPedidosPagadosDeTienda(@Param("idTienda") Long idTienda);
}