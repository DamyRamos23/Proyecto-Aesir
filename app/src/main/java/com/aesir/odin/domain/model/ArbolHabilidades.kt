package com.aesir.odin.domain.model

import kotlin.math.abs

/** Actividad que se juega en un nivel del roadmap. */
enum class Actividad(val nombre: String, val jugable: Boolean = true) {
    LECCION("Lección interactiva"),
    CRUCIGRAMA("Crucigrama"),
    MEMORAMA("Memorama"),
    FLASHCARDS("Flash cards"),

    /** CU-09 Resolver reto de arrastrar y soltar. */
    ARRASTRAR("Arrastrar y soltar");

    companion object {
        fun desde(texto: String?): Actividad = entries.firstOrNull { it.name == texto } ?: LECCION
    }
}

/**
 * Forma del árbol de habilidades de cada mundo (SRS v3.2, «Estructura del
 * roadmap de cada mundo»): cuatro anillos alrededor del Ragnarök, que va en el
 * centro. En total 18 niveles por mundo.
 *
 * - Anillo 1 (interior): 4 lecciones, una por cada tema 1 a 4. Cada una se
 *   conecta con sus dos vecinas y la última con la primera.
 * - Anillo 2 (exterior): 6 minijuegos (2 crucigramas, 2 memoramas y 2 de
 *   flashcards). Cada uno pertenece a un tema 1 a 4 y se conecta con la
 *   lección de su tema; queda dibujado junto a ella.
 * - Anillo 3: 2 retos de arrastrar y soltar, cada uno conectado a los dos
 *   minijuegos que tiene más cerca.
 * - Anillo 4: 6 lecciones de los temas 5 a 10. Cada una se conecta con sus
 *   lecciones vecinas del mismo anillo y con el reto o el minijuego que tiene
 *   más cerca (el reto si quedan alineados).
 *
 * No hay un orden obligatorio. Los ángulos son los mismos que usa el escudo
 * del roadmap, así que lo que se ve conectado es lo que el MOD-01 considera
 * conectado.
 */
object ArbolHabilidades {

    const val ANILLO_LECCIONES = 1
    const val ANILLO_MINIJUEGOS = 2
    const val ANILLO_RETOS = 3
    const val ANILLO_LECCIONES_NUEVAS = 4

    /** Niveles que hay que superar para que se habilite el Ragnarök (RN-05). */
    const val NIVELES_POR_MUNDO = 18

    /** Temas de cada mundo: 4 del anillo interior y 6 del cuarto anillo. */
    const val TEMAS_POR_MUNDO = 10

    /**
     * Tema (1 a 4) de cada minijuego del anillo 2, en orden: crucigrama I,
     * memorama I, flashcards I, crucigrama II, memorama II y flashcards II.
     * Cada minijuego queda junto a la lección de su tema en el escudo y los dos
     * de una misma actividad son de temas distintos.
     */
    val TEMA_DE_MINIJUEGO = listOf(1, 2, 3, 3, 4, 1)

    /** Ángulo (grados) de la lección [i] de [n]: empieza arriba y avanza en sentido antihorario. */
    fun anguloInterior(i: Int, n: Int): Float = -90f - i * 360f / n.coerceAtLeast(1)

    /** Ángulo del minijuego [k] de [m]: desfasado medio paso para quedar entre dos lecciones. */
    fun anguloExterior(k: Int, n: Int, m: Int): Float =
        -90f - 180f / n.coerceAtLeast(1) - k * 360f / m.coerceAtLeast(1)

    /** Ángulo del reto [j] de [p] del tercer anillo: empieza arriba, como las lecciones. */
    fun anguloRetos(j: Int, p: Int): Float = -90f - j * 360f / p.coerceAtLeast(1)

    /** Ángulo de la lección [i] de [q] del cuarto anillo: empieza arriba, junto al primer reto. */
    fun anguloCuarto(i: Int, q: Int): Float = -90f - i * 360f / q.coerceAtLeast(1)

    /** Índices de las dos lecciones más cercanas al minijuego [k] (en caso de empate, la de menor índice). */
    fun leccionesCercanas(k: Int, n: Int, m: Int): List<Int> =
        cercanos(anguloExterior(k, n, m), n) { anguloInterior(it, n) }

    /** Índices de los dos minijuegos del anillo 2 más cercanos al reto [j]. */
    fun minijuegosCercanos(j: Int, p: Int, n: Int, m: Int): List<Int> =
        cercanos(anguloRetos(j, p), m) { anguloExterior(it, n, m) }

    /**
     * Nivel interior al que se conecta la lección [i] del cuarto anillo: el
     * reto (índice en el anillo 3) si está alineado con ella o, si no, el
     * minijuego más cercano (índice en el anillo 2).
     */
    fun interiorDeCuarto(i: Int, q: Int, p: Int, n: Int, m: Int): Pair<Int, Int>? {
        val angulo = anguloCuarto(i, q)
        val reto = (0 until p).minByOrNull { distancia(angulo, anguloRetos(it, p)) }
        val minijuego = (0 until m).minByOrNull { distancia(angulo, anguloExterior(it, n, m)) }
        val dReto = reto?.let { distancia(angulo, anguloRetos(it, p)) } ?: Float.MAX_VALUE
        val dMini = minijuego?.let { distancia(angulo, anguloExterior(it, n, m)) } ?: Float.MAX_VALUE
        return when {
            reto != null && dReto <= dMini -> ANILLO_RETOS to reto
            minijuego != null -> ANILLO_MINIJUEGOS to minijuego
            else -> null
        }
    }

    /**
     * Conexiones entre niveles (en ambos sentidos) a partir del orden de cada
     * anillo. [leccionDe] da la lección del tema de un minijuego; si no la da,
     * se usa la lección más cercana en el escudo.
     */
    fun <T> conexiones(
        lecciones: List<T>,
        minijuegos: List<T>,
        retos: List<T> = emptyList(),
        cuarto: List<T> = emptyList(),
        leccionDe: (T) -> T? = { null }
    ): Map<T, Set<T>> {
        val mapa = mutableMapOf<T, MutableSet<T>>()
        fun unir(a: T, b: T) {
            if (a == b) return
            mapa.getOrPut(a) { mutableSetOf() } += b
            mapa.getOrPut(b) { mutableSetOf() } += a
        }
        val n = lecciones.size
        val m = minijuegos.size
        val p = retos.size
        val q = cuarto.size
        if (n > 1) for (i in 0 until n) unir(lecciones[i], lecciones[(i + 1) % n])
        minijuegos.forEachIndexed { k, minijuego ->
            val leccion = leccionDe(minijuego)?.takeIf { it in lecciones }
                ?: leccionesCercanas(k, n, m).firstOrNull()?.let { lecciones[it] }
            if (leccion != null) unir(minijuego, leccion)
        }
        retos.forEachIndexed { j, reto ->
            minijuegosCercanos(j, p, n, m).forEach { unir(reto, minijuegos[it]) }
        }
        if (q > 1) for (i in 0 until q) unir(cuarto[i], cuarto[(i + 1) % q])
        cuarto.forEachIndexed { i, leccion ->
            when (val interior = interiorDeCuarto(i, q, p, n, m)) {
                null -> Unit
                else -> {
                    val (anillo, indice) = interior
                    unir(leccion, if (anillo == ANILLO_RETOS) retos[indice] else minijuegos[indice])
                }
            }
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
