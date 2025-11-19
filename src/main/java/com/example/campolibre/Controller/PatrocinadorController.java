    package com.example.campolibre.Controller;

    import com.example.campolibre.DTO.PatrocinadorDTO;
    import com.example.campolibre.Service.PatrocinadorService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.util.List;

    @Controller
    @RequestMapping("/admin/patrocinadores")
    public class PatrocinadorController {

        private final PatrocinadorService patrocinadorService;

        @Autowired
        public PatrocinadorController(PatrocinadorService patrocinadorService) {
            this.patrocinadorService = patrocinadorService;
        }

        // Archivo: PatrocinadorController.java

    // ... (Inyecciones)

        /**
         * 1. Listar patrocinadores con filtro opcional
         */
        @GetMapping
        @PreAuthorize("hasAuthority('ADMINISTRADOR')")
        public String listarPatrocinadores(@RequestParam(required = false) Boolean soloActivos,
                                           Model model) {
            List<PatrocinadorDTO> patrocinadores;

            if (soloActivos != null && soloActivos) {
                patrocinadores = patrocinadorService.obtenerPatrocinadoresActivos();
            } else {
                patrocinadores = patrocinadorService.obtenerTodosLosPatrocinadores();
            }

            model.addAttribute("patrocinadores", patrocinadores);
            model.addAttribute("soloActivos", soloActivos);
            return "patrocinadores/list";
        }

        /**
         * 2. Mostrar formulario de creación (Solo ADMIN)
         */
        @GetMapping("/crear")
        @PreAuthorize("hasAuthority('ADMINISTRADOR')") // ✅ Corrección
        public String mostrarFormularioCreacion(Model model) { // Eliminamos Authentication y RedirectAttributes

            // Bloque IF eliminado

            model.addAttribute("patrocinador", new PatrocinadorDTO());
            return "patrocinadores/form";
        }

        /**
         * 3. Crear nuevo patrocinador (Solo ADMIN)
         */
        @PostMapping("/crear")
        @PreAuthorize("hasAuthority('ADMINISTRADOR')")
        public String crearPatrocinador(@ModelAttribute PatrocinadorDTO patrocinadorDTO,
                                        RedirectAttributes redirectAttributes) {
            try {
                // Validación básica
                if (patrocinadorDTO.getNombre() == null || patrocinadorDTO.getNombre().trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "El nombre del patrocinador es obligatorio.");
                    return "redirect:/admin/patrocinadores/crear";
                }

                patrocinadorService.crearPatrocinador(patrocinadorDTO);
                redirectAttributes.addFlashAttribute("mensaje", "Patrocinador creado exitosamente.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error al crear Patrocinador: " + e.getMessage());
                return "redirect:/admin/patrocinadores/crear";
            }

            return "redirect:/admin/patrocinadores";
        }

        /**
         * 4. Mostrar formulario de edición (Solo ADMIN)
         */
        @GetMapping("/editar/{id}")
        @PreAuthorize("hasAuthority('ADMINISTRADOR')") // ✅ Corrección
        public String mostrarFormularioEdicion(@PathVariable Long id,
                                               Model model,
                                               RedirectAttributes redirectAttributes) { // Eliminamos Authentication

            // Bloque IF eliminado

            try {
                PatrocinadorDTO patrocinador = patrocinadorService.obtenerPatrocinadorPorId(id);
                model.addAttribute("patrocinador", patrocinador);
                return "patrocinadores/edit";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Patrocinador no encontrado.");
                return "redirect:/admin/patrocinadores";
            }
        }

        /**
         * 5. Actualizar patrocinador (Solo ADMIN)
         */
        @PostMapping("/actualizar/{id}")
        @PreAuthorize("hasAuthority('ADMINISTRADOR')") // ✅ Corrección
        public String actualizarPatrocinador(@PathVariable Long id,
                                             @ModelAttribute PatrocinadorDTO patrocinadorDTO,
                                             RedirectAttributes redirectAttributes) { // Eliminamos Authentication

            // Bloque IF eliminado

            try {
                patrocinadorService.actualizarPatrocinador(id, patrocinadorDTO);
                redirectAttributes.addFlashAttribute("mensaje", "Patrocinador actualizado exitosamente.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error al actualizar Patrocinador: " + e.getMessage());
            }

            return "redirect:/admin/patrocinadores";
        }

        /**
         * 6. Eliminar patrocinador (Solo ADMIN)
         * (Este método ya estaba bien)
         */
        @GetMapping("/eliminar/{id}")
        @PreAuthorize("hasAuthority('ADMINISTRADOR')")
        public String eliminarPatrocinador(@PathVariable Long id,
                                           RedirectAttributes redirectAttributes) { // Eliminamos Authentication

            try {
                patrocinadorService.eliminarPatrocinador(id);
                redirectAttributes.addFlashAttribute("mensaje", "Patrocinador eliminado correctamente.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
            }

            return "redirect:/admin/patrocinadores";
        }
        /**
         * 7. Cambiar estado del patrocinador (activar/desactivar)
         */
        @PostMapping("/cambiar-estado/{id}")
        @PreAuthorize("hasAuthority('ADMINISTRADOR')")
        public String cambiarEstadoPatrocinador(@PathVariable Long id,
                                                @RequestParam Boolean activo,
                                                RedirectAttributes redirectAttributes) {
            try {
                patrocinadorService.cambiarEstadoPatrocinador(id, activo);
                String mensaje = activo ? "Patrocinador activado." : "Patrocinador archivado.";
                redirectAttributes.addFlashAttribute("mensaje", mensaje);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            }

            return "redirect:/admin/patrocinadores";
        }
        /**
         * 8. Ver detalles de un patrocinador + eventos asociados
         */
        @GetMapping("/ver/{id}")
        @PreAuthorize("hasAuthority('ADMINISTRADOR')")
        public String verPatrocinador(@PathVariable Long id,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
            try {
                PatrocinadorDTO patrocinador = patrocinadorService.obtenerPatrocinadorPorId(id);

                // Opcional: Agregar eventos que ha patrocinado
                // List<EventoDTO> eventos = eventoService.obtenerEventosPorPatrocinador(id);

                model.addAttribute("patrocinador", patrocinador);
                // model.addAttribute("eventos", eventos);

                return "patrocinadores/view";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Patrocinador no encontrado.");
                return "redirect:/admin/patrocinadores";
            }
        }

    }
