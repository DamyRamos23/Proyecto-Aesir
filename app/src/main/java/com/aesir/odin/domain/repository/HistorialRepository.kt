package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.HistorialError

/** MOD-06: sin operación de borrado (RN-35). */
interface HistorialRepository {
    suspend fun buscarPorPregunta(preguntaId: String): HistorialError?
    suspend fun registrar(error: HistorialError)
    suspend fun actualizar(error: HistorialError)
    suspend fun obtenerErroresParaRepaso(): List<HistorialError>
}
