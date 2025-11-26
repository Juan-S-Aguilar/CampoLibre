package com.example.campolibre.Config;

import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Entity.Rol;
import com.example.campolibre.Entity.Usuario;
import com.example.campolibre.Repository.RolRepository;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Autowired
    private RolRepository rolRepository;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        Converter<Usuario, Long> usuarioToLong = new Converter<Usuario, Long>() {
            @Override
            public Long convert(MappingContext<Usuario, Long> context) {
                Usuario src = context.getSource();
                return (src == null) ? null : src.getId_usuario();
            }
        };

        mapper.addConverter(usuarioToLong);

        // Configurar el mapeo de UsuarioDTO a Usuario
        TypeMap<UsuarioDTO, Usuario> typeMap = mapper.createTypeMap(UsuarioDTO.class, Usuario.class);
        typeMap.addMappings(mapping -> {
            // Mapear id_rol a Rol entity
            mapping.using(new Converter<Long, Rol>() {
                @Override
                public Rol convert(MappingContext<Long, Rol> context) {
                    Long idRol = context.getSource();
                    if (idRol == null) {
                        return null;
                    }
                    return rolRepository.findById(idRol).orElse(null);
                }
            }).map(UsuarioDTO::getId_rol, Usuario::setRol);
        });

        return mapper;
    }
}