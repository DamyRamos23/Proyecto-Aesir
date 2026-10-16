package com.aesir.odin.ui.components.odin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Coloca el centro del elemento en (x, y), en dp de diseño (dentro de un
 * [LienzoDiseno]). Es el `.position(...)` de SwiftUI: el elemento conserva
 * su tamaño natural (o el que se le dé después con size/width).
 */
fun Modifier.centroEn(x: Float, y: Float): Modifier = layout { medible, restricciones ->
    val p = medible.measure(restricciones.copy(minWidth = 0, minHeight = 0))
    val ancho = if (restricciones.hasBoundedWidth) restricciones.maxWidth else p.width
    val alto = if (restricciones.hasBoundedHeight) restricciones.maxHeight else p.height
    layout(ancho, alto) {
        p.place(x.dp.roundToPx() - p.width / 2, y.dp.roundToPx() - p.height / 2)
    }
}

/** Botón que se hunde un poco al presionarlo (sin onda), como EstiloHundir de iOS. */
fun Modifier.pulsable(habilitado: Boolean = true, alPulsar: () -> Unit): Modifier = composed {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.97f else 1f, tween(100), label = "hundir")
    this
        .graphicsLayer {
            scaleX = escala
            scaleY = escala
            alpha = if (presionado) 0.92f else 1f
        }
        .clickable(
            interactionSource = interaccion,
            indication = null,
            enabled = habilitado,
            role = Role.Button,
            onClick = alPulsar
        )
}

/**
 * Texto que se encoge hasta [minEscala] de su tamaño si no cabe en el
 * espacio que se le da (el minimumScaleFactor de SwiftUI).
 */
@Composable
fun TextoAjustable(
    texto: String,
    estilo: TextStyle,
    modifier: Modifier = Modifier,
    minEscala: Float = 0.6f,
    maxLineas: Int = 1,
    color: Color = Color.Unspecified
) {
    val tamano = if (estilo.fontSize.isSp) estilo.fontSize.value else 14f
    BasicText(
        text = texto,
        modifier = modifier,
        style = if (color != Color.Unspecified) estilo.copy(color = color) else estilo,
        maxLines = maxLineas,
        overflow = TextOverflow.Clip,
        autoSize = TextAutoSize.StepBased(
            minFontSize = (tamano * minEscala).sp,
            maxFontSize = tamano.sp,
            stepSize = 0.5.sp
        )
    )
}
