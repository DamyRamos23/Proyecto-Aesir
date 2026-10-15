package com.aesir.odin.ui.diagnostico

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.OdinPaleta

/**
 * Ilustración de la presentación del CU-10: el pozo de Mímir sobre unos
 * escalones, con su agua del color del mundo, un haz de luz que baja sobre
 * él, la máscara dorada de Mímir y velas alrededor. Se dibuja con Canvas,
 * sin imágenes, como el resto de los paisajes de la app.
 */
@Composable
fun PozoDeMimir(estilo: EstiloMundo, modifier: Modifier = Modifier) {
    val pulso by rememberInfiniteTransition(label = "pozo").animateFloat(
        initialValue = .75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_400, easing = LinearEasing), RepeatMode.Reverse),
        label = "brillo"
    )
    Canvas(modifier.semantics { contentDescription = "Mímir, guardián del pozo" }) {
        val w = size.width
        val h = size.height
        val cx = w / 2

        ramas(w, h, estilo)

        // Haz de luz que baja hasta el agua
        val haz = Path().apply {
            moveTo(cx - w * .05f, 0f)
            lineTo(cx + w * .05f, 0f)
            lineTo(cx + w * .2f, h * .74f)
            lineTo(cx - w * .2f, h * .74f)
            close()
        }
        drawPath(haz, Brush.verticalGradient(listOf(Color.White.copy(alpha = .28f * pulso), estilo.acento.copy(alpha = .08f)), 0f, h * .74f))

        // Escalones
        escalon(cx, h * .93f, w * .44f, h * .06f, Color(0xFF15120F), estilo.metalOscuro)
        escalon(cx, h * .87f, w * .37f, h * .055f, Color(0xFF1E1A16), estilo.metalOscuro)

        // Cuerpo del pozo
        val rx = w * .28f
        val bocaY = h * .72f
        drawRect(
            Brush.horizontalGradient(listOf(Color(0xFF1C1915), Color(0xFF3A342C), Color(0xFF1C1915)), cx - rx, cx + rx),
            topLeft = Offset(cx - rx, bocaY),
            size = Size(rx * 2, h * .13f)
        )
        drawOval(Color(0xFF1C1915), Offset(cx - rx, bocaY + h * .13f - h * .045f), Size(rx * 2, h * .09f))
        // Piedras del pozo
        for (i in 1..5) {
            val x = cx - rx + rx * 2 * i / 6f
            drawLine(Color.Black.copy(alpha = .35f), Offset(x, bocaY + h * .02f), Offset(x, bocaY + h * .12f), 1.5f)
        }
        // Borde y agua luminosa
        drawOval(estilo.metalOscuro, Offset(cx - rx, bocaY - h * .05f), Size(rx * 2, h * .1f))
        drawOval(
            Brush.radialGradient(
                listOf(estilo.acento.copy(alpha = .95f * pulso), estilo.acento.copy(alpha = .35f), Color.Black.copy(alpha = .6f)),
                center = Offset(cx, bocaY),
                radius = rx
            ),
            Offset(cx - rx * .86f, bocaY - h * .038f),
            Size(rx * 1.72f, h * .076f)
        )

        mascara(cx, h * .5f, w * .095f, h)

        // Velas sobre el borde del pozo
        val velas = listOf(-.92f, -.6f, .6f, .92f)
        velas.forEach { f ->
            val x = cx + rx * f
            val y = bocaY - h * .015f
            drawRect(Color(0xFFE8DCC0), Offset(x - 2.5f, y - h * .04f), Size(5f, h * .04f))
            drawCircle(estilo.acento.copy(alpha = .25f * pulso), h * .03f, Offset(x, y - h * .05f))
            drawCircle(Color(0xFFFFE6A8), h * .008f, Offset(x, y - h * .05f))
        }
    }
}

