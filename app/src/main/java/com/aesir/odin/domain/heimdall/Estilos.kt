package com.aesir.odin.domain.heimdall

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

fun interface EstiloLectura {
    fun crearPresentacion(): PresentacionLectura
}

/** Presentación normal: no cambia nada de lo que define cada pantalla. */
class EstiloNormal : EstiloLectura {
    override fun crearPresentacion() = PresentacionLectura(
        modo = ModoLectura.NORMAL,
        fuente = null,
        escalaTexto = 1f,
        espacioLetras = 0f,
        espacioPalabras = 0f,
        interlineado = null,
        espacioParrafos = 0f,
        alineacion = null,
        enfasis = null,
        colorFondo = null,
        colorTexto = null
    )
}

/** Valores de la tabla «Ajustes de presentación» del MOD-03. */
class EstiloHeimdall : EstiloLectura {
    override fun crearPresentacion() = PresentacionLectura(
        modo = ModoLectura.VISION_HEIMDALL,
        fuente = FUENTE,
        escalaTexto = ESCALA_TEXTO,
        espacioLetras = ESPACIO_LETRAS,
        espacioPalabras = ESPACIO_PALABRAS,
        interlineado = INTERLINEADO,
        espacioParrafos = ESPACIO_PARRAFOS,
        alineacion = Alineacion.IZQUIERDA,
        enfasis = TipoEnfasis.NEGRITAS,
        // Decisión del equipo: sin fondo crema ni texto #2B2B2B (tapaban el arte
        // de las pantallas); cada pantalla conserva sus colores.
        colorFondo = null,
        colorTexto = null
    )

    companion object {
        const val FUENTE = "Lexend"
        const val ESCALA_TEXTO = 1.2f
        const val ESPACIO_LETRAS = 0.12f
        const val ESPACIO_PALABRAS = 0.16f
        const val INTERLINEADO = 1.5f
        const val ESPACIO_PARRAFOS = 2.0f
    }
}

/** RN-19: contraste mínimo 4.5:1 entre texto y fondo (WCAG 2.2, 1.4.3). */
class ValidadorContraste {

    fun calcularContraste(colorFondo: String, colorTexto: String): Double {
        val a = luminancia(colorFondo)
        val b = luminancia(colorTexto)
        return (max(a, b) + 0.05) / (min(a, b) + 0.05)
    }

    /** Sin colores propios la presentación usa los de la pantalla: no hay nada que validar. */
    fun cumple(presentacion: PresentacionLectura): Boolean {
        val fondo = presentacion.colorFondo ?: return true
        val texto = presentacion.colorTexto ?: return true
        return calcularContraste(fondo, texto) >= CONTRASTE_MINIMO
    }

    private fun luminancia(hex: String): Double {
        val rgb = hex.removePrefix("#").toLong(16)
        val canales = listOf(rgb shr 16, rgb shr 8, rgb).map { c ->
            val s = (c and 0xFF) / 255.0
            if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * canales[0] + 0.7152 * canales[1] + 0.0722 * canales[2]
    }

    companion object {
        const val CONTRASTE_MINIMO = 4.5
    }
}

class SelectorPresentacion(
    private val estilos: Map<ModoLectura, EstiloLectura> = mapOf(
        ModoLectura.NORMAL to EstiloNormal(),
        ModoLectura.VISION_HEIMDALL to EstiloHeimdall()
    ),
    private val validador: ValidadorContraste = ValidadorContraste()
) {
    /** Si el estilo no cumple el contraste (RN-19) se usa la presentación normal. */
    fun seleccionar(modo: ModoLectura): PresentacionLectura {
        val presentacion = estilos.getValue(modo).crearPresentacion()
        return if (validador.cumple(presentacion)) presentacion
        else estilos.getValue(ModoLectura.NORMAL).crearPresentacion()
    }
}
