package com.example.campolibre.Implement;

import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Entity.UsuarioRol;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Repository.UsuarioRolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }

        // Obtener todos los roles del usuario
        List<UsuarioRol> usuarioRoles = usuarioRolRepository.findByUsuarioId(usuario.getId_usuario());

        List<GrantedAuthority> authorities = usuarioRoles.stream()
                .map(ur -> new SimpleGrantedAuthority(ur.getRol().getNombre_rol().name()))
                .collect(Collectors.toList());

        return new User(
                usuario.getEmail(),
                usuario.getContrasena(),
                authorities
        );
    }
}