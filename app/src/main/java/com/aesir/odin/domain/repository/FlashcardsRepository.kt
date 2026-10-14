package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.ResultadoFlashcards

/**
 * Acceso a datos del CU-03. Los conceptos (concepto + descripción) son los
 * mismos del banco del crucigrama: un solo banco alimenta ambos minijuegos.
 */
interface FlashcardsRepository {
    suspend fun obtenerConceptosPorTemas(temaIds: List<String>): List<ConceptoCrucigrama>
    suspend fun guardarResultado(resultado: ResultadoFlashcards)
    suspend fun obtenerMejorResultado(temaId: String): ResultadoFlashcards?
}
