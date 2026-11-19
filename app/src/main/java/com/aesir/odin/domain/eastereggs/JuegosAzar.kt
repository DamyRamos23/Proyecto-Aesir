package com.aesir.odin.domain.eastereggs

import com.aesir.odin.domain.hidromiel.PoliticaCosto
import kotlin.random.Random

// Pagos: «premio» es lo que se recibe en total (ya incluye la apuesta); 0 si se pierde.

enum class ColorRuleta { VERDE, BEIGE }

/** Ruleta de dos colores. Paga el doble menos el diezmo de Odín (10 %): retorno 90 %. */
object Ruleta {
    const val MINIMA = PoliticaCosto.RULETA_MIN
    const val MAXIMA = PoliticaCosto.RULETA_MAX
    const val PASO = PoliticaCosto.RULETA_PASO
    const val DIEZMO_PORCIENTO = PoliticaCosto.DIEZMO_ODIN_PORCIENTO

    fun girar(azar: Random): ColorRuleta = ColorRuleta.entries[azar.nextInt(ColorRuleta.entries.size)]

    fun premio(apuesta: Int, elegido: ColorRuleta, salio: ColorRuleta): Int {
        if (elegido != salio) return 0
        val bruto = apuesta * 2
        return bruto - bruto * DIEZMO_PORCIENTO / 100
    }
}

/** Símbolos de la tragamonedas: cuántos hay en cada rodillo de 20 y cuánto paga el triple. */
enum class Simbolo(val glifo: String, val enRodillo: Int, val premioTriple: Int) {
    RUNA("ᛟ", 7, PoliticaCosto.TRIPLE_RUNA),
    CUERNO("ᚠ", 6, PoliticaCosto.TRIPLE_CUERNO),   // fehu, la riqueza
    MJOLNIR("ᚦ", 4, PoliticaCosto.TRIPLE_MJOLNIR), // thurisaz, la runa de Thor
    CUERVO("ᛉ", 2, PoliticaCosto.TRIPLE_CUERVO),   // algiz, como la pata de un cuervo
    OJO("ᚨ", 1, PoliticaCosto.TRIPLE_OJO)          // ansuz, la runa de Odín
}

/** Tres rodillos de 20 símbolos. Tiro de 5; retorno ≈ 91 %. */
object Tragamonedas {
    const val COSTO = PoliticaCosto.TRAGAMONEDAS_COSTO
    const val PREMIO_DOS_CUERNOS = PoliticaCosto.TRAGAMONEDAS_DOS_CUERNOS

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
    const val APUESTA = PoliticaCosto.DADOS_APUESTA

    fun tirar(azar: Random): Pair<Int, Int> = azar.nextInt(1, 7) to azar.nextInt(1, 7)

    fun premio(eleccion: ApuestaDados, suma: Int): Int = when {
        eleccion == ApuestaDados.MENOR && suma < 7 -> PoliticaCosto.DADOS_MENOR_MAYOR
        eleccion == ApuestaDados.SIETE && suma == 7 -> PoliticaCosto.DADOS_SIETE
        eleccion == ApuestaDados.MAYOR && suma > 7 -> PoliticaCosto.DADOS_MENOR_MAYOR
        else -> 0
    }
}
