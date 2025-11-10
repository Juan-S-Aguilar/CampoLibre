package com.example.campolibre.Controller;

import com.example.campolibre.DTO.PagoDTO;
import com.example.campolibre.DTO.PedidoDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Enum.EstadoPago;
import com.example.campolibre.Enum.MetodoPago;
import com.example.campolibre.Service.PagoService;
import com.example.campolibre.Service.PedidoService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Procesar pago de un pedido
     */
    @PostMapping("/procesar/{idPedido}")
    public String procesarPago(@PathVariable Long idPedido,
                               @RequestParam MetodoPago metodoPago,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // Verificar que el pedido sea del usuario
            PedidoDTO pedido = pedidoService.obtenerPedidoPorId(idPedido);
            if (!pedido.getId_usuario().equals(usuario.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes permiso para pagar este pedido");
                return "redirect:/pedidos/mis-pedidos";
            }

            System.out.println("🔄 Procesando pago para pedido: " + pedido.getNumero_pedido());

            // Procesar pago (simulado)
            PagoDTO pago = pagoService.procesarPago(idPedido, metodoPago);

            if (pago.getEstado() == EstadoPago.EXITOSO) {
                System.out.println("✅ Pago exitoso - Transacción: " + pago.getNumero_transaccion());
                redirectAttributes.addFlashAttribute("mensaje",
                        "¡Pago procesado exitosamente!");
                redirectAttributes.addFlashAttribute("pago", pago);
                return "redirect:/pagos/exitoso/" + idPedido;
            } else {
                System.out.println("❌ Pago fallido - " + pago.getMensaje_error());
                redirectAttributes.addFlashAttribute("error",
                        "Pago rechazado: " + pago.getMensaje_error());
                return "redirect:/pagos/fallido/" + idPedido;
            }

        } catch (Exception e) {
            System.err.println("❌ Error al procesar pago: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error al procesar pago: " + e.getMessage());
            return "redirect:/pedidos/mis-pedidos";
        }
    }

    /**
     * Página de pago exitoso
     */
    @GetMapping("/exitoso/{idPedido}")
    public String pagoExitoso(@PathVariable Long idPedido,
                              Model model,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            PedidoDTO pedido = pedidoService.obtenerPedidoPorId(idPedido);

            // Verificar que el pedido sea del usuario
            if (!pedido.getId_usuario().equals(usuario.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes permiso para ver este pedido");
                return "redirect:/pedidos/mis-pedidos";
            }

            PagoDTO pago = pagoService.obtenerPagoPorPedido(idPedido);

            model.addAttribute("pedido", pedido);
            model.addAttribute("pago", pago);
            model.addAttribute("usuario", usuario);

            // Agrupar items del pedido por tienda para la vista
            try {
                if (pedido.getItems() != null) {
                    java.util.Map<Long, java.util.List<com.example.campolibre.DTO.ItemPedidoDTO>> itemsPorTienda =
                            pedido.getItems().stream()
                                    .collect(java.util.stream.Collectors.groupingBy(
                                            com.example.campolibre.DTO.ItemPedidoDTO::getId_tienda,
                                            java.util.LinkedHashMap::new,
                                            java.util.stream.Collectors.toList()
                                    ));

                    model.addAttribute("itemsPorTienda", itemsPorTienda);
                }
            } catch (Exception ex) {
                // no bloquear la vista si falla la agrupación
                System.err.println("Error agrupando items por tienda: " + ex.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Error al mostrar pago exitoso: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al cargar información: " + e.getMessage());
            return "redirect:/pedidos/mis-pedidos";
        }

        return "pago/exitoso";
    }

    /**
     * Página de pago fallido
     */
    @GetMapping("/fallido/{idPedido}")
    public String pagoFallido(@PathVariable Long idPedido,
                              Model model,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            PedidoDTO pedido = pedidoService.obtenerPedidoPorId(idPedido);

            // Verificar que el pedido sea del usuario
            if (!pedido.getId_usuario().equals(usuario.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes permiso para ver este pedido");
                return "redirect:/pedidos/mis-pedidos";
            }

            try {
                PagoDTO pago = pagoService.obtenerPagoPorPedido(idPedido);
                model.addAttribute("pago", pago);
            } catch (Exception e) {
                // Si no hay pago registrado, continuar sin él
                System.out.println("No hay pago registrado para este pedido");
            }

            model.addAttribute("pedido", pedido);
            model.addAttribute("usuario", usuario);

        } catch (Exception e) {
            System.err.println("❌ Error al mostrar pago fallido: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al cargar información: " + e.getMessage());
            return "redirect:/pedidos/mis-pedidos";
        }

        return "pago/fallido";
    }

    /**
     * Ver detalles de un pago específico
     */
    @GetMapping("/ver/{idPedido}")
    public String verPago(@PathVariable Long idPedido,
                          Model model,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            PedidoDTO pedido = pedidoService.obtenerPedidoPorId(idPedido);

            // Verificar que el pedido sea del usuario
            if (!pedido.getId_usuario().equals(usuario.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes permiso para ver este pago");
                return "redirect:/pedidos/mis-pedidos";
            }

            PagoDTO pago = pagoService.obtenerPagoPorPedido(idPedido);

            model.addAttribute("pedido", pedido);
            model.addAttribute("pago", pago);
            model.addAttribute("usuario", usuario);

        } catch (Exception e) {
            System.err.println("❌ Error al ver pago: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al cargar información de pago: " + e.getMessage());
            return "redirect:/pedidos/mis-pedidos";
        }

        return "pago/view";
    }

    /**
     * Página de inicio de pago: muestra resumen del pedido y selección de método
     */
    @GetMapping("/checkout")
    public String iniciarCheckout(@RequestParam("pedidoId") Long pedidoId,
                                  Model model,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            PedidoDTO pedido = pedidoService.obtenerPedidoPorId(pedidoId);

            if (!pedido.getId_usuario().equals(usuario.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para pagar este pedido");
                return "redirect:/pedidos/mis-pedidos";
            }

            model.addAttribute("pedido", pedido);
            model.addAttribute("metodos", MetodoPago.values());
            model.addAttribute("usuario", usuario);
            return "pago/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al iniciar pago: " + e.getMessage());
            return "redirect:/pedidos/mis-pedidos";
        }
    }
}