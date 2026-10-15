package com.aesir.odin.domain.model

import kotlin.math.abs

/** Actividad que se juega en un nivel del roadmap. */
enum class Actividad(val nombre: String, val jugable: Boolean) {
    LECCION("Lección interactiva", jugable = true),
    CRUCIGRAMA("Crucigrama", jugable = true),
    MEMORAMA("Memorama", jugable = false),
    FLASHCARDS("Flash cards", jugable = true),
    /** CU-09 Resolver reto de arrastrar y soltar. */
    ARRASTRAR("Arrastrar y soltar", jugable = true),
    /** Minijuego del tercer anillo que todavía no se define. */
    POR_DEFINIR("Por definir", jugable = false);

    companion object {
        fun desde(texto: String?): Actividad = entries.firstOrNull { it.name == texto } ?: LECCION
    }
}

/**
 * Forma del árbol de habilidades de cada mundo: tres anillos alrededor del
 * Ragnarök, que va en el centro.
 *
 * - Anillo 1 (interior): las lecciones. Cada una se conecta con sus dos
 *   vecinas y la última con la primera, porque no hay un orden obligatorio.
 * - Anillo 2: los minijuegos. Cada uno queda entre dos lecciones y se conecta
 *   con esas dos, las que tiene más cerca ("por cercanía").
 * - Anillo 3 (exterior): los retos (arrastrar y soltar y los que se definan
 *   después). Cada uno se conecta con los dos minijuegos del anillo 2 que
 *   tiene más cerca.
 *
 * Los niveles de un mismo anillo exterior no se conectan entre sí. Los
 * ángulos son los mismos que usa el escudo del roadmap, así que lo que se ve
 * conectado es lo que el MOD-01 considera conectado.
 */
object ArbolHabilidades {

    const val ANILLO_LECCIONES = 1
    const val ANILLO_MINIJUEGOS = 2
    const val ANILLO_RETOS = 3

    /** Ángulo (grados) de la lección [i] de [n]: empieza arriba y avanza en sentido antihorario. */
    fun anguloInterior(i: Int, n: Int): Float = -90f - i * 360f / n.coerceAtLeast(1)

    /** Ángulo del minijuego [k] de [m]: desfasado medio paso para quedar entre dos lecciones. */
    fun anguloExterior(k: Int, n: Int, m: Int): Float = -90f - 180f / n.coerceAtLeast(1) - k * 360f / m.coerceAtLeast(1)

    /** Ángulo del reto [j] de [p] del tercer anillo: empieza arriba, como las lecciones. */
    fun anguloRetos(j: Int, p: Int): Float = -90f - j * 360f / p.coerceAtLeast(1)

    /** Índices de las dos lecciones más cercanas al minijuego [k] (en caso de empate, la de menor índice). */
    fun leccionesCercanas(k: Int, n: Int, m: Int): List<Int> =
        cercanos(anguloExterior(k, n, m), n) { anguloInterior(it, n) }

    /** Índices de los dos minijuegos del anillo 2 más cercanos al reto [j]. */
    fun minijuegosCercanos(j: Int, p: Int, n: Int, m: Int): List<Int> =
        cercanos(anguloRetos(j, p), m) { anguloExterior(it, n, m) }

    /** Índices de las dos lecciones más cercanas al reto [j] (para repasar o armar su contenido). */
    fun leccionesCercanasAReto(j: Int, p: Int, n: Int): List<Int> =
        cercanos(anguloRetos(j, p), n) { anguloInterior(it, n) }

    /** Conexiones entre niveles (en ambos sentidos) a partir del orden de cada anillo. */
    fun <T> conexiones(lecciones: List<T>, minijuegos: List<T>, retos: List<T> = emptyList()): Map<T, Set<T>> {
        val mapa = mutableMapOf<T, MutableSet<T>>()
        fun unir(a: T, b: T) {
            if (a == b) return
            mapa.getOrPut(a) { mutableSetOf() } += b
            mapa.getOrPut(b) { mutableSetOf() } += a
        }
        val n = lecciones.size
        val m = minijuegos.size
        if (n > 1) for (i in 0 until n) unir(lecciones[i], lecciones[(i + 1) % n])
        minijuegos.forEachIndexed { k, minijuego ->
            leccionesCercanas(k, n, m).forEach { unir(minijuego, lecciones[it]) }
        }
        retos.forEachIndexed { j, reto ->
            minijuegosCercanos(j, retos.size, n, m).forEach { unir(reto, minijuegos[it]) }
        }
        return mapa
    }

    private fun cercanos(angulo: Float, cuantos: Int, anguloDe: (Int) -> Float): List<Int> {
        if (cuantos <= 0) return emptyList()
        return (0 until cuantos)
            .sortedWith(compareBy({ distancia(angulo, anguloDe(it)) }, { it }))
            .take(minOf(2, cuantos))
    }

    private fun distancia(a: Float, b: Float): Float {
        val d = abs((a - b) % 360f)
        return if (d > 180f) 360f - d else d
    }
}
