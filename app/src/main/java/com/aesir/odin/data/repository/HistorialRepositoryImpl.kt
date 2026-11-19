package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.HistorialErrorDao
import com.aesir.odin.data.local.entity.HistorialErrorEntity
import com.aesir.odin.domain.bitacora.OrigenError
import com.aesir.odin.domain.model.EstadoError
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.repository.HistorialRepository
import java.util.concurrent.TimeUnit

class HistorialRepositoryImpl(
    private val dao: HistorialErrorDao,
    private val reloj: () -> Long = System::currentTimeMillis
) : HistorialRepository {

    override suspend fun buscarPorPregunta(preguntaId: String): HistorialError? =
        dao.getPorPregunta(preguntaId)?.toDomain()

    override suspend fun registrar(error: HistorialError) = dao.insertar(error.toEntity())

    override suspend fun actualizar(error: HistorialError) = dao.actualizar(error.toEntity())

    /** Errores de los últimos [DIAS_REPASO] días, para las actividades de refuerzo. */
    override suspend fun obtenerErroresParaRepaso(): List<HistorialError> =
        dao.getDesde(reloj() - TimeUnit.DAYS.toMillis(DIAS_REPASO)).map { it.toDomain() }

    private companion object {
        const val DIAS_REPASO = 7L
    }
}

private fun HistorialError.toEntity() = HistorialErrorEntity(
    id, preguntaId, origen.name, temaId, enunciado, opciones, respuestaCorrecta,
    respuestaAlumno, pista, explicacion, estado.name, fallos, fecha
)

private fun HistorialErrorEntity.toDomain() = HistorialError(
    id = id,
    preguntaId = preguntaId,
    origen = OrigenError.valueOf(origen),
    temaId = temaId,
    enunciado = enunciado,
    opciones = opciones,
    respuestaCorrecta = respuestaCorrecta,
    respuestaAlumno = respuestaAlumno,
    pista = pista,
    explicacion = explicacion,
    estado = EstadoError.valueOf(estado),
    fallos = fallos,
    fecha = fecha
)
