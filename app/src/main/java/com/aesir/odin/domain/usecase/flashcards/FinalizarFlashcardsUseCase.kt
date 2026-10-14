package com.aesir.odin.domain.usecase.flashcards

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.PilaFlashcards
import com.aesir.odin.domain.model.ResultadoFlashcards
import com.aesir.odin.domain.repository.FlashcardsRepository
import com.aesir.odin.domain.usecase.leccion.RegistrarLeccionCompletadaUseCase

/**
 * Cierra las flashcards cuando la pila queda vacía (pasos 8 y 9):
 * 1. Guarda el resultado con el puntaje (aciertos al primer intento, 0 a 100).
 * 2. Registra la actividad del tema como completada (RegistrarLeccionCompletadaUseCase,
 *    el mismo de CU-05 y CU-07).
 * 3. Avisa al MOD-01 que el nivel se superó: él otorga la recompensa y abre
 *    los niveles conectados.
 */
class FinalizarFlashcardsUseCase(
    private val repository: FlashcardsRepository,
    private val registrarCompletadaUseCase: RegistrarLeccionCompletadaUseCase,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(temaId: String, pila: PilaFlashcards): ResultadoFlashcards {
        require(pila.terminada) { "La pila todavía tiene tarjetas" }
        val resultado = ResultadoFlashcards(
            temaId = temaId,
            puntaje = pila.puntaje,
            aciertosPrimerIntento = pila.aciertosPrimerIntento,
            errores = pila.errores,
            totalTarjetas = pila.total,
            fecha = reloj()
        )
        repository.guardarResultado(resultado)
        registrarCompletadaUseCase(temaId)
        moduloHidromiel.alFinalizarNivel(
            NivelFinalizado(idUsuario, temaId, resultado.puntaje, ResultadoNivel.SUPERADO)
        )
        return resultado
    }
}
