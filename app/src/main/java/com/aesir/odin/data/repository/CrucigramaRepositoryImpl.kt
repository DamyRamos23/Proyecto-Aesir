package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.ConceptoCrucigramaDao
import com.aesir.odin.data.local.dao.ResultadoCrucigramaDao
import com.aesir.odin.data.local.entity.ResultadoCrucigramaEntity
import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.ResultadoCrucigrama
import com.aesir.odin.domain.repository.CrucigramaRepository
import java.util.UUID

class CrucigramaRepositoryImpl(
    private val conceptoDao: ConceptoCrucigramaDao,
    private val resultadoDao: ResultadoCrucigramaDao
) : CrucigramaRepository {

    override suspend fun obtenerBancoPorTemas(temaIds: List<String>): List<ConceptoCrucigrama> =
        conceptoDao.getByTemaIds(temaIds).map { ConceptoCrucigrama(it.id, it.temaId, it.respuesta, it.descripcion) }

    override suspend fun guardarResultado(resultado: ResultadoCrucigrama) {
        resultadoDao.insertar(
            ResultadoCrucigramaEntity(
                id = UUID.randomUUID().toString(),
                temaId = resultado.temaId,
                puntajeFinal = resultado.puntajeFinal,
                aciertos = resultado.aciertos,
                erroresCometidos = resultado.erroresCometidos,
                fecha = resultado.fecha
            )
        )
    }

    override suspend fun obtenerMejorResultado(temaId: String): ResultadoCrucigrama? =
        resultadoDao.getMejorPorTema(temaId)?.let {
            ResultadoCrucigrama(it.temaId, it.puntajeFinal, it.aciertos, it.erroresCometidos, it.fecha)
        }
}
