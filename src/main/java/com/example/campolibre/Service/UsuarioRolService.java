package com.example.campolibre.Service;

import com.example.campolibre.DTO.UsuarioRolDTO;
import com.example.campolibre.Enum.NombreRol;
import java.util.List;

public interface UsuarioRolService {
    UsuarioRolDTO asignarRolAUsuario(UsuarioRolDTO usuarioRolDTO);
    List<UsuarioRolDTO> obtenerRolesPorUsuario(Long idUsuario);
    List<UsuarioRolDTO> obtenerUsuariosPorRol(NombreRol nombreRol);
    void eliminarRolDeUsuario(Long id);
    boolean usuarioTieneRol(Long idUsuario, NombreRol nombreRol);
}