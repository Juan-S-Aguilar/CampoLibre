package com.example.campolibre.Controller;

// ⚠️ CAMBIO: Importar los nuevos DTOs y Servicios
import com.example.campolibre.DTO.*;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.MetodoPago;
import com.example.campolibre.Enum.TipoEvento;
import com.example.campolibre.Service.*; // Importar todos los servicios
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

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

        // Cargar patrocinadores para el dropdown
        List<PatrocinadorDTO> patrocinadores = patrocinadorService.obtenerTodosLosPatrocinadores();

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
        List<PatrocinadorDTO> patrocinadores = patrocinadorService.obtenerTodosLosPatrocinadores();

        // Mapeo completo
        EventoCreacionDTO eventoEdit = new EventoCreacionDTO();

        // 1. Agregar ID
        eventoEdit.setId_evento(evento.getId_evento()); // ⬅️ AGREGADO: Necesario para la actualización

        // 2. Agregar Propiedades de Evento
        eventoEdit.setNombre(evento.getNombre());
        eventoEdit.setDescripcion(evento.getDescripcion());
        eventoEdit.setUbicacion(evento.getUbicacion());
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
        EventoDTO evento = eventoService.obtenerEventoPorId(id);

        // 1. CÁLCULO CONSISTENTE: Calcular cupos disponibles
        int cuposOcupados = evento.getCuposOcupados() != null ? evento.getCuposOcupados() : 0;
        int cuposMaximos = evento.getCuposMaximosProveedor() != null ? evento.getCuposMaximosProveedor() : 0;

        // Aseguramos que el resultado no sea negativo
        int cuposDisponibles = Math.max(0, cuposMaximos - cuposOcupados);

        // 2. Usar el cálculo para la variable booleana
        boolean hayCupos = cuposDisponibles > 0; // Se basa en el CÁLCULO 1, eliminando la llamada al servicio redundante

        // Si necesitas que el DTO tenga el valor de cupos disponibles, puedes setearlo:
        evento.setCuposDisponibles(cuposDisponibles);

        boolean yaGuardoEvento = false;
        boolean yaSeInscribio = false;

        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // Lógica de Consumidor
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("CONSUMIDOR"))) {
                yaGuardoEvento = misEventosService.usuarioTieneEventoGuardado(usuario.getId_usuario(), id);
            }

            // Lógica de Proveedor
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
                yaSeInscribio = inscripcionProveedorService.proveedorEstaInscrito(usuario.getId_usuario(), id);
            }
        }

        // 3. AGREGAR AL MODEL: Ambos son necesarios para la vista view.html
        model.addAttribute("evento", evento);
        model.addAttribute("cuposDisponibles", cuposDisponibles); // ⬅️ ¡CRUCIAL! La vista lo necesita
        model.addAttribute("yaGuardoEvento", yaGuardoEvento);
        model.addAttribute("yaSeInscribio", yaSeInscribio);
        model.addAttribute("hayCupos", hayCupos);

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

        // ⚠️ CAMBIO: Usar el servicio de Inscripción
        var inscripciones = inscripcionProveedorService.obtenerEventosConfirmadosDeProveedor(usuario.getId_usuario());

        model.addAttribute("inscripciones", inscripciones); // Se debe adaptar la vista
        model.addAttribute("tipoLista", "mis_inscripciones");
        return "evento/mis-inscripciones"; // ⚠️ Vista nueva o adaptada
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

            // ✅ AGREGAR: Validar cupos disponibles antes de crear inscripción
            if (!inscripcionProveedorService.hayCuposDisponibles(idEvento)) {
                redirectAttributes.addFlashAttribute("error", "Lo sentimos, no quedan cupos disponibles.");
                return "redirect:/eventos/ver/" + idEvento;
            }

            var inscripcion = inscripcionProveedorService.solicitarCupo(proveedor.getId_usuario(), idEvento);

            redirectAttributes.addFlashAttribute("mensaje", "Cupo reservado. Completa el pago en los próximos 15 minutos.");
            return "redirect:/eventos/pago/simular/" + inscripcion.getId_inscripcion();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al solicitar cupo: " + e.getMessage());
            return "redirect:/eventos/ver/" + idEvento;
        }
    }

    @GetMapping("/pago/simular/{idInscripcion}")
    public String mostrarPaginaPago(@PathVariable Long idInscripcion,
                                    Model model,
                                    Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/login";
        }

        InscripcionProveedorDTO inscripcion = inscripcionProveedorService.obtenerInscripcionPorId(idInscripcion);

        model.addAttribute("inscripcion", inscripcion);
        return "redirect:/pagos-eventos/checkout/" + inscripcion.getId_inscripcion();
    }


    @PostMapping("/confirmar-pago-simulado/{idInscripcion}")
    public String confirmarPagoSimulado(@PathVariable Long idInscripcion,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/login";
        }

        try {
            // 1. Crear el pago
            PagoEventoCreacionDTO pagoCreacion = new PagoEventoCreacionDTO();
            pagoCreacion.setIdInscripcion(idInscripcion);
            pagoCreacion.setMetodoPago(MetodoPago.TARJETA_CREDITO); // O el que sea

            PagoEventoDTO pago = pagoEventoService.crearPago(pagoCreacion);

            // 2. Procesar el pago (simulación)
            pagoEventoService.procesarPago(pago.getIdPagoEvento());

            redirectAttributes.addFlashAttribute("mensaje", "¡Pago confirmado! Tu cupo está asegurado.");
            return "redirect:/eventos/mis-inscripciones";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar el pago: " + e.getMessage());
            return "redirect:/eventos";
        }
    }




    /**
     * Proveedor cancela su inscripción (si está PENDIENTE_PAGO o CONFIRMADO).
     */
    @PostMapping("/cancelar-inscripcion/{idInscripcion}")
    public String cancelarInscripcion(@PathVariable Long idInscripcion,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/login";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO proveedor = usuarioService.obtenerUsuarioPorEmail(email);

            // Validar que la inscripción pertenece al proveedor
            InscripcionProveedorDTO inscripcion = inscripcionProveedorService.obtenerInscripcionPorId(idInscripcion);

            if (!inscripcion.getId_proveedor().equals(proveedor.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error", "No puedes cancelar una inscripción que no es tuya.");
                return "redirect:/eventos/mis-inscripciones";
            }

            inscripcionProveedorService.cancelarInscripcion(idInscripcion, "Cancelado por el proveedor");
            redirectAttributes.addFlashAttribute("mensaje", "Inscripción cancelada correctamente.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar: " + e.getMessage());
        }

        return "redirect:/eventos/mis-inscripciones";
    }
    /**
     * Proveedor ve su código de confirmación para presentar en el evento.
     */
    @GetMapping("/mi-codigo/{idInscripcion}")
    public String verCodigoConfirmacion(@PathVariable Long idInscripcion,
                                        Model model,
                                        Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/login";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO proveedor = usuarioService.obtenerUsuarioPorEmail(email);

            InscripcionProveedorDTO inscripcion = inscripcionProveedorService.obtenerInscripcionPorId(idInscripcion);

            if (!inscripcion.getId_proveedor().equals(proveedor.getId_usuario())) {
                return "redirect:/eventos/mis-inscripciones";
            }

            model.addAttribute("inscripcion", inscripcion);
            return "evento/codigo-confirmacion"; // Vista con QR o código grande

        } catch (Exception e) {
            return "redirect:/eventos/mis-inscripciones";
        }
    }
}