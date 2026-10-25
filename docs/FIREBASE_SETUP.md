# Configuración de Firebase para HelpMe

## Instrucciones para conectar Firebase (Fase 2)
Este proyecto fue diseñado con una arquitectura que permite sustituir los repositorios simulados (Mocks) por implementaciones reales de Firebase.

### Pasos:
1. Crear un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Registrar la app con el Application ID: `com.helpme.app`.
3. Descargar `google-services.json` y colocarlo en el directorio `app/`.
4. En Firebase Console, habilitar:
   - **Authentication:** Email/Password (y opcionalmente Google).
   - **Realtime Database:** Crear la base de datos en una región cercana y aplicar `database.rules.json`.
   - **Storage:** Crear el bucket y aplicar las reglas necesarias.
5. Modificar la inyección de dependencias en `AppContainer.kt` para que provea las implementaciones de Firebase (ej. `FirebaseAuthRepository` en lugar de `MockAuthRepository`).
6. Eliminar el indicativo de "Modo Demostración" de la UI si se considera necesario.

**ADVERTENCIA:** Nunca subas el archivo `google-services.json` o claves API al repositorio público.

La sección de reportes usa Realtime Database en `reportes/{reporteId}` y guarda las fotografías en `reportes/{uid}/{reporteId}/{nombre_archivo}` de Storage, según el contrato de `DATA_CONTRACT.md`. Configura reglas que permitan leer los reportes y que solo usuarios autenticados puedan crearlos o actualizar su estado. La app necesita el permiso de Internet (declarado en el manifiesto). Si no encuentra la configuración de Firebase, usa un repositorio de demostración que persiste reportes e imágenes únicamente en el dispositivo; esos datos no se sincronizan con Firebase.
