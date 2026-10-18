package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.model.RetoArrastrar

/** Contrato de acceso a los retos de arrastrar y soltar (CU-09, capa Interfaces). */
interface RetoRepository {
    /** Reto registrado para el tema; lanza si el tema no tiene uno (precondición). */
    suspend fun obtenerRetoPorTema(temaId: String): RetoArrastrar

    /** Guarda el resultado y, si se aprobó, marca el reto como completado. */
    suspend fun guardarResultado(resultado: ResultadoReto)
}
