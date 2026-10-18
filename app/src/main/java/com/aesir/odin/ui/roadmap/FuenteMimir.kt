package com.aesir.odin.ui.roadmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.components.odin.FuentesMockup
import com.aesir.odin.ui.components.odin.pulsable
import com.aesir.odin.ui.components.odin.rememberTiempo
import com.aesir.odin.ui.diagnostico.dibujarRostroMimir
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.math.pow
import kotlin.math.sin

/**
 * Entrada al repaso del mundo. Es un pozo pequeño: mientras el mundo no se
 * completa, Mímir duerme bajo el agua (solo asoman sus cuernos y el agua está
 * quieta y oscura). Al superar el Ragnarök el agua se enciende con el color
 * del mundo, Mímir emerge, abre los ojos y caen ondas: ya se puede beber de él.
 */
@Composable
fun FuenteMimir(estilo: EstiloMundo, despierto: Boolean, onTocar: () -> Unit, modifier: Modifier = Modifier) {
    val tiempo = rememberTiempo()
    /** Momento (en segundos de [tiempo]) en que Mímir empezó a emerger. */
    var despertoEn by remember { mutableFloatStateOf(Float.NEGATIVE_INFINITY) }
    LaunchedEffect(despierto) { if (despierto && despertoEn == Float.NEGATIVE_INFINITY) despertoEn = tiempo.value }
    val forma = CutCornerShape(10.dp)

    Row(
        modifier
            .drawBehind {
                if (despierto) {
                    // Resplandor del pozo despierto alrededor de la tarjeta
                    drawRect(estilo.acento.copy(alpha = 0.08f), Offset(-6.dp.toPx(), -6.dp.toPx()), Size(size.width + 12.dp.toPx(), size.height + 12.dp.toPx()))
                }
            }
            .clip(forma)
            .background(Color(0xE6120F0C))
            .border(if (despierto) 1.4.dp else 1.dp, if (despierto) estilo.acento.copy(alpha = 0.8f) else estilo.metal.copy(alpha = 0.35f), forma)
            .pulsable(despierto, onTocar)
            .semantics {
                contentDescription = if (despierto) "Pozo de Mímir: repaso del mundo" else "Pozo de Mímir, dormido. Supera el Ragnarök para despertarlo."
            }
            .padding(start = 8.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(92.dp, 76.dp)) {
            val t = tiempo.value
            val avance = if (despierto) ((t - despertoEn) / 1.6f).coerceIn(0f, 1f) else 0f
            dibujarFuente(estilo.acento, despierto, t, 1f - (1f - avance).pow(3))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (despierto) "POZO DE MÍMIR · REPASO" else "POZO DE MÍMIR",
                style = TextStyle(fontFamily = FuentesMockup.cinzel(), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 2.sp),
                color = if (despierto) estilo.acento else OdinPaleta.HuesoTenue
            )
            Text(
                if (despierto) "Bebe de nuevo del pozo" else "El pozo duerme",
                style = TextStyle(fontFamily = FuentesMockup.alegreya(), fontWeight = FontWeight.Bold, fontSize = 18.sp),
                color = if (despierto) OdinPaleta.Hueso else OdinPaleta.Hueso.copy(alpha = 0.7f)
            )
            Text(
                if (despierto) "10 preguntas al azar del mundo. No cambia tu progreso."
                else "Supera el Ragnarök de este mundo para despertar a Mímir.",
                style = TextStyle(fontFamily = FuentesMockup.alegreya(), fontSize = 13.5.sp, lineHeight = 17.sp),
                color = Color(0xFFB8AE9C)
            )
        }
        Icon(
            if (despierto) Icons.Filled.KeyboardArrowRight else Icons.Filled.Lock,
            contentDescription = null,
            tint = if (despierto) estilo.acento else OdinPaleta.Escarcha.copy(alpha = 0.7f),
            modifier = Modifier.size(if (despierto) 22.dp else 16.dp)
        )
    }
}

private fun elipse(c: Offset, rx: Float, ry: Float) = Path().apply { addOval(Rect(c.x - rx, c.y - ry, c.x + rx, c.y + ry)) }

