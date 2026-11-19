package com.aesir.odin.domain.eastereggs

/**
 * Reconoce una secuencia de toques (por ejemplo, índices de mundos). Si pasa
 * más de [pausaMaximaMs] entre dos toques, la secuencia se reinicia.
 */
class CombinacionSecreta(
    private val secuencia: List<Int>,
    private val pausaMaximaMs: Long = 1_500
) {
    private var progreso = 0
    private var ultimoToque: Long? = null

    /** Registra un toque; devuelve true cuando se completa la combinación. */
    fun registrar(valor: Int, ahoraMs: Long = System.currentTimeMillis()): Boolean {
        if (secuencia.isEmpty()) return false
        val ultimo = ultimoToque
        if (ultimo != null && ahoraMs - ultimo > pausaMaximaMs) progreso = 0
        ultimoToque = ahoraMs
        progreso = when {
            valor == secuencia[progreso] -> progreso + 1
            valor == secuencia[0] -> 1
            else -> 0
        }
        if (progreso == secuencia.size) {
            progreso = 0
            ultimoToque = null
            return true
        }
        return false
    }
}
