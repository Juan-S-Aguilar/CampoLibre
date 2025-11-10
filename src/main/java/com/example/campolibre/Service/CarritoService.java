package com.example.campolibre.Service;

import com.example.campolibre.DTO.CarritoCompraDTO;
import com.example.campolibre.DTO.ItemCarritoDTO;

import java.util.List;

public interface CarritoService {

    /**
     * Obtiene o crea el carrito del usuario
     */
    CarritoCompraDTO obtenerCarritoPorUsuario(Long idUsuario);

    /**
     * Agrega un producto al carrito (o aumenta cantidad si ya existe)
     */
    ItemCarritoDTO agregarProducto(Long idUsuario, Long idProducto, Integer cantidad);

    /**
     * Actualiza la cantidad de un item en el carrito
     */
    ItemCarritoDTO actualizarCantidad(Long idUsuario, Long idItemCarrito, Integer nuevaCantidad);

    /**
     * Elimina un item del carrito
     */
    void eliminarItem(Long idUsuario, Long idItemCarrito);

    /**
     * Vacía completamente el carrito del usuario
     */
    void vaciarCarrito(Long idUsuario);

    /**
     * Valida que todos los productos del carrito tengan stock disponible
     */
    boolean validarStockCarrito(Long idUsuario);

    /**
     * Obtiene el número total de items en el carrito
     */
    Integer contarItemsCarrito(Long idUsuario);

    // ========== MÉTODOS ADICIONALES NECESARIOS ===========

    /**
     * Devuelve la lista de items del carrito en DTO
     */
    List<ItemCarritoDTO> obtenerItemsCarrito(Long idUsuario);

    /**
     * Calcula el total del carrito (suma de subtotales)
     */
    Double calcularTotal(Long idUsuario);
}