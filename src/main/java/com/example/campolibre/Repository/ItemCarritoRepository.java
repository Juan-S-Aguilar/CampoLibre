package com.example.campolibre.Repository;

import com.example.campolibre.Entity.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    @Query("SELECT ic FROM ItemCarrito ic WHERE ic.carrito.id_carrito = :idCarrito AND ic.producto.id_producto = :idProducto")
    Optional<ItemCarrito> findByCarritoIdAndProductoId(@Param("idCarrito") Long idCarrito,
                                                       @Param("idProducto") Long idProducto);

    @Modifying
    @Transactional
    @Query("DELETE FROM ItemCarrito ic WHERE ic.carrito.id_carrito = :idCarrito")
    void deleteByCarritoId(@Param("idCarrito") Long idCarrito);
}
