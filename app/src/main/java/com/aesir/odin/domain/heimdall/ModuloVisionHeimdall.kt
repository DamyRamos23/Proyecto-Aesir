package com.aesir.odin.domain.heimdall

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

interface AlmacenPreferencias {
    fun leer(): PreferenciaLectura?
    fun guardar(preferencia: PreferenciaLectura): Boolean
}

/**
 * MOD-03 Visión de Heimdall. Solo decide cómo se presenta el texto: no
 * conoce tiempos, puntajes, hidromiel ni respuestas (RN-16, E2).
 */
class ModuloVisionHeimdall(
    private val almacen: AlmacenPreferencias,
    private val selector: SelectorPresentacion = SelectorPresentacion(),
    private val ahora: () -> LocalDateTime = LocalDateTime::now
) {
    // No está en el diagrama: el tema de la app lo observa para cambiar todas
    // las pantallas en cuanto se activa en Ajustes (paso 3).
    private val _presentacion = MutableStateFlow(selector.seleccionar(ModoLectura.NORMAL))
    val presentacion: StateFlow<PresentacionLectura> = _presentacion.asStateFlow()

    /** Paso 4. E3: si la preferencia no se puede leer, inicia desactivado. */
    fun alAbrirAplicacion(): PresentacionLectura {
        val preferencia = runCatching { almacen.leer() }.getOrNull()
        val modo = if (preferencia?.estaActivo() == true) ModoLectura.VISION_HEIMDALL else ModoLectura.NORMAL
        return aplicar(modo)
    }

    fun obtenerPresentacion(): PresentacionLectura = _presentacion.value

    /** Pasos 1-3 y 5. RN-20: solo se invoca desde la pantalla de Ajustes. */
    fun cambiarModo(modo: ModoLectura): PresentacionLectura {
        // Si no se pudo guardar se aplica igual; solo se pierde al cerrar la app.
        runCatching { almacen.guardar(PreferenciaLectura(modo, ahora())) }
        return aplicar(modo)
    }

    private fun aplicar(modo: ModoLectura): PresentacionLectura =
        selector.seleccionar(modo).also { _presentacion.value = it }
}
