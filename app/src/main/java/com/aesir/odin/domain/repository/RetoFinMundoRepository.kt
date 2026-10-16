package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.IntentoRetoFinMundo

/** Historial de intentos del CU-08 Reto de fin de mundo. */
interface RetoFinMundoRepository {
    suspend fun guardarIntento(intento: IntentoRetoFinMundo)
    suspend fun obtenerIntentos(mundoId: String): List<IntentoRetoFinMundo>
}
