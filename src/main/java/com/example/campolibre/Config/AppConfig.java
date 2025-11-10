package com.example.campolibre.Config;

import com.example.campolibre.Entity.Usuario;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

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

        return mapper;
    }
}