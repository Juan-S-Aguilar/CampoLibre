package com.example.campolibre.Controller;

import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.DTO.TiendaDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Enum.SubcategoriaProducto;
import com.example.campolibre.Enum.UnidadMedida;
import com.example.campolibre.Service.ProductoService;
import com.example.campolibre.Service.TiendaService;
import com.example.campolibre.Service.UsuarioService;
import com.example.campolibre.Service.FileStorageService;
import com.example.campolibre.Service.CarritoService;
import com.example.campolibre.Util.MapeoCategoria;
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

    // ========== LISTAR PRODUCTOS ==========

    @GetMapping
    public String listarProductos(Model model) {
        List<ProductoDTO> productos = productoService.obtenerProductosActivos();
        model.addAttribute("productos", productos);
        return "producto/list";
    }

    @GetMapping("/tienda/{idTienda}")
    public String listarProductosPorTienda(@PathVariable Long idTienda, Model model) {
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(idTienda);
        List<ProductoDTO> productos = productoService.obtenerProductosPorTienda(idTienda);

        model.addAttribute("tienda", tienda);
        model.addAttribute("productos", productos);
        return "producto/list_by_tienda";
    }

    // ========== VER DETALLE DE PRODUCTO ==========

    @GetMapping("/ver/{id}")
    public String verProducto(@PathVariable Long id, Model model, Authentication authentication) {
        ProductoDTO producto = productoService.obtenerProductoPorId(id);
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(producto.getId_tienda());

        model.addAttribute("producto", producto);
        model.addAttribute("tienda", tienda);

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

    // ========== CREAR PRODUCTO (PROVEEDOR) ==========

    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(@RequestParam Long idTienda,
                                            Model model,
                                            Authentication authentication,
                                            RedirectAttributes redirectAttributes) {
        // ✅ DEBUG
        System.out.println("🔹 GET /productos/nuevo - idTienda: " + idTienda);

        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/productos";
        }

        try {
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(idTienda);

            // ✅ DEBUG
            System.out.println("🔹 Tienda encontrada: " + tienda.getNombre());
            System.out.println("🔹 Categoría de tienda: " + tienda.getCategoriaPrincipal());

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para agregar productos a esta tienda.");
                return "redirect:/tiendas";
            }

            // ✅ CREAR PRODUCTO CON VALORES POR DEFECTO ROBUSTOS
            ProductoDTO producto = new ProductoDTO();
            producto.setId_tienda(idTienda);
            producto.setNombre("");
            producto.setDescripcion("");
            producto.setPrecio(100.0);        // ✅ Precio mínimo válido
            producto.setStock(0);             // ✅ 0 por defecto (usuario debe llenar)
            producto.setStockMinimo(5);       // ✅ 5 por defecto (recomendado)
            producto.setEstado("ACTIVO");

            // ✅ DEBUG
            System.out.println("🔹 Producto DTO creado con id_tienda: " + producto.getId_tienda());
            System.out.println("🔹 Valores por defecto: precio=" + producto.getPrecio() + ", stock=" + producto.getStock() + ", stockMinimo=" + producto.getStockMinimo());

            model.addAttribute("producto", producto);
            model.addAttribute("tienda", tienda);

            // ✅ CRÍTICO: Subcategorías permitidas
            List<SubcategoriaProducto> subcategoriasPermitidas =
                    MapeoCategoria.obtenerSubcategoriasOrdenadas(tienda.getCategoriaPrincipal());

            // ✅ DEBUG
            System.out.println("🔹 Subcategorías disponibles: " + subcategoriasPermitidas.size());
            subcategoriasPermitidas.forEach(sub -> System.out.println("   - " + sub.getDisplayName()));

            model.addAttribute("subcategorias", subcategoriasPermitidas);
            model.addAttribute("unidadesMedida", UnidadMedida.values());

            // ✅ DEBUG - Verificar modelo
            System.out.println("🔹 Modelo preparado - Redirigiendo a form.html");
            System.out.println("   producto.id_tienda = " + producto.getId_tienda());
            System.out.println("   subcategorias.size = " + subcategoriasPermitidas.size());
            System.out.println("   unidadesMedida.length = " + UnidadMedida.values().length);

            return "producto/form";

        } catch (Exception e) {
            // ✅ DEBUG
            System.err.println("❌ Error en GET /productos/nuevo: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al cargar formulario: " + e.getMessage());
            return "redirect:/tiendas";
        }
    }

    @PostMapping("/crear")
    public String crearProducto(@ModelAttribute ProductoDTO productoDTO,
                                @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        // ✅ DEBUG COMPLETO
        System.out.println("🔹 POST /productos/crear");
        System.out.println("   Nombre: " + productoDTO.getNombre());
        System.out.println("   Descripción length: " + (productoDTO.getDescripcion() != null ? productoDTO.getDescripcion().length() : "null"));
        System.out.println("   Subcategoría: " + productoDTO.getSubcategoria());
        System.out.println("   UnidadMedida: " + productoDTO.getUnidadMedida());
        System.out.println("   Precio: " + productoDTO.getPrecio());
        System.out.println("   Stock: " + productoDTO.getStock());
        System.out.println("   StockMinimo: " + productoDTO.getStockMinimo());
        System.out.println("   ID Tienda: " + productoDTO.getId_tienda());
        System.out.println("   Imagen presente: " + (imagen != null && !imagen.isEmpty()));

        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/productos";
        }

        try {
            // ✅ VALIDACIÓN CRÍTICA
            if (productoDTO.getId_tienda() == null) {
                System.err.println("❌ ERROR CRÍTICO: id_tienda es NULL");
                redirectAttributes.addFlashAttribute("error", "Error: ID de tienda no válido");
                return "redirect:/tiendas";
            }

            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(productoDTO.getId_tienda());
            System.out.println("🔹 Tienda verificada: " + tienda.getNombre());

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para agregar productos a esta tienda.");
                return "redirect:/tiendas";
            }

            // ✅ GUARDAR IMAGEN
            if (imagen != null && !imagen.isEmpty()) {
                System.out.println("🔹 Guardando imagen: " + imagen.getOriginalFilename());
                String rutaImagen = fileStorageService.guardarArchivo(imagen, "productos");
                productoDTO.setImagen_producto(rutaImagen);
                System.out.println("🔹 Imagen guardada en: " + rutaImagen);
            } else {
                System.out.println("⚠️ No se proporcionó imagen");
            }

            // ✅ CREAR PRODUCTO
            System.out.println("🔹 Llamando a productoService.crearProducto()");
            ProductoDTO productoCreado = productoService.crearProducto(productoDTO, null);
            System.out.println("✅ Producto creado exitosamente con ID: " + productoCreado.getId_producto());

            redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente.");
            return "redirect:/productos/tienda/" + productoDTO.getId_tienda();

        } catch (Exception e) {
            System.err.println("❌ Error al crear producto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al crear producto: " + e.getMessage());

            // REDIRIGIR AL FORMULARIO CON EL PARÁMETRO
            return "redirect:/productos/nuevo?idTienda=" + productoDTO.getId_tienda();
        }
    }

    // ========== EDITAR PRODUCTO (PROVEEDOR/ADMIN) ==========

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id,
                                           Model model,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO producto = productoService.obtenerProductoPorId(id);
            TiendaDTO tienda = tiendaService.obtenerTiendaPorId(producto.getId_tienda());

            if (!tienePuedeGestionarProductos(authentication, tienda)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para editar este producto.");
                return "redirect:/productos";
            }

            model.addAttribute("producto", producto);
            model.addAttribute("tienda", tienda);

            List<SubcategoriaProducto> subcategoriasPermitidas =
                    MapeoCategoria.obtenerSubcategoriasOrdenadas(tienda.getCategoriaPrincipal());
            model.addAttribute("subcategorias", subcategoriasPermitidas);
            model.addAttribute("unidadesMedida", UnidadMedida.values());

            return "producto/edit";

        } catch (Exception e) {
            System.err.println("❌ Error al cargar producto para editar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al cargar producto: " + e.getMessage());
            return "redirect:/productos";
        }
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

            if (imagen != null && !imagen.isEmpty()) {
                String rutaImagen = fileStorageService.guardarArchivo(imagen, "productos");
                productoDTO.setImagen_producto(rutaImagen);
            } else {
                productoDTO.setImagen_producto(productoExistente.getImagen_producto());
            }

            productoService.actualizarProducto(productoDTO.getId_producto(), productoDTO, null);
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente.");

        } catch (Exception e) {
            System.err.println("❌ Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar producto: " + e.getMessage());
        }

        return "redirect:/productos/tienda/" + productoDTO.getId_tienda();
    }

    // ========== ELIMINAR PRODUCTO (PROVEEDOR/ADMIN) ==========

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
            redirectAttributes.addFlashAttribute("error", "Error al eliminar producto: " + e.getMessage());
            return "redirect:/productos";
        }
    }

    // ========== MÉTODO AUXILIAR ==========

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