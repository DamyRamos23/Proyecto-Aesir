package com.aesir.odin.domain.eastereggs

/**
 * Juegos ocultos de la pantalla de Mundos. Se descubren tocando los mundos
 * del escudo en orden ([combinacion], con los índices del escudo:
 * 0 UML, 1 Casos de Uso, 2 Ciclo de Vida, 3 Modelos Tradicionales).
 *
 * No están en el SRS: no cuentan para la racha, la bitácora ni el progreso.
 * Todo el hidromiel pasa por el MOD-01 (apuesta con piso, RN-08).
 */
enum class JuegoOculto(val nombre: String, val combinacion: List<Int>) {
    RULETA("Ruleta", repetir(3, 0, 3)),
    TRAGAMONEDAS("Tragamonedas", repetir(3, 2, 1)),
    BLACKJACK("Blackjack", repetir(2, 0, 2, 1)),
    DADOS("Dados de la suerte", repetir(2, 2, 1, 3)),
    MAYOR_MENOR("Mayor o menor", repetir(3, 0, 3, 1, 2));

    /** «UML → Modelos Tradicionales (×3)», para el menú una vez descubierto. */
    fun textoCombinacion(): String {
        val ciclo = combinacion.size / veces
        val nombres = combinacion.take(ciclo).joinToString(" → ") { MUNDOS_ESCUDO[it] }
        return "$nombres (×$veces)"
    }

    private val veces: Int
        get() = when (this) {
            RULETA, TRAGAMONEDAS, MAYOR_MENOR -> 3
            BLACKJACK, DADOS -> 2
        }

    companion object {
        val MUNDOS_ESCUDO = listOf("UML", "Casos de Uso", "Ciclo de Vida", "Modelos Tradicionales")
    }
}

private fun repetir(veces: Int, vararg mundos: Int): List<Int> = List(veces) { mundos.toList() }.flatten()

/** Juegos que el alumno ya descubrió (se conservan entre sesiones). */
interface AlmacenEastereggs {
    fun descubiertos(): Set<JuegoOculto>
    fun descubrir(juego: JuegoOculto)
}
