package com.aesir.odin.domain.usecase.retofinmundo

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.IntentoRetoFinMundo
import com.aesir.odin.domain.model.PreguntaReto
import com.aesir.odin.domain.model.ReglasReto
import com.aesir.odin.domain.model.ResultadoRetoFinMundo
import com.aesir.odin.domain.model.RetoFinMundo
import com.aesir.odin.domain.model.TemaConErrores
import com.aesir.odin.domain.repository.RetoFinMundoRepository
import com.aesir.odin.domain.repository.RoadmapRepository

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
    suspend operator fun invoke(
        reto: RetoFinMundo,
        respuestas: List<Pair<PreguntaReto, EstadoRespuesta>>
    ): ResultadoRetoFinMundo {
        val total = reto.preguntas.size
        val aciertos = respuestas.count { it.second == EstadoRespuesta.CORRECTA }
        val puntaje = if (total == 0) 0 else aciertos * 100 / total
        val superado = puntaje >= ReglasReto.PUNTAJE_MINIMO
        val fecha = reloj()

        val conErrores = respuestas
            .filter { it.second != EstadoRespuesta.CORRECTA }
            .groupBy { it.first.temaId }
            .map { (temaId, lista) -> TemaConErrores(temaId, lista.first().first.temaNombre, lista.size) }
            .sortedByDescending { it.errores }

        repository.guardarIntento(
            IntentoRetoFinMundo(reto.mundoId, puntaje, aciertos, total - aciertos, superado, abandonado = false, fecha = fecha)
        )
        moduloHidromiel.alFinalizarNivel(
            NivelFinalizado(
                idUsuario, reto.ragnarokId, puntaje,
                if (superado) ResultadoNivel.SUPERADO else ResultadoNivel.NO_SUPERADO
            )
        )

        var siguiente: String? = null
        var siguienteNombre: String? = null
        var ultimo = false
        if (superado) {
            roadmapRepository.marcarLeccionCompletada(reto.ragnarokId)
            val mundos = roadmapRepository.obtenerMundos().sortedBy { it.orden }
            val actual = mundos.indexOfFirst { it.id == reto.mundoId }
            val proximo = mundos.getOrNull(actual + 1)
            siguiente = proximo?.id
            siguienteNombre = proximo?.nombre
            ultimo = proximo == null
        }

        return ResultadoRetoFinMundo(
            mundoId = reto.mundoId,
            puntaje = puntaje,
            aciertos = aciertos,
            errores = total - aciertos,
            superado = superado,
            fecha = fecha,
            temasConErrores = conErrores,
            mundoDesbloqueadoId = siguiente,
            mundoDesbloqueadoNombre = siguienteNombre,
            ultimoMundo = ultimo
        )
    }
}
