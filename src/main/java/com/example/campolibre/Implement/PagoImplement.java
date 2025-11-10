package com.example.campolibre.Implement;

import com.example.campolibre.DTO.PagoDTO;
import com.example.campolibre.Entity.ItemPedido;
import com.example.campolibre.Entity.Pago;
import com.example.campolibre.Entity.Pedido;
import com.example.campolibre.Entity.Producto;
import com.example.campolibre.Enum.EstadoPago;
import com.example.campolibre.Enum.EstadoPedido;
import com.example.campolibre.Enum.MetodoPago;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.CarritoCompraRepository;
import com.example.campolibre.Repository.PagoRepository;
import com.example.campolibre.Repository.PedidoRepository;
import com.example.campolibre.Repository.ProductoRepository;
import com.example.campolibre.Service.CarritoService;
import com.example.campolibre.Service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class PagoImplement implements PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoService carritoService;

    private final Random random = new Random();

    @Override
    @Transactional
    public PagoDTO procesarPago(Long idPedido, MetodoPago metodoPago) {
        // 1. Validar pedido
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new CustomException("Pedido no encontrado"));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
            throw new CustomException("El pedido no está en estado pendiente de pago");
        }

        // 2. Validar stock una última vez antes de procesar el pago
        StringBuilder erroresStock = new StringBuilder();
        for (ItemPedido item : pedido.getItems()) {
            Producto producto = productoRepository.findById(item.getProducto().getId_producto())
                    .orElseThrow(() -> new CustomException("Producto no encontrado"));

            if (producto.getStock() < item.getCantidad()) {
                erroresStock.append("- El producto '")
                        .append(producto.getNombre())
                        .append("' solo tiene ")
                        .append(producto.getStock())
                        .append(" unidades disponibles\n");
            }
        }

        if (erroresStock.length() > 0) {
            throw new CustomException("Stock insuficiente:\n" + erroresStock.toString());
        }

        // 3. Crear registro de pago (pendiente)
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMonto(pedido.getTotal());
        pago.setMetodo_pago(metodoPago);
        pago.setEstado(EstadoPago.PENDIENTE);

        // Asociar en memoria el pago al pedido para mantener consistencia antes de guardar
        pedido.setPago(pago);

        // 4. Simular procesamiento de pasarela
        boolean pagoExitoso = simularPasarelaPago(metodoPago, pedido.getTotal());

        if (pagoExitoso) {
            // ✅ PAGO EXITOSO (tentativo)
            pago.marcarExitoso();
            pedido.marcarComoPagado();

            // ✅ DESCONTAR STOCK DE LOS PRODUCTOS - usando la query atómica
            for (ItemPedido item : pedido.getItems()) {
                int updated = productoRepository.descontarStock(item.getProducto().getId_producto(), item.getCantidad());
                if (updated == 0) {
                    // No se pudo descontar (otro proceso pudo consumir el stock). Forzamos rollback.
                    throw new CustomException("No hay stock suficiente para el producto: " + item.getProducto().getNombre());
                }
            }

            // ✅ VACIAR CARRITO DEL USUARIO
            carritoService.vaciarCarrito(pedido.getUsuario().getId_usuario());

            System.out.println("✅ Pago exitoso - Pedido: " + pedido.getNumero_pedido());
        } else {
            // ❌ PAGO FALLIDO
            pago.marcarFallido("Transacción rechazada por la entidad financiera");
            pedido.cancelar();

            System.out.println("❌ Pago fallido - Pedido: " + pedido.getNumero_pedido());
        }

        // 5. Guardar cambios (si ocurrió excepción antes, la transacción hace rollback)
        Pago pagoGuardado = pagoRepository.save(pago);
        pedidoRepository.save(pedido);

        return convertirADTO(pagoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoDTO obtenerPagoPorPedido(Long idPedido) {
        Pago pago = pagoRepository.findByPedidoId(idPedido)
                .orElseThrow(() -> new CustomException("Pago no encontrado"));
        return convertirADTO(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoDTO obtenerPagoPorNumeroTransaccion(String numeroTransaccion) {
        Pago pago = pagoRepository.findByNumeroTransaccion(numeroTransaccion)
                .orElseThrow(() -> new CustomException("Pago no encontrado"));
        return convertirADTO(pago);
    }

    @Override
    public boolean simularPasarelaPago(MetodoPago metodoPago, Double monto) {
        try {
            Thread.sleep(1000 + random.nextInt(2000)); // simula 1-3s
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return random.nextInt(100) < 95; // 95% éxito
    }

    // ========== MÉTODOS PRIVADOS HELPER ==========

    private PagoDTO convertirADTO(Pago pago) {
        PagoDTO dto = new PagoDTO();
        dto.setId_pago(pago.getId_pago());
        dto.setId_pedido(pago.getPedido().getId_pedido());
        dto.setMonto(pago.getMonto());
        dto.setMetodo_pago(pago.getMetodo_pago());
        dto.setEstado(pago.getEstado());
        dto.setNumero_transaccion(pago.getNumero_transaccion());
        dto.setFecha_pago(pago.getFecha_pago());
        dto.setMensaje_error(pago.getMensaje_error());
        return dto;
    }
}