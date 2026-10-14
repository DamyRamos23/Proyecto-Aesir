package com.aesir.odin.domain.hidromiel

/** Estructura del roadmap que el módulo consulta (lo implementa la capa de datos). */
interface CatalogoNiveles {
    suspend fun obtener(idNivel: String): NivelInfo

    /** Niveles del mundo en orden; el Ragnarök va al final. */
    suspend fun nivelesDelMundo(idMundo: String): List<NivelInfo>

    /** Niveles unidos a [idNivel] por una rama del árbol. */
    suspend fun conectados(idNivel: String): List<String>

    /** Mundo que se abre al superar el Ragnarök de [idMundo], o null si es el último. */
    suspend fun siguienteMundo(idMundo: String): String?
}
