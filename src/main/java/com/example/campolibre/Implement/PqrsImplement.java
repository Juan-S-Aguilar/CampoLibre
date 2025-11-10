package com.example.campolibre.Implement;

import com.example.campolibre.DTO.PqrsDTO;
import com.example.campolibre.Entity.Pqrs;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Enum.EstadoPqrs;
import com.example.campolibre.Enum.TipoPqrs;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.PqrsRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Repository.PqrsTiendaRepository;
import com.example.campolibre.Repository.PqrsEventoRepository;
import com.example.campolibre.Service.PqrsService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@Service
public class PqrsImplement implements PqrsService {

    private final PqrsRepository pqrsRepository;
    private final UsuarioRepository usuarioRepository;
    private final PqrsTiendaRepository pqrsTiendaRepository;
    private final PqrsEventoRepository pqrsEventoRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public PqrsImplement(PqrsRepository pqrsRepository,
                         UsuarioRepository usuarioRepository,
                         PqrsTiendaRepository pqrsTiendaRepository,
                         PqrsEventoRepository pqrsEventoRepository,
                         ModelMapper modelMapper) {
        this.pqrsRepository = pqrsRepository;
        this.usuarioRepository = usuarioRepository;
        this.pqrsTiendaRepository = pqrsTiendaRepository;
        this.pqrsEventoRepository = pqrsEventoRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PqrsDTO crearPqrs(PqrsDTO pqrsDTO) {
        Usuario emisor = usuarioRepository.findById(pqrsDTO.getId_emisor())
                .orElseThrow(() -> new CustomException("Usuario emisor no encontrado"));

        Pqrs pqrs = modelMapper.map(pqrsDTO, Pqrs.class);
        pqrs.setEmisor(emisor);
        pqrs.setEstado(EstadoPqrs.PENDIENTE);

        Pqrs nuevaPqrs = pqrsRepository.save(pqrs);

        PqrsDTO resultado = mapToDto(nuevaPqrs);
        return resultado;
    }

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

    @Override
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
    }

    @Override
    public void cambiarEstadoPqrs(Long id, EstadoPqrs estado) {
        Pqrs pqrs = pqrsRepository.findById(id)
                .orElseThrow(() -> new CustomException("PQRS no encontrada"));
        pqrs.setEstado(estado);
        pqrsRepository.save(pqrs);
    }

    // Nuevo método: devuelve PQRS visibles para el usuario
    @Override
    public List<PqrsDTO> obtenerPqrsVisibles(Long idUsuario, boolean esAdmin) {
        Set<Pqrs> visibles = new HashSet<>();

        if (esAdmin) {
            // ADMIN ve SOLO las PQRS sin asociación (sin tienda ni evento)
            List<Pqrs> unlinked = pqrsRepository.findUnlinkedPqrs();
            visibles.addAll(unlinked);
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
                    dto.setPuede_responder(puedeResponder(dto.getId_pqrs(), idUsuario, esAdmin));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean puedeResponder(Long idPqrs, Long idUsuario, boolean esAdmin) {
        // Admin puede responder SOLO las PQRS sin asociación
        if (esAdmin) {
            try {
                // Verificar si NO tiene asociación con tienda
                var pt = pqrsTiendaRepository.findByPqrsId(idPqrs);
                if (pt != null) return false; // Tiene tienda, admin NO puede responder

                // Verificar si NO tiene asociación con evento
                var pe = pqrsEventoRepository.findByPqrsId(idPqrs);
                if (pe != null) return false; // Tiene evento, admin NO puede responder

                // No tiene asociaciones, admin SÍ puede responder
                return true;
            } catch (Exception e) {
                return true; // Si hay error, asumimos que no tiene asociaciones
            }
        }

        // Usuarios normales solo pueden responder si son dueños de la tienda/evento asociado
        if (idUsuario == null) return false;

        // Verificar asociación con tienda
        try {
            var pt = pqrsTiendaRepository.findByPqrsId(idPqrs);
            if (pt != null && pt.getTienda() != null && pt.getTienda().getUsuario() != null) {
                if (idUsuario.equals(pt.getTienda().getUsuario().getId_usuario())) {
                    return true; // Es dueño de la tienda
                }
            }
        } catch (Exception ignored) {}

        // Verificar asociación con evento
        try {
            var pe = pqrsEventoRepository.findByPqrsId(idPqrs);
            if (pe != null && pe.getEvento() != null && pe.getEvento().getCreado_por() != null) {
                if (idUsuario.equals(pe.getEvento().getCreado_por().getId_usuario())) {
                    return true; // Es creador del evento
                }
            }
        } catch (Exception ignored) {}

        return false;
    }

    @Override
    public List<PqrsDTO> obtenerPqrsPendientesAdmin() {
        // Devuelve PQRS con estado PENDIENTE y sin asociación a tienda ni evento
        List<Pqrs> pendientes = pqrsRepository.findAllPendientes();
        return pendientes.stream()
                .map(this::mapToDto)
                .filter(dto -> dto.getId_tienda() == null && dto.getId_evento() == null)
                .collect(Collectors.toList());
    }

    // Helper: mapear entidad Pqrs a PqrsDTO y completar asociaciones (tienda/evento)
    private PqrsDTO mapToDto(Pqrs pqrs) {
        PqrsDTO dto = modelMapper.map(pqrs, PqrsDTO.class);
        if (pqrs.getEmisor() != null) dto.setId_emisor(pqrs.getEmisor().getId_usuario());
        if (pqrs.getReceptor() != null) dto.setId_receptor(pqrs.getReceptor().getId_usuario());

        // comprobar si tiene asociación a tienda
        try {
            var pt = pqrsTiendaRepository.findByPqrsId(pqrs.getId_pqrs());
            if (pt != null && pt.getTienda() != null) dto.setId_tienda(pt.getTienda().getId_tienda());
        } catch (Exception ignored) {}

        // comprobar si tiene asociación a evento
        try {
            var pe = pqrsEventoRepository.findByPqrsId(pqrs.getId_pqrs());
            if (pe != null && pe.getEvento() != null) dto.setId_evento(pe.getEvento().getId_evento());
        } catch (Exception ignored) {}

        return dto;
    }

    @Override
    public List<Pqrs> buscarPqrsConFiltros(LocalDateTime fechaDesde, LocalDateTime fechaHasta,
                                           TipoPqrs tipo, EstadoPqrs estado) {
        return pqrsRepository.buscarPqrsConFiltros(fechaDesde, fechaHasta, tipo, estado);
    }
}