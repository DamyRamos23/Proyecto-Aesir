package com.aesir.odin.domain.hidromiel

/**
 * Recompensa al superar un nivel por primera vez:
 * bono = redondear(B × puntaje / 100), con el puntaje de 0 a 100.
 * Recompensa = C + bono; si con ese nivel se completan los niveles del
 * mundo, 3C + bono (para cubrir la entrada al Ragnarök).
 */
class CalculadoraRecompensa(private val politica: PoliticaCosto = PoliticaCosto()) {

    fun calcularBono(puntaje: Int): Int = (B * puntaje.coerceIn(0, 100) + 50) / 100

    fun calcularRecompensa(puntaje: Int, completaMundo: Boolean): Int {
        val base = PoliticaCosto.C * if (completaMundo) PoliticaCosto.MULT_RAGNAROK else 1
        return base + calcularBono(puntaje)
    }

    companion object {
        const val B = 10
    }
}
