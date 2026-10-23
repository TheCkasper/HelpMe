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
