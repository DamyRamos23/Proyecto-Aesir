package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.PreguntaFlashBancoDao
import com.aesir.odin.data.local.dao.PreguntaFlashDao
import com.aesir.odin.data.local.entity.PreguntaFlashAttemptEntity
import com.aesir.odin.data.local.entity.PreguntaFlashEntity
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.PreguntaFlashAttempt
import com.aesir.odin.domain.repository.PreguntaFlashRepository

class PreguntaFlashRepositoryImpl(
    private val dao: PreguntaFlashDao,
    private val bancoDao: PreguntaFlashBancoDao
) : PreguntaFlashRepository {

    override suspend fun obtenerPreguntaAleatoriaDeTema(temaId: String): PreguntaFlash? =
        bancoDao.getAleatoriaPorTema(temaId)?.aDominio()

    override suspend fun obtenerPreguntasParaReto(temaIds: List<String>, cantidad: Int): List<PreguntaFlash> {
        if (temaIds.isEmpty() || cantidad <= 0) return emptyList()
        return bancoDao.getAleatoriasPorTemas(temaIds, cantidad).map { it.aDominio() }
    }

    override suspend fun guardarIntento(intento: PreguntaFlashAttempt) {
        dao.insertar(
            PreguntaFlashAttemptEntity(
                id = intento.id,
                preguntaId = intento.preguntaId,
                respuestasElegidas = intento.respuestasElegidas,
                estado = intento.estado.name,
                puntos = intento.puntos,
                tiempoRespuestaMs = intento.tiempoRespuestaMs,
                fecha = intento.fecha
            )
        )
    }
}

private fun PreguntaFlashEntity.aDominio() = PreguntaFlash(
    id = id,
    temaId = temaId,
    enunciado = enunciado,
    opciones = opciones,
    respuestasCorrectas = respuestasCorrectas
)
