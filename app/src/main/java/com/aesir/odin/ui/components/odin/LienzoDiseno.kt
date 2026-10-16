package com.aesir.odin.ui.components.odin

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Lienzo de diseño: las pantallas copiadas de los mockups se escriben con
 * medidas fijas (en dp de diseño, 390 de ancho) y este lienzo las escala al
 * teléfono cambiando la densidad, así el texto sigue nítido.
 *
 * @param alto alto total del diseño (dp de diseño).
 * @param altoUtil alto que debe caber en pantalla; si la pantalla es más
 *   baja, todo se encoge para que quepa.
 */
@Composable
fun LienzoDiseno(
    alto: Float,
    modifier: Modifier = Modifier,
    altoUtil: Float = alto,
    ancho: Float = 390f,
    contenido: @Composable BoxScope.() -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val anchoPx = constraints.maxWidth.toFloat()
        val altoPx = constraints.maxHeight.toFloat()
        val escala = min(anchoPx / ancho, altoPx / altoUtil)
        CompositionLocalProvider(LocalDensity provides Density(escala, 1f)) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .wrapContentSize(Alignment.TopCenter, unbounded = true)
                    .requiredSize(ancho.dp, alto.dp),
                content = contenido
            )
        }
    }
}

/** Coloca un elemento centrado en (x, y) con tamaño w × h, en dp de diseño. */
fun Modifier.en(x: Float, y: Float, w: Float, h: Float): Modifier =
    this
        .offset(x = (x - w / 2f).dp, y = (y - h / 2f).dp)
        .requiredSize(w.dp, h.dp)

/** Segundos desde que aparece la vista (para animar dibujos en cada cuadro). */
@Composable
fun rememberTiempo(): State<Float> {
    val tiempo = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val inicio = withFrameNanos { it }
        while (true) {
            withFrameNanos { ahora -> tiempo.floatValue = (ahora - inicio) / 1_000_000_000f }
        }
    }
    return tiempo
}

/**
 * Tipografías del mockup: Cinzel (títulos) y Alegreya (texto). Si sus .ttf
 * están en `res/font` (cinzel_regular, cinzel_bold, cinzel_black,
 * alegreya_regular, alegreya_italic, alegreya_medium, alegreya_bold) se usan;
 * si no, la serif del sistema.
 */
object FuentesMockup {

    @Composable
    fun cinzel(): FontFamily = familia(
        listOf(
            Triple("cinzel_regular", FontWeight.Normal, FontStyle.Normal),
            Triple("cinzel_bold", FontWeight.Bold, FontStyle.Normal),
            Triple("cinzel_black", FontWeight.Black, FontStyle.Normal)
        )
    )

    @Composable
    fun alegreya(): FontFamily = familia(
        listOf(
            Triple("alegreya_regular", FontWeight.Normal, FontStyle.Normal),
            Triple("alegreya_italic", FontWeight.Normal, FontStyle.Italic),
            Triple("alegreya_medium", FontWeight.Medium, FontStyle.Normal),
            Triple("alegreya_bold", FontWeight.Bold, FontStyle.Normal)
        )
    )

    /** true si Cinzel está en la app (sus minúsculas ya son versalitas). */
    @SuppressLint("DiscouragedApi")
    @Composable
    fun hayCinzel(): Boolean {
        val contexto = LocalContext.current
        return remember { contexto.resources.getIdentifier("cinzel_bold", "font", contexto.packageName) != 0 }
    }

    @SuppressLint("DiscouragedApi")
    @Composable
    private fun familia(lista: List<Triple<String, FontWeight, FontStyle>>): FontFamily {
        val contexto = LocalContext.current
        return remember(lista) {
            val fuentes = lista.mapNotNull { (nombre, peso, estilo) ->
                val id = contexto.resources.getIdentifier(nombre, "font", contexto.packageName)
                if (id != 0) Font(id, peso, estilo) else null
            }
            if (fuentes.isEmpty()) FontFamily.Serif else FontFamily(fuentes)
        }
    }

    /**
     * Texto en versalitas: si no hay Cinzel, las minúsculas se escriben como
     * mayúsculas más chicas (así se ve "Ragnarök" en el mockup).
     */
    fun versalitas(texto: String, tamano: TextUnit, conCinzel: Boolean): AnnotatedString =
        if (conCinzel) {
            AnnotatedString(texto)
        } else {
            buildAnnotatedString {
                for (c in texto) {
                    if (c.isLowerCase()) {
                        withStyle(SpanStyle(fontSize = tamano * 0.78f)) { append(c.uppercaseChar()) }
                    } else {
                        append(c)
                    }
                }
            }
        }
}
