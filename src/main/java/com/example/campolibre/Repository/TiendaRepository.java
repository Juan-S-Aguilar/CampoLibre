package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Tienda;
import com.example.campolibre.Enum.EstadoTienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, Long> {

    @Query("SELECT t FROM Tienda t WHERE t.estado = :estado")
    List<Tienda> findByEstado(@Param("estado") EstadoTienda estado);

    @Query("SELECT t FROM Tienda t WHERE t.usuario.id_usuario = :idUsuario")
    List<Tienda> findByUsuarioId(@Param("idUsuario") Long idUsuario);

    @Query("SELECT t FROM Tienda t WHERE t.usuario.id_usuario = :idUsuario AND t.estado = :estado")
    List<Tienda> findByUsuarioIdAndEstado(@Param("idUsuario") Long idUsuario, @Param("estado") EstadoTienda estado);

    @Query("SELECT t FROM Tienda t WHERE t.estado = 'ACTIVA'")
    List<Tienda> findAllActive();
}