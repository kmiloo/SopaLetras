package com.miempresa.sopaletras.datos.fuentes.local

/**
 * Fuente local de palabras. Sirve como banco de respaldo (fallback) cuando
 * la Random Word API no está disponible o devuelve menos palabras de las requeridas.
 *
 * Todas las palabras están normalizadas a mayúsculas y sin acentos para que
 * encajen en la cuadrícula sin caracteres especiales.
 */
class FuentePalabrasLocal {

    fun obtenerPalabras(): List<String> = listOf(
        // Términos del proyecto
        "ANDROID", "KOTLIN", "COMPOSE", "MVVM", "LIMPIA",
        "DOMINIO", "DATOS", "GRADLE", "CORUTINA", "ESTADO",
        "VISTA", "PATRON", "SOLID", "FLUJO", "MODELO",
        "CLASE", "PAQUETE", "OBJETO", "FUNCION", "NULO",
        // Palabras en español comunes (favorecen los cruces)
        "MONEDA", "PUENTE", "FUENTE", "PUERTA", "MUNDO",
        "CIELO", "TIERRA", "FUEGO", "AGUA", "AIRE",
        "PONTE", "TONO", "MANO", "PESO", "LADO",
        "MESA", "SILLA", "LIBRO", "PAPEL", "LAPIZ"
    )
}
