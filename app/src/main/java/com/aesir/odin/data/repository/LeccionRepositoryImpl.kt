package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.EjercicioDao
import com.aesir.odin.data.local.dao.LeccionDao
import com.aesir.odin.data.local.entity.EjercicioEntity
import com.aesir.odin.data.local.entity.LeccionEntity
import com.aesir.odin.data.local.entity.ResultadoLeccionEntity
import com.aesir.odin.domain.model.Ejercicio
import com.aesir.odin.domain.model.Leccion
import com.aesir.odin.domain.model.ResultadoLeccion
import com.aesir.odin.domain.repository.LeccionRepository
import java.util.UUID

class LeccionRepositoryImpl(
    private val dao: LeccionDao,
    private val ejercicioDao: EjercicioDao
) : LeccionRepository {

    override suspend fun obtenerLeccion(leccionId: String): Leccion? =
        dao.getById(leccionId)?.let { it.toDomain(ejercicioDao.getByLeccionId(it.id)) }

    override suspend fun obtenerLeccionesPorTema(temaId: String): List<Leccion> =
        dao.getByTemaId(temaId).map { it.toDomain(ejercicioDao.getByLeccionId(it.id)) }

    override suspend fun guardarResultado(resultado: ResultadoLeccion) {
        dao.insertarResultado(
            ResultadoLeccionEntity(
                id = UUID.randomUUID().toString(),
                leccionId = resultado.leccionId,
                puntaje = resultado.puntaje,
                erroresCometidos = resultado.erroresCometidos,
                aprobada = resultado.aprobada,
                fecha = resultado.fecha
            )
        )
    }

    override suspend fun marcarCompletada(leccionId: String) {
        dao.actualizarCompletada(leccionId, true)
    }
}

private fun LeccionEntity.toDomain(ejercicios: List<EjercicioEntity>) = Leccion(
    id = id,
    temaId = temaId,
    titulo = titulo,
    introduccion = introduccion,
    orden = orden,
    ejercicios = ejercicios.map { it.toDomain() },
    completada = completada
)

private fun EjercicioEntity.toDomain() = Ejercicio(
    id = id,
    leccionId = leccionId,
    enunciado = enunciado,
    opciones = opciones,
    respuestasCorrectas = respuestasCorrectas,
    orden = orden
)
