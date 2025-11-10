package com.example.campolibre.Repository;

import com.example.campolibre.Entity.PqrsTienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PqrsTiendaRepository extends JpaRepository<PqrsTienda, Long> {

    @Query("SELECT pt FROM PqrsTienda pt WHERE pt.tienda.id_tienda = :idTienda")
    List<PqrsTienda> findByTiendaId(@Param("idTienda") Long idTienda);

    @Query("SELECT pt FROM PqrsTienda pt WHERE pt.pqrs.id_pqrs = :idPqrs")
    PqrsTienda findByPqrsId(@Param("idPqrs") Long idPqrs);
}