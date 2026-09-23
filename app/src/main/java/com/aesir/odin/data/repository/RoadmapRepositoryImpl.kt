package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.data.local.entity.TemaEntity
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.repository.RoadmapRepository

class RoadmapRepositoryImpl(private val temaDao: TemaDao) : RoadmapRepository {

    override suspend fun obtenerTemas(): List<Tema> = temaDao.getAll().map { it.toDomain() }

    override suspend fun obtenerTema(temaId: String): Tema? = temaDao.getById(temaId)?.toDomain()

    override suspend fun marcarLeccionCompletada(temaId: String) {
        temaDao.marcarLeccionCompletada(temaId)
    }

    override suspend fun desbloquearTema(temaId: String) {
        temaDao.desbloquear(temaId)
    }
}

private fun TemaEntity.toDomain() = Tema(
    id = id,
    mundoId = mundoId,
    nombre = nombre,
    orden = orden,
    desbloqueado = desbloqueado,
    introduccionVista = introduccionVista,
    leccionCompletada = leccionCompletada
)
