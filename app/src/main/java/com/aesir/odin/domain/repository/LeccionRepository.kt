package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.Leccion
import com.aesir.odin.domain.model.ResultadoLeccion

interface LeccionRepository {
    suspend fun obtenerLeccion(leccionId: String): Leccion?
    suspend fun obtenerLeccionesPorTema(temaId: String): List<Leccion>
    suspend fun guardarResultado(resultado: ResultadoLeccion)
    suspend fun marcarCompletada(leccionId: String)
}
