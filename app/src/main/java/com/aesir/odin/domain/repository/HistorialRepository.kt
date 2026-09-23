package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.HistorialError

interface HistorialRepository {
    suspend fun obtenerErrores(): List<HistorialError>
    suspend fun obtenerErroresParaRepaso(): List<HistorialError>
    suspend fun registrarError(error: HistorialError)
}
