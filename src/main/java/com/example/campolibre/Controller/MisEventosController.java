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

import java.util.List;

@Controller
@RequestMapping("/mis-eventos")
public class MisEventosController {

    @Autowired
    private MisEventosService misEventosService;

    @Autowired
    private UsuarioService usuarioService;

    // Ver mis eventos confirmados
    @GetMapping
    public String listarMisEventos(Model model, Authentication authentication) {
        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

        List<MisEventosDTO> misEventos = misEventosService.obtenerEventosDeUsuario(usuario.getId_usuario());

        model.addAttribute("misEventos", misEventos);
        return "mis-eventos/list";
    }

    // Confirmar asistencia a un evento
    @PostMapping("/confirmar/{idEvento}")
    public String confirmarAsistencia(@PathVariable Long idEvento,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            MisEventosDTO misEventosDTO = new MisEventosDTO();
            misEventosDTO.setId_usuario(usuario.getId_usuario());
            misEventosDTO.setId_evento(idEvento);

            misEventosService.confirmarAsistencia(misEventosDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Asistencia confirmada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/eventos/ver/" + idEvento;
    }

    // Cancelar asistencia
    @GetMapping("/cancelar/{id}")
    public String cancelarAsistencia(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            misEventosService.cancelarAsistencia(id);
            redirectAttributes.addFlashAttribute("mensaje", "Asistencia cancelada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar asistencia: " + e.getMessage());
        }
        return "redirect:/mis-eventos";
    }
}