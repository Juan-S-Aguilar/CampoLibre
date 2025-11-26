package com.example.campolibre.Implement;

import com.example.campolibre.DTO.PqrsDTO;
import com.example.campolibre.Entity.*;
import com.example.campolibre.Enum.EstadoPqrs;
import com.example.campolibre.Enum.RolProceso; // Importar el nuevo Enum
import com.example.campolibre.Enum.TipoPqrs;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.PqrsRepository;
import com.example.campolibre.Repository.PqrsRespuestaRepository; // Importar el nuevo Repository
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Repository.PqrsTiendaRepository;
import com.example.campolibre.Repository.PqrsEventoRepository;
import com.example.campolibre.Service.PqrsService;
import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


import com.example.campolibre.Repository.TiendaRepository;
import com.example.campolibre.Repository.EventoRepository;

@Service
public class PqrsImplement implements PqrsService {

    private final PqrsRepository pqrsRepository;
    private final PqrsRespuestaRepository pqrsRespuestaRepository; // NUEVO
    private final UsuarioRepository usuarioRepository;
    private final PqrsTiendaRepository pqrsTiendaRepository;
    private final PqrsEventoRepository pqrsEventoRepository;
    private final TiendaRepository tiendaRepository;
    private final EventoRepository eventoRepository;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;


    @Autowired
    public PqrsImplement(PqrsRepository pqrsRepository,
                         UsuarioRepository usuarioRepository,
                         PqrsTiendaRepository pqrsTiendaRepository,
                         PqrsEventoRepository pqrsEventoRepository,
                         PqrsRespuestaRepository pqrsRespuestaRepository,
                         TiendaRepository tiendaRepository,
                         EventoRepository eventoRepository,
                         ModelMapper modelMapper,
                         EntityManager entityManager) {
        this.pqrsRepository = pqrsRepository;
        this.usuarioRepository = usuarioRepository;
        this.pqrsTiendaRepository = pqrsTiendaRepository;
        this.pqrsEventoRepository = pqrsEventoRepository;
        this.pqrsRespuestaRepository = pqrsRespuestaRepository;
        this.tiendaRepository = tiendaRepository;
        this.eventoRepository = eventoRepository;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
    }

    // ============================================================================
// MÉTODOS CORREGIDOS PARA QUE ADMIN VEA PQRS DE EVENTOS
// ============================================================================

    // 1️⃣ Método crearPqrs - PQRS de eventos van al Admin
    @Override
    @Transactional
    public PqrsDTO crearPqrs(PqrsDTO pqrsDTO) {
        Usuario emisor = usuarioRepository.findById(pqrsDTO.getId_emisor())
                .orElseThrow(() -> new CustomException("Usuario emisor no encontrado"));

        // --- 1. Determinar el RECEPTOR (Proveedor/Admin) ---
        Usuario receptorInicial = null;

        // Prioridad a la Tienda asociada
        if (pqrsDTO.getId_tienda() != null) {
            Tienda tienda = tiendaRepository.findById(pqrsDTO.getId_tienda())
                    .orElseThrow(() -> new CustomException("Tienda asociada no encontrada"));
            receptorInicial = tienda.getUsuario(); // Asigna al dueño de la tienda (Proveedor)
        }
        // ✅ CORRECCIÓN: Si es un Evento, va directo al Administrador
        else if (pqrsDTO.getId_evento() != null) {
            // Verificar que el evento existe
            Evento evento = eventoRepository.findById(pqrsDTO.getId_evento())
                    .orElseThrow(() -> new CustomException("Evento asociado no encontrado"));

            // Asignar al Administrador General
            receptorInicial = usuarioRepository.findByEmail("admin@campolibre.com");
            if (receptorInicial == null) {
                throw new CustomException("No se encontró un usuario Administrador para asignar la PQRS del evento");
            }
        }
        // Si no hay asociación, va al Administrador General
        else {
            receptorInicial = usuarioRepository.findByEmail("admin@campolibre.com");
            if (receptorInicial == null) {
                throw new CustomException("No se encontró un usuario Administrador para asignar la PQRS");
            }
        }

        Pqrs pqrs = modelMapper.map(pqrsDTO, Pqrs.class);

        // --- 2. Asignar campos de Trazabilidad y Receptor ---
        pqrs.setEmisor(emisor);
        pqrs.setReceptor(receptorInicial); // Asigna el responsable
        pqrs.setEstado(EstadoPqrs.PENDIENTE);
        pqrs.setFecha_envio(LocalDateTime.now());
        pqrs.setPendienteDe(RolProceso.PROVEEDOR);

        Pqrs nuevaPqrs = pqrsRepository.save(pqrs);

        // --- 3. Guardar la asociación (PqrsTienda/PqrsEvento) ---
        if (pqrsDTO.getId_tienda() != null) {
            PqrsTienda pt = new PqrsTienda();
            pt.setPqrs(nuevaPqrs);
            pt.setTienda(tiendaRepository.findById(pqrsDTO.getId_tienda()).get());
            pqrsTiendaRepository.save(pt);
        } else if (pqrsDTO.getId_evento() != null) {
            PqrsEvento pe = new PqrsEvento();
            pe.setPqrs(nuevaPqrs);
            pe.setEvento(eventoRepository.findById(pqrsDTO.getId_evento()).get());
            pqrsEventoRepository.save(pe);
        }

        PqrsDTO resultado = mapToDto(nuevaPqrs);
        return resultado;
    }

