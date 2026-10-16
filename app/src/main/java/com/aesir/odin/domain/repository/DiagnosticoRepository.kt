package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.EstadoDiagnostico
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.model.ResultadoDiagnostico

/** Contrato del CU-10 (capa Interfaces del diagrama de clases). */
interface DiagnosticoRepository {
    suspend fun obtenerPreguntasPorMundo(mundoId: String, cantidad: Int): List<PreguntaDiagnostica>
    suspend fun estadoDiagnostico(mundoId: String): EstadoDiagnostico
    suspend fun guardarResultado(resultado: ResultadoDiagnostico)
    suspend fun registrarOmision(mundoId: String)
}
