package com.example.campolibre.Repository;

import com.example.campolibre.Entity.MisEventos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MisEventosRepository extends JpaRepository<MisEventos, Long> {

    @Query("SELECT me FROM MisEventos me WHERE me.usuario.id_usuario = :idUsuario")
    List<MisEventos> findByUsuarioId(@Param("idUsuario") Long idUsuario);

    @Query("SELECT me FROM MisEventos me WHERE me.evento.id_evento = :idEvento")
    List<MisEventos> findByEventoId(@Param("idEvento") Long idEvento);

    @Query("SELECT me FROM MisEventos me WHERE me.usuario.id_usuario = :idUsuario AND me.evento.id_evento = :idEvento")
    MisEventos findByUsuarioIdAndEventoId(@Param("idUsuario") Long idUsuario, @Param("idEvento") Long idEvento);

    @Query("DELETE FROM MisEventos me WHERE me.evento.id_evento = :idEvento")
    void deleteByEventoId(@Param("idEvento") Long idEvento);
}