package com.example.campolibre.Repository;

import com.example.campolibre.Entity.Pqrs;
import com.example.campolibre.Entity.PqrsRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // 💡 ¡Necesitas esta importación para Optional!

@Repository
public interface PqrsRespuestaRepository extends JpaRepository<PqrsRespuesta, Long> {

    /**
     * Busca el registro de PqrsRespuesta más reciente para una PQRS dada.
     * Esto es crucial para la trazabilidad (obtener la última respuesta/réplica).
     */
    Optional<PqrsRespuesta> findFirstByPqrsOrderByFechaEmisionDesc(Pqrs pqrs);


}
