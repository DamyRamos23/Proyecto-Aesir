package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ConceptoEnigma
import com.aesir.odin.domain.model.DueloEnigmas
import com.aesir.odin.domain.model.PreguntaEnigma
import com.aesir.odin.domain.model.ResultadoDuelo

/** CU-13: conceptos, preguntas y resultados del duelo de enigmas. */
interface DueloRepository {
    suspend fun obtenerConceptos(temaIds: List<String>): List<ConceptoEnigma>
    suspend fun obtenerPreguntas(): List<PreguntaEnigma>

    /** Duelos iniciados desde [inicioDia] (incluye ganados, perdidos y abandonados). */
    suspend fun contarDuelosDelDia(inicioDia: Long): Int

    /** Paso 4: el duelo cuenta en el límite diario desde que se apuesta (resultado null = en curso). */
    suspend fun registrarDueloIniciado(duelo: DueloEnigmas, fecha: Long)

    /** E7.2: el duelo que quedó sin terminar al cerrarse la app. */
    suspend fun obtenerDueloSinTerminar(): ResultadoDuelo?

    suspend fun guardarResultado(resultado: ResultadoDuelo)
}
