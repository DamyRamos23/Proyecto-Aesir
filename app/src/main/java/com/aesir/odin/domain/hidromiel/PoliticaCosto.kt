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
     * CU-12 (RF-173 a RF-176): Profecía cumplida paga la apuesta ×2.5 y Lectura
     * clara ×1.5, menos el diezmo de Odín del 10 %, ambos redondeados hacia
     * abajo. Humo del caldero devuelve la apuesta sin diezmo y Maldición no paga.
     */
    fun pagoVolva(apuesta: Int, lectura: com.aesir.odin.domain.model.Lectura): PagoValhalla = when (lectura) {
        com.aesir.odin.domain.model.Lectura.PROFECIA_CUMPLIDA -> conDiezmo(apuesta * 5 / 2)
        com.aesir.odin.domain.model.Lectura.LECTURA_CLARA -> conDiezmo(apuesta * 3 / 2)
        com.aesir.odin.domain.model.Lectura.HUMO_DEL_CALDERO -> PagoValhalla(apuesta, 0, apuesta)
        com.aesir.odin.domain.model.Lectura.MALDICION -> PagoValhalla(0, 0, 0)
    }

    /** Diezmo de Odín: 10 % del premio bruto, redondeado hacia abajo (MOD-01). */
    private fun conDiezmo(bruto: Int): PagoValhalla {
        val diezmo = bruto * DIEZMO_ODIN_PORCIENTO / 100
        return PagoValhalla(bruto, diezmo, bruto - diezmo)
    }

    companion object {
        const val APUESTA_MIN_VOLVA = 15
        const val APUESTA_MAX_VOLVA = 70
        const val DIEZMO_ODIN_PORCIENTO = 10
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
    }
}
