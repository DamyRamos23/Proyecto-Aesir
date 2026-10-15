package com.aesir.odin.domain.usecase.preguntaflash

import com.aesir.odin.domain.repository.HidromielFlashRepository
import kotlinx.coroutines.flow.Flow

/** RF-45: hidromiel acumulada del alumno, en vivo (la pregunta flash da y quita hidromiel). */
class ObservarHidromielUseCase(
    private val hidromiel: HidromielFlashRepository
) {
    operator fun invoke(): Flow<Int> = hidromiel.observarSaldo()
}
