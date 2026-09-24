package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.MundoDao
import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.data.local.entity.MundoEntity
import com.aesir.odin.data.local.entity.TemaEntity
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * Implementación del repositorio del roadmap, apoyada en Room.
 * Corresponde a `RoadmapRepositoryImpl` de la capa Persistencia del
 * diagrama de clases: traduce entre las entidades de Room (Fuentes de
 * datos) y los modelos de dominio (Negocio).
 */
class RoadmapRepositoryImpl(
    private val mundoDao: MundoDao,
    private val temaDao: TemaDao
) : RoadmapRepository {

    override suspend fun obtenerTemas(): List<Tema> =
        temaDao.getAll().map { it.aTema() }

    override suspend fun obtenerTema(temaId: String): Tema =
        temaDao.getById(temaId).aTema()

    override suspend fun obtenerMundos(): List<Mundo> =
        mundoDao.getAll().map { it.aMundo() }

    override suspend fun obtenerMundo(mundoId: String): Mundo =
        mundoDao.getById(mundoId).aMundo()

    override suspend fun obtenerTemasPorMundo(mundoId: String): List<Tema> =
        temaDao.getByMundoId(mundoId).map { it.aTema() }

    override suspend fun marcarLeccionCompletada(temaId: String) {
        temaDao.actualizarCompletado(temaId, completado = true)
    }

    override suspend fun marcarIntroduccionVista(temaId: String) {
        temaDao.actualizarIntroduccionVista(temaId, vista = true)
    }

    override suspend fun desbloquearTema(temaId: String) {
        temaDao.actualizarDesbloqueado(temaId, desbloqueado = true)
    }

    private fun MundoEntity.aMundo() = Mundo(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        orden = orden,
        desbloqueado = desbloqueado
    )

    private fun TemaEntity.aTema() = Tema(
        id = id,
        mundoId = mundoId,
        nombre = nombre,
        descripcion = descripcion,
        orden = orden,
        desbloqueado = desbloqueado,
        completado = completado,
        introduccionVista = introduccionVista
    )
}
