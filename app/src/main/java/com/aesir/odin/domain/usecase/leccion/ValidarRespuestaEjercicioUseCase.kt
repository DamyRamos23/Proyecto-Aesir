package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.model.Ejercicio
import com.aesir.odin.domain.model.ResultadoRespuesta
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase

class ValidarRespuestaEjercicioUseCase(private val validador: ValidarRespuestaUseCase) {
    operator fun invoke(ejercicio: Ejercicio, seleccionadas: List<Int>): ResultadoRespuesta =
        validador(seleccionadas, ejercicio.respuestasCorrectas)
}
