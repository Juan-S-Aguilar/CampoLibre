package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Rol;
import com.example.campolibre.Enum.NombreRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    @Query("SELECT r FROM Rol r WHERE r.nombre_rol = :nombreRol")
    Rol findByNombreRol(@Param("nombreRol") NombreRol nombreRol);
}