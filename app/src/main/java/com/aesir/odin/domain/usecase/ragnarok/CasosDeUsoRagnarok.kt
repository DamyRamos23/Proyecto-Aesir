package com.aesir.odin.domain.usecase.ragnarok

import com.aesir.odin.domain.hidromiel.EstadoNivel
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.DisponibilidadReto
import com.aesir.odin.domain.model.Ejercicio
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.IntentoRetoFinMundo
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.PreguntaReto
import com.aesir.odin.domain.model.ReglasReto
import com.aesir.odin.domain.model.ResultadoRetoFinMundo
import com.aesir.odin.domain.model.RetoFinMundo
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.model.TemaConErrores
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.LeccionRepository
import com.aesir.odin.domain.repository.RetoFinMundoRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import java.util.UUID
import kotlin.random.Random

/**
 * Pasos 2 y 3 del CU-08: verifica que se pueda enfrentar el reto y lo arma.
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
                .filter { tema -> estados[tema.id]?.let { it.estado != EstadoNivel.SUPERADO } ?: false }
                .sortedWith(compareBy({ it.anillo }, { it.orden }))
                .map { it.nombre }
            return DisponibilidadReto.Bloqueado(pendientes)
        }

        val ejerciciosPorTema = temasNormales.map { tema ->
            tema to leccionRepository.obtenerLeccionesPorTema(tema.id).flatMap { it.ejercicios }
        }
        val preguntas = seleccionar(ejerciciosPorTema, excluir)
        if (preguntas.isEmpty()) return DisponibilidadReto.SinPreguntas
        return DisponibilidadReto.Disponible(RetoFinMundo(mundoId, ragnarok.id, preguntas))
    }

    fun seleccionar(ejerciciosPorTema: List<Pair<Tema, List<Ejercicio>>>, excluir: Set<String> = emptySet()): List<PreguntaReto> {
        // Dentro de cada tema van primero las preguntas que no se vieron en el intento anterior
        val ordenadasPorTema = ejerciciosPorTema.mapNotNull { (tema, ejercicios) ->
            val mezcla = ejercicios.shuffled(random)
            val ordenadas = mezcla.filter { it.id !in excluir } + mezcla.filter { it.id in excluir }
            if (ordenadas.isEmpty()) null else tema to ordenadas
        }
        val elegidas = ordenadasPorTema.map { it.first to it.second.first() }.toMutableList()
        val restoMezclado = ordenadasPorTema
            .flatMap { (tema, ejercicios) -> ejercicios.drop(1).map { tema to it } }
            .shuffled(random)
        val resto = restoMezclado.filter { it.second.id !in excluir } + restoMezclado.filter { it.second.id in excluir }
        elegidas += resto.take((ReglasReto.PREGUNTAS - elegidas.size).coerceAtLeast(0))

        return elegidas.take(ReglasReto.PREGUNTAS)
            .shuffled(random)
            .map { (tema, ejercicio) -> aPregunta(tema, ejercicio) }
    }

    private fun aPregunta(tema: Tema, ejercicio: Ejercicio): PreguntaReto {
        val orden = ejercicio.opciones.indices.shuffled(random)
        return PreguntaReto(
            id = ejercicio.id,
            temaId = tema.id,
            temaNombre = tema.nombre,
            enunciado = ejercicio.enunciado,
            opciones = orden.map { ejercicio.opciones[it] },
            respuestasCorrectas = ejercicio.respuestasCorrectas.map { orden.indexOf(it) }.filter { it >= 0 }.sorted()
        )
    }
}

/**
 * A7.1: registra en el historial de errores la respuesta incorrecta o
 * incompleta, indicando el tema de la pregunta (RF-59).
 */
class RegistrarErrorRetoUseCase(
    private val repository: HistorialRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    companion object {
        const val TIPO_RETO_FIN_MUNDO = "RETO_FIN_MUNDO"
    }

    suspend operator fun invoke(pregunta: PreguntaReto, estado: EstadoRespuesta) {
        val como = if (estado == EstadoRespuesta.INCOMPLETA) "incompleta" else "incorrecta"
        repository.registrarError(
            HistorialError(
                id = UUID.randomUUID().toString(),
                tipoDesafio = TIPO_RETO_FIN_MUNDO,
                actividadId = pregunta.id,
                descripcion = "Reto de fin de mundo, tema «${pregunta.temaNombre}»: respuesta $como",
                fecha = reloj()
            )
        )
    }
}

