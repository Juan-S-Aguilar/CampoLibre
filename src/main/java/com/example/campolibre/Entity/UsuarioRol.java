package com.example.campolibre.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuarios_roles")
@Data
public class UsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario_rol", nullable = false, unique = true)
    private Long id_usuario_rol;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;
}