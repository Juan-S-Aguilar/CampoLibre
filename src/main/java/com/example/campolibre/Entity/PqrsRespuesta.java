package com.example.campolibre.Entity;

import com.example.campolibre.Enum.RolProceso; // Importación para el enum
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pqrs_respuestas") // Asumo que el nombre de tu tabla es algo así
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PqrsRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_respuesta;

    // Contenido de la respuesta o réplica
    @Column(name = "contenido", length = 1000, nullable = false)
    private String contenido;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    // El rol que envió la respuesta (PROVEEDOR o CONSUMIDOR)
    @Enumerated(EnumType.STRING)
    @Column(name = "emitido_por", nullable = false)
    private RolProceso emitidoPor;

    // ----------------------------------------------------
    // 💡 RELACIONES CRÍTICAS (Las que causaron el error)

    // Relación con la PQRS a la que pertenece esta respuesta/réplica
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pqrs", nullable = false)
    private Pqrs pqrs;

    // ❌ CAMPO FALTANTE: La persona que emitió la respuesta (Usuario/Admin/Consumidor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emisor", nullable = false)
    private Usuario emisor; // ¡ESTE CAMPO ES NECESARIO PARA getEmisor()!

    // ----------------------------------------------------
}
