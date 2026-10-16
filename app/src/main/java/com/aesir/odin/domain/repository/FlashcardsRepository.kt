package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.ResultadoFlashcards

/** Acceso a datos del CU-03 Resolver flashcards de conceptos. Usa el mismo banco de conceptos del crucigrama. */
interface FlashcardsRepository {
    suspend fun obtenerConceptosPorTemas(temaIds: List<String>): List<ConceptoCrucigrama>
    suspend fun guardarResultado(resultado: ResultadoFlashcards)
    suspend fun obtenerMejorResultado(temaId: String): ResultadoFlashcards?
}
