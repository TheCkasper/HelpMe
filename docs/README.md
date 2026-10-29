# 🚀 HelpMe - Guía del Proyecto para el Equipo

¡Bienvenidos al repositorio de **HelpMe**! Este documento es el punto de partida para que todos sepamos en qué estado se encuentra el proyecto, qué sigue y qué le toca a cada integrante.

## 📌 ESTADO ACTUAL (¡Firebase Conectado & Cambios Recientes!)
- **Autenticación Real:** Inicio de sesión y registro integrados con Firebase Authentication.
- **Dashboard Limpio:** Se eliminó la lista de botones temporales en `HomeFragment`. Ahora la app muestra un panel informativo sobre impacto comunitario con acceso directo a "Nuevo Reporte Rápido".
- **Barra de Navegación Inferior (BottomNavigationView):** Configurada e integrada para alternar libremente entre Inicio, Reportes, Mapa y Perfil.
- **Corrección de Errores & Estabilidad:** Se unificaron recursos XML (`strings.xml`, `file_paths.xml`, `locales_config.xml`) y se creó la estructura base del `ProfileFragment` para evitar cierres de la app.
- **Base de Datos:** Firebase Realtime Database lista y enlazada en el contenedor de dependencias (`AppContainer`).

---

## 🛠️ ¿Cómo empezar a trabajar hoy? (PASOS PARA EL EQUIPO)

Como el proyecto ya está conectado a Firebase y el archivo secreto de conexión NO se sube a GitHub por seguridad, **Sigan estos pasos estrictamente luego de clonar el repositorio:**

1. **Pídanle a Emanuel el archivo `google-services.json` por chat privado.** (¡OJO! Ese archivo NO ESTÁ en GitHub).
2. Peguen el archivo `google-services.json` dentro de la carpeta `app/` de este proyecto en su Android Studio.
3. Hagan clic en el elefante de Gradle: **"Sync Project with Gradle Files"**.
4. ¡Ejecuten la app! Como el proyecto está limpio, **deben registrar una nueva cuenta** desde la pantalla de la app (ej. usando su correo real) para poder entrar al sistema.

---

## 📝 Asignaciones y Responsabilidades de Módulos

Creen las carpetas de sus módulos dentro de `app/src/main/java/com/helpme/app/feature/` y enlacen sus pantallas en `app/src/main/res/navigation/nav_graph.xml`.

👤 **Marcos (Módulo 2 - Perfil):** 
- **Estado:** Ya fue creada la clase base `ProfileFragment.kt` en `feature/profile/` para prevenir cierres.
- **Qué debe hacer:**
  1. Diseñar el layout XML `fragment_profile.xml` con:
     - Campo de texto para Nombre Completo (editable).
     - Campo de texto para Correo Electrónico (deshabilitado/lectura).
     - Campo de texto para DPI (opcional, máximo 13 dígitos).
     - Botón "Guardar Cambios".
     - Botón "Cerrar Sesión" (llama a `AppContainer.authRepository.signOut()`).
  2. Implementar `ProfileViewModel` para leer el nodo `usuarios/{uid}` de Realtime Database al abrir la pantalla y rellenar los campos.
  3. Guardar las modificaciones enviando la actualización al nodo `usuarios/{uid}` respetando el `DATA_CONTRACT.md` (`nombre`, `correo`, `dpi`, `fechaActualizacion`).

📸 **Juan (Módulo 3 - Creación de reportes):** 
- **Tarea:** Pantalla para elegir el tipo de incidencia, agregar descripción y capturar latitud/longitud. 
- *Nota sobre fotos:* Debido a que Firebase Storage requiere tarjeta de crédito, guarda las imágenes en la base de datos convirtiéndolas a Base64 o usando un link simulado (Revisar alternativas con Emanuel).

📋 **Abner (Módulo 4 - Consulta y seguimiento):** 
- **Tarea:** Listar todos los reportes, crear la vista de detalle y permitir cambiar el estado (`Pendiente`, `En proceso`, `Resuelto`).

🗺️ **Julia (Módulo 5 - Mapa y estadísticas):** 
- **Tarea:** Mostrar marcadores en un mapa y estadísticas agrupadas por tipo y estado.

---

## 🗺️ Módulo de mapa: tecnología, funciones e integración

El mapa está implementado en [MapFragment](../app/src/main/java/com/helpme/app/feature/map/MapFragment.kt) como un `WebView` que carga [map.html](../app/src/main/assets/map.html). La interfaz web usa **Leaflet 1.9.4** (CSS y JavaScript desde unpkg) y mosaicos de **Esri World Street Map**; si un mosaico falla, intenta usar **OpenStreetMap**. Se necesita conexión a Internet para cargar estas librerías y los mosaicos. El HTML conserva la atribución de Esri.

La comunicación entre Android y el mapa se realiza con [LeafletMapBridge](../app/src/main/java/com/helpme/app/feature/map/LeafletMapBridge.kt), registrado como `AndroidMapBridge` en el `WebView`. Android envía marcadores y ubicación al JavaScript; el mapa notifica a Android cuando termina de cargar y cuando se selecciona un marcador. La pantalla y sus controles se construyen en [MapUiBuilder](../app/src/main/java/com/helpme/app/feature/map/MapUiBuilder.kt).

### Funciones disponibles

