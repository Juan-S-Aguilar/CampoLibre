# 📦 Guía del Seeder de Datos - Campo Libre

## 🎯 Resumen

El proyecto Campo Libre cuenta con un **seeder completo y robusto** (`ComprehensiveDataSeeder.java`) que carga automáticamente datos de prueba para todos los módulos del sistema.

---

## ✨ Características del Seeder

### **Datos Creados:**

| Módulo | Cantidad | Detalles |
|--------|----------|----------|
| **Roles** | 3 | ADMINISTRADOR, PROVEEDOR, CONSUMIDOR |
| **Usuarios** | 19 | 2 admins, 10 proveedores, 7 consumidores |
| **Patrocinadores** | 7 | 5 activos, 2 inactivos |
| **Eventos** | 12 | 2 BORRADOR, 5 PUBLICADO, 2 EN_CURSO, 2 FINALIZADO, 1 CANCELADO |
| **Inscripciones** | 35+ | CONFIRMADAS, PENDIENTES, CANCELADAS |
| **Pagos** | 35+ | PSE, Tarjeta Crédito, Tarjeta Débito, Efectivo |
| **Tiendas** | 10 | 9 activas, 1 inactiva |
| **Productos** | 35+ | Distribuidos en todas las tiendas activas |
| **Mis Eventos** | 15+ | Eventos guardados por consumidores |
| **PQRS** | 3 | PENDIENTES y RESPONDIDAS |

---

## 🚀 Cómo Usar el Seeder

### **1. Ejecución Automática**

El seeder se ejecuta **automáticamente** al iniciar la aplicación Spring Boot si la base de datos está **vacía**.

```bash
mvn spring-boot:run
```

**Salida esperada:**
```
╔════════════════════════════════════════════════════════════╗
║   📦 INICIANDO CARGA DE DATOS DE PRUEBA - CAMPO LIBRE    ║
╚════════════════════════════════════════════════════════════╝

🔐 [1/9] Creando roles del sistema...
    ✓ 3 roles creados

👥 [2/9] Creando usuarios del sistema...
    ✓ 19 usuarios creados
      - 2 administradores
      - 10 proveedores
      - 7 consumidores

🏢 [3/9] Creando patrocinadores...
    ✓ 7 patrocinadores creados
      - 5 activos
      - 2 inactivos

📅 [4/9] Creando eventos...
    ✓ 12 eventos creados
      - BORRADOR: 2
      - PUBLICADO: 5
      - EN_CURSO: 2
      - FINALIZADO: 2
      - CANCELADO: 1

... (continúa)

╔════════════════════════════════════════════════════════════╗
║   ✅ DATOS DE PRUEBA CARGADOS EXITOSAMENTE               ║
╚════════════════════════════════════════════════════════════╝
```

---

### **2. Omisión del Seeder**

Si la base de datos **YA tiene datos**, el seeder se omite automáticamente:

```
ℹ️  Ya existen datos en la base de datos. Omitiendo seeder.
💡 Para limpiar los datos, ejecuta: DELETE FROM usuario; (y todas las tablas relacionadas)
```

---

## 🗑️ Cómo Limpiar Datos

Para volver a ejecutar el seeder, debes **limpiar la base de datos**:

### **Opción 1: SQL Manual (Recomendado)**

```sql
-- Orden correcto para evitar errores de FK
DELETE FROM pqrs_respuesta;
DELETE FROM pqrs_evento;
DELETE FROM pqrs_tienda;
DELETE FROM pqrs;
DELETE FROM mis_eventos;
DELETE FROM producto;
DELETE FROM tienda;
DELETE FROM pago_evento;
DELETE FROM inscripcion_proveedor;
DELETE FROM evento;
DELETE FROM patrocinador;
DELETE FROM usuario;
DELETE FROM rol;
```

### **Opción 2: Recrear Base de Datos**

```bash
# MySQL
mysql -u root -p
DROP DATABASE campolibre;
CREATE DATABASE campolibre;
```

### **Opción 3: Spring JPA (application.properties)**

**⚠️ CUIDADO: Esto borra TODOS los datos cada vez que inicias la app**

