package com.aesir.odin.domain.usecase.preguntaflash

import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import kotlin.random.Random

/**
 * CU-06 paso 03 / RF-32: elige al azar una pregunta de un tema cuya lección
 * interactiva ya fue completada. Devuelve null si el alumno aún no completó
 * ningún tema (o los temas completados no tienen preguntas).
 */
class ObtenerPreguntaFlashUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val repository: PreguntaFlashRepository,
    private val aleatorio: Random = Random.Default
) {
    suspend operator fun invoke(): PreguntaFlash? {
        val completados = roadmapRepository.obtenerTemas().filter { it.completado }
        for (tema in completados.shuffled(aleatorio)) {
            repository.obtenerPreguntaAleatoriaDeTema(tema.id)?.let { return it }
        }
        return null
    }
}
