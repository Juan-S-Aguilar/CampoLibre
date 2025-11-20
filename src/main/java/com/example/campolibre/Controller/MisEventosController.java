package com.example.campolibre.Controller;

import com.example.campolibre.DTO.MisEventosDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Service.MisEventosService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mis-eventos")
public class MisEventosController {

    @Autowired
    private MisEventosService misEventosService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Listar eventos guardados por el consumidor.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CONSUMIDOR')") // ✅ Usar anotación
    public String listarMisEventos(Model model, Authentication authentication) {
        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

        List<MisEventosDTO> misEventos = misEventosService.obtenerEventosGuardadosDeUsuario(usuario.getId_usuario());

        model.addAttribute("misEventos", misEventos);
        model.addAttribute("totalEventos", misEventos.size()); // ✅ Agregar contador

        return "mis-eventos/list";
    }

    /**
     * Remover un evento de la lista de guardados.
     */
    @PostMapping("/remover/{idEvento}")
    @PreAuthorize("hasAuthority('CONSUMIDOR')") // ✅ Usar anotación
    public String removerIntencionAsistencia(@PathVariable Long idEvento,
                                             Authentication authentication,
                                             RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            misEventosService.removerIntencionAsistencia(usuario.getId_usuario(), idEvento);
            redirectAttributes.addFlashAttribute("mensaje", "Evento removido de tu lista.");

        } catch (Exception e) {
            System.err.println("❌ Error al remover evento: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al remover evento: " + e.getMessage());
        }

        return "redirect:/mis-eventos";
    }

    /**
     * Ver detalles de un evento guardado (opcional).
     */
    @GetMapping("/ver/{idEvento}")
    @PreAuthorize("hasAuthority('CONSUMIDOR')")
    public String verEventoGuardado(@PathVariable Long idEvento,
                                    Model model,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // Verificar que el usuario tenga el evento guardado
            if (!misEventosService.usuarioTieneEventoGuardado(usuario.getId_usuario(), idEvento)) {
                redirectAttributes.addFlashAttribute("error", "Este evento no está en tu lista.");
                return "redirect:/mis-eventos";
            }

            // Redirigir a la vista general del evento
            return "redirect:/eventos/ver/" + idEvento;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/mis-eventos";
        }
    }

    /**
     * Limpiar todos los eventos guardados (opcional).
     */
    @PostMapping("/limpiar-todo")
    @PreAuthorize("hasAuthority('CONSUMIDOR')")
    public String limpiarTodosLosEventos(Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            List<MisEventosDTO> eventos = misEventosService.obtenerEventosGuardadosDeUsuario(usuario.getId_usuario());

            // Remover cada evento
            for (MisEventosDTO evento : eventos) {
                misEventosService.removerIntencionAsistencia(usuario.getId_usuario(), evento.getId_evento());
            }

            redirectAttributes.addFlashAttribute("mensaje",
                    "Se eliminaron " + eventos.size() + " eventos de tu lista.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/mis-eventos";
    }
}