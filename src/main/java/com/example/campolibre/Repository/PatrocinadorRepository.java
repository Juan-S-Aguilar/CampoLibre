package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Patrocinador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatrocinadorRepository extends JpaRepository<Patrocinador, Long> {

    Optional<Patrocinador> findByNombre(String nombre);

    // Listar solo patrocinadores activos (para selección en formularios)
    @Query("SELECT p FROM Patrocinador p WHERE p.activo = true")
    List<Patrocinador> findAllActivos();

    // Buscar por email (útil para validaciones)
    Optional<Patrocinador> findByContactoEmail(String email);
}