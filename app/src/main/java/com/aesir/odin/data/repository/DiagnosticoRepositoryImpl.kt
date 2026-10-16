package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.DiagnosticoDao
import com.aesir.odin.data.local.entity.ResultadoDiagnosticoEntity
import com.aesir.odin.domain.model.EstadoDiagnostico
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.model.ResultadoDiagnostico
import com.aesir.odin.domain.repository.DiagnosticoRepository

/** CU-10: traduce entre las entidades de Room y los modelos de Negocio. */
class DiagnosticoRepositoryImpl(
    private val dao: DiagnosticoDao,
    private val reloj: () -> Long = System::currentTimeMillis
) : DiagnosticoRepository {

    override suspend fun obtenerPreguntasPorMundo(mundoId: String, cantidad: Int): List<PreguntaDiagnostica> =
        dao.getPreguntasByMundoId(mundoId, cantidad).map {
            PreguntaDiagnostica(it.id, it.mundoId, it.temaId, it.enunciado, it.opciones, it.respuestasCorrectas)
        }

    override suspend fun estadoDiagnostico(mundoId: String): EstadoDiagnostico =
        dao.getResultado(mundoId)
            ?.let { fila -> EstadoDiagnostico.entries.firstOrNull { it.name == fila.estado } }
            ?: EstadoDiagnostico.PENDIENTE

    override suspend fun guardarResultado(resultado: ResultadoDiagnostico) {
        dao.insertarResultado(
            ResultadoDiagnosticoEntity(
                mundoId = resultado.mundoId,
                estado = resultado.estado.name,
                aciertos = resultado.aciertos,
                totalPreguntas = resultado.totalPreguntas,
                temasConocidos = resultado.temasConocidos,
                fecha = resultado.fecha
            )
        )
    }

    /** A1.1: la evaluación queda omitida y ya no se puede volver a presentar. */
    override suspend fun registrarOmision(mundoId: String) {
        dao.insertarResultado(
            ResultadoDiagnosticoEntity(
                mundoId = mundoId,
                estado = EstadoDiagnostico.OMITIDA.name,
                aciertos = 0,
                totalPreguntas = 0,
                temasConocidos = emptyList(),
                fecha = reloj()
            )
        )
    }
}
