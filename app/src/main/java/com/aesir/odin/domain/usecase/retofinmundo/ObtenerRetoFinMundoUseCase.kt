package com.aesir.odin.domain.usecase.retofinmundo

import com.aesir.odin.domain.hidromiel.EstadoNivel
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.DisponibilidadReto
import com.aesir.odin.domain.model.Ejercicio
import com.aesir.odin.domain.model.PreguntaReto
import com.aesir.odin.domain.model.ReglasReto
import com.aesir.odin.domain.model.RetoFinMundo
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.repository.LeccionRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import kotlin.random.Random

/**
 * Pasos 2 y 3 del CU-08: verifica que se puedan enfrentar el reto y lo arma.
 *
 * - A2.1: si el MOD-01 dice que el Ragnarök sigue bloqueado, regresa los
 *   temas que faltan por completar (RF-54, RF-55).
 * - Arma el reto con [ReglasReto.PREGUNTAS] preguntas al azar que cubren
 *   todos los temas del mundo (RF-56): primero una por tema y el resto al
 *   azar entre todas las demás.
 * - [excluir] son preguntas de un intento anterior; se prefieren otras para
 *   que el reintento tenga un nuevo conjunto de preguntas (RF-64).
 */
class ObtenerRetoFinMundoUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val leccionRepository: LeccionRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val random: Random = Random.Default
) {

    suspend operator fun invoke(mundoId: String, excluir: Set<String> = emptySet()): DisponibilidadReto {
        val temas = roadmapRepository.obtenerTemasPorMundo(mundoId)
        val ragnarok = temas.firstOrNull { it.esRagnarok } ?: return DisponibilidadReto.SinPreguntas
        val temasNormales = temas.filter { !it.esRagnarok && !it.esMinijuego }.sortedBy { it.orden }

        val estados = moduloHidromiel.obtenerEstadoRoadmap(idUsuario, mundoId).associateBy { it.idNivel }
        if (estados[ragnarok.id]?.estado == EstadoNivel.BLOQUEADO) {
            val pendientes = temas
                .filter { !it.esRagnarok }
                .filter { tema -> estados[tema.id]?.let { it.estado != EstadoNivel.SUPERADO } == true }
                .sortedWith(compareBy({ it.anillo }, { it.orden }))
                .map { it.nombre }
            return DisponibilidadReto.Bloqueado(pendientes)
        }

        val ejerciciosPorTema = temasNormales.associateWith { tema ->
            leccionRepository.obtenerLeccionesPorTema(tema.id).flatMap { it.ejercicios }
        }
        val preguntas = seleccionar(ejerciciosPorTema, excluir)
        if (preguntas.isEmpty()) return DisponibilidadReto.SinPreguntas
        return DisponibilidadReto.Disponible(RetoFinMundo(mundoId, ragnarok.id, preguntas))
    }

    fun seleccionar(ejerciciosPorTema: Map<Tema, List<Ejercicio>>, excluir: Set<String> = emptySet()): List<PreguntaReto> {
        // Dentro de cada tema van primero las preguntas que no se vieron en el intento anterior
        val ordenadasPorTema = ejerciciosPorTema.mapValues { (_, ejercicios) ->
            ejercicios.shuffled(random).sortedBy { it.id in excluir }
        }.filterValues { it.isNotEmpty() }

        val elegidas = ordenadasPorTema.map { (tema, ejercicios) -> tema to ejercicios.first() }.toMutableList()
        val resto = ordenadasPorTema.flatMap { (tema, ejercicios) -> ejercicios.drop(1).map { tema to it } }
            .shuffled(random)
            .sortedBy { it.second.id in excluir }
        elegidas += resto.take((ReglasReto.PREGUNTAS - elegidas.size).coerceAtLeast(0))

        return elegidas.take(ReglasReto.PREGUNTAS).shuffled(random).map { (tema, ejercicio) -> aPregunta(tema, ejercicio) }
    }

    private fun aPregunta(tema: Tema, ejercicio: Ejercicio): PreguntaReto {
        val orden = ejercicio.opciones.indices.shuffled(random)
        return PreguntaReto(
            id = ejercicio.id,
            temaId = tema.id,
            temaNombre = tema.nombre,
            enunciado = ejercicio.enunciado,
            opciones = orden.map { ejercicio.opciones[it] },
            respuestasCorrectas = ejercicio.respuestasCorrectas.map { orden.indexOf(it) }.sorted()
        )
    }
}
