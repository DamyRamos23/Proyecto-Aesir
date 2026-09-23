package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.Tema

interface RoadmapRepository {

    // Paso 5: obtenerTema(temaId) -> Paso 9: Tema
    suspend fun obtenerTema(temaId: String): Tema?

    // Paso 15: marcarIntroduccionVista(temaId) -> Paso 19: Unit
    suspend fun marcarIntroduccionVista(temaId: String)
}

