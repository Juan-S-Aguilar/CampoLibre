# CLAUDE.md - CampoLibre Development Guide for AI Assistants

**Last Updated:** 2025-11-14
**Project:** NewCampoLibre (CampoLibre)
**Version:** 0.0.1-SNAPSHOT
**Purpose:** Comprehensive guide for AI assistants working on the CampoLibre codebase

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Architecture Patterns](#architecture-patterns)
5. [Database Schema](#database-schema)
6. [Security & Authentication](#security--authentication)
7. [Development Workflow](#development-workflow)
8. [Coding Conventions](#coding-conventions)
9. [Testing Guidelines](#testing-guidelines)
10. [File Upload System](#file-upload-system)
11. [Email Integration](#email-integration)
12. [Document Generation](#document-generation)
13. [Common Tasks](#common-tasks)
14. [Troubleshooting](#troubleshooting)
15. [Security Considerations](#security-considerations)

---

## Project Overview

**CampoLibre** is a full-stack agricultural e-commerce and event management platform built with Spring Boot. It enables:

- **E-commerce:** Stores, products, shopping cart, orders, and payments
- **Events:** Agricultural events creation, management, and registration
- **Multi-role System:** Admin, Provider (Proveedor), and Consumer (Consumidor) roles
- **PQRS System:** Complaints, questions, requests, and suggestions management
- **Document Generation:** PDF reports and Excel exports

### Key Business Domains

1. **User Management:** Registration, authentication, role assignment
2. **Store & Product Management:** Catalog management for providers
3. **Shopping & Orders:** Cart, checkout, payment processing
4. **Event Management:** Event creation, approval workflow, user registration
5. **PQRS:** Customer service and complaint tracking

---

## Technology Stack

### Backend Framework
- **Java 21** - Programming language
- **Spring Boot 3.5.6** - Application framework
- **Spring Web MVC** - Web layer
- **Spring Data JPA** - Data access layer
- **Spring Security 6** - Authentication & authorization
- **Hibernate** - ORM implementation
- **Maven** - Build tool

### Database
- **MySQL 8** - Relational database
- **JDBC URL:** `jdbc:mysql://localhost:3307/new_campolibre`
- **Schema Management:** Hibernate DDL auto-update

### Frontend
- **Thymeleaf 3** - Server-side template engine
- **Bootstrap 5.3** - CSS framework (CDN)
- **Bootstrap Icons** - Icon library (CDN)
- **Custom CSS** - 7 custom stylesheets in `/static/css/`

### Libraries & Tools
- **Lombok** - Reduce boilerplate code
- **ModelMapper 3.1.1** - Entity-DTO mapping
- **Apache POI 5.2.3** - Excel generation
- **Flying Saucer 9.1.22** - HTML to PDF conversion
- **iText 2.1.7** - PDF library
- **Spring Boot Mail** - Email functionality (Gmail SMTP)
- **Spring Boot DevTools** - Hot reload

### Build & Development
- **Maven Wrapper** - `mvnw` / `mvnw.cmd`
- **Java Version:** 21
- **Encoding:** UTF-8 (enforced)

---

## Project Structure

```
CampoLibre/
├── src/
│   ├── main/
│   │   ├── java/com/example/campolibre/
│   │   │   ├── Config/              # 6 configuration classes
│   │   │   │   ├── AppConfig.java            # Bean configuration, ModelMapper
│   │   │   │   ├── SecurityConfig.java       # Spring Security setup
│   │   │   │   ├── WebConfig.java            # Static resources
│   │   │   │   ├── FileStorageConfig.java    # Upload path config
│   │   │   │   ├── DataLoader.java           # Initial data (roles, admin user)
│   │   │   │   └── DataSeeder.java           # Sample data seeding
│   │   │   │
│   │   │   ├── Controller/          # 9 web controllers
│   │   │   │   ├── HomeController.java       # Auth, dashboards
│   │   │   │   ├── UsuarioController.java    # User CRUD
│   │   │   │   ├── TiendaController.java     # Store management
│   │   │   │   ├── ProductoController.java   # Product catalog
│   │   │   │   ├── EventoController.java     # Event management
│   │   │   │   ├── CarritoController.java    # Shopping cart
│   │   │   │   ├── PedidoController.java     # Order processing
│   │   │   │   ├── PagoController.java       # Payment handling
│   │   │   │   ├── PqrsController.java       # PQRS system
│   │   │   │   └── MisEventosController.java # Event registrations
│   │   │   │
│   │   │   ├── Service/             # 13+ service interfaces
│   │   │   │   ├── UsuarioService.java
│   │   │   │   ├── TiendaService.java
│   │   │   │   ├── ProductoService.java
│   │   │   │   ├── CarritoService.java
│   │   │   │   ├── PedidoService.java
│   │   │   │   ├── PagoService.java
│   │   │   │   ├── EventoService.java
│   │   │   │   ├── PqrsService.java
│   │   │   │   ├── FileStorageService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── PdfService.java
│   │   │   │   └── ExcelService.java (implied)
│   │   │   │
│   │   │   ├── Implement/           # 14 service implementations
│   │   │   │   ├── UsuarioImplement.java
│   │   │   │   ├── TiendaImplement.java
│   │   │   │   ├── ProductoImplement.java
│   │   │   │   ├── CarritoImplement.java
│   │   │   │   ├── PedidoImplement.java
│   │   │   │   ├── PagoImplement.java
│   │   │   │   ├── EventoImplement.java
│   │   │   │   ├── PqrsImplement.java
│   │   │   │   ├── FileStorageImplement.java
│   │   │   │   ├── CustomUserDetailsService.java # Spring Security
│   │   │   │   └── ... (other implementations)
│   │   │   │
│   │   │   ├── Repository/          # 14 JPA repositories
│   │   │   │   ├── UsuarioRepository.java
│   │   │   │   ├── TiendaRepository.java
│   │   │   │   ├── ProductoRepository.java
│   │   │   │   ├── CarritoCompraRepository.java
│   │   │   │   ├── PedidoRepository.java
│   │   │   │   ├── PagoRepository.java
│   │   │   │   ├── EventoRepository.java
│   │   │   │   └── ... (other repositories)
│   │   │   │
│   │   │   ├── Entity/              # 16 JPA entities
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Rol.java
│   │   │   │   ├── UsuarioRol.java
│   │   │   │   ├── Tienda.java
│   │   │   │   ├── Producto.java
│   │   │   │   ├── CarritoCompra.java
│   │   │   │   ├── ItemCarrito.java
│   │   │   │   ├── Pedido.java
│   │   │   │   ├── ItemPedido.java
│   │   │   │   ├── Pago.java
│   │   │   │   ├── Evento.java
│   │   │   │   ├── MisEventos.java
│   │   │   │   ├── Pqrs.java
│   │   │   │   ├── PqrsRespuesta.java
│   │   │   │   ├── PqrsTienda.java
│   │   │   │   └── PqrsEvento.java
│   │   │   │
│   │   │   ├── DTO/                 # 18 data transfer objects
│   │   │   │   ├── UsuarioDTO.java
│   │   │   │   ├── ProductoDTO.java
│   │   │   │   ├── PedidoDTO.java
│   │   │   │   └── ... (other DTOs)
│   │   │   │
│   │   │   ├── Enum/                # 12 enumerations
│   │   │   │   ├── NombreRol.java (ADMINISTRADOR, CONSUMIDOR, PROVEEDOR)
│   │   │   │   ├── EstadoPedido.java
│   │   │   │   ├── EstadoPago.java
│   │   │   │   ├── TipoPqrs.java
│   │   │   │   ├── EstadoPqrs.java
│   │   │   │   ├── TipoEvento.java
│   │   │   │   ├── EstadoEvento.java
│   │   │   │   ├── CategoriaProducto.java
│   │   │   │   ├── TipoDocumento.java
│   │   │   │   ├── EstadoTienda.java
│   │   │   │   └── RolProceso.java
│   │   │   │
│   │   │   ├── Exception/           # Custom exceptions
│   │   │   │   └── CustomException.java
│   │   │   │
│   │   │   └── NewCampoLibreApplication.java  # Main class
│   │   │
│   │   └── resources/
│   │       ├── application.properties  # Configuration file
│   │       ├── static/
│   │       │   └── css/                # 7 CSS files
│   │       │       ├── base.css
│   │       │       ├── header-footer.css
│   │       │       ├── auth.css
│   │       │       ├── dashboard.css
│   │       │       ├── forms.css
│   │       │       ├── lists.css
│   │       │       └── cards.css
│   │       │
│   │       └── templates/              # 46 Thymeleaf HTML templates
│   │           ├── layout/
│   │           │   └── base.html           # Master layout
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   └── footer.html
│   │           ├── auth/
│   │           │   ├── index.html
│   │           │   ├── login.html
│   │           │   └── register.html
│   │           ├── dashboard/
│   │           │   ├── admin.html
│   │           │   ├── proveedor.html
│   │           │   └── consumidor.html
│   │           ├── usuario/              # User management (5 templates)
│   │           ├── tienda/               # Store management (4 templates)
│   │           ├── producto/             # Product catalog (4 templates)
│   │           ├── carrito/              # Shopping cart
│   │           ├── pedido/               # Orders (7 templates)
│   │           ├── pago/                 # Payments (4 templates)
│   │           ├── evento/               # Events (6 templates)
│   │           ├── mis-eventos/          # Event registrations
│   │           ├── pqrs/                 # PQRS system (5 templates)
│   │           └── reportes/             # PDF report templates
│   │
│   └── test/
│       └── java/com/example/campolibre/
│           └── NewCampoLibreApplicationTests.java
│
├── uploads/                    # User-uploaded files (gitignored)
│   ├── eventos/
│   ├── productos/
│   └── tiendas/
│
├── pom.xml                     # Maven configuration
├── mvnw / mvnw.cmd            # Maven wrapper
├── .gitignore
└── CLAUDE.md                  # This file
```

---

## Architecture Patterns

### Layered Architecture

CampoLibre follows a **4-layer architecture**:

```
┌─────────────────────────────────────────┐
│  Controller Layer (Web/Presentation)    │  @Controller, @GetMapping, @PostMapping
│  - Handle HTTP requests/responses       │  - HomeController, UsuarioController, etc.
│  - Validate input                       │
│  - Return view names (Thymeleaf)        │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│  Service Layer (Business Logic)         │  @Service (Interface + Implementation)
│  - Business rules & workflows           │  - UsuarioService, ProductoService, etc.
│  - Transaction management               │  - UsuarioImplement, ProductoImplement
│  - DTO mapping                          │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│  Repository Layer (Data Access)         │  extends JpaRepository<Entity, Long>
│  - Database queries                     │  - UsuarioRepository, ProductoRepository
│  - Custom @Query methods                │  - JPQL/native queries
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│  Entity Layer (Domain Model)            │  @Entity, @Table
│  - JPA entities                         │  - Usuario, Producto, Pedido, etc.
│  - Database schema mapping              │  - Relationships (@OneToMany, @ManyToOne)
│  - Business methods                     │
└─────────────────────────────────────────┘
```

### Design Patterns Used

1. **MVC (Model-View-Controller)**
   - Controllers: Handle HTTP requests
   - Models: Entity classes + DTOs
   - Views: Thymeleaf templates

2. **Repository Pattern**
   - Spring Data JPA repositories
   - Abstraction over data access

3. **Service Layer Pattern**
   - Interface-based services
   - Separation of business logic

4. **DTO Pattern**
   - Entity-DTO conversion with ModelMapper
   - Data transfer without exposing entities

5. **Dependency Injection**
   - Spring @Autowired (constructor injection preferred)

6. **Template Method**
   - Thymeleaf layout inheritance

7. **Strategy Pattern**
   - Role-based dashboard routing
   - Custom authentication success handler

---

## Database Schema

### Core Entities & Relationships

#### User Management
```
Usuario (users)
  ├── id_usuario (PK)
  ├── nombre, email (unique), contrasena (BCrypt hashed)
  ├── telefono, documento, tipo_documento
  ├── fecha_registro, estado (ACTIVO/ELIMINADO)
  └── Relationships:
      ├── OneToMany → Tienda (stores owned)
      ├── OneToMany → Evento (events created)
      ├── OneToMany → Pqrs (complaints/requests)
      ├── OneToMany → MisEventos (event registrations)
      └── ManyToMany → Rol (via UsuarioRol)

Rol (roles)
  ├── id_rol (PK)
  ├── nombre (ADMINISTRADOR, CONSUMIDOR, PROVEEDOR)
  └── ManyToMany → Usuario (via UsuarioRol)

UsuarioRol (user_roles junction table)
  ├── id_usuario_rol (PK)
  ├── usuario_id (FK → Usuario)
  └── rol_id (FK → Rol)
```

#### E-commerce
```
Tienda (stores)
  ├── id_tienda (PK)
  ├── nombre, descripcion, email, telefono
  ├── ubicacion, imagen, estado (ACTIVA/INACTIVA/SUSPENDIDA)
  ├── usuario_id (FK → Usuario, store owner)
  └── OneToMany → Producto

Producto (products)
  ├── id_producto (PK)
  ├── nombre, descripcion, precio, stock
  ├── categoria (enum), imagen
  ├── tienda_id (FK → Tienda)
  └── OneToMany → ItemCarrito

CarritoCompra (shopping_carts)
  ├── id_carrito (PK)
  ├── usuario_id (FK → Usuario, unique)
  ├── fecha_creacion
  └── OneToMany → ItemCarrito

ItemCarrito (cart_items)
  ├── id_item_carrito (PK)
  ├── carrito_id (FK → CarritoCompra)
  ├── producto_id (FK → Producto)
  ├── cantidad, precio_unitario, subtotal
  └── CascadeType.ALL from CarritoCompra

Pedido (orders)
  ├── id_pedido (PK)
  ├── numero_pedido (generated: PED-YEAR-SEQUENCE)
  ├── usuario_id (FK → Usuario)
  ├── tienda_id (FK → Tienda)
  ├── estado (PENDIENTE_PAGO, PAGADO, CANCELADO)
  ├── total, fecha_pedido, fecha_actualizacion
  ├── nombre_contacto, telefono_contacto
  ├── direccion_entrega, ciudad, notas
  └── OneToMany → ItemPedido

ItemPedido (order_items)
  ├── id_item_pedido (PK)
  ├── pedido_id (FK → Pedido)
  ├── producto_id (FK → Producto)
  ├── cantidad, precio_unitario, subtotal
  └── CascadeType.ALL, orphanRemoval=true

Pago (payments)
  ├── id_pago (PK)
  ├── pedido_id (FK → Pedido, OneToOne)
  ├── monto, metodo_pago (TARJETA_CREDITO, TARJETA_DEBITO, PSE, EFECTIVO)
  ├── estado (EXITOSO, FALLIDO, PENDIENTE)
  ├── numero_transaccion (generated: TRX-YYYYMM-UUID)
  ├── fecha_pago, mensaje_error
  └── Methods: marcarExitoso(), marcarFallido()
```

#### Events
```
Evento (events)
  ├── id_evento (PK)
  ├── nombre, descripcion, ubicacion
  ├── fecha_evento, hora_evento
  ├── tipo_evento (FERIA, TALLER, ACTIVIDAD, OTRO)
  ├── estado (PENDIENTE, APROBADO, RECHAZADO)
  ├── imagen, fecha_creacion
  ├── creador_id (FK → Usuario)
  └── OneToMany → MisEventos

MisEventos (event_registrations)
  ├── id_mis_eventos (PK)
  ├── usuario_id (FK → Usuario)
  ├── evento_id (FK → Evento)
  ├── fecha_guardado
  └── UniqueConstraint(usuario_id, evento_id)
```

#### PQRS System
```
Pqrs (complaints/requests)
  ├── id_pqrs (PK)
  ├── tipo (PREGUNTA, QUEJA, SUGERENCIA)
  ├── descripcion, estado (PENDIENTE, RESPONDIDA, CERRADA)
  ├── emisor_id (FK → Usuario)
  ├── receptor_id (FK → Usuario, nullable)
  ├── pendienteDe (RolProceso enum)
  ├── fecha_creacion
  └── OneToMany → PqrsRespuesta

PqrsRespuesta (pqrs_responses)
  ├── id_respuesta (PK)
  ├── pqrs_id (FK → Pqrs)
  ├── respuesta, fecha_respuesta
  └── CascadeType.ALL

PqrsTienda, PqrsEvento (specialized PQRS types)
```

### Important Database Conventions

1. **Primary Keys:** All entities use `Long id` with `@GeneratedValue(strategy = GenerationType.IDENTITY)`
2. **Naming:**
   - Entity class: `Usuario` (CamelCase)
   - Table name: `usuario` or `usuarios` (snake_case)
   - Column name: `id_usuario`, `fecha_creacion` (snake_case)
3. **Timestamps:** Use `@PrePersist` for automatic `fecha_creacion`
4. **Soft Deletes:** Usuario has `estado` field (ACTIVO/ELIMINADO)
5. **Cascading:** Use `CascadeType.ALL` for parent-child relationships (e.g., Pedido → ItemPedido)
6. **Orphan Removal:** `orphanRemoval=true` for ItemPedido
7. **Unique Constraints:**
   - `Usuario.email` (unique)
   - `CarritoCompra.usuario_id` (one cart per user)
   - `MisEventos(usuario_id, evento_id)` (one registration per user-event)

---

## Security & Authentication

### Spring Security Configuration

**File:** `src/main/java/com/example/campolibre/Config/SecurityConfig.java`

#### Role-Based Access Control

```java
// Public access (no authentication)
/login, /, /register, /css/**, /js/**, /images/**, /uploads/**

// Any authenticated user
/usuarios/perfil, /pqrs/**

// CONSUMIDOR, PROVEEDOR, or ADMINISTRADOR
/consumidor/**, /tiendas/**, /productos/**, /eventos/**, /mis-eventos/**

// PROVEEDOR or ADMINISTRADOR
/proveedor/**, /eventos/crear, /eventos/mis-eventos

// ADMINISTRADOR only
/admin/**, /usuarios/**

// CONSUMIDOR only
/compras/**
```

#### Authentication Flow

1. **Login:** `/login` → Form-based authentication
2. **Success Handler:** Custom handler redirects based on highest role:
   - `ADMINISTRADOR` → `/admin/dashboard`
   - `PROVEEDOR` → `/proveedor/dashboard`
   - `CONSUMIDOR` → `/consumidor/dashboard`
3. **Logout:** `/logout` → Redirect to `/login?logout`

#### Password Security

- **Encoder:** `BCryptPasswordEncoder` (Bean configured in SecurityConfig)
- **Hashing:** Passwords hashed during user creation in `UsuarioImplement`
- **Validation:** Handled by `CustomUserDetailsService` (loads user by email)

#### User Details Service

**File:** `src/main/java/com/example/campolibre/Implement/CustomUserDetailsService.java`

- Implements `UserDetailsService`
- Loads user by email
- Converts `UsuarioRol` to Spring Security `GrantedAuthority`
- Checks user status (ACTIVO/ELIMINADO)

### Multi-Role Support

Users can have multiple roles:
- **Registration:** Users automatically get `CONSUMIDOR` role
- **Provider Registration:** Users get both `CONSUMIDOR` + `PROVEEDOR`
- **Admin Creation:** Can assign any combination of roles

**Role Hierarchy:** ADMINISTRADOR > PROVEEDOR > CONSUMIDOR

---

## Development Workflow

### Initial Setup

1. **Prerequisites:**
   - Java 21 JDK
   - MySQL 8 (running on port 3307, or change in `application.properties`)
   - Maven (or use wrapper: `mvnw`)

2. **Database Setup:**
   ```sql
   -- MySQL will auto-create database on first run
   -- Default: jdbc:mysql://localhost:3307/new_campolibre?createDatabaseIfNotExist=true
   ```

3. **Configuration:**
   - Edit `src/main/resources/application.properties`
   - Update database credentials if needed
   - **IMPORTANT:** Change email password or use environment variable

4. **Build & Run:**
   ```bash
   # Using Maven wrapper (recommended)
   ./mvnw clean install
   ./mvnw spring-boot:run

   # Or using Maven directly
   mvn clean install
   mvn spring-boot:run

   # Or run JAR directly
   java -jar target/campolibre-0.0.1-SNAPSHOT.jar
   ```

5. **Access Application:**
   - URL: `http://localhost:8080`
   - Default admin user:
     - Email: `admin@campolibre.com`
     - Password: `admin123` (created by DataLoader)

### Development Commands

```bash
# Clean build
./mvnw clean

# Compile
./mvnw compile

# Run tests
./mvnw test

# Package (creates JAR)
./mvnw package

# Run with hot reload (DevTools enabled)
./mvnw spring-boot:run

# Skip tests during build
./mvnw clean install -DskipTests

# Run specific test
./mvnw test -Dtest=NewCampoLibreApplicationTests
```

### Hot Reload

Spring Boot DevTools is enabled for hot reload:
- Java file changes trigger automatic restart
- Template changes (`.html`) reload without restart
- CSS/JS changes reload without restart
- Disable Thymeleaf cache in dev: `spring.thymeleaf.cache=false`

### Database Schema Updates

- **Mode:** `spring.jpa.hibernate.ddl-auto=update`
- **Behavior:** Hibernate automatically updates schema on entity changes
- **WARNING:** Use migrations (Flyway/Liquibase) for production

---

## Coding Conventions

### Java Conventions

1. **Package Organization:**
   ```
   com.example.campolibre
   ├── Config/         # @Configuration classes
   ├── Controller/     # @Controller classes
   ├── Service/        # Service interfaces
   ├── Implement/      # Service implementations (@Service)
   ├── Repository/     # @Repository interfaces
   ├── Entity/         # @Entity classes
   ├── DTO/            # Data Transfer Objects
   ├── Enum/           # Enumerations
   └── Exception/      # Custom exceptions
   ```

2. **Naming Conventions:**
   - **Classes:** PascalCase (e.g., `UsuarioController`)
   - **Methods:** camelCase (e.g., `crearUsuario()`)
   - **Variables:** camelCase (e.g., `usuarioDTO`)
   - **Constants:** UPPER_SNAKE_CASE (e.g., `MAX_FILE_SIZE`)
   - **Database columns:** snake_case (e.g., `id_usuario`, `fecha_creacion`)

3. **Lombok Annotations:**
   ```java
   @Data                    // Generates getters, setters, toString, equals, hashCode
   @AllArgsConstructor      // Constructor with all fields
   @NoArgsConstructor       // No-arg constructor (required by JPA)
   @Builder                 // Builder pattern (use sparingly)
   ```

4. **Dependency Injection:**
   ```java
   // PREFERRED: Constructor injection
   @Service
   public class UsuarioImplement implements UsuarioService {
       private final UsuarioRepository usuarioRepository;

       public UsuarioImplement(UsuarioRepository usuarioRepository) {
           this.usuarioRepository = usuarioRepository;
       }
   }

   // ACCEPTABLE: Field injection (already used in codebase)
   @Autowired
   private UsuarioRepository usuarioRepository;
   ```

5. **Service Layer Pattern:**
   ```java
   // Interface in Service/
   public interface UsuarioService {
       UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO);
       List<UsuarioDTO> listarUsuarios();
   }

   // Implementation in Implement/
   @Service
   public class UsuarioImplement implements UsuarioService {
       @Override
       public UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO) {
           // Business logic here
       }
   }
   ```

6. **Exception Handling:**
   ```java
   // Use CustomException for business logic errors
   throw new CustomException("Email ya está registrado");

   // Catch in controllers and show flash messages
   try {
       usuarioService.crearUsuario(usuarioDTO);
   } catch (CustomException e) {
       redirectAttributes.addFlashAttribute("error", e.getMessage());
       return "redirect:/usuarios/form";
   }
   ```

### Controller Conventions

1. **Controller Structure:**
   ```java
   @Controller
   @RequestMapping("/usuarios")  // Base path at class level
   public class UsuarioController {

       @Autowired
       private UsuarioService usuarioService;

       @GetMapping("/list")  // GET /usuarios/list
       public String listar(Model model) {
           model.addAttribute("usuarios", usuarioService.listarUsuarios());
           return "usuario/list";  // templates/usuario/list.html
       }

       @PostMapping("/save")  // POST /usuarios/save
       public String guardar(@ModelAttribute UsuarioDTO usuarioDTO,
                            RedirectAttributes redirectAttributes) {
           try {
               usuarioService.crearUsuario(usuarioDTO);
               redirectAttributes.addFlashAttribute("success", "Usuario creado");
           } catch (CustomException e) {
               redirectAttributes.addFlashAttribute("error", e.getMessage());
           }
           return "redirect:/usuarios/list";
       }
   }
   ```

2. **Flash Messages:**
   ```java
   // Success message
   redirectAttributes.addFlashAttribute("success", "Operación exitosa");

   // Error message
   redirectAttributes.addFlashAttribute("error", "Error al procesar");

   // Display in Thymeleaf
   <div th:if="${success}" class="alert alert-success" th:text="${success}"></div>
   <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
   ```

### Entity Conventions

1. **Entity Structure:**
   ```java
   @Entity
   @Table(name = "usuario")
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public class Usuario {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id_usuario;

       @Column(nullable = false, unique = true)
       private String email;

       @Column(nullable = false)
       private String contrasena;  // Hashed with BCrypt

       @Enumerated(EnumType.STRING)
       private EstadoUsuario estado;

       @Column(name = "fecha_registro")
       private LocalDateTime fechaRegistro;

       @PrePersist
       protected void onCreate() {
           fechaRegistro = LocalDateTime.now();
       }

       // Relationships
       @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
       private List<Tienda> tiendas;
   }
   ```

2. **Relationship Conventions:**
   - Use `mappedBy` on the inverse side of bidirectional relationships
   - Use `CascadeType.ALL` for parent-child relationships (e.g., Pedido → ItemPedido)
   - Use `orphanRemoval=true` for owned entities
   - Use `@JoinColumn` to specify foreign key name
   - Avoid `FetchType.EAGER` unless necessary (causes N+1 queries)

### DTO Conventions

1. **DTO Structure:**
   ```java
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public class UsuarioDTO {
       private Long id_usuario;
       private String nombre;
       private String email;
       // Do NOT include 'contrasena' in DTO for security
       private String telefono;
       private LocalDateTime fechaRegistro;
   }
   ```

2. **Entity-DTO Mapping:**
   ```java
   // Using ModelMapper (configured in AppConfig)
   @Autowired
   private ModelMapper modelMapper;

   // Entity to DTO
   UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);

   // DTO to Entity
   Usuario usuario = modelMapper.map(usuarioDTO, Usuario.class);
   ```

### Repository Conventions

1. **Repository Structure:**
   ```java
   @Repository
   public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

       // Spring Data JPA generates implementation automatically
       Optional<Usuario> findByEmail(String email);

       List<Usuario> findByEstado(EstadoUsuario estado);

       // Custom query
       @Query("SELECT u FROM Usuario u WHERE u.estado = 'ACTIVO' ORDER BY u.fechaRegistro DESC")
       List<Usuario> findAllActivos();

       // Native query
       @Query(value = "SELECT * FROM usuario WHERE email = ?1", nativeQuery = true)
       Usuario findByEmailNative(String email);

       // Modifying query (UPDATE/DELETE)
       @Modifying
       @Transactional
       @Query("UPDATE Usuario u SET u.estado = 'ELIMINADO' WHERE u.id_usuario = ?1")
       void softDelete(Long id);
   }
   ```

2. **Query Method Naming:**
   - `findBy{Property}` - Single result or list
   - `findAllBy{Property}` - List of results
   - `countBy{Property}` - Count
   - `deleteBy{Property}` - Delete
   - `existsBy{Property}` - Boolean check

### Thymeleaf Conventions

1. **Template Structure:**
   ```html
   <!DOCTYPE html>
   <html xmlns:th="http://www.thymeleaf.org"
         xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
   <head>
       <title th:text="${titulo}">Título</title>
       <link rel="stylesheet" th:href="@{/css/base.css}">
   </head>
   <body>
       <!-- Header fragment -->
       <div th:replace="~{fragments/header :: header}"></div>

       <!-- Flash messages -->
       <div th:if="${success}" class="alert alert-success" th:text="${success}"></div>

       <!-- Content -->
       <div class="container">
           <h1 th:text="${titulo}">Título</h1>

           <!-- Loop through list -->
           <div th:each="usuario : ${usuarios}">
               <span th:text="${usuario.nombre}">Nombre</span>
           </div>

           <!-- Conditional rendering -->
           <div th:if="${usuario.estado == 'ACTIVO'}">
               Usuario activo
           </div>

           <!-- Security -->
           <div sec:authorize="hasAuthority('ADMINISTRADOR')">
               Admin only content
           </div>

           <!-- Links -->
           <a th:href="@{/usuarios/edit/{id}(id=${usuario.id_usuario})}">Editar</a>
       </div>

       <!-- Footer fragment -->
       <div th:replace="~{fragments/footer :: footer}"></div>
   </body>
   </html>
   ```

2. **Form Conventions:**
   ```html
   <form th:action="@{/usuarios/save}" method="post" th:object="${usuarioDTO}">
       <input type="hidden" th:field="*{id_usuario}">

       <div class="form-group">
           <label for="nombre">Nombre:</label>
           <input type="text" class="form-control" th:field="*{nombre}" required>
       </div>

       <div class="form-group">
           <label for="email">Email:</label>
           <input type="email" class="form-control" th:field="*{email}" required>
       </div>

       <button type="submit" class="btn btn-primary">Guardar</button>
   </form>
   ```

---

## Testing Guidelines

### Current Testing State

- **Test Coverage:** Minimal (only basic application startup test)
- **Test File:** `src/test/java/com/example/campolibre/NewCampoLibreApplicationTests.java`

### Testing Recommendations for AI Assistants

When adding new features, consider adding tests:

1. **Unit Tests (Service Layer):**
   ```java
   @ExtendWith(MockitoExtension.class)
   class UsuarioServiceTest {

       @Mock
       private UsuarioRepository usuarioRepository;

       @Mock
       private ModelMapper modelMapper;

       @InjectMocks
       private UsuarioImplement usuarioService;

       @Test
       void testCrearUsuario() {
           // Given
           UsuarioDTO dto = new UsuarioDTO();
           dto.setEmail("test@example.com");

           Usuario usuario = new Usuario();
           when(modelMapper.map(dto, Usuario.class)).thenReturn(usuario);
           when(usuarioRepository.save(any())).thenReturn(usuario);

           // When
           UsuarioDTO result = usuarioService.crearUsuario(dto);

           // Then
           assertNotNull(result);
           verify(usuarioRepository).save(any());
       }
   }
   ```

2. **Integration Tests (Repository Layer):**
   ```java
   @DataJpaTest
   class UsuarioRepositoryTest {

       @Autowired
       private UsuarioRepository usuarioRepository;

       @Test
       void testFindByEmail() {
           // Given
           Usuario usuario = new Usuario();
           usuario.setEmail("test@example.com");
           usuarioRepository.save(usuario);

           // When
           Optional<Usuario> found = usuarioRepository.findByEmail("test@example.com");

           // Then
           assertTrue(found.isPresent());
           assertEquals("test@example.com", found.get().getEmail());
       }
   }
   ```

3. **Controller Tests:**
   ```java
   @WebMvcTest(UsuarioController.class)
   class UsuarioControllerTest {

       @Autowired
       private MockMvc mockMvc;

       @MockBean
       private UsuarioService usuarioService;

       @Test
       void testListarUsuarios() throws Exception {
           // Given
           List<UsuarioDTO> usuarios = Arrays.asList(new UsuarioDTO());
           when(usuarioService.listarUsuarios()).thenReturn(usuarios);

           // When & Then
           mockMvc.perform(get("/usuarios/list"))
               .andExpect(status().isOk())
               .andExpect(view().name("usuario/list"))
               .andExpect(model().attributeExists("usuarios"));
       }
   }
   ```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UsuarioServiceTest

# Run with coverage (requires jacoco plugin)
./mvnw clean test jacoco:report
```

---

## File Upload System

### Configuration

**File:** `src/main/resources/application.properties`
```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
upload.path=uploads/
```

**File:** `src/main/java/com/example/campolibre/Config/FileStorageConfig.java`
- Configures absolute upload path
- Creates directories if not exist

**File:** `src/main/java/com/example/campolibre/Config/WebConfig.java`
- Maps `/uploads/**` to file system path

### Usage in Services

**File:** `src/main/java/com/example/campolibre/Service/FileStorageService.java`

```java
// Save file
String fileName = fileStorageService.saveFile(multipartFile, "productos");
// Returns: unique filename (UUID-based)
// Saves to: uploads/productos/{filename}

// Delete file
fileStorageService.deleteFile(fileName, "productos");
// Deletes: uploads/productos/{filename}
```

### Upload Directories

- `uploads/eventos/` - Event images
- `uploads/productos/` - Product images
- `uploads/tiendas/` - Store logos/images

### Security Considerations

- **Max file size:** 10MB (configured)
- **File type validation:** NOT IMPLEMENTED (add validation in service)
- **Path traversal:** Protected by using UUID filenames
- **Access control:** Static files served publicly at `/uploads/**`

### Recommended Improvements

1. Add file type validation (images only)
2. Add virus scanning
3. Add file size validation before upload
4. Consider cloud storage (AWS S3, Google Cloud Storage)

---

## Email Integration

### Configuration

**File:** `src/main/resources/application.properties`
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=librecampo1@gmail.com
spring.mail.password=vkdx kcbg andj hnnk  # SECURITY RISK: Use environment variable
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**CRITICAL:** Email password is exposed in plain text. Should use:
```properties
spring.mail.password=${MAIL_PASSWORD}
```

### Email Service

**File:** `src/main/java/com/example/campolibre/Service/EmailService.java`

```java
@Service
public class EmailService {

    @Async  // Non-blocking email sending
    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        // Send email using JavaMailSender
    }

    @Async
    public void enviarCorreoConfirmacionCuenta(String destinatario, String nombreUsuario) {
        // Welcome email
    }

    @Async
    public void enviarCorreoConfirmacionEvento(String destinatario, String nombreEvento) {
        // Event registration confirmation
    }

    @Async
    public void enviarCorreoInvitacionEvento(String destinatario, String nombreEvento) {
        // Event invitation
    }
}
```

### Usage Examples

```java
// Send account creation confirmation
emailService.enviarCorreoConfirmacionCuenta(usuario.getEmail(), usuario.getNombre());

// Send event registration confirmation
emailService.enviarCorreoConfirmacionEvento(usuario.getEmail(), evento.getNombre());

// Send custom email
emailService.enviarCorreo("user@example.com", "Subject", "Email body");
```

### Async Configuration

- Emails are sent asynchronously using `@Async`
- Does not block request processing
- Configured with `@EnableAsync` (check main application class)

---

## Document Generation

### PDF Generation

**Library:** Flying Saucer (HTML to PDF) + iText

**File:** `src/main/java/com/example/campolibre/Service/PdfService.java`

```java
@Service
public class PdfService {

    public ByteArrayInputStream generarPqrsReportePdf(List<PqrsReporteItemDTO> items) {
        // 1. Render Thymeleaf template to HTML
        Context context = new Context();
        context.setVariable("items", items);
        String html = templateEngine.process("reportes/pqrs-reporte-pdf", context);

        // 2. Convert HTML to PDF using Flying Saucer
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
```

**Template:** `src/main/resources/templates/reportes/pqrs-reporte-pdf.html`

### Excel Generation

**Library:** Apache POI 5.2.3

**Usage Pattern:**
```java
// Create workbook
Workbook workbook = new XSSFWorkbook();
Sheet sheet = workbook.createSheet("Datos");

// Create header row
Row headerRow = sheet.createRow(0);
headerRow.createCell(0).setCellValue("ID");
headerRow.createCell(1).setCellValue("Nombre");

// Add data rows
int rowNum = 1;
for (Item item : items) {
    Row row = sheet.createRow(rowNum++);
    row.createCell(0).setCellValue(item.getId());
    row.createCell(1).setCellValue(item.getNombre());
}

// Write to output stream
ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
workbook.write(outputStream);
workbook.close();

return new ByteArrayInputStream(outputStream.toByteArray());
```

### Download Controller Pattern

```java
@GetMapping("/download/pdf")
public ResponseEntity<byte[]> downloadPdf() {
    ByteArrayInputStream pdf = pdfService.generarReporte();

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=reporte.pdf");

    return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf.readAllBytes());
}
```

---

## Common Tasks

### Adding a New Entity

1. **Create Entity Class** (`Entity/NuevaEntidad.java`):
   ```java
   @Entity
   @Table(name = "nueva_entidad")
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public class NuevaEntidad {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @Column(nullable = false)
       private String nombre;

       @Column(name = "fecha_creacion")
       private LocalDateTime fechaCreacion;

       @PrePersist
       protected void onCreate() {
           fechaCreacion = LocalDateTime.now();
       }
   }
   ```

2. **Create Repository** (`Repository/NuevaEntidadRepository.java`):
   ```java
   @Repository
   public interface NuevaEntidadRepository extends JpaRepository<NuevaEntidad, Long> {
       List<NuevaEntidad> findByNombre(String nombre);
   }
   ```

3. **Create DTO** (`DTO/NuevaEntidadDTO.java`):
   ```java
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public class NuevaEntidadDTO {
       private Long id;
       private String nombre;
       private LocalDateTime fechaCreacion;
   }
   ```

4. **Create Service Interface** (`Service/NuevaEntidadService.java`):
   ```java
   public interface NuevaEntidadService {
       NuevaEntidadDTO crear(NuevaEntidadDTO dto);
       List<NuevaEntidadDTO> listar();
       NuevaEntidadDTO obtenerPorId(Long id);
   }
   ```

5. **Create Service Implementation** (`Implement/NuevaEntidadImplement.java`):
   ```java
   @Service
   public class NuevaEntidadImplement implements NuevaEntidadService {
       @Autowired
       private NuevaEntidadRepository repository;

       @Autowired
       private ModelMapper modelMapper;

       @Override
       public NuevaEntidadDTO crear(NuevaEntidadDTO dto) {
           NuevaEntidad entidad = modelMapper.map(dto, NuevaEntidad.class);
           NuevaEntidad guardada = repository.save(entidad);
           return modelMapper.map(guardada, NuevaEntidadDTO.class);
       }

       @Override
       public List<NuevaEntidadDTO> listar() {
           return repository.findAll().stream()
               .map(e -> modelMapper.map(e, NuevaEntidadDTO.class))
               .collect(Collectors.toList());
       }
   }
   ```

6. **Create Controller** (`Controller/NuevaEntidadController.java`):
   ```java
   @Controller
   @RequestMapping("/nueva-entidad")
   public class NuevaEntidadController {
       @Autowired
       private NuevaEntidadService service;

       @GetMapping("/list")
       public String listar(Model model) {
           model.addAttribute("items", service.listar());
           return "nueva-entidad/list";
       }

       @PostMapping("/save")
       public String guardar(@ModelAttribute NuevaEntidadDTO dto,
                            RedirectAttributes redirectAttributes) {
           service.crear(dto);
           redirectAttributes.addFlashAttribute("success", "Creado exitosamente");
           return "redirect:/nueva-entidad/list";
       }
   }
   ```

7. **Create Templates** (`templates/nueva-entidad/`):
   - `list.html` - List view
   - `form.html` - Create/edit form
   - `view.html` - Detail view

8. **Update Security Configuration** (if needed):
   ```java
   .requestMatchers("/nueva-entidad/**").hasAuthority("ADMINISTRADOR")
   ```

### Adding a New Feature

1. **Understand Requirements:**
   - Which entities are involved?
   - Which roles have access?
   - What business rules apply?

2. **Design Database Schema:**
   - Add new entity or modify existing
   - Define relationships
   - Add enums if needed

3. **Implement Backend:**
   - Entity → Repository → Service → Controller
   - Add validation and error handling
   - Write tests (recommended)

4. **Implement Frontend:**
   - Create Thymeleaf templates
   - Add CSS styling
   - Add form validation

5. **Update Security:**
   - Configure role-based access
   - Update SecurityConfig if needed

6. **Test Thoroughly:**
   - Manual testing with different roles
   - Edge cases and error scenarios

### Modifying Existing Features

1. **Read Existing Code:**
   - Start with Controller to understand flow
   - Check Service for business logic
   - Review Entity for data model

2. **Make Changes:**
   - Entity changes → Update Repository queries if needed
   - Service changes → Update Controller if needed
   - Controller changes → Update Templates

3. **Test Changes:**
   - Verify existing functionality still works
   - Test new functionality

4. **Update Documentation:**
   - Update this CLAUDE.md if architectural changes
   - Add comments for complex logic

---

## Troubleshooting

### Common Issues

#### Database Connection Errors

**Error:** `Communications link failure`

**Solution:**
1. Check MySQL is running: `sudo systemctl status mysql`
2. Verify port (default: 3307 in this project)
3. Check credentials in `application.properties`
4. Ensure database exists (auto-creates with `createDatabaseIfNotExist=true`)

#### Port Already in Use

**Error:** `Port 8080 already in use`

**Solution:**
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

#### Lombok Not Working

**Error:** `Cannot resolve symbol 'getData'`

**Solution:**
1. Install Lombok plugin in IDE
2. Enable annotation processing:
   - IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
   - Eclipse: Install Lombok following official guide
3. Rebuild project: `./mvnw clean install`

#### Thymeleaf Template Not Found

**Error:** `TemplateInputException: Error resolving template`

**Solution:**
1. Check template path: `src/main/resources/templates/{path}.html`
2. Verify controller returns correct view name (without `.html`)
3. Check Thymeleaf config in `application.properties`:
   ```properties
   spring.thymeleaf.prefix=classpath:/templates/
   spring.thymeleaf.suffix=.html
   ```

#### Email Not Sending

**Error:** `Authentication failed` or `Timeout`

**Solution:**
1. Check Gmail allows "Less secure apps" or use App Password
2. Verify SMTP configuration in `application.properties`
3. Check firewall allows port 587
4. Test connection: `spring.mail.test-connection=true`

#### File Upload Fails

**Error:** `Maximum upload size exceeded`

**Solution:**
1. Increase max file size:
   ```properties
   spring.servlet.multipart.max-file-size=20MB
   spring.servlet.multipart.max-request-size=20MB
   ```
2. Check `uploads/` directory exists and is writable
3. Verify file path in WebConfig

#### Role-Based Access Not Working

**Error:** `Access Denied` or redirects to login

**Solution:**
1. Check user has correct role in `usuario_rol` table
2. Verify SecurityConfig has correct path mapping
3. Check role name matches exactly (case-sensitive)
4. Ensure user is authenticated (`SecurityContextHolder.getContext().getAuthentication()`)

### Debugging Tips

1. **Enable SQL Logging:**
   ```properties
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   ```

2. **Enable Debug Logging:**
   ```properties
   logging.level.com.example.campolibre=DEBUG
   logging.level.org.springframework.security=DEBUG
   ```

3. **Check Logs:**
   - Spring Boot logs to console by default
   - Add file logging:
     ```properties
     logging.file.name=campolibre.log
     ```

4. **Database Inspection:**
   ```sql
   -- Check user roles
   SELECT u.email, r.nombre
   FROM usuario u
   JOIN usuario_rol ur ON u.id_usuario = ur.usuario_id
   JOIN rol r ON ur.rol_id = r.id_rol;

   -- Check product stock
   SELECT nombre, stock FROM producto WHERE stock < 10;

   -- Check order status
   SELECT numero_pedido, estado, total FROM pedido ORDER BY fecha_pedido DESC;
   ```

---

## Security Considerations

### Current Security Strengths

1. **Password Hashing:** BCrypt encryption for passwords
2. **Role-Based Access Control:** Spring Security with authority checks
3. **CSRF Protection:** Enabled by default in Spring Security
4. **Session Management:** Spring Security handles sessions
5. **SQL Injection Protection:** JPA/Hibernate parameterized queries

### Critical Security Issues

#### 1. Email Password Exposure

**Issue:** Email password hardcoded in `application.properties:48`

**Fix:**
```properties
# Use environment variable
spring.mail.password=${MAIL_PASSWORD}
```

Set environment variable:
```bash
# Linux/Mac
export MAIL_PASSWORD="your_app_password"

# Windows (PowerShell)
$env:MAIL_PASSWORD="your_app_password"

# Or use .env file with Spring Boot
```

#### 2. Database Password Exposure

**Issue:** Database credentials in `application.properties`

**Fix:**
```properties
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
```

#### 3. No Input Validation

**Issue:** No `@Valid` or validation annotations on DTOs

**Fix:**
```java
// Add to DTO
@NotBlank(message = "Nombre es requerido")
@Size(min = 3, max = 100)
private String nombre;

@Email(message = "Email inválido")
private String email;

// Add to controller
@PostMapping("/save")
public String guardar(@Valid @ModelAttribute UsuarioDTO dto,
                     BindingResult result,
                     RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        return "usuario/form";
    }
    // ...
}
```

#### 4. No File Type Validation

**Issue:** File upload accepts any file type

**Fix:**
```java
// In FileStorageService
private static final List<String> ALLOWED_EXTENSIONS =
    Arrays.asList("jpg", "jpeg", "png", "gif");

public String saveFile(MultipartFile file, String directory) {
    String extension = getFileExtension(file.getOriginalFilename());
    if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
        throw new CustomException("Tipo de archivo no permitido");
    }
    // ... rest of save logic
}
```

#### 5. No Rate Limiting

**Issue:** No protection against brute force attacks

**Recommendation:** Add Spring Security rate limiting or use library like Bucket4j

#### 6. No HTTPS Configuration

**Issue:** Application runs on HTTP (port 8080)

**Fix (Production):**
```properties
# application.properties
server.port=8443
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

### Security Best Practices for AI Assistants

When adding new features:

1. **Always validate user input:**
   - Use `@Valid` and validation annotations
   - Sanitize HTML input to prevent XSS
   - Use prepared statements (JPA handles this)

2. **Check authorization:**
   - Verify user has permission to access resource
   - Don't rely only on SecurityConfig - check in service layer too
   ```java
   // In service method
   Usuario currentUser = getCurrentUser();
   if (!tienda.getUsuario().equals(currentUser) &&
       !isAdmin(currentUser)) {
       throw new AccessDeniedException("No autorizado");
   }
   ```

3. **Never expose sensitive data:**
   - Exclude passwords from DTOs
   - Don't log sensitive information
   - Mask email/phone in logs

4. **Use HTTPS in production:**
   - Configure SSL certificate
   - Redirect HTTP to HTTPS

5. **Keep dependencies updated:**
   - Run `./mvnw versions:display-dependency-updates`
   - Update Spring Boot and libraries regularly

6. **Add security headers:**
   ```java
   // In SecurityConfig
   http.headers()
       .contentSecurityPolicy("default-src 'self'")
       .and()
       .frameOptions().deny()
       .xssProtection().block(true);
   ```

---

## Additional Resources

### Official Documentation

- **Spring Boot:** https://spring.io/projects/spring-boot
- **Spring Security:** https://spring.io/projects/spring-security
- **Spring Data JPA:** https://spring.io/projects/spring-data-jpa
- **Thymeleaf:** https://www.thymeleaf.org/documentation.html
- **Hibernate:** https://hibernate.org/orm/documentation/

### Useful Commands Reference

```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Test
./mvnw test

# Package
./mvnw package

# Skip tests
./mvnw clean install -DskipTests

# Check dependency updates
./mvnw versions:display-dependency-updates

# Generate project info
./mvnw site
```

### Database Backup

```bash
# Export database
mysqldump -u root -p --port=3307 new_campolibre > backup.sql

# Import database
mysql -u root -p --port=3307 new_campolibre < backup.sql
```

---

## Appendix: Key Files Reference

### Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven dependencies and build configuration |
| `application.properties` | Application configuration (DB, email, upload) |
| `SecurityConfig.java` | Spring Security configuration |
| `AppConfig.java` | Bean configuration (ModelMapper) |
| `WebConfig.java` | Static resource mapping |
| `FileStorageConfig.java` | File upload configuration |
| `DataLoader.java` | Initial data (roles, admin user) |
| `DataSeeder.java` | Sample data seeding |

### Core Entities

| Entity | Table | Primary Key | Description |
|--------|-------|-------------|-------------|
| Usuario | usuario | id_usuario | User accounts |
| Rol | rol | id_rol | User roles |
| UsuarioRol | usuario_rol | id_usuario_rol | User-role junction |
| Tienda | tienda | id_tienda | Stores/shops |
| Producto | producto | id_producto | Products |
| CarritoCompra | carrito_compra | id_carrito | Shopping carts |
| ItemCarrito | item_carrito | id_item_carrito | Cart items |
| Pedido | pedido | id_pedido | Orders |
| ItemPedido | item_pedido | id_item_pedido | Order items |
| Pago | pago | id_pago | Payments |
| Evento | evento | id_evento | Events |
| MisEventos | mis_eventos | id_mis_eventos | Event registrations |
| Pqrs | pqrs | id_pqrs | Complaints/requests |
| PqrsRespuesta | pqrs_respuesta | id_respuesta | PQRS responses |

### Controllers & Routes

| Controller | Base Path | Main Routes |
|------------|-----------|-------------|
| HomeController | `/` | /, /login, /register, /admin/dashboard, /proveedor/dashboard, /consumidor/dashboard |
| UsuarioController | `/usuarios` | /list, /form, /save, /edit/{id}, /delete/{id} |
| TiendaController | `/tiendas` | /list, /form, /save, /edit/{id}, /view/{id} |
| ProductoController | `/productos` | /list, /form, /save, /edit/{id}, /view/{id} |
| EventoController | `/eventos` | /list, /crear, /save, /edit/{id}, /aprobar/{id} |
| CarritoController | `/carrito` | /view, /agregar, /eliminar/{id} |
| PedidoController | `/pedidos` | /confirmar, /mis-pedidos, /ventas |
| PagoController | `/pago` | /checkout/{id}, /exitoso, /fallido |
| PqrsController | `/pqrs` | /list, /form, /save, /responder/{id} |

---

## Changelog

### 2025-11-14 - Initial Creation
- Created comprehensive CLAUDE.md documentation
- Documented project structure, architecture, and conventions
- Added security considerations and troubleshooting guide
- Documented all major components and workflows

---

**End of CLAUDE.md**

For questions or updates, please refer to the project repository or contact the development team.
