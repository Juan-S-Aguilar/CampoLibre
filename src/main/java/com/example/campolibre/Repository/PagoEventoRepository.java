package com.example.campolibre.Repository;

import com.example.campolibre.Entity.PagoEvento;
import com.example.campolibre.Enum.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoEventoRepository extends JpaRepository<PagoEvento, Long> {

    // Buscar pago por número de transacción
    Optional<PagoEvento> findByNumeroTransaccion(String numeroTransaccion);

    // Buscar pago por inscripción
    @Query("SELECT p FROM PagoEvento p WHERE p.inscripcionProveedor.id_inscripcion = :idInscripcion")
    Optional<PagoEvento> findByInscripcionId(@Param("idInscripcion") Long idInscripcion);

    // Listar pagos por estado
    @Query("SELECT p FROM PagoEvento p WHERE p.estado = :estado")
    List<PagoEvento> findByEstado(@Param("estado") EstadoPago estado);

    // Listar pagos de un proveedor (a través de inscripciones)
    @Query("SELECT p FROM PagoEvento p WHERE p.inscripcionProveedor.proveedor.id_usuario = :idUsuario")
    List<PagoEvento> findPagosByProveedorId(@Param("idUsuario") Long idUsuario);

    // Sumar total recaudado por un evento (solo pagos exitosos)
    @Query("SELECT COALESCE(SUM(p.monto), 0.0) FROM PagoEvento p " +
            "WHERE p.inscripcionProveedor.evento.id_evento = :idEvento " +
            "AND p.estado = com.example.campolibre.Enum.EstadoPago.EXITOSO")
    Double calcularTotalRecaudadoPorEvento(@Param("idEvento") Long idEvento);

    // Contar pagos exitosos por evento
    @Query("SELECT COUNT(p) FROM PagoEvento p " +
            "WHERE p.inscripcionProveedor.evento.id_evento = :idEvento " +
            "AND p.estado = com.example.campolibre.Enum.EstadoPago.EXITOSO")
    Long countPagosExitososPorEvento(@Param("idEvento") Long idEvento);



}