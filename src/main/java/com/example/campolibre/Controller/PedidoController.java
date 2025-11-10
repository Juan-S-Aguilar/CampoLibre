package com.example.campolibre.Controller;

import com.example.campolibre.DTO.ConfirmarPedidoRequest;
import com.example.campolibre.DTO.ItemCarritoDTO;
import com.example.campolibre.DTO.PedidoDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Service.CarritoService;
import com.example.campolibre.Service.PedidoService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Muestra el resumen del pedido con los productos del carrito del usuario
     */
    @GetMapping("/resumen")
    public String mostrarResumenPedido(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());

        // Obtener los productos del carrito
        List<ItemCarritoDTO> items = carritoService.obtenerItemsCarrito(usuario.getId_usuario());

        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }

        double total = carritoService.calcularTotal(usuario.getId_usuario());

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("datosEntrega", new ConfirmarPedidoRequest()); // para el formulario
        return "pedido/resumen";
    }

    /**
     * Confirma el pedido con los datos de entrega y redirige al pago
     */
    @PostMapping("/confirmar")
    public String confirmarPedido(@ModelAttribute("datosEntrega") ConfirmarPedidoRequest datosEntrega,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());

        try {
            PedidoDTO pedido = pedidoService.crearPedidoDesdeCarrito(usuario.getId_usuario(), datosEntrega);
            redirectAttributes.addFlashAttribute("pedidoId", pedido.getId_pedido());
            // Redirigir al endpoint de pago (controller /pagos)
            return "redirect:/pagos/checkout?pedidoId=" + pedido.getId_pedido();
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/carrito";
        }
    }

    /**
     * Lista los pedidos del usuario
     */
    @GetMapping("/mis-pedidos")
    public String misPedidos(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());

        try {
            List<PedidoDTO> pedidos = pedidoService.obtenerPedidosPorUsuario(usuario.getId_usuario());
            model.addAttribute("pedidos", pedidos);
            return "pedido/mis_pedidos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * Redirige a la pantalla de pago para un pedido específico
     */
    @GetMapping("/pagar/{id}")
    public String pagarPedido(@PathVariable("id") Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());

        try {
            PedidoDTO pedido = pedidoService.obtenerPedidoPorId(id);
            if (!pedido.getId_usuario().equals(usuario.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para pagar este pedido");
                return "redirect:/pedidos/mis-pedidos";
            }
            return "redirect:/pagos/checkout?pedidoId=" + id;
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/mis-pedidos";
        }
    }

    /**
     * Cancela un pedido (si no está pagado)
     */
    @GetMapping("/cancelar/{id}")
    public String cancelarPedido(@PathVariable("id") Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());

        try {
            PedidoDTO pedido = pedidoService.obtenerPedidoPorId(id);
            if (!pedido.getId_usuario().equals(usuario.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para cancelar este pedido");
                return "redirect:/pedidos/mis-pedidos";
            }
            pedidoService.cancelarPedido(id);
            redirectAttributes.addFlashAttribute("mensaje", "Pedido cancelado correctamente");
            return "redirect:/pedidos/mis-pedidos";
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/mis-pedidos";
        }
    }

    /**
     * Permite ver el detalle de un pedido (historial o confirmación)
     */
    @GetMapping("/ver/{idPedido}")
    public String verDetallePedido(@PathVariable Long idPedido,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());

        try {
            PedidoDTO pedido = pedidoService.obtenerPedidoPorId(idPedido);
            // Verificar que el pedido pertenece al usuario (consumidor) que solicita verlo
            if (!pedido.getId_usuario().equals(usuario.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver este pedido");
                return "redirect:/pedidos/mis-pedidos";
            }

            model.addAttribute("pedido", pedido);
            return "pedido/view";
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/mis-pedidos";
        }
    }
}
