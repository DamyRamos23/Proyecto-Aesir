package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ParConcepto
import com.aesir.odin.domain.model.ResultadoMemorama

/** Contrato del CU-04 (capa Interfaces del diagrama de clases). */
interface MemoramaRepository {
    suspend fun obtenerParesPorMundo(mundoId: String): List<ParConcepto>
    suspend fun mundoTieneMemorama(mundoId: String): Boolean
    suspend fun guardarResultado(resultado: ResultadoMemorama)
}
