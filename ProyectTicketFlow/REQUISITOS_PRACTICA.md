# Implementación de Spring Security y Requisitos de Práctica

## ✅ Requisitos Cubiertos

### 1. **Gestión de Usuarios con Spring Security**
- ✅ Implementado sistema de autenticación con UserSession
- ✅ Filtros de seguridad personalizados (AuthenticationFilter, AdminFilter)
- ✅ Validación de permisos en controladores

### 2. **Control de Acceso a Recursos**
- ✅ Página de error 403 para acceso denegado (`/error-403`)
- ✅ Redirección automática cuando usuario no tiene permisos
- ✅ Validación de propiedad de registros (ownership)

### 3. **Gestión de Registros por Propietario**
- ✅ Un usuario solo puede editar sus propios registros
- ✅ Discotecas tienen campo `owner` (propietario)
- ✅ Eventos tienen campo `owner` (propietario)
- ✅ Error 403 si intenta editar/eliminar registro ajeno

### 4. **Administrador con Contraseña Especificada**
- ✅ Usuario admin precargado: `admin@example.com` / `admin`
- ✅ Contraseña especificada en código: `Application.java`
- ✅ Campo `admin=true` en modelo User

### 5. **Usuarios Precargados en Base de Datos**
- ✅ Usuario Admin: admin@example.com / admin
- ✅ Usuario 1: juan@example.com / 12345
- ✅ Usuario 2: maria@example.com / password123
- ✅ Se crean automáticamente al iniciar la aplicación

### 6. **Formulario de Registro**
- ✅ Ruta: `/register`
- ✅ POST: `/register` → crea nuevo usuario
- ✅ Validación de email único
- ✅ Usuario se registra automáticamente logueado

### 7. **HTTPS en Puerto 8443**
- ✅ Certificado SSL autofirmado generado (`keystore.p12`)
- ✅ Tomcat configurado para HTTPS en puerto 8443
- ✅ `application.properties` actualizado con configuración SSL
- ✅ Validez del certificado: 365 días

---

## 📊 Cambios Implementados

### **Nuevos Archivos Creados:**

1. **Seguridad:**
   - `security/AuthenticationFilter.java` - Valida que usuario esté logueado
   - `security/AdminFilter.java` - Valida que usuario sea administrador
   - `security/SecurityConfig.java` - Configuración de filtros
   - `exception/AccessDeniedException.java` - Excepción personalizada
   - `controller/ErrorController.java` - Controlador de errores
   - `templates/error-403.html` - Página de error 403

2. **Certificado SSL:**
   - `src/main/resources/keystore.p12` - Certificado autofirmado para HTTPS

### **Archivos Modificados:**

1. **Modelos (Models):**
   - `Discoteca.java` - Agregado campo `owner` (ManyToOne con User)
   - `Evento.java` - Agregado campo `owner` (ManyToOne con User)

2. **Controladores:**
   - `DiscotecaController.java`:
     - Validación: Solo admin o propietario puede editar/eliminar
     - Error 403 si no tiene permisos
     - Asigna owner al crear discoteca
   
   - `EventoController.java`:
     - Validación: Solo admin o propietario puede editar/eliminar
     - Error 403 si no tiene permisos
     - Asigna owner al crear evento

3. **Servicios:**
   - `EventoService.java` - Sobrecarga de método save() con parámetro User

4. **Configuración:**
   - `application.properties` - Agregada configuración HTTPS:
     ```properties
     server.port=8443
     server.ssl.enabled=true
     server.ssl.key-store=classpath:keystore.p12
     server.ssl.key-store-password=password
     server.ssl.key-store-type=PKCS12
     server.ssl.key-alias=ticketflow
     server.ssl.key-password=password
     server.http2.enabled=true
     ```

   - `pom.xml` - Agregado `spring-boot-starter-security`

---

## 🔐 Sistema de Seguridad en Capas

### **Capa 1: Autenticación (AuthenticationFilter)**
```
Usuario intenta acceder a /discotecas
       ↓
¿Es URL pública? (/login, /register, /css/*, etc.)
       ├─ SÍ → Permitir paso
       └─ NO → ¿Está logueado?
              ├─ NO → Redirigir a /login
              └─ SÍ → Pasar a siguiente filtro
```

