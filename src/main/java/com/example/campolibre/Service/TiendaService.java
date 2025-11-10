package com.example.campolibre.Service;

import com.example.campolibre.DTO.TiendaDTO;
import com.example.campolibre.Enum.EstadoTienda;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface TiendaService {
    TiendaDTO crearTienda(TiendaDTO tiendaDTO, MultipartFile imagen);
    TiendaDTO obtenerTiendaPorId(Long id);
    List<TiendaDTO> obtenerTodasLasTiendas();
    List<TiendaDTO> obtenerTiendasActivas();
    List<TiendaDTO> obtenerTiendasPorUsuario(Long idUsuario);
    List<TiendaDTO> obtenerTiendasPorEstado(EstadoTienda estado);
    TiendaDTO actualizarTienda(Long id, TiendaDTO tiendaDTO, MultipartFile imagen);
    void cambiarEstadoTienda(Long id, EstadoTienda estado);
    void eliminarTienda(Long id);
}