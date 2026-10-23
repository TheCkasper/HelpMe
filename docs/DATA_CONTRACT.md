# Contrato de Datos Oficial (Firebase)

Este documento define la estructura de datos que TODO el equipo debe utilizar para integrarse con Firebase.

## 1. Nodos de Realtime Database

### `usuarios/{uid}`
- `uid` (String): Coincide con FirebaseAuth UID.
- `nombre` (String): Nombre visible del usuario.
- `correo` (String): Correo electrónico.
- `dpi` (String): 13 dígitos o vacío (Gestionado por perfil).
- `fechaActualizacion` (Long): Timestamp en ms.
- `rol` (String): "Ciudadano", "Moderador", "Administrador".
- `estadoCuenta` (String): "Activo", "Suspendido".
- `proveedorAuth` (String): "password", "google.com".
- `fechaRegistro` (Long): Timestamp en ms.

### `reportes/{reporteId}`
- `reporteId` (String): Clave única del reporte.
- `tipo` (String): Categoría del reporte.
- `descripcion` (String): Descripción de la incidencia.
- `fotoUrl` (String): URL de la imagen en Firebase Storage.
- `latitud` (Double): Coordenada GPS válida.
- `longitud` (Double): Coordenada GPS válida.
- `estado` (String): "Pendiente", "En proceso", "Resuelto".
- `usuarioId` (String): UID del creador.
- `fechaCreacion` (Long): Timestamp en ms.
- `fechaActualizacion` (Long): Timestamp en ms.
- `tipoId` (String): Clave estable del catálogo.

## 2. Storage
- Las fotografías de los reportes deben subirse a la ruta: `reportes/{uid}/{reporteId}/{nombre_archivo}`.

## Notas
Ningún módulo debe renombrar estos campos ni crear nuevos sin consenso.
