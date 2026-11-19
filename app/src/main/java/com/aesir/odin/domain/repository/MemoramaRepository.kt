package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ParConcepto
import com.aesir.odin.domain.model.ResultadoMemorama

/** Contrato del CU-04 (capa Interfaces del diagrama de clases). */
interface MemoramaRepository {
    suspend fun obtenerParesPorMundo(mundoId: String): List<ParConcepto>

    /** RF-58: pares de concepto y definición del tema al que pertenece el nivel. */
    suspend fun obtenerParesPorNivel(nivelId: String): List<ParConcepto> = emptyList()
    suspend fun mundoTieneMemorama(mundoId: String): Boolean
    suspend fun guardarResultado(resultado: ResultadoMemorama)
}
