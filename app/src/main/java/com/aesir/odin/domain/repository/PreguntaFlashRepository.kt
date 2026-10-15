package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.PreguntaFlash

/**
 * Banco de preguntas flash. Aquí solo está lo que necesita CU-11; los métodos
 * de CU-06 (obtenerPregunta, guardarIntento) se agregan al implementar ese CU.
 */
interface PreguntaFlashRepository {
    /** Hasta [cantidad] preguntas distintas y en orden aleatorio de los temas dados. */
    suspend fun obtenerPreguntasParaReto(temaIds: List<String>, cantidad: Int): List<PreguntaFlash>
}
