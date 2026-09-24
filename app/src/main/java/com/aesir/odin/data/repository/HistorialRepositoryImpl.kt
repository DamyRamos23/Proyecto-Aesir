package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.HistorialErrorDao
import com.aesir.odin.data.local.entity.HistorialErrorEntity
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.repository.HistorialRepository
import java.util.concurrent.TimeUnit

class HistorialRepositoryImpl(
    private val dao: HistorialErrorDao,
    private val reloj: () -> Long = System::currentTimeMillis
) : HistorialRepository {

    override suspend fun obtenerErrores(): List<HistorialError> =
        dao.getAll().map { it.toDomain() }

    /** Errores de los últimos [DIAS_REPASO] días, para las actividades de refuerzo. */
    override suspend fun obtenerErroresParaRepaso(): List<HistorialError> =
        dao.getDesde(reloj() - TimeUnit.DAYS.toMillis(DIAS_REPASO)).map { it.toDomain() }

    override suspend fun registrarError(error: HistorialError) {
        dao.insertar(
            HistorialErrorEntity(
                id = error.id,
                tipoDesafio = error.tipoDesafio,
                actividadId = error.actividadId,
                descripcion = error.descripcion,
                fecha = error.fecha
            )
        )
    }

    private companion object {
        const val DIAS_REPASO = 7L
    }
}

private fun HistorialErrorEntity.toDomain() = HistorialError(
    id = id,
    tipoDesafio = tipoDesafio,
    actividadId = actividadId,
    descripcion = descripcion,
    fecha = fecha
)
