package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Producto;
import com.example.campolibre.Enum.SubcategoriaProducto;
import com.example.campolibre.Enum.UnidadMedida;
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

    // ========== NUEVAS QUERIES - BÚSQUEDA POR SUBCATEGORÍA ==========

    @Query("SELECT p FROM Producto p WHERE p.subcategoria = :subcategoria AND p.estado = 'ACTIVO'")
    List<Producto> findBySubcategoriaAndEstadoActivo(@Param("subcategoria") SubcategoriaProducto subcategoria);

    @Query("SELECT p FROM Producto p WHERE p.subcategoria = :subcategoria")
    List<Producto> findBySubcategoria(@Param("subcategoria") SubcategoriaProducto subcategoria);

    // ========== NUEVAS QUERIES - BÚSQUEDA POR UNIDAD DE MEDIDA ==========

    @Query("SELECT p FROM Producto p WHERE p.unidadMedida = :unidad AND p.estado = 'ACTIVO'")
    List<Producto> findByUnidadMedidaAndEstadoActivo(@Param("unidad") UnidadMedida unidad);

    // ========== NUEVAS QUERIES - GESTIÓN DE INVENTARIO ==========

    @Query("SELECT p FROM Producto p WHERE p.tienda.id_tienda = :idTienda " +
            "AND p.stock > 0 AND p.stock <= p.stockMinimo")
    List<Producto> findProductosConStockBajo(@Param("idTienda") Long idTienda);

    @Query("SELECT p FROM Producto p WHERE p.tienda.id_tienda = :idTienda " +
            "AND p.stock = :stock")
    List<Producto> findByTiendaIdAndStock(
            @Param("idTienda") Long idTienda,
            @Param("stock") Integer stock
    );

    @Query("SELECT p FROM Producto p WHERE p.tienda.id_tienda = :idTienda " +
            "AND p.estado = :estado")
    List<Producto> findByTiendaIdAndEstado(
            @Param("idTienda") Long idTienda,
            @Param("estado") String estado
    );

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.tienda.id_tienda = :idTienda " +
            "AND p.stock > 0 AND p.stock <= p.stockMinimo")
    Long contarProductosConStockBajo(@Param("idTienda") Long idTienda);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.tienda.id_tienda = :idTienda " +
            "AND p.stock = 0")
    Long contarProductosSinStock(@Param("idTienda") Long idTienda);

    // ========== QUERIES COMBINADAS - FILTROS AVANZADOS ==========

    @Query("SELECT p FROM Producto p WHERE p.tienda.categoriaPrincipal = :categoria " +
            "AND p.subcategoria = :subcategoria AND p.estado = 'ACTIVO'")
    List<Producto> findByCategoriaYSubcategoriaActivos(
            @Param("categoria") com.example.campolibre.Enum.CategoriaTienda categoria,
            @Param("subcategoria") SubcategoriaProducto subcategoria
    );

    @Query("SELECT p FROM Producto p WHERE p.subcategoria = :subcategoria " +
            "AND p.unidadMedida = :unidad AND p.estado = 'ACTIVO'")
    List<Producto> findBySubcategoriaYUnidadActivos(
            @Param("subcategoria") SubcategoriaProducto subcategoria,
            @Param("unidad") UnidadMedida unidad
    );
}