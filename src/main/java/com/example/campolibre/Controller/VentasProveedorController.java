package com.example.campolibre.Controller;

import com.example.campolibre.DTO.PedidoDTO;
import com.example.campolibre.DTO.TiendaDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Service.PedidoService;
import com.example.campolibre.Service.TiendaService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pedidos/ventas")
public class VentasProveedorController {

    @Autowired
    private TiendaService tiendaService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Muestra el panel de ventas del proveedor (resumen de sus tiendas y pedidos pagados)
     */
    @GetMapping
    public String panelVentas(Authentication authentication,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Obtener usuario y verificar rol
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/";
        }

        // Obtener tiendas del proveedor
        List<TiendaDTO> misTiendas = tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario());
        model.addAttribute("misTiendas", misTiendas);

        // Si hay al menos una tienda, mostrar pedidos pagados globales (de todas sus tiendas)
        if (misTiendas != null && !misTiendas.isEmpty()) {
            // Usar el primer id como ejemplo para obtener el listado de pedidos pagados (puedes adaptar filtros later)
            List<Long> tiendaIds = misTiendas.stream().map(TiendaDTO::getId_tienda).collect(Collectors.toList());

            // Obtener los pedidos pagados que contienen productos de las tiendas del proveedor
            // Se reutiliza el método existente para una tienda; para múltiples tiendas, llamamos al servicio por tienda y combinamos.
            List<PedidoDTO> pedidos = pedidoService.obtenerPedidosPagadosPorTienda(tiendaIds.get(0));

            // Si solo tenemos un método por tienda, combinar resultados de cada tienda (evitar duplicados)
            if (tiendaIds.size() > 1) {
                for (int i = 1; i < tiendaIds.size(); i++) {
                    List<PedidoDTO> adicionales = pedidoService.obtenerPedidosPagadosPorTienda(tiendaIds.get(i));
                    for (PedidoDTO p : adicionales) {
                        if (!pedidos.contains(p)) pedidos.add(p);
                    }
                }
            }

            model.addAttribute("pedidos", pedidos);

            // Calcular ganancias totales sumando por tienda (servicio ya tiene calcularGananciasTienda)
            Double gananciasTotales = 0.0;
            int totalVentas = 0;
            for (TiendaDTO t : misTiendas) {
                Double g = pedidoService.calcularGananciasTienda(t.getId_tienda());
                if (g != null) gananciasTotales += g;

                List<PedidoDTO> pT = pedidoService.obtenerPedidosPagadosPorTienda(t.getId_tienda());
                if (pT != null) totalVentas += pT.size();
            }

            model.addAttribute("gananciasTotales", gananciasTotales);
            model.addAttribute("totalVentas", totalVentas);
        }

        return "pedido/ventas";
    }

    /**
     * Muestra las ventas de una tienda en particular del proveedor
     */
    @GetMapping("/tienda/{id}")
    public String ventasPorTienda(@PathVariable("id") Long id,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/pedidos/ventas";
        }

        // Validar que la tienda pertenece al proveedor
        List<TiendaDTO> misTiendas = tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario());
        boolean pertenece = misTiendas.stream().anyMatch(t -> t.getId_tienda().equals(id));
        if (!pertenece) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver estas ventas");
            return "redirect:/pedidos/ventas";
        }

        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(id);
        List<PedidoDTO> pedidos = pedidoService.obtenerPedidosPagadosPorTienda(id);

        model.addAttribute("tienda", tienda);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("gananciasTotales", pedidoService.calcularGananciasTienda(id));

        return "pedido/ventas"; // reusar la misma vista que muestra detalle si hay tienda en el modelo
    }
}

