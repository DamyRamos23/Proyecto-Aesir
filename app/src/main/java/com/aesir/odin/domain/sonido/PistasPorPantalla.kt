package com.aesir.odin.domain.sonido

/** Pantallas del menú de selección (suena el tema principal). */
private val RUTAS_PRINCIPAL = setOf("mundos", "caraMoneda", "ajustes", "roadmap", "racha", "juegoOculto")
/** Pantallas de combate. */
private val RUTAS_BATALLA = setOf("ragnarok", "hastaElAlba", "forjaBrokkr")

/**
 * Qué pista toca en cada pantalla, a partir de su ruta de navegación (solo
 * cuenta la parte antes de «/» o «?»). Lo que no es menú, combate, contrarreloj
 * ni la Völva es una actividad de estudio: lecciones, minijuegos, diagnóstico,
 * repaso de Mímir, duelo de enigmas y sus resúmenes.
 */
fun pistaParaRuta(ruta: String?): PistaMusica {
    val base = ruta?.substringBefore('/')?.substringBefore('?') ?: return PistaMusica.PRINCIPAL
    return when {
        base in RUTAS_PRINCIPAL -> PistaMusica.PRINCIPAL
        base in RUTAS_BATALLA -> PistaMusica.BATALLA
        base == "retoContrareloj" -> PistaMusica.CONTRARRELOJ
        base == "volva" -> PistaMusica.VOLVA
        else -> PistaMusica.LECCION
    }
}
