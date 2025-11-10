package com.example.campolibre.Service;

import com.example.campolibre.DTO.ProductoDTO;
import com.example.campolibre.Enum.CategoriaProducto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ProductoService {
    ProductoDTO crearProducto(ProductoDTO productoDTO, MultipartFile imagen);
    ProductoDTO obtenerProductoPorId(Long id);
    List<ProductoDTO> obtenerTodosLosProductos();
    List<ProductoDTO> obtenerProductosActivos();
    List<ProductoDTO> obtenerProductosPorTienda(Long idTienda);
    List<ProductoDTO> obtenerProductosPorCategoria(CategoriaProducto categoria);
    List<ProductoDTO> obtenerProductosEliminados();
    ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO, MultipartFile imagen);
    void eliminarProducto(Long id);
}