package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.ResultadoCrucigrama

interface CrucigramaRepository {
    suspend fun obtenerBancoPorTemas(temaIds: List<String>): List<ConceptoCrucigrama>
    suspend fun guardarResultado(resultado: ResultadoCrucigrama)
    suspend fun obtenerMejorResultado(temaId: String): ResultadoCrucigrama?
}
