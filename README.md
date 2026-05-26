# Sopa de Letras

Aplicacion Android de sopa de letras desarrollada en Kotlin con Jetpack Compose. El juego genera una grilla de palabras, permite seleccionar letras por toque o arrastre, valida palabras encontradas y muestra el progreso de la partida.

## Caracteristicas

- Pantalla inicial con selector de dificultad.
- Dificultades:
  - Facil: matriz 8x8 con 5 palabras.
  - Medio: matriz 10x10 con 7 palabras.
  - Dificil: matriz 12x12 con 9 palabras.
- Generacion dinamica de la sopa de letras.
- Palabras obtenidas desde Random Words API mediante Retrofit.
- Fallback local de palabras cuando la API no esta disponible o no entrega suficientes resultados.
- Seleccion de celdas por toque y por arrastre en linea recta.
- Validacion de palabras en sentido directo e inverso.
- Marcado visual de palabras encontradas con colores distintos.
- Temporizador, contador de errores y contador de pistas usadas.
- Acciones de limpiar seleccion, usar pista, rendirse y comenzar nueva partida.

## Tecnologias

- Kotlin 2.0.20
- Android Gradle Plugin 8.5.2
- Jetpack Compose con Material 3
- AndroidX Lifecycle ViewModel
- Kotlin Coroutines y StateFlow
- Retrofit 2.11.0 con Gson Converter
- JUnit 4 para pruebas unitarias

## Requisitos

- Android Studio Hedgehog o superior.
- JDK 17.
- Android SDK con `compileSdk` 34.
- Dispositivo o emulador con Android 7.0 (API 24) o superior.

La aplicacion solicita permiso de Internet para consultar la API remota de palabras.

## Estructura del Proyecto

```text
app/src/main/java/com/miempresa/sopaletras/
|-- datos/
|   |-- fuentes/
|   |   |-- local/          # Lista local de palabras de respaldo
|   |   `-- remoto/         # Retrofit y servicio de Random Words API
|   `-- repositorios/       # Implementacion del repositorio
|-- di/                     # Marcador para futuras dependencias de DI
|-- dominio/
|   |-- casosdeuso/         # Obtener sopa y validar seleccion
|   |-- modelos/            # Entidades del juego
|   `-- repositorios/       # Contrato del repositorio
`-- presentacion/
    |-- MainActivity.kt     # Ensamblado manual de dependencias
    `-- ui/
        |-- pantallas/      # Pantalla, estado y ViewModel
        `-- tema/           # Tema, colores y tipografia
```

## Arquitectura

El proyecto separa responsabilidades en tres capas principales:

- Dominio: modelos, contratos y casos de uso independientes de Android.
- Datos: obtencion de palabras, generacion de la sopa y adaptacion al contrato del dominio.
- Presentacion: UI declarativa en Compose, ViewModel y estado inmutable expuesto con `StateFlow`.

La inyeccion de dependencias se realiza manualmente en `MainActivity`, donde se conectan las fuentes de datos, el repositorio, los casos de uso y el `SopaLetrasViewModel`.

## Como Ejecutar

1. Abrir el proyecto en Android Studio.
2. Sincronizar Gradle.
3. Ejecutar la configuracion `app` en un emulador o dispositivo fisico.
