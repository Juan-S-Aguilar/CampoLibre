package com.example.campolibre.Implement;

import com.example.campolibre.DTO.InscripcionProveedorDTO;
import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Entity.InscripcionProveedor;
import com.example.campolibre.Entity.PagoEvento;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Enum.EstadoCupo;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.EstadoPago;
import com.example.campolibre.Enum.NombreRol;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.EventoRepository;
import com.example.campolibre.Repository.InscripcionProveedorRepository;
import com.example.campolibre.Repository.PagoEventoRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Service.InscripcionProveedorService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InscripcionProveedorImplement implements InscripcionProveedorService {

    private final InscripcionProveedorRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    private  final PagoEventoRepository pagoEventoRepository;
    // Podríamos inyectar EmailService aquí si quisiéramos notificar al proveedor

    @Autowired
    public InscripcionProveedorImplement(InscripcionProveedorRepository inscripcionRepository,
                                         EventoRepository eventoRepository,
                                         UsuarioRepository usuarioRepository,
                                         ModelMapper modelMapper,
                                         PagoEventoRepository pagoEventoRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
        this.pagoEventoRepository = pagoEventoRepository;
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

        // Validar fecha del evento (no inscribirse a eventos pasados)
        if (evento.getFecha_evento().isBefore(LocalDate.now())) {
            throw new CustomException("No puedes inscribirte a un evento que ya pasó.");
        }

        // 4. Crear la inscripción PENDIENTE
        InscripcionProveedor inscripcion = new InscripcionProveedor();
        inscripcion.setProveedor(proveedor);
        inscripcion.setEvento(evento);
        inscripcion.setEstadoCupo(EstadoCupo.PENDIENTE);
        inscripcion.setCostoPagado(evento.getCostoEspacio());

        InscripcionProveedor nuevaInscripcion = inscripcionRepository.save(inscripcion);

        // ✅ Mapeo completo con datos de visualización
        return mapearConDatosVisualizacion(nuevaInscripcion);
    }

    /**
     * Lógica que se llama desde el Webhook o pasarela de pago para confirmar la transacción.
     */
    @Transactional
    @Override
    public InscripcionProveedorDTO confirmarPago(Long idInscripcion, Long idPagoEvento) {
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

        // 3. Buscar y validar el pago ✅ NUEVO
        PagoEvento pagoEvento = pagoEventoRepository.findById(idPagoEvento)
                .orElseThrow(() -> new CustomException("Pago no encontrado."));

        if (pagoEvento.getEstado() != EstadoPago.EXITOSO) {
            throw new CustomException("El pago no está en estado exitoso.");
        }

        // 4. Confirmar Cupo
        inscripcion.setEstadoCupo(EstadoCupo.CONFIRMADO);
        inscripcion.setPagoEvento(pagoEvento); // ✅ CONECTAR CON PAGO
        inscripcion.setFechaConfirmacion(LocalDateTime.now());

        // 5. Incrementar cupos ocupados del evento ✅ CRÍTICO
        // A. OBTENER EL EVENTO CON BLOQUEO
        Evento evento = eventoRepository.findByIdWithLock(inscripcion.getEvento().getId_evento())
                .orElseThrow(() -> new CustomException("Evento no encontrado (al intentar bloquear)."));

        // B. DOBLE VERIFICACIÓN (CRÍTICA DENTRO DE LA TRANSACCIÓN BLOQUEADA)
        if (evento.getCuposOcupados() >= evento.getCuposMaximosProveedor()) {
            // Liberar este cupo pendiente y lanzar excepción, ya que el cupo se agotó
            // Mantenemos la inscripcion en PENDIENTE o la marcamos CANCELADA si el tiempo de espera expiró.
            // Aquí simplificaremos con una excepción:
            throw new CustomException("¡Lo sentimos! El cupo se agotó durante el procesamiento del pago.");
        }

        // C. INCREMENTAR Y GUARDAR
        evento.setCuposOcupados(evento.getCuposOcupados() + 1);
        eventoRepository.save(evento); // Se guarda bajo el bloqueo

        // 6. Finalizar la Inscripción (se mantiene)
        inscripcion.setEstadoCupo(EstadoCupo.CONFIRMADO);
        inscripcion.setPagoEvento(pagoEvento);
        inscripcion.setFechaConfirmacion(LocalDateTime.now());
        InscripcionProveedor confirmada = inscripcionRepository.save(inscripcion);


        // TODO: Enviar correo al proveedor confirmando el cupo asegurado

        return mapearConDatosVisualizacion(confirmada);
    }

    @Override
    public boolean hayCuposDisponibles(Long idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new CustomException("Evento no encontrado."));

        // Usar el método de conteo y pasar el enum CONFIRMADO
        Long cuposOcupados = inscripcionRepository.countByEventoIdAndEstadoCupo(
                idEvento, EstadoCupo.CONFIRMADO
        );
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
                        i.getEstadoCupo() == EstadoCupo.PENDIENTE)
                .orElse(false);
    }

    @Override
    public InscripcionProveedorDTO obtenerInscripcionPorUsuarioYEvento(Long idProveedor, Long idEvento) {
        // 1. Buscar la inscripción usando el método del Repositorio que ya existe.
        // Retorna un Optional, que puede estar vacío.
        return inscripcionRepository
                .findByProveedorIdUsuarioAndEventoId(idProveedor, idEvento)
                .map(this::mapearConDatosVisualizacion) // 2. Si existe (map), la mapeamos a DTO
                .orElse(null); // 3. Si no existe, devolvemos NULL, tal como espera el Controlador.
    }

    // ✅ Método helper para mapeo completo (ya lo tenías bien)
    private InscripcionProveedorDTO mapearConDatosVisualizacion(InscripcionProveedor inscripcion) {
        InscripcionProveedorDTO dto = modelMapper.map(inscripcion, InscripcionProveedorDTO.class);

        // Datos del proveedor
        if (inscripcion.getProveedor() != null) {
            dto.setId_proveedor(inscripcion.getProveedor().getId_usuario()); // ⬅️ AGREGADO
            dto.setNombreProveedor(inscripcion.getProveedor().getNombre());
        }

        // Datos del evento
        if (inscripcion.getEvento() != null) {
            dto.setId_evento(inscripcion.getEvento().getId_evento()); // ⬅️ AGREGADO
            dto.setNombreEvento(inscripcion.getEvento().getNombre());
            dto.setFechaEvento(inscripcion.getEvento().getFecha_evento());
            dto.setUbicacionEvento(inscripcion.getEvento().getUbicacion());
            dto.setImagenEvento(inscripcion.getEvento().getImagen_evento()); // ⬅️ OPCIONAL (útil para la vista)
        }

        // Datos del pago (si existe)
        if (inscripcion.getPagoEvento() != null) {
            dto.setId_pago_evento(inscripcion.getPagoEvento().getIdPagoEvento()); // ⬅️ AGREGADO
        }

        return dto;
    }
    // ✅ AGREGAR: Cancelar inscripción
    @Override
    public void cancelarInscripcion(Long idInscripcion, String motivo) {
        InscripcionProveedor inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new CustomException("Inscripción no encontrada."));

        // Solo se puede cancelar si está PENDIENTE o CONFIRMADO
        if (inscripcion.getEstadoCupo() == EstadoCupo.CANCELADO) {
            throw new CustomException("La inscripción ya está cancelada.");
        }

        // Si estaba confirmado, liberar el cupo
        if (inscripcion.getEstadoCupo() == EstadoCupo.CONFIRMADO) {
            Evento evento = inscripcion.getEvento();
            evento.setCuposOcupados(evento.getCuposOcupados() - 1);
            eventoRepository.save(evento);
        }

        inscripcion.setEstadoCupo(EstadoCupo.CANCELADO);
        inscripcionRepository.save(inscripcion);

        // TODO: Enviar email al proveedor notificando cancelación
        System.out.println("[InscripcionProveedor] Inscripción cancelada. Motivo: " + motivo);
    }

    // ✅ AGREGAR: Obtener cupos disponibles (número)
    @Override
    public Integer obtenerCuposDisponibles(Long idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new CustomException("Evento no encontrado."));

        Long cuposOcupados = inscripcionRepository.countByEventoIdAndEstadoCupo(
                idEvento, EstadoCupo.CONFIRMADO
        );

        return evento.getCuposMaximosProveedor() - cuposOcupados.intValue();
    }

    // ✅ AGREGAR: Todas las inscripciones del proveedor (no solo confirmadas)
    @Override
    public List<InscripcionProveedorDTO> obtenerTodasLasInscripcionesDeProveedor(Long idProveedor) {
        return inscripcionRepository.findByProveedorId(idProveedor).stream()
                .map(this::mapearConDatosVisualizacion)
                .collect(Collectors.toList());
    }

    // ✅ AGREGAR: Buscar por código de confirmación
    @Override
    public InscripcionProveedorDTO obtenerInscripcionPorCodigo(String codigoConfirmacion) {
        InscripcionProveedor inscripcion = inscripcionRepository.findByCodigoConfirmacion(codigoConfirmacion)
                .orElseThrow(() -> new CustomException("Código de confirmación inválido."));
        return mapearConDatosVisualizacion(inscripcion);
    }

}