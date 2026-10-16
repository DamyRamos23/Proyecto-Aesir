package com.aesir.odin.domain.usecase.diagnostico

import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.repository.DiagnosticoRepository
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import kotlin.random.Random

/**
 * Repaso del mundo en el pozo de Mímir (se abre al superar el Ragnarök, o en
 * modo admin). Toma [NUM_PREGUNTAS] preguntas al azar del banco compartido,
 * repartidas entre las lecciones del mundo; si el banco no alcanza, usa las
 * de la evaluación diagnóstica. No registra nada ni da hidromiel.
 */
class ObtenerPreguntasRepasoUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val preguntaRepository: PreguntaFlashRepository,
    private val diagnosticoRepository: DiagnosticoRepository,
    private val random: Random = Random.Default
) {
    companion object {
        const val NUM_PREGUNTAS = 10
    }

    suspend operator fun invoke(mundoId: String): List<PreguntaDiagnostica> {
        val lecciones = roadmapRepository.obtenerTemasPorMundo(mundoId)
            .filter { !it.esRagnarok && !it.esMinijuego }
            .sortedBy { it.orden }
        // Se reparten por turnos entre las lecciones para que todas salgan
        val porTema = lecciones.map {
            ArrayDeque(preguntaRepository.obtenerPreguntasParaReto(listOf(it.id), 100).shuffled(random))
        }
        val elegidas = mutableListOf<PreguntaFlash>()
        var i = 0
        while (elegidas.size < NUM_PREGUNTAS && porTema.any { it.isNotEmpty() }) {
            val cola = porTema[i % porTema.size]
            if (cola.isNotEmpty()) elegidas += cola.removeFirst()
            i++
        }
        if (elegidas.isEmpty()) return diagnosticoRepository.obtenerPreguntasPorMundo(mundoId, NUM_PREGUNTAS)
        val orden = lecciones.associate { it.id to it.orden }
        return elegidas
            .sortedBy { orden[it.temaId] ?: 0 }
            .map { PreguntaDiagnostica(it.id, mundoId, it.temaId, it.enunciado, it.opciones, it.respuestasCorrectas) }
    }
}
