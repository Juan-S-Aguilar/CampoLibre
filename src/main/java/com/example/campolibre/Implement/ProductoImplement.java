package com.example.campolibre.Implement;

import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Service.FileStorageService;
import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.Entity.Producto;
import com.example.campolibre.Entity.Tienda;
import com.example.campolibre.Enum.CategoriaProducto;
import com.example.campolibre.Enum.SubcategoriaProducto;
import com.example.campolibre.Repository.ProductoRepository;
import com.example.campolibre.Repository.TiendaRepository;
import com.example.campolibre.Service.ProductoService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoImplement implements ProductoService {

    private final ProductoRepository productoRepository;
    private final TiendaRepository tiendaRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;

    public ProductoImplement(ProductoRepository productoRepository,
                                    TiendaRepository tiendaRepository,
                                    FileStorageService fileStorageService,
                                    ModelMapper modelMapper) {
        this.productoRepository = productoRepository;
        this.tiendaRepository = tiendaRepository;
        this.fileStorageService = fileStorageService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public ProductoDTO crearProducto(ProductoDTO productoDTO, MultipartFile imagen) {
        // Validar que la tienda existe
        Tienda tienda = tiendaRepository.findById(productoDTO.getId_tienda())
                .orElseThrow(() -> new CustomException("Tienda no encontrada"));

        // Validar que la subcategoría pertenece a la categoría
        if (!productoDTO.esSubcategoriaValida()) {
            throw new CustomException("La subcategoría no corresponde a la categoría seleccionada");
        }

        Producto producto = modelMapper.map(productoDTO, Producto.class);
        producto.setTienda(tienda);
        producto.setEstado("ACTIVO");

        // Guardar imagen si existe
        if (imagen != null && !imagen.isEmpty()) {
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "productos");
            producto.setImagen_producto(rutaImagen);
        }

        Producto nuevoProducto = productoRepository.save(producto);

        ProductoDTO resultado = modelMapper.map(nuevoProducto, ProductoDTO.class);
        resultado.setId_tienda(nuevoProducto.getTienda().getId_tienda());
        resultado.setNombre_tienda(nuevoProducto.getTienda().getNombre());
        return resultado;
    }

    @Override
    public ProductoDTO obtenerProductoPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        ProductoDTO resultado = modelMapper.map(producto, ProductoDTO.class);
        resultado.setId_tienda(producto.getTienda().getId_tienda());
        resultado.setNombre_tienda(producto.getTienda().getNombre());
        return resultado;
    }

    @Override
    public List<ProductoDTO> obtenerTodosLosProductos() {
        List<Producto> productos = productoRepository.findAll();
        return convertirListaADTO(productos);
    }

    @Override
    public List<ProductoDTO> obtenerProductosActivos() {
        List<Producto> productos = productoRepository.findAllActive();
        return convertirListaADTO(productos);
    }

    @Override
    public List<ProductoDTO> obtenerProductosPorTienda(Long idTienda) {
        List<Producto> productos = productoRepository.findByTiendaIdAndEstadoActivo(idTienda);
        return convertirListaADTO(productos);
    }

    @Override
    public List<ProductoDTO> obtenerProductosPorCategoria(CategoriaProducto categoria) {
        List<Producto> productos = productoRepository.findByCategoriaAndEstadoActivo(categoria);
        return convertirListaADTO(productos);
    }

    @Override
    public List<ProductoDTO> obtenerProductosPorSubcategoria(SubcategoriaProducto subcategoria) {
        List<Producto> productos = productoRepository.findBySubcategoriaAndEstadoActivo(subcategoria);
        return convertirListaADTO(productos);
    }

    @Override
    public List<ProductoDTO> obtenerProductosPorCategoriaYSubcategoria(CategoriaProducto categoria, SubcategoriaProducto subcategoria) {
        List<Producto> productos = productoRepository.findByCategoriaAndSubcategoriaAndEstadoActivo(categoria, subcategoria);
        return convertirListaADTO(productos);
    }

    @Override
    public List<ProductoDTO> obtenerProductosEliminados() {
        List<Producto> productos = productoRepository.findAllDeleted();
        return convertirListaADTO(productos);
    }

    @Override
    @Transactional
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO, MultipartFile imagen) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        // Validar subcategoría
        if (!productoDTO.esSubcategoriaValida()) {
            throw new CustomException("La subcategoría no corresponde a la categoría seleccionada");
        }

        // Actualizar campos
        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setStock(productoDTO.getStock());
        productoExistente.setCategoria(productoDTO.getCategoria());
        productoExistente.setSubcategoria(productoDTO.getSubcategoria());
        productoExistente.setCantidad(productoDTO.getCantidad());
        productoExistente.setUnidadMedida(productoDTO.getUnidadMedida());

        // Actualizar imagen si se proporciona una nueva
        if (imagen != null && !imagen.isEmpty()) {
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "productos");
            productoExistente.setImagen_producto(rutaImagen);
        }

        Producto productoActualizado = productoRepository.save(productoExistente);

        ProductoDTO resultado = modelMapper.map(productoActualizado, ProductoDTO.class);
        resultado.setId_tienda(productoActualizado.getTienda().getId_tienda());
        resultado.setNombre_tienda(productoActualizado.getTienda().getNombre());
        return resultado;
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));
        producto.setEstado("ELIMINADO");
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public boolean descontarStock(Long idProducto, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new CustomException("La cantidad debe ser mayor a cero");
        }
        int filasActualizadas = productoRepository.descontarStock(idProducto, cantidad);
        return filasActualizadas > 0;
    }

    @Override
    @Transactional
    public boolean incrementarStock(Long idProducto, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new CustomException("La cantidad debe ser mayor a cero");
        }
        int filasActualizadas = productoRepository.incrementarStock(idProducto, cantidad);
        return filasActualizadas > 0;
    }

    @Override
    @Transactional
    public ProductoDTO aumentarStockManual(Long idProducto, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new CustomException("La cantidad a aumentar debe ser mayor a cero");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        int stockAnterior = producto.getStock();
        producto.setStock(stockAnterior + cantidad);
        Producto productoActualizado = productoRepository.save(producto);

        System.out.println(String.format("✓ Stock aumentado: Producto '%s' | Stock anterior: %d | Cantidad agregada: %d | Stock actual: %d",
                producto.getNombre(), stockAnterior, cantidad, productoActualizado.getStock()));

        ProductoDTO resultado = modelMapper.map(productoActualizado, ProductoDTO.class);
        resultado.setId_tienda(productoActualizado.getTienda().getId_tienda());
        resultado.setNombre_tienda(productoActualizado.getTienda().getNombre());
        return resultado;
    }

    @Override
    @Transactional
    public ProductoDTO disminuirStockManual(Long idProducto, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new CustomException("La cantidad a disminuir debe ser mayor a cero");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        int stockActual = producto.getStock();

        if (stockActual < cantidad) {
            throw new CustomException(String.format(
                    "Stock insuficiente. Stock actual: %d, Cantidad solicitada: %d",
                    stockActual, cantidad));
        }

        int stockAnterior = producto.getStock();
        producto.setStock(stockActual - cantidad);
        Producto productoActualizado = productoRepository.save(producto);

        System.out.println(String.format("✓ Stock disminuido: Producto '%s' | Stock anterior: %d | Cantidad restada: %d | Stock actual: %d",
                producto.getNombre(), stockAnterior, cantidad, productoActualizado.getStock()));

        ProductoDTO resultado = modelMapper.map(productoActualizado, ProductoDTO.class);
        resultado.setId_tienda(productoActualizado.getTienda().getId_tienda());
        resultado.setNombre_tienda(productoActualizado.getTienda().getNombre());
        return resultado;
    }

    @Override
    public List<ProductoDTO> obtenerProductosConStockBajo(Integer umbral) {
        if (umbral == null || umbral < 0) {
            umbral = 10; // Valor por defecto
        }
        List<Producto> productos = productoRepository.findProductosConStockBajo(umbral);
        return convertirListaADTO(productos);
    }

    @Override
    public List<ProductoDTO> buscarProductosPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return obtenerProductosActivos();
        }
        List<Producto> productos = productoRepository.buscarPorNombre(nombre.trim());
        return convertirListaADTO(productos);
    }

    @Override
    public List<ProductoDTO> obtenerProductosPorRangoPrecio(Double precioMin, Double precioMax) {
        if (precioMin == null) precioMin = 0.0;
        if (precioMax == null) precioMax = Double.MAX_VALUE;

        if (precioMin > precioMax) {
            throw new CustomException("El precio mínimo no puede ser mayor al precio máximo");
        }

        List<Producto> productos = productoRepository.findByRangoPrecio(precioMin, precioMax);
        return convertirListaADTO(productos);
    }

    @Override
    public Long contarProductosActivosPorTienda(Long idTienda) {
        return productoRepository.contarProductosActivosPorTienda(idTienda);
    }

    @Override
    @Transactional
    public ProductoDTO cambiarEstadoProducto(Long id, String nuevoEstado) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        if (!nuevoEstado.equals("ACTIVO") && !nuevoEstado.equals("INACTIVO") && !nuevoEstado.equals("ELIMINADO")) {
            throw new CustomException("Estado no válido. Use: ACTIVO, INACTIVO o ELIMINADO");
        }

        producto.setEstado(nuevoEstado);
        Producto productoActualizado = productoRepository.save(producto);

        ProductoDTO resultado = modelMapper.map(productoActualizado, ProductoDTO.class);
        resultado.setId_tienda(productoActualizado.getTienda().getId_tienda());
        resultado.setNombre_tienda(productoActualizado.getTienda().getNombre());
        return resultado;
    }

    @Override
    @Transactional
    public ProductoDTO activarProducto(Long id) {
        return cambiarEstadoProducto(id, "ACTIVO");
    }

    @Override
    @Transactional
    public ProductoDTO inactivarProducto(Long id) {
        return cambiarEstadoProducto(id, "INACTIVO");
    }

    // Método auxiliar para convertir lista
    private List<ProductoDTO> convertirListaADTO(List<Producto> productos) {
        return productos.stream()
                .map(producto -> {
                    ProductoDTO dto = modelMapper.map(producto, ProductoDTO.class);
                    dto.setId_tienda(producto.getTienda().getId_tienda());
                    dto.setNombre_tienda(producto.getTienda().getNombre());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}