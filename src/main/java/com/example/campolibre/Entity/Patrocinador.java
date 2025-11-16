package com.example.campolibre.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "patrocinadores")
@Data
public class Patrocinador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_patrocinador", nullable = false, unique = true)
    private Long id_patrocinador;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "contacto_email", length = 100)
    private String contactoEmail;

    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    @Column(name = "sitio_web", length = 255)
    private String sitioWeb;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true; // Para "archivar" patrocinadores sin borrarlos

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }


}