package com.example.campolibre.Service;

import com.example.campolibre.DTO.ConfirmarPedidoRequest;
import com.example.campolibre.DTO.PedidoDTO;
import com.example.campolibre.Enum.EstadoPedido;

import java.util.List;

public interface PedidoService {

    /**
     * Crea un pedido desde el carrito del usuario
     */
    PedidoDTO crearPedidoDesdeCarrito(Long idUsuario, ConfirmarPedidoRequest datosEntrega);

    /**
     * Obtiene un pedido por su ID
     */
    PedidoDTO obtenerPedidoPorId(Long idPedido);

    /**
     * Obtiene un pedido por su número
     */
    PedidoDTO obtenerPedidoPorNumero(String numeroPedido);

    /**
     * Obtiene todos los pedidos de un usuario
     */
    List<PedidoDTO> obtenerPedidosPorUsuario(Long idUsuario);

    /**
     * Obtiene pedidos por estado
     */
    List<PedidoDTO> obtenerPedidosPorEstado(EstadoPedido estado);

    /**
     * Obtiene pedidos que contienen productos de una tienda
     */
    List<PedidoDTO> obtenerPedidosPorTienda(Long idTienda);

    /**
     * Obtiene solo pedidos pagados de una tienda
     */
    List<PedidoDTO> obtenerPedidosPagadosPorTienda(Long idTienda);

    /**
     * Cancela un pedido
     */
    void cancelarPedido(Long idPedido);

    /**
     * Calcula las ganancias totales de una tienda
     */
    Double calcularGananciasTienda(Long idTienda);

    /**
     * Genera el siguiente número de pedido único
     */
    String generarNumeroPedido();

    /**
     * Obtiene todos los pedidos que contienen productos de las tiendas de un proveedor
     */
    List<PedidoDTO> obtenerPedidosPorProveedor(Long idUsuarioProveedor);
}