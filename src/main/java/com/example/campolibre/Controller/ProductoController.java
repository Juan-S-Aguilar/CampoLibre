package com.example.campolibre.Controller;

import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.DTO.TiendaDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Enum.CategoriaProducto;
import com.example.campolibre.Enum.SubcategoriaProducto;
import com.example.campolibre.Service.ProductoService;
import com.example.campolibre.Service.TiendaService;
import com.example.campolibre.Service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final TiendaService tiendaService;
    private final UsuarioService usuarioService;

    public ProductoController(ProductoService productoService,
                              TiendaService tiendaService,
                              UsuarioService usuarioService) {
        this.productoService = productoService;
        this.tiendaService = tiendaService;
        this.usuarioService = usuarioService;
    }

    // Listar todos los productos activos (Para CONSUMIDORES)
    @GetMapping
    public String listarProductos(Model model) {
        List<ProductoDTO> productos = productoService.obtenerProductosActivos();
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", CategoriaProducto.values());
        return "producto/list";
    }

    // Ver detalle de un producto
    @GetMapping("/detalle/{id}")
    public String verDetalleProducto(@PathVariable Long id, Model model) {
        ProductoDTO producto = productoService.obtenerProductoPorId(id);
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(producto.getId_tienda());

        model.addAttribute("producto", producto);
        model.addAttribute("tienda", tienda);
        return "producto/view";
    }

    // Ver productos de una tienda específica
    @GetMapping("/tienda/{idTienda}")
    public String verProductosPorTienda(@PathVariable Long idTienda, Model model) {
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(idTienda);
        List<ProductoDTO> productos = productoService.obtenerProductosPorTienda(idTienda);

        model.addAttribute("tienda", tienda);
        model.addAttribute("productos", productos);
        return "producto/list-by-store";
    }

    // Filtrar por categoría
    @GetMapping("/categoria/{categoria}")
    public String filtrarPorCategoria(@PathVariable CategoriaProducto categoria, Model model) {
        List<ProductoDTO> productos = productoService.obtenerProductosPorCategoria(categoria);
        model.addAttribute("productos", productos);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("categorias", CategoriaProducto.values());
        return "producto/list";
    }

    // Buscar productos
    @GetMapping("/buscar")
    public String buscarProductos(@RequestParam(required = false) String nombre, Model model) {
        List<ProductoDTO> productos = productoService.buscarProductosPorNombre(nombre);
        model.addAttribute("productos", productos);
        model.addAttribute("terminoBusqueda", nombre);
        return "producto/list";
    }

    // Formulario para crear nuevo producto (PROVEEDOR/ADMIN)
    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(@RequestParam Long idTienda,
                                            Model model,
                                            Authentication authentication,
                                            RedirectAttributes redirectAttributes) {
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(idTienda);

        if (!tienePuedeGestionarProductos(authentication, tienda)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para agregar productos a esta tienda.");
            return "redirect:/tiendas";
        }

        ProductoDTO nuevoProducto = new ProductoDTO();
        nuevoProducto.setId_tienda(idTienda);

        model.addAttribute("producto", nuevoProducto);
        model.addAttribute("tienda", tienda);
        model.addAttribute("categorias", CategoriaProducto.values());
        model.addAttribute("unidadesMedida", Arrays.asList(
                com.example.campolibre.Enum.UnidadMedida.values()
        ));
        return "producto/create";
    }

    // Crear producto
    @PostMapping("/guardar")
    public String crearProducto(@ModelAttribute("producto") ProductoDTO productoDTO,
                                BindingResult result,
                                @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(productoDTO.getId_tienda());

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para crear productos en esta tienda.");
                return "redirect:/tiendas";
            }

            if (result.hasErrors()) {
                model.addAttribute("tienda", tienda);
                model.addAttribute("categorias", CategoriaProducto.values());
                model.addAttribute("unidadesMedida", Arrays.asList(
                        com.example.campolibre.Enum.UnidadMedida.values()
                ));
                return "producto/create";
            }

            // Validar que la subcategoría corresponde a la categoría
            if (!productoDTO.esSubcategoriaValida()) {
                model.addAttribute("error", "La subcategoría seleccionada no corresponde a la categoría");
                model.addAttribute("tienda", tienda);
                model.addAttribute("categorias", CategoriaProducto.values());
                model.addAttribute("unidadesMedida", Arrays.asList(
                        com.example.campolibre.Enum.UnidadMedida.values()
                ));
                return "producto/create";
            }

            productoService.crearProducto(productoDTO, imagen);
            redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente.");

        } catch (Exception e) {
            System.err.println("✗ Error al crear producto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al crear producto: " + e.getMessage());
            return "redirect:/productos/nuevo?idTienda=" + productoDTO.getId_tienda();
        }

        return "redirect:/productos/tienda/" + productoDTO.getId_tienda();
    }

    // Editar producto (PROVEEDOR/ADMIN)
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id,
                                           Model model,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        ProductoDTO producto = productoService.obtenerProductoPorId(id);
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(producto.getId_tienda());

        if (!tienePuedeGestionarProductos(authentication, tienda)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para editar este producto.");
            return "redirect:/productos";
        }

        // Obtener subcategorías de la categoría actual del producto
        List<SubcategoriaProducto> subcategorias =
                SubcategoriaProducto.getSubcategoriasPorCategoria(producto.getCategoria());

        model.addAttribute("producto", producto);
        model.addAttribute("tienda", tienda);
        model.addAttribute("categorias", CategoriaProducto.values());
        model.addAttribute("subcategorias", subcategorias);
        model.addAttribute("unidadesMedida", Arrays.asList(
                com.example.campolibre.Enum.UnidadMedida.values()
        ));
        return "producto/edit";
    }

    @PostMapping("/actualizar")
    public String actualizarProducto(@ModelAttribute("producto") ProductoDTO productoDTO,
                                     BindingResult result,
                                     @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        try {
            ProductoDTO productoExistente = productoService.obtenerProductoPorId(productoDTO.getId_producto());
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(productoExistente.getId_tienda());

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para modificar este producto.");
                return "redirect:/productos";
            }

            if (result.hasErrors()) {
                model.addAttribute("tienda", tienda);
                model.addAttribute("categorias", CategoriaProducto.values());
                model.addAttribute("unidadesMedida", Arrays.asList(
                        com.example.campolibre.Enum.UnidadMedida.values()
                ));
                return "producto/edit";
            }

            // Validar subcategoría
            if (!productoDTO.esSubcategoriaValida()) {
                model.addAttribute("error", "La subcategoría seleccionada no corresponde a la categoría");
                model.addAttribute("tienda", tienda);
                model.addAttribute("categorias", CategoriaProducto.values());
                model.addAttribute("unidadesMedida", Arrays.asList(
                        com.example.campolibre.Enum.UnidadMedida.values()
                ));
                return "producto/edit";
            }

            productoService.actualizarProducto(productoDTO.getId_producto(), productoDTO, imagen);
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente.");

        } catch (Exception e) {
            System.err.println("✗ Error al actualizar producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/productos/editar/" + productoDTO.getId_producto();
        }

        return "redirect:/productos/detalle/" + productoDTO.getId_producto();
    }

    // Eliminar producto (PROVEEDOR/ADMIN)
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO producto = productoService.obtenerProductoPorId(id);
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(producto.getId_tienda());

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar este producto.");
                return "redirect:/productos";
            }

            Long idTienda = producto.getId_tienda();
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente.");
            return "redirect:/productos/tienda/" + idTienda;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
            return "redirect:/productos";
        }
    }

    // ==================== GESTIÓN DE STOCK ====================

    /**
     * Aumentar stock manualmente
     */
    @PostMapping("/aumentar-stock/{id}")
    public String aumentarStock(@PathVariable Long id,
                                @RequestParam Integer cantidad,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO producto = productoService.obtenerProductoPorId(id);
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(producto.getId_tienda());

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para modificar el stock.");
                return "redirect:/productos/detalle/" + id;
            }

            if (cantidad == null || cantidad <= 0) {
                redirectAttributes.addFlashAttribute("error", "La cantidad debe ser mayor a cero.");
                return "redirect:/productos/detalle/" + id;
            }

            productoService.aumentarStockManual(id, cantidad);
            redirectAttributes.addFlashAttribute("mensaje",
                    String.format("Stock aumentado en %d unidades exitosamente.", cantidad));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al aumentar stock: " + e.getMessage());
        }

        return "redirect:/productos/detalle/" + id;
    }

    /**
     * Disminuir stock manualmente
     */
    @PostMapping("/disminuir-stock/{id}")
    public String disminuirStock(@PathVariable Long id,
                                 @RequestParam Integer cantidad,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO producto = productoService.obtenerProductoPorId(id);
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(producto.getId_tienda());

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para modificar el stock.");
                return "redirect:/productos/detalle/" + id;
            }

            if (cantidad == null || cantidad <= 0) {
                redirectAttributes.addFlashAttribute("error", "La cantidad debe ser mayor a cero.");
                return "redirect:/productos/detalle/" + id;
            }

            productoService.disminuirStockManual(id, cantidad);
            redirectAttributes.addFlashAttribute("mensaje",
                    String.format("Stock disminuido en %d unidades exitosamente.", cantidad));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al disminuir stock: " + e.getMessage());
        }

        return "redirect:/productos/detalle/" + id;
    }

    /**
     * Ver productos con stock bajo
     */
    @GetMapping("/stock-bajo")
    public String verProductosConStockBajo(@RequestParam(defaultValue = "10") Integer umbral,
                                           Authentication authentication,
                                           Model model,
                                           RedirectAttributes redirectAttributes) {
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());

        if (!"PROVEEDOR".equals(usuario.getRolSeleccionado().name()) && !"ADMINISTRADOR".equals(usuario.getRolSeleccionado().name())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para acceder a esta sección.");
            return "redirect:/productos";
        }

        List<ProductoDTO> productos = productoService.obtenerProductosConStockBajo(umbral);

        // Si es PROVEEDOR, filtrar solo sus productos
        if ("PROVEEDOR".equals(usuario.getRolSeleccionado().name())) {
            productos = productos.stream()
                    .filter(p -> {
                        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(p.getId_tienda());
                        return tienda.getId_usuario().equals(usuario.getId_usuario());
                    })
                    .toList();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("umbral", umbral);
        return "producto/low-stock";
    }

    /**
     * API endpoint para obtener subcategorías por categoría (AJAX)
     */
    @GetMapping("/subcategorias/{categoria}")
    @ResponseBody
    public List<SubcategoriaProducto> obtenerSubcategorias(@PathVariable CategoriaProducto categoria) {
        return SubcategoriaProducto.getSubcategoriasPorCategoria(categoria);
    }

    // Método auxiliar para verificar permisos
    private boolean tienePuedeGestionarProductos(Authentication authentication, TiendaDTO tienda) {
        if (authentication == null) return false;

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(authentication.getName());

        // ADMIN puede gestionar todo
        if ("ADMINISTRADOR".equals(usuario.getRolSeleccionado().name())) {
            return true;
        }

        // PROVEEDOR solo puede gestionar sus propias tiendas
        if ("PROVEEDOR".equals(usuario.getRolSeleccionado().name())) {
            return usuario.getId_usuario().equals(tienda.getId_usuario());
        }

        return false;
    }
}