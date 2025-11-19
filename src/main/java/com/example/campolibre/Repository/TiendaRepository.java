package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Tienda;
import com.example.campolibre.Enum.CategoriaTienda;
import com.example.campolibre.Enum.EstadoTienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, Long> {

    // ========== QUERIES BÁSICAS ==========

    @Query("SELECT t FROM Tienda t WHERE t.usuario.id_usuario = :idUsuario")
    List<Tienda> findByUsuarioId(@Param("idUsuario") Long idUsuario);

    @Query("SELECT t FROM Tienda t WHERE t.usuario.id_usuario = :idUsuario AND t.estado = :estado")
    List<Tienda> findByUsuarioIdAndEstado(@Param("idUsuario") Long idUsuario, @Param("estado") EstadoTienda estado);

    @Query("SELECT t FROM Tienda t WHERE t.estado = 'ACTIVA'")
    List<Tienda> findAllActive();

    // ========== NUEVO MÉTODO - BÚSQUEDA POR NOMBRE ==========

    /**
     * Busca tiendas por nombre exacto
     * Usado principalmente en el DataSeeder
     */
    @Query("SELECT t FROM Tienda t WHERE t.nombre = :nombre")
    List<Tienda> findByNombre(@Param("nombre") String nombre);

    /**
     * Busca tiendas por nombre parcial (útil para búsquedas)
     * Ejemplo: "Finca" encontrará "Finca El Paraíso"
     */
    @Query("SELECT t FROM Tienda t WHERE LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Tienda> findByNombreContaining(@Param("nombre") String nombre);

    // ========== QUERIES POR ESTADO ==========

    @Query("SELECT t FROM Tienda t WHERE t.estado = :estado")
    List<Tienda> findByEstado(@Param("estado") EstadoTienda estado);

    // ========== QUERIES POR CATEGORÍA ==========

    /**
     * Obtiene tiendas activas por categoría principal
     */
    @Query("SELECT t FROM Tienda t WHERE t.categoriaPrincipal = :categoria AND t.estado = 'ACTIVA'")
    List<Tienda> findByCategoriaPrincipalAndEstadoActivo(@Param("categoria") CategoriaTienda categoria);

    /**
     * Obtiene todas las tiendas por categoría (sin filtrar por estado)
     */
    @Query("SELECT t FROM Tienda t WHERE t.categoriaPrincipal = :categoria")
    List<Tienda> findByCategoriaPrincipal(@Param("categoria") CategoriaTienda categoria);

    /**
     * Obtiene todas las categorías que tienen tiendas activas
     */
    @Query("SELECT DISTINCT t.categoriaPrincipal FROM Tienda t WHERE t.estado = 'ACTIVA'")
    List<CategoriaTienda> findAllCategoriasActivas();
}