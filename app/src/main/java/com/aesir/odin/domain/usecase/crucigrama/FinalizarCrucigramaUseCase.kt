package com.aesir.odin.domain.usecase.crucigrama

import com.aesir.odin.domain.racha.ActividadTerminada
import com.aesir.odin.domain.racha.ModuloCarreraSol
import com.aesir.odin.domain.racha.TipoActividad
import com.aesir.odin.domain.racha.fechaLocal
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.Crucigrama
import com.aesir.odin.domain.model.ResultadoCrucigrama
import com.aesir.odin.domain.repository.CrucigramaRepository
import com.aesir.odin.domain.usecase.leccion.RegistrarLeccionCompletadaUseCase

/**
 * Cierra el crucigrama (fase 3 del diagrama de secuencia):
 * 1. Construye el ResultadoCrucigrama y lo guarda.
 * 2. Marca el tema como completado (RegistrarLeccionCompletadaUseCase, reutilizado de CU-05).
 * 3. Avisa al MOD-01 que el nivel se superó: el desbloqueo del siguiente nivel
 *    y la recompensa de hidromiel los decide el módulo de gamificación.
 *
 * Las palabras reveladas con la ayuda "Revelar palabra" no cuentan como aciertos.
 */
class FinalizarCrucigramaUseCase(
    private val crucigramaRepository: CrucigramaRepository,
    private val registrarCompletadaUseCase: RegistrarLeccionCompletadaUseCase,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    /** MOD-05: terminar la actividad sin abandonarla cuenta para la racha. */
    private val moduloCarreraSol: ModuloCarreraSol? = null,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(crucigrama: Crucigrama, puntaje: Int, errores: Int): ResultadoCrucigrama {
        val resultado = ResultadoCrucigrama(
            temaId = crucigrama.temaId,
            puntajeFinal = puntaje,
            aciertos = crucigrama.palabras.count { it.resuelta && !it.revelada },
            erroresCometidos = errores,
            fecha = reloj()
        )
        crucigramaRepository.guardarResultado(resultado)
        registrarCompletadaUseCase(crucigrama.temaId)
        moduloHidromiel.alFinalizarNivel(
            NivelFinalizado(idUsuario, crucigrama.temaId, porcentaje(crucigrama, puntaje), ResultadoNivel.SUPERADO)
        )
        moduloCarreraSol?.alTerminarActividad(ActividadTerminada(idUsuario, TipoActividad.NIVEL_ROADMAP, abandonada = false, fechaFin = fechaLocal(resultado.fecha)))
        return resultado
    }

    /** Puntaje en escala de 0 a 100, que es la que usa el MOD-01 para el bono. */
    fun porcentaje(crucigrama: Crucigrama, puntaje: Int): Int {
        val maximo = crucigrama.palabras.size * ActualizarPuntajeUseCase.PUNTOS_POR_ACIERTO
        return if (maximo == 0) 0 else (puntaje * 100 / maximo).coerceIn(0, 100)
    }
}
