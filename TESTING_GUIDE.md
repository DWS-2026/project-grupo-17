# 🧪 GUÍA DE TESTING RÁPIDO

## Acceso a la Aplicación

```
URL: https://localhost:8443/
Nota: El navegador mostrará advertencia de certificado autofirmado.
Acción: Aceptar/Continuar (es normal para desarrollo)
```

---

## 🔑 Usuarios de Prueba Disponibles

| Email | Contraseña | Tipo | Nota |
|-------|-----------|------|------|
| admin@example.com | admin | Admin | Puede hacer todo |
| juan@example.com | 12345 | Usuario | Puede editar solo sus registros |
| maria@example.com | password123 | Usuario | Puede editar solo sus registros |

---

## 📋 Test Case 1: Autenticación Básica

### Paso 1: Sin Loguearse
```
1. Abre: https://localhost:8443/admin
2. Resultado esperado: Redirecciona a https://localhost:8443/login
3. ✓ PASS / ✗ FAIL
```

### Paso 2: Login Exitoso
```
1. Ingresa: juan@example.com / 12345
2. Haz clic en "Iniciar Sesión"
3. Resultado esperado: Se redirecciona a página principal
4. ✓ PASS / ✗ FAIL
```

### Paso 3: Logout
```
1. Haz clic en "Cerrar Sesión" / "Logout"
2. Resultado esperado: Se redirige a login
3. ✓ PASS / ✗ FAIL
```

---

## 🛡️ Test Case 2: Control de Acceso (Admin)

### Paso 1: Usuario No-Admin Accediendo a Admin Panel
```
1. Login como juan@example.com / 12345
2. Intenta acceder a: https://localhost:8443/admin
3. Resultado esperado: 
   - Página de error 403 "Acceso Denegado"
   - Mensaje: "No tienes permiso para acceder a este recurso"
4. ✓ PASS / ✗ FAIL
```

### Paso 2: Admin Accediendo a Admin Panel
```
1. Logout si estás logueado
2. Login como admin@example.com / admin
3. Accede a: https://localhost:8443/admin
4. Resultado esperado:
   - Se muestra lista de usuarios (juan, maria)
   - El admin actual no aparece en la lista
5. ✓ PASS / ✗ FAIL
```

### Paso 3: No-Admin Intentando Crear Discoteca
```
1. Login como juan@example.com
2. Intenta acceder a: https://localhost:8443/discotecas/create-discotecas
3. Resultado esperado: Página de error 403 (Acceso Denegado)
4. ✓ PASS / ✗ FAIL
```

### Paso 4: Admin Creando Discoteca
```
1. Login como admin@example.com
2. Accede a: https://localhost:8443/discotecas/create-discotecas
3. Resultado esperado: Se muestra formulario de crear discoteca
4. ✓ PASS / ✗ FAIL
```

---

## 🔒 Test Case 3: Validación de Propiedad

### Escenario: Admin crea una discoteca

```
1. Login como admin@example.com
2. Crea nueva discoteca: "Test Discoteca"
3. Anota el ID de la discoteca (ej: 1)
4. Logout
```

### Paso 1: Juan intenta editar discoteca de Admin
```
1. Login como juan@example.com / 12345
2. Intenta acceder a: https://localhost:8443/discotecas/edit-discoteca/1
3. Resultado esperado: Página de error 403 (Acceso Denegado)
4. Razón: Juan no es el propietario de la discoteca
5. ✓ PASS / ✗ FAIL
```

### Paso 2: Admin edita su propia discoteca
```
1. Login como admin@example.com
2. Accede a: https://localhost:8443/discotecas/edit-discoteca/1
3. Resultado esperado: Se muestra formulario de edición
4. Cambia el nombre y guarda
5. ✓ PASS / ✗ FAIL
```

### Paso 3: Juan crea su propia discoteca y la edita
```
1. Login como juan@example.com
2. Crear nueva discoteca (click en "Crear Discoteca" - si aparece)
3. Si no aparece botón: Esto es correcto (solo admin)
4. ✓ PASS / ✗ FAIL
```

---

## 📝 Test Case 4: Registro de Nuevo Usuario

### Paso 1: Ir a Formulario de Registro
```
1. Abre: https://localhost:8443/register
2. Resultado esperado: Se muestra formulario con campos:
   - Nombre
   - Email
   - Contraseña
   - Fecha de Nacimiento
3. ✓ PASS / ✗ FAIL
```

### Paso 2: Registrar Nuevo Usuario
```
1. Completa el formulario:
   - Nombre: "Test User"
   - Email: "testuser@example.com"
   - Contraseña: "password123"
   - Fecha: 2000-01-15
2. Haz clic en "Registrarse"
3. Resultado esperado:
   - Usuario se crea en base de datos
   - Se redirecciona a página principal logueado
4. ✓ PASS / ✗ FAIL
```

