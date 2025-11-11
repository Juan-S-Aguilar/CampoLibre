package com.example.campolibre.Controller;

import com.example.campolibre.DTO.EventoDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.TipoEvento;
import com.example.campolibre.Service.EmailService;
import com.example.campolibre.Service.EventoService;
import com.example.campolibre.Service.MisEventosService;
import com.example.campolibre.Service.UsuarioService;
import com.example.campolibre.Service.FileStorageService;
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
@RequestMapping("/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MisEventosService misEventosService;

    @Autowired
    private FileStorageService fileStorageService;

    // INYECCIÓN DEL SERVICIO DE CORREO
    @Autowired
    private EmailService emailService;

    // Ver eventos aprobados (todos pueden ver)
    @GetMapping
    public String listarEventos(@RequestParam(required = false) TipoEvento tipo,
                                Model model) {
        List<EventoDTO> eventos;

        if (tipo != null) {
            eventos = eventoService.obtenerEventosPorTipo(tipo);
        } else {
            eventos = eventoService.obtenerEventosAprobados();
        }

        model.addAttribute("eventos", eventos);
        model.addAttribute("tiposEvento", TipoEvento.values());
        model.addAttribute("tipoSeleccionado", tipo);
        return "evento/list";
    }

    // Ver eventos pendientes (ADMIN)
    @GetMapping("/pendientes")
    public String listarEventosPendientes(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/eventos";
        }

        List<EventoDTO> eventos = eventoService.obtenerEventosPendientes();
        model.addAttribute("eventos", eventos);
        model.addAttribute("tipoLista", "pendientes");
        return "evento/pendientes";
    }

    // Ver mis eventos creados (PROVEEDOR)
    @GetMapping("/mis-eventos")
    public String misEventos(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/eventos";
        }

        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
        List<EventoDTO> eventos = eventoService.obtenerEventosPorCreador(usuario.getId_usuario());

        model.addAttribute("eventos", eventos);
        model.addAttribute("tipoLista", "mis_eventos");
        return "evento/mis-eventos";
    }

    // Ver detalle de evento
    @GetMapping("/ver/{id}")
    public String verEvento(@PathVariable Long id, Model model, Authentication authentication) {
        EventoDTO evento = eventoService.obtenerEventoPorId(id);

        // Verificar si el usuario ya confirmó asistencia
        boolean yaConfirmo = false;
        if (authentication != null) {
            String email = authentication.getName();
            // Solo buscar usuario si el email no es null (ej: usuario anónimo)
            if (email != null && !email.equals("anonymousUser")) {
                UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
                yaConfirmo = misEventosService.usuarioConfirmoAsistencia(usuario.getId_usuario(), id);
            }
        }

        model.addAttribute("evento", evento);
        model.addAttribute("yaConfirmo", yaConfirmo);
        return "evento/view";
    }

    // 🎯 ENDPOINT: Guardar evento / Confirmar asistencia (CONSUMIDOR) - DISPARA CORREO
    @PostMapping("/confirmar/{idEvento}")
    public String confirmarAsistencia(@PathVariable Long idEvento,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {

        // 1. Asegurar que haya un usuario autenticado
        if (authentication == null || authentication.getName() == null || authentication.getName().equals("anonymousUser")) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para confirmar asistencia.");
            return "redirect:/login";
        }

        try {
            String emailConsumidor = authentication.getName();
            System.out.println("[EventoController] confirmarAsistencia llamada. idEvento=" + idEvento + ", emailConsumidor=" + emailConsumidor);

            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(emailConsumidor);
            EventoDTO evento = eventoService.obtenerEventoPorId(idEvento);

            if (usuario == null || evento == null) {
                redirectAttributes.addFlashAttribute("error", "Error: Datos de usuario o evento no encontrados.");
                return "redirect:/eventos/ver/" + idEvento;
            }

            // 2. Lógica principal: Guardar la relación en la base de datos
            System.out.println("[EventoController] Guardando asistencia para usuarioId=" + usuario.getId_usuario() + " eventoId=" + idEvento);
            misEventosService.guardarAsistencia(usuario.getId_usuario(), idEvento);
            System.out.println("[EventoController] Asistencia guardada para usuarioId=" + usuario.getId_usuario() + " eventoId=" + idEvento);

            // 3. ENVÍO AUTOMÁTICO DEL CORREO: enviar confirmación/invitación al consumidor
            try {
                if (emailConsumidor != null && !emailConsumidor.isEmpty()) {
                    System.out.println("[EventoController] Llamando a EmailService.enviarConfirmacionParticipacion para " + emailConsumidor);
                    emailService.enviarConfirmacionParticipacion(emailConsumidor, evento.getNombre());
                    System.out.println("[EventoController] llamada a EmailService finalizada para " + emailConsumidor);
                }
            } catch (Exception mailEx) {
                System.err.println("[EventoController] Error enviando correo tras confirmar asistencia: " + mailEx.getMessage());
                mailEx.printStackTrace();
            }

            redirectAttributes.addFlashAttribute("mensaje",
                    "¡Has confirmado tu asistencia a " + evento.getNombre() +
                            "! Se ha enviado la confirmación a tu correo electrónico.");

        } catch (Exception e) {
            System.err.println("❌ Error al confirmar asistencia y enviar correo: " + e.getMessage());
            e.printStackTrace();
            // Muestra el mensaje de error específico (ej: "Ya has guardado este evento...")
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/eventos/ver/" + idEvento;
    }


    // Crear evento (PROVEEDOR) - Estado PENDIENTE
    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/eventos";
        }

        model.addAttribute("evento", new EventoDTO());
        model.addAttribute("tiposEvento", TipoEvento.values());
        return "evento/form";
    }

    @PostMapping("/crear")
    public String crearEvento(@ModelAttribute EventoDTO eventoDTO,
                              @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/eventos";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            eventoDTO.setCreado_por(usuario.getId_usuario());
            eventoDTO.setEstado(EstadoEvento.PENDIENTE);

            // Guardar imagen si se proporcionó
            if (imagen != null && !imagen.isEmpty()) {
                String rutaImagen = fileStorageService.guardarArchivo(imagen, "eventos");
                eventoDTO.setImagen_evento(rutaImagen);
            }

            // Crear evento (pasar null como imagen ya que la guardamos manualmente)
            eventoService.crearEvento(eventoDTO, null);
            redirectAttributes.addFlashAttribute("mensaje", "Evento creado exitosamente. Pendiente de aprobación.");

        } catch (Exception e) {
            System.err.println("❌ Error al crear evento: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al crear evento: " + e.getMessage());
            return "redirect:/eventos/crear";
        }

        return "redirect:/eventos/mis-eventos";
    }

    // Editar evento (PROVEEDOR/ADMIN)
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id,
                                           Model model,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        EventoDTO evento = eventoService.obtenerEventoPorId(id);

        if (!puedeEditarEvento(authentication, evento)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para editar este evento.");
            return "redirect:/eventos";
        }

        model.addAttribute("evento", evento);
        model.addAttribute("tiposEvento", TipoEvento.values());
        return "evento/edit";
    }

    @PostMapping("/actualizar")
    public String actualizarEvento(@ModelAttribute("evento") EventoDTO eventoDTO,
                                   @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            EventoDTO eventoExistente = eventoService.obtenerEventoPorId(eventoDTO.getId_evento());

            if (!puedeEditarEvento(authentication, eventoExistente)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para modificar este evento.");
                return "redirect:/eventos";
            }

            // Si hay nueva imagen
            if (imagen != null && !imagen.isEmpty()) {
                // Eliminar imagen anterior
                if (eventoExistente.getImagen_evento() != null) {
                    fileStorageService.eliminarArchivo(eventoExistente.getImagen_evento());
                }

                // Guardar nueva imagen
                String rutaImagen = fileStorageService.guardarArchivo(imagen, "eventos");
                eventoDTO.setImagen_evento(rutaImagen);
            } else {
                // Mantener imagen anterior
                eventoDTO.setImagen_evento(eventoExistente.getImagen_evento());
            }

            eventoService.actualizarEvento(eventoDTO.getId_evento(), eventoDTO, null);
            redirectAttributes.addFlashAttribute("mensaje", "Evento actualizado correctamente.");

        } catch (Exception e) {
            // Se corrige el error de sintaxis y se completa el catch/return
            System.err.println("❌ Error al actualizar evento: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar evento: " + e.getMessage());
        }

        return "redirect:/eventos/mis-eventos";
    }

    // Aprobar evento (ADMIN)
    @GetMapping("/aprobar/{id}")
    public String aprobarEvento(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/eventos";
        }

        try {
            eventoService.cambiarEstadoEvento(id, EstadoEvento.APROBADO);
            redirectAttributes.addFlashAttribute("mensaje", "Evento aprobado exitosamente.");

            // Enviar invitación por correo a todos los usuarios activos
            try {
                EventoDTO evento = eventoService.obtenerEventoPorId(id);
                List<com.example.campolibre.DTO.UsuarioDTO> usuarios = usuarioService.obtenerUsuariosActivos();
                if (usuarios != null) {
                    for (com.example.campolibre.DTO.UsuarioDTO u : usuarios) {
                        try {
                            if (u.getEmail() != null && !u.getEmail().isEmpty()) {
                                emailService.enviarInvitacion(u.getEmail(), evento.getNombre());
                            }
                        } catch (Exception mailEx) {
                            System.err.println("Advertencia: error al enviar invitación a " + u.getEmail() + ": " + mailEx.getMessage());
                        }
                    }
                }
            } catch (Exception eSend) {
                System.err.println("Advertencia: error al enviar invitaciones tras aprobar evento: " + eSend.getMessage());
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al aprobar evento: " + e.getMessage());
        }
        return "redirect:/eventos/pendientes";
    }

    // Rechazar evento (ADMIN)
    @GetMapping("/rechazar/{id}")
    public String rechazarEvento(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/eventos";
        }

        try {
            eventoService.cambiarEstadoEvento(id, EstadoEvento.RECHAZADO);
            redirectAttributes.addFlashAttribute("mensaje", "Evento rechazado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al rechazar evento: " + e.getMessage());
        }
        return "redirect:/eventos/pendientes";
    }

    // Eliminar evento (PROVEEDOR/ADMIN)
    @GetMapping("/eliminar/{id}")
    public String eliminarEvento(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            EventoDTO evento = eventoService.obtenerEventoPorId(id);

            if (!puedeEditarEvento(authentication, evento)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar este evento.");
                return "redirect:/eventos";
            }

            // Eliminar imagen del sistema de archivos
            if (evento.getImagen_evento() != null) {
                fileStorageService.eliminarArchivo(evento.getImagen_evento());
            }

            eventoService.eliminarEvento(id);
            redirectAttributes.addFlashAttribute("mensaje", "Evento eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar evento: " + e.getMessage());
        }
        return "redirect:/eventos/mis-eventos";
    }

    // Método auxiliar para verificar permisos
    private boolean puedeEditarEvento(Authentication authentication, EventoDTO evento) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return true;
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            return evento.getCreado_por().equals(usuario.getId_usuario());
        }

        return false;
    }
}