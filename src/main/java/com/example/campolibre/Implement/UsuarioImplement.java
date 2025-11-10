package com.example.campolibre.Implement;

import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Exception.CustomException;
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

    @Autowired
    public UsuarioImplement(UsuarioRepository usuarioRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
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

        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        UsuarioDTO resultado = modelMapper.map(nuevoUsuario, UsuarioDTO.class);
        resultado.setContrasena(null);
        return resultado;
    }

    @Override
    public UsuarioDTO obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        UsuarioDTO resultado = modelMapper.map(usuario, UsuarioDTO.class);
        resultado.setContrasena(null);
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
        return resultado;
    }

    @Override
    public List<UsuarioDTO> obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(usuario -> {
                    UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);
                    dto.setContrasena(null);
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

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);

        UsuarioDTO resultado = modelMapper.map(usuarioActualizado, UsuarioDTO.class);
        resultado.setContrasena(null);
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