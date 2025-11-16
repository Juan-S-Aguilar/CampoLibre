package com.example.campolibre.Service;

import com.example.campolibre.DTO.MisEventosDTO;
import com.example.campolibre.DTO.PatrocinadorDTO;

import java.util.List;

public interface MisEventosService {


    MisEventosDTO guardarIntencionAsistencia(Long idUsuario, Long idEvento);

    void removerIntencionAsistencia(Long idUsuario, Long idEvento);


    List<MisEventosDTO> obtenerEventosGuardadosDeUsuario(Long idUsuario);


    boolean usuarioTieneEventoGuardado(Long idUsuario, Long idEvento);


}