package com.example.campolibre.Service;

import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.Enum.CategoriaProducto;
import com.example.campolibre.Enum.SubcategoriaProducto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductoService {

    // CRUD Básico
    ProductoDTO crearProducto(ProductoDTO productoDTO, MultipartFile imagen);
    ProductoDTO obtenerProductoPorId(Long id);
    List<ProductoDTO> obtenerTodosLosProductos();
    ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO, MultipartFile imagen);
    void eliminarProducto(Long id);

    // Consultas por Estado
    List<ProductoDTO> obtenerProductosActivos();
    List<ProductoDTO> obtenerProductosEliminados();

    // Consultas por Tienda
    List<ProductoDTO> obtenerProductosPorTienda(Long idTienda);
    Long contarProductosActivosPorTienda(Long idTienda);

    // Consultas por Categoría y Subcategoría
    List<ProductoDTO> obtenerProductosPorCategoria(CategoriaProducto categoria);
    List<ProductoDTO> obtenerProductosPorSubcategoria(SubcategoriaProducto subcategoria);
    List<ProductoDTO> obtenerProductosPorCategoriaYSubcategoria(CategoriaProducto categoria, SubcategoriaProducto subcategoria);

    // Gestión de Stock
    boolean descontarStock(Long idProducto, Integer cantidad);
    boolean incrementarStock(Long idProducto, Integer cantidad);
    ProductoDTO aumentarStockManual(Long idProducto, Integer cantidad);
    ProductoDTO disminuirStockManual(Long idProducto, Integer cantidad);
    List<ProductoDTO> obtenerProductosConStockBajo(Integer umbral);

    // Búsqueda y Filtros
    List<ProductoDTO> buscarProductosPorNombre(String nombre);
    List<ProductoDTO> obtenerProductosPorRangoPrecio(Double precioMin, Double precioMax);

    // Cambio de Estado
    ProductoDTO cambiarEstadoProducto(Long id, String nuevoEstado);
    ProductoDTO activarProducto(Long id);
    ProductoDTO inactivarProducto(Long id);
}