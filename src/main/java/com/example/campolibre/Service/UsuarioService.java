package com.example.campolibre.Service;

import com.example.campolibre.DTO.UsuarioDTO;
import java.util.List;

public interface UsuarioService {
    UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO);
    UsuarioDTO obtenerUsuarioPorId(Long id);
    UsuarioDTO obtenerUsuarioPorEmail(String email);
    List<UsuarioDTO> obtenerTodosLosUsuarios();
    List<UsuarioDTO> obtenerUsuariosActivos();
    List<UsuarioDTO> obtenerUsuariosEliminados();
    UsuarioDTO actualizarUsuario(Long id, UsuarioDTO usuarioDTO);
    void eliminarUsuario(Long id);
}