package com.example.campolibre.Config;

import com.example.campolibre.Entity.Rol;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Enum.NombreRol;
import com.example.campolibre.Enum.TipoDocumento;
import com.example.campolibre.Repository.RolRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Se ejecuta primero
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear roles si no existen
        crearRolSiNoExiste(NombreRol.ADMINISTRADOR);
        crearRolSiNoExiste(NombreRol.PROVEEDOR);
        crearRolSiNoExiste(NombreRol.CONSUMIDOR);

        // Crear usuarios de prueba con UN ÚNICO ROL cada uno
        // Usuario administrador (acceso total al sistema)
        crearUsuarioConRol("admin@campolibre.com", "admin123", "Administrador CampoLibre",
                "1000000000", TipoDocumento.CC, NombreRol.ADMINISTRADOR);

        // Usuario proveedor (puede vender productos y participar en eventos)
        crearUsuarioConRol("proveedor@campolibre.com", "proveedor123", "Proveedor Test",
                "2000000000", TipoDocumento.CC, NombreRol.PROVEEDOR);

        // Usuario consumidor (solo puede comprar productos y asistir a eventos)
        crearUsuarioConRol("consumidor@campolibre.com", "consumidor123", "Consumidor Test",
                "3000000000", TipoDocumento.CC, NombreRol.CONSUMIDOR);
    }

    private void crearRolSiNoExiste(NombreRol nombreRol) {
        if (rolRepository.findByNombreRol(nombreRol) == null) {
            Rol rol = new Rol();
            rol.setNombre_rol(nombreRol);
            rolRepository.save(rol);
            System.out.println("Rol creado: " + nombreRol);
        }
    }

    private void crearUsuarioConRol(String email, String contrasena, String nombre, String documento,
                                    TipoDocumento tipoDocumento, NombreRol nombreRol) {
        if (usuarioRepository.findByEmail(email) == null) {
            // Buscar el rol
            Rol rol = rolRepository.findByNombreRol(nombreRol);
            if (rol == null) {
                System.err.println("Error: Rol " + nombreRol + " no encontrado");
                return;
            }

            // Crear usuario con su único rol
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setContrasena(passwordEncoder.encode(contrasena));
            usuario.setNombre(nombre);
            usuario.setDocumento(documento);
            usuario.setTipo_documento(tipoDocumento);
            usuario.setTelefono("3001234567");
            usuario.setRol(rol); // Asignar el único rol directamente

            usuarioRepository.save(usuario);
            System.out.println("Usuario creado: " + email + " con rol " + nombreRol);
        }
    }
}