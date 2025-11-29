package com.example.campolibre.Controller;

import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.DTO.ResumenInventarioDTO;
import com.example.campolibre.DTO.TiendaDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Service.ProductoService;
import com.example.campolibre.Service.TiendaService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private TiendaService tiendaService;

    @Autowired
    private UsuarioService usuarioService;

    // ========== VER PANEL DE INVENTARIO (TODAS LAS TIENDAS) ==========

    @GetMapping("/panel")
    public String verPanelInventarioGeneral(@RequestParam(required = false) String filtro,
                                           Model model,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // Obtener todas las tiendas del proveedor
            List<TiendaDTO> misTiendas = tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario());

            if (misTiendas.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No tienes tiendas registradas.");
                return "redirect:/tiendas";
            }

            // Obtener productos de todas las tiendas
            List<ProductoDTO> todosProductos = misTiendas.stream()
                    .flatMap(tienda -> productoService.obtenerProductosPorTienda(tienda.getId_tienda()).stream())
                    .collect(Collectors.toList());

            // Calcular resumen agregado
            ResumenInventarioDTO resumen = calcularResumenAgregado(todosProductos);

            // Aplicar filtros
            List<ProductoDTO> productos;
            String tituloFiltro = "Todos los productos";

            if ("sin_stock".equals(filtro)) {
                productos = todosProductos.stream()
                        .filter(p -> p.getSinStock() != null && p.getSinStock())
                        .collect(Collectors.toList());
                tituloFiltro = "Productos sin stock";
            } else if ("stock_bajo".equals(filtro)) {
                productos = todosProductos.stream()
                        .filter(p -> p.getTieneStockBajo() != null && p.getTieneStockBajo())
                        .collect(Collectors.toList());
                tituloFiltro = "Productos con stock bajo";
            } else if ("activos".equals(filtro)) {
                productos = todosProductos.stream()
                        .filter(p -> "ACTIVO".equals(p.getEstado()))
                        .collect(Collectors.toList());
                tituloFiltro = "Productos activos";
            } else {
                productos = todosProductos;
                tituloFiltro = "Todos los productos";
            }

            // Crear un mapa de nombres de tiendas para usar en la vista
            Map<Long, String> tiendaNombres = misTiendas.stream()
                    .collect(Collectors.toMap(TiendaDTO::getId_tienda, TiendaDTO::getNombre));

            model.addAttribute("tienda", null); // No hay tienda específica
            model.addAttribute("resumen", resumen);
            model.addAttribute("productos", productos);
            model.addAttribute("filtroActual", filtro);
            model.addAttribute("tituloFiltro", tituloFiltro);
            model.addAttribute("misTiendas", misTiendas);
            model.addAttribute("tiendaNombres", tiendaNombres);
            model.addAttribute("viendoTodasTiendas", true);

            return "inventario/panel";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar inventario: " + e.getMessage());
            return "redirect:/tiendas";
        }
    }

    // ========== VER PANEL DE INVENTARIO (TIENDA ESPECÍFICA) ==========

    @GetMapping("/tienda/{idTienda}")
    public String verInventario(@PathVariable Long idTienda,
                                @RequestParam(required = false) String filtro,
                                Model model,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        // Verificar permisos
        if (!puedeGestionarInventario(authentication, idTienda)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para acceder a este inventario.");
            return "redirect:/tiendas";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            List<TiendaDTO> misTiendas = tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario());

            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(idTienda);
            ResumenInventarioDTO resumen = productoService.obtenerResumenInventario(idTienda);

            List<ProductoDTO> productos;
            String tituloFiltro = "Todos los productos";

            if ("sin_stock".equals(filtro)) {
                productos = productoService.obtenerProductosSinStock(idTienda);
                tituloFiltro = "Productos sin stock";
            } else if ("stock_bajo".equals(filtro)) {
                productos = productoService.obtenerProductosConStockBajo(idTienda);
                tituloFiltro = "Productos con stock bajo";
            } else if ("activos".equals(filtro)) {
                productos = productoService.obtenerProductosPorTienda(idTienda)
                        .stream()
                        .filter(p -> "ACTIVO".equals(p.getEstado()))
                        .collect(Collectors.toList());
                tituloFiltro = "Productos activos";
            } else {
                productos = productoService.obtenerProductosPorTienda(idTienda);
                tituloFiltro = "Todos los productos";
            }

            model.addAttribute("tienda", tienda);
            model.addAttribute("resumen", resumen);
            model.addAttribute("productos", productos);
            model.addAttribute("filtroActual", filtro);
            model.addAttribute("tituloFiltro", tituloFiltro);
            model.addAttribute("misTiendas", misTiendas);
            model.addAttribute("viendoTodasTiendas", false);

            return "inventario/panel";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar inventario: " + e.getMessage());
            return "redirect:/tiendas";
        }
    }

    // ========== MÉTODO AUXILIAR - CALCULAR RESUMEN AGREGADO ==========

    private ResumenInventarioDTO calcularResumenAgregado(List<ProductoDTO> productos) {
        ResumenInventarioDTO resumen = new ResumenInventarioDTO();

        resumen.setTotalProductos(productos.size());
        resumen.setProductosActivos((int) productos.stream()
                .filter(p -> "ACTIVO".equals(p.getEstado()))
                .count());
        resumen.setProductosConStockBajo((int) productos.stream()
                .filter(p -> p.getTieneStockBajo() != null && p.getTieneStockBajo())
                .count());
        resumen.setProductosSinStock((int) productos.stream()
                .filter(p -> p.getSinStock() != null && p.getSinStock())
                .count());
        resumen.setValorTotalInventario(productos.stream()
                .mapToDouble(p -> p.getPrecio() * p.getStock())
                .sum());

        return resumen;
    }

    // ========== INCREMENTAR STOCK (AJAX) ==========

    @PostMapping("/incrementar/{idProducto}")
    @ResponseBody
    public ResponseEntity<?> incrementarStock(@PathVariable Long idProducto,
                                              @RequestParam Integer cantidad,
                                              Authentication authentication) {
        try {
            ProductoDTO productoActual = productoService.obtenerProductoPorId(idProducto);

            // Verificar permisos
            if (!puedeGestionarInventario(authentication, productoActual.getId_tienda())) {
                return ResponseEntity.status(403).body(
                        Map.of("success", false, "message", "No tienes permisos para modificar este producto")
                );
            }

            ProductoDTO actualizado = productoService.incrementarStock(idProducto, cantidad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stock incrementado exitosamente");
            response.put("nuevoStock", actualizado.getStock());
            response.put("estado", actualizado.getEstado());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ========== REDUCIR STOCK (AJAX) ==========

    @PostMapping("/reducir/{idProducto}")
    @ResponseBody
    public ResponseEntity<?> reducirStock(@PathVariable Long idProducto,
                                          @RequestParam Integer cantidad,
                                          Authentication authentication) {
        try {
            ProductoDTO productoActual = productoService.obtenerProductoPorId(idProducto);

            // Verificar permisos
            if (!puedeGestionarInventario(authentication, productoActual.getId_tienda())) {
                return ResponseEntity.status(403).body(
                        Map.of("success", false, "message", "No tienes permisos para modificar este producto")
                );
            }

            ProductoDTO actualizado = productoService.reducirStock(idProducto, cantidad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stock reducido exitosamente");
            response.put("nuevoStock", actualizado.getStock());
            response.put("estado", actualizado.getEstado());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ========== ACTUALIZAR STOCK DIRECTO (AJAX) ==========

    @PostMapping("/actualizar/{idProducto}")
    @ResponseBody
    public ResponseEntity<?> actualizarStock(@PathVariable Long idProducto,
                                             @RequestParam Integer nuevoStock,
                                             Authentication authentication) {
        try {
            ProductoDTO productoActual = productoService.obtenerProductoPorId(idProducto);

            // Verificar permisos
            if (!puedeGestionarInventario(authentication, productoActual.getId_tienda())) {
                return ResponseEntity.status(403).body(
                        Map.of("success", false, "message", "No tienes permisos para modificar este producto")
                );
            }

            ProductoDTO actualizado = productoService.actualizarStock(idProducto, nuevoStock);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stock actualizado exitosamente");
            response.put("nuevoStock", actualizado.getStock());
            response.put("estado", actualizado.getEstado());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ========== REACTIVAR PRODUCTO ==========

    @PostMapping("/reactivar/{idProducto}")
    public String reactivarProducto(@PathVariable Long idProducto,
                                    @RequestParam Integer nuevoStock,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO productoActual = productoService.obtenerProductoPorId(idProducto);

            // Verificar permisos
            if (!puedeGestionarInventario(authentication, productoActual.getId_tienda())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para reactivar este producto.");
                return "redirect:/tiendas";
            }

            ProductoDTO actualizado = productoService.reactivarProducto(idProducto, nuevoStock);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Producto reactivado exitosamente con " + nuevoStock + " unidades");
            return "redirect:/inventario/tienda/" + actualizado.getId_tienda();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al reactivar producto: " + e.getMessage());
            return "redirect:/productos";
        }
    }

    // ========== MÉTODO AUXILIAR - VERIFICAR PERMISOS ==========

    private boolean puedeGestionarInventario(Authentication authentication, Long idTienda) {
        if (authentication == null) {
            return false;
        }

        // Administradores tienen acceso total
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return true;
        }

        // Proveedores solo pueden gestionar sus propias tiendas
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            try {
                String email = authentication.getName();
                UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
                TiendaDTO tienda = tiendaService.obtenerTiendaPorId(idTienda);
                return tienda.getId_usuario().equals(usuario.getId_usuario());
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }
}