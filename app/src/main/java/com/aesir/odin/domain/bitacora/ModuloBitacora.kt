package com.aesir.odin.domain.bitacora

import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.repository.HistorialRepository
import java.util.UUID

/** Actividad que produjo un fallo en la bitácora de errores (MOD-06). */
enum class OrigenFallo { LECCION, MINIJUEGO, RETO, FORJA }

/** Un fallo que se anota en la bitácora: nunca incluye la respuesta correcta. */
data class FalloBitacora(
    val origen: OrigenFallo,
    val actividadId: String,
    val descripcion: String,
    val fecha: Long
)

/**
 * MOD-06 Bitácora de errores (solo lo que usa la forja del CU-02).
 * Guarda cada fallo en el historial de errores existente, que es el que
 * alimenta el repaso del pozo de Mímir.
 */
class ModuloBitacora(private val historial: HistorialRepository) {

    suspend fun registrarFallo(fallo: FalloBitacora) {
        historial.registrarError(
            HistorialError(
                id = UUID.randomUUID().toString(),
                tipoDesafio = fallo.origen.name,
                actividadId = fallo.actividadId,
                descripcion = fallo.descripcion,
                fecha = fallo.fecha
            )
        )
    }
}
