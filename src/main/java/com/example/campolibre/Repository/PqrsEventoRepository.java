package com.example.campolibre.Repository;

import com.example.campolibre.Entity.PqrsEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PqrsEventoRepository extends JpaRepository<PqrsEvento, Long> {

    @Query("SELECT pe FROM PqrsEvento pe WHERE pe.evento.id_evento = :idEvento")
    List<PqrsEvento> findByEventoId(@Param("idEvento") Long idEvento);

    @Query("SELECT pe FROM PqrsEvento pe WHERE pe.pqrs.id_pqrs = :idPqrs")
    PqrsEvento findByPqrsId(@Param("idPqrs") Long idPqrs);
}