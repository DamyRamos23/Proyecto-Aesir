package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.Leccion
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.ResultadoLeccion
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.LeccionRepository
import com.aesir.odin.domain.repository.RoadmapRepository

class FakeLeccionRepository(lecciones: List<Leccion>) : LeccionRepository {
    val lecciones = lecciones.associateBy { it.id }.toMutableMap()
    val resultados = mutableListOf<ResultadoLeccion>()

    override suspend fun obtenerLeccion(leccionId: String) = lecciones[leccionId]
    override suspend fun obtenerLeccionesPorTema(temaId: String) = lecciones.values.filter { it.temaId == temaId }
    override suspend fun guardarResultado(resultado: ResultadoLeccion) {
        resultados += resultado
    }
    override suspend fun marcarCompletada(leccionId: String) {
        lecciones[leccionId]?.let { lecciones[leccionId] = it.copy(completada = true) }
    }
}

class FakeHistorialRepository : HistorialRepository {
    val errores = mutableListOf<HistorialError>()

    override suspend fun obtenerErrores() = errores.toList()
    override suspend fun obtenerErroresParaRepaso() = errores.toList()
    override suspend fun registrarError(error: HistorialError) {
        errores += error
    }
}

class FakeRoadmapRepository(
    temas: List<Tema> = emptyList(),
    mundos: List<Mundo> = emptyList()
) : RoadmapRepository {
    val temas = temas.associateBy { it.id }.toMutableMap()
    val mundos = mundos.associateBy { it.id }.toMutableMap()

    override suspend fun obtenerTemas(): List<Tema> = temas.values.sortedBy { it.orden }
    override suspend fun obtenerTema(temaId: String): Tema = temas.getValue(temaId)
    override suspend fun obtenerMundos(): List<Mundo> = mundos.values.sortedBy { it.orden }
    override suspend fun obtenerMundo(mundoId: String): Mundo = mundos.getValue(mundoId)
    override suspend fun obtenerTemasPorMundo(mundoId: String): List<Tema> =
        temas.values.filter { it.mundoId == mundoId }.sortedBy { it.orden }

    override suspend fun marcarLeccionCompletada(temaId: String) {
        temas[temaId]?.let { temas[temaId] = it.copy(completado = true) }
    }
    override suspend fun marcarIntroduccionVista(temaId: String) {
        temas[temaId]?.let { temas[temaId] = it.copy(introduccionVista = true) }
    }
    override suspend fun desbloquearTema(temaId: String) {
        temas[temaId]?.let { temas[temaId] = it.copy(desbloqueado = true) }
    }
}

fun mundo(id: String, orden: Int = 1, desbloqueado: Boolean = true, nombre: String = "Mundo $id") =
    Mundo(id, nombre, "Descripción $id", orden, desbloqueado)

fun tema(id: String, orden: Int, desbloqueado: Boolean, mundoId: String = "m1") =
    Tema(
        id = id,
        mundoId = mundoId,
        nombre = "Tema $orden",
        descripcion = "Descripción $orden",
        orden = orden,
        desbloqueado = desbloqueado,
        completado = false,
        introduccionVista = false
    )
