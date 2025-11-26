package com.example.campolibre.Implement;

import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Entity.Rol;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.RolRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Service.UsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioImplement implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

    @Autowired
    public UsuarioImplement(UsuarioRepository usuarioRepository, ModelMapper modelMapper,
                           PasswordEncoder passwordEncoder, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.rolRepository = rolRepository;
    }

    @Override
    public UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.findByEmail(usuarioDTO.getEmail()) != null) {
            throw new CustomException("El email ya está registrado.");
        }

        if (usuarioRepository.findByDocumento(usuarioDTO.getDocumento()) != null) {
            throw new CustomException("El documento ya está registrado.");
        }

        Usuario usuario = modelMapper.map(usuarioDTO, Usuario.class);
        usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena()));
        usuario.setEstado("ACTIVO");

        // Asignar el rol al usuario
        if (usuarioDTO.getId_rol() != null) {
            Rol rol = rolRepository.findById(usuarioDTO.getId_rol())
                    .orElseThrow(() -> new CustomException("Rol no encontrado"));
            usuario.setRol(rol);
        }

        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        UsuarioDTO resultado = modelMapper.map(nuevoUsuario, UsuarioDTO.class);
        resultado.setContrasena(null);

        // Mapear el rol único
        if (nuevoUsuario.getRol() != null) {
            resultado.setId_rol(nuevoUsuario.getRol().getId_rol());
            resultado.setNombreRol(nuevoUsuario.getRol().getNombre_rol());
        }

        return resultado;
    }

    @Override
    public UsuarioDTO obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        UsuarioDTO resultado = modelMapper.map(usuario, UsuarioDTO.class);
        resultado.setContrasena(null);

        // Mapear el rol único
        if (usuario.getRol() != null) {
            resultado.setId_rol(usuario.getRol().getId_rol());
            resultado.setNombreRol(usuario.getRol().getNombre_rol());
        }

        return resultado;
    }

    @Override
    public UsuarioDTO obtenerUsuarioPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new CustomException("Usuario no encontrado con email: " + email);
        }

        UsuarioDTO resultado = modelMapper.map(usuario, UsuarioDTO.class);
        resultado.setContrasena(null);

        // Mapear el rol único
        if (usuario.getRol() != null) {
            resultado.setId_rol(usuario.getRol().getId_rol());
            resultado.setNombreRol(usuario.getRol().getNombre_rol());
        }

        return resultado;
    }

    @Override
    public List<UsuarioDTO> obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(usuario -> {
                    UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);
                    dto.setContrasena(null);

                    // Mapear el rol único
                    if (usuario.getRol() != null) {
                        dto.setId_rol(usuario.getRol().getId_rol());
                        dto.setNombreRol(usuario.getRol().getNombre_rol());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> obtenerUsuariosActivos() {
        List<Usuario> usuarios = usuarioRepository.findAllActive();
        return usuarios.stream()
                .map(usuario -> {
                    UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);
                    dto.setContrasena(null);

                    // Mapear el rol único
                    if (usuario.getRol() != null) {
                        dto.setId_rol(usuario.getRol().getId_rol());
                        dto.setNombreRol(usuario.getRol().getNombre_rol());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> obtenerUsuariosEliminados() {
        List<Usuario> usuarios = usuarioRepository.findAllDeleted();
        return usuarios.stream()
                .map(usuario -> {
                    UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);
                    dto.setContrasena(null);

                    // Mapear el rol único
                    if (usuario.getRol() != null) {
                        dto.setId_rol(usuario.getRol().getId_rol());
                        dto.setNombreRol(usuario.getRol().getNombre_rol());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioDTO actualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        if (usuarioDTO.getNombre() != null && !usuarioDTO.getNombre().isEmpty()) {
            usuarioExistente.setNombre(usuarioDTO.getNombre());
        }

        if (usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().isEmpty()) {
            Usuario usuarioConEmail = usuarioRepository.findByEmail(usuarioDTO.getEmail());
            if (usuarioConEmail != null && !usuarioConEmail.getId_usuario().equals(id)) {
                throw new CustomException("El email ya está en uso");
            }
            usuarioExistente.setEmail(usuarioDTO.getEmail());
        }

        if (usuarioDTO.getTelefono() != null) {
            usuarioExistente.setTelefono(usuarioDTO.getTelefono());
        }

        if (usuarioDTO.getContrasena() != null && !usuarioDTO.getContrasena().isEmpty()) {
            usuarioExistente.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena()));
        }

        // Actualizar el rol si se proporciona un nuevo id_rol
        if (usuarioDTO.getId_rol() != null) {
            Rol nuevoRol = rolRepository.findById(usuarioDTO.getId_rol())
                    .orElseThrow(() -> new CustomException("Rol no encontrado"));
            usuarioExistente.setRol(nuevoRol);
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);

        UsuarioDTO resultado = modelMapper.map(usuarioActualizado, UsuarioDTO.class);
        resultado.setContrasena(null);

        // Mapear el rol único
        if (usuarioActualizado.getRol() != null) {
            resultado.setId_rol(usuarioActualizado.getRol().getId_rol());
            resultado.setNombreRol(usuarioActualizado.getRol().getNombre_rol());
        }

        return resultado;
    }

    @Override
    public void eliminarUsuario(Long id) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        usuarioExistente.setEstado("ELIMINADO");
        usuarioRepository.save(usuarioExistente);
    }
}