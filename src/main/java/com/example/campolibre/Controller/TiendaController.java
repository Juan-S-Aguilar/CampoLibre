package com.example.campolibre.Controller;

import com.example.campolibre.DTO.TiendaDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Enum.CategoriaTienda;
import com.example.campolibre.Enum.EstadoTienda;
import com.example.campolibre.Service.TiendaService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tiendas")
public class TiendaController {

    @Autowired
    private TiendaService tiendaService;

    @Autowired
    private UsuarioService usuarioService;

    // Ver todas las tiendas (CONSUMIDOR/ADMIN) o mis tiendas (PROVEEDOR)
    @GetMapping
    public String listarTiendas(Model model, Authentication authentication) {
        List<TiendaDTO> tiendas;
        String tipoLista;

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            tiendas = tiendaService.obtenerTodasLasTiendas();
            tipoLista = "todas";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            tiendas = tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario());
            tipoLista = "mis_tiendas";
        } else {
            // CONSUMIDOR ve solo tiendas activas
            tiendas = tiendaService.obtenerTiendasActivas();
            tipoLista = "activas";
        }

        model.addAttribute("tiendas", tiendas);
        model.addAttribute("tipoLista", tipoLista);
        return "tienda/list";
    }

    // Mis tiendas (PROVEEDOR)
    @GetMapping("/mis-tiendas")
    public String misTiendas(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/tiendas";
        }

        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
        List<TiendaDTO> tiendas = tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario());

        model.addAttribute("tiendas", tiendas);
        model.addAttribute("tipoLista", "mis_tiendas");
        return "tienda/list";
    }

    // Ver detalle de tienda + productos
    @GetMapping("/ver/{id}")
    public String verTienda(@PathVariable Long id, Model model) {
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(id);
        model.addAttribute("tienda", tienda);
        return "tienda/view";
    }

    // ✅ CORREGIDO: Crear tienda (PROVEEDOR)
    @GetMapping("/nueva")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/tiendas";
        }

        model.addAttribute("tienda", new TiendaDTO());
        // ✅ CRÍTICO: Pasar lista de categorías al formulario
        model.addAttribute("categoriasTienda", CategoriaTienda.values());
        return "tienda/form";
    }

    @PostMapping("/crear")
    public String crearTienda(@ModelAttribute TiendaDTO tiendaDTO,
                              @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/tiendas";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            tiendaDTO.setId_usuario(usuario.getId_usuario());

            tiendaService.crearTienda(tiendaDTO, imagen);
            redirectAttributes.addFlashAttribute("mensaje", "Tienda creada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear tienda: " + e.getMessage());
        }
        return "redirect:/tiendas/mis-tiendas";
    }

    // ✅ CORREGIDO: Editar tienda (PROVEEDOR/ADMIN)
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(id);

        // Verificar permisos
        if (!puedeEditarTienda(authentication, tienda)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para editar esta tienda.");
            return "redirect:/tiendas";
        }

        model.addAttribute("tienda", tienda);
        // ✅ CRÍTICO: Pasar lista de categorías al formulario de edición
        model.addAttribute("categoriasTienda", CategoriaTienda.values());
        return "tienda/edit";
    }

    @PostMapping("/actualizar")
    public String actualizarTienda(@ModelAttribute("tienda") TiendaDTO tiendaDTO,
                                   @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            TiendaDTO tiendaExistente = tiendaService.obtenerTiendaPorId(tiendaDTO.getId_tienda());

            if (!puedeEditarTienda(authentication, tiendaExistente)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para modificar esta tienda.");
                return "redirect:/tiendas";
            }

            tiendaService.actualizarTienda(tiendaDTO.getId_tienda(), tiendaDTO, imagen);
            redirectAttributes.addFlashAttribute("mensaje", "Tienda actualizada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar tienda: " + e.getMessage());
        }
        return "redirect:/tiendas/mis-tiendas";
    }

    // Cambiar estado (ADMIN)
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoTienda estado,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/tiendas";
        }

        try {
            tiendaService.cambiarEstadoTienda(id, estado);
            redirectAttributes.addFlashAttribute("mensaje", "Estado actualizado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
        }
        return "redirect:/tiendas";
    }

    // Eliminar (PROVEEDOR/ADMIN)
    @PostMapping("/eliminar/{id}")
    public String eliminarTienda(@PathVariable Long id, Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(id);

            if (!puedeEditarTienda(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar esta tienda.");
                return "redirect:/tiendas";
            }

            tiendaService.eliminarTienda(id);
            redirectAttributes.addFlashAttribute("mensaje", "Tienda eliminada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar tienda: " + e.getMessage());
        }
        return "redirect:/tiendas/mis-tiendas";
    }

    // Método auxiliar para verificar permisos
    private boolean puedeEditarTienda(Authentication authentication, TiendaDTO tienda) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return true;
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            return tienda.getId_usuario().equals(usuario.getId_usuario());
        }

        return false;
    }
}