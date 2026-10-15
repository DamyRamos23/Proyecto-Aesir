package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.model.RetoContrareloj
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.repository.RoadmapRepository

/** No hay ningún tema estudiado (completado) del cual tomar preguntas. */
class SinTemasEstudiadosException : Exception("No hay temas estudiados")

/** Hay temas estudiados, pero el banco no tiene preguntas para ellos. */
class SinPreguntasDisponiblesException : Exception("No hay preguntas disponibles")

/**
 * CU-11, pasos 1 y 2: arma el reto con preguntas del banco de Pregunta Flash,
 * solo de temas ya estudiados (completados) y sin repetir preguntas.
 */
class ArmarRetoContrarrelojUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val preguntaRepository: PreguntaFlashRepository
) {
    suspend operator fun invoke(): Result<RetoContrareloj> {
        val temasEstudiados = roadmapRepository.obtenerTemas()
            .filter { it.completado }
            .map { it.id }
        if (temasEstudiados.isEmpty()) return Result.failure(SinTemasEstudiadosException())

        val preguntas = preguntaRepository.obtenerPreguntasParaReto(temasEstudiados, CANTIDAD_PREGUNTAS)
        if (preguntas.isEmpty()) return Result.failure(SinPreguntasDisponiblesException())

        return Result.success(RetoContrareloj(preguntas))
    }

    companion object {
        /** Más de las que se alcanzan a contestar en 2 minutos; si se acaban, termina el reto (A7.2). */
        const val CANTIDAD_PREGUNTAS = 20
    }
}
