package com.example.campolibre.Repository;

import com.example.campolibre.Entity.InscripcionProveedor;
import com.example.campolibre.Enum.EstadoCupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionProveedorRepository extends JpaRepository<InscripcionProveedor, Long> {

    // 1. Encontrar la inscripción de un proveedor a un evento específico
    // CORRECCIÓN: Usar @Query para navegar correctamente al ID: evento.id_evento
    @Query("SELECT i FROM InscripcionProveedor i WHERE i.proveedor.id_usuario = :idUsuario AND i.evento.id_evento = :idEvento")
    Optional<InscripcionProveedor> findByProveedorIdUsuarioAndEventoId(@Param("idUsuario") Long idUsuario, @Param("idEvento") Long idEvento);


    // 2. Listar todas las inscripciones (sin importar el estado) para un evento
    // CORRECCIÓN: Usar @Query para navegar correctamente al ID: evento.id_evento
    @Query("SELECT i FROM InscripcionProveedor i WHERE i.evento.id_evento = :idEvento")
    List<InscripcionProveedor> findByEventoId(@Param("idEvento") Long idEvento);

    // Solo contar cupos CONFIRMADOS (los que realmente pagaron)
    @Query("SELECT COUNT(i) FROM InscripcionProveedor i WHERE i.evento.id_evento = :idEvento " +
            "AND i.estadoCupo = com.example.campolibre.Enum.EstadoCupo.CONFIRMADO")
    Long countCuposConfirmadosPorEvento(@Param("idEvento") Long idEvento);

    // 4. Listar todas las inscripciones de un proveedor (para Mis Eventos Proveedor)
    @Query("SELECT i FROM InscripcionProveedor i WHERE i.proveedor.id_usuario = :idUsuario")
    List<InscripcionProveedor> findByProveedorId(@Param("idUsuario") Long idUsuario);

    // 5. Contar cupos por evento y estado (LA SOLUCIÓN FUNCIONAL)
    @Query("SELECT COUNT(i) FROM InscripcionProveedor i WHERE i.evento.id_evento = :idEvento AND i.estadoCupo = :estadoCupo")
    Long countByEventoIdAndEstadoCupo(@Param("idEvento") Long idEvento, @Param("estadoCupo") EstadoCupo estadoCupo);

    // Buscar inscripción por código (útil para validar entrada al evento)
    @Query("SELECT i FROM InscripcionProveedor i WHERE i.codigoConfirmacion = :codigo")
    Optional<InscripcionProveedor> findByCodigoConfirmacion(@Param("codigo") String codigo);

    // Listar inscripciones confirmadas de un proveedor (sus eventos activos)
    @Query("SELECT i FROM InscripcionProveedor i WHERE i.proveedor.id_usuario = :idUsuario " +
            "AND i.estadoCupo = com.example.campolibre.Enum.EstadoCupo.CONFIRMADO")
    List<InscripcionProveedor> findEventosConfirmadosPorProveedor(@Param("idUsuario") Long idUsuario);

    // ===== NUEVO MÉTODO PARA REPORTE DE INSCRIPCIONES =====
    @Query("SELECT i FROM InscripcionProveedor i " +
            "JOIN FETCH i.proveedor " +
            "JOIN FETCH i.evento " +
            "LEFT JOIN FETCH i.pagoEvento " +
            "WHERE i.evento.id_evento = :idEvento " +
            "ORDER BY i.fechaInscripcion DESC")
    List<InscripcionProveedor> findInscripcionesConDetallesPorEvento(@Param("idEvento") Long idEvento);

}