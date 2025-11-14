package com.example.campolibre.Controller;

import com.example.campolibre.DTO.MisEventosDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Service.MisEventosService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Controller
@RequestMapping("/mis-eventos")
public class MisEventosController {

    @Autowired
    private MisEventosService misEventosService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listarMisEventos(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        // Asegurar que solo los consumidores usen este endpoint
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("CONSUMIDOR"))) {
            redirectAttributes.addFlashAttribute("error", "Funcionalidad solo para Consumidores.");
            return "redirect:/eventos";
        }

        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

        // ⚠️ CAMBIO CLAVE: Usar el método correcto para obtener la lista de guardados
        List<MisEventosDTO> misEventos = misEventosService.obtenerEventosGuardadosDeUsuario(usuario.getId_usuario());

        model.addAttribute("misEventos", misEventos);
        return "mis-eventos/list";
    }

    /**
     * Permite al Consumidor remover un evento de su lista de guardados.
     */
    @PostMapping("/remover/{idEvento}")
    public String removerIntencionAsistencia(@PathVariable Long idEvento,
                                             Authentication authentication,
                                             RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("CONSUMIDOR"))) {
            return "redirect:/eventos";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // ⚠️ CAMBIO CLAVE: Usar el método de remover
            misEventosService.removerIntencionAsistencia(usuario.getId_usuario(), idEvento);
            redirectAttributes.addFlashAttribute("mensaje", "Evento removido de tu lista.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al remover evento: " + e.getMessage());
        }
        return "redirect:/mis-eventos";
    }


}