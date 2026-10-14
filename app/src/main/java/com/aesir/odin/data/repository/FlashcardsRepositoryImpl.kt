package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.ConceptoCrucigramaDao
import com.aesir.odin.data.local.dao.ResultadoFlashcardsDao
import com.aesir.odin.data.local.entity.ResultadoFlashcardsEntity
import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.ResultadoFlashcards
import com.aesir.odin.domain.repository.FlashcardsRepository
import java.util.UUID

class FlashcardsRepositoryImpl(
    private val conceptoDao: ConceptoCrucigramaDao,
    private val resultadoDao: ResultadoFlashcardsDao
) : FlashcardsRepository {

    override suspend fun obtenerConceptosPorTemas(temaIds: List<String>): List<ConceptoCrucigrama> =
        conceptoDao.getByTemaIds(temaIds).map { ConceptoCrucigrama(it.id, it.temaId, it.respuesta, it.descripcion) }

    override suspend fun guardarResultado(resultado: ResultadoFlashcards) {
        resultadoDao.insertar(
            ResultadoFlashcardsEntity(
                id = UUID.randomUUID().toString(),
                temaId = resultado.temaId,
                puntaje = resultado.puntaje,
                aciertosPrimerIntento = resultado.aciertosPrimerIntento,
                errores = resultado.errores,
                totalTarjetas = resultado.totalTarjetas,
                fecha = resultado.fecha
            )
        )
    }

    override suspend fun obtenerMejorResultado(temaId: String): ResultadoFlashcards? =
        resultadoDao.getMejorPorTema(temaId)?.let {
            ResultadoFlashcards(it.temaId, it.puntaje, it.aciertosPrimerIntento, it.errores, it.totalTarjetas, it.fecha)
        }
}
