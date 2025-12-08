package com.example.campolibre.Implement;

import com.example.campolibre.DTO.ConfirmarPedidoRequest;
import com.example.campolibre.DTO.ItemPedidoDTO;
import com.example.campolibre.DTO.PedidoDTO;
import com.example.campolibre.Entity.*;
import com.example.campolibre.Enum.EstadoPedido;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.*;
import com.example.campolibre.Service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PedidoImplement implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private CarritoCompraRepository carritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private TiendaRepository tiendaRepository;

    @Override
    @Transactional
    public PedidoDTO crearPedidoDesdeCarrito(Long idUsuario, ConfirmarPedidoRequest datosEntrega) {
        // 1-6: validaciones y creación del pedido (idéntico a tu implementación actual)
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        CarritoCompra carrito = carritoRepository.findByUsuarioIdWithItems(idUsuario)
                .orElseThrow(() -> new CustomException("Carrito no encontrado"));

        if (carrito.getItems().isEmpty()) {
            throw new CustomException("El carrito está vacío");
        }

        StringBuilder erroresStock = new StringBuilder();
        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = item.getProducto();

            if (!producto.getEstado().equals("ACTIVO")) {
                erroresStock.append("- El producto '")
                        .append(producto.getNombre())
                        .append("' ya no está disponible\n");
            }

            if (producto.getStock() < item.getCantidad()) {
                erroresStock.append("- El producto '")
                        .append(producto.getNombre())
                        .append("' solo tiene ")
                        .append(producto.getStock())
                        .append(" unidades disponibles (necesitas ")
                        .append(item.getCantidad())
                        .append(")\n");
            }
        }

        if (erroresStock.length() > 0) {
            throw new CustomException("Stock insuficiente:\n" + erroresStock.toString());
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        pedido.setNombre_contacto(datosEntrega.getNombre_contacto());
        pedido.setTelefono_contacto(datosEntrega.getTelefono_contacto());
        pedido.setDireccion_entrega(datosEntrega.getDireccion_entrega());
        pedido.setCiudad(datosEntrega.getCiudad());
        pedido.setNotas_adicionales(datosEntrega.getNotas_adicionales());

        List<ItemPedido> itemsPedido = new ArrayList<>();
        for (ItemCarrito itemCarrito : carrito.getItems()) {
            ItemPedido itemPedido = ItemPedido.crearDesdeItemCarrito(itemCarrito, pedido);
            itemsPedido.add(itemPedido);
        }
        pedido.setItems(itemsPedido);

        pedido.calcularTotal();

        // ---------- GENERAR NÚMERO Y GUARDAR CON REINTENTOS ----------
        final int MAX_INTENTOS = 5;
        int intento = 0;
        Pedido pedidoGuardado = null;
        while (intento < MAX_INTENTOS) {
            intento++;
            String numeroPedido = generarNumeroPedido();
            pedido.setNumero_pedido(numeroPedido);
            try {
                pedidoGuardado = pedidoRepository.save(pedido);
                // Si save OK, salimos del loop
                break;
            } catch (DataIntegrityViolationException ex) {
                // Probable duplicado del numero_pedido por concurrencia: reintentar
                System.err.println("⚠️ Duplicado numero_pedido detectado. Reintentando... intento=" + intento);
                if (intento >= MAX_INTENTOS) {
                    throw new CustomException("No se pudo generar un número de pedido único. Intenta de nuevo.");
                }
                // el loop volverá a generar un nuevo número y reintentar
            }
        }

        if (pedidoGuardado == null) {
            throw new CustomException("Error al guardar el pedido");
        }

        return convertirADTO(pedidoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDTO obtenerPedidoPorId(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new CustomException("Pedido no encontrado"));
        return convertirADTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDTO obtenerPedidoPorNumero(String numeroPedido) {
        Pedido pedido = pedidoRepository.findByNumeroPedido(numeroPedido);
        if (pedido == null) {
            throw new CustomException("Pedido no encontrado");
        }
        return convertirADTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> obtenerPedidosPorUsuario(Long idUsuario) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioId(idUsuario);
        return pedidos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> obtenerPedidosPorEstado(EstadoPedido estado) {
        List<Pedido> pedidos = pedidoRepository.findByEstado(estado);
        return pedidos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> obtenerPedidosPorTienda(Long idTienda) {
        List<Pedido> pedidos = pedidoRepository.findPedidosConProductosDeTienda(idTienda);
        return pedidos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> obtenerPedidosPagadosPorTienda(Long idTienda) {
        List<Pedido> pedidos = pedidoRepository.findPedidosPagadosDeTienda(idTienda);
        return pedidos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelarPedido(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new CustomException("Pedido no encontrado"));

        if (pedido.getEstado() == EstadoPedido.PAGADO) {
            throw new CustomException("No se puede cancelar un pedido que ya fue pagado");
        }

        pedido.cancelar();
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularGananciasTienda(Long idTienda) {
        Double total = itemPedidoRepository.calcularTotalVentasPorTienda(idTienda);
        return total != null ? total : 0.0;
    }

    @Override
    @Transactional
    public String generarNumeroPedido() {
        int year = LocalDateTime.now().getYear();
        String patron = "PED-" + year + "-%";

        Long cantidad = pedidoRepository.countByNumeroPedidoPattern(patron);
        int siguienteNumero = (cantidad != null ? cantidad.intValue() : 0) + 1;

        return String.format("PED-%d-%05d", year, siguienteNumero);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> obtenerPedidosPorProveedor(Long idUsuarioProveedor) {
        // Obtener todas las tiendas del proveedor
        List<Tienda> tiendas = tiendaRepository.findByUsuarioId(idUsuarioProveedor);

        Set<Long> pedidosIds = new HashSet<>();
        List<Pedido> todosPedidos = new ArrayList<>();

        // Para cada tienda, obtener pedidos
        for (Tienda tienda : tiendas) {
            List<Pedido> pedidosTienda = pedidoRepository.findPedidosConProductosDeTienda(tienda.getId_tienda());
            for (Pedido p : pedidosTienda) {
                if (!pedidosIds.contains(p.getId_pedido())) {
                    pedidosIds.add(p.getId_pedido());
                    todosPedidos.add(p);
                }
            }
        }

        // Ordenar por fecha descendente
        todosPedidos.sort((p1, p2) -> p2.getFecha_pedido().compareTo(p1.getFecha_pedido()));

        return todosPedidos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // ========== MÉTODOS PRIVADOS HELPER ==========

    private PedidoDTO convertirADTO(Pedido pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setId_pedido(pedido.getId_pedido());
        dto.setNumero_pedido(pedido.getNumero_pedido());
        dto.setId_usuario(pedido.getUsuario().getId_usuario());
        dto.setNombre_usuario(pedido.getUsuario().getNombre());
        dto.setEstado(pedido.getEstado());
        dto.setTotal(pedido.getTotal());
        dto.setFecha_pedido(pedido.getFecha_pedido());
        dto.setFecha_pago(pedido.getFecha_pago());
        dto.setNombre_contacto(pedido.getNombre_contacto());
        dto.setTelefono_contacto(pedido.getTelefono_contacto());
        dto.setDireccion_entrega(pedido.getDireccion_entrega());
        dto.setCiudad(pedido.getCiudad());
        dto.setNotas_adicionales(pedido.getNotas_adicionales());

        List<ItemPedidoDTO> itemsDTO = pedido.getItems().stream()
                .map(this::convertirItemADTO)
                .collect(Collectors.toList());
        dto.setItems(itemsDTO);

        return dto;
    }

    private ItemPedidoDTO convertirItemADTO(ItemPedido item) {
        ItemPedidoDTO dto = new ItemPedidoDTO();
        dto.setId_item_pedido(item.getId_item_pedido());
        dto.setId_pedido(item.getPedido().getId_pedido());
        dto.setId_producto(item.getProducto().getId_producto());
        dto.setNombre_producto(item.getProducto().getNombre());
        dto.setImagen_producto(item.getProducto().getImagen_producto());
        dto.setId_tienda(item.getTienda().getId_tienda());
        dto.setNombre_tienda(item.getTienda().getNombre());
        dto.setCantidad(item.getCantidad());
        dto.setPrecio_unitario(item.getPrecio_unitario());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}