/** Dibujo del pozo pequeño en dp (92 × 76). [subida] 0 = dormido, 1 = Mímir fuera del agua. */
private fun DrawScope.dibujarFuente(acento: Color, despierto: Boolean, t: Float, subida: Float) {
    val d = density
    withTransform({ scale(d, d, pivot = Offset.Zero) }) {
        val w = 92f
        val agua = Offset(w / 2f, 54f)
        val pulso = 0.8f + 0.2f * sin(t * 1.6f)
        // Resplandor y haz de luz cuando está despierto
        if (despierto) {
            val cLuz = Offset(w / 2f, 40f)
            drawOval(
                Brush.radialGradient(listOf(acento.copy(alpha = 0.3f * pulso), acento.copy(alpha = 0f)), center = cLuz, radius = 46f),
                Offset(cLuz.x - 46f, cLuz.y - 38f), Size(92f, 76f), blendMode = BlendMode.Plus
            )
            val haz = Path().apply {
                moveTo(w / 2f - 5f, 0f); lineTo(w / 2f + 5f, 0f); lineTo(w / 2f + 14f, agua.y); lineTo(w / 2f - 14f, agua.y); close()
            }
            drawPath(
                haz, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0f), Color.White.copy(alpha = 0.22f * pulso)), startY = 0f, endY = agua.y),
                blendMode = BlendMode.Plus
            )
        }
        // Muro del pozo
        val muro = Path().apply {
            moveTo(w / 2f - 38f, agua.y)
            lineTo(w / 2f - 38f, agua.y + 12f)
            quadraticTo(w / 2f, agua.y + 32f, w / 2f + 38f, agua.y + 12f)
            lineTo(w / 2f + 38f, agua.y)
            close()
        }
        drawPath(muro, Brush.horizontalGradient(listOf(Color(0xFF2A2A29), Color(0xFF4A4A48), Color(0xFF2A2A29)), startX = w / 2f - 38f, endX = w / 2f + 38f))
        for (f in listOf(-0.5f, 0f, 0.5f)) {
            drawLine(Color.Black.copy(alpha = 0.4f), Offset(w / 2f + f * 38f, agua.y + 2f), Offset(w / 2f + f * 38f, agua.y + 18f - kotlin.math.abs(f) * 8f), 1f)
        }
        // Brocal y agua
        drawPath(elipse(agua, 38f, 11f), Color(0xFF5A5A58))
        val forma = elipse(agua, 31f, 8f)
        if (despierto) {
            drawPath(forma, acento.copy(alpha = 0.85f))
            withTransform({ scale(1f, 8f / 31f, pivot = agua) }) {
                drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.3f * pulso), Color.Black.copy(alpha = 0.25f)), center = agua, radius = 31f), 31f, agua)
            }
        } else {
            drawPath(forma, Color(0xFF141A20))
            drawPath(forma, acento.copy(alpha = 0.08f + 0.05f * sin(t * 0.8f)))
        }
        // Mímir: dormido bajo el agua (asoman los cuernos) o emergiendo
        val flota = if (despierto) sin(t * 1.2f) * 1f else 0f
        clipPath(Path().apply { addRect(Rect(0f, 0f, w, agua.y + 1f)) }) {
            withTransform({
                translate(w / 2f, agua.y + 12f - 40f * subida + flota)
                scale(0.42f, 0.42f, pivot = Offset.Zero)
                translate(-250f, -330f)
            }) {
                val alfa = if (despierto) 1f else 0.55f
                drawContext.canvas.saveLayer(Rect(150f, 230f, 350f, 400f), androidx.compose.ui.graphics.Paint().apply { this.alpha = alfa })
                dibujarRostroMimir(acento, if (despierto) pulso else 0.2f, if (despierto) 0.6f + 0.4f * sin(t * 0.9f) else 0f)
                drawContext.canvas.restore()
            }
        }
        // Ondas
        clipPath(forma) {
            val velocidad = if (despierto) 0.35f else 0.12f
            for (i in 0 until 2) {
                val f = (t * velocidad + i / 2f) % 1f
                val r = 4f + f * 30f
                drawPath(elipse(agua, r, r * 0.26f), Color.White.copy(alpha = (if (despierto) 0.45f else 0.15f) * (1f - f)), style = Stroke(1f))
            }
        }
        // Velas a los lados (encendidas solo si está despierto)
        for (lado in listOf(-1f, 1f)) {
            val base = Offset(w / 2f + lado * 30f, agua.y + 2f)
            drawRect(Color(0xFFEDE6D6).copy(alpha = if (despierto) 1f else 0.5f), Offset(base.x - 1.6f, base.y - 7f), Size(3.2f, 7f))
            if (despierto) {
                val tiembla = 0.75f + 0.25f * sin(t * 9f + lado * 2f)
                val llama = Offset(base.x, base.y - 9.5f)
                drawCircle(
                    Brush.radialGradient(listOf(acento.copy(alpha = 0.5f * tiembla), acento.copy(alpha = 0f)), center = llama, radius = 7f),
                    7f, llama, blendMode = BlendMode.Plus
                )
                drawOval(Color(0xFFFFE7A8), Offset(llama.x - 1.3f, llama.y - 2.4f * tiembla), Size(2.6f, 4.8f * tiembla))
            }
        }
    }
}
