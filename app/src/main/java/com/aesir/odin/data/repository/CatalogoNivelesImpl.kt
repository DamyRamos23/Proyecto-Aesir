package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.MundoDao
import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.data.local.entity.TemaEntity
import com.aesir.odin.domain.hidromiel.CatalogoNiveles
import com.aesir.odin.domain.hidromiel.NivelInfo
import com.aesir.odin.domain.hidromiel.TipoNivel
import com.aesir.odin.domain.model.ArbolHabilidades

/**
 * Catálogo de niveles del MOD-01 sobre las tablas de temas y mundos: cada
 * tema es un nivel.
 *
 * Árbol de habilidades de cuatro anillos (ArbolHabilidades, SRS v3.2): las
 * lecciones de los temas 1 a 4 en el anillo interior, conectadas en círculo;
 * los minijuegos en el segundo, cada uno conectado a la lección de su tema;
 * los retos de arrastrar y soltar en el tercero, cada uno conectado a los dos
 * minijuegos que tiene más cerca; y las lecciones de los temas 5 a 10 en el
 * cuarto, conectadas entre sí y con el reto o minijuego que tienen más cerca.
 * Son 18 niveles por mundo más el Ragnarök.
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
        return arbol.todos
            .plus(ragnarok)
            .map { aNivel(it, arbol) }
    }

    override suspend fun conectados(idNivel: String): List<String> {
        val tema = temaDao.getById(idNivel)
        val arbol = arbolDe(tema.mundoId)
        val ids = arbol.todos.map { it.id }
        if (tema.tipo == RAGNAROK) return ids
        val vecinos = arbol.conexiones()[idNivel].orEmpty()
        return ids.filter { it in vecinos }
    }

    override suspend fun siguienteMundo(idMundo: String): String? {
        val mundos = mundoDao.getAll().sortedBy { it.orden }
        val i = mundos.indexOfFirst { it.id == idMundo }
        return if (i < 0) null else mundos.getOrNull(i + 1)?.id
    }

    override suspend fun mundosDesbloqueados(): List<String> =
        mundoDao.getAll().filter { it.desbloqueado }.map { it.id }
    override suspend fun idsMundos(): List<String> =
        mundoDao.getAll().sortedBy { it.orden }.map { it.id }

    private class Arbol(
        val lecciones: List<TemaEntity>,
        val minijuegos: List<TemaEntity>,
        val retos: List<TemaEntity>,
        val cuarto: List<TemaEntity>
    ) {
        val todos: List<TemaEntity> get() = lecciones + minijuegos + retos + cuarto

        fun conexiones(): Map<String, Set<String>> {
            val temaDe = minijuegos.associate { it.id to it.temaBaseId }
            return ArbolHabilidades.conexiones(
                lecciones.map { it.id }, minijuegos.map { it.id }, retos.map { it.id }, cuarto.map { it.id }
            ) { temaDe[it]?.ifEmpty { null } }
        }
    }

    private suspend fun arbolDe(idMundo: String): Arbol {
        val normales = temaDao.getByMundoId(idMundo).filter { it.tipo != RAGNAROK }
        fun delAnillo(anillo: Int) = normales.filter { it.anillo == anillo }.sortedBy { it.orden }
        return Arbol(
            lecciones = normales.filter { it.anillo <= ArbolHabilidades.ANILLO_LECCIONES }.sortedBy { it.orden },
            minijuegos = delAnillo(ArbolHabilidades.ANILLO_MINIJUEGOS),
            retos = delAnillo(ArbolHabilidades.ANILLO_RETOS),
            cuarto = delAnillo(ArbolHabilidades.ANILLO_LECCIONES_NUEVAS)
        )
    }

    private fun aNivel(tema: TemaEntity, arbol: Arbol) = NivelInfo(
        idNivel = tema.id,
        idMundo = tema.mundoId,
        nombre = tema.nombre,
        tipo = if (tema.tipo == RAGNAROK) TipoNivel.RAGNAROK else TipoNivel.NORMAL,
        // La entrada gratuita es la primera lección del anillo interior
        esPrimero = tema.tipo != RAGNAROK && tema.id == arbol.lecciones.firstOrNull()?.id,
        anillo = tema.anillo.coerceAtLeast(ArbolHabilidades.ANILLO_LECCIONES),
        idTema = tema.temaBaseId.ifEmpty { tema.id }
    )

    private companion object {
        val RAGNAROK = TipoNivel.RAGNAROK.name
    }
}
