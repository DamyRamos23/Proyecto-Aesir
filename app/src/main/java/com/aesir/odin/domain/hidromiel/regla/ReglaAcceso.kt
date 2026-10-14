package com.aesir.odin.domain.hidromiel.regla

import com.aesir.odin.domain.hidromiel.CatalogoNiveles
import com.aesir.odin.domain.hidromiel.EstadoNivel
import com.aesir.odin.domain.hidromiel.HidromielRepository
import com.aesir.odin.domain.hidromiel.NivelInfo
import com.aesir.odin.domain.hidromiel.TipoNivel

/**
 * Una regla de desbloqueo. Devuelve el estado del nivel si la regla aplica,
 * o null para que [com.aesir.odin.domain.hidromiel.ControlAcceso] pase a la siguiente.
 */
interface ReglaAcceso {
    suspend fun evaluar(idUsuario: String, nivel: NivelInfo): EstadoNivel?
}

/** 1. SUPERADO si existe NivelSuperado, jugado o por diagnóstico (RN-06). */
class ReglaNivelSuperado(private val repositorio: HidromielRepository) : ReglaAcceso {
    override suspend fun evaluar(idUsuario: String, nivel: NivelInfo): EstadoNivel? =
        if (repositorio.obtenerNivelSuperado(idUsuario, nivel.idNivel) != null) EstadoNivel.SUPERADO else null
}

/** 2. GRATUITO si es el primer nivel del mundo (E2). */
class ReglaPrimerNivel : ReglaAcceso {
    override suspend fun evaluar(idUsuario: String, nivel: NivelInfo): EstadoNivel? =
        if (nivel.esPrimero) EstadoNivel.GRATUITO else null
}

/**
 * 3. Ragnarök: BLOQUEADO si falta algún nivel del mundo, DISPONIBLE si ya
 * están todos (RN-05).
 *
 * El diagrama fija NIVELES_REQUERIDOS = 6 porque así se diseñaron los
 * mundos; aquí se cuentan los niveles normales que tenga el mundo, para que
 * funcione también con los 4 temas de prueba actuales.
 */
class ReglaRagnarok(
    private val catalogo: CatalogoNiveles,
    private val repositorio: HidromielRepository
) : ReglaAcceso {
    override suspend fun evaluar(idUsuario: String, nivel: NivelInfo): EstadoNivel? {
        if (nivel.tipo != TipoNivel.RAGNAROK) return null
        return if (nivelesFaltantes(idUsuario, nivel.idMundo) == 0) EstadoNivel.DISPONIBLE else EstadoNivel.BLOQUEADO
    }

    suspend fun nivelesFaltantes(idUsuario: String, idMundo: String): Int =
        catalogo.nivelesDelMundo(idMundo)
            .filter { it.tipo == TipoNivel.NORMAL }
            .count { repositorio.obtenerNivelSuperado(idUsuario, it.idNivel) == null }
}

/** 4. DISPONIBLE si está conectado a un nivel superado; si no, BLOQUEADO (RN-04). */
class ReglaConexionArbol(
    private val catalogo: CatalogoNiveles,
    private val repositorio: HidromielRepository
) : ReglaAcceso {
    override suspend fun evaluar(idUsuario: String, nivel: NivelInfo): EstadoNivel {
        val conectadoASuperado = catalogo.conectados(nivel.idNivel)
            .any { repositorio.obtenerNivelSuperado(idUsuario, it) != null }
        return if (conectadoASuperado) EstadoNivel.DISPONIBLE else EstadoNivel.BLOQUEADO
    }
}
