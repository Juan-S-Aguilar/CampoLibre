package com.example.campolibre.Service;

import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.DTO.ProductoMasVendidoDTO;
import com.example.campolibre.DTO.ResumenInventarioDTO;
import com.example.campolibre.Enum.SubcategoriaProducto;
import com.example.campolibre.Enum.UnidadMedida;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ProductoService {
    ProductoDTO crearProducto(ProductoDTO productoDTO, MultipartFile imagen);
    ProductoDTO obtenerProductoPorId(Long id);
    List<ProductoDTO> obtenerTodosLosProductos();
    List<ProductoDTO> obtenerProductosActivos();
    List<ProductoDTO> obtenerProductosPorTienda(Long idTienda);
    List<ProductoDTO> obtenerProductosEliminados();
    ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO, MultipartFile imagen);
    void eliminarProducto(Long id);

    // ========== NUEVOS MÉTODOS - BÚSQUEDA POR SUBCATEGORÍA ==========

    /**
     * Obtiene productos activos por subcategoría
     */
    List<ProductoDTO> obtenerProductosPorSubcategoria(SubcategoriaProducto subcategoria);

    // ========== NUEVOS MÉTODOS - BÚSQUEDA POR UNIDAD DE MEDIDA ==========

    /**
     * Obtiene productos activos por unidad de medida
     */
    List<ProductoDTO> obtenerProductosPorUnidadMedida(UnidadMedida unidad);

    // ========== NUEVOS MÉTODOS - GESTIÓN DE INVENTARIO ==========

    /**
     * Incrementa el stock de un producto
     */
    ProductoDTO incrementarStock(Long idProducto, Integer cantidad);

    /**
     * Reduce el stock de un producto
     */
    ProductoDTO reducirStock(Long idProducto, Integer cantidad);

    /**
     * Actualiza el stock directamente
     */
    ProductoDTO actualizarStock(Long idProducto, Integer nuevoStock);

    /**
     * Reactiva un producto sin stock y actualiza inventario
     */
    ProductoDTO reactivarProducto(Long idProducto, Integer nuevoStock);

    /**
     * Obtiene productos con stock bajo de una tienda
     */
    List<ProductoDTO> obtenerProductosConStockBajo(Long idTienda);

    /**
     * Obtiene productos sin stock de una tienda
     */
    List<ProductoDTO> obtenerProductosSinStock(Long idTienda);

    /**
     * Obtiene resumen de inventario de una tienda
     */
    ResumenInventarioDTO obtenerResumenInventario(Long idTienda);

    /**
     * Cuenta productos con stock bajo
     */
    Long contarProductosConStockBajo(Long idTienda);

    /**
     * Cuenta productos sin stock
     */
    Long contarProductosSinStock(Long idTienda);

    /**
     * Obtiene los productos más vendidos de una tienda
     * @param idTienda ID de la tienda
     * @param limite Número máximo de productos a retornar (ej: top 10)
     * @return Lista de productos más vendidos ordenados por cantidad
     */
    List<ProductoMasVendidoDTO> obtenerProductosMasVendidos(Long idTienda, Integer limite);

}