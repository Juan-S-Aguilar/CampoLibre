package com.example.campolibre.Controller;

import com.example.campolibre.DTO.ItemPedidoDTO;
import com.example.campolibre.DTO.TiendaDTO;
import com.example.campolibre.DTO.UsuarioDTO;
import com.example.campolibre.Entity.Rol;
import com.example.campolibre.Enum.NombreRol;
import com.example.campolibre.Exception.CustomException;
import com.example.campolibre.Repository.RolRepository;
import com.example.campolibre.Service.*;
import com.example.campolibre.Enum.EstadoPedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRolService usuarioRolService;

    @Autowired
    private TiendaService tiendaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private PqrsService pqrsService;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PedidoService pedidoService;


    // ==================== RUTAS PÚBLICAS ====================

    @GetMapping("/")
    public String index() {
        return "auth/login";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new UsuarioDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registrarUsuario(@ModelAttribute("usuario") UsuarioDTO usuarioDTO,
                                   RedirectAttributes redirectAttributes) {
        try {
            // 1. Crear el usuario
            UsuarioDTO nuevoUsuario = usuarioService.crearUsuario(usuarioDTO);
            Long idNuevoUsuario = nuevoUsuario.getId_usuario();

            // 2. Obtener el rol seleccionado del DTO
            NombreRol rolPrincipal = usuarioDTO.getRolSeleccionado();

            // 3. Asignar el rol CONSUMIDOR (siempre)
            Rol rolConsumidor = rolRepository.findByNombreRol(NombreRol.CONSUMIDOR);
            if (rolConsumidor == null) {
                throw new CustomException("Rol CONSUMIDOR no encontrado.");
            }
            usuarioRolService.asignarRolAUsuario(
                    new com.example.campolibre.DTO.UsuarioRolDTO(
                            null,
                            idNuevoUsuario,
                            rolConsumidor.getId_rol()
                    )
            );

            // 4. Si selecciona PROVEEDOR, asignar también el rol PROVEEDOR
            if (rolPrincipal == NombreRol.PROVEEDOR) {
                Rol rolProveedor = rolRepository.findByNombreRol(NombreRol.PROVEEDOR);
                if (rolProveedor == null) {
                    throw new CustomException("Rol PROVEEDOR no encontrado.");
                }
                usuarioRolService.asignarRolAUsuario(
                        new com.example.campolibre.DTO.UsuarioRolDTO(
                                null,
                                idNuevoUsuario,
                                rolProveedor.getId_rol()
                        )
                );
            }

            redirectAttributes.addFlashAttribute("success", "Usuario registrado exitosamente. Puedes iniciar sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    // ==================== DASHBOARDS POR ROL ====================

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
            return "redirect:/";
        }

        try {
            // Estadísticas para el dashboard de ADMIN
            Long totalUsuarios = (long) usuarioService.obtenerUsuariosActivos().size();
            Long totalTiendas = (long) tiendaService.obtenerTiendasActivas().size();
            Long totalProductos = (long) productoService.obtenerProductosActivos().size();
            Long eventosAprobados = (long) eventoService.obtenerEventosAprobados().size();
            Long eventosPendientes = (long) eventoService.obtenerEventosPendientes().size();
            Long pqrsPendientes = (long) pqrsService.obtenerPqrsPendientes().size();
            // ✅ NUEVAS ESTADÍSTICAS DE VENTAS
            Long totalPedidos = (long) pedidoService.obtenerPedidosPorEstado(EstadoPedido.PAGADO).size();

            // Calcular total de productos vendidos (suma de cantidades en items de pedidos pagados)
            Long totalProductosVendidos = pedidoService.obtenerPedidosPorEstado(EstadoPedido.PAGADO).stream()
                    .flatMap(p -> p.getItems().stream())
                    .mapToLong(ItemPedidoDTO::getCantidad)
                    .sum();

            model.addAttribute("totalPedidos", totalPedidos);
            model.addAttribute("totalProductosVendidos", totalProductosVendidos);

            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("totalTiendas", totalTiendas);
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("eventosAprobados", eventosAprobados);
            model.addAttribute("eventosPendientes", eventosPendientes);
            model.addAttribute("pqrsPendientes", pqrsPendientes);
            model.addAttribute("totalPedidos", totalPedidos);
            model.addAttribute("totalProductosVendidos", totalProductosVendidos);
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar estadísticas");
        }

        return "dashboard/admin";
    }

    @GetMapping("/proveedor/dashboard")
    public String proveedorDashboard(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            return "redirect:/";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // Estadísticas para el dashboard de PROVEEDOR
            Long misTiendas = (long) tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario()).size();
            Long misProductos = productoService.obtenerTodosLosProductos().stream()
                    .filter(p -> tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario())
                            .stream().anyMatch(t -> t.getId_tienda().equals(p.getId_tienda())))
                    .count();
            Long misEventos = (long) eventoService.obtenerEventosPorCreador(usuario.getId_usuario()).size();
            Long eventosAprobados = eventoService.obtenerEventosPorCreador(usuario.getId_usuario()).stream()
                    .filter(e -> e.getEstado().name().equals("APROBADO"))
                    .count();
            Long eventosPendientes = eventoService.obtenerEventosPorCreador(usuario.getId_usuario()).stream()
                    .filter(e -> e.getEstado().name().equals("PENDIENTE"))
                    .count();
            Long misPqrs = (long) pqrsService.obtenerPqrsPorEmisor(usuario.getId_usuario()).size();

            // ✅ NUEVAS ESTADÍSTICAS DE VENTAS DEL PROVEEDOR
            List<TiendaDTO> misTiendasList = tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario());

            Double gananciasTotales = 0.0;
            Long totalVentas = 0L;

            for (TiendaDTO tienda : misTiendasList) {
                gananciasTotales += pedidoService.calcularGananciasTienda(tienda.getId_tienda());
                totalVentas += pedidoService.obtenerPedidosPagadosPorTienda(tienda.getId_tienda()).size();
            }

            model.addAttribute("gananciasTotales", gananciasTotales);
            model.addAttribute("totalVentas", totalVentas);

            model.addAttribute("usuario", usuario);
            model.addAttribute("misTiendas", misTiendas);
            model.addAttribute("misProductos", misProductos);
            model.addAttribute("misEventos", misEventos);
            model.addAttribute("eventosAprobados", eventosAprobados);
            model.addAttribute("eventosPendientes", eventosPendientes);
            model.addAttribute("misPqrs", misPqrs);
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar estadísticas");
        }

        return "dashboard/proveedor";
    }

    @GetMapping("/consumidor/dashboard")
    public String consumidorDashboard(Model model, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("CONSUMIDOR"))) {
            return "redirect:/";
        }

        try {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);

            // Estadísticas para el dashboard de CONSUMIDOR
            Long tiendasDisponibles = (long) tiendaService.obtenerTiendasActivas().size();
            Long productosDisponibles = (long) productoService.obtenerProductosActivos().size();
            Long eventosDisponibles = (long) eventoService.obtenerEventosAprobados().size();
            Long misPqrs = (long) pqrsService.obtenerPqrsPorEmisor(usuario.getId_usuario()).size();

            model.addAttribute("usuario", usuario);
            model.addAttribute("tiendasDisponibles", tiendasDisponibles);
            model.addAttribute("productosDisponibles", productosDisponibles);
            model.addAttribute("eventosDisponibles", eventosDisponibles);
            model.addAttribute("misPqrs", misPqrs);
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar estadísticas");
        }

        return "dashboard/consumidor";
    }
}