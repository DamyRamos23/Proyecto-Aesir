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

    /**
     * CU-02 (RF-41): premio de la forja perfecta. La apuesta ×3 menos el
     * diezmo de Odín del 10 %, ambos redondeados hacia abajo (20 → 60 − 6 = 54).
     */
    fun premioForja(apuesta: Int): Int {
        val bruto = apuesta.coerceAtLeast(0) * MULTIPLICADOR_FORJA
        // Entero para no arrastrar errores de punto flotante: 10 % = bruto / 10, hacia abajo
        val diezmo = bruto * DIEZMO_ODIN_PORCIENTO / 100
        return bruto - diezmo
    }

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
        // CU-02 La apuesta de Brokkr
        const val APUESTA_FORJA_MIN = 20
        const val APUESTA_FORJA_MAX = 80
        const val MULTIPLICADOR_FORJA = 3
        const val DIEZMO_ODIN = 0.10
        const val DIEZMO_ODIN_PORCIENTO = 10
    }
}
