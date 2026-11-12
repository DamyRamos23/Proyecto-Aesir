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
        /** Se descuenta al fallar una pregunta flash: incorrecta, incompleta o sin respuesta. */
        const val FLASH_FALLO = 2
        const val ABANDONO_CONTRARRELOJ = 20
        const val ENTRADA_CONTRARRELOJ = 10
        const val CONTRARRELOJ_CORRECTA = 2
        /** Se resta por cada respuesta incorrecta en el contrarreloj, para no responder a lo bruto. */
        const val CONTRARRELOJ_FALLO = 1

        // CU-13 Duelo de enigmas
        const val APUESTA_DUELO = 24
        const val HIDROMIEL_POR_SORBO = 4
        const val MAX_DUELOS_DIA = 3

        /** Paso 12: se recupera la apuesta más 4 por sorbo sobrante, sin diezmo (RF-194). */
        fun premioDuelo(sorbosSobrantes: Int): Int = APUESTA_DUELO + HIDROMIEL_POR_SORBO * sorbosSobrantes
    }
}
