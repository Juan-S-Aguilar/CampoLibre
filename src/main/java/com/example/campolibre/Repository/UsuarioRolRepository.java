package com.example.campolibre.Repository;

import com.example.campolibre.Entity.UsuarioRol;
import com.example.campolibre.Enum.NombreRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {

    @Query("SELECT ur FROM UsuarioRol ur WHERE ur.usuario.id_usuario = :idUsuario")
    List<UsuarioRol> findByUsuarioId(@Param("idUsuario") Long idUsuario);

    @Query("SELECT ur FROM UsuarioRol ur WHERE ur.rol.nombre_rol = :nombreRol")
    List<UsuarioRol> findByRolNombre(@Param("nombreRol") NombreRol nombreRol);

    @Query("SELECT ur FROM UsuarioRol ur WHERE ur.usuario.id_usuario = :idUsuario AND ur.rol.id_rol = :idRol")
    UsuarioRol findByUsuarioIdAndRolId(@Param("idUsuario") Long idUsuario, @Param("idRol") Long idRol);
}