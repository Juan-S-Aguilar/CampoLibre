package com.example.campolibre.Implement;

import com.example.campolibre.DTO.InscripcionProveedorDTO;
import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Entity.InscripcionProveedor;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Enum.EstadoCupo;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.NombreRol;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.EventoRepository;
import com.example.campolibre.Repository.InscripcionProveedorRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Service.InscripcionProveedorService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InscripcionProveedorImplement implements InscripcionProveedorService {

    private final InscripcionProveedorRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    // Podríamos inyectar EmailService aquí si quisiéramos notificar al proveedor

    @Autowired
    public InscripcionProveedorImplement(InscripcionProveedorRepository inscripcionRepository,
                                         EventoRepository eventoRepository,
                                         UsuarioRepository usuarioRepository,
                                         ModelMapper modelMapper) {
        this.inscripcionRepository = inscripcionRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Lógica para que el Proveedor reserve un cupo.
     * El estado inicial es PENDIENTE_PAGO.
     */
    @Override
    public InscripcionProveedorDTO solicitarCupo(Long idProveedor, Long idEvento) {
        // 1. Verificar existencia de Entidades
        Usuario proveedor = usuarioRepository.findById(idProveedor)
                .orElseThrow(() -> new CustomException("Proveedor no encontrado."));

        // ✅ Validar que sea proveedor (con Set<Rol>)
        boolean esProveedor = proveedor.getRoles().stream()
                .anyMatch(rol -> rol.getNombre_rol().equals(NombreRol.PROVEEDOR));

        if (!esProveedor) {
            throw new CustomException("Solo los proveedores pueden inscribirse a eventos.");
        }

        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new CustomException("Evento no encontrado."));

        // 2. Validar que el evento esté PUBLICADO
        if (evento.getEstado() != EstadoEvento.PUBLICADO) {
            throw new CustomException("El evento no está disponible para inscripciones. Estado actual: " + evento.getEstado());
        }

        if (!hayCuposDisponibles(idEvento)) {
            throw new CustomException("Lo sentimos, no quedan cupos disponibles para proveedores en este evento.");
        }

        // 3. Validar que el proveedor no esté ya inscrito
        if (inscripcionRepository.findByProveedorIdUsuarioAndEventoId(idProveedor, idEvento).isPresent()) {
            throw new CustomException("Ya tienes una inscripción (pendiente o confirmada) para este evento.");
        }

        // 4. Crear la inscripción PENDIENTE_PAGO
        InscripcionProveedor inscripcion = new InscripcionProveedor();
        inscripcion.setProveedor(proveedor);
        inscripcion.setEvento(evento);
        inscripcion.setEstadoCupo(EstadoCupo.PENDIENTE_PAGO);
        inscripcion.setCostoPagado(evento.getCostoEspacio());

        InscripcionProveedor nuevaInscripcion = inscripcionRepository.save(inscripcion);

        // ✅ Mapeo completo con datos de visualización
        return mapearConDatosVisualizacion(nuevaInscripcion);
    }

    /**
     * Lógica que se llama desde el Webhook o pasarela de pago para confirmar la transacción.
     */
    @Override
    public InscripcionProveedorDTO confirmarPago(Long idInscripcion, Long idPagoSistemaExterno) {
        // 1. Buscar la inscripción
        InscripcionProveedor inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new CustomException("Inscripción no encontrada."));

        // 2. Validar el estado actual
        if (inscripcion.getEstadoCupo() == EstadoCupo.CONFIRMADO) {
            throw new CustomException("Esta inscripción ya ha sido confirmada.");
        }
        if (inscripcion.getEstadoCupo() == EstadoCupo.CANCELADO) {
            throw new CustomException("Esta inscripción está cancelada y no puede ser reactivada.");
        }

        // 3. Realizar la verificación del pago (Lógica externa, aquí se simula)
        // Lógica: Se debe usar idPagoSistemaExterno para consultar la tabla 'Pagos'
        // y validar que el monto, evento e inscripción coincidan.
        // Asumimos que la tabla 'Pago' existe y tiene un repositorio para ser inyectado.

        // 4. Confirmar Cupo
        inscripcion.setEstadoCupo(EstadoCupo.CONFIRMADO);
        // Si tienes una entidad Pago, la asignas aquí: inscripcion.setPago(pagoEncontrado);

        InscripcionProveedor confirmada = inscripcionRepository.save(inscripcion);

        // Notificación: Enviar correo al proveedor confirmando el cupo asegurado.

        // ✅ CAMBIO: Usar el método helper en lugar de mapeo simple
        return mapearConDatosVisualizacion(confirmada);
    }

    @Override
    public boolean hayCuposDisponibles(Long idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new CustomException("Evento no encontrado."));

        // Usar el método de conteo y pasar el enum CONFIRMADO
        Long cuposConfirmados = inscripcionRepository.countByEventoIdAndEstadoCupo(
                idEvento, EstadoCupo.CONFIRMADO
        );

        // Incluir cupos PENDIENTES_PAGO en el conteo para prevenir sobreventa
        Long cuposPendientes = inscripcionRepository.countByEventoIdAndEstadoCupo(
                idEvento, EstadoCupo.PENDIENTE_PAGO
        );

        Long cuposOcupados = cuposConfirmados + cuposPendientes;

        return cuposOcupados < evento.getCuposMaximosProveedor();
    }

    @Override
    public List<InscripcionProveedorDTO> obtenerEventosConfirmadosDeProveedor(Long idProveedor) {
        return inscripcionRepository.findByProveedorId(idProveedor).stream()
                .filter(i -> i.getEstadoCupo() == EstadoCupo.CONFIRMADO)
                // ✅ CAMBIO: Usar el método helper
                .map(this::mapearConDatosVisualizacion)
                .collect(Collectors.toList());
    }

    @Override
    public List<InscripcionProveedorDTO> obtenerInscripcionesPorEvento(Long idEvento) {
        // Devuelve todas las inscripciones (PENDIENTE, CONFIRMADO, CANCELADO) para trazabilidad
        return inscripcionRepository.findByEventoId(idEvento).stream()
                // ✅ CAMBIO: Usar el método helper
                .map(this::mapearConDatosVisualizacion)
                .collect(Collectors.toList());
    }

    @Override
    public InscripcionProveedorDTO obtenerInscripcionPorId(Long idInscripcion) {
        InscripcionProveedor inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new CustomException("Inscripción no encontrada."));
        // ✅ CAMBIO: Usar el método helper
        return mapearConDatosVisualizacion(inscripcion);
    }

    @Override
    public boolean proveedorEstaInscrito(Long idProveedor, Long idEvento) {
        return inscripcionRepository.findByProveedorIdUsuarioAndEventoId(idProveedor, idEvento)
                .map(i -> i.getEstadoCupo() == EstadoCupo.CONFIRMADO ||
                        i.getEstadoCupo() == EstadoCupo.PENDIENTE_PAGO)
                .orElse(false);
    }

    // ✅ Método helper para mapeo completo (ya lo tenías bien)
    private InscripcionProveedorDTO mapearConDatosVisualizacion(InscripcionProveedor inscripcion) {
        InscripcionProveedorDTO dto = modelMapper.map(inscripcion, InscripcionProveedorDTO.class);

        // Datos del proveedor
        if (inscripcion.getProveedor() != null) {
            dto.setNombreProveedor(inscripcion.getProveedor().getNombre());
        }

        // Datos del evento
        if (inscripcion.getEvento() != null) {
            dto.setNombreEvento(inscripcion.getEvento().getNombre());
            dto.setFechaEvento(inscripcion.getEvento().getFecha_evento());
            dto.setUbicacionEvento(inscripcion.getEvento().getUbicacion());
        }

        return dto;
    }
}