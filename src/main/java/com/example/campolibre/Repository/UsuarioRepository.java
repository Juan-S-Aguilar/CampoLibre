package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Enum.NombreRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByEmail(String email);

    Usuario findByDocumento(String documento);

    @Query("SELECT u FROM Usuario u WHERE u.estado = 'ACTIVO'")
    List<Usuario> findAllActive();

    @Query("SELECT u FROM Usuario u WHERE u.estado = 'ELIMINADO'")
    List<Usuario> findAllDeleted();

    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.estado = 'ACTIVO'")
    Usuario findByEmailAndEstadoActivo(@Param("email") String email);

    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre_rol = :nombreRol AND u.estado = 'ACTIVO'")
    List<Usuario> findByRolNombre(@Param("nombreRol") NombreRol nombreRol);
}