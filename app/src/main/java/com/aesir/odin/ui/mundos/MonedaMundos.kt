package com.aesir.odin.ui.mundos

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.SelloRunico
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.OdinPaleta
import com.aesir.odin.ui.theme.Text
import kotlin.math.floor

/**
 * La pantalla de Mundos es una moneda: deslizar hacia arriba la gira media
 * vuelta y muestra la cara siguiente. [caras] se recorren en orden y al final
 * vuelve a la primera (hoy: los mundos y el duelo de enigmas; después, más
 * juegos). [giros] cuenta las medias vueltas; la cara visible es giros % caras.
 */
@Composable
fun MonedaMundos(
    giros: Int,
    onGirar: () -> Unit,
    modifier: Modifier = Modifier,
    caras: List<@Composable () -> Unit>
) {
    val angulo by animateFloatAsState(giros * 180f, tween(700, easing = FastOutSlowInEasing), label = "moneda")
    val umbral = with(LocalDensity.current) { 60.dp.toPx() }
    // Media vuelta visible ahora (cambia de cara al pasar de canto, a los 90°).
    val mediaVuelta = floor((angulo + 90f) / 180f).toInt()
    Box(
        modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                var arrastre = 0f
                detectVerticalDragGestures(
                    onDragStart = { arrastre = 0f },
                    onDragEnd = { if (arrastre < -umbral) onGirar() },
                    onVerticalDrag = { cambio, dy -> arrastre += dy; cambio.consume() }
                )
            }
            .semantics { customActions = listOf(CustomAccessibilityAction("Girar la moneda") { onGirar(); true }) }
            .graphicsLayer {
                rotationX = -angulo
                cameraDistance = 14f * density
            }
    ) {
        // La cara de atrás se voltea otra vez para que no se vea de cabeza.
        Box(Modifier.fillMaxSize().graphicsLayer { if (mediaVuelta % 2 != 0) rotationX = 180f }) {
            caras[mediaVuelta.mod(caras.size)]()
        }
    }
}

/** Reverso de la moneda: la entrada al duelo de enigmas (CU-13, paso 1). */
@Composable
fun ReversoDuelo(onRetar: () -> Unit) {
    val estilo = EstilosMundo.Madera
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Disco de la moneda
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            drawCircle(Brush.radialGradient(listOf(Color(0xFF3A2C1C), Color(0xFF14110D)), center, r), r)
            drawCircle(estilo.metal, r - 3.dp.toPx(), style = Stroke(5.dp.toPx()))
            drawCircle(estilo.metal.copy(alpha = .4f), r - 16.dp.toPx(), style = Stroke(1.5.dp.toPx()))
            for (k in 0 until 36) {
                val a = Math.toRadians(k * 10.0)
                val p = Offset(center.x + (r - 10.dp.toPx()) * kotlin.math.cos(a).toFloat(), center.y + (r - 10.dp.toPx()) * kotlin.math.sin(a).toFloat())
                drawCircle(estilo.metal.copy(alpha = .6f), 1.5.dp.toPx(), p)
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SelloRunico(runa = "ᛃ", estilo = estilo, tamano = 64.dp)
            Spacer(Modifier.height(10.dp))
            Text("DUELO DE ENIGMAS", style = estiloTitulo(12.sp, color = estilo.metal), maxLines = 1)
            Text("El reto del gigante", style = estiloTitulo(24.sp, brillo = estilo.acento), maxLines = 1)
            Text(
                "Adivina el saber que esconde entre doce tarjetas.",
                color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 14.sp, textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
            )
            BotonOdin("Retar al gigante", onRetar, estilo.acento, metal = estilo.metal)
        }
    }
}