```properties
# SOLO PARA DESARROLLO - NO USAR EN PRODUCCIÓN
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## 👤 Usuarios de Prueba

### **Administradores**

| Email | Contraseña | Nombre |
|-------|------------|--------|
| `admin@campolibre.com` | `admin123` | Carlos Rodríguez |
| `admin2@campolibre.com` | `admin123` | María González |

### **Proveedores** (10 total)

| Email | Contraseña | Nombre |
|-------|------------|--------|
| `proveedor1@campolibre.com` | `proveedor123` | Juan Pablo Martínez |
| `proveedor2@campolibre.com` | `proveedor123` | Ana María López |
| `proveedor3@campolibre.com` | `proveedor123` | Pedro Sánchez |
| ... (hasta proveedor10) | `proveedor123` | ... |

### **Consumidores** (7 total)

| Email | Contraseña | Nombre |
|-------|------------|--------|
| `consumidor1@campolibre.com` | `consumidor123` | Laura Daniela Ruiz |
| `consumidor2@campolibre.com` | `consumidor123` | Miguel Ángel Hernández |
| ... (hasta consumidor7) | `consumidor123` | ... |

---

## 📊 Casos de Prueba Cubiertos

### **Módulo de Eventos**

✅ Eventos en diferentes estados (BORRADOR, PUBLICADO, EN_CURSO, FINALIZADO, CANCELADO)
✅ Eventos con cupos casi llenos vs disponibles
✅ Eventos con fechas próximas (para probar reembolsos)
✅ Eventos con fechas pasadas (finalizados)

### **Módulo de Inscripciones**

✅ Inscripciones CONFIRMADAS (con pago exitoso)
✅ Inscripciones PENDIENTES (sin pagar - botón "Completar Pago")
✅ Inscripciones CANCELADAS (pagos fallidos)

### **Módulo de Pagos**

✅ Pagos EXITOSOS con diferentes métodos (PSE, Tarjeta Crédito, Débito, Efectivo)
✅ Pagos PENDIENTES
✅ Pagos FALLIDOS con mensajes de error
✅ Detalles específicos de PSE (entidad bancaria, tipo persona, documento)

### **Módulo de Marketplace**

✅ Tiendas activas e inactivas
✅ Múltiples productos por tienda
✅ Diferentes categorías y subcategorías
✅ Variedad de precios y stocks

### **Módulo de Patrocinadores**

✅ Patrocinadores activos (pueden asignarse a eventos)
✅ Patrocinadores inactivos (para probar validaciones)

---

## 🔧 Configuración Avanzada

### **Personalizar Cantidad de Datos**

Edita el archivo `ComprehensiveDataSeeder.java` y modifica las listas:

```java
// Ejemplo: Agregar más proveedores
proveedores.add(crearUsuario(
    "proveedor11@campolibre.com", "proveedor123", "Nombre Nuevo",
    "2000000011", "3201234577", NombreRol.PROVEEDOR
));
```

### **Seeders Antiguos Deshabilitados**

Los seeders antiguos han sido renombrados:
- `DataLoader.java` → `DataLoader.java.OLD`
- `DataSeeder.java` → `DataSeeder.java.OLD`

Están disponibles como respaldo pero **NO se ejecutan**.

---

## 🐛 Solución de Problemas

### **Error: "Constraint violation"**

**Causa:** Intento de insertar datos duplicados.

**Solución:** Limpia la base de datos completamente antes de ejecutar el seeder.

### **Error: "No se encontró el rol ADMINISTRADOR"**

**Causa:** Los roles no se crearon correctamente.

**Solución:**
1. Verifica que la tabla `rol` existe
2. Ejecuta manualmente:
```sql
INSERT INTO rol (nombre_rol) VALUES ('ADMINISTRADOR'), ('PROVEEDOR'), ('CONSUMIDOR');
```

### **El seeder no se ejecuta**

**Causa:** Ya hay datos en la base.

**Solución:** Limpia la tabla `usuario` o toda la BD.

---

## 📝 Notas Importantes

1. **Idempotencia:** El seeder solo se ejecuta si `usuarioRepository.count() == 0`
2. **Orden de Ejecución:** `@Order(1)` asegura que se ejecute primero
3. **Consistencia:** El seeder verifica que los cupos de eventos coincidan con inscripciones confirmadas
4. **Datos Realistas:** Usa nombres colombianos, ciudades reales y productos agrícolas típicos

---

## 🎓 Aprendizajes y Buenas Prácticas

- **Relaciones Correctas:** Todos los datos están correctamente relacionados (FK)
- **Estados Realistas:** Los estados reflejan el flujo real del negocio
- **Variedad de Casos:** Cubre escenarios normales y excepcionales
- **Fácil Mantenimiento:** Código organizado por fases y bien comentado
- **Performance:** Usa `saveAll()` cuando es posible para optimizar inserts

---

## 📧 Soporte

Si encuentras problemas con el seeder, revisa:
1. Logs de Spring Boot (busca mensajes del seeder)
2. Tabla de roles (`SELECT * FROM rol;`)
3. Tabla de usuarios (`SELECT * FROM usuario;`)
4. Restricciones de FK en las tablas relacionadas

---

**¡Listo! El sistema ahora cuenta con datos de prueba completos y realistas.** 🎉
