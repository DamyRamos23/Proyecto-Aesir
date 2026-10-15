package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.MundoDao
import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.data.local.entity.TemaEntity
import com.aesir.odin.domain.hidromiel.CatalogoNiveles
import com.aesir.odin.domain.hidromiel.NivelInfo
import com.aesir.odin.domain.hidromiel.TipoNivel
import com.aesir.odin.domain.model.Actividad
import com.aesir.odin.domain.model.ArbolHabilidades

/**
 * Catálogo de niveles del MOD-01 sobre las tablas de temas y mundos: cada
 * tema es un nivel.
 *
 * Árbol de habilidades de tres anillos (ArbolHabilidades): las lecciones en el
 * anillo interior, conectadas en círculo; los minijuegos en el segundo, cada
 * uno conectado a las dos lecciones que tiene más cerca; y los retos en el
 * tercero, cada uno conectado a los dos minijuegos que tiene más cerca.
 *
 * Los niveles cuya actividad todavía no se puede jugar (memorama, por definir) no entran
 * al catálogo: no se cobran, no abren a nadie y no cuentan
 * para el Ragnarök hasta que se programen (Actividad.jugable).
 */
class CatalogoNivelesImpl(
    private val temaDao: TemaDao,
    private val mundoDao: MundoDao
) : CatalogoNiveles {

    override suspend fun obtener(idNivel: String): NivelInfo {
        val tema = temaDao.getById(idNivel)
        return aNivel(tema, arbolDe(tema.mundoId))
    }

    override suspend fun nivelesDelMundo(idMundo: String): List<NivelInfo> {
        val arbol = arbolDe(idMundo)
        val ragnarok = temaDao.getByMundoId(idMundo).filter { it.tipo == RAGNAROK }
        return arbol.todos.filter { it.jugable() }
            .plus(ragnarok)
            .map { aNivel(it, arbol) }
    }

    override suspend fun conectados(idNivel: String): List<String> {
        val tema = temaDao.getById(idNivel)
        val arbol = arbolDe(tema.mundoId)
        val jugables = arbol.todos.filter { it.jugable() }.map { it.id }
        if (tema.tipo == RAGNAROK) return jugables
        // La geometría se calcula con todos los niveles, para que coincida con el escudo
        return ArbolHabilidades.conexiones(arbol.lecciones.map { it.id }, arbol.minijuegos.map { it.id }, arbol.retos.map { it.id })[idNivel]
            .orEmpty()
            .filter { it in jugables }
    }

    override suspend fun siguienteMundo(idMundo: String): String? {
        val mundos = mundoDao.getAll().sortedBy { it.orden }
        val i = mundos.indexOfFirst { it.id == idMundo }
        return if (i < 0) null else mundos.getOrNull(i + 1)?.id
    }

    private data class Arbol(val lecciones: List<TemaEntity>, val minijuegos: List<TemaEntity>, val retos: List<TemaEntity>) {
        val todos: List<TemaEntity> get() = lecciones + minijuegos + retos
    }

    private suspend fun arbolDe(idMundo: String): Arbol {
        val normales = temaDao.getByMundoId(idMundo).filter { it.tipo != RAGNAROK }
        return Arbol(
            lecciones = normales.filter { it.anillo < ArbolHabilidades.ANILLO_MINIJUEGOS }.sortedBy { it.orden },
            minijuegos = normales.filter { it.anillo == ArbolHabilidades.ANILLO_MINIJUEGOS }.sortedBy { it.orden },
            retos = normales.filter { it.anillo == ArbolHabilidades.ANILLO_RETOS }.sortedBy { it.orden }
        )
    }

    private fun TemaEntity.jugable() = Actividad.desde(actividad).jugable

    private fun aNivel(tema: TemaEntity, arbol: Arbol) = NivelInfo(
        idNivel = tema.id,
        idMundo = tema.mundoId,
        nombre = tema.nombre,
        tipo = if (tema.tipo == RAGNAROK) TipoNivel.RAGNAROK else TipoNivel.NORMAL,
        // La entrada gratuita es la primera lección del anillo interior
        esPrimero = tema.tipo != RAGNAROK && tema.id == arbol.lecciones.firstOrNull()?.id
    )

    private companion object {
        val RAGNAROK = TipoNivel.RAGNAROK.name
    }
}
