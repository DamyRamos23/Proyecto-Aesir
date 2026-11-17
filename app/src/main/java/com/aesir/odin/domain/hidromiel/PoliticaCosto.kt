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

    /** RN-07: puntaje mínimo aprobatorio, 60 (70 en el Ragnarök). */
    fun minimoAprobatorio(nivel: NivelInfo): Int =
        if (nivel.tipo == TipoNivel.RAGNAROK) PUNTAJE_MINIMO_RAGNAROK else PUNTAJE_MINIMO

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

    /**
     * CU-01 (RF-16): premio bruto = apuesta × multiplicador (en centésimas, con
     * el bono de Draupnir ya sumado), redondeado hacia abajo, menos el diezmo de
     * Odín del 10 %, también redondeado hacia abajo.
     */
    fun pagoAlba(apuesta: Int, multiplicadorCentesimas: Int): PagoValhalla =
        if (multiplicadorCentesimas <= 0) PagoValhalla(0, 0, 0) else conDiezmo(apuesta * multiplicadorCentesimas / 100)

    /** Diezmo de Odín: 10 % del premio bruto, redondeado hacia abajo (MOD-01). */
    private fun conDiezmo(bruto: Int): PagoValhalla {
        val diezmo = bruto * DIEZMO_ODIN_PORCIENTO / 100
        return PagoValhalla(bruto, diezmo, bruto - diezmo)
    }

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
        const val APUESTA_MIN_ALBA = 10
        const val APUESTA_MAX_ALBA = 60
        const val APUESTA_MIN_VOLVA = 15
        const val APUESTA_MAX_VOLVA = 70
        const val DIEZMO_ODIN_PORCIENTO = 10
        const val C = 20
        /** RN-07: puntaje mínimo para superar un nivel normal y el Ragnarök. */
        const val PUNTAJE_MINIMO = 60
        const val PUNTAJE_MINIMO_RAGNAROK = 70
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
        /** CU-11, RF-147: retos contrarreloj por día real. */
        const val MAX_CONTRARRELOJ_DIA = 3

        /** Paso 12: se recupera la apuesta más 4 por sorbo sobrante, sin diezmo (RF-194). */
        fun premioDuelo(sorbosSobrantes: Int): Int = APUESTA_DUELO + HIDROMIEL_POR_SORBO * sorbosSobrantes

        // CU-02 La apuesta de Brokkr
        const val APUESTA_FORJA_MIN = 20
        const val APUESTA_FORJA_MAX = 80
        const val MULTIPLICADOR_FORJA = 3
        const val DIEZMO_ODIN = 0.10
    }
}
