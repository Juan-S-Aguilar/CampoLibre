package com.example.campolibre.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.util.Collection;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/", "/register", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        .requestMatchers("usuarios/perfil", "/usuarios/perfil/actualizar", "/carrito/calcular").hasAnyAuthority("ADMINISTRADOR", "CONSUMIDOR", "PROVEEDOR")
                        .requestMatchers("/consumidor/**", "/tiendas/**", "/productos/**", "/eventos/**", "/mis-eventos/**").hasAnyAuthority("ADMINISTRADOR", "CONSUMIDOR", "PROVEEDOR")
                        .requestMatchers("/proveedor/**", "/eventos/crear", "/eventos/mis-eventos").hasAnyAuthority("ADMINISTRADOR", "PROVEEDOR")
                        .requestMatchers("/admin/**", "/usuarios/**").hasAuthority("ADMINISTRADOR")
                        .requestMatchers("/compras/**").hasAnyAuthority("CONSUMIDOR")
                        .requestMatchers("/pqrs/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                // Jerarquía: ADMINISTRADOR > PROVEEDOR > CONSUMIDOR
                if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMINISTRADOR"))) {
                    return "/admin/dashboard";
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("PROVEEDOR"))) {
                    return "/proveedor/dashboard";
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("CONSUMIDOR"))) {
                    return "/consumidor/dashboard";
                }
                return "/";
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}