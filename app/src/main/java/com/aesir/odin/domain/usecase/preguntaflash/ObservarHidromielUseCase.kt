package com.aesir.odin.domain.usecase.preguntaflash

import com.aesir.odin.domain.repository.HidromielFlashRepository
import kotlinx.coroutines.flow.Flow

/** RF-88, RF-89: hidromiel acumulada del alumno, en vivo (la pregunta flash da y quita hidromiel). */
class ObservarHidromielUseCase(
    private val hidromiel: HidromielFlashRepository
) {
    operator fun invoke(): Flow<Int> = hidromiel.observarSaldo()
}
