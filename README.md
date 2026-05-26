# Sopa de Letras - Aplicación Android

Aplicación Android de Sopa de Letras desarrollada con **Jetpack Compose**, siguiendo los principios de **Clean Architecture** y el patrón **MVVM**.

## Arquitectura

El proyecto se divide en tres capas principales siguiendo los principios de Clean Architecture:

```
com.miempresa.sopaletras/
│
├── dominio/                          # Capa de Dominio (núcleo del negocio)
│   ├── modelos/                      # Entidades del dominio
│   ├── repositorios/                 # Contratos (interfaces) de repositorios
│   └── casosdeuso/                   # Casos de uso (lógica de aplicación)
│
├── datos/                            # Capa de Datos
│   ├── repositorios/                 # Implementaciones de repositorios
│   ├── fuentes/                      # Orígenes de datos (local, remoto)
│   └── modelos/                      # DTOs y mapeadores
│
├── presentacion/                     # Capa de Presentación (UI)
│   ├── ui/
│   │   ├── pantallas/                # Composables de pantalla + ViewModels
│   │   ├── componentes/              # Composables reutilizables
│   │   └── tema/                     # Tema visual de la aplicación
│   └── navegacion/                   # Configuración de navegación
│
├── nucleo/                           # Utilidades compartidas
└── di/                               # Inyección de dependencias
```

## Tecnologías Utilizadas

- **Kotlin** - Lenguaje de programación.
- **Jetpack Compose** - Framework declarativo de UI.
- **Coroutines + Flow** - Programación asíncrona reactiva.
- **ViewModel** - Componente de Android Architecture.
- **Material 3** - Sistema de diseño.

## Principios y Patrones Aplicados

- **SOLID** - Especialmente Inversión de Dependencias (DIP) y Responsabilidad Única (SRP).
- **Clean Architecture** - Separación estricta entre Dominio, Datos y Presentación.
- **MVVM** - Modelo-Vista-ViewModel.
- **Repository Pattern** - Abstracción del origen de datos.
- **ViewState Pattern** - Estado de UI inmutable expuesto vía StateFlow.

## Cómo Ejecutar

1. Abrir el proyecto en **Android Studio Hedgehog** o superior.
2. Sincronizar las dependencias de Gradle.
3. Ejecutar en un emulador o dispositivo con Android 7.0 (API 24) o superior.

## Estructura de Entregas Académicas

Esta es la **Primera Entrega**: arquitectura base, estructura del proyecto y funcionalidad mínima de visualización y validación.

### Próximas entregas previstas
- Algoritmo de generación de sopa con palabras insertadas en distintas direcciones.
- Persistencia con Room.
- Inyección de dependencias con Hilt.
- Selección por arrastre.
- Pruebas unitarias e instrumentadas.
