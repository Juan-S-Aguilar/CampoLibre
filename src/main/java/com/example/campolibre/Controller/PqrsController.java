package com.example.campolibre.Controller;

import com.example.campolibre.DTO.*;
import com.example.campolibre.Entity.Pqrs;
import com.example.campolibre.Enum.EstadoPqrs;
import com.example.campolibre.Enum.RolProceso;
import com.example.campolibre.Enum.TipoPqrs;
import com.example.campolibre.Repository.PqrsEventoRepository;
import com.example.campolibre.Repository.PqrsRepository;
import com.example.campolibre.Repository.PqrsTiendaRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Service.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lowagie.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import java.util.List;

@Controller
@RequestMapping("/pqrs")
public class PqrsController {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private PqrsService pqrsService;

    @Autowired
    private PqrsRepository pqrsRepository;

    @Autowired
    private PqrsTiendaService pqrsTiendaService;

    @Autowired
    private PqrsEventoService pqrsEventoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TiendaService tiendaService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    public PqrsController(PqrsRepository pqrsRepository) {
        this.pqrsRepository = pqrsRepository;

    }

    // Ver PQRS (ADMIN ve solo sin asociación, otros ven las suyas y las de sus tiendas/eventos)
    @GetMapping
    public String listarPqrs(
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) TipoPqrs tipo,
            @RequestParam(required = false) EstadoPqrs estado,
            Model model,
            Authentication authentication) {

        List<PqrsDTO> pqrsList;
        boolean esAdmin = false;
        Long idUsuario = null;

        if (authentication != null && authentication.isAuthenticated()) {
            esAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"));
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            if (usuario != null) {
                idUsuario = usuario.getId_usuario();
            }
        }

        // ✅ Aplicar filtros si existen
        if (fechaDesde != null || fechaHasta != null || tipo != null || estado != null) {
            LocalDateTime fechaInicio = (fechaDesde != null && !fechaDesde.isEmpty())
                    ? LocalDate.parse(fechaDesde).atStartOfDay()
                    : null;
            LocalDateTime fechaFin = (fechaHasta != null && !fechaHasta.isEmpty())
                    ? LocalDate.parse(fechaHasta).atTime(23, 59, 59)
                    : null;

            List<Pqrs> pqrsEntities = pqrsService.buscarPqrsConFiltros(fechaInicio, fechaFin, tipo, estado);

            // Filtrar por visibilidad del usuario
            if (!esAdmin && idUsuario != null) {
                final Long userId = idUsuario;
                pqrsEntities = pqrsEntities.stream()
                        .filter(p -> p.getEmisor().getId_usuario().equals(userId) ||
                                // Agregar lógica de tiendas/eventos propios
                                false)
                        .collect(Collectors.toList());
            }

            pqrsList = pqrsEntities.stream()
                    .map(pqrs -> pqrsService.obtenerPqrsPorId(pqrs.getId_pqrs()))
                    .collect(Collectors.toList());
        } else {
            // Sin filtros: comportamiento actual
            pqrsList = pqrsService.obtenerPqrsVisibles(idUsuario, esAdmin);
        }

        // Marcar permisos de respuesta (Lógica Corregida)
        for (PqrsDTO dto : pqrsList) {
            boolean tienePermisoEstatico = pqrsService.puedeResponder(dto.getId_pqrs(), idUsuario, esAdmin);

            // Solo puede responder si tiene el permiso Y es el turno del proveedor
            boolean puedeResponderAhora = tienePermisoEstatico &&
                    dto.getPendienteDe() == RolProceso.PROVEEDOR;

            dto.setPuede_responder(puedeResponderAhora);
        }

        model.addAttribute("pqrsList", pqrsList);
        model.addAttribute("idUsuarioActual", idUsuario);
        model.addAttribute("tipoLista", esAdmin ? "todas" : "mis_pqrs");

        return "pqrs/list";
    }

