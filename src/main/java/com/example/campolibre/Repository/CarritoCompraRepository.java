package com.example.campolibre.Repository;

import com.example.campolibre.Entity.CarritoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoCompraRepository extends JpaRepository<CarritoCompra, Long> {

    @Query("SELECT c FROM CarritoCompra c WHERE c.usuario.id_usuario = :idUsuario")
    Optional<CarritoCompra> findByUsuarioId(@Param("idUsuario") Long idUsuario);

    @Query("SELECT c FROM CarritoCompra c LEFT JOIN FETCH c.items WHERE c.usuario.id_usuario = :idUsuario")
    Optional<CarritoCompra> findByUsuarioIdWithItems(@Param("idUsuario") Long idUsuario);
}