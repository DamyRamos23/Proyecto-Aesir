package com.aesir.odin.domain.usecase.flashcards

import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.TarjetaFlashcard
import com.aesir.odin.domain.repository.HistorialRepository
import java.util.UUID

/** A5.1: registra en el historial de errores la tarjeta que el alumno falló. */
class RegistrarErrorFlashcardUseCase(
    private val repository: HistorialRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(tarjeta: TarjetaFlashcard, opcionElegida: String) {
        repository.registrarError(
            HistorialError(
                id = UUID.randomUUID().toString(),
                tipoDesafio = TIPO_FLASHCARD,
                actividadId = tarjeta.id,
                descripcion = "Flashcard: «${tarjeta.concepto}» confundido con «$opcionElegida»",
                fecha = reloj()
            )
        )
    }

    companion object {
        const val TIPO_FLASHCARD = "FLASHCARD"
    }
}
