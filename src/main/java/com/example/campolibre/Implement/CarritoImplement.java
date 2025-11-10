package com.example.campolibre.Implement;

import com.example.campolibre.DTO.CarritoCompraDTO;
import com.example.campolibre.DTO.ItemCarritoDTO;
import com.example.campolibre.Entity.*;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.*;
import com.example.campolibre.Service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarritoImplement implements CarritoService {

    @Autowired
    private CarritoCompraRepository carritoRepository;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public CarritoCompraDTO obtenerCarritoPorUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        CarritoCompra carrito = carritoRepository.findByUsuarioIdWithItems(idUsuario)
                .orElseGet(() -> crearCarritoNuevo(usuario));

        return convertirADTO(carrito);
    }

    @Override
    @Transactional
    public ItemCarritoDTO agregarProducto(Long idUsuario, Long idProducto, Integer cantidad) {
        if (cantidad <= 0) {
            throw new CustomException("La cantidad debe ser mayor a 0");
        }

        // Validar usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        // Validar producto
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new CustomException("Producto no encontrado"));

        if (!producto.getEstado().equals("ACTIVO")) {
            throw new CustomException("El producto no está disponible");
        }

        if (producto.getTienda().getEstado().name().equals("ELIMINADA") ||
                producto.getTienda().getEstado().name().equals("INACTIVA")) {
            throw new CustomException("La tienda de este producto no está disponible");
        }

        // Validar stock disponible
        if (producto.getStock() < cantidad) {
            throw new CustomException("Stock insuficiente. Solo hay " + producto.getStock() + " unidades disponibles");
        }

        // Obtener o crear carrito
        CarritoCompra carrito = carritoRepository.findByUsuarioId(idUsuario)
                .orElseGet(() -> crearCarritoNuevo(usuario));

        // Verificar si el producto ya está en el carrito
        ItemCarrito itemExistente = itemCarritoRepository
                .findByCarritoIdAndProductoId(carrito.getId_carrito(), idProducto)
                .orElse(null);

        if (itemExistente != null) {
            // Actualizar cantidad
            int nuevaCantidad = itemExistente.getCantidad() + cantidad;

            if (producto.getStock() < nuevaCantidad) {
                throw new CustomException("Stock insuficiente. Solo hay " + producto.getStock() + " unidades disponibles");
            }

            itemExistente.setCantidad(nuevaCantidad);
            itemExistente.calcularSubtotal();
            ItemCarrito itemActualizado = itemCarritoRepository.save(itemExistente);

            return convertirItemADTO(itemActualizado);
        } else {
            // Crear nuevo item
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecio_unitario(producto.getPrecio());
            nuevoItem.calcularSubtotal();

            ItemCarrito itemGuardado = itemCarritoRepository.save(nuevoItem);

            return convertirItemADTO(itemGuardado);
        }
    }

    @Override
    @Transactional
    public ItemCarritoDTO actualizarCantidad(Long idUsuario, Long idItemCarrito, Integer nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            throw new CustomException("La cantidad debe ser mayor a 0");
        }

        ItemCarrito item = itemCarritoRepository.findById(idItemCarrito)
                .orElseThrow(() -> new CustomException("Item no encontrado en el carrito"));

        // Verificar que el item pertenece al carrito del usuario
        if (!item.getCarrito().getUsuario().getId_usuario().equals(idUsuario)) {
            throw new CustomException("No tienes permiso para modificar este item");
        }

        // Validar stock disponible
        if (item.getProducto().getStock() < nuevaCantidad) {
            throw new CustomException("Stock insuficiente. Solo hay " +
                    item.getProducto().getStock() + " unidades disponibles");
        }

        item.setCantidad(nuevaCantidad);
        item.calcularSubtotal();
        ItemCarrito itemActualizado = itemCarritoRepository.save(item);

        return convertirItemADTO(itemActualizado);
    }

    @Override
    @Transactional
    public void eliminarItem(Long idUsuario, Long idItemCarrito) {
        ItemCarrito item = itemCarritoRepository.findById(idItemCarrito)
                .orElseThrow(() -> new CustomException("Item no encontrado en el carrito"));

        // Verificar que el item pertenece al carrito del usuario
        if (!item.getCarrito().getUsuario().getId_usuario().equals(idUsuario)) {
            throw new CustomException("No tienes permiso para eliminar este item");
        }

        itemCarritoRepository.delete(item);
    }

    @Override
    @Transactional
    public void vaciarCarrito(Long idUsuario) {
        CarritoCompra carrito = carritoRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new CustomException("Carrito no encontrado"));

        itemCarritoRepository.deleteByCarritoId(carrito.getId_carrito());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validarStockCarrito(Long idUsuario) {
        CarritoCompra carrito = carritoRepository.findByUsuarioIdWithItems(idUsuario)
                .orElseThrow(() -> new CustomException("Carrito no encontrado"));

        for (ItemCarrito item : carrito.getItems()) {
            if (!item.stockDisponible()) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarItemsCarrito(Long idUsuario) {
        CarritoCompra carrito = carritoRepository.findByUsuarioId(idUsuario)
                .orElse(null);

        if (carrito == null) {
            return 0;
        }

        return carrito.getCantidadTotalItems();
    }

    // ========== MÉTODOS PRIVADOS HELPER ==========

    private CarritoCompra crearCarritoNuevo(Usuario usuario) {
        CarritoCompra nuevoCarrito = new CarritoCompra();
        nuevoCarrito.setUsuario(usuario);
        return carritoRepository.save(nuevoCarrito);
    }

    private CarritoCompraDTO convertirADTO(CarritoCompra carrito) {
        CarritoCompraDTO dto = new CarritoCompraDTO();
        dto.setId_carrito(carrito.getId_carrito());
        dto.setId_usuario(carrito.getUsuario().getId_usuario());
        dto.setFecha_creacion(carrito.getFecha_creacion());
        dto.setFecha_actualizacion(carrito.getFecha_actualizacion());

        List<ItemCarritoDTO> itemsDTO = carrito.getItems().stream()
                .map(this::convertirItemADTO)
                .collect(Collectors.toList());

        dto.setItems(itemsDTO);
        dto.setTotal(carrito.calcularTotal());
        dto.setCantidad_total_items(carrito.getCantidadTotalItems());

        return dto;
    }

    private ItemCarritoDTO convertirItemADTO(ItemCarrito item) {
        ItemCarritoDTO dto = new ItemCarritoDTO();
        dto.setId_item_carrito(item.getId_item_carrito());
        dto.setId_carrito(item.getCarrito().getId_carrito());
        dto.setId_producto(item.getProducto().getId_producto());
        dto.setNombre_producto(item.getProducto().getNombre());
        dto.setImagen_producto(item.getProducto().getImagen_producto());
        dto.setId_tienda(item.getProducto().getTienda().getId_tienda());
        dto.setNombre_tienda(item.getProducto().getTienda().getNombre());
        dto.setCantidad(item.getCantidad());
        dto.setPrecio_unitario(item.getPrecio_unitario());
        dto.setSubtotal(item.getSubtotal());
        dto.setStock_disponible(item.getProducto().getStock());
        dto.setStock_suficiente(item.stockDisponible());
        dto.setFecha_agregado(item.getFecha_agregado());

        return dto;
    }

    // ========== NUEVOS MÉTODOS REQUERIDOS ==========

    @Override
    @Transactional(readOnly = true)
    public List<ItemCarritoDTO> obtenerItemsCarrito(Long idUsuario) {
        CarritoCompra carrito = carritoRepository.findByUsuarioIdWithItems(idUsuario)
                .orElse(null);

        if (carrito == null) {
            return new ArrayList<>();
        }

        return carrito.getItems().stream()
                .map(this::convertirItemADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularTotal(Long idUsuario) {
        CarritoCompra carrito = carritoRepository.findByUsuarioIdWithItems(idUsuario)
                .orElse(null);

        if (carrito == null) {
            return 0.0;
        }

        return carrito.calcularTotal();
    }
}