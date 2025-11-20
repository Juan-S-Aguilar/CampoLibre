package com.example.campolibre.Entity;

import com.example.campolibre.Enum.TipoDocumento;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false, unique = true)
    private Long id_usuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "documento", nullable = false, length = 20, unique = true)
    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipo_documento;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fecha_registro;

    @Column(name = "estado", nullable = false)
    private String estado = "ACTIVO";

    // ✅ AGREGAR ESTA RELACIÓN
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuarios_roles",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private Set<Rol> roles;

    @PrePersist
    protected void onCreate() {
        fecha_registro = LocalDateTime.now();
    }
}