/** Ramas secas a los lados, como raíces de Yggdrasil sobre el pozo. */
private fun DrawScope.ramas(w: Float, h: Float, estilo: EstiloMundo) {
    val color = estilo.metal.copy(alpha = .35f)
    val trazo = Stroke(2.2f, cap = StrokeCap.Round)
    listOf(1f, -1f).forEach { lado ->
        val x0 = if (lado > 0) 0f else w
        val p = Path().apply {
            moveTo(x0, h * .18f)
            cubicTo(x0 + lado * w * .12f, h * .2f, x0 + lado * w * .2f, h * .3f, x0 + lado * w * .26f, h * .52f)
            moveTo(x0 + lado * w * .1f, h * .22f)
            quadraticTo(x0 + lado * w * .16f, h * .1f, x0 + lado * w * .24f, h * .06f)
            moveTo(x0 + lado * w * .18f, h * .34f)
            quadraticTo(x0 + lado * w * .28f, h * .3f, x0 + lado * w * .33f, h * .2f)
            moveTo(x0, h * .4f)
            quadraticTo(x0 + lado * w * .1f, h * .42f, x0 + lado * w * .16f, h * .58f)
        }
        drawPath(p, color, style = trazo)
    }
}

private fun DrawScope.escalon(cx: Float, y: Float, rx: Float, alto: Float, color: Color, borde: Color) {
    drawRect(color, Offset(cx - rx, y - alto / 2), Size(rx * 2, alto))
    drawOval(color, Offset(cx - rx, y + alto / 2 - alto * .5f), Size(rx * 2, alto))
    drawOval(color, Offset(cx - rx, y - alto), Size(rx * 2, alto))
    drawOval(borde.copy(alpha = .5f), Offset(cx - rx, y - alto), Size(rx * 2, alto), style = Stroke(1.2f))
}

/** Máscara dorada de Mímir: frente, ojos cerrados, nariz y barba en punta. */
private fun DrawScope.mascara(cx: Float, cy: Float, r: Float, h: Float) {
    val oro = Brush.verticalGradient(listOf(Color(0xFFF2D58A), OdinPaleta.Oro, Color(0xFF7A5A26)), cy - r * 1.3f, cy + r * 2f)
    val cara = Path().apply {
        moveTo(cx, cy - r * 1.3f)
        cubicTo(cx + r * 1.1f, cy - r * 1.3f, cx + r * 1.15f, cy, cx + r * .9f, cy + r * .6f)
        lineTo(cx + r * .45f, cy + r * 1.9f)
        lineTo(cx, cy + r * 2.25f)
        lineTo(cx - r * .45f, cy + r * 1.9f)
        lineTo(cx - r * .9f, cy + r * .6f)
        cubicTo(cx - r * 1.15f, cy, cx - r * 1.1f, cy - r * 1.3f, cx, cy - r * 1.3f)
        close()
    }
    drawCircle(OdinPaleta.Oro.copy(alpha = .18f), r * 2.6f, Offset(cx, cy + r * .3f))
    drawPath(cara, oro)
    drawPath(cara, Color(0xFF4A3414), style = Stroke(1.5f))
    val oscuro = Color(0xFF3A2A12)
    // Cejas y ojos cerrados
    drawLine(oscuro, Offset(cx - r * .75f, cy - r * .35f), Offset(cx - r * .15f, cy - r * .2f), 3f, StrokeCap.Round)
    drawLine(oscuro, Offset(cx + r * .75f, cy - r * .35f), Offset(cx + r * .15f, cy - r * .2f), 3f, StrokeCap.Round)
    drawLine(oscuro, Offset(cx - r * .62f, cy + r * .05f), Offset(cx - r * .2f, cy + r * .05f), 2.5f, StrokeCap.Round)
    drawLine(oscuro, Offset(cx + r * .62f, cy + r * .05f), Offset(cx + r * .2f, cy + r * .05f), 2.5f, StrokeCap.Round)
    // Nariz y boca
    drawLine(oscuro, Offset(cx, cy - r * .1f), Offset(cx, cy + r * .6f), 2f, StrokeCap.Round)
    drawLine(oscuro, Offset(cx - r * .35f, cy + r * .95f), Offset(cx + r * .35f, cy + r * .95f), 2.5f, StrokeCap.Round)
    // Trenzas de la barba
    for (i in -1..1) {
        drawLine(oscuro.copy(alpha = .6f), Offset(cx + i * r * .25f, cy + r * 1.15f), Offset(cx + i * r * .12f, cy + r * 1.95f), 1.5f)
    }
}
