package com.aesir.odin.domain.model

import kotlin.math.abs

/** Actividad que se juega en un nivel del roadmap. */
enum class Actividad(val nombre: String, val jugable: Boolean) {
    LECCION("Lección interactiva", jugable = true),
    CRUCIGRAMA("Crucigrama", jugable = true),
    MEMORAMA("Memorama", jugable = false),
    FLASHCARDS("Flash cards", jugable = false);

    companion object {
        fun desde(texto: String?): Actividad = entries.firstOrNull { it.name == texto } ?: LECCION
    }
}

/**
 * Forma del árbol de habilidades de cada mundo: dos anillos alrededor del
 * Ragnarök, que va en el centro.
 *
 * - Anillo interior (1): las lecciones. Cada una se conecta con sus dos
 *   vecinas y la última con la primera, porque no hay un orden obligatorio.
 * - Anillo exterior (2): los minijuegos. Cada uno queda entre dos lecciones y
 *   se conecta con esas dos, las que tiene más cerca ("por cercanía"). Los
 *   minijuegos no se conectan entre sí.
 *
 * Los ángulos son los mismos que usa el escudo del roadmap, así que lo que se
 * ve conectado es lo que el MOD-01 considera conectado.
 */
object ArbolHabilidades {

    const val ANILLO_LECCIONES = 1
    const val ANILLO_MINIJUEGOS = 2

    /** Ángulo (grados) de la lección [i] de [n]: empieza arriba y avanza en sentido antihorario. */
    fun anguloInterior(i: Int, n: Int): Float = -90f - i * 360f / n

    /** Ángulo del minijuego [k] de [m]: desfasado medio paso para quedar entre dos lecciones. */
    fun anguloExterior(k: Int, n: Int, m: Int): Float = -90f - 180f / n.coerceAtLeast(1) - k * 360f / m

    /** Índices de las dos lecciones más cercanas al minijuego [k] (en caso de empate, la de menor índice). */
    fun leccionesCercanas(k: Int, n: Int, m: Int): List<Int> {
        if (n <= 0) return emptyList()
        val a = anguloExterior(k, n, m)
        return (0 until n)
            .sortedWith(compareBy({ distancia(a, anguloInterior(it, n)) }, { it }))
            .take(minOf(2, n))
    }

    /** Conexiones entre niveles (en ambos sentidos) a partir del orden de cada anillo. */
    fun <T> conexiones(lecciones: List<T>, minijuegos: List<T>): Map<T, Set<T>> {
        val mapa = mutableMapOf<T, MutableSet<T>>()
        fun unir(a: T, b: T) {
            if (a == b) return
            mapa.getOrPut(a) { mutableSetOf() } += b
            mapa.getOrPut(b) { mutableSetOf() } += a
        }
        val n = lecciones.size
        if (n > 1) for (i in 0 until n) unir(lecciones[i], lecciones[(i + 1) % n])
        minijuegos.forEachIndexed { k, minijuego ->
            leccionesCercanas(k, n, minijuegos.size).forEach { unir(minijuego, lecciones[it]) }
        }
        return mapa
    }

    private fun distancia(a: Float, b: Float): Float {
        val d = abs((a - b) % 360f)
        return if (d > 180f) 360f - d else d
    }
}
