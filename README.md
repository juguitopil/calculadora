# Calculadora Android Multimedia y Personalizable 🚀📱

Una aplicación de calculadora moderna, interactiva y altamente personalizable desarrollada para Android utilizando **Jetpack Compose** y **Kotlin**. Combina una interfaz visual avanzada con efectos de cristal (*Glassmorphism*), un reproductor de video de fondo en bucle y soporte completo para multimedia personalizada desde la galería del dispositivo.

---

## ✨ Características Principales

* 🎨 **Diseño Moderno (*Glassmorphism*)**: Interfaz elegante inspirada en efectos de cristal translúcido con bordes luminosos y pantallas de salida de alta transparencia.
* 🎬 **Video de Fondo en Bucle Infinito**: Reproducción de videos de fondo adaptados (*responsivos*) con control de volumen integrado para un aspecto estético inmersivo.
* 📁 **Selector Multimedia desde la Galería**: Permite al usuario elegir y cambiar en tiempo real su propio video de fondo y sonidos personalizados directamente desde el almacenamiento del teléfono (*Storage Access Framework*).
* 🎵 **Sonidos Personalizados por Botón**: Cada tecla (números, operadores, borrar, igual) activa efectos de sonido únicos e independientes utilizando `MediaPlayer` y recursos nativos.
* 🔇 **Controles de Bloqueo / Silencio Independientes**: Botones dedicados en la barra superior para silenciar o activar la música de fondo y los tonos de los botones de forma instantánea.
* 📐 **Diseño 100% Responsivo**: Adaptación dinámica de tamaños de botones, fuentes y espaciados mediante `BoxWithConstraints` para ajustarse perfectamente a cualquier pantalla o tamaño de dispositivo móvil.
* 🖼️ **Iconos Vectoriales Personalizados (SVG)**: Uso de iconos optimizados en formato Vector Drawable para los controles de ajustes y audio.

---

## 🛠️ Tecnologías y Librerías

* **Lenguaje**: [Kotlin](https://kotlinlang.org/)
* **Interfaz de Usuario**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material 3](https://m3.material.io/)
* **Multimedia**: Android `MediaPlayer` & `VideoView` (`AndroidView` interoperability)
* **Gestión de Archivos**: `ActivityResultContracts.GetContent()` (SAF)
* **Arquitectura**: Clean UI state handling con enfoques declarativos

---

## 📁 Estructura del Proyecto

```text
com.tuapp.calculadora/
│
├── MainActivity.kt         # Actividad principal, UI en Jetpack Compose, Video player y selectores multimedia
├── CalculatorLogic.kt      # Lógica matemática de la calculadora (operaciones y estado)
└── ui/
    └── theme/
        ├── Color.kt        # Definición de paleta de colores
        ├── Theme.kt        # Configuración de MaterialTheme
        └── Type.kt         # Estilos tipográficos
```

---

## 🚀 Cómo Ejecutar el Proyecto

1. Clona o descarga este repositorio en tu computadora.
2. Abre **Android Studio** (versión Hedgehog o superior recomendada).
3. Selecciona **Open** y busca la carpeta raíz del proyecto (`calculadora`).
4. Espera a que Gradle sincronice las dependencias.
5. Conecta tu teléfono Android (activando la *Depuración por USB* o *Depuración por Wi-Fi*) o inicia un emulador Android (API 24 o superior).
6. Haz clic en el botón **Run** ▶ (o presiona `Shift + F10`) para compilar e instalar la aplicación.