/*    // ✅ Método helper para convertir entidad a DTO
    private PqrsDTO convertirAPqrsDTO(Pqrs pqrs) {
        PqrsDTO dto = new PqrsDTO();
        dto.setId_pqrs(pqrs.getId_pqrs());
        dto.setTipo(pqrs.getTipo());
        dto.setDescripcion(pqrs.getDescripcion());
        dto.setFecha_envio(pqrs.getFecha_envio());
        dto.setEstado(pqrs.getEstado());
        dto.setFecha_respuesta(pqrs.getFecha_respuesta());
        dto.setRespuesta(pqrs.getRespuesta());

        if (pqrs.getEmisor() != null) {
            dto.setId_emisor(pqrs.getEmisor().getId_usuario());
        }
        if (pqrs.getReceptor() != null) {
            dto.setId_receptor(pqrs.getReceptor().getId_usuario());
        }

        // Asociaciones (tienda/evento)
        // ... (copiar lógica de mapToDto en PqrsImplement)

        return dto;
    }*/

    // Ver PQRS pendientes (ADMIN)
    @GetMapping("/pendientes")
    public String listarPqrsPendientes(Model model, Authentication authentication) {
        // Solo admin puede acceder
        if (authentication == null || !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/pqrs";
        }

        // Obtener ID del admin
        Long idAdmin = null;
        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
        if (usuario != null) {
            idAdmin = usuario.getId_usuario();
        }

        // Obtener solo PQRS pendientes sin asociación
        List<PqrsDTO> pqrsList = pqrsService.obtenerPqrsPendientesAdmin();

        // Marcar que todas son respondibles por el admin
        for (PqrsDTO dto : pqrsList) {
            dto.setPuede_responder(true);
        }

        model.addAttribute("pqrsList", pqrsList);
        model.addAttribute("tipoLista", "pendientes");
        return "pqrs/pendientes";
    }

    @GetMapping("/ver/{id}")
    public String verPqrs(@PathVariable Long id, Model model, Authentication authentication) {
        PqrsDTO pqrs = pqrsService.obtenerPqrsPorId(id);

        // Verificar si está asociada a tienda o evento
        PqrsTiendaDTO pqrsTienda = pqrsTiendaService.obtenerPorPqrsId(id);
        PqrsEventoDTO pqrsEvento = pqrsEventoService.obtenerPorPqrsId(id);

        // Obtener usuario actual (idUsuarioActual, esAdmin)
        Long idUsuarioActual = null;
        boolean esAdmin = false;
        if (authentication != null && authentication.isAuthenticated()) {
            esAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"));
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            if (usuario != null) idUsuarioActual = usuario.getId_usuario();
        }

        // Regla de acceso (permitido)
        boolean permitido = false;
        if (pqrs.getId_emisor() != null && idUsuarioActual != null && pqrs.getId_emisor().equals(idUsuarioActual)) {
            permitido = true; // emisor
        }

        if (!permitido) {
            if (pqrsTienda != null && pqrsTienda.getId_tienda() != null) {
                TiendaDTO tienda = tiendaService.obtenerTiendaPorId(pqrsTienda.getId_tienda());
                if (tienda != null && tienda.getId_usuario() != null && tienda.getId_usuario().equals(idUsuarioActual)) {
                    permitido = true; // dueño de la tienda
                }
            }
        }

        if (!permitido) {
            if (pqrsEvento != null && pqrsEvento.getId_evento() != null) {
                EventoDTO evento = eventoService.obtenerEventoPorId(pqrsEvento.getId_evento());
                if (evento != null && evento.getCreado_por() != null && evento.getCreado_por().equals(idUsuarioActual)) {
                    permitido = true; // creador del evento
                }
            }
        }

        if (!permitido) {
            // Si no está asociada a tienda ni evento -> sólo admin
            if (pqrsTienda == null && pqrsEvento == null && esAdmin) {
                permitido = true;
            }
        }

        if (!permitido) {
            return "redirect:/pqrs"; // denegar acceso
        }

        // ---------------------------------------------------------------------------------
        // 💡 LÓGICA DE UI PARA TRAZABILIDAD (FINAL Y CORREGIDA)
        // ---------------------------------------------------------------------------------

        // 1. Lógica del Botón "Contáctenos"
        boolean mostrarBotonContacto = pqrs.getEstado() == EstadoPqrs.CERRADA_DEFINITIVA;

        // Lógica de Permisos Estáticos (para simplificar las banderas)
        boolean tienePermisoEstaticoProveedor = pqrsService.puedeResponder(id, idUsuarioActual, esAdmin);
        boolean esEmisorOriginal = idUsuarioActual != null && idUsuarioActual.equals(pqrs.getId_emisor());


        // Bandera para el botón "Responder" (Proveedor/Admin)
        // Solo si tiene el permiso estático Y le toca al PROVEEDOR.
        boolean puedeResponderAhora = tienePermisoEstaticoProveedor &&
                pqrs.getPendienteDe() == RolProceso.PROVEEDOR;

        pqrs.setPuede_responder(puedeResponderAhora);


        // Bandera para los botones "Replicar" y "Cerrar por Aceptación" (Consumidor)
        // Solo si es el Emisor Y le toca al CONSUMIDOR (independiente del estado exacto).
        boolean puedeConsumidorActuar = esEmisorOriginal &&
                pqrs.getPendienteDe() == RolProceso.CONSUMIDOR;

        boolean puedeReplicar = puedeConsumidorActuar;
        boolean puedeCerrar = puedeConsumidorActuar; // Mismo turno

        // Marcar los booleanos para la vista
        model.addAttribute("pqrs", pqrs);
        model.addAttribute("pqrsTienda", pqrsTienda);
        model.addAttribute("pqrsEvento", pqrsEvento);

        model.addAttribute("mostrarBotonContacto", mostrarBotonContacto);
        model.addAttribute("puedeReplicar", puedeReplicar);
        model.addAttribute("puedeCerrar", puedeCerrar);

        return "pqrs/view";
    }

    // Crear PQRS
    @GetMapping("/crear")
    public String mostrarFormularioCreacion(@RequestParam(required = false) Long idTienda,
                                            @RequestParam(required = false) Long idEvento,
                                            Model model) {
        model.addAttribute("pqrs", new PqrsDTO());
        model.addAttribute("tiposPqrs", TipoPqrs.values());
        model.addAttribute("tiendas", tiendaService.obtenerTiendasActivas());
        model.addAttribute("eventos", eventoService.obtenerEventosAprobados());
        model.addAttribute("idTiendaSeleccionada", idTienda);
        model.addAttribute("idEventoSeleccionado", idEvento);
        return "pqrs/form";
    }
    @PostMapping("/crear")
    public String crearPqrs(@ModelAttribute PqrsDTO pqrsDTO,
                            @RequestParam(required = false) Long idTienda,
                            @RequestParam(required = false) Long idEvento,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // 1. Asignar el ID del emisor.
            pqrsDTO.setId_emisor(usuario.getId_usuario());

            // 💡 CORRECCIÓN CRÍTICA: Asignar los IDs de asociación al DTO
            // Esto permite que PqrsImplement determine el receptor (Proveedor/Admin)
            // y cree la asociación (PqrsTienda/PqrsEvento).
            if (idTienda != null) {
                pqrsDTO.setId_tienda(idTienda);
                // Asegurarse de que idEvento sea null si se selecciona Tienda, para evitar ambigüedad.
                pqrsDTO.setId_evento(null);
            } else if (idEvento != null) {
                pqrsDTO.setId_evento(idEvento);
                // Asegurarse de que idTienda sea null si se selecciona Evento.
                pqrsDTO.setId_tienda(null);
            } else {
                // Si ambos son null, la PQRS va al Admin (que ya es el valor por defecto en el DTO).
                pqrsDTO.setId_tienda(null);
                pqrsDTO.setId_evento(null);
            }


            // 2. Llamar al servicio.
            // El servicio centraliza la lógica de: Receptor, pendienteDe, Pqrs.save(), y PqrsTienda/Evento.save().
            PqrsDTO nuevaPqrs = pqrsService.crearPqrs(pqrsDTO);

            redirectAttributes.addFlashAttribute("mensaje", "PQRS creada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear PQRS: " + e.getMessage());
        }
        return "redirect:/pqrs";
    }


    // Responder PQRS (ADMIN o propietario/creador asociado)
    @GetMapping("/responder/{id}")
    public String mostrarFormularioRespuesta(@PathVariable Long id,
                                             Model model,
                                             Authentication authentication) {
        boolean esAdmin = false;
        Long idUsuario = null;
        if (authentication != null && authentication.isAuthenticated()) {
            esAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"));
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            if (usuario != null) idUsuario = usuario.getId_usuario();
        }

        // comprobar permisos mediante el servicio
        if (!pqrsService.puedeResponder(id, idUsuario, esAdmin)) {
            return "redirect:/pqrs";
        }

        PqrsDTO pqrs = pqrsService.obtenerPqrsPorId(id);
        model.addAttribute("pqrs", pqrs);
        return "pqrs/responder";
    }

   /* @PostMapping("/responder/{id}")
    public String responderPqrs(@PathVariable Long id,
                                @RequestParam String respuesta,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        boolean esAdmin = false;
        Long idUsuario = null;
        if (authentication != null && authentication.isAuthenticated()) {
            esAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"));
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            if (usuario != null) idUsuario = usuario.getId_usuario();
        }

        // permiso para responder
        if (!pqrsService.puedeResponder(id, idUsuario, esAdmin)) {
            redirectAttributes.addFlashAttribute("error", "No autorizado para responder esta PQRS.");
            return "redirect:/pqrs";
        }

        try {
            pqrsService.responderPqrs(id, respuesta, idUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "PQRS respondida exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al responder PQRS: " + e.getMessage());
        }
        return "redirect:/pqrs/pendientes";
    }*/

    /**
     * Genera reporte de PQRS en formato Excel
     */
    @GetMapping("/reporte/excel")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'PROVEEDOR')")
    public ResponseEntity<InputStreamResource> descargarReporteExcel(
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) TipoPqrs tipo,
            @RequestParam(required = false) EstadoPqrs estado,
            @RequestParam(required = false) Long idTienda,
            @RequestParam(required = false) Long idEvento,
            Authentication authentication) throws IOException {

        // Convertir fechas String a LocalDateTime
        LocalDateTime fechaInicio = null;
        LocalDateTime fechaFin = null;

        if (fechaDesde != null && !fechaDesde.isEmpty()) {
            fechaInicio = LocalDate.parse(fechaDesde).atStartOfDay();
        }
        if (fechaHasta != null && !fechaHasta.isEmpty()) {
            fechaFin = LocalDate.parse(fechaHasta).atTime(23, 59, 59);
        }

        // Obtener usuario actual
        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
        boolean esAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"));

        // Buscar PQRS con filtros
        List<Pqrs> pqrsList;

        if (idTienda != null) {
            // Filtrar por tienda específica
            pqrsList = pqrsRepository.findByTiendaParaReporte(idTienda);
        } else if (idEvento != null) {
            // Filtrar por evento específico
            pqrsList = pqrsRepository.findByEventoParaReporte(idEvento);
        } else {
            // Filtros generales
            pqrsList = pqrsService.buscarPqrsConFiltros(fechaInicio, fechaFin, tipo, estado);

            // Si no es admin, filtrar solo sus PQRS visibles
            if (!esAdmin && usuario != null) {
                Long idUsuario = usuario.getId_usuario();
                pqrsList = pqrsList.stream()
                        .filter(p ->
                                p.getEmisor().getId_usuario().equals(idUsuario) ||
                                        // Aquí podrías agregar lógica adicional para verificar si es dueño de tienda/evento
                                        false
                        )
                        .collect(Collectors.toList());
            }
        }

        // Generar Excel
        ByteArrayInputStream bais = excelService.generarReportePqrs(pqrsList);

        // Configurar respuesta HTTP
        HttpHeaders headers = new HttpHeaders();
        String nombreArchivo = "reporte_pqrs_" + LocalDate.now().toString() + ".xlsx";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bais));
    }

    /**
     * Genera reporte de PQRS en formato PDF
     */
    @GetMapping("/reporte/pdf")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'PROVEEDOR')")
    public ResponseEntity<InputStreamResource> descargarReportePdf(
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) TipoPqrs tipo,
            @RequestParam(required = false) EstadoPqrs estado,
            @RequestParam(required = false) Long idTienda,
            @RequestParam(required = false) Long idEvento,
            Authentication authentication) throws DocumentException, IOException {

        // Convertir fechas String a LocalDateTime
        LocalDateTime fechaInicio = null;
        LocalDateTime fechaFin = null;

        if (fechaDesde != null && !fechaDesde.isEmpty()) {
            fechaInicio = LocalDate.parse(fechaDesde).atStartOfDay();
        }
        if (fechaHasta != null && !fechaHasta.isEmpty()) {
            fechaFin = LocalDate.parse(fechaHasta).atTime(23, 59, 59);
        }

        // Obtener usuario actual
        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
        boolean esAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"));

        // Buscar PQRS con filtros (misma lógica que Excel)
        List<Pqrs> pqrsList;

        if (idTienda != null) {
            pqrsList = pqrsRepository.findByTiendaParaReporte(idTienda);
        } else if (idEvento != null) {
            pqrsList = pqrsRepository.findByEventoParaReporte(idEvento);
        } else {
            pqrsList = pqrsService.buscarPqrsConFiltros(fechaInicio, fechaFin, tipo, estado);

            if (!esAdmin && usuario != null) {
                Long idUsuario = usuario.getId_usuario();
                pqrsList = pqrsList.stream()
                        .filter(p -> p.getEmisor().getId_usuario().equals(idUsuario))
                        .collect(Collectors.toList());
            }
        }

        // Generar PDF
        ByteArrayInputStream bais = pdfService.generarReportePqrs(pqrsList);

        // Configurar respuesta HTTP
        HttpHeaders headers = new HttpHeaders();
        String nombreArchivo = "reporte_pqrs_" + LocalDate.now().toString() + ".pdf";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bais));
    }

    //___________________________________________________________________________________________________________________________



    @PostMapping("/registrar-respuesta/{id}")
    public String registrarRespuesta(@PathVariable Long id,
                                     @RequestParam String contenido,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        boolean esAdmin = false;
        Long idUsuario = null;
        if (authentication != null && authentication.isAuthenticated()) {
            esAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"));
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            if (usuario != null) idUsuario = usuario.getId_usuario();
        }

        // permiso para responder (La lógica se mantiene igual)
        if (!pqrsService.puedeResponder(id, idUsuario, esAdmin)) {
            redirectAttributes.addFlashAttribute("error", "No autorizado para registrar respuesta en esta PQRS.");
            return "redirect:/pqrs";
        }

        try {
            pqrsService.registrarRespuesta(id, contenido, idUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Respuesta registrada exitosamente. El consumidor ha sido notificado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar respuesta: " + e.getMessage());
        }

        // 💡 AJUSTE DE REDIRECCIÓN: Redirigir según el rol
        if (esAdmin) {
            // El Administrador debe volver a su lista de pendientes para continuar el trabajo
            return "redirect:/pqrs/pendientes";
        } else {
            // Los dueños de tienda/evento vuelven al detalle para ver la trazabilidad
            return "redirect:/pqrs/ver/" + id;
        }
    }

    // --- ✅ NUEVO Endpoint POST /registrar-replica/{id} (Réplica del Consumidor) ---
    @PostMapping("/registrar-replica/{id}")
    public String registrarReplica(@PathVariable Long id,
                                   @RequestParam String contenido,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {

        Long idUsuario = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            if (usuario != null) idUsuario = usuario.getId_usuario();
        }

        try {
            // El servicio valida si es el emisor y si el estado es RESPONDIDA
            pqrsService.registrarReplica(id, contenido, idUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Su réplica ha sido enviada. El proveedor debe responder.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar la réplica: " + e.getMessage());
        }

        return "redirect:/pqrs/ver/" + id;
    }

    // --- ✅ NUEVO Endpoint POST /cerrar-pqrs/{id} (Aceptación del Consumidor) ---
    @PostMapping("/cerrar-pqrs/{id}")
    public String cerrarPqrsPorConsumidor(@PathVariable Long id,
                                          Authentication authentication,
                                          RedirectAttributes redirectAttributes) {

        Long idUsuario = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            if (usuario != null) idUsuario = usuario.getId_usuario();
        }

        try {
            // El servicio valida si es el emisor y si el estado es RESPONDIDA
            pqrsService.cerrarPqrsPorConsumidor(id, idUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "PQRS cerrada exitosamente por aceptación.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cerrar la PQRS: " + e.getMessage());
        }

        return "redirect:/pqrs/ver/" + id;
    }

}
