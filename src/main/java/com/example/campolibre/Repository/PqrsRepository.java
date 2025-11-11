package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Pqrs;
import com.example.campolibre.Enum.EstadoPqrs;
import com.example.campolibre.Enum.TipoPqrs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;



@Repository
public interface PqrsRepository extends JpaRepository<Pqrs, Long> {


    @Query("SELECT p FROM Pqrs p WHERE p.estado = :estado")
    List<Pqrs> findByEstado(@Param("estado") EstadoPqrs estado);

    @Query("SELECT p FROM Pqrs p WHERE p.emisor.id_usuario = :idEmisor")
    List<Pqrs> findByEmisorId(@Param("idEmisor") Long idEmisor);

    @Query("SELECT p FROM Pqrs p WHERE p.receptor.id_usuario = :idReceptor")
    List<Pqrs> findByReceptorId(@Param("idReceptor") Long idReceptor);

    @Query("SELECT p FROM Pqrs p WHERE p.tipo = :tipo")
    List<Pqrs> findByTipo(@Param("tipo") TipoPqrs tipo);

    @Query("SELECT p FROM Pqrs p WHERE p.estado = 'PENDIENTE'")
    List<Pqrs> findAllPendientes();

    // PQRS que no están asociadas ni a una tienda ni a un evento (para ADMIN)
    @Query("SELECT p FROM Pqrs p WHERE p.id_pqrs NOT IN (SELECT pt.pqrs.id_pqrs FROM PqrsTienda pt) AND p.id_pqrs NOT IN (SELECT pe.pqrs.id_pqrs FROM PqrsEvento pe)")
    List<Pqrs> findUnlinkedPqrs();

    // NUEVO: PQRS sin asociación Y con estado PENDIENTE (para vista pendientes del ADMIN)
    @Query("SELECT p FROM Pqrs p WHERE p.estado = 'PENDIENTE' AND p.id_pqrs NOT IN (SELECT pt.pqrs.id_pqrs FROM PqrsTienda pt) AND p.id_pqrs NOT IN (SELECT pe.pqrs.id_pqrs FROM PqrsEvento pe)")
    List<Pqrs> findUnlinkedPendientes();

    // PQRS asociados a tiendas cuyo dueño (usuario) tiene id = :ownerId
    @Query("SELECT pt.pqrs FROM PqrsTienda pt WHERE pt.tienda.usuario.id_usuario = :ownerId")
    List<Pqrs> findByTiendaOwnerId(@Param("ownerId") Long ownerId);

    // PQRS asociados a eventos cuyo creador tiene id = :ownerId
    @Query("SELECT pe.pqrs FROM PqrsEvento pe WHERE pe.evento.creado_por.id_usuario = :ownerId")
    List<Pqrs> findByEventoOwnerId(@Param("ownerId") Long ownerId);

    // Query para reportes con filtros opcionales
    // ✅ Verificar que esta query esté bien en PqrsRepository
    @Query("SELECT p FROM Pqrs p WHERE " +
            "(:fechaDesde IS NULL OR p.fecha_envio >= :fechaDesde) AND " +
            "(:fechaHasta IS NULL OR p.fecha_envio <= :fechaHasta) AND " +
            "(:tipo IS NULL OR p.tipo = :tipo) AND " +
            "(:estado IS NULL OR p.estado = :estado)")
    List<Pqrs> buscarPqrsConFiltros(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("tipo") TipoPqrs tipo,
            @Param("estado") EstadoPqrs estado
    );

    // Query para reportes de PQRS por tienda
    @Query("SELECT pt.pqrs FROM PqrsTienda pt WHERE " +
            "pt.tienda.id_tienda = :idTienda")
    List<Pqrs> findByTiendaParaReporte(
            @Param("idTienda") Long idTienda
    );

    // Query para reportes de PQRS por evento
    @Query("SELECT pe.pqrs FROM PqrsEvento pe WHERE " +
            "pe.evento.id_evento = :idEvento")
    List<Pqrs> findByEventoParaReporte(
            @Param("idEvento") Long idEvento
    );


}