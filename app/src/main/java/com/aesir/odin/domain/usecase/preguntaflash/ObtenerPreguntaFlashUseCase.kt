package com.aesir.odin.domain.usecase.preguntaflash

import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import kotlin.random.Random

/**
 * CU-06 paso 03 / RF-32: elige al azar una pregunta de un tema cuya lección
 * interactiva ya fue completada. Devuelve null si el alumno aún no completó
 * ningún tema (o los temas completados no tienen preguntas).
 *
 * Recuerda las últimas [MEMORIA_RECIENTES] preguntas mostradas y no las repite
 * mientras haya otras disponibles. Vive en el contenedor, así que la memoria
 * dura lo que dura la sesión de la app.
 */
class ObtenerPreguntaFlashUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val repository: PreguntaFlashRepository,
    private val aleatorio: Random = Random.Default
) {
    private val recientes = ArrayDeque<String>()

    suspend operator fun invoke(): PreguntaFlash? {
        val temas = roadmapRepository.obtenerTemas()
        // En modo admin todas las runas cuentan como estudiadas, igual que en el contrarreloj.
        val completados = (if (ModoAdmin.estaActivo) temas.filter { !it.esRagnarok && !it.esMinijuego } else temas.filter { it.completado })
            .map { it.id }
        if (completados.isEmpty()) return null

        val candidatas = repository.obtenerPreguntasParaReto(completados, MAXIMO_CANDIDATAS)
            .shuffled(aleatorio)
        if (candidatas.isEmpty()) return null

        // Siempre queda al menos una candidata libre: se bloquean como mucho size - 1.
        val bloqueadas = recientes.takeLast(minOf(MEMORIA_RECIENTES, candidatas.size - 1)).toSet()
        val elegida = candidatas.first { it.id !in bloqueadas }

        recientes.addLast(elegida.id)
        while (recientes.size > MEMORIA_RECIENTES) recientes.removeFirst()
        return elegida
    }

    companion object {
        /** Cuántas preguntas recientes se evitan antes de permitir que se repitan. */
        const val MEMORIA_RECIENTES = 20

        /** Tope de preguntas que se consultan al banco en cada elección. */
        private const val MAXIMO_CANDIDATAS = 500
    }
}
