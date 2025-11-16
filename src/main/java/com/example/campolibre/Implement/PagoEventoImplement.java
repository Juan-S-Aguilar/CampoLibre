package com.example.campolibre.Implement;

import com.example.campolibre.DTO.PagoEventoDTO;
import com.example.campolibre.DTO.PagoEventoCreacionDTO;
import com.example.campolibre.DTO.PagoEventoEstadisticasDTO;
import com.example.campolibre.Entity.InscripcionProveedor;
import com.example.campolibre.Entity.PagoEvento;
import com.example.campolibre.Enum.EstadoCupo;
import com.example.campolibre.Enum.EstadoPago;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.InscripcionProveedorRepository;
import com.example.campolibre.Repository.PagoEventoRepository;
import com.example.campolibre.Service.PagoEventoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.campolibre.Service.EmailService;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PagoEventoImplement implements PagoEventoService {

    private final PagoEventoRepository pagoEventoRepository;
    private final InscripcionProveedorRepository inscripcionProveedorRepository;
    private final ModelMapper modelMapper;
    private  final EmailService emailService;
    // Si tienes EmailService, inyéctalo para notificaciones

    @Autowired
    public PagoEventoImplement(PagoEventoRepository pagoEventoRepository,
                               InscripcionProveedorRepository inscripcionProveedorRepository,
                               ModelMapper modelMapper,
                               EmailService emailService
                               ) {
        this.pagoEventoRepository = pagoEventoRepository;
        this.inscripcionProveedorRepository = inscripcionProveedorRepository;
        this.modelMapper = modelMapper;

        this.emailService = emailService;
    }

    /**
     * Crea un nuevo pago para una inscripción.
     * Estado inicial: PENDIENTE
     */
    @Override
    @Transactional
    public PagoEventoDTO crearPago(PagoEventoCreacionDTO pagoCreacionDTO) {
        // 1. Buscar la inscripción
        InscripcionProveedor inscripcion = inscripcionProveedorRepository.findById(pagoCreacionDTO.getIdInscripcion())
                .orElseThrow(() -> new CustomException("Inscripción no encontrada."));

        // 2. Validar que la inscripción esté en estado PENDIENTE_PAGO
        if (inscripcion.getEstadoCupo() != EstadoCupo.PENDIENTE_PAGO) {
            throw new CustomException("La inscripción no está en estado pendiente de pago. Estado actual: "
                    + inscripcion.getEstadoCupo());
        }

        // 3. Validar que no exista ya un pago para esta inscripción
        pagoEventoRepository.findByInscripcionId(inscripcion.getId_inscripcion())
                .ifPresent(p -> {
                    throw new CustomException("Ya existe un pago asociado a esta inscripción.");
                });

        // 4. Crear el pago
        PagoEvento pagoEvento = new PagoEvento();
        pagoEvento.setInscripcionProveedor(inscripcion);
        pagoEvento.setMonto(inscripcion.getCostoPagado());
        pagoEvento.setMetodoPago(pagoCreacionDTO.getMetodoPago());
        pagoEvento.setEstado(EstadoPago.PENDIENTE);
        // El número de transacción se genera automáticamente en @PrePersist

        PagoEvento nuevoPago = pagoEventoRepository.save(pagoEvento);

        System.out.println("[PagoEventoImplement] Pago creado: " + nuevoPago.getNumeroTransaccion()
                + " - Monto: $" + nuevoPago.getMonto());

        return convertirADTO(nuevoPago);
    }

    /**
     * Procesa el pago (simulación o integración con pasarela real).
     * En producción, aquí se llamaría a la API de la pasarela de pagos.
     */
    @Override
    @Transactional
    public PagoEventoDTO procesarPago(Long idPagoEvento) {
        PagoEvento pagoEvento = pagoEventoRepository.findById(idPagoEvento)
                .orElseThrow(() -> new CustomException("Pago no encontrado."));

        // Validar estado
        if (pagoEvento.getEstado() != EstadoPago.PENDIENTE) {
            throw new CustomException("El pago no está en estado pendiente. Estado actual: "
                    + pagoEvento.getEstado());
        }

        try {
            // ========================================
            // AQUÍ VA LA INTEGRACIÓN CON PASARELA REAL
            // Ejemplo: llamar API de Wompi, PayU, Stripe, etc.
            // ========================================

            // SIMULACIÓN: Asumimos que el pago fue exitoso
            boolean pagoExitoso = simularPasarelaDePagos(pagoEvento);

            if (pagoExitoso) {
                return marcarPagoExitoso(idPagoEvento);
            } else {
                return marcarPagoFallido(idPagoEvento, "Error en la pasarela de pagos (simulado)");
            }

        } catch (Exception e) {
            System.err.println("[PagoEventoImplement] Error al procesar pago: " + e.getMessage());
            return marcarPagoFallido(idPagoEvento, "Error interno: " + e.getMessage());
        }
    }

    /**
     * Marca un pago como exitoso.
     * Llamado desde webhook de pasarela o desde procesarPago().
     */
    @Override
    @Transactional
    public PagoEventoDTO marcarPagoExitoso(Long idPagoEvento) {
        PagoEvento pagoEvento = pagoEventoRepository.findById(idPagoEvento)
                .orElseThrow(() -> new CustomException("Pago no encontrado."));

        // Marcar pago como exitoso
        pagoEvento.marcarExitoso();
        PagoEvento pagoActualizado = pagoEventoRepository.save(pagoEvento);

        // Actualizar inscripción a CONFIRMADO
        InscripcionProveedor inscripcion = pagoEvento.getInscripcionProveedor();
        inscripcion.setEstadoCupo(EstadoCupo.CONFIRMADO);
        inscripcion.setPagoEvento(pagoEvento);

        // Incrementar cupos ocupados del evento
        inscripcion.getEvento().setCuposOcupados(
                inscripcion.getEvento().getCuposOcupados() + 1
        );

        inscripcionProveedorRepository.save(inscripcion);

        System.out.println("[PagoEventoImplement] Pago exitoso: " + pagoEvento.getNumeroTransaccion()
                + " - Cupo confirmado para proveedor: " + inscripcion.getProveedor().getNombre());

        // ✅ ENVIAR EMAIL DE CONFIRMACIÓN
        try {
            emailService.enviarConfirmacionPago(
                    inscripcion.getProveedor().getEmail(),
                    inscripcion.getProveedor().getNombre(),
                    inscripcion.getEvento().getNombre(),
                    inscripcion.getCodigoConfirmacion(),
                    pagoEvento.getNumeroTransaccion(),
                    pagoEvento.getMonto()
            );
        } catch (Exception e) {
            System.err.println("[PagoEventoImplement] Error al enviar email de confirmación: " + e.getMessage());
            // No fallar la operación si el email falla
        }

        return convertirADTO(pagoActualizado);
    }

    /**
     * Marca un pago como fallido.
     */
    @Override
    @Transactional
    public PagoEventoDTO marcarPagoFallido(Long idPagoEvento, String mensajeError) {
        PagoEvento pagoEvento = pagoEventoRepository.findById(idPagoEvento)
                .orElseThrow(() -> new CustomException("Pago no encontrado."));

        // Marcar pago como fallido
        pagoEvento.marcarFallido(mensajeError);
        PagoEvento pagoActualizado = pagoEventoRepository.save(pagoEvento);

        // Actualizar inscripción a CANCELADO
        InscripcionProveedor inscripcion = pagoEvento.getInscripcionProveedor();
        inscripcion.setEstadoCupo(EstadoCupo.CANCELADO);
        inscripcionProveedorRepository.save(inscripcion);

        System.err.println("[PagoEventoImplement] Pago fallido: " + pagoEvento.getNumeroTransaccion()
                + " - Error: " + mensajeError);

        // ✅ ENVIAR EMAIL DE NOTIFICACIÓN DE FALLO
        try {
            emailService.enviarNotificacionPagoFallido(
                    inscripcion.getProveedor().getEmail(),
                    inscripcion.getProveedor().getNombre(),
                    inscripcion.getEvento().getNombre(),
                    pagoEvento.getNumeroTransaccion(),
                    mensajeError
            );
        } catch (Exception e) {
            System.err.println("[PagoEventoImplement] Error al enviar email de fallo: " + e.getMessage());
        }

        return convertirADTO(pagoActualizado);
    }

    @Override
    public PagoEventoDTO obtenerPagoPorId(Long idPagoEvento) {
        PagoEvento pagoEvento = pagoEventoRepository.findById(idPagoEvento)
                .orElseThrow(() -> new CustomException("Pago no encontrado."));
        return convertirADTO(pagoEvento);
    }

    @Override
    public PagoEventoDTO obtenerPagoPorNumeroTransaccion(String numeroTransaccion) {
        PagoEvento pagoEvento = pagoEventoRepository.findByNumeroTransaccion(numeroTransaccion)
                .orElseThrow(() -> new CustomException("Pago no encontrado con ese número de transacción."));
        return convertirADTO(pagoEvento);
    }

    @Override
    public List<PagoEventoDTO> obtenerPagosPorProveedor(Long idProveedor) {
        return pagoEventoRepository.findPagosByProveedorId(idProveedor).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PagoEventoDTO> obtenerPagosPorEvento(Long idEvento) {
        // Esta query navega por InscripcionProveedor.evento
        return pagoEventoRepository.findAll().stream()
                .filter(p -> p.getInscripcionProveedor().getEvento().getId_evento().equals(idEvento))
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PagoEventoDTO> obtenerPagosPorEstado(EstadoPago estado) {
        return pagoEventoRepository.findByEstado(estado).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public Double calcularTotalRecaudadoPorEvento(Long idEvento) {
        return pagoEventoRepository.calcularTotalRecaudadoPorEvento(idEvento);
    }

    @Override
    public PagoEventoEstadisticasDTO obtenerEstadisticasPagosEvento(Long idEvento) {
        List<PagoEvento> todosPagos = pagoEventoRepository.findAll().stream()
                .filter(p -> p.getInscripcionProveedor().getEvento().getId_evento().equals(idEvento))
                .collect(Collectors.toList());

        long totalPagos = todosPagos.size();
        long pagosExitosos = todosPagos.stream().filter(p -> p.getEstado() == EstadoPago.EXITOSO).count();
        long pagosPendientes = todosPagos.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE).count();
        long pagosFallidos = todosPagos.stream().filter(p -> p.getEstado() == EstadoPago.FALLIDO).count();

        Double totalRecaudado = calcularTotalRecaudadoPorEvento(idEvento);
        Double montoPromedio = pagosExitosos > 0 ? totalRecaudado / pagosExitosos : 0.0;

        return new PagoEventoEstadisticasDTO(
                totalPagos,
                pagosExitosos,
                pagosPendientes,
                pagosFallidos,
                totalRecaudado,
                montoPromedio
        );
    }

    // ========================================
    // MÉTODOS HELPER PRIVADOS
    // ========================================

    /**
     * Simulación de pasarela de pagos.
     * En producción, reemplazar con llamada a API real.
     */
    private boolean simularPasarelaDePagos(PagoEvento pagoEvento) {
        System.out.println("[PagoEventoImplement] Simulando pago en pasarela...");

        // Simulación: 90% de pagos exitosos
        double random = Math.random();
        boolean exitoso = random > 0.1;

        System.out.println("[PagoEventoImplement] Resultado: " + (exitoso ? "EXITOSO" : "FALLIDO"));
        return exitoso;
    }

    /**
     * Convierte PagoEvento a DTO con datos de visualización.
     */
    private PagoEventoDTO convertirADTO(PagoEvento pagoEvento) {
        PagoEventoDTO dto = modelMapper.map(pagoEvento, PagoEventoDTO.class);

        // ID de la inscripción
        if (pagoEvento.getInscripcionProveedor() != null) {
            dto.setIdInscripcion(pagoEvento.getInscripcionProveedor().getId_inscripcion());

            // Nombre del proveedor
            if (pagoEvento.getInscripcionProveedor().getProveedor() != null) {
                dto.setNombreProveedor(pagoEvento.getInscripcionProveedor().getProveedor().getNombre());
            }

            // Nombre del evento
            if (pagoEvento.getInscripcionProveedor().getEvento() != null) {
                dto.setNombreEvento(pagoEvento.getInscripcionProveedor().getEvento().getNombre());
            }

            // Código de confirmación
            dto.setCodigoConfirmacion(pagoEvento.getInscripcionProveedor().getCodigoConfirmacion());
        }

        return dto;
    }
}