- Muestra incidencias con coordenadas válidas. Los marcadores se ajustan para incluir los resultados visibles.
- Colorea cada marcador según su estado: pendiente (ámbar), en proceso (azul) o resuelto (verde). Al tocarlo, muestra tipo, estado y descripción; también abre la ficha de la incidencia en Android.
- Permite centrar el mapa en la ubicación actual. Solicita permiso de ubicación en tiempo de ejecución; si no se concede o no se obtiene una ubicación, usa Cobán como centro predeterminado.
- Filtra reportes por día, semana o mes (con fecha de referencia), estado, actividad (todas, activas o no activas) y tipo de incidencia.
- Presenta métricas del periodo y una tabla agrupada por tipo y estado. Los reportes sin coordenadas se cuentan en las estadísticas, pero no se dibujan en el mapa.
- Lee los reportes en tiempo real desde Firebase Realtime Database, en el nodo `reportes`, mediante [FirebaseMapRepository](../app/src/main/java/com/helpme/app/feature/map/MapRepository.kt).

El filtrado, la selección y el cálculo de estadísticas están en [MapViewModel](../app/src/main/java/com/helpme/app/feature/map/MapViewModel.kt). El modelo esperado es [Report](../app/src/main/java/com/helpme/app/domain/model/Report.kt): `reporteId`, `tipo`, `tipoId`, `descripcion`, `latitud`, `longitud`, `estado` y `fechaCreacion` son especialmente relevantes para el mapa y sus estadísticas.

### Reutilizar el mapa en otro módulo

Si el módulo pertenece a esta misma app, es preferible reutilizar las clases de `feature/map` en vez de mantener una copia divergente. Para copiar también la pantalla y sus funciones, integra lo siguiente:

1. Copia la carpeta `feature/map` (incluye `MapFragment.kt`, `MapUiBuilder.kt`, `MapState.kt`, `MapViewModel.kt`, `MapRepository.kt` y `LeafletMapBridge.kt`) y el recurso `app/src/main/assets/map.html`. Ajusta el `package` si el nuevo módulo usa otro.
2. Usa el modelo `Report` del proyecto o adapta el repositorio y el mapeo para que proporcionen identificador, tipo, descripción, estado, latitud, longitud y fecha de creación. Si guardas los datos en otro lugar, implementa `MapRepository.observeReports()` para ese origen y conserva el flujo de `List<Report>` o adapta el `ViewModel`.
3. En el layout de la pantalla agrega un `WebView` y configura JavaScript y el puente con el mismo nombre que espera el HTML. El patrón utilizado por este módulo es:

   ```kotlin
   webView.settings.javaScriptEnabled = true
   webView.addJavascriptInterface(mapBridge, LeafletMapBridge.BRIDGE_NAME)
   webView.loadUrl("file:///android_asset/map.html")
   ```

   `mapBridge` debe ser una instancia de `LeafletMapBridge` con callbacks para `onMarkerClick` y `onMapReady`. Una vez cargado el mapa, envía los marcadores con `LeafletMapBridge.updateMarkersInWebView(webView, markers)`; para centrarlo, usa `LeafletMapBridge.setUserLocationInWebView(webView, lat, lng, isUserLocation)`. Los marcadores se envían como `ReportMarker`. Actualiza el mapa cuando cambien los datos y destruye el `WebView` al destruir la vista del fragmento, como hace `MapFragment`.
4. Agrega al `AndroidManifest.xml` los permisos `INTERNET`, `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`. Si el módulo requiere la ubicación actual, solicita los permisos de ubicación en tiempo de ejecución; `INTERNET` es necesario para Leaflet y los mosaicos externos.
5. Si quieres toda la pantalla de estadísticas, conserva también los controles y la observación de `MapUiState` de `MapFragment`; si solo necesitas el mapa, puedes omitir `MapUiBuilder` y el filtrado/estadísticas, pero mantén el `WebView`, el puente, el modelo de marcadores y el HTML.
6. Agrega el nuevo fragmento a `app/src/main/res/navigation/nav_graph.xml` y configura una acción desde la pantalla que lo abrirá.

No se requiere una dependencia Gradle adicional para Leaflet: se carga desde el HTML. Este módulo sí usa las dependencias Android existentes de `WebView`, Firebase Realtime Database, Lifecycle y Coroutines. Verifica que la política de red de la app permita acceder a unpkg, Esri y OpenStreetMap.

---

## 🧠 ¿Cómo usar Firebase para guardar sus datos (Módulos 2, 3, 4 y 5)?

En Firebase Realtime Database **NO EXISTEN LAS TABLAS**. Es un gran diccionario de datos JSON.

Para "crear una tabla" (nodo) y guardar datos, no necesitan que Emanuel haga nada en la consola web. Los nodos se crean automáticamente cuando guardan algo desde el código.

**Ejemplo de cómo guardarán un reporte (Juan):**
```kotlin
val database = FirebaseDatabase.getInstance().reference
val nuevoReporte = hashMapOf(
    "descripcion" to "Poste caído",
    "latitud" to 14.634,
    "longitud" to -90.512,
    "estado" to "Pendiente"
)
// Al hacer esto, Firebase crea la "tabla" reportes sola:
database.child("reportes").push().setValue(nuevoReporte)
```

---

## 📖 Documentación OBLIGATORIA (¡Leer antes de programar!)
Para que el ejemplo de arriba no sea un desorden y todos los módulos se entiendan, **hay reglas estrictas sobre cómo nombrar las variables en la base de datos**.

1. 📄 **[DATA_CONTRACT.md](./DATA_CONTRACT.md)**: **¡LA LEY DEL EQUIPO!** Define los nombres exactos de los campos (Ej: `latitud`, no `lat`. `Pendiente`, no `pendiente`). Si no respetan esto, los módulos de Abner y Julia fallarán al leer los reportes de Juan.
2. 🤝 **[HANDOFF_EQUIPO.md](./HANDOFF_EQUIPO.md)**: Explica cómo obtener el `UID` del usuario que tiene la sesión iniciada para amarrar los reportes a ese usuario.
