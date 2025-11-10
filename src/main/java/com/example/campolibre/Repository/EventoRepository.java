package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    @Query("SELECT e FROM Evento e WHERE e.estado = :estado")
    List<Evento> findByEstado(@Param("estado") EstadoEvento estado);

    @Query("SELECT e FROM Evento e WHERE e.creado_por.id_usuario = :idUsuario")
    List<Evento> findByCreadorId(@Param("idUsuario") Long idUsuario);

    @Query("SELECT e FROM Evento e WHERE e.creado_por.id_usuario = :idUsuario AND e.estado = :estado")
    List<Evento> findByCreadorIdAndEstado(@Param("idUsuario") Long idUsuario, @Param("estado") EstadoEvento estado);

    @Query("SELECT e FROM Evento e WHERE e.tipo_evento = :tipoEvento AND e.estado = 'APROBADO'")
    List<Evento> findByTipoEventoAndAprobado(@Param("tipoEvento") TipoEvento tipoEvento);

    @Query("SELECT e FROM Evento e WHERE e.fecha_evento >= :fecha AND e.estado = 'APROBADO'")
    List<Evento> findEventosProximos(@Param("fecha") LocalDate fecha);

    @Query("SELECT e FROM Evento e WHERE e.estado = 'APROBADO'")
    List<Evento> findAllApproved();
}