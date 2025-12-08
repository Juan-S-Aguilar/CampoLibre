package com.example.campolibre.Implement;

import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }

        // Verificar que el usuario esté activo
        if (!"ACTIVO".equals(usuario.getEstado())) {
            throw new UsernameNotFoundException("Usuario no está activo. No puede iniciar sesión.");
        }

        // Verificar que el usuario tenga un rol asignado
        if (usuario.getRol() == null) {
            throw new UsernameNotFoundException("Usuario sin rol asignado: " + username);
        }

        // Crear la autoridad con el único rol del usuario
        GrantedAuthority authority = new SimpleGrantedAuthority(usuario.getRol().getNombre_rol().name());
        List<GrantedAuthority> authorities = Collections.singletonList(authority);

        return new User(
                usuario.getEmail(),
                usuario.getContrasena(),
                authorities
        );
    }
}