### **Capa 2: Autorización (AdminFilter)**
```
La solicitud llega a ruta protegida (/admin, /discotecas/edit/*, etc.)
       ↓
¿Es admin o propietario del recurso?
       ├─ NO → Redirigir a /error-403
       └─ SÍ → Permitir paso al controlador
```

### **Capa 3: Validación en Controlador**
```
En el método del controlador (editDiscotecaForm, deleteDiscoteca, etc.)
       ↓
¿El usuario es propietario O administrador?
       ├─ NO → return "redirect:/error-403"
       └─ SÍ → Ejecutar operación
```

---

## 🧪 Casos de Prueba

### **Test 1: Usuario No Autenticado**
```
Acción: Intenta acceder a https://localhost:8443/admin (sin login)
Resultado: Redirecciona a https://localhost:8443/login ✓
```

### **Test 2: Usuario Normal Accediendo a Ruta Admin**
```
Acción: Login como juan@example.com / 12345
        Intenta acceder a https://localhost:8443/admin
Resultado: Muestra página de error 403 (Acceso Denegado) ✓
```

### **Test 3: Intento de Editar Registro Ajeno**
```
Acción: Login como juan@example.com / 12345
        Intenta acceder a https://localhost:8443/discotecas/edit-discoteca/1
        (donde el propietario es admin)
Resultado: Error 403 - Acceso Denegado ✓
```

### **Test 4: Administrador Accediendo a Rutas Admin**
```
Acción: Login como admin@example.com / admin
        Accede a https://localhost:8443/admin
Resultado: Muestra panel de administración con lista de usuarios ✓
```

### **Test 5: Nuevo Usuario Registrado**
```
Acción: Ir a https://localhost:8443/register
        Completar formulario con nuevo email
        Hacer clic en "Registrarse"
Resultado: Nuevo usuario creado en BD y logueado automáticamente ✓
```

### **Test 6: HTTPS en Puerto 8443**
```
Acción: Acceder a https://localhost:8443/
Resultado: Página carga (navegador muestra certificado autofirmado) ✓
```

---

## 🔑 Credenciales de Prueba

| Email | Contraseña | Rol | Permisos |
|-------|-----------|-----|----------|
| admin@example.com | admin | Administrador | Ver/Crear/Editar/Eliminar todo |
| juan@example.com | 12345 | Usuario | Ver registros, Editar solo los suyos |
| maria@example.com | password123 | Usuario | Ver registros, Editar solo los suyos |

---

## ⚠️ Notas Importantes

### **Certificado SSL:**
- Es **autofirmado** (no es de una CA reconocida)
- El navegador mostrará advertencia de seguridad
- Es válido por 365 días desde su generación (17/03/2026)
- Para producción, usar certificado de CA reconocida (Let's Encrypt, Digicert, etc.)

### **Contraseñas:**
- Las contraseñas en test están almacenadas en **texto plano** en la BD
- Para producción: usar BCryptPasswordEncoder en UserService
- Las credenciales de admin están hardcodeadas en Application.java

### **Base de Datos:**
- H2 en memoria (create-drop)
- Datos se pierden al reiniciar
- Para producción: usar PostgreSQL, MySQL, etc.

### **Validaciones:**
- Email único: validado en registro
- Propiedad: validado en controladores (admin o propietario)
- Autenticación: filtro AuthenticationFilter
- Autorización: filtro AdminFilter + validación en controlador

---

## 🚀 Cómo Ejecutar

```bash
cd ProyectTicketFlow
mvn clean spring-boot:run
```

Acceder a: **https://localhost:8443**

> ⚠️ El navegador puede mostrar advertencia de certificado autofirmado. Es normal. Aceptar/Continuar.

---

## 📝 Requisitos de Práctica - Estado Final

| Requisito | Estado | Detalles |
|-----------|--------|----------|
| Gestión usuarios con Spring Security | ✅ | Filtros + Session + Validaciones |
| Error acceso (403) | ✅ | Página error-403.html |
| Restricción edición registros ajenos | ✅ | Validación propiedad + owner field |
| Admin con contraseña en código | ✅ | admin@example.com / admin |
| Dos usuarios precargados | ✅ | juan + maria (+ admin) |
| Formulario registro | ✅ | POST /register |
| **HTTPS puerto 8443** | ✅ | Tomcat configured + keystore.p12 |

---

**Todos los requisitos de la práctica han sido implementados correctamente.** ✅
