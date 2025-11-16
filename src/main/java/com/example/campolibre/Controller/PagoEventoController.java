package com.example.campolibre.Controller;

import com.example.campolibre.DTO.InscripcionProveedorDTO;
import com.example.campolibre.DTO.PagoEventoCreacionDTO;
import com.example.campolibre.DTO.PagoEventoDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Enum.MetodoPago;
import com.example.campolibre.Service.InscripcionProveedorService;
import com.example.campolibre.Service.PagoEventoService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pagos-eventos")
public class PagoEventoController {

    @Autowired
    private PagoEventoService pagoEventoService;

    @Autowired
    private InscripcionProveedorService inscripcionProveedorService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Página de checkout para pagar un cupo de evento.
     * Muestra resumen de la inscripción y métodos de pago.
     */
    @GetMapping("/checkout/{idInscripcion}")
    @PreAuthorize("hasAuthority('PROVEEDOR')")
    public String mostrarCheckout(@PathVariable Long idInscripcion,
                                  Model model,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO proveedor = usuarioService.obtenerUsuarioPorEmail(email);

            // Obtener la inscripción
            InscripcionProveedorDTO inscripcion = inscripcionProveedorService.obtenerInscripcionPorId(idInscripcion);

            // Validar que la inscripción pertenezca al proveedor
            if (!inscripcion.getId_proveedor().equals(proveedor.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes permiso para pagar esta inscripción.");
                return "redirect:/eventos/mis-inscripciones";
            }

            // Validar que esté en estado PENDIENTE_PAGO
            if (inscripcion.getEstadoCupo() != com.example.campolibre.Enum.EstadoCupo.PENDIENTE_PAGO) {
                redirectAttributes.addFlashAttribute("error",
                        "Esta inscripción no está pendiente de pago. Estado actual: " + inscripcion.getEstadoCupo());
                return "redirect:/eventos/mis-inscripciones";
            }

            model.addAttribute("inscripcion", inscripcion);
            model.addAttribute("metodosPago", MetodoPago.values());
            model.addAttribute("proveedor", proveedor);

            return "pago-evento/checkout";

        } catch (Exception e) {
            System.err.println("❌ Error al mostrar checkout: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al cargar información: " + e.getMessage());
            return "redirect:/eventos/mis-inscripciones";
        }
    }

    /**
     * Procesar el pago de un cupo de evento.
     * Crea el pago y lo procesa (simulación o API real).
     */
    @PostMapping("/procesar/{idInscripcion}")
    @PreAuthorize("hasAuthority('PROVEEDOR')")
    public String procesarPago(@PathVariable Long idInscripcion,
                               @RequestParam MetodoPago metodoPago,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO proveedor = usuarioService.obtenerUsuarioPorEmail(email);

            // Obtener y validar inscripción
            InscripcionProveedorDTO inscripcion = inscripcionProveedorService.obtenerInscripcionPorId(idInscripcion);

            if (!inscripcion.getId_proveedor().equals(proveedor.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes permiso para pagar esta inscripción.");
                return "redirect:/eventos/mis-inscripciones";
            }

            System.out.println("🔄 Procesando pago de cupo - Inscripción: " + idInscripcion
                    + " | Método: " + metodoPago);

            // 1. Crear el pago
            PagoEventoCreacionDTO pagoCreacion = new PagoEventoCreacionDTO();
            pagoCreacion.setIdInscripcion(idInscripcion);
            pagoCreacion.setMetodoPago(metodoPago);

            PagoEventoDTO pago = pagoEventoService.crearPago(pagoCreacion);

            // 2. Procesar el pago (aquí se simula o se llama a API real)
            PagoEventoDTO pagoFinal = pagoEventoService.procesarPago(pago.getIdPagoEvento());

            // 3. Redirigir según resultado
            if (pagoFinal.getEstado() == com.example.campolibre.Enum.EstadoPago.EXITOSO) {
                System.out.println("✅ Pago exitoso - Transacción: " + pagoFinal.getNumeroTransaccion());
                redirectAttributes.addFlashAttribute("mensaje",
                        "¡Pago procesado exitosamente! Tu cupo está confirmado.");
                return "redirect:/pagos-eventos/exitoso/" + pago.getIdPagoEvento();
            } else {
                System.out.println("❌ Pago fallido - " + pagoFinal.getMensajeError());
                redirectAttributes.addFlashAttribute("error",
                        "Pago rechazado: " + pagoFinal.getMensajeError());
                return "redirect:/pagos-eventos/fallido/" + pago.getIdPagoEvento();
            }

        } catch (Exception e) {
            System.err.println("❌ Error al procesar pago: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error al procesar pago: " + e.getMessage());
            return "redirect:/eventos/mis-inscripciones";
        }
    }

    /**
     * Página de pago exitoso.
     * Muestra código de confirmación y detalles del evento.
     */
    @GetMapping("/exitoso/{idPagoEvento}")
    @PreAuthorize("hasAuthority('PROVEEDOR')")
    public String pagoExitoso(@PathVariable Long idPagoEvento,
                              Model model,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO proveedor = usuarioService.obtenerUsuarioPorEmail(email);

            // Obtener el pago
            PagoEventoDTO pago = pagoEventoService.obtenerPagoPorId(idPagoEvento);

            // Obtener la inscripción asociada
            InscripcionProveedorDTO inscripcion = inscripcionProveedorService
                    .obtenerInscripcionPorId(pago.getIdInscripcion());

            // Validar que sea del proveedor
            if (!inscripcion.getId_proveedor().equals(proveedor.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes permiso para ver esta información.");
                return "redirect:/eventos/mis-inscripciones";
            }

            model.addAttribute("pago", pago);
            model.addAttribute("inscripcion", inscripcion);
            model.addAttribute("proveedor", proveedor);

            return "pago-evento/exitoso";

        } catch (Exception e) {
            System.err.println("❌ Error al mostrar pago exitoso: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al cargar información: " + e.getMessage());
            return "redirect:/eventos/mis-inscripciones";
        }
    }

