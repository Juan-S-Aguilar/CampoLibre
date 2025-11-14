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

    @Query("SELECT COUNT(i) FROM InscripcionProveedor i WHERE i.evento.id_evento = :idEvento " +
            "AND (i.estadoCupo = com.example.campolibre.Enum.EstadoCupo.CONFIRMADO " +
            "OR i.estadoCupo = com.example.campolibre.Enum.EstadoCupo.PENDIENTE_PAGO)")
    Long countCuposOcupadosParaEvento(Long idEvento);

    // 4. Listar todas las inscripciones de un proveedor (para Mis Eventos Proveedor)
    @Query("SELECT i FROM InscripcionProveedor i WHERE i.proveedor.id_usuario = :idUsuario")
    List<InscripcionProveedor> findByProveedorId(@Param("idUsuario") Long idUsuario);

    // 5. Contar cupos por evento y estado (LA SOLUCIÓN FUNCIONAL)
    @Query("SELECT COUNT(i) FROM InscripcionProveedor i WHERE i.evento.id_evento = :idEvento AND i.estadoCupo = :estadoCupo")
    Long countByEventoIdAndEstadoCupo(@Param("idEvento") Long idEvento, @Param("estadoCupo") EstadoCupo estadoCupo);
}