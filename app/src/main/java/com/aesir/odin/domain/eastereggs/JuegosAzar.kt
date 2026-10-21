package com.aesir.odin.domain.eastereggs

import kotlin.random.Random

// Pagos: «premio» es lo que se recibe en total (ya incluye la apuesta); 0 si se pierde.

enum class ColorRuleta { VERDE, BEIGE }

/** Ruleta de dos colores. Paga el doble menos el diezmo de Odín (10 %): retorno 90 %. */
object Ruleta {
    const val MINIMA = 5
    const val MAXIMA = 50
    const val PASO = 5
    const val DIEZMO_PORCIENTO = 10

    fun girar(azar: Random): ColorRuleta = ColorRuleta.entries[azar.nextInt(ColorRuleta.entries.size)]

    fun premio(apuesta: Int, elegido: ColorRuleta, salio: ColorRuleta): Int {
        if (elegido != salio) return 0
        val bruto = apuesta * 2
        return bruto - bruto * DIEZMO_PORCIENTO / 100
    }
}

/** Símbolos de la tragamonedas: cuántos hay en cada rodillo de 20 y cuánto paga el triple. */
enum class Simbolo(val glifo: String, val enRodillo: Int, val premioTriple: Int) {
    RUNA("ᛟ", 7, 20),
    CUERNO("🍺", 6, 45),
    MJOLNIR("🔨", 4, 120),
    CUERVO("🐦", 2, 400),
    OJO("👁", 1, 1500)
}

/** Tres rodillos de 20 símbolos. Tiro de 5; retorno ≈ 91 %. */
object Tragamonedas {
    const val COSTO = 5
    const val PREMIO_DOS_CUERNOS = 5

    val rodillo: List<Simbolo> = Simbolo.entries.flatMap { s -> List(s.enRodillo) { s } }

    fun tirar(azar: Random): List<Simbolo> = List(3) { rodillo[azar.nextInt(rodillo.size)] }

    fun premio(tiro: List<Simbolo>): Int = when {
        tiro.distinct().size == 1 -> tiro[0].premioTriple
        tiro.count { it == Simbolo.CUERNO } == 2 -> PREMIO_DOS_CUERNOS
        else -> 0
    }
}

enum class ApuestaDados(val texto: String) { MENOR("Menor que 7"), SIETE("Exactamente 7"), MAYOR("Mayor que 7") }

/** Dos dados de seis caras. Apuesta fija de 10; retorno 83 %. */
object Dados {
    const val APUESTA = 10

    fun tirar(azar: Random): Pair<Int, Int> = azar.nextInt(1, 7) to azar.nextInt(1, 7)

    fun premio(eleccion: ApuestaDados, suma: Int): Int = when {
        eleccion == ApuestaDados.MENOR && suma < 7 -> 20
        eleccion == ApuestaDados.SIETE && suma == 7 -> 50
        eleccion == ApuestaDados.MAYOR && suma > 7 -> 20
        else -> 0
    }
}
