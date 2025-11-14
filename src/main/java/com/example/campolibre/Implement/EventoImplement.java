package com.example.campolibre.Implement;

import com.example.campolibre.DTO.EventoDTO;
import com.example.campolibre.DTO.MisEventosDTO;
import com.example.campolibre.Entity.Evento;
import com.example.campolibre.Entity.MisEventos;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Enum.EstadoCupo;
import com.example.campolibre.Enum.EstadoEvento;
import com.example.campolibre.Enum.NombreRol;
import com.example.campolibre.Enum.TipoEvento;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.EventoRepository;
import com.example.campolibre.Repository.InscripcionProveedorRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Service.EmailService;
import com.example.campolibre.Service.EventoService;
import com.example.campolibre.Service.FileStorageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;



import com.example.campolibre.DTO.EventoCreacionDTO; // Importar el nuevo DTO
import com.example.campolibre.Entity.Patrocinador; // Importar la nueva entidad Patrocinador
import com.example.campolibre.Repository.PatrocinadorRepository; // Inyectar el nuevo Repository

@Service
public class EventoImplement implements EventoService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;
    private final PatrocinadorRepository patrocinadorRepository; // Inyección de dependencia
    private final InscripcionProveedorRepository inscripcionProveedorRepository;
    private final EmailService emailService;



    @Autowired
    public EventoImplement(EventoRepository eventoRepository,
                           UsuarioRepository usuarioRepository,
                           FileStorageService fileStorageService,
                           ModelMapper modelMapper,
                           PatrocinadorRepository patrocinadorRepository,
                           InscripcionProveedorRepository inscripcionProveedorRepository,
                           EmailService emailService
                           ) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.fileStorageService = fileStorageService;
        this.modelMapper = modelMapper;
        this.patrocinadorRepository = patrocinadorRepository;
        this.inscripcionProveedorRepository = inscripcionProveedorRepository;
        this.emailService = emailService;

    }
