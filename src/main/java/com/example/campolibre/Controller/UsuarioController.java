package com.example.campolibre.Controller;

import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.DTO.UsuarioRolDTO;
import com.example.campolibre.Enum.NombreRol;
import com.example.campolibre.Service.UsuarioRolService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRolService usuarioRolService;

    // Solo ADMIN puede acceder a la gestión de usuarios
    @GetMapping
    public String listarUsuarios(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        }

        List<UsuarioDTO> usuarios = usuarioService.obtenerUsuariosActivos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("tipoLista", "activos");
        return "usuario/list";
    }

    @GetMapping("/eliminados")
    public String listarUsuariosEliminados(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        }

        List<UsuarioDTO> usuarios = usuarioService.obtenerUsuariosEliminados();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("tipoLista", "eliminados");
        return "usuario/list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("usuario", new UsuarioDTO());
        model.addAttribute("roles", NombreRol.values());
        return "usuario/form";
    }

    @PostMapping("/crear")
    public String crearUsuario(@ModelAttribute UsuarioDTO usuarioDTO,
                               @RequestParam(value = "rolesSeleccionados", required = false) List<Long> rolesIds,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        }

        try {
            UsuarioDTO nuevoUsuario = usuarioService.crearUsuario(usuarioDTO);

            // Asignar roles seleccionados
            if (rolesIds != null && !rolesIds.isEmpty()) {
                for (Long idRol : rolesIds) {
                    UsuarioRolDTO usuarioRolDTO = new UsuarioRolDTO();
                    usuarioRolDTO.setId_usuario(nuevoUsuario.getId_usuario());
                    usuarioRolDTO.setId_rol(idRol);
                    usuarioRolService.asignarRolAUsuario(usuarioRolDTO);
                }
            }

            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear usuario: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/ver/{id}")
    public String verUsuario(@PathVariable Long id, Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorId(id);
        List<UsuarioRolDTO> roles = usuarioRolService.obtenerRolesPorUsuario(id);

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", roles);
        return "usuario/view";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorId(id);
        List<UsuarioRolDTO> rolesAsignados = usuarioRolService.obtenerRolesPorUsuario(id);

        model.addAttribute("usuario", usuario);
        model.addAttribute("rolesAsignados", rolesAsignados);
        model.addAttribute("todosLosRoles", NombreRol.values());
        return "usuario/edit";
    }

    @PostMapping("/actualizar")
    public String actualizarUsuario(@ModelAttribute("usuario") UsuarioDTO usuarioDTO,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        }

        try {
            usuarioService.actualizarUsuario(usuarioDTO.getId_usuario(), usuarioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar usuario: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes,
                                  Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        }

        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar usuario: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    // Ver perfil propio (todos los roles)
    @GetMapping("/perfil")
    public String verPerfil(Model model, Authentication authentication) {
        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
        List<UsuarioRolDTO> roles = usuarioRolService.obtenerRolesPorUsuario(usuario.getId_usuario());

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", roles);
        return "usuario/perfil";
    }

    // Actualizar perfil propio (todos los roles)
    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@ModelAttribute UsuarioDTO usuarioDTO,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuarioActual = usuarioService.obtenerUsuarioPorEmail(email);

            // Solo permitir cambiar nombre, teléfono y contraseña
            usuarioDTO.setId_usuario(usuarioActual.getId_usuario());
            usuarioDTO.setEmail(usuarioActual.getEmail());
            usuarioDTO.setDocumento(usuarioActual.getDocumento());
            usuarioDTO.setTipo_documento(usuarioActual.getTipo_documento());

            usuarioService.actualizarUsuario(usuarioDTO.getId_usuario(), usuarioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar perfil: " + e.getMessage());
        }
        return "redirect:/usuarios/perfil";
    }
}