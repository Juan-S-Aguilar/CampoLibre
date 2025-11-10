package com.example.campolibre.Implement;

import com.example.campolibre.DTO.UsuarioRolDTO;
import com.example.campolibre.Entity.Rol;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Entity.UsuarioRol;
import com.example.campolibre.Enum.NombreRol;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.RolRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Repository.UsuarioRolRepository;
import com.example.campolibre.Service.UsuarioRolService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioRolImplement implements UsuarioRolService {

    private final UsuarioRolRepository usuarioRolRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public UsuarioRolImplement(UsuarioRolRepository usuarioRolRepository, UsuarioRepository usuarioRepository,
                               RolRepository rolRepository, ModelMapper modelMapper) {
        this.usuarioRolRepository = usuarioRolRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public UsuarioRolDTO asignarRolAUsuario(UsuarioRolDTO usuarioRolDTO) {
        Usuario usuario = usuarioRepository.findById(usuarioRolDTO.getId_usuario())
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        Rol rol = rolRepository.findById(usuarioRolDTO.getId_rol())
                .orElseThrow(() -> new CustomException("Rol no encontrado"));

        // Verificar si ya existe la asignación
        UsuarioRol existente = usuarioRolRepository.findByUsuarioIdAndRolId(
                usuarioRolDTO.getId_usuario(), usuarioRolDTO.getId_rol());

        if (existente != null) {
            throw new CustomException("El usuario ya tiene este rol asignado");
        }

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);

        UsuarioRol nuevaAsignacion = usuarioRolRepository.save(usuarioRol);

        UsuarioRolDTO resultado = new UsuarioRolDTO();
        resultado.setId_usuario_rol(nuevaAsignacion.getId_usuario_rol());
        resultado.setId_usuario(nuevaAsignacion.getUsuario().getId_usuario());
        resultado.setId_rol(nuevaAsignacion.getRol().getId_rol());

        return resultado;
    }

    @Override
    public List<UsuarioRolDTO> obtenerRolesPorUsuario(Long idUsuario) {
        List<UsuarioRol> roles = usuarioRolRepository.findByUsuarioId(idUsuario);
        return roles.stream()
                .map(ur -> {
                    UsuarioRolDTO dto = new UsuarioRolDTO();
                    dto.setId_usuario_rol(ur.getId_usuario_rol());
                    dto.setId_usuario(ur.getUsuario().getId_usuario());
                    dto.setId_rol(ur.getRol().getId_rol());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioRolDTO> obtenerUsuariosPorRol(NombreRol nombreRol) {
        List<UsuarioRol> usuarios = usuarioRolRepository.findByRolNombre(nombreRol);
        return usuarios.stream()
                .map(ur -> {
                    UsuarioRolDTO dto = new UsuarioRolDTO();
                    dto.setId_usuario_rol(ur.getId_usuario_rol());
                    dto.setId_usuario(ur.getUsuario().getId_usuario());
                    dto.setId_rol(ur.getRol().getId_rol());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarRolDeUsuario(Long id) {
        UsuarioRol usuarioRol = usuarioRolRepository.findById(id)
                .orElseThrow(() -> new CustomException("Asignación de rol no encontrada"));
        usuarioRolRepository.delete(usuarioRol);
    }

    @Override
    public boolean usuarioTieneRol(Long idUsuario, NombreRol nombreRol) {
        Rol rol = rolRepository.findByNombreRol(nombreRol);
        if (rol == null) {
            return false;
        }
        UsuarioRol usuarioRol = usuarioRolRepository.findByUsuarioIdAndRolId(idUsuario, rol.getId_rol());
        return usuarioRol != null;
    }
}