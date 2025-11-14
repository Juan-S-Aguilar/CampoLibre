package com.example.campolibre.Repository;

import com.example.campolibre.Entity.AsistenciaConsumidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AsistenciaConsumidorRepository extends JpaRepository<AsistenciaConsumidor, Long> {

    // 1. Verificar si un consumidor ya registró asistencia a un evento
    @Query("SELECT a FROM AsistenciaConsumidor a WHERE a.consumidor.id_usuario = :idConsumidor AND a.evento.id_evento = :idEvento")
    Optional<AsistenciaConsumidor> findByConsumidorAndEventoId(@Param("idConsumidor") Long idConsumidor, @Param("idEvento") Long idEvento);

    // 2. Contar la asistencia total para un evento (para reportes de trazabilidad)
    @Query("SELECT COUNT(a) FROM AsistenciaConsumidor a WHERE a.evento.id_evento = :idEvento")
    Long countAsistenciaByEventoId(@Param("idEvento") Long idEvento);

    // 3. Listar todos los asistentes a un evento (para el reporte del Administrador)
    @Query("SELECT a FROM AsistenciaConsumidor a WHERE a.evento.id_evento = :idEvento")
    List<AsistenciaConsumidor> findByEventoId(@Param("idEvento") Long idEvento);
}