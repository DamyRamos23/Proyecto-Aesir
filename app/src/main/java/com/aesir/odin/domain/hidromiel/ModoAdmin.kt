package com.aesir.odin.domain.hidromiel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Modo admin oculto, para probar la app sin jugar todo el progreso.
 *
 * Mientras está activo:
 * - Todos los mundos se ven y se pueden abrir.
 * - Todos los niveles (incluido el Ragnarök / reto de fin de mundo) están
 *   abiertos y su entrada es gratuita. Las ayudas del crucigrama no cobran.
 * - Los niveles se siguen registrando como superados normalmente, así que
 *   superar un Ragnarök también abre el siguiente mundo de verdad.
 *
 * Se activa y desactiva con una combinación secreta en la pantalla de Mundos
 * (ver [CombinacionSecreta]). [AppContainer] lo carga y lo guarda en las
 * preferencias de la app, así que se conserva al cerrarla.
 */
object ModoAdmin {
    private val _activo = MutableStateFlow(false)
    val activo: StateFlow<Boolean> = _activo.asStateFlow()

    val estaActivo: Boolean get() = _activo.value

    /** Se llama cada vez que cambia, para guardarlo (lo asigna AppContainer). */
    var alCambiar: ((Boolean) -> Unit)? = null

    /** Valor inicial leído de las preferencias (no vuelve a guardarlo). */
    fun cargar(valor: Boolean) {
        _activo.value = valor
    }

    fun establecer(valor: Boolean) {
        _activo.value = valor
        alCambiar?.invoke(valor)
    }

    fun alternar() = establecer(!estaActivo)
}

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
