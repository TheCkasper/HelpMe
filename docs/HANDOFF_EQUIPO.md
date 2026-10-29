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

## Instrucciones Específicas para Marcos (Módulo 2 - Perfil)
- La clase plantilla `ProfileFragment.kt` ya está creada en `com.helpme.app.feature.profile`.
- Debes crear la interfaz XML `fragment_profile.xml` con campos para Nombre, Correo (solo lectura), DPI (opcional) y el botón de Guardar.
- Al cargar el fragmento, debes consultar en Realtime Database la ruta `usuarios/{uid}` usando el UID de la sesión actual.
- Al presionar Guardar, actualiza los valores `nombre`, `correo`, `dpi` y `fechaActualizacion` respetando las llaves exactas definidas en `DATA_CONTRACT.md`.

## Recomendaciones
- Utilicen View Binding como está configurado en `build.gradle.kts`.
- La navegación se gestiona centralizadamente mediante Jetpack Navigation (`nav_graph.xml`) y la barra inferior `BottomNavigationView`.
