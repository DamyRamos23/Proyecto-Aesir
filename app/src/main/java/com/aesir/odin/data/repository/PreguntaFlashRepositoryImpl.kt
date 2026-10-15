package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.PreguntaFlashBancoDao
import com.aesir.odin.data.local.entity.PreguntaFlashEntity
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.repository.PreguntaFlashRepository

class PreguntaFlashRepositoryImpl(
    private val bancoDao: PreguntaFlashBancoDao
) : PreguntaFlashRepository {

    override suspend fun obtenerPreguntasParaReto(temaIds: List<String>, cantidad: Int): List<PreguntaFlash> {
        if (temaIds.isEmpty() || cantidad <= 0) return emptyList()
        return bancoDao.getAleatoriasPorTemas(temaIds, cantidad).map { it.aPregunta() }
    }

    private fun PreguntaFlashEntity.aPregunta() = PreguntaFlash(
        id = id,
        temaId = temaId,
        enunciado = enunciado,
        opciones = opciones,
        respuestasCorrectas = respuestasCorrectas
    )
}
