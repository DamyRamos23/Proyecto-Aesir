package com.aesir.odin.domain.hidromiel

import com.aesir.odin.domain.hidromiel.regla.ReglaAcceso

/**
 * Evalúa las reglas en orden y gana la primera que responde:
 * 1. ReglaNivelSuperado · 2. ReglaPrimerNivel · 3. ReglaRagnarok · 4. ReglaConexionArbol
 */
class ControlAcceso(private val reglas: List<ReglaAcceso>) {

    init {
        require(reglas.isNotEmpty()) { "ControlAcceso necesita al menos una regla" }
    }

    suspend fun evaluar(idUsuario: String, nivel: NivelInfo): EstadoNivel {
        for (regla in reglas) {
            regla.evaluar(idUsuario, nivel)?.let { return it }
        }
        return EstadoNivel.BLOQUEADO
    }
}