/**
 * Pasos 9 a 11 del CU-08 (y A10.1, A10.2):
 * 1. Calcula el puntaje: porcentaje de preguntas respondidas correctamente (RF-60).
 * 2. El reto se supera con [ReglasReto.PUNTAJE_MINIMO] o más (RF-61).
 * 3. Guarda el intento (RF-66) y avisa al MOD-01: él otorga la recompensa y,
 *    al superar el Ragnarök, desbloquea el siguiente mundo (RF-62).
 * 4. Si se superó, el tema de fin de mundo queda marcado como completado.
 *
 * [respuestas] lleva el estado de cada pregunta contestada; una incompleta no
 * cuenta como acierto.
 */
class FinalizarRetoFinMundoUseCase(
    private val repository: RetoFinMundoRepository,
    private val roadmapRepository: RoadmapRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(reto: RetoFinMundo, respuestas: List<Pair<PreguntaReto, EstadoRespuesta>>): ResultadoRetoFinMundo {
        val total = reto.preguntas.size
        val aciertos = respuestas.count { it.second == EstadoRespuesta.CORRECTA }
        val puntaje = if (total == 0) 0 else aciertos * 100 / total
        val superado = puntaje >= ReglasReto.PUNTAJE_MINIMO
        val fecha = reloj()

        // Temas con errores, de más a menos errores (y en el orden en que aparecieron)
        val conErrores = respuestas
            .filter { it.second != EstadoRespuesta.CORRECTA }
            .groupBy { it.first.temaId }
            .entries
            .mapIndexed { i, (id, lista) -> i to TemaConErrores(id, lista.first().first.temaNombre, lista.size) }
            .sortedWith(compareByDescending<Pair<Int, TemaConErrores>> { it.second.errores }.thenBy { it.first })
            .map { it.second }

        repository.guardarIntento(
            IntentoRetoFinMundo(reto.mundoId, puntaje, aciertos, total - aciertos, superado, abandonado = false, fecha = fecha)
        )
        moduloHidromiel.alFinalizarNivel(
            NivelFinalizado(idUsuario, reto.ragnarokId, puntaje, if (superado) ResultadoNivel.SUPERADO else ResultadoNivel.NO_SUPERADO)
        )

        var siguiente: Mundo? = null
        var ultimo = false
        if (superado) {
            roadmapRepository.marcarLeccionCompletada(reto.ragnarokId)
            val mundos = roadmapRepository.obtenerMundos().sortedBy { it.orden }
            val actual = mundos.indexOfFirst { it.id == reto.mundoId }
            if (actual >= 0 && actual + 1 < mundos.size) siguiente = mundos[actual + 1]
            ultimo = siguiente == null
        }

        return ResultadoRetoFinMundo(
            mundoId = reto.mundoId,
            puntaje = puntaje,
            aciertos = aciertos,
            errores = total - aciertos,
            superado = superado,
            fecha = fecha,
            temasConErrores = conErrores,
            mundoDesbloqueadoId = siguiente?.id,
            mundoDesbloqueadoNombre = siguiente?.nombre,
            ultimoMundo = ultimo
        )
    }
}

/**
 * A6.1: el alumno abandona el reto. Se registra como no superado, sin
 * puntaje, y el MOD-01 descuenta la entrada pagada.
 */
class AbandonarRetoFinMundoUseCase(
    private val repository: RetoFinMundoRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(reto: RetoFinMundo, aciertosHastaAhora: Int) {
        repository.guardarIntento(
            IntentoRetoFinMundo(
                mundoId = reto.mundoId,
                puntaje = 0,
                aciertos = aciertosHastaAhora,
                errores = reto.preguntas.size - aciertosHastaAhora,
                superado = false,
                abandonado = true,
                fecha = reloj()
            )
        )
        moduloHidromiel.alFinalizarNivel(NivelFinalizado(idUsuario, reto.ragnarokId, 0, ResultadoNivel.ABANDONADO))
    }
}
