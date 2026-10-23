# Handoff para el Equipo

**A: Marcos, Juan, Abner y Julia.**

## Estado Actual
El sistema de Autenticación de la Fase 2 está terminado y **está 100% conectado a la nube real de Firebase Authentication.**
La base de datos Realtime Database está lista para recibir sus datos.

## ¿Cómo trabajar en sus módulos?
1. **PASO OBLIGATORIO:** Pídanme el archivo `google-services.json` por privado, péguenlo en su carpeta `app/` y sincronicen Gradle para que les compile el proyecto.
2. Creen sus carpetas de trabajo dentro de `com.helpme.app.feature` (ej. `/feature/profile/`).
3. Para la base de datos, lean el `DATA_CONTRACT.md`. **Sus modelos de Kotlin deben tener exactamente esos nombres en las variables.**
4. Para saber qué usuario está utilizando la app, pueden inyectar `AppContainer.authRepository` y leer el flujo `observeSession()`.

## Recomendaciones
- Utilicen View Binding como está configurado en `build.gradle.kts`.
- La navegación provisional se hace con Intent/Activities o Fragments (ver `HomeActivity` para los puntos de entrada).
