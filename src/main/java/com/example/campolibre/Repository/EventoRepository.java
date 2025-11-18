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
import java.util.Optional;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    @Query("SELECT e FROM Evento e WHERE e.estado = :estado")
    List<Evento> findByEstado(@Param("estado") EstadoEvento estado);

    @Query("SELECT e FROM Evento e WHERE e.creado_por.id_usuario = :idUsuario")
    List<Evento> findByCreadorId(@Param("idUsuario") Long idUsuario);

    @Query("SELECT e FROM Evento e WHERE e.creado_por.id_usuario = :idUsuario AND e.estado = :estado")
    List<Evento> findByCreadorIdAndEstado(@Param("idUsuario") Long idUsuario, @Param("estado") EstadoEvento estado);

    // Reemplazar los queries que usan String 'APROBADO' por un parámetro de Enum para mayor seguridad
    @Query("SELECT e FROM Evento e WHERE e.tipo_evento = :tipoEvento AND e.estado = :estado")
    List<Evento> findByTipoEventoAndEstado(@Param("tipoEvento") TipoEvento tipoEvento, @Param("estado") EstadoEvento estado);

    @Query("SELECT e FROM Evento e WHERE e.fecha_evento >= :fecha AND e.estado = :estado")
    List<Evento> findEventosProximosAndEstado(@Param("fecha") LocalDate fecha, @Param("estado") EstadoEvento estado);


    // Nuevo método para encontrar eventos por patrocinador
    @Query("SELECT e FROM Evento e WHERE e.patrocinador.id_patrocinador = :idPatrocinador")
    List<Evento> findByPatrocinadorId(@Param("idPatrocinador") Long idPatrocinador);

    // Buscar eventos publicados con cupos disponibles
    @Query("SELECT e FROM Evento e WHERE e.estado = :estado " +
            "AND e.cuposOcupados < e.cuposMaximosProveedor")
    List<Evento> findEventosConCuposDisponibles(@Param("estado") EstadoEvento estado);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Evento e WHERE e.id_evento = :id")
    Optional<Evento> findByIdWithLock(@Param("id") Long id);


}