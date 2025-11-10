package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Pago;
import com.example.campolibre.Enum.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query("SELECT p FROM Pago p WHERE p.pedido.id_pedido = :idPedido")
    Optional<Pago> findByPedidoId(@Param("idPedido") Long idPedido);

    @Query("SELECT p FROM Pago p WHERE p.estado = :estado")
    List<Pago> findByEstado(@Param("estado") EstadoPago estado);

    @Query("SELECT p FROM Pago p WHERE p.numero_transaccion = :numeroTransaccion")
    Optional<Pago> findByNumeroTransaccion(@Param("numeroTransaccion") String numeroTransaccion);
}