//_________________________________________________________________
    @Override
    public EventoDTO crearEvento(EventoCreacionDTO eventoCreacionDTO, Long idAdmin, MultipartFile imagen) {
        // 1. Verificar Usuario (Debe ser un Administrador)
        Usuario creador = usuarioRepository.findById(idAdmin)
                .orElseThrow(() -> new CustomException("Administrador no encontrado"));

        // Validar que sea administrador
        boolean esAdmin = creador.getRoles().stream()
                .anyMatch(rol -> rol.getNombre_rol().equals(NombreRol.ADMINISTRADOR));

        if (!esAdmin) {
            throw new CustomException("Solo los administradores pueden crear eventos");
        }

        // 2. Verificar Patrocinador
        Patrocinador patrocinador = patrocinadorRepository.findById(eventoCreacionDTO.getId_patrocinador())
                .orElseThrow(() -> new CustomException("Patrocinador no encontrado"));

        // 3. Mapeo y Asignación de Entidades
        Evento evento = modelMapper.map(eventoCreacionDTO, Evento.class);

        // Asignación de relaciones
        evento.setCreado_por(creador); // El Admin que crea
        evento.setPatrocinador(patrocinador); // El patrocinador que financia

        // Asignación de campos nuevos
        evento.setCuposMaximosProveedor(eventoCreacionDTO.getCuposMaximosProveedor());
        evento.setCostoEspacio(eventoCreacionDTO.getCostoEspacio());
        evento.setTerminosCondiciones(eventoCreacionDTO.getTerminosCondiciones());
        evento.setEstado(EstadoEvento.BORRADOR);

        // 4. Manejo de imagen
        if (imagen != null && !imagen.isEmpty()) {
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "eventos");
            evento.setImagen_evento(rutaImagen);
        }

        Evento nuevoEvento = eventoRepository.save(evento);

        // 5. Mapeo a DTO de salida
        EventoDTO resultado = modelMapper.map(nuevoEvento, EventoDTO.class);
        resultado.setCreado_por(nuevoEvento.getCreado_por().getId_usuario());

        // Datos del patrocinador
        resultado.setId_patrocinador(nuevoEvento.getPatrocinador().getId_patrocinador());
        resultado.setNombrePatrocinador(nuevoEvento.getPatrocinador().getNombre());

        // Calcular cupos disponibles
        Long cuposOcupados = inscripcionProveedorRepository.countCuposOcupadosParaEvento(nuevoEvento.getId_evento());

        resultado.setCuposDisponibles(nuevoEvento.getCuposMaximosProveedor() - cuposOcupados.intValue());

        return resultado;
    }

    @Override
    public EventoDTO obtenerEventoPorId(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Evento no encontrado"));

        // Mapeo a DTO
        EventoDTO resultado = modelMapper.map(evento, EventoDTO.class);

        // Incluir el ID del creador
        if (evento.getCreado_por() != null) {
            resultado.setCreado_por(evento.getCreado_por().getId_usuario());
        }

        // Incluir datos del patrocinador
        if (evento.getPatrocinador() != null) {
            resultado.setId_patrocinador(evento.getPatrocinador().getId_patrocinador());
            resultado.setNombrePatrocinador(evento.getPatrocinador().getNombre());
        }

        // Calcular cupos disponibles
        Long cuposOcupados = inscripcionProveedorRepository.countCuposOcupadosParaEvento(id);

        int cuposDisponibles = evento.getCuposMaximosProveedor() - cuposOcupados.intValue();
        resultado.setCuposDisponibles(cuposDisponibles);

        return resultado;
    }

    @Override
    public List<EventoDTO> obtenerTodosLosEventos() {
        List<Evento> eventos = eventoRepository.findAll();
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());

                    // Opcional: agregar datos completos del patrocinador
                    if (evento.getPatrocinador() != null) {
                        dto.setId_patrocinador(evento.getPatrocinador().getId_patrocinador());
                        dto.setNombrePatrocinador(evento.getPatrocinador().getNombre());
                    }

                    // Opcional: calcular cupos disponibles
                    Long cuposOcupados = inscripcionProveedorRepository.countCuposOcupadosParaEvento(evento.getId_evento());
                    dto.setCuposDisponibles(evento.getCuposMaximosProveedor() - cuposOcupados.intValue());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> obtenerEventosPublicados() {
        List<Evento> eventos = eventoRepository.findByEstado(EstadoEvento.PUBLICADO);
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());

                    // Opcional: agregar datos completos del patrocinador
                    if (evento.getPatrocinador() != null) {
                        dto.setId_patrocinador(evento.getPatrocinador().getId_patrocinador());
                        dto.setNombrePatrocinador(evento.getPatrocinador().getNombre());
                    }

                    // Opcional: calcular cupos disponibles
                    Long cuposOcupados = inscripcionProveedorRepository.countCuposOcupadosParaEvento(evento.getId_evento());

                    dto.setCuposDisponibles(evento.getCuposMaximosProveedor() - cuposOcupados.intValue());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> obtenerEventosBorrador() {
        List<Evento> eventos = eventoRepository.findByEstado(EstadoEvento.BORRADOR);
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());

                    // Opcional: agregar datos completos del patrocinador
                    if (evento.getPatrocinador() != null) {
                        dto.setId_patrocinador(evento.getPatrocinador().getId_patrocinador());
                        dto.setNombrePatrocinador(evento.getPatrocinador().getNombre());
                    }

                    // Opcional: calcular cupos disponibles
                    Long cuposOcupados = inscripcionProveedorRepository.countCuposOcupadosParaEvento(evento.getId_evento());

                    dto.setCuposDisponibles(evento.getCuposMaximosProveedor() - cuposOcupados.intValue());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> obtenerEventosPorCreador(Long idCreador) {
        List<Evento> eventos = eventoRepository.findByCreadorId(idCreador);
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());

                    // Opcional: agregar datos completos del patrocinador
                    if (evento.getPatrocinador() != null) {
                        dto.setId_patrocinador(evento.getPatrocinador().getId_patrocinador());
                        dto.setNombrePatrocinador(evento.getPatrocinador().getNombre());
                    }

                    // Opcional: calcular cupos disponibles
                    Long cuposOcupados = inscripcionProveedorRepository.countCuposOcupadosParaEvento(evento.getId_evento());
                    dto.setCuposDisponibles(evento.getCuposMaximosProveedor() - cuposOcupados.intValue());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> obtenerEventosPorTipo(TipoEvento tipoEvento) {
        // CORRECCIÓN: Usar el parámetro 'tipoEvento' de la función.
        List<Evento> eventos = eventoRepository.findByTipoEventoAndEstado(tipoEvento, EstadoEvento.PUBLICADO);
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());

                    // Opcional: agregar datos completos del patrocinador
                    if (evento.getPatrocinador() != null) {
                        dto.setId_patrocinador(evento.getPatrocinador().getId_patrocinador());
                        dto.setNombrePatrocinador(evento.getPatrocinador().getNombre());
                    }

                    // Opcional: calcular cupos disponibles
                    Long cuposOcupados = inscripcionProveedorRepository.countCuposOcupadosParaEvento(evento.getId_evento());
                    dto.setCuposDisponibles(evento.getCuposMaximosProveedor() - cuposOcupados.intValue());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventoDTO> obtenerEventosProximos(LocalDate fecha) {
        // CORRECCIÓN: Usar el parámetro 'fecha' de la función.
        List<Evento> eventos = eventoRepository.findEventosProximosAndEstado(fecha, EstadoEvento.PUBLICADO);
        return eventos.stream()
                .map(evento -> {
                    EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
                    dto.setCreado_por(evento.getCreado_por().getId_usuario());

                    // Opcional: agregar datos completos del patrocinador
                    if (evento.getPatrocinador() != null) {
                        dto.setId_patrocinador(evento.getPatrocinador().getId_patrocinador());
                        dto.setNombrePatrocinador(evento.getPatrocinador().getNombre());
                    }

                    // Opcional: calcular cupos disponibles
                    Long cuposOcupados = inscripcionProveedorRepository.countCuposOcupadosParaEvento(evento.getId_evento());
                    dto.setCuposDisponibles(evento.getCuposMaximosProveedor() - cuposOcupados.intValue());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // --- LÓGICA DE ACTUALIZACIÓN DE EVENTOS REDISEÑADA ---
    @Override
    public EventoDTO actualizarEvento(Long id, EventoCreacionDTO eventoCreacionDTO, MultipartFile imagen) {
        Evento eventoExistente = eventoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Evento no encontrado"));

        // 1. Validar que los nuevos cupos no sean menores a los ya ocupados (PRIMERO)
        Long cuposOcupados = inscripcionProveedorRepository.countCuposOcupadosParaEvento(id);

        if (eventoCreacionDTO.getCuposMaximosProveedor() < cuposOcupados.intValue()) {
            throw new CustomException("No puedes reducir los cupos por debajo de los ya confirmados (" + cuposOcupados + ")");
        }

        // 2. Actualización de Patrocinador
        if (!eventoExistente.getPatrocinador().getId_patrocinador().equals(eventoCreacionDTO.getId_patrocinador())) {
            Patrocinador nuevoPatrocinador = patrocinadorRepository.findById(eventoCreacionDTO.getId_patrocinador())
                    .orElseThrow(() -> new CustomException("Nuevo Patrocinador no encontrado"));
            eventoExistente.setPatrocinador(nuevoPatrocinador);
        }

        // 3. Actualizar campos básicos
        eventoExistente.setNombre(eventoCreacionDTO.getNombre());
        eventoExistente.setDescripcion(eventoCreacionDTO.getDescripcion());
        eventoExistente.setUbicacion(eventoCreacionDTO.getUbicacion());
        eventoExistente.setFecha_evento(eventoCreacionDTO.getFecha_evento());
        eventoExistente.setHora_evento(eventoCreacionDTO.getHora_evento());
        eventoExistente.setTipo_evento(eventoCreacionDTO.getTipo_evento());

        // 4. Actualizar campos de control
        eventoExistente.setCuposMaximosProveedor(eventoCreacionDTO.getCuposMaximosProveedor());
        eventoExistente.setCostoEspacio(eventoCreacionDTO.getCostoEspacio());
        eventoExistente.setTerminosCondiciones(eventoCreacionDTO.getTerminosCondiciones());

        // 5. Manejo de imagen
        if (imagen != null && !imagen.isEmpty()) {
            // Eliminar imagen anterior del sistema de archivos
            if (eventoExistente.getImagen_evento() != null) {
                fileStorageService.eliminarArchivo(eventoExistente.getImagen_evento());
            }
            // Guardar la nueva imagen y actualizar la URL
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "eventos");
            eventoExistente.setImagen_evento(rutaImagen);
        }

        // 6. Guardar evento actualizado
        Evento eventoActualizado = eventoRepository.save(eventoExistente);

        // 7. Mapeo a DTO de salida
        EventoDTO resultado = modelMapper.map(eventoActualizado, EventoDTO.class);
        resultado.setCreado_por(eventoActualizado.getCreado_por().getId_usuario());

        // Incluir datos del patrocinador
        if (eventoActualizado.getPatrocinador() != null) {
            resultado.setId_patrocinador(eventoActualizado.getPatrocinador().getId_patrocinador());
            resultado.setNombrePatrocinador(eventoActualizado.getPatrocinador().getNombre());
        }

        // Calcular cupos disponibles actualizados
        Long cuposOcupadosActualizados = inscripcionProveedorRepository.countCuposOcupadosParaEvento(id);
        int cuposDisponibles = eventoActualizado.getCuposMaximosProveedor() - cuposOcupadosActualizados.intValue();
        resultado.setCuposDisponibles(cuposDisponibles);

        return resultado;
    }

    @Override
    public void cambiarEstadoEvento(Long id, EstadoEvento estado) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Evento no encontrado"));
        evento.setEstado(estado);
        eventoRepository.save(evento);

        if (estado == EstadoEvento.PUBLICADO) {
            notificarProveedoresEventoPublicado(evento);
        }
    }

    @Override
    public void eliminarEvento(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Evento no encontrado"));
        evento.setEstado(EstadoEvento.CANCELADO);
        eventoRepository.save(evento);
    }

    private void notificarProveedoresEventoPublicado(Evento evento) {
        // Obtener todos los usuarios con rol PROVEEDOR que estén ACTIVOS
        List<Usuario> proveedores = usuarioRepository.findByRolNombre(NombreRol.PROVEEDOR);

        System.out.println("[EventoImplement] Notificando a " + proveedores.size() + " proveedores sobre el evento: " + evento.getNombre());

        // Enviar correo a cada proveedor
        for (Usuario proveedor : proveedores) {
            try {
                emailService.enviarNotificacionEventoPublicado(
                        proveedor.getEmail(),
                        proveedor.getNombre(),
                        evento.getNombre(),
                        evento.getUbicacion(),
                        evento.getFecha_evento().toString(),
                        evento.getHora_evento().toString(),
                        evento.getCuposMaximosProveedor(),
                        evento.getCostoEspacio()
                );
            } catch (Exception e) {
                System.err.println("[EventoImplement] Error al enviar correo a " + proveedor.getEmail() + ": " + e.getMessage());
            }
        }

        System.out.println("[EventoImplement] Notificación completada para el evento: " + evento.getNombre());
    }
}
