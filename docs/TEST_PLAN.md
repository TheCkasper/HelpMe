# Plan de Pruebas - HelpMe (Módulo 1)

## 1. Pruebas de UI (Modo Mock)
- [ ] **Login:** Ingresar credenciales inválidas muestra error.
- [ ] **Login:** Ingresar credenciales válidas permite acceso a Home.
- [ ] **Registro:** Validar que contraseñas no coinciden muestra error.
- [ ] **Registro:** Flujo exitoso de registro redirige a Home o muestra éxito.
- [ ] **Cerrar Sesión:** Desde Home, cerrar sesión redirige al Login y no permite regresar con el botón "Atrás".

## 2. Validaciones Unitarias
- `EmailValidator`: Verifica formatos de correo válidos e inválidos.
- `PasswordValidator`: Mínimo 8 caracteres.
- `NameValidator`: Longitud entre 2 y 80 caracteres.

## 3. Pruebas Futuras (Firebase)
- Reglas de seguridad: un usuario no puede leer datos de `usuarios/` que no sean suyos sin permiso.
- Integración de Google Sign In.
- Tiempos de respuesta y comportamiento offline (caché de Firebase).
