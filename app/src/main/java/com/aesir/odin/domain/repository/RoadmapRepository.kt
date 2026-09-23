package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.Tema

/**
 * Solo contiene lo que necesita CU-05. Al integrar CU-01 se completa con
 * obtenerMundos(), obtenerTemasPorMundo() y marcarIntroduccionVista().
 */
interface RoadmapRepository {
    suspend fun obtenerTemas(): List<Tema>
    suspend fun obtenerTema(temaId: String): Tema?
    suspend fun marcarLeccionCompletada(temaId: String)
    suspend fun desbloquearTema(temaId: String)
}
