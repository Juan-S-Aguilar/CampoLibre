package com.example.campolibre.Entity;

import com.example.campolibre.Enum.EstadoPqrs;
import com.example.campolibre.Enum.TipoPqrs;
import com.example.campolibre.Enum.RolProceso; // Importar el nuevo Enum
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pqrs")
@Data
public class Pqrs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pqrs", nullable = false, unique = true)
    private Long id_pqrs;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoPqrs tipo;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fecha_envio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPqrs estado = EstadoPqrs.PENDIENTE;

    // Nuevo campo: Indica a qué rol le toca la siguiente acción
    @Enumerated(EnumType.STRING)
    @Column(name = "pendiente_de", nullable = false)
    private RolProceso pendienteDe = RolProceso.PROVEEDOR; // Inicialmente, el Proveedor debe responder

    // ¡CAMPOS ELIMINADOS! Ya no se usan: fecha_respuesta, respuesta

    // AÑADIDO: Relación Uno a Muchos con el Historial de Respuestas/Réplicas
    @OneToMany(mappedBy = "pqrs", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PqrsRespuesta> historialRespuestas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "id_emisor", nullable = false)
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "id_receptor")
    private Usuario receptor;

    @PrePersist
    protected void onCreate() {
        fecha_envio = LocalDateTime.now();
    }
}