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

    @Autowired
    private com.example.campolibre.Service.EmailService emailService;

    @Autowired
    private InscripcionProveedorService inscripcionService;


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
            // 1. Obtener el rol seleccionado del DTO
            NombreRol rolSeleccionado = usuarioDTO.getRolSeleccionado();

            // Si no seleccionó ningún rol, asignar CONSUMIDOR por defecto
            if (rolSeleccionado == null) {
                rolSeleccionado = NombreRol.CONSUMIDOR;
            }

            // 2. Buscar el rol en la base de datos
            Rol rol = rolRepository.findByNombreRol(rolSeleccionado);
            if (rol == null) {
                throw new CustomException("Rol " + rolSeleccionado + " no encontrado.");
            }

            // 3. Asignar el ID del rol al DTO para que se guarde con el usuario
            usuarioDTO.setId_rol(rol.getId_rol());

            // 4. Crear el usuario (ahora con el rol asignado)
            UsuarioDTO nuevoUsuario = usuarioService.crearUsuario(usuarioDTO);

            // 5. Enviar correo de confirmación de cuenta (async dentro del servicio)
            try {
                if (nuevoUsuario.getEmail() != null && !nuevoUsuario.getEmail().isEmpty()) {
                    emailService.enviarConfirmacionCuenta(nuevoUsuario.getEmail(), nuevoUsuario.getNombre());
                }
            } catch (Exception mailEx) {
                System.err.println("Advertencia: error al enviar correo de confirmación de cuenta: " + mailEx.getMessage());
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
            Long eventosAprobados = (long) eventoService.obtenerEventosPublicados().size();
            Long eventosPendientes = (long) eventoService.obtenerEventosBorrador().size();
            Long pqrsPendientes = (long) pqrsService.obtenerPqrsPendientes().size();
            // ✅ NUEVAS ESTADÍSTICAS DE VENTAS
            Long totalPedidos = (long) pedidoService.obtenerPedidosPorEstado(EstadoPedido.PAGADO).size();

            // Calcular total de productos vendidos (suma de cantidades en items de pedidos pagados)
            Long totalProductosVendidos = pedidoService.obtenerPedidosPorEstado(EstadoPedido.PAGADO).stream()
                    .flatMap(p -> p.getItems().stream())
                    .mapToLong(ItemPedidoDTO::getCantidad)
                    .sum();

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

            // ========== ESTADÍSTICAS BÁSICAS (mantener) ==========
            Long misTiendas = (long) tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario()).size();

            Long misProductos = productoService.obtenerTodosLosProductos().stream()
                    .filter(p -> tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario())
                            .stream().anyMatch(t -> t.getId_tienda().equals(p.getId_tienda())))
                    .count();

            Long misPqrs = (long) pqrsService.obtenerPqrsPorEmisor(usuario.getId_usuario()).size();

            // ========== ESTADÍSTICAS DE VENTAS (mantener) ==========
            List<TiendaDTO> misTiendasList = tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario());
            Double gananciasTotales = 0.0;
            Long totalVentas = 0L;

            for (TiendaDTO tienda : misTiendasList) {
                gananciasTotales += pedidoService.calcularGananciasTienda(tienda.getId_tienda());
                totalVentas += pedidoService.obtenerPedidosPagadosPorTienda(tienda.getId_tienda()).size();
            }

            // ========== ✅ NUEVAS ESTADÍSTICAS DE INSCRIPCIONES A EVENTOS ==========

            // Total de inscripciones confirmadas (cupos asegurados)
            Long inscripcionesConfirmadas = inscripcionService
                    .obtenerEventosConfirmadosDeProveedor(usuario.getId_usuario())
                    .stream()
                    .count();

            // Total de inscripciones pendientes de pago
            Long inscripcionesPendientes = inscripcionService
                    .obtenerTodasLasInscripcionesDeProveedor(usuario.getId_usuario())
                    .stream()
                    .filter(i -> i.getEstadoCupo() == com.example.campolibre.Enum.EstadoCupo.PENDIENTE)
                    .count();

            // Total invertido en eventos (solo pagos confirmados)
            Double totalInvertido = inscripcionService
                    .obtenerTodasLasInscripcionesDeProveedor(usuario.getId_usuario())
                    .stream()
                    .filter(i -> i.getEstadoCupo() == com.example.campolibre.Enum.EstadoCupo.CONFIRMADO)
                    .mapToDouble(com.example.campolibre.DTO.InscripcionProveedorDTO::getCostoPagado)
                    .sum();

            // ========== AGREGAR AL MODELO ==========
            model.addAttribute("usuario", usuario);
            model.addAttribute("misTiendas", misTiendas);
            model.addAttribute("misProductos", misProductos);
            model.addAttribute("misPqrs", misPqrs);
            model.addAttribute("gananciasTotales", gananciasTotales);
            model.addAttribute("totalVentas", totalVentas);

            // ✅ NUEVAS VARIABLES PARA INSCRIPCIONES
            model.addAttribute("inscripcionesConfirmadas", inscripcionesConfirmadas);
            model.addAttribute("inscripcionesPendientes", inscripcionesPendientes);
            model.addAttribute("totalInvertido", totalInvertido);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar estadísticas: " + e.getMessage());
            e.printStackTrace(); // Para debugging
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
            Long eventosDisponibles = (long) eventoService.obtenerEventosPublicados().size();
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
