package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Producto;
import com.example.campolibre.Enum.CategoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE p.tienda.id_tienda = :idTienda AND p.estado = 'ACTIVO'")
    List<Producto> findByTiendaIdAndEstadoActivo(@Param("idTienda") Long idTienda);

    @Query("SELECT p FROM Producto p WHERE p.tienda.id_tienda = :idTienda")
    List<Producto> findAllByTiendaId(@Param("idTienda") Long idTienda);

    @Query("SELECT p FROM Producto p WHERE p.categoria = :categoria AND p.estado = 'ACTIVO'")
    List<Producto> findByCategoriaAndEstadoActivo(@Param("categoria") CategoriaProducto categoria);

    @Query("SELECT p FROM Producto p WHERE p.estado = 'ACTIVO'")
    List<Producto> findAllActive();

    @Query("SELECT p FROM Producto p WHERE p.estado = 'ELIMINADO'")
    List<Producto> findAllDeleted();

    /**
     * Descuenta stock de forma atómica: solo resta si stock >= cantidad.
     * Retorna número de filas afectadas (1 si se descontó, 0 si no había stock suficiente).
     */
    @Modifying
    @Transactional
    @Query("UPDATE Producto p SET p.stock = p.stock - :cantidad WHERE p.id_producto = :idProducto AND p.stock >= :cantidad")
    int descontarStock(@Param("idProducto") Long idProducto, @Param("cantidad") Integer cantidad);
}
