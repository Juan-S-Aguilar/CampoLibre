package com.example.campolibre.Controller;

import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.DTO.TiendaDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Enum.CategoriaProducto;
import com.example.campolibre.Service.ProductoService;
import com.example.campolibre.Service.TiendaService;
import com.example.campolibre.Service.UsuarioService;
import com.example.campolibre.Service.FileStorageService;
import com.example.campolibre.Service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private TiendaService tiendaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CarritoService carritoService;

    // Ver todos los productos (público para todos)
    @GetMapping
    public String listarProductos(@RequestParam(required = false) CategoriaProducto categoria,
                                  Model model,
                                  Authentication authentication) {
        List<ProductoDTO> productos;

        if (categoria != null) {
            productos = productoService.obtenerProductosPorCategoria(categoria);
        } else {
            productos = productoService.obtenerProductosActivos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", CategoriaProducto.values());
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("tipoLista", "todos");

        // ✅ NUEVO: Agregar contador de items del carrito
        if (authentication != null) {
            try {
                String email = authentication.getName();
                UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
                Integer itemsCarrito = carritoService.contarItemsCarrito(usuario.getId_usuario());
                model.addAttribute("itemsCarrito", itemsCarrito);
            } catch (Exception e) {
                model.addAttribute("itemsCarrito", 0);
            }
        }

        return "producto/list";
    }

    // Ver productos de una tienda específica
    @GetMapping("/tienda/{idTienda}")
    public String listarProductosPorTienda(@PathVariable Long idTienda,
                                           Model model,
                                           Authentication authentication) {
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(idTienda);
        List<ProductoDTO> productos = productoService.obtenerProductosPorTienda(idTienda);

        model.addAttribute("tienda", tienda);
        model.addAttribute("productos", productos);
        model.addAttribute("tipoLista", "tienda");

        // ✅ NUEVO: Agregar contador de items del carrito
        if (authentication != null) {
            try {
                String email = authentication.getName();
                UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
                Integer itemsCarrito = carritoService.contarItemsCarrito(usuario.getId_usuario());
                model.addAttribute("itemsCarrito", itemsCarrito);
            } catch (Exception e) {
                model.addAttribute("itemsCarrito", 0);
            }
        }

        return "producto/list";
    }

    // Ver detalle de producto
    @GetMapping("/ver/{id}")
    public String verProducto(@PathVariable Long id,
                              Model model,
                              Authentication authentication) {
        ProductoDTO producto = productoService.obtenerProductoPorId(id);
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(producto.getId_tienda());

        model.addAttribute("producto", producto);
        model.addAttribute("tienda", tienda);

        // ✅ NUEVO: Agregar contador de items del carrito
        if (authentication != null) {
            try {
                String email = authentication.getName();
                UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
                Integer itemsCarrito = carritoService.contarItemsCarrito(usuario.getId_usuario());
                model.addAttribute("itemsCarrito", itemsCarrito);
            } catch (Exception e) {
                model.addAttribute("itemsCarrito", 0);
            }
        }

        return "producto/view";
    }

    // Crear producto (PROVEEDOR)
    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(@RequestParam Long idTienda,
                                            Model model,
                                            Authentication authentication,
                                            RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/productos";
        }

        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(idTienda);

        // Verificar que la tienda pertenece al proveedor
        if (!tienePuedeGestionarProductos(authentication, tienda)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para agregar productos a esta tienda.");
            return "redirect:/tiendas";
        }

        ProductoDTO producto = new ProductoDTO();
        producto.setId_tienda(idTienda);

        model.addAttribute("producto", producto);
        model.addAttribute("tienda", tienda);
        model.addAttribute("categorias", CategoriaProducto.values());
        return "producto/form";
    }

    @PostMapping("/crear")
    public String crearProducto(@ModelAttribute ProductoDTO productoDTO,
                                @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/productos";
        }

        try {
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(productoDTO.getId_tienda());

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para agregar productos a esta tienda.");
                return "redirect:/tiendas";
            }

            // Guardar imagen si se proporcionó
            if (imagen != null && !imagen.isEmpty()) {
                String rutaImagen = fileStorageService.guardarArchivo(imagen, "productos");
                productoDTO.setImagen_producto(rutaImagen);
            }

            productoService.crearProducto(productoDTO, null);
            redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente.");

        } catch (Exception e) {
            System.err.println("❌ Error al crear producto: " + e.getMessage());
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

        model.addAttribute("producto", producto);
        model.addAttribute("tienda", tienda);
        model.addAttribute("categorias", CategoriaProducto.values());
        return "producto/edit";
    }

    @PostMapping("/actualizar")
    public String actualizarProducto(@ModelAttribute("producto") ProductoDTO productoDTO,
                                     @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO productoExistente = productoService.obtenerProductoPorId(productoDTO.getId_producto());
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(productoExistente.getId_tienda());

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para modificar este producto.");
                return "redirect:/productos";
            }

            // Si hay nueva imagen
            if (imagen != null && !imagen.isEmpty()) {
                if (productoExistente.getImagen_producto() != null) {
                    fileStorageService.eliminarArchivo(productoExistente.getImagen_producto());
                }
                String rutaImagen = fileStorageService.guardarArchivo(imagen, "productos");
                productoDTO.setImagen_producto(rutaImagen);
            } else {
                productoDTO.setImagen_producto(productoExistente.getImagen_producto());
            }

            productoService.actualizarProducto(productoDTO.getId_producto(), productoDTO, null);
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado correctamente.");

        } catch (Exception e) {
            System.err.println("❌ Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar producto: " + e.getMessage());
        }

        return "redirect:/productos/tienda/" + productoDTO.getId_tienda();
    }

    // Eliminar producto (PROVEEDOR/ADMIN)
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO producto = productoService.obtenerProductoPorId(id);
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(producto.getId_tienda());
            Long idTienda = producto.getId_tienda();

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar este producto.");
                return "redirect:/productos";
            }

            if (producto.getImagen_producto() != null) {
                fileStorageService.eliminarArchivo(producto.getImagen_producto());
            }

            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente.");
            return "redirect:/productos/tienda/" + idTienda;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar producto: " + e.getMessage());
            return "redirect:/productos";
        }
    }

    // Ver productos eliminados (ADMIN)
    @GetMapping("/eliminados")
    public String listarProductosEliminados(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/productos";
        }

        List<ProductoDTO> productos = productoService.obtenerProductosEliminados();
        model.addAttribute("productos", productos);
        model.addAttribute("tipoLista", "eliminados");
        return "producto/list";
    }

    // Método auxiliar para verificar permisos
    private boolean tienePuedeGestionarProductos(Authentication authentication, TiendaDTO tienda) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return true;
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            return tienda.getId_usuario().equals(usuario.getId_usuario());
        }

        return false;
    }
}