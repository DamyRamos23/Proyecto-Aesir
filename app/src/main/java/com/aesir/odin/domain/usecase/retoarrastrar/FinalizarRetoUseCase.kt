package com.aesir.odin.domain.usecase.retoarrastrar

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.model.RetoArrastrar
import com.aesir.odin.domain.repository.RetoRepository
import com.aesir.odin.domain.usecase.leccion.RegistrarLeccionCompletadaUseCase

/**
 * Fase 3 del CU-09: cierra el reto (aprobado o al llegar al límite de intentos).
 * 1. Calcula el puntaje: 100 − 20 por intento incorrecto; 0 si no se aprobó (A7.2).
 * 2. Guarda el ResultadoReto (el repositorio marca el reto como completado si se aprobó).
 * 3. Si se aprobó, marca la actividad del tema como completada (reutilizado de CU-05).
 * 4. Avisa al MOD-01 (módulo de gamificación): él otorga el hidromiel y abre
 *    los niveles conectados; si no se aprobó, la entrada pagada se pierde.
 */
class FinalizarRetoUseCase(
    private val retoRepository: RetoRepository,
    private val puntajeUseCase: CalcularPuntajeRetoUseCase,
    private val registrarCompletadaUseCase: RegistrarLeccionCompletadaUseCase,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(reto: RetoArrastrar, intentosFallidos: Int, aprobado: Boolean): ResultadoReto {
        val puntaje = if (aprobado) puntajeUseCase(intentosFallidos) else 0
        val resultado = ResultadoReto(
            retoId = reto.id,
            temaId = reto.temaId,
            puntaje = puntaje,
            intentos = intentosFallidos + if (aprobado) 1 else 0,
            aprobado = aprobado,
            fecha = reloj()
        )
        retoRepository.guardarResultado(resultado)
        if (aprobado) registrarCompletadaUseCase(reto.temaId)
        moduloHidromiel.alFinalizarNivel(
            NivelFinalizado(
                idUsuario, reto.temaId, puntaje,
                if (aprobado) ResultadoNivel.SUPERADO else ResultadoNivel.NO_SUPERADO
            )
        )
        return resultado
    }
}
