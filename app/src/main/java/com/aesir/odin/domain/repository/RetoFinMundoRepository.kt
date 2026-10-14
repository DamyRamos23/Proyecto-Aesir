package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.IntentoRetoFinMundo

interface RetoFinMundoRepository {
    suspend fun guardarIntento(intento: IntentoRetoFinMundo)
    suspend fun obtenerIntentos(mundoId: String): List<IntentoRetoFinMundo>
}
