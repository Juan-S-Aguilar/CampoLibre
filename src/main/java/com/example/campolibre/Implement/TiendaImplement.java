package com.example.campolibre.Implement;

import com.example.campolibre.DTO.TiendaDTO;
import com.example.campolibre.Entity.Tienda;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Enum.EstadoTienda;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.TiendaRepository;
import com.example.campolibre.Repository.UsuarioRepository;
import com.example.campolibre.Service.FileStorageService;
import com.example.campolibre.Service.TiendaService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TiendaImplement implements TiendaService {

    private final TiendaRepository tiendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;

    @Autowired
    public TiendaImplement(TiendaRepository tiendaRepository, UsuarioRepository usuarioRepository,
                           FileStorageService fileStorageService, ModelMapper modelMapper) {
        this.tiendaRepository = tiendaRepository;
        this.usuarioRepository = usuarioRepository;
        this.fileStorageService = fileStorageService;
        this.modelMapper = modelMapper;
    }

    @Override
    public TiendaDTO crearTienda(TiendaDTO tiendaDTO, MultipartFile imagen) {
        Usuario usuario = usuarioRepository.findById(tiendaDTO.getId_usuario())
                .orElseThrow(() -> new CustomException("Usuario no encontrado"));

        Tienda tienda = modelMapper.map(tiendaDTO, Tienda.class);
        tienda.setUsuario(usuario);
        tienda.setEstado(EstadoTienda.ACTIVA);

        if (imagen != null && !imagen.isEmpty()) {
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "tiendas");
            tienda.setImagen_tienda(rutaImagen);
        }

        Tienda nuevaTienda = tiendaRepository.save(tienda);

        TiendaDTO resultado = modelMapper.map(nuevaTienda, TiendaDTO.class);
        resultado.setId_usuario(nuevaTienda.getUsuario().getId_usuario());
        return resultado;
    }

    @Override
    public TiendaDTO obtenerTiendaPorId(Long id) {
        Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> new CustomException("Tienda no encontrada"));

        TiendaDTO resultado = modelMapper.map(tienda, TiendaDTO.class);
        resultado.setId_usuario(tienda.getUsuario().getId_usuario());
        return resultado;
    }

    @Override
    public List<TiendaDTO> obtenerTodasLasTiendas() {
        List<Tienda> tiendas = tiendaRepository.findAll();
        return tiendas.stream()
                .map(tienda -> {
                    TiendaDTO dto = modelMapper.map(tienda, TiendaDTO.class);
                    dto.setId_usuario(tienda.getUsuario().getId_usuario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TiendaDTO> obtenerTiendasActivas() {
        List<Tienda> tiendas = tiendaRepository.findAllActive();
        return tiendas.stream()
                .map(tienda -> {
                    TiendaDTO dto = modelMapper.map(tienda, TiendaDTO.class);
                    dto.setId_usuario(tienda.getUsuario().getId_usuario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TiendaDTO> obtenerTiendasPorUsuario(Long idUsuario) {
        List<Tienda> tiendas = tiendaRepository.findByUsuarioId(idUsuario);
        return tiendas.stream()
                .map(tienda -> {
                    TiendaDTO dto = modelMapper.map(tienda, TiendaDTO.class);
                    dto.setId_usuario(tienda.getUsuario().getId_usuario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TiendaDTO> obtenerTiendasPorEstado(EstadoTienda estado) {
        List<Tienda> tiendas = tiendaRepository.findByEstado(estado);
        return tiendas.stream()
                .map(tienda -> {
                    TiendaDTO dto = modelMapper.map(tienda, TiendaDTO.class);
                    dto.setId_usuario(tienda.getUsuario().getId_usuario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public TiendaDTO actualizarTienda(Long id, TiendaDTO tiendaDTO, MultipartFile imagen) {
        Tienda tiendaExistente = tiendaRepository.findById(id)
                .orElseThrow(() -> new CustomException("Tienda no encontrada"));

        tiendaExistente.setNombre(tiendaDTO.getNombre());
        tiendaExistente.setDescripcion(tiendaDTO.getDescripcion());
        tiendaExistente.setCorreo_tienda(tiendaDTO.getCorreo_tienda());
        tiendaExistente.setTelefono_tienda(tiendaDTO.getTelefono_tienda());
        tiendaExistente.setUbicacion(tiendaDTO.getUbicacion());

        if (imagen != null && !imagen.isEmpty()) {
            if (tiendaExistente.getImagen_tienda() != null) {
                fileStorageService.eliminarArchivo(tiendaExistente.getImagen_tienda());
            }
            String rutaImagen = fileStorageService.guardarArchivo(imagen, "tiendas");
            tiendaExistente.setImagen_tienda(rutaImagen);
        }

        Tienda tiendaActualizada = tiendaRepository.save(tiendaExistente);

        TiendaDTO resultado = modelMapper.map(tiendaActualizada, TiendaDTO.class);
        resultado.setId_usuario(tiendaActualizada.getUsuario().getId_usuario());
        return resultado;
    }

    @Override
    public void cambiarEstadoTienda(Long id, EstadoTienda estado) {
        Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> new CustomException("Tienda no encontrada"));
        tienda.setEstado(estado);
        tiendaRepository.save(tienda);
    }

    @Override
    public void eliminarTienda(Long id) {
        Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> new CustomException("Tienda no encontrada"));
        tienda.setEstado(EstadoTienda.ELIMINADA);
        tiendaRepository.save(tienda);
    }
}