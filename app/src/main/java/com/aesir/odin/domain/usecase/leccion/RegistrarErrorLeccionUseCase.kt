package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.bitacora.FalloBitacora
import com.aesir.odin.domain.bitacora.ModuloBitacora
import com.aesir.odin.domain.bitacora.OrigenError
import com.aesir.odin.domain.bitacora.opcionesElegidas
import com.aesir.odin.domain.model.Ejercicio
import com.aesir.odin.domain.model.Leccion

/**
 * Avisa a la bitácora (MOD-06) la respuesta incorrecta o incompleta de un
 * ejercicio de la lección (RF-74).
 *
 * @return true si [erroresActuales] alcanzó el límite y la lección debe terminar (RF-75).
 */
class RegistrarErrorLeccionUseCase(
    private val moduloBitacora: ModuloBitacora,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    operator fun invoke(leccion: Leccion, ejercicio: Ejercicio, seleccion: List<Int>, erroresActuales: Int): Boolean {
        moduloBitacora.registrarFallo(
            FalloBitacora(
                origen = OrigenError.LECCION,
                preguntaId = ejercicio.id,
                temaId = leccion.temaId,
                enunciado = ejercicio.enunciado,
                opciones = ejercicio.opciones,
                respuestaCorrecta = ejercicio.respuestasCorrectas.joinToString(", ") { ejercicio.opciones[it] },
                respuestaAlumno = opcionesElegidas(ejercicio.opciones, seleccion),
                pista = leccion.titulo,
                fecha = reloj()
            )
        )
        return erroresActuales >= LIMITE_ERRORES
    }

    companion object {
        const val LIMITE_ERRORES = 3
    }
}
