# Sopa de Letras

Aplicacion Android de sopa de letras desarrollada en Kotlin con Jetpack Compose y Material 3. El juego permite elegir dificultad, genera una matriz de letras, valida palabras seleccionadas por arrastre y muestra progreso, errores, pistas, temporizador y celebracion de victoria.

## Caracteristicas

- Interfaz clara, colorida y de alto contraste.
- Pantalla inicial visual con selector de dificultad.
- Dificultades:
  - Facil: matriz 8x8 con 5 palabras.
  - Medio: matriz 10x10 con 7 palabras.
  - Dificil: matriz 12x12 con 9 palabras.
- Seleccion de palabras por arrastre en linea recta.
- Linea de seleccion con degradado brillante.
- Celdas animadas al seleccionar letras.
- Animacion de error con sacudida y color rojo.
- Temporizador con alerta visual cuando queda poco tiempo.
- Contador de palabras encontradas, errores y pistas.
- Acciones de pista, limpiar seleccion, rendirse y nueva partida.
- Celebracion de victoria con confeti, tarjeta animada y feedback haptico.
- Palabras obtenidas desde Random Words API mediante Retrofit.
- Fallback local cuando la API no responde o no entrega suficientes palabras.

## Tecnologias

- Kotlin 2.0.20
- Android Gradle Plugin 8.5.2
- Gradle Wrapper 8.7
- Jetpack Compose
- Material Design 3
- AndroidX Lifecycle ViewModel
- Kotlin Coroutines y StateFlow
- Retrofit 2.11.0 con Gson Converter
- JUnit 4

## Requisitos

- Android Studio con JBR incluido.
- Android SDK con `compileSdk` 34.
- Dispositivo o emulador con Android 7.0 (API 24) o superior.

La aplicacion solicita permiso de Internet para consultar la API remota de palabras.

## Estructura del Proyecto

```text
app/src/main/java/com/miempresa/sopaletras/
|-- MainActivity.kt
|-- data/
|   |-- local/
|   |   |-- dao/
|   |   |-- database/
|   |   |-- datasource/
|   |   |   `-- FuentePalabrasLocal.kt
|   |   `-- entity/
|   |-- mapper/
|   |-- remote/
|   |   |-- api/
|   |   |   `-- PalabrasApiService.kt
|   |   |-- datasource/
|   |   |   |-- FuentePalabrasRemota.kt
|   |   |   `-- ProveedorRetrofit.kt
|   |   `-- dto/
|   |       `-- PalabraRemotaDto.kt
|   `-- repository/
|       `-- SopaLetrasRepositorioImpl.kt
|-- di/
|   `-- MarcadorPaqueteDi.kt
|-- domain/
|   |-- model/
|   |   |-- Celda.kt
|   |   |-- Dificultad.kt
|   |   |-- Direccion.kt
|   |   |-- Matriz.kt
|   |   |-- Palabra.kt
|   |   |-- Posicion.kt
|   |   `-- SopaLetras.kt
|   |-- repository/
|   |   `-- SopaLetrasRepositorio.kt
|   `-- usecase/
|       |-- ObtenerSopaLetrasUseCase.kt
|       `-- ValidarPalabraUseCase.kt
|-- presentation/
|   |-- components/
|   |   |-- CountdownTimerComponent.kt
|   |   |-- LetterCell.kt
|   |   `-- VictoryCelebrationOverlay.kt
|   |-- navigation/
|   |-- screens/
|   |   `-- sopaletras/
|   |       `-- SopaLetrasScreen.kt
|   |-- theme/
|   |   |-- Color.kt
|   |   |-- ColoresPalabras.kt
|   |   |-- Tema.kt
|   |   `-- Tipografia.kt
|   `-- viewmodel/
|       |-- SopaLetrasEstado.kt
|       `-- SopaLetrasViewModel.kt
`-- utils/
```

## Arquitectura

El proyecto usa una organizacion tipo Clean Architecture:

- `domain`: modelos, contratos y casos de uso. No depende de Android ni de Retrofit.
- `data`: fuentes de datos remotas/locales e implementacion del repositorio.
- `presentation`: UI en Compose, componentes reutilizables, tema visual, estado y ViewModel.
- `di`: espacio reservado para futura inyeccion de dependencias.

Actualmente las dependencias se ensamblan manualmente en `MainActivity`: fuente remota, fuente local, repositorio, casos de uso y `SopaLetrasViewModel`.