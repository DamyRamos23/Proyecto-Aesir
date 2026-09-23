package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.repository.RoadmapRepository

class RoadmapRepositoryImpl(
    private val temaDao: TemaDao
) : RoadmapRepository {

    override suspend fun obtenerTema(temaId: String): Tema? {
        val entity = temaDao.getById(temaId) ?: return null
        return Tema(
            id = entity.id,
            nombre = entity.nombre,
            descripcion = entity.descripcion,
            introduccionVista = entity.introduccionVista
        )
    }

    override suspend fun marcarIntroduccionVista(temaId: String) {
        temaDao.actualizarIntroduccionVista(temaId, true)
    }
}