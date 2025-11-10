package com.example.campolibre.Implement;

import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.Entity.Producto;
import com.example.campolibre.Entity.Tienda;
import com.example.campolibre.Enum.CategoriaProducto;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.ProductoRepository;
import com.example.campolibre.Repository.TiendaRepository;
import com.example.campolibre.Service.FileStorageService;
import com.example.campolibre.Service.ProductoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoImplement implements ProductoService {

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

        Producto producto = modelMapper.map(productoDTO, Producto.class);
        producto.setTienda(tienda);
        producto.setEstado("ACTIVO");

        if (imagen != null && !imagen.isEmpty()) {
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "productos");
            producto.setImagen_producto(rutaImagen);
        }

        Producto nuevoProducto = productoRepository.save(producto);

        ProductoDTO resultado = modelMapper.map(nuevoProducto, ProductoDTO.class);
        resultado.setId_tienda(nuevoProducto.getTienda().getId_tienda());
        return resultado;
    }

    @Override
    public ProductoDTO obtenerProductoPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        ProductoDTO resultado = modelMapper.map(producto, ProductoDTO.class);
        resultado.setId_tienda(producto.getTienda().getId_tienda());
        return resultado;
    }

    @Override
    public List<ProductoDTO> obtenerTodosLosProductos() {
        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .map(producto -> {
                    ProductoDTO dto = modelMapper.map(producto, ProductoDTO.class);
                    dto.setId_tienda(producto.getTienda().getId_tienda());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> obtenerProductosActivos() {
        List<Producto> productos = productoRepository.findAllActive();
        return productos.stream()
                .map(producto -> {
                    ProductoDTO dto = modelMapper.map(producto, ProductoDTO.class);
                    dto.setId_tienda(producto.getTienda().getId_tienda());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> obtenerProductosPorTienda(Long idTienda) {
        List<Producto> productos = productoRepository.findByTiendaIdAndEstadoActivo(idTienda);
        return productos.stream()
                .map(producto -> {
                    ProductoDTO dto = modelMapper.map(producto, ProductoDTO.class);
                    dto.setId_tienda(producto.getTienda().getId_tienda());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> obtenerProductosPorCategoria(CategoriaProducto categoria) {
        List<Producto> productos = productoRepository.findByCategoriaAndEstadoActivo(categoria);
        return productos.stream()
                .map(producto -> {
                    ProductoDTO dto = modelMapper.map(producto, ProductoDTO.class);
                    dto.setId_tienda(producto.getTienda().getId_tienda());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> obtenerProductosEliminados() {
        List<Producto> productos = productoRepository.findAllDeleted();
        return productos.stream()
                .map(producto -> {
                    ProductoDTO dto = modelMapper.map(producto, ProductoDTO.class);
                    dto.setId_tienda(producto.getTienda().getId_tienda());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO, MultipartFile imagen) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        // Actualizar campos básicos
        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setStock(productoDTO.getStock());
        productoExistente.setCategoria(productoDTO.getCategoria());

        // ✅ CORRECCIÓN: Manejar imagen desde MultipartFile O desde DTO
        if (imagen != null && !imagen.isEmpty()) {
            // Caso 1: Viene imagen nueva desde el form (usado en versiones antiguas)
            System.out.println("📸 [Service] Guardando imagen desde MultipartFile");
            if (productoExistente.getImagen_producto() != null) {
                fileStorageService.eliminarArchivo(productoExistente.getImagen_producto());
            }
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "productos");
            productoExistente.setImagen_producto(rutaImagen);
        } else if (productoDTO.getImagen_producto() != null) {
            // ✅ Caso 2: La imagen ya fue guardada en el Controller
            System.out.println("📸 [Service] Usando imagen desde DTO: " + productoDTO.getImagen_producto());
            productoExistente.setImagen_producto(productoDTO.getImagen_producto());
        }
        // Si ambos son null, mantiene la imagen existente (no hace nada)

        Producto productoActualizado = productoRepository.save(productoExistente);

        ProductoDTO resultado = modelMapper.map(productoActualizado, ProductoDTO.class);
        resultado.setId_tienda(productoActualizado.getTienda().getId_tienda());
        return resultado;
    }

    @Override
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));
        producto.setEstado("ELIMINADO");
        productoRepository.save(producto);
    }
}