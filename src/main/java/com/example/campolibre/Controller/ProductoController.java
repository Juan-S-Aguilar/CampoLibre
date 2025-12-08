package com.example.campolibre.Controller;

import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.DTO.TiendaDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Entity.Tienda;
import com.example.campolibre.Enum.CategoriaTienda;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

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
    public String listarProductos(
            @RequestParam(value = "idTienda", required = false) Long idTienda,
            Model model) {

        List<ProductoDTO> productos = (idTienda != null)
                ? productoService.obtenerProductosPorTienda(idTienda)
                : productoService.obtenerProductosActivos();

        // Crear mapa idTienda -> nombreTienda (solo para ids presentes)
        Set<Long> tiendaIds = productos.stream()
                .map(ProductoDTO::getId_tienda)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> tiendaNombres = new HashMap<>();
        for (Long tid : tiendaIds) {
            var tiendaDto = tiendaService.obtenerTiendaPorId(tid);
            if (tiendaDto != null) {
                tiendaNombres.put(tid, tiendaDto.getNombre());
            }
        }

        model.addAttribute("productos", productos);
        model.addAttribute("tiendaNombres", tiendaNombres);
        model.addAttribute("tienda", idTienda != null ? tiendaService.obtenerTiendaPorId(idTienda) : null);
        model.addAttribute("tipoLista", idTienda != null ? "tienda" : "todos");

        return "producto/list";
    }

    @GetMapping("/tienda/{idTienda}")
    public String listarProductosPorTienda(@PathVariable Long idTienda,
                                           Model model,
                                           Authentication authentication) {
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(idTienda);

        // ✅ REDIRECCIÓN INTELIGENTE: Si es PROVEEDOR dueño de la tienda,
        // redirigir a vista de inventario/panel
        if (authentication != null &&
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {

            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // Si es el propietario de la tienda, mostrar panel de inventario
            if (tienda.getId_usuario().equals(usuario.getId_usuario())) {
                return "redirect:/inventario/tienda/" + idTienda;
            }
        }

        // Para CONSUMIDORES, ADMINISTRADORES o proveedores que no son dueños,
        // mostrar vista de catálogo normal
        List<ProductoDTO> productos = productoService.obtenerProductosPorTienda(idTienda);

        // Filtrar subcategorías según la categoría de la tienda
        CategoriaTienda categoriaTienda = tienda.getCategoriaPrincipal();
        List<SubcategoriaProducto> subcategoriasFiltradas = Arrays.stream(SubcategoriaProducto.values())
                .filter(sub -> sub.perteneceA(categoriaTienda))
                .collect(Collectors.toList());

        model.addAttribute("tienda", tienda);
        model.addAttribute("productos", productos);
        model.addAttribute("subcategorias", subcategoriasFiltradas);
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

            // ✅ VALIDACIÓN: No permitir crear productos si la tienda está inactiva
            if (!"ACTIVA".equals(tienda.getEstado().name())) {
                redirectAttributes.addFlashAttribute("error", "No se pueden crear productos en una tienda inactiva o eliminada.");
                return "redirect:/tiendas/ver/" + idTienda;
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

            // ✅ VALIDACIÓN: No permitir crear productos si la tienda está inactiva
            if (!"ACTIVA".equals(tienda.getEstado().name())) {
                redirectAttributes.addFlashAttribute("error", "No se pueden crear productos en una tienda inactiva o eliminada.");
                return "redirect:/tiendas/ver/" + productoDTO.getId_tienda();
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

    // ========== VER PRODUCTOS CON STOCK BAJO (PROVEEDOR) ==========

    @GetMapping("/stock-bajo")
    public String verProductosStockBajo(Model model,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para acceder a esta página.");
            return "redirect:/";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // Obtener todas las tiendas del proveedor
            List<TiendaDTO> misTiendas = tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario());

            // Obtener productos con stock bajo de todas las tiendas del proveedor
            List<ProductoDTO> productosStockBajo = new java.util.ArrayList<>();
            java.util.Set<Long> tiendasAfectadas = new java.util.HashSet<>();
            java.util.Map<Long, String> tiendaNombres = new java.util.HashMap<>();

            for (TiendaDTO tienda : misTiendas) {
                List<ProductoDTO> productosConStockBajo = productoService.obtenerProductosConStockBajo(tienda.getId_tienda());
                List<ProductoDTO> productosSinStock = productoService.obtenerProductosSinStock(tienda.getId_tienda());

                productosStockBajo.addAll(productosConStockBajo);
                productosStockBajo.addAll(productosSinStock);

                if (!productosConStockBajo.isEmpty() || !productosSinStock.isEmpty()) {
                    tiendasAfectadas.add(tienda.getId_tienda());
                }

                tiendaNombres.put(tienda.getId_tienda(), tienda.getNombre());
            }

            // Eliminar duplicados (en caso de que un producto esté en ambas listas)
            productosStockBajo = productosStockBajo.stream()
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

            // Calcular estadísticas
            long productosSinStock = productosStockBajo.stream()
                    .filter(p -> p.getSinStock() != null && p.getSinStock())
                    .count();

            long productosStockCritico = productosStockBajo.stream()
                    .filter(p -> p.getStock() > 0 && p.getStock() < 5)
                    .count();

            model.addAttribute("productos", productosStockBajo);
            model.addAttribute("totalProductos", productosStockBajo.size());
            model.addAttribute("productosSinStock", productosSinStock);
            model.addAttribute("productosStockCritico", productosStockCritico);
            model.addAttribute("tiendasAfectadas", tiendasAfectadas.size());
            model.addAttribute("tiendaNombres", tiendaNombres);

            return "producto/low-stock";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar productos con stock bajo: " + e.getMessage());
            return "redirect:/proveedor/dashboard";
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