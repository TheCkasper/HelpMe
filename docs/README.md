# 🚀 HelpMe - Guía del Proyecto para el Equipo

¡Bienvenidos al repositorio de **HelpMe**! Este documento es el punto de partida para que todos sepamos en qué estado se encuentra el proyecto, qué sigue y qué le toca a cada integrante.

## 📌 ESTADO ACTUAL (¡Firebase Conectado!)
- **Autenticación Real:** El proyecto ya no usa datos falsos. El inicio de sesión y registro están conectados a la nube real de Firebase Authentication.
- **Base de Datos Configurada:** Firebase Realtime Database está lista y las dependencias ya están en los archivos Gradle.
- **Arquitectura Base:** MVVM, View Binding y Jetpack Navigation Component.
- **Identidad Visual:** Tema verde implementado en `colors.xml` y `themes.xml`.
- **Navegación Inicial:** `HomeFragment` tiene botones (placeholders) que ustedes deben conectar a los fragmentos de sus respectivos módulos.

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
- **Tarea:** Mostrar y editar los datos del usuario logueado en Realtime Database. Permitir agregar el DPI (opcional).

📸 **Juan (Módulo 3 - Creación de reportes):** 
- **Tarea:** Pantalla para elegir el tipo de incidencia, agregar descripción y capturar latitud/longitud. 
- *Nota sobre fotos:* Debido a que Firebase Storage requiere tarjeta de crédito, guarda las imágenes en la base de datos convirtiéndolas a Base64 o usando un link simulado (Revisar alternativas con Emanuel).

📋 **Abner (Módulo 4 - Consulta y seguimiento):** 
- **Tarea:** Listar todos los reportes, crear la vista de detalle y permitir cambiar el estado (`Pendiente`, `En proceso`, `Resuelto`).

🗺️ **Julia (Módulo 5 - Mapa y estadísticas):** 
- **Tarea:** Mostrar marcadores en un mapa y estadísticas agrupadas por tipo y estado.

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
