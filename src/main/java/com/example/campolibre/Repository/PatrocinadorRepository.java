package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Patrocinador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatrocinadorRepository extends JpaRepository<Patrocinador, Long> {
    // Métodos CRUD básicos heredados
    Optional<Patrocinador> findByNombre(String nombre);
}