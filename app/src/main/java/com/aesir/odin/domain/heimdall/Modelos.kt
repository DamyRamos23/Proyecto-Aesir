package com.aesir.odin.domain.heimdall

import java.time.LocalDateTime

/** MOD-03: modo de lectura elegido por el alumno. Al instalar es NORMAL. */
enum class ModoLectura { NORMAL, VISION_HEIMDALL }

enum class Alineacion { IZQUIERDA, CENTRADA, JUSTIFICADA }

enum class TipoEnfasis { NEGRITAS, CURSIVAS, MAYUSCULAS }

/** Preferencia guardada del alumno (RN-18). */
data class PreferenciaLectura(
    val modo: ModoLectura = ModoLectura.NORMAL,
    val fechaCambio: LocalDateTime
) {
    fun estaActivo(): Boolean = modo == ModoLectura.VISION_HEIMDALL
}

/**
 * Cómo se debe mostrar el texto. Los tamaños y espacios son múltiplos del
 * tamaño de la fuente; null en fuente o colores = "el definido por la pantalla".
 */
data class PresentacionLectura(
    val modo: ModoLectura,
    val fuente: String?,
    val escalaTexto: Float,
    val espacioLetras: Float,
    val espacioPalabras: Float,
    val interlineado: Float?,
    val espacioParrafos: Float,
    val alineacion: Alineacion?,
    val enfasis: TipoEnfasis?,
    val colorFondo: String?,
    val colorTexto: String?,
    /** E1: el texto nunca se recorta; pasa a la siguiente línea o se desplaza. */
    val permitirAjusteLinea: Boolean = true
) {
    val activa: Boolean get() = modo == ModoLectura.VISION_HEIMDALL
}
