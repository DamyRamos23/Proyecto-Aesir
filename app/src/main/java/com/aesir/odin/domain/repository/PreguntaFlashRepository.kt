package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.PreguntaFlashAttempt

/**
 * Banco de preguntas compartido. Lo usa CU-06 (Pregunta Flash) y lo reutiliza
 * CU-11 (Reto Contrarreloj) u otro módulo que necesite preguntas por tema.
 */
interface PreguntaFlashRepository {
    /** Una pregunta al azar del banco para ese tema, o null si no tiene. */
    suspend fun obtenerPreguntaAleatoriaDeTema(temaId: String): PreguntaFlash?

    /** Hasta [cantidad] preguntas distintas y en orden aleatorio de los temas dados. */
    suspend fun obtenerPreguntasParaReto(temaIds: List<String>, cantidad: Int): List<PreguntaFlash>

    suspend fun guardarIntento(intento: PreguntaFlashAttempt)
}