    /**
     * Página de pago fallido.
     * Permite reintentar el pago.
     */
    @GetMapping("/fallido/{idPagoEvento}")
    @PreAuthorize("hasAuthority('PROVEEDOR')")
    public String pagoFallido(@PathVariable Long idPagoEvento,
                              Model model,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO proveedor = usuarioService.obtenerUsuarioPorEmail(email);

            // Obtener el pago
            PagoEventoDTO pago = pagoEventoService.obtenerPagoPorId(idPagoEvento);

            // Obtener la inscripción asociada
            InscripcionProveedorDTO inscripcion = inscripcionProveedorService
                    .obtenerInscripcionPorId(pago.getIdInscripcion());

            // Validar que sea del proveedor
            if (!inscripcion.getId_proveedor().equals(proveedor.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes permiso para ver esta información.");
                return "redirect:/eventos/mis-inscripciones";
            }

            model.addAttribute("pago", pago);
            model.addAttribute("inscripcion", inscripcion);
            model.addAttribute("proveedor", proveedor);

            return "pago-evento/fallido";

        } catch (Exception e) {
            System.err.println("❌ Error al mostrar pago fallido: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al cargar información: " + e.getMessage());
            return "redirect:/eventos/mis-inscripciones";
        }
    }

    /**
     * Ver detalles de un pago específico.
     * (Histórico de pagos)
     */
    @GetMapping("/ver/{idPagoEvento}")
    @PreAuthorize("hasAuthority('PROVEEDOR')")
    public String verDetallePago(@PathVariable Long idPagoEvento,
                                 Model model,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO proveedor = usuarioService.obtenerUsuarioPorEmail(email);

            PagoEventoDTO pago = pagoEventoService.obtenerPagoPorId(idPagoEvento);
            InscripcionProveedorDTO inscripcion = inscripcionProveedorService
                    .obtenerInscripcionPorId(pago.getIdInscripcion());

            if (!inscripcion.getId_proveedor().equals(proveedor.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes permiso para ver este pago.");
                return "redirect:/eventos/mis-inscripciones";
            }

            model.addAttribute("pago", pago);
            model.addAttribute("inscripcion", inscripcion);
            model.addAttribute("proveedor", proveedor);

            return "pago-evento/view";

        } catch (Exception e) {
            System.err.println("❌ Error al ver detalle de pago: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al cargar información: " + e.getMessage());
            return "redirect:/eventos/mis-inscripciones";
        }
    }

    /**
     * Historial de pagos del proveedor.
     */
    @GetMapping("/mis-pagos")
    @PreAuthorize("hasAuthority('PROVEEDOR')")
    public String misPagos(Model model, Authentication authentication) {
        try {
            String email = authentication.getName();
            UsuarioDTO proveedor = usuarioService.obtenerUsuarioPorEmail(email);

            var pagos = pagoEventoService.obtenerPagosPorProveedor(proveedor.getId_usuario());

            model.addAttribute("pagos", pagos);
            model.addAttribute("proveedor", proveedor);

            return "pago-evento/mis-pagos";

        } catch (Exception e) {
            System.err.println("❌ Error al listar pagos: " + e.getMessage());
            model.addAttribute("error", "Error al cargar tus pagos: " + e.getMessage());
            return "pago-evento/mis-pagos";
        }
    }

    // ========================================
    // WEBHOOK (Para integración con pasarela real)
    // ========================================

    /**
     * Endpoint para recibir notificaciones de la pasarela de pagos.
     * Este endpoint NO requiere autenticación (viene de servidor externo).
     *
     * IMPORTANTE: En producción, validar la firma/token de la pasarela.
     */
    @PostMapping("/webhook")
    public String recibirWebhook(@RequestParam String numeroTransaccion,
                                 @RequestParam String estado,
                                 @RequestParam(required = false) String mensajeError) {
        try {
            System.out.println("📥 Webhook recibido - Transacción: " + numeroTransaccion
                    + " | Estado: " + estado);

            // Buscar el pago por número de transacción
            PagoEventoDTO pago = pagoEventoService.obtenerPagoPorNumeroTransaccion(numeroTransaccion);

            // Actualizar estado según respuesta de la pasarela
            if ("exitoso".equalsIgnoreCase(estado) || "approved".equalsIgnoreCase(estado)) {
                pagoEventoService.marcarPagoExitoso(pago.getIdPagoEvento());
                System.out.println("✅ Pago confirmado vía webhook");
            } else if ("fallido".equalsIgnoreCase(estado) || "rejected".equalsIgnoreCase(estado)) {
                pagoEventoService.marcarPagoFallido(pago.getIdPagoEvento(),
                        mensajeError != null ? mensajeError : "Pago rechazado por la pasarela");
                System.out.println("❌ Pago rechazado vía webhook");
            }

            return "OK"; // Responder a la pasarela

        } catch (Exception e) {
            System.err.println("❌ Error procesando webhook: " + e.getMessage());
            return "ERROR";
        }
    }


}