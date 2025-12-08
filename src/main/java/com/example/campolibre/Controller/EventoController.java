package com.example.campolibre.Controller;

// ⚠️ CAMBIO: Importar los nuevos DTOs y Servicios
import com.example.campolibre.DTO.*;
import com.example.campolibre.Enum.EstadoCupo;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.MetodoPago;
import com.example.campolibre.Enum.TipoEvento;
import com.example.campolibre.Service.*; // Importar todos los servicios
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    // --- ⚠️ CAMBIO: Inyección de TODOS los servicios necesarios ---
    @Autowired private EventoService eventoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private MisEventosService misEventosService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private EmailService emailService; // Se mantiene por si el Admin notifica

    // ⚠️ NUEVAS INYECCIONES
    @Autowired private PatrocinadorService patrocinadorService;
    @Autowired private InscripcionProveedorService inscripcionProveedorService;
    @Autowired private PdfService pdfService;

    @Autowired private PagoEventoService pagoEventoService;



    // --- Lógica del Administrador ---

    /**
     * ⚠️ CAMBIO: Formulario de CREACIÓN ahora es solo para ADMIN.
     * Necesita cargar la lista de patrocinadores.
     */
    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/eventos";
        }

        // ✅ Cargar solo patrocinadores ACTIVOS para el dropdown
        List<PatrocinadorDTO> patrocinadores = patrocinadorService.obtenerPatrocinadoresActivos();

        model.addAttribute("evento", new EventoCreacionDTO()); // ⚠️ Usar el DTO de Creación
        model.addAttribute("tiposEvento", TipoEvento.values());
        model.addAttribute("patrocinadores", patrocinadores); // ⚠️ Añadir patrocinadores
        return "evento/form"; // Asumimos que la vista 'form' se adaptará
    }

    /**
     * ⚠️ CAMBIO: CREACIÓN de evento solo por ADMIN.
     * Usa EventoCreacionDTO.
     */
    @PostMapping("/crear")
    public String crearEvento(@ModelAttribute("evento") EventoCreacionDTO eventoCreacionDTO, // ⚠️ Usar DTO de Creación
                              @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/eventos";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO admin = usuarioService.obtenerUsuarioPorEmail(email);

            // ⚠️ Llamar al nuevo método de servicio
            eventoService.crearEvento(eventoCreacionDTO, admin.getId_usuario(), imagen);
            redirectAttributes.addFlashAttribute("mensaje", "Evento creado exitosamente. Pendiente de aprobación.");

        } catch (Exception e) {
            System.err.println("❌ Error al crear evento: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al crear evento: " + e.getMessage());
            return "redirect:/eventos/crear";
        }

        return "redirect:/eventos/pendientes"; // Redirigir a pendientes
    }


    /**
     * ⚠️ CAMBIO: Formulario de EDICIÓN ahora es solo para ADMIN.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id,
                                           Model model,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para editar este evento.");
            return "redirect:/eventos";
        }

        EventoDTO evento = eventoService.obtenerEventoPorId(id);
        // ✅ Cargar solo patrocinadores ACTIVOS para el dropdown
        List<PatrocinadorDTO> patrocinadores = patrocinadorService.obtenerPatrocinadoresActivos();

        // Mapeo completo
        EventoCreacionDTO eventoEdit = new EventoCreacionDTO();

        // 1. Agregar ID
        eventoEdit.setId_evento(evento.getId_evento()); // ⬅️ AGREGADO: Necesario para la actualización

        // 2. Agregar Propiedades de Evento
        eventoEdit.setNombre(evento.getNombre());
        eventoEdit.setDescripcion(evento.getDescripcion());
        eventoEdit.setUbicacion(evento.getUbicacion());
        eventoEdit.setDireccionCompleta(evento.getDireccionCompleta());
        eventoEdit.setFecha_evento(evento.getFecha_evento());
        eventoEdit.setHora_evento(evento.getHora_evento());
        eventoEdit.setTipo_evento(evento.getTipo_evento());
        eventoEdit.setId_patrocinador(evento.getId_patrocinador());
        eventoEdit.setCuposMaximosProveedor(evento.getCuposMaximosProveedor());
        eventoEdit.setCostoEspacio(evento.getCostoEspacio());
        eventoEdit.setTerminosCondiciones(evento.getTerminosCondiciones());

        // 3. Agregar imagen_evento (para mostrar la actual)
        eventoEdit.setImagen_evento(evento.getImagen_evento()); // ⬅️ AGREGADO: Imagen actual

        // 4. Agregar cuposOcupados (para advertencias)
        eventoEdit.setCuposOcupados(evento.getCuposOcupados()); // ⬅️ AGREGADO: Cupos ocupados


        model.addAttribute("evento", eventoEdit); // ✅ Enviar el DTO mapeado
        model.addAttribute("tiposEvento", TipoEvento.values());
        model.addAttribute("patrocinadores", patrocinadores);
        return "evento/edit";
    }

    /**
     * ⚠️ CAMBIO: ACTUALIZACIÓN de evento solo por ADMIN.
     */
    @PostMapping("/actualizar")
    public String actualizarEvento(@ModelAttribute("evento") EventoCreacionDTO eventoCreacionDTO,
                                   @RequestParam("id_evento") Long idEvento,
                                   @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para modificar este evento.");
            return "redirect:/eventos";
        }

        try {
            // ✅ El service maneja todo (imagen, validaciones, etc.)
            eventoService.actualizarEvento(idEvento, eventoCreacionDTO, imagen);
            redirectAttributes.addFlashAttribute("mensaje", "Evento actualizado correctamente.");
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar evento: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar evento: " + e.getMessage());
        }

        return "redirect:/eventos/pendientes";
    }

    // Ver eventos pendientes (ADMIN)
    @GetMapping("/pendientes")
    public String listarEventosPendientes(Model model, Authentication authentication) {
        // Validación de rol (se mantiene)
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/eventos";
        }

        List<EventoDTO> eventos = eventoService.obtenerEventosBorrador();
        model.addAttribute("eventos", eventos);
        model.addAttribute("tipoLista", "pendientes");
        return "evento/pendientes";
    }

    @GetMapping("/aprobar/{id}")
    public String aprobarEvento(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        /* ... (código sin cambios, aunque el envío masivo de correos es opcional) ... */
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) { return "redirect:/eventos"; }
        eventoService.cambiarEstadoEvento(id, EstadoEvento.PUBLICADO);
        redirectAttributes.addFlashAttribute("mensaje", "Evento aprobado.");
        // Opcional: Notificar a todos los usuarios
        return "redirect:/eventos/pendientes";
    }

    @GetMapping("/rechazar/{id}")
    public String rechazarEvento(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) { return "redirect:/eventos"; }
        eventoService.cambiarEstadoEvento(id, EstadoEvento.CANCELADO);
        redirectAttributes.addFlashAttribute("mensaje", "Evento rechazado.");
        return "redirect:/eventos/pendientes";
    }

    /**
     * ✅ NUEVO: Iniciar evento (solo ADMIN)
     */
    @PostMapping("/admin/iniciar-evento/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public String iniciarEvento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventoService.cambiarEstadoEvento(id, EstadoEvento.EN_CURSO);
            redirectAttributes.addFlashAttribute("success", "Evento iniciado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al iniciar evento: " + e.getMessage());
        }
        return "redirect:/eventos/admin/todos";
    }

    /**
     * ✅ NUEVO: Finalizar evento (solo ADMIN)
     */
    @PostMapping("/admin/finalizar-evento/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public String finalizarEvento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventoService.cambiarEstadoEvento(id, EstadoEvento.FINALIZADO);
            redirectAttributes.addFlashAttribute("success", "Evento finalizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al finalizar evento: " + e.getMessage());
        }
        return "redirect:/eventos/admin/todos";
    }

    // ⚠️ CAMBIO: Eliminar solo ADMIN
    @GetMapping("/eliminar/{id}")
    public String eliminarEvento(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar este evento.");
            return "redirect:/eventos";
        }
        // ... (lógica de eliminación de archivo y evento) ...
        eventoService.eliminarEvento(id);
        return "redirect:/eventos/pendientes";
    }

    // --- Lógica del Consumidor ---

    // Ver eventos aprobados (todos pueden ver)
    @GetMapping
    public String listarEventos(@RequestParam(required = false) TipoEvento tipo,
                                Model model) {
        List<EventoDTO> eventos;

        if (tipo != null) {
            // Obtener eventos APROBADOS por tipo
            eventos = eventoService.obtenerEventosPorTipo(tipo);
        } else {
            // Obtener todos los eventos APROBADOS
            eventos = eventoService.obtenerEventosPublicados();
        }

        model.addAttribute("eventos", eventos);
        model.addAttribute("tiposEvento", TipoEvento.values());
        model.addAttribute("tipoSeleccionado", tipo);
        return "evento/list";
    }

    // DETALLE DE EVENTO
    @GetMapping("/ver/{id}")
    public String verEvento(@PathVariable Long id, Model model, Authentication authentication) {
        // 1. Obtener información del evento
        EventoDTO evento = eventoService.obtenerEventoPorId(id);

        // El DTO ya tiene cuposDisponibles calculado
        boolean hayCupos = evento.getCuposDisponibles() != null && evento.getCuposDisponibles() > 0;

        // 2. INICIALIZAR VARIABLES (Esto evita el error de NULL en la vista)
        boolean yaGuardoEvento = false;
        boolean yaSeInscribio = false;
        boolean isPagoPendiente = false;       
        Long idInscripcionPendiente = null;    

        // 3. Lógica si el usuario está logueado
        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // --- Lógica CONSUMIDOR ---
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("CONSUMIDOR"))) {
                yaGuardoEvento = misEventosService.usuarioTieneEventoGuardado(usuario.getId_usuario(), id);
            }

            // --- Lógica PROVEEDOR ---
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {

                // Intentamos obtener la inscripción específica de este usuario en este evento
                // NOTA: Asegúrate de tener este método en tu servicio, o uno similar que devuelva el objeto InscripcionDTO
                // Si no tienes este método exacto, úsalo como guía de lo que necesitas recuperar.
                InscripcionProveedorDTO inscripcion = inscripcionProveedorService
                        .obtenerInscripcionPorUsuarioYEvento(usuario.getId_usuario(), id);

                if (inscripcion != null) {
                    yaSeInscribio = true;

                    // Verificamos si el estado es PENDIENTE
                    if (inscripcion.getEstadoCupo() == com.example.campolibre.Enum.EstadoCupo.PENDIENTE) {
                        isPagoPendiente = true;
                        idInscripcionPendiente = inscripcion.getId_inscripcion();
                    }
                }
            }
        }

        // 4. Enviar TODO al modelo (Blindaje contra nulos)
        model.addAttribute("evento", evento);
        model.addAttribute("hayCupos", hayCupos);

        model.addAttribute("yaGuardoEvento", yaGuardoEvento);
        model.addAttribute("yaSeInscribio", yaSeInscribio);

        // ESTAS DOS LÍNEAS ARREGLAN TU ERROR EN PANTALLA BLANCA:
        model.addAttribute("isPagoPendiente", isPagoPendiente);
        model.addAttribute("idInscripcionPendiente", idInscripcionPendiente);

        return "evento/view";
    }

    /**
     * ⚠️ CAMBIO: Confirmar ahora es "Guardar Intención de Asistencia" (Consumidor).
     * El servicio interno (MisEventosImplement) se encarga del correo.
     */
    @PostMapping("/guardar-intencion/{idEvento}")
    public String guardarIntencionAsistencia(@PathVariable Long idEvento,
                                             Authentication authentication,
                                             RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("CONSUMIDOR"))) {
            redirectAttributes.addFlashAttribute("error", "Solo los consumidores pueden guardar eventos.");
            return "redirect:/eventos/ver/" + idEvento;
        }

        try {
            String emailConsumidor = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(emailConsumidor);

            // ⚠️ CAMBIO: Llamar al nuevo método de servicio
            misEventosService.guardarIntencionAsistencia(usuario.getId_usuario(), idEvento);

            // ⚠️ Ya no se envía el correo desde el controlador. El servicio lo hace.

            redirectAttributes.addFlashAttribute("mensaje",
                    "¡Evento guardado! Revisa tu correo electrónico para ver los detalles y tu pase de asistencia.");

        } catch (Exception e) {
            System.err.println("❌ Error al guardar intención de asistencia: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/eventos/ver/" + idEvento;
    }

    // --- Lógica del Proveedor ---

    /**
     * ⚠️ CAMBIO: "Mis Eventos" para el Proveedor ahora muestra sus cupos CONFIRMADOS.
     */
    @GetMapping("/mis-inscripciones")
    public String misInscripciones(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/eventos";
        }

        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

        // ✅ Obtener TODAS las inscripciones (incluye PENDIENTES, CONFIRMADAS y CANCELADAS)
        var inscripciones = inscripcionProveedorService.obtenerTodasLasInscripcionesDeProveedor(usuario.getId_usuario());

        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("tipoLista", "mis_inscripciones");
        return "evento/mis-inscripciones";
    }

    /**
     * ⚠️ NUEVO ENDPOINT: Proveedor solicita un cupo (Pasa a PENDIENTE_PAGO).
     */
    @PostMapping("/inscribir-proveedor/{idEvento}")
    public String solicitarCupoProveedor(@PathVariable Long idEvento,
                                         Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/login";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO proveedor = usuarioService.obtenerUsuarioPorEmail(email);

            // Validación temprana (opcional pero buena práctica)
            if (!inscripcionProveedorService.hayCuposDisponibles(idEvento)) {
                redirectAttributes.addFlashAttribute("error", "Lo sentimos, no quedan cupos disponibles.");
                return "redirect:/eventos/ver/" + idEvento;
            }

            var inscripcion = inscripcionProveedorService.solicitarCupo(proveedor.getId_usuario(), idEvento);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Cupo reservado temporalmente. Completa el pago para confirmarlo.");

            // ✅ Redirigir al controller de pagos
            return "redirect:/pagos-eventos/checkout/" + inscripcion.getId_inscripcion();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al solicitar cupo: " + e.getMessage());
            return "redirect:/eventos/ver/" + idEvento;
        }
    }



    /**
     * Proveedor cancela su inscripción (si está PENDIENTE_PAGO o CONFIRMADO).
     */
    @PostMapping("/cancelar-inscripcion/{idInscripcion}")
    @PreAuthorize("hasAuthority('PROVEEDOR')")
    public String cancelarInscripcion(@PathVariable Long idInscripcion,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // Obtener inscripción para validar propiedad
            InscripcionProveedorDTO inscripcion = inscripcionProveedorService.obtenerInscripcionPorId(idInscripcion);

            if (!inscripcion.getId_proveedor().equals(usuario.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para cancelar esta inscripción");
                return "redirect:/eventos/mis-inscripciones";
            }

            // Cancelar con reembolso
            inscripcionProveedorService.cancelarInscripcion(idInscripcion, "Cancelado por el proveedor");

            // ✅ MENSAJE DE ÉXITO CON INFORMACIÓN DE REEMBOLSO
            redirectAttributes.addFlashAttribute("success",
                "Inscripción cancelada exitosamente. El proceso de reembolso se llevará a cabo mediante correo electrónico en los próximos 5-7 días hábiles.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar: " + e.getMessage());
        }

        return "redirect:/eventos/mis-inscripciones";
    }


    //  NUEVO ENDPOINT PARA EL ADMINISTRADOR (Lista TODOS los eventos)
    @GetMapping("/admin/todos") // Puedes usar "/mis-eventos-admin" o similar
    public String listarTodosLosEventosAdmin(Model model, Authentication authentication) {

        // 1. Verificar Rol de Administrador
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/eventos"; // Redirigir si no es admin
        }

        // 2. Obtener ID del Admin
        String email = authentication.getName();
        UsuarioDTO admin = usuarioService.obtenerUsuarioPorEmail(email); // Asegúrate de que esto devuelve el ID

        if (admin == null) {
            // Manejar error
            return "redirect:/login";
        }

        Long idAdmin = admin.getId_usuario();

        // 3. Usar el NUEVO SERVICIO (Trae TODOS los estados: Borrador, Publicado, Finalizado, etc.)
        List<EventoDTO> eventos = eventoService.obtenerTodosLosEventosCreadosPorAdministrador(idAdmin);

        // 4. Enviar la lista completa a la vista
        model.addAttribute("eventos", eventos);
        model.addAttribute("tipoLista", "admin_todos"); // Útil para filtros en la vista

        // IMPORTANTE: Necesitas una vista llamada "evento/admin-list" o similar.
        return "evento/admin-list";
    }

    @GetMapping("/proveedor/todos")
    @PreAuthorize("hasAuthority('PROVEEDOR')")
    public String listarEventosProveedor(Model model) {

        // ✅ Usamos el método que trae TODOS menos los BORRADOR
        List<EventoDTO> eventos = eventoService.obtenerEventosVisiblesParaPublico();

        // Usar la vista del administrador para el listado, pero con restricciones.
        // Usaremos "evento/proveedor-list" para evitar modificar la vista pública "list.html".
        model.addAttribute("eventos", eventos);

        // Necesitas una vista dedicada o modificar list.html, ver Paso 4.
        return "evento/proveedor-list";
    }

    /**
     * ⚠️ NUEVO ENDPOINT: Reporte de proveedores inscritos a un evento (ADMIN)
     */
    @GetMapping("/admin/reporte-inscripciones/{idEvento}")
    public String verReporteInscripciones(@PathVariable Long idEvento,
                                          Model model,
                                          Authentication authentication,
                                          RedirectAttributes redirectAttributes) {
        // 1. Verificar que sea ADMINISTRADOR
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para ver este reporte.");
            return "redirect:/eventos";
        }

        try {
            // 2. Obtener el reporte completo desde el servicio
            ReporteInscripcionesEventoDTO reporte = eventoService.obtenerReporteInscripciones(idEvento);
            EventoDTO evento = eventoService.obtenerEventoPorId(idEvento);

            // 3. Enviar datos a la vista
            model.addAttribute("reporte", reporte);
            model.addAttribute("evento", evento);

            return "evento/reporte-inscripciones";

        } catch (Exception e) {
            System.err.println("❌ Error al obtener reporte de inscripciones: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al cargar el reporte: " + e.getMessage());
            return "redirect:/eventos/admin/todos";
        }
    }

    /**
     * ✅ NUEVO: Descargar PDF con la lista de proveedores inscritos
     */
    @GetMapping("/admin/reporte-inscripciones/{idEvento}/pdf")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<InputStreamResource> descargarPdfProveedores(@PathVariable Long idEvento)
            throws Exception {

        // 1. Obtener el reporte de inscripciones
        ReporteInscripcionesEventoDTO reporte = eventoService.obtenerReporteInscripciones(idEvento);
        EventoDTO evento = eventoService.obtenerEventoPorId(idEvento);

        // 2. Preparar datos para el PDF (solo proveedores CONFIRMADOS)
        List<Map<String, Object>> inscripciones = new ArrayList<>();
        int contador = 1;

        // ✅ Filtrar solo inscripciones confirmadas
        List<InscripcionProveedorDTO> confirmados = reporte.getProveedoresInscritos().stream()
                .filter(insc -> insc.getEstadoCupo() == EstadoCupo.CONFIRMADO)
                .collect(Collectors.toList());

        for (InscripcionProveedorDTO insc : confirmados) {
            Map<String, Object> datos = new HashMap<>();
            datos.put("numero", contador++);
            datos.put("nombre", insc.getNombreProveedor());
            datos.put("email", insc.getEmailProveedor());
            datos.put("telefono", insc.getTelefonoProveedor() != null ? insc.getTelefonoProveedor() : "N/A");
            datos.put("fechaInscripcion", insc.getFechaInscripcion() != null ?
                    insc.getFechaInscripcion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");
            datos.put("codigoConfirmacion", insc.getCodigoConfirmacion());
            inscripciones.add(datos);
        }

        // 3. Generar PDF
        String fechaEvento = evento.getFecha_evento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " " + evento.getHora_evento().format(DateTimeFormatter.ofPattern("HH:mm"));

        ByteArrayInputStream bais = pdfService.generarListaProveedores(
                inscripciones,
                evento.getNombre(),
                fechaEvento
        );

        // 4. Configurar respuesta HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=proveedores_" +
                evento.getNombre().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bais));
    }
}