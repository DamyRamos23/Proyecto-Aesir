package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.model.Leccion
import com.aesir.odin.domain.repository.LeccionRepository

class ObtenerLeccionUseCase(private val repository: LeccionRepository) {
    suspend operator fun invoke(leccionId: String): Leccion? =
        repository.obtenerLeccion(leccionId)?.let { leccion ->
            leccion.copy(ejercicios = leccion.ejercicios.sortedBy { it.orden })
        }
}
