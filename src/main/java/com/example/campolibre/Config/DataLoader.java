package com.example.campolibre.Config;

import com.example.campolibre.Entity.Rol;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Entity.UsuarioRol;
import com.example.campolibre.Enum.NombreRol;
import com.example.campolibre.Enum.TipoDocumento;
import com.example.campolibre.Repository.RolRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Repository.UsuarioRolRepository;
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
    private UsuarioRolRepository usuarioRolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear roles si no existen
        crearRolSiNoExiste(NombreRol.ADMINISTRADOR);
        crearRolSiNoExiste(NombreRol.PROVEEDOR);
        crearRolSiNoExiste(NombreRol.CONSUMIDOR);

        // Crear usuarios de prueba
        crearUsuarioConRoles("admin@campolibre.com", "admin123", "Administrador CampoLibre",
                "1000000000", TipoDocumento.CC, new NombreRol[]{NombreRol.ADMINISTRADOR, NombreRol.PROVEEDOR, NombreRol.CONSUMIDOR});

        crearUsuarioConRoles("proveedor@campolibre.com", "proveedor123", "Proveedor Test",
                "2000000000", TipoDocumento.CC, new NombreRol[]{NombreRol.PROVEEDOR, NombreRol.CONSUMIDOR});

        crearUsuarioConRoles("consumidor@campolibre.com", "consumidor123", "Consumidor Test",
                "3000000000", TipoDocumento.CC, new NombreRol[]{NombreRol.CONSUMIDOR});
    }

    private void crearRolSiNoExiste(NombreRol nombreRol) {
        if (rolRepository.findByNombreRol(nombreRol) == null) {
            Rol rol = new Rol();
            rol.setNombre_rol(nombreRol);
            rolRepository.save(rol);
            System.out.println("Rol creado: " + nombreRol);
        }
    }

    private void crearUsuarioConRoles(String email, String contrasena, String nombre, String documento,
                                      TipoDocumento tipoDocumento, NombreRol[] roles) {
        if (usuarioRepository.findByEmail(email) == null) {
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setContrasena(passwordEncoder.encode(contrasena));
            usuario.setNombre(nombre);
            usuario.setDocumento(documento);
            usuario.setTipo_documento(tipoDocumento);
            usuario.setTelefono("3001234567");
            usuarioRepository.save(usuario);

            // Asignar roles
            for (NombreRol nombreRol : roles) {
                Rol rol = rolRepository.findByNombreRol(nombreRol);
                if (rol != null) {
                    UsuarioRol usuarioRol = new UsuarioRol();
                    usuarioRol.setUsuario(usuario);
                    usuarioRol.setRol(rol);
                    usuarioRolRepository.save(usuarioRol);
                    System.out.println("Rol " + nombreRol + " asignado a " + email);
                }
            }
            System.out.println("Usuario creado: " + email);
        }
    }
}