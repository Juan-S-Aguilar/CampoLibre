    // ==================== DataSeeder.java ====================
    package com.example.campolibre.Config;

    import com.example.campolibre.Entity.*;
    import com.example.campolibre.Enum.*;
    import com.example.campolibre.Repository.*;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.core.annotation.Order;
    import org.springframework.stereotype.Component;

    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.time.LocalTime;

    @Component
    @Order(2) // Se ejecuta después de DataLoader
    public class DataSeeder implements CommandLineRunner {

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private TiendaRepository tiendaRepository;

        @Autowired
        private ProductoRepository productoRepository;

        @Autowired
        private EventoRepository eventoRepository;

        @Autowired
        private MisEventosRepository misEventosRepository;

        @Autowired
        private PqrsRepository pqrsRepository;

        @Autowired
        private PqrsTiendaRepository pqrsTiendaRepository;

        @Autowired
        private PqrsEventoRepository pqrsEventoRepository;

        @Autowired
        private PqrsRespuestaRepository pqrsRespuestaRepository;

        @Autowired
        private PatrocinadorRepository patrocinadorRepository; // Asumimos que inyectaste este

        @Override
        public void run(String... args) throws Exception {
            // Solo crear datos si no existen
            if (tiendaRepository.count() == 0) {
                System.out.println("📦 Iniciando carga de datos de prueba...");

                cargarTiendas();
                cargarProductos();
                cargarEventos();
                cargarMisEventos();
                cargarPqrs();

                System.out.println("✅ Datos de prueba cargados exitosamente!");
            } else {
                System.out.println("ℹ️ Ya existen datos en la base de datos. Omitiendo seeder.");
            }
        }

        private void cargarTiendas() {
            Usuario admin = usuarioRepository.findByEmail("admin@campolibre.com");
            Usuario proveedor = usuarioRepository.findByEmail("proveedor@campolibre.com");

            // Tienda 1 - Del proveedor
            Tienda tienda1 = new Tienda();
            tienda1.setNombre("Frutas del Valle");
            tienda1.setDescripcion("Frutas frescas y orgánicas directamente del campo. Cultivamos sin químicos y con amor por la naturaleza.");
            tienda1.setEstado(EstadoTienda.ACTIVA);
            tienda1.setCorreo_tienda("frutas@valle.com");
            tienda1.setTelefono_tienda("3101234567");
            tienda1.setUbicacion("Vereda El Paraíso, Municipio de Fusagasugá");
            tienda1.setUsuario(proveedor);
            tienda1.setImagen_tienda(null);
            tiendaRepository.save(tienda1);
            System.out.println("✓ Tienda creada: " + tienda1.getNombre());

            // Tienda 2 - Del proveedor
            Tienda tienda2 = new Tienda();
            tienda2.setNombre("Lácteos La Campiña");
            tienda2.setDescripcion("Productos lácteos artesanales: quesos, yogurt y mantequilla. Más de 20 años de tradición familiar.");
            tienda2.setEstado(EstadoTienda.ACTIVA);
            tienda2.setCorreo_tienda("lacteos@campina.com");
            tienda2.setTelefono_tienda("3209876543");
            tienda2.setUbicacion("Finca Los Rosales, Vía La Mesa");
            tienda2.setUsuario(proveedor);
            tienda2.setImagen_tienda(null);
            tiendaRepository.save(tienda2);
            System.out.println("✓ Tienda creada: " + tienda2.getNombre());

            // Tienda 3 - Del admin (para pruebas)
            Tienda tienda3 = new Tienda();
            tienda3.setNombre("Herramientas AgroTech");
            tienda3.setDescripcion("Herramientas y maquinaria agrícola de calidad. Asesoría técnica incluida.");
            tienda3.setEstado(EstadoTienda.ACTIVA);
            tienda3.setCorreo_tienda("ventas@agrotech.com");
            tienda3.setTelefono_tienda("3157654321");
            tienda3.setUbicacion("Centro Comercial Agrícola, Bogotá");
            tienda3.setUsuario(admin);
            tienda3.setImagen_tienda(null);
            tiendaRepository.save(tienda3);
            System.out.println("✓ Tienda creada: " + tienda3.getNombre());
        }

        private void cargarProductos() {
            Tienda tienda1 = tiendaRepository.findById(1L).orElse(null);
            Tienda tienda2 = tiendaRepository.findById(2L).orElse(null);
            Tienda tienda3 = tiendaRepository.findById(3L).orElse(null);

            if (tienda1 == null || tienda2 == null || tienda3 == null) {
                System.out.println("⚠️ No se encontraron tiendas para agregar productos");
                return;
            }

            // Productos de Tienda 1 (Frutas del Valle)
            Producto p1 = new Producto();
            p1.setNombre("Aguacate Hass");
            p1.setDescripcion("Aguacate de primera calidad, cremoso y nutritivo. Perfecto para ensaladas y preparaciones.");
            p1.setPrecio(2500.0);
            p1.setStock(50);
            p1.setCategoria(CategoriaProducto.FRUTAS);
            p1.setTienda(tienda1);
            p1.setImagen_producto(null);
            p1.setEstado("ACTIVO");
            productoRepository.save(p1);

            Producto p2 = new Producto();
            p2.setNombre("Mango Tommy");
            p2.setDescripcion("Mango dulce y jugoso, ideal para jugos y postres. Cosecha reciente.");
            p2.setPrecio(3000.0);
            p2.setStock(30);
            p2.setCategoria(CategoriaProducto.FRUTAS);
            p2.setTienda(tienda1);
            p2.setImagen_producto(null);
            p2.setEstado("ACTIVO");
            productoRepository.save(p2);

            Producto p3 = new Producto();
            p3.setNombre("Papaya Maradol");
            p3.setDescripcion("Papaya grande y dulce, rica en vitaminas. Excelente para el desayuno.");
            p3.setPrecio(4500.0);
            p3.setStock(20);
            p3.setCategoria(CategoriaProducto.FRUTAS);
            p3.setTienda(tienda1);
            p3.setImagen_producto(null);
            p3.setEstado("ACTIVO");
            productoRepository.save(p3);

            // Productos de Tienda 2 (Lácteos La Campiña)
            Producto p4 = new Producto();
            p4.setNombre("Queso Campesino");
            p4.setDescripcion("Queso fresco artesanal de 500g. Elaborado con leche del día, sin conservantes.");
            p4.setPrecio(12000.0);
            p4.setStock(25);
            p4.setCategoria(CategoriaProducto.LACTEOS);
            p4.setTienda(tienda2);
            p4.setImagen_producto(null);
            p4.setEstado("ACTIVO");
            productoRepository.save(p4);

            Producto p5 = new Producto();
            p5.setNombre("Yogurt Natural");
            p5.setDescripcion("Yogurt casero de 1 litro, sin azúcar añadida. Probióticos naturales.");
            p5.setPrecio(8000.0);
            p5.setStock(40);
            p5.setCategoria(CategoriaProducto.LACTEOS);
            p5.setTienda(tienda2);
            p5.setImagen_producto(null);
            p5.setEstado("ACTIVO");
            productoRepository.save(p5);

            Producto p6 = new Producto();
            p6.setNombre("Mantequilla Artesanal");
            p6.setDescripcion("Mantequilla casera de 250g. Sabor único y tradicional.");
            p6.setPrecio(6500.0);
            p6.setStock(15);
            p6.setCategoria(CategoriaProducto.LACTEOS);
            p6.setTienda(tienda2);
            p6.setImagen_producto(null);
            p6.setEstado("ACTIVO");
            productoRepository.save(p6);

            // Productos de Tienda 3 (Herramientas AgroTech)
            Producto p7 = new Producto();
            p7.setNombre("Pala Agrícola Reforzada");
            p7.setDescripcion("Pala de acero con mango de madera. Resistente y duradera.");
            p7.setPrecio(45000.0);
            p7.setStock(12);
            p7.setCategoria(CategoriaProducto.HERRAMIENTAS);
            p7.setTienda(tienda3);
            p7.setImagen_producto(null);
            p7.setEstado("ACTIVO");
            productoRepository.save(p7);

            Producto p8 = new Producto();
            p8.setNombre("Azadón Premium");
            p8.setDescripcion("Azadón de alta calidad con cabeza forjada. Ideal para labores pesadas.");
            p8.setPrecio(38000.0);
            p8.setStock(10);
            p8.setCategoria(CategoriaProducto.HERRAMIENTAS);
            p8.setTienda(tienda3);
            p8.setImagen_producto(null);
            p8.setEstado("ACTIVO");
            productoRepository.save(p8);

            Producto p9 = new Producto();
            p9.setNombre("Fumigadora Manual 20L");
            p9.setDescripcion("Fumigadora de espalda con capacidad de 20 litros. Boquilla ajustable.");
            p9.setPrecio(125000.0);
            p9.setStock(5);
            p9.setCategoria(CategoriaProducto.MAQUINARIA);
            p9.setTienda(tienda3);
            p9.setImagen_producto(null);
            p9.setEstado("ACTIVO");
            productoRepository.save(p9);

            System.out.println("✓ 9 productos creados exitosamente");
        }

        // ==================== DataSeeder.java (CORREGIDO) ====================

    // ... (líneas 1 a 209)

        private void cargarEventos() {
            Usuario admin = usuarioRepository.findByEmail("admin@campolibre.com");
            Usuario proveedor = usuarioRepository.findByEmail("proveedor@campolibre.com");

            // PASO 1: Crear o asegurar que existe un Patrocinador
            // Usamos findById(1L) para intentar recuperarlo, si no existe, lo creamos.
            Patrocinador patrocinadorDefault = patrocinadorRepository.findById(1L).orElseGet(() -> {
                Patrocinador p = new Patrocinador();
                p.setNombre("Patrocinador Oficial CL"); // Nombre obligatorio según la entidad
                p.setDescripcion("Patrocinador de eventos de Campo Libre.");
                p.setLogoUrl(null);
                p.setContactoEmail("contacto@patrocinador.com");
                Patrocinador savedPatrocinador = patrocinadorRepository.save(p);
                System.out.println("✓ Patrocinador de prueba creado: " + savedPatrocinador.getNombre());
                return savedPatrocinador;
            });

            // Evento 1 - APROBADO
            Evento evento1 = new Evento();
            evento1.setNombre("Feria Agrícola Regional 2025");
            evento1.setDescripcion("Gran feria donde productores del campo mostrarán sus mejores productos. " +
                    "Habrá degustaciones, música en vivo y actividades para toda la familia.");
            evento1.setUbicacion("Parque Principal de Fusagasugá");
            evento1.setFecha_evento(LocalDate.now().plusDays(15));
            evento1.setHora_evento(LocalTime.of(9, 0));
            evento1.setTipo_evento(TipoEvento.FERIA);
            evento1.setEstado(EstadoEvento.PUBLICADO);
            evento1.setCreado_por(proveedor);
            evento1.setImagen_evento(null);
            evento1.setCostoEspacio(50000.0);
            evento1.setCuposMaximosProveedor(20);
            // ✅ SOLUCIÓN: Asignar Patrocinador
            evento1.setPatrocinador(patrocinadorDefault);
            eventoRepository.save(evento1);
            System.out.println("✓ Evento creado: " + evento1.getNombre());

            // Evento 2 - APROBADO
            Evento evento2 = new Evento();
            evento2.setNombre("Taller de Agricultura Sostenible");
            evento2.setDescripcion("Aprende técnicas modernas de cultivo orgánico y sostenible. " +
                    "Incluye certificado de participación y kit de semillas.");
            evento2.setUbicacion("Auditorio SENA, Centro Agroindustrial");
            evento2.setFecha_evento(LocalDate.now().plusDays(7));
            evento2.setHora_evento(LocalTime.of(14, 0));
            evento2.setTipo_evento(TipoEvento.TALLER);
            evento2.setEstado(EstadoEvento.PUBLICADO);
            evento2.setCreado_por(admin);
            evento2.setImagen_evento(null);
            evento2.setCostoEspacio(20000.0);
            evento2.setCuposMaximosProveedor(10);
            // ✅ SOLUCIÓN: Asignar Patrocinador
            evento2.setPatrocinador(patrocinadorDefault);
            eventoRepository.save(evento2);
            System.out.println("✓ Evento creado: " + evento2.getNombre());

            // Evento 3 - PENDIENTE (para que admin lo apruebe)
            Evento evento3 = new Evento();
            evento3.setNombre("Día del Campesino - Celebración");
            evento3.setDescripcion("Celebración especial en honor a los campesinos de la región. " +
                    "Comida típica, presentaciones culturales y reconocimientos.");
            evento3.setUbicacion("Plaza de Mercado Municipal");
            evento3.setFecha_evento(LocalDate.now().plusDays(30));
            evento3.setHora_evento(LocalTime.of(10, 0));
            evento3.setTipo_evento(TipoEvento.TALLER);
            evento3.setEstado(EstadoEvento.BORRADOR);
            evento3.setCreado_por(proveedor);
            evento3.setImagen_evento(null);
            evento3.setCostoEspacio(0.0);
            evento3.setCuposMaximosProveedor(5);
            // ✅ SOLUCIÓN: Asignar Patrocinador
            evento3.setPatrocinador(patrocinadorDefault);
            eventoRepository.save(evento3);
            System.out.println("✓ Evento creado: " + evento3.getNombre() + " (PENDIENTE)");
        }
    // ...

        private void cargarMisEventos() {
            Usuario consumidor = usuarioRepository.findByEmail("consumidor@campolibre.com");
            Usuario admin = usuarioRepository.findByEmail("admin@campolibre.com");

            Evento evento1 = eventoRepository.findById(1L).orElse(null);
            Evento evento2 = eventoRepository.findById(2L).orElse(null);

            if (evento1 == null || evento2 == null || consumidor == null) {
                System.out.println("⚠️ No se encontraron eventos o usuarios para confirmaciones");
                return;
            }

            // Confirmación 1 - Consumidor va a la Feria
            MisEventos me1 = new MisEventos();
            me1.setUsuario(consumidor);
            me1.setEvento(evento1);
            misEventosRepository.save(me1);

            // Confirmación 2 - Admin va al Taller
            MisEventos me2 = new MisEventos();
            me2.setUsuario(admin);
            me2.setEvento(evento2);
            misEventosRepository.save(me2);

            System.out.println("✓ 2 confirmaciones de asistencia creadas");
        }

        private void cargarPqrs() {
            Usuario consumidor = usuarioRepository.findByEmail("consumidor@campolibre.com");
            Usuario proveedor = usuarioRepository.findByEmail("proveedor@campolibre.com");
            Usuario admin = usuarioRepository.findByEmail("admin@campolibre.com");

            Tienda tienda1 = tiendaRepository.findById(1L).orElse(null);
            Evento evento1 = eventoRepository.findById(1L).orElse(null);

            if (consumidor == null || tienda1 == null || evento1 == null || admin == null) {
                System.out.println("⚠️ No se encontraron datos para crear PQRS");
                return;
            }

            // --- PQRS 1 - PENDIENTE (sobre una tienda) ---
            Pqrs pqrs1 = new Pqrs();
            pqrs1.setTipo(TipoPqrs.PREGUNTA);
            pqrs1.setDescripcion("¿Hacen envíos a domicilio? Me interesa comprar varios productos de su tienda pero vivo lejos.");
            pqrs1.setEstado(EstadoPqrs.PENDIENTE);
            pqrs1.setEmisor(consumidor);
            pqrs1.setReceptor(admin); // Asignar al receptor inicial para que aparezca en su bandeja
            // pqrs1.setRespuesta(null); // ❌ ELIMINADO
            // pqrs1.setFecha_respuesta(null); // ❌ ELIMINADO
            pqrs1.setPendienteDe(RolProceso.PROVEEDOR); // 💡 Indicamos que el PROVEEDOR tiene el turno
            pqrsRepository.save(pqrs1);

            // Asociar con tienda
            PqrsTienda pt1 = new PqrsTienda();
            pt1.setPqrs(pqrs1);
            pt1.setTienda(tienda1);
            pqrsTiendaRepository.save(pt1);
            System.out.println("✓ PQRS creada: Pregunta sobre tienda (PENDIENTE)");

            // --- PQRS 2 - RESPONDIDA (sobre un evento) ---
            Pqrs pqrs2 = new Pqrs();
            pqrs2.setTipo(TipoPqrs.PREGUNTA);
            pqrs2.setDescripcion("¿La entrada al evento es gratuita o tiene algún costo?");
            pqrs2.setEstado(EstadoPqrs.RESPONDIDA);
            pqrs2.setEmisor(consumidor);
            pqrs2.setReceptor(consumidor); // El último receptor es el Consumidor, quien debe replicar
            // pqrs2.setRespuesta("..."); // ❌ ELIMINADO
            // pqrs2.setFecha_respuesta(...) // ❌ ELIMINADO
            pqrs2.setPendienteDe(RolProceso.CONSUMIDOR); // 💡 Indicamos que el CONSUMIDOR tiene el turno
            pqrsRepository.save(pqrs2);

            // 💡 CREAR EL REGISTRO DE RESPUESTA
            PqrsRespuesta resp2 = new PqrsRespuesta();
            resp2.setPqrs(pqrs2);
            resp2.setContenido("¡Hola! La entrada es completamente gratuita para todos los asistentes. Te esperamos.");
            resp2.setEmisor(admin);
            resp2.setEmitidoPor(RolProceso.PROVEEDOR);
            resp2.setFechaEmision(LocalDateTime.now().minusDays(1));
            pqrsRespuestaRepository.save(resp2);

            // Asociar con evento
            PqrsEvento pe1 = new PqrsEvento();
            pe1.setPqrs(pqrs2);
            pe1.setEvento(evento1);
            pqrsEventoRepository.save(pe1);
            System.out.println("✓ PQRS creada: Pregunta sobre evento (RESPONDIDA)");

            // --- PQRS 3 - RESPONDIDA (sugerencia general) ---
            Pqrs pqrs3 = new Pqrs();
            pqrs3.setTipo(TipoPqrs.SUGERENCIA);
            pqrs3.setDescripcion("Sería excelente si pudieran agregar filtros por precio en la búsqueda de productos. Facilitaría mucho encontrar lo que busco.");
            pqrs3.setEstado(EstadoPqrs.RESPONDIDA);
            pqrs3.setEmisor(proveedor);
            pqrs3.setReceptor(proveedor); // El último receptor es el Emisor/Proveedor (quien debe cerrar o replicar)
            // pqrs3.setRespuesta("..."); // ❌ ELIMINADO
            // pqrs3.setFecha_respuesta(...) // ❌ ELIMINADO
            pqrs3.setPendienteDe(RolProceso.CONSUMIDOR); // 💡 Indicamos que el CONSUMIDOR tiene el turno
            pqrsRepository.save(pqrs3);

            // 💡 CREAR EL REGISTRO DE RESPUESTA
            PqrsRespuesta resp3 = new PqrsRespuesta();
            resp3.setPqrs(pqrs3);
            resp3.setContenido("¡Gracias por tu sugerencia! La tomaremos en cuenta para futuras actualizaciones de la plataforma.");
            resp3.setEmisor(admin);
            resp3.setEmitidoPor(RolProceso.PROVEEDOR);
            resp3.setFechaEmision(LocalDateTime.now().minusDays(2));
            pqrsRespuestaRepository.save(resp3);

            System.out.println("✓ PQRS creada: Sugerencia general (RESPONDIDA)");
        }
    }