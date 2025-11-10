package com.example.campolibre.Controller;

import com.example.campolibre.DTO.CarritoCompraDTO;
import com.example.campolibre.DTO.ItemCarritoDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Service.CarritoService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Ver carrito de compras
     */
    @GetMapping
    public String verCarrito(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());

        try {
            CarritoCompraDTO carrito = carritoService.obtenerCarritoPorUsuario(usuario.getId_usuario());
            model.addAttribute("carrito", carrito);
            model.addAttribute("usuario", usuario);
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el carrito: " + e.getMessage());
        }

        return "carrito/view";
    }

    /**
     * Agregar producto al carrito
     */
    @PostMapping("/agregar")
    public String agregarProducto(@RequestParam Long idProducto,
                                  @RequestParam(defaultValue = "1") Integer cantidad,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            ItemCarritoDTO item = carritoService.agregarProducto(
                    usuario.getId_usuario(),
                    idProducto,
                    cantidad
            );

            redirectAttributes.addFlashAttribute("mensaje",
                    "Producto agregado al carrito exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error al agregar producto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error al agregar producto: " + e.getMessage());
        }

        // Redirigir a la página anterior o al carrito
        String referer = "/carrito";
        return "redirect:" + referer;
    }

    /**
     * Actualizar cantidad de un item
     */
    @PostMapping("/actualizar/{idItem}")
    public String actualizarCantidad(@PathVariable Long idItem,
                                     @RequestParam Integer cantidad,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            carritoService.actualizarCantidad(usuario.getId_usuario(), idItem, cantidad);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Cantidad actualizada correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error al actualizar cantidad: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al actualizar cantidad: " + e.getMessage());
        }

        return "redirect:/carrito";
    }

    /**
     * Eliminar item del carrito
     */
    @GetMapping("/eliminar/{idItem}")
    public String eliminarItem(@PathVariable Long idItem,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            carritoService.eliminarItem(usuario.getId_usuario(), idItem);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Producto eliminado del carrito");

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar item: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al eliminar producto: " + e.getMessage());
        }

        return "redirect:/carrito";
    }

    /**
     * Vaciar carrito completo
     */
    @GetMapping("/vaciar")
    public String vaciarCarrito(Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            carritoService.vaciarCarrito(usuario.getId_usuario());
            redirectAttributes.addFlashAttribute("mensaje",
                    "Carrito vaciado exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error al vaciar carrito: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al vaciar carrito: " + e.getMessage());
        }

        return "redirect:/carrito";
    }

    /**
     * Contador de items para mostrar en el navbar (AJAX)
     */
    @GetMapping("/contador")
    @ResponseBody
    public Integer contadorItems(Authentication authentication) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            return carritoService.contarItemsCarrito(usuario.getId_usuario());
        } catch (Exception e) {
            return 0;
        }
    }
}