## Test Plan para Spring Security Filters

### URLs de Prueba

#### 1. **Rutas Públicas (Sin Autenticación Requerida)**
- ✅ GET `http://localhost:8080/` → Debe mostrar página de inicio
- ✅ GET `http://localhost:8080/login` → Formulario de login
- ✅ GET `http://localhost:8080/register` → Formulario de registro
- ✅ GET `http://localhost:8080/css/styles.css` → CSS estático

#### 2. **Usuario No Autenticado - Debe Redirigir a /login**
- GET `http://localhost:8080/profile` → Debe redirigir a /login
- GET `http://localhost:8080/discotecas` → Debe redirigir a /login
- GET `http://localhost:8080/eventos` → Debe redirigir a /login
- GET `http://localhost:8080/admin` → Debe redirigir a /login

#### 3. **Usuario Autenticado (No Admin) - Debe Redirigir a /**
- Login: admin@example.com / admin (será admin)
- GET `http://localhost:8080/discotecas/create-discotecas` → Debe redirigir a /
- GET `http://localhost:8080/discotecas/edit-discoteca/1` → Debe redirigir a /
- GET `http://localhost:8080/admin` → Debe redirigir a /
- GET `http://localhost:8080/eventos/create-event` → Debe redirigir a /

#### 4. **Usuario Admin - Acceso Permitido**
- Login: admin@example.com / admin
- GET `http://localhost:8080/admin` → Debe mostrar lista de usuarios
- GET `http://localhost:8080/discotecas/create-discotecas` → Debe mostrar formulario
- GET `http://localhost:8080/discotecas` → Debe mostrar lista con botones de admin

### Usuarios de Prueba Disponibles

1. **Admin**
   - Email: admin@example.com
   - Password: admin
   - Rol: Administrador

2. **Usuario Normal 1**
   - Email: juan@example.com
   - Password: 12345
   - Rol: Usuario

3. **Usuario Normal 2**
   - Email: maria@example.com
   - Password: password123
   - Rol: Usuario

### Pasos para Probar

#### Paso 1: Acceder a Aplicación Sin Login
1. Abre `http://localhost:8080/admin` (sin estar logueado)
2. Verifica: Debe redirigir a /login (AuthenticationFilter)

#### Paso 2: Login con Usuario Normal
1. Ve a `http://localhost:8080/login`
2. Ingresa: juan@example.com / 12345
3. Luego accede a `http://localhost:8080/admin`
4. Verifica: Debe redirigir a / (AdminFilter)

#### Paso 3: Login con Admin
1. Ve a `http://localhost:8080/login`
2. Ingresa: admin@example.com / admin
3. Accede a `http://localhost:8080/admin`
4. Verifica: Debe mostrar página de administración

#### Paso 4: Intentar Editar Discoteca como No-Admin
1. Con usuario juan logueado
2. Intenta: `http://localhost:8080/discotecas/edit-discoteca/1`
3. Verifica: Debe redirigir a /

#### Paso 5: URL Manipulation
1. Intenta acceder a rutas admin sin cambiar de usuario
2. Usa: `http://localhost:8080/discotecas/edit/1` (directo)
3. Verifica: El filtro debe interceptar y redirigir

### Filtros Configurados

**AuthenticationFilter (Orden 0)** - Se ejecuta primero
- Verifica: ¿Está el usuario logueado?
- Si NO: Redirige a /login
- Rutas públicas permitidas: /, /login, /register, /logout, /css/*, /images/*, /js/*, /h2-console

**AdminFilter (Orden 1)** - Se ejecuta después
- Verifica: ¿Es el usuario admin?
- Si NO: Redirige a /
- Rutas protegidas:
  - /discotecas/create-discotecas
  - /discotecas/edit-discoteca/*
  - /discotecas/edit/*
  - /discotecas/delete/*
  - /discotecas/*/eventos/create
  - /eventos/create-event
  - /eventos/*/edit
  - /eventos/*/delete
  - /admin

### Comportamiento Esperado

✅ **Antes**: Los controladores tenían validaciones manuales que se podían evitar
✅ **Ahora**: Los filtros interceptan antes de que llegue al controlador
✅ **Seguridad**: No importa si el usuario manipula la URL, los filtros lo protegen

### Notas de Depuración

Si algo no funciona:
1. Verifica los logs de la aplicación: busca mensajes de los filtros
2. Abre DevTools (F12) → Network → busca redirecciones (HTTP 302)
3. Verifica que UserSession tenga los valores correctos después de login
4. Confirma que el valor `isAdmin` es true después de login como admin
