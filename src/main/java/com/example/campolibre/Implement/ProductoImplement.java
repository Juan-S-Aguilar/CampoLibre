package com.example.campolibre.Implement;

import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.DTO.ResumenInventarioDTO;
import com.example.campolibre.Entity.Producto;
import com.example.campolibre.Entity.Tienda;
import com.example.campolibre.Enum.SubcategoriaProducto;
import com.example.campolibre.Enum.UnidadMedida;
import com.example.campolibre.Repository.ItemPedidoRepository;
import com.example.campolibre.DTO.ProductoMasVendidoDTO;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.ProductoRepository;
import com.example.campolibre.Repository.TiendaRepository;
import com.example.campolibre.Service.FileStorageService;
import com.example.campolibre.Service.ProductoService;
import com.example.campolibre.Util.MapeoCategoria;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoImplement implements ProductoService {

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    private final ProductoRepository productoRepository;
    private final TiendaRepository tiendaRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;

    @Autowired
    public ProductoImplement(ProductoRepository productoRepository, TiendaRepository tiendaRepository,
                             FileStorageService fileStorageService, ModelMapper modelMapper) {
        this.productoRepository = productoRepository;
        this.tiendaRepository = tiendaRepository;
        this.fileStorageService = fileStorageService;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductoDTO crearProducto(ProductoDTO productoDTO, MultipartFile imagen) {
        Tienda tienda = tiendaRepository.findById(productoDTO.getId_tienda())
                .orElseThrow(() -> new CustomException("Tienda no encontrada"));

        // ✅ VALIDACIÓN 1: Subcategoría debe estar definida
        if (productoDTO.getSubcategoria() == null) {
            throw new CustomException("Debe seleccionar una subcategoría para el producto.");
        }

        // ✅ VALIDACIÓN 2: Subcategoría coherente con categoría de tienda
        if (!MapeoCategoria.esSubcategoriaValida(
                tienda.getCategoriaPrincipal(),
                productoDTO.getSubcategoria())) {
            throw new CustomException(
                    "La subcategoría '" + productoDTO.getSubcategoria().getDisplayName() +
                            "' no corresponde con la categoría de la tienda '" +
                            tienda.getCategoriaPrincipal().getDisplayName() + "'."
            );
        }

        // ✅ VALIDACIÓN 3: Unidad de medida debe estar definida
        if (productoDTO.getUnidadMedida() == null) {
            throw new CustomException("Debe especificar una unidad de medida para el producto.");
        }

        Producto producto = modelMapper.map(productoDTO, Producto.class);
        producto.setTienda(tienda);
        producto.setEstado("ACTIVO");

        // ✅ Desactivar automáticamente si stock es 0
        if (producto.getStock() == 0) {
            producto.desactivarPorSinStock();
        }

        if (imagen != null && !imagen.isEmpty()) {
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "productos");
            producto.setImagen_producto(rutaImagen);
        }

        Producto nuevoProducto = productoRepository.save(producto);

        ProductoDTO resultado = convertirADTO(nuevoProducto);
        return resultado;
    }

    @Override
    public ProductoDTO obtenerProductoPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        return convertirADTO(producto);
    }

    @Override
    public List<ProductoDTO> obtenerTodosLosProductos() {
        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> obtenerProductosActivos() {
        List<Producto> productos = productoRepository.findAllActive();
        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> obtenerProductosPorTienda(Long idTienda) {
        List<Producto> productos = productoRepository.findAllByTiendaId(idTienda);
        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> obtenerProductosEliminados() {
        List<Producto> productos = productoRepository.findAllDeleted();
        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO, MultipartFile imagen) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        // ✅ VALIDACIÓN: Si se cambia subcategoría, validar coherencia
        if (productoDTO.getSubcategoria() != null &&
                !productoDTO.getSubcategoria().equals(productoExistente.getSubcategoria())) {

            Tienda tienda = productoExistente.getTienda();
            if (!MapeoCategoria.esSubcategoriaValida(
                    tienda.getCategoriaPrincipal(),
                    productoDTO.getSubcategoria())) {
                throw new CustomException(
                        "La subcategoría '" + productoDTO.getSubcategoria().getDisplayName() +
                                "' no corresponde con la categoría de la tienda."
                );
            }
            productoExistente.setSubcategoria(productoDTO.getSubcategoria());
        }

        // Actualizar campos básicos
        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setStock(productoDTO.getStock());

        if (productoDTO.getStockMinimo() != null) {
            productoExistente.setStockMinimo(productoDTO.getStockMinimo());
        }

        if (productoDTO.getUnidadMedida() != null) {
            productoExistente.setUnidadMedida(productoDTO.getUnidadMedida());
        }

        // ✅ Manejar estado según stock
        if (productoExistente.getStock() == 0) {
            productoExistente.desactivarPorSinStock();
        } else if ("SIN_STOCK".equals(productoExistente.getEstado())) {
            productoExistente.setEstado("ACTIVO");
        }

        // Manejar imagen
        if (imagen != null && !imagen.isEmpty()) {
            if (productoExistente.getImagen_producto() != null) {
                fileStorageService.eliminarArchivo(productoExistente.getImagen_producto());
            }
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "productos");
            productoExistente.setImagen_producto(rutaImagen);
        } else if (productoDTO.getImagen_producto() != null) {
            productoExistente.setImagen_producto(productoDTO.getImagen_producto());
        }

        Producto productoActualizado = productoRepository.save(productoExistente);

        return convertirADTO(productoActualizado);
    }

    @Override
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));
        producto.setEstado("ELIMINADO");
        productoRepository.save(producto);
    }

    // ========== NUEVOS MÉTODOS - BÚSQUEDA POR SUBCATEGORÍA ==========

    @Override
    public List<ProductoDTO> obtenerProductosPorSubcategoria(SubcategoriaProducto subcategoria) {
        List<Producto> productos = productoRepository.findBySubcategoriaAndEstadoActivo(subcategoria);
        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // ========== NUEVOS MÉTODOS - BÚSQUEDA POR UNIDAD DE MEDIDA ==========

    @Override
    public List<ProductoDTO> obtenerProductosPorUnidadMedida(UnidadMedida unidad) {
        List<Producto> productos = productoRepository.findByUnidadMedidaAndEstadoActivo(unidad);
        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // ========== NUEVOS MÉTODOS - GESTIÓN DE INVENTARIO ==========

    @Override
    @Transactional
    public ProductoDTO incrementarStock(Long idProducto, Integer cantidad) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        if (cantidad <= 0) {
            throw new CustomException("La cantidad debe ser mayor a 0");
        }

        producto.setStock(producto.getStock() + cantidad);

        // Si estaba sin stock, reactivar
        if ("SIN_STOCK".equals(producto.getEstado())) {
            producto.setEstado("ACTIVO");
        }

        Producto actualizado = productoRepository.save(producto);
        return convertirADTO(actualizado);
    }

    @Override
    @Transactional
    public ProductoDTO reducirStock(Long idProducto, Integer cantidad) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        if (cantidad <= 0) {
            throw new CustomException("La cantidad debe ser mayor a 0");
        }

        if (producto.getStock() < cantidad) {
            throw new CustomException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        producto.setStock(producto.getStock() - cantidad);

        // Si llega a 0, desactivar automáticamente
        if (producto.getStock() == 0) {
            producto.desactivarPorSinStock();
        }

        Producto actualizado = productoRepository.save(producto);
        return convertirADTO(actualizado);
    }

    @Override
    @Transactional
    public ProductoDTO actualizarStock(Long idProducto, Integer nuevoStock) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        if (nuevoStock < 0) {
            throw new CustomException("El stock no puede ser negativo");
        }

        producto.setStock(nuevoStock);

        if (nuevoStock == 0) {
            producto.desactivarPorSinStock();
        } else if ("SIN_STOCK".equals(producto.getEstado())) {
            producto.setEstado("ACTIVO");
        }

        Producto actualizado = productoRepository.save(producto);
        return convertirADTO(actualizado);
    }

    @Override
    @Transactional
    public ProductoDTO reactivarProducto(Long idProducto, Integer nuevoStock) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        if (nuevoStock <= 0) {
            throw new CustomException("El stock debe ser mayor a 0 para reactivar el producto");
        }

        producto.reactivarConStock(nuevoStock);
        Producto actualizado = productoRepository.save(producto);

        return convertirADTO(actualizado);
    }

    @Override
    public List<ProductoDTO> obtenerProductosConStockBajo(Long idTienda) {
        List<Producto> productos = productoRepository.findProductosConStockBajo(idTienda);
        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> obtenerProductosSinStock(Long idTienda) {
        List<Producto> productos = productoRepository.findByTiendaIdAndStock(idTienda, 0);
        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResumenInventarioDTO obtenerResumenInventario(Long idTienda) {
        List<Producto> todosProductos = productoRepository.findAllByTiendaId(idTienda);

        ResumenInventarioDTO resumen = new ResumenInventarioDTO();
        resumen.setIdTienda(idTienda);

        if (!todosProductos.isEmpty()) {
            resumen.setNombreTienda(todosProductos.get(0).getTienda().getNombre());
        }

        resumen.setTotalProductos(todosProductos.size());

        long activos = todosProductos.stream()
                .filter(p -> "ACTIVO".equals(p.getEstado()))
                .count();
        resumen.setProductosActivos((int) activos);

        long stockBajo = todosProductos.stream()
                .filter(Producto::tieneStockBajo)
                .count();
        resumen.setProductosConStockBajo((int) stockBajo);

        long sinStock = todosProductos.stream()
                .filter(Producto::sinStock)
                .count();
        resumen.setProductosSinStock((int) sinStock);

        long inactivos = todosProductos.stream()
                .filter(p -> "INACTIVO".equals(p.getEstado()) ||
                        "SIN_STOCK".equals(p.getEstado()))
                .count();
        resumen.setProductosInactivos((int) inactivos);

        double valorTotal = todosProductos.stream()
                .mapToDouble(p -> p.getPrecio() * p.getStock())
                .sum();
        resumen.setValorTotalInventario(valorTotal);

        // Productos con alerta (stock bajo o sin stock)
        List<ProductoDTO> productosAlerta = todosProductos.stream()
                .filter(p -> p.tieneStockBajo() || p.sinStock())
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        resumen.setProductosAlerta(productosAlerta);

        return resumen;
    }

    @Override
    public Long contarProductosConStockBajo(Long idTienda) {
        return productoRepository.contarProductosConStockBajo(idTienda);
    }

    @Override
    public Long contarProductosSinStock(Long idTienda) {
        return productoRepository.contarProductosSinStock(idTienda);
    }

    // ========== MÉTODO PRIVADO HELPER ==========

    private ProductoDTO convertirADTO(Producto producto) {
        ProductoDTO dto = modelMapper.map(producto, ProductoDTO.class);
        dto.setId_tienda(producto.getTienda().getId_tienda());
        dto.setTieneStockBajo(producto.tieneStockBajo());
        dto.setSinStock(producto.sinStock());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoMasVendidoDTO> obtenerProductosMasVendidos(Long idTienda, Integer limite) {
        // Obtener datos desde el repository
        List<Object[]> resultados = itemPedidoRepository.findProductosMasVendidosPorTienda(idTienda);

        // Aplicar límite si no está definido, usar 10 por defecto
        int limiteAplicar = (limite != null && limite > 0) ? limite : 10;

        // Convertir resultados a DTOs
        return resultados.stream()
                .limit(limiteAplicar)
                .map(obj -> {
                    String nombreProducto = (String) obj[0];
                    Long cantidadVendida = ((Number) obj[1]).longValue();

                    ProductoMasVendidoDTO dto = new ProductoMasVendidoDTO();
                    dto.setNombreProducto(nombreProducto);
                    dto.setCantidadVendida(cantidadVendida);

                    return dto;
                })
                .collect(Collectors.toList());
    }

}