    // 2️⃣ Método obtenerPqrsVisibles - Admin ve PQRS sin asociación + PQRS de eventos
    @Override
    public List<PqrsDTO> obtenerPqrsVisibles(Long idUsuario, boolean esAdmin) {
        Set<Pqrs> visibles = new HashSet<>();

        if (esAdmin) {
            // ✅ CORRECCIÓN: ADMIN ve:
            // 1. PQRS sin asociación (sin tienda ni evento)
            List<Pqrs> unlinked = pqrsRepository.findUnlinkedPqrs();
            visibles.addAll(unlinked);

            // 2. PQRS asociadas a eventos (porque eventos son manejados por admin)
            List<Pqrs> todasPqrs = pqrsRepository.findAll();
            for (Pqrs p : todasPqrs) {
                try {
                    var pe = pqrsEventoRepository.findByPqrsId(p.getId_pqrs());
                    if (pe != null) {
                        visibles.add(p); // Tiene evento, admin puede verla
                    }
                } catch (Exception ignored) {}
            }
        } else {
            // Usuarios normales ven:
            if (idUsuario != null) {
                // 1. PQRS que ellos crearon (como emisores)
                List<Pqrs> misEnviadas = pqrsRepository.findByEmisorId(idUsuario);
                visibles.addAll(misEnviadas);

                // 2. PQRS asociadas a sus tiendas (como dueños)
                List<Pqrs> deMisTiendas = pqrsRepository.findByTiendaOwnerId(idUsuario);
                visibles.addAll(deMisTiendas);

                // 3. PQRS asociadas a sus eventos (como creadores)
                List<Pqrs> deMisEventos = pqrsRepository.findByEventoOwnerId(idUsuario);
                visibles.addAll(deMisEventos);
            }
        }

        // Convertir a DTO y marcar permisos de respuesta
        return visibles.stream()
                .map(pqrs -> {
                    PqrsDTO dto = mapToDto(pqrs);
                    boolean tienePermisoEstatico = puedeResponder(dto.getId_pqrs(), idUsuario, esAdmin);
                    boolean puedeResponderAhora = tienePermisoEstatico &&
                            dto.getPendienteDe() == RolProceso.PROVEEDOR;
                    dto.setPuede_responder(puedeResponderAhora);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 3️⃣ Método obtenerPqrsPendientesAdmin - Admin ve pendientes sin tienda O con evento
    @Override
    public List<PqrsDTO> obtenerPqrsPendientesAdmin() {
        // ✅ CORRECCIÓN: Admin ve PQRS sin tienda (incluye eventos y sin asociación)
        List<Pqrs> pendientes = pqrsRepository.findAllPendientes();
        return pendientes.stream()
                .map(this::mapToDto)
                .filter(dto -> dto.getId_tienda() == null) // Excluye solo las de tiendas
                .collect(Collectors.toList());
    }

    // 4️⃣ Método puedeResponder - Admin puede responder PQRS sin tienda
    @Override
    public boolean puedeResponder(Long idPqrs, Long idUsuario, boolean esAdmin) {
        // ✅ CORRECCIÓN: Admin puede responder PQRS sin tienda (incluye eventos)
        if (esAdmin) {
            try {
                var pt = pqrsTiendaRepository.findByPqrsId(idPqrs);
                if (pt != null) return false; // Tiene tienda, admin NO puede responder

                // Si no tiene tienda, admin SÍ puede responder (sea evento o sin asociación)
                return true;
            } catch (Exception e) {
                return true;
            }
        }

        // Usuarios normales solo pueden responder si son dueños de la tienda/evento asociado
        if (idUsuario == null) return false;

        // Verificar asociación con tienda
        try {
            var pt = pqrsTiendaRepository.findByPqrsId(idPqrs);
            if (pt != null && pt.getTienda() != null && pt.getTienda().getUsuario() != null) {
                if (idUsuario.equals(pt.getTienda().getUsuario().getId_usuario())) {
                    return true;
                }
            }
        } catch (Exception ignored) {}

        // Verificar asociación con evento
        try {
            var pe = pqrsEventoRepository.findByPqrsId(idPqrs);
            if (pe != null && pe.getEvento() != null && pe.getEvento().getCreado_por() != null) {
                if (idUsuario.equals(pe.getEvento().getCreado_por().getId_usuario())) {
                    return true;
                }
            }
        } catch (Exception ignored) {}

        return false;
    }
//__________________________________________________________________________________________________
    @Override
    public PqrsDTO obtenerPqrsPorId(Long id) {
        Pqrs pqrs = pqrsRepository.findById(id)
                .orElseThrow(() -> new CustomException("PQRS no encontrada"));

        return mapToDto(pqrs);
    }

    @Override
    public List<PqrsDTO> obtenerTodasLasPqrs() {
        List<Pqrs> pqrsList = pqrsRepository.findAll();
        return pqrsList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PqrsDTO> obtenerPqrsPendientes() {
        List<Pqrs> pqrsList = pqrsRepository.findAllPendientes();
        return pqrsList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PqrsDTO> obtenerPqrsPorEmisor(Long idEmisor) {
        List<Pqrs> pqrsList = pqrsRepository.findByEmisorId(idEmisor);
        return pqrsList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PqrsDTO> obtenerPqrsPorReceptor(Long idReceptor) {
        List<Pqrs> pqrsList = pqrsRepository.findByReceptorId(idReceptor);
        return pqrsList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PqrsDTO> obtenerPqrsPorTipo(TipoPqrs tipo) {
        List<Pqrs> pqrsList = pqrsRepository.findByTipo(tipo);
        return pqrsList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

/*    @Override
    public PqrsDTO responderPqrs(Long id, String respuesta, Long idReceptor) {
        Pqrs pqrs = pqrsRepository.findById(id)
                .orElseThrow(() -> new CustomException("PQRS no encontrada"));

        Usuario receptor = usuarioRepository.findById(idReceptor)
                .orElseThrow(() -> new CustomException("Usuario receptor no encontrado"));

        pqrs.setRespuesta(respuesta);
        pqrs.setReceptor(receptor);
        pqrs.setFecha_respuesta(LocalDateTime.now());
        pqrs.setEstado(EstadoPqrs.RESPONDIDA);

        Pqrs pqrsActualizada = pqrsRepository.save(pqrs);

        return mapToDto(pqrsActualizada);
    }*/

    @Override
    public void cambiarEstadoPqrs(Long id, EstadoPqrs estado) {
        Pqrs pqrs = pqrsRepository.findById(id)
                .orElseThrow(() -> new CustomException("PQRS no encontrada"));
        pqrs.setEstado(estado);
        pqrsRepository.save(pqrs);
    }


    @Override
    public List<Pqrs> buscarPqrsConFiltros(LocalDateTime fechaDesde, LocalDateTime fechaHasta,
                                           TipoPqrs tipo, EstadoPqrs estado) {
        return pqrsRepository.buscarPqrsConFiltros(fechaDesde, fechaHasta, tipo, estado);
    }

    //______________________________________________________________________________________________________

    @Override
    @Transactional
    public PqrsDTO registrarRespuesta(Long idPqrs, String contenido, Long idUsuarioEmisorRespuesta) {
        Pqrs pqrs = pqrsRepository.findById(idPqrs)
                .orElseThrow(() -> new CustomException("PQRS no encontrada"));

        Usuario emisorRespuesta = usuarioRepository.findById(idUsuarioEmisorRespuesta)
                .orElseThrow(() -> new CustomException("Usuario emisor de respuesta no encontrado"));

        // Validar: Solo se puede responder si está PENDIENTE o EN_REPLICA
        if (pqrs.getEstado() != EstadoPqrs.PENDIENTE && pqrs.getEstado() != EstadoPqrs.EN_REPLICA) {
            throw new CustomException("No se puede responder, la PQRS está en estado: " + pqrs.getEstado());
        }

        // 1. Crear el registro de interacción (trazabilidad)
        PqrsRespuesta nuevaRespuesta = new PqrsRespuesta();
        nuevaRespuesta.setPqrs(pqrs);
        nuevaRespuesta.setContenido(contenido);
        nuevaRespuesta.setEmitidoPor(RolProceso.PROVEEDOR);
        // 💡 CORRECCIÓN CRÍTICA: Asignar el usuario que envía la respuesta
        nuevaRespuesta.setEmisor(emisorRespuesta);

        // 2. Actualizar la PQRS
        pqrs.setReceptor(emisorRespuesta); // El proveedor/admin que responde se convierte en el receptor/responsable

        if (pqrs.getEstado() == EstadoPqrs.EN_REPLICA) {
            // Lógica de Respuesta 2 (cierre definitivo)
            pqrs.setEstado(EstadoPqrs.CERRADA_DEFINITIVA);
            pqrs.setPendienteDe(RolProceso.NINGUNO);
        } else {
            // Lógica de Respuesta 1
            pqrs.setEstado(EstadoPqrs.RESPONDIDA);
            pqrs.setPendienteDe(RolProceso.CONSUMIDOR); // Ahora le toca al consumidor
        }

        // 3. Guardar
        pqrsRespuestaRepository.save(nuevaRespuesta);
        Pqrs pqrsActualizada = pqrsRepository.save(pqrs);

        return mapToDto(pqrsActualizada);
    }

    @Override
    @Transactional
    public PqrsDTO registrarReplica(Long idPqrs, String contenido, Long idEmisorReplica) {
        Pqrs pqrs = pqrsRepository.findById(idPqrs)
                .orElseThrow(() -> new CustomException("PQRS no encontrada"));

        Usuario emisorReplica = usuarioRepository.findById(idEmisorReplica)
                .orElseThrow(() -> new CustomException("Usuario emisor de réplica no encontrado"));

        // Validar: Solo si está RESPONDIDA y es el emisor original
        if (pqrs.getEstado() != EstadoPqrs.RESPONDIDA || !pqrs.getEmisor().getId_usuario().equals(idEmisorReplica)) {
            throw new CustomException("Solo el emisor puede replicar y solo si la PQRS está RESPONDIDA.");
        }

        // 1. Crear el registro de interacción (trazabilidad)
        PqrsRespuesta nuevaReplica = new PqrsRespuesta();
        nuevaReplica.setPqrs(pqrs);
        nuevaReplica.setContenido(contenido);
        nuevaReplica.setEmitidoPor(RolProceso.CONSUMIDOR);
        // 💡 CORRECCIÓN CRÍTICA: Asignar el usuario que envía la réplica
        nuevaReplica.setEmisor(emisorReplica);

        // 2. Actualizar la PQRS
        pqrs.setEstado(EstadoPqrs.EN_REPLICA);
        pqrs.setPendienteDe(RolProceso.PROVEEDOR); // La acción vuelve al proveedor

        // 3. Guardar
        pqrsRespuestaRepository.save(nuevaReplica);
        Pqrs pqrsActualizada = pqrsRepository.save(pqrs);

        return mapToDto(pqrsActualizada);
    }

    // ... cerrarPqrsPorConsumidor (se mantiene igual) ...
    @Override
    @Transactional
    public PqrsDTO cerrarPqrsPorConsumidor(Long idPqrs, Long idUsuario) {
        Pqrs pqrs = pqrsRepository.findById(idPqrs)
                .orElseThrow(() -> new CustomException("PQRS no encontrada"));

        // Validar: Solo el emisor puede aceptar y cerrar
        if (!pqrs.getEmisor().getId_usuario().equals(idUsuario)) {
            throw new CustomException("Solo el emisor original puede cerrar la PQRS.");
        }

        // Validar: Solo se puede cerrar si está en estado RESPONDIDA
        if (pqrs.getEstado() != EstadoPqrs.RESPONDIDA) {
            throw new CustomException("La PQRS no puede ser cerrada por el consumidor en el estado actual: " + pqrs.getEstado());
        }

        // Cierre por aceptación
        pqrs.setEstado(EstadoPqrs.CERRADA_ACEPTADA);
        pqrs.setPendienteDe(RolProceso.NINGUNO);

        Pqrs pqrsActualizada = pqrsRepository.save(pqrs);
        entityManager.flush();
        entityManager.clear();


        return mapToDto(pqrsActualizada);
    }

    // ----------------------------------------------------------------------------------
    // 💡 AJUSTE FINAL AL MAPEO: Limpiar la lógica de id_receptor
    // ----------------------------------------------------------------------------------
    private PqrsDTO mapToDto(Pqrs pqrs) {
        // 1. Mapeo base
        PqrsDTO dto = modelMapper.map(pqrs, PqrsDTO.class);

        // 2. Mapeo manual de IDs de usuarios relacionados
        if (pqrs.getEmisor() != null) dto.setId_emisor(pqrs.getEmisor().getId_usuario());

        // 💡 CORRECCIÓN: El id_receptor SIEMPRE es el responsable actual (Proveedor/Admin)
        if (pqrs.getReceptor() != null) dto.setId_receptor(pqrs.getReceptor().getId_usuario());

        // 3. Obtener la última interacción usando el Repositorio (Eficiente)
        Optional<PqrsRespuesta> ultimaInteraccion = pqrsRespuestaRepository
                .findFirstByPqrsOrderByFechaEmisionDesc(pqrs);

        if (ultimaInteraccion.isPresent()) {
            PqrsRespuesta r = ultimaInteraccion.get();

            // Mapeamos el contenido y la fecha de la última interacción al DTO.
            dto.setRespuesta(r.getContenido());
            dto.setFecha_respuesta(r.getFechaEmision());
        } else {
            dto.setRespuesta(null);
            dto.setFecha_respuesta(null);
        }

        // ... Comprobar y mapear asociaciones a tienda/evento (se mantienen igual) ...
        try {
            var pt = pqrsTiendaRepository.findByPqrsId(pqrs.getId_pqrs());
            if (pt != null && pt.getTienda() != null) dto.setId_tienda(pt.getTienda().getId_tienda());
        } catch (Exception ignored) {}

        try {
            var pe = pqrsEventoRepository.findByPqrsId(pqrs.getId_pqrs());
            if (pe != null && pe.getEvento() != null) dto.setId_evento(pe.getEvento().getId_evento());
        } catch (Exception ignored) {}

        return dto;
    }

}


