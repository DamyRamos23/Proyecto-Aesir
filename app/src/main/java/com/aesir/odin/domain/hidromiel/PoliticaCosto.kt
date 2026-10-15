package com.aesir.odin.domain.hidromiel

/**
 * Valores del MOD-01. Nivel normal = C = 20 · Ragnarök = 3C = 60 ·
 * recompensa diaria = 2C = 40 (no acumulable) · cantidad inicial = 2C = 40.
 */
class PoliticaCosto {

    /** Entrada de un nivel: 0 si es el primero del mundo, C o 3C si es el Ragnarök. */
    fun costoDe(nivel: NivelInfo): Int = when {
        nivel.esPrimero -> 0
        nivel.tipo == TipoNivel.RAGNAROK -> C * MULT_RAGNAROK
        else -> C
    }

    /** E5 */
    fun costoAyuda(tipo: TipoAyuda): Int = when (tipo) {
        TipoAyuda.REVELAR_LETRA -> AYUDA_LETRA
        TipoAyuda.REVELAR_PALABRA -> AYUDA_PALABRA
    }

    /** E3 */
    fun recompensaDiaria(): Int = C * ENTRADAS_DIARIAS

    fun cantidadInicial(): Int = C * ENTRADAS_INICIALES

    companion object {
        const val C = 20
        const val MULT_RAGNAROK = 3
        const val ENTRADAS_DIARIAS = 2
        const val ENTRADAS_INICIALES = 2
        const val AYUDA_LETRA = 5
        const val AYUDA_PALABRA = 15
        const val FLASH_CORRECTA = 5
        const val FLASH_SIN_RESPUESTA = 2
        const val ABANDONO_CONTRARRELOJ = 20
        const val ENTRADA_CONTRARRELOJ = 10
        const val CONTRARRELOJ_CORRECTA = 2
    }
}
