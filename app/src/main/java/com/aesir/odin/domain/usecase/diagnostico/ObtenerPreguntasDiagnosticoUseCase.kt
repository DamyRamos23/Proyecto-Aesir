package com.aesir.odin.domain.usecase.diagnostico

import com.aesir.odin.domain.model.ArbolHabilidades
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.repository.DiagnosticoRepository
import kotlin.random.Random

/**
 * RF-130: las [NUM_PREGUNTAS] preguntas de la evaluación del mundo,
 * [PREGUNTAS_POR_TEMA] de cada uno de sus 10 temas, en el orden de los temas.
 * Si un tema tiene preguntas de reserva, se eligen al azar.
 */
class ObtenerPreguntasDiagnosticoUseCase(
    private val repository: DiagnosticoRepository,
    private val random: Random = Random.Default
) {
    suspend operator fun invoke(mundoId: String): List<PreguntaDiagnostica> =
        repository.obtenerPreguntasPorMundo(mundoId, LIMITE_BANCO)
            .groupBy { it.temaId }
            .toList()
            .sortedWith(compareBy({ numeroDeTema(it.first) }, { it.first }))
            .flatMap { (_, delTema) -> delTema.shuffled(random).take(PREGUNTAS_POR_TEMA) }
            .take(NUM_PREGUNTAS)

    /** Número del tema a partir de su id (tema_uml_10 → 10), para ordenar 1, 2, …, 10. */
    private fun numeroDeTema(temaId: String): Int = temaId.substringAfterLast('_').toIntOrNull() ?: Int.MAX_VALUE

    companion object {
        const val PREGUNTAS_POR_TEMA = 2
        const val NUM_PREGUNTAS = PREGUNTAS_POR_TEMA * ArbolHabilidades.TEMAS_POR_MUNDO

        /** Cuántas preguntas del banco del mundo se leen para elegir entre ellas. */
        private const val LIMITE_BANCO = 1000
    }
}
