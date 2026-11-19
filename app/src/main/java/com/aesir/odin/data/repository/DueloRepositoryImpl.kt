package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.ConceptoEnigmaDao
import com.aesir.odin.data.local.dao.PreguntaEnigmaDao
import com.aesir.odin.data.local.dao.ResultadoDueloDao
import com.aesir.odin.data.local.entity.ResultadoDueloEntity
import com.aesir.odin.domain.model.ConceptoEnigma
import com.aesir.odin.domain.model.DueloEnigmas
import com.aesir.odin.domain.model.EstadoFinalDuelo
import com.aesir.odin.domain.model.PreguntaEnigma
import com.aesir.odin.domain.model.ResultadoDuelo
import com.aesir.odin.domain.model.TipoPregunta
import com.aesir.odin.domain.repository.DueloRepository

class DueloRepositoryImpl(
    private val conceptoDao: ConceptoEnigmaDao,
    private val preguntaDao: PreguntaEnigmaDao,
    private val resultadoDao: ResultadoDueloDao
) : DueloRepository {

    override suspend fun obtenerConceptos(temaIds: List<String>): List<ConceptoEnigma> =
        if (temaIds.isEmpty()) emptyList()
        else conceptoDao.getPorTemas(temaIds).map { ConceptoEnigma(it.id, it.temaId, it.nombre, it.explicacion) }

    override suspend fun obtenerPreguntas(): List<PreguntaEnigma> =
        preguntaDao.getAll().map { PreguntaEnigma(it.id, it.texto, TipoPregunta.valueOf(it.tipo), it.conceptosSi) }

    override suspend fun contarDuelosDelDia(inicioDia: Long): Int = resultadoDao.contarDesde(inicioDia)

    override suspend fun registrarDueloIniciado(duelo: DueloEnigmas, fecha: Long) {
        resultadoDao.insertar(
            ResultadoDueloEntity(duelo.id, duelo.conceptoVerdadero().id, null, 0, 0, duelo.cuerno.sorbos, 0, fecha)
        )
    }

    override suspend fun obtenerDueloSinTerminar(): ResultadoDuelo? = resultadoDao.getSinTerminar()?.aDominio()

    override suspend fun guardarResultado(resultado: ResultadoDuelo) {
        resultadoDao.insertar(
            ResultadoDueloEntity(
                resultado.id, resultado.conceptoId, resultado.resultado?.name, resultado.preguntasRealizadas,
                resultado.adivinanzasFallidas, resultado.sorbosSobrantes, resultado.hidromielObtenido, resultado.fecha
            )
        )
    }

    private fun ResultadoDueloEntity.aDominio() = ResultadoDuelo(
        id, conceptoId, resultado?.let(EstadoFinalDuelo::valueOf), preguntasRealizadas,
        adivinanzasFallidas, sorbosSobrantes, hidromielObtenido, fecha
    )
}