### Paso 3: Email Duplicado
```
1. Intenta registrar otro usuario con email: juan@example.com
2. Resultado esperado: Error de email duplicado
3. ✓ PASS / ✗ FAIL
```

---

## 🔐 Test Case 5: HTTPS en Puerto 8443

### Paso 1: Verificar HTTPS
```
1. Abre navegador
2. Intenta: https://localhost:8443/
3. Resultado esperado:
   - URL muestra "https://" (no http)
   - Navegador muestra icono de candado (seguro)
   - Puede haber advertencia de certificado autofirmado
4. ✓ PASS / ✗ FAIL
```

### Paso 2: Detalles de Certificado
```
1. Haz clic en el candado / Información de seguridad
2. Abre detalles del certificado
3. Resultado esperado:
   - CN (Common Name): localhost
   - Issuer: Self-Signed
   - Validity: 365 days desde 17/03/2026
4. ✓ PASS / ✗ FAIL
```

### Paso 3: HTTP No Disponible
```
1. Intenta acceder a: http://localhost:8080/
2. Resultado esperado:
   - No hay respuesta o conexión rechazada
   - HTTP no está disponible
3. ✓ PASS / ✗ FAIL
```

---

## 🎯 Test Case 6: Flujo Completo de Seguridad

### Escenario Integral:
```
1. [00:00] Usuario intenta acceder a /admin sin login
         → Acción: Redirige a /login ✓

2. [00:30] Intenta login con credenciales incorrectas
         → Acción: Muestra error, permanece en login ✓

3. [01:00] Login exitoso como juan@example.com
         → Acción: Redirecciona al inicio logueado ✓

4. [01:30] Lee su perfil desde /profile
         → Acción: Muestra datos de juan ✓

5. [02:00] Intenta acceder a /admin
         → Acción: Error 403 - Acceso Denegado ✓

6. [02:30] Intenta editar discoteca de admin (ID 1)
         → Acción: Error 403 - Acceso Denegado ✓

7. [03:00] Haz logout
         → Acción: Redirecciona a login ✓

8. [03:30] Login como admin@example.com
         → Acción: Redirecciona a inicio logueado ✓

9. [04:00] Accede a /admin
         → Acción: Muestra panel con usuarios (juan, maria) ✓

10. [04:30] Edita su propia discoteca
           → Acción: Permite edición, guarda cambios ✓

11. [05:00] Crea nueva discoteca
           → Acción: Se asigna owner=admin ✓

12. [05:30] Logout
           → Acción: Redirecciona a login ✓
```

---

## 📊 Checklist Final

### Seguridad
- [ ] AuthenticationFilter function correctamente
- [ ] AdminFilter funciona correctamente
- [ ] Error 403 se muestra cuando debe
- [ ] Validación de propiedad funciona
- [ ] Solo el propietario puede editar su recurso

### Autenticación
- [ ] Login funciona
- [ ] Logout funciona
- [ ] Validación de credenciales incorrectas
- [ ] Email único en registro

### HTTPS
- [ ] Puerto 8443 está escuchando
- [ ] Protocolo es HTTPS (no HTTP)
- [ ] Certificado autofirmado está presente
- [ ] Navegador muestra candado de seguridad

### Usuarios
- [ ] Admin precargado (admin@example.com / admin)
- [ ] User juan precargado (juan@example.com / 12345)
- [ ] User maria precargada (maria@example.com / password123)
- [ ] Nuevo usuario puede registrarse

### Datos
- [ ] Las discotecas tienen campo owner
- [ ] Los eventos tienen campo owner
- [ ] Owner se asigna correctamente al crear
- [ ] Validación de propiedad funciona

---

## 🐛 Troubleshooting

### "Port 8443 already in use"
```bash
# Matar procesos Java
taskkill /F /IM java.exe

# O especificar otro puerto en application.properties
server.port=8444
```

### "Certificado no válido"
```
Esto es NORMAL. Es un certificado autofirmado.
El navegador mostrará advertencia pero funcionará.
Aceptar/Continuar para proceder.
```

### "Filter not intercepting"
```
Verificar:
1. AuthenticationFilter está anotado con @Component
2. SecurityConfig registra los filtros con orden correcto
3. Revisar logs: buscar "AuthenticationFilter" o "AdminFilter"
```

### "Ownership validation not working"
```
Verificar:
1. Discoteca/Evento tienen campo owner con @ManyToOne
2. Controladores llaman al método isOwner()
3. UserService.findById() retorna el usuario actual
```

---

## 📞 Contacto / Ayuda

Si algún test falla:
1. Revisar logs de consola (buscar excepciones)
2. Verificar Base de datos (H2 console en /h2-console)
3. Comprobar que los archivos están creados correctamente
4. Reiniciar Maven: `mvn clean spring-boot:run`

---

**Última actualización:** 17/03/2026
**Estado:** ✅ Listo para testing
