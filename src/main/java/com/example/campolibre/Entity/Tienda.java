package com.example.campolibre.Entity;

import com.example.campolibre.Enum.CategoriaTienda;
import com.example.campolibre.Enum.EstadoTienda;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tiendas")
@Data
public class Tienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tienda", nullable = false, unique = true)
    private Long id_tienda;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_principal", nullable = false)
    private CategoriaTienda categoriaPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTienda estado = EstadoTienda.ACTIVA;

    @Column(name = "correo_tienda", length = 100)
    private String correo_tienda;

    @Column(name = "telefono_tienda", length = 20)
    private String telefono_tienda;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "imagen_tienda", length = 255)
    private String imagen_tienda;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fecha_creacion;

    @PrePersist
    protected void onCreate() {
        fecha_creacion = LocalDateTime.now();
    }
}