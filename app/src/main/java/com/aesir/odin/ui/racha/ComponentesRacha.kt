package com.aesir.odin.ui.racha

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.racha.AvisoRacha
import com.aesir.odin.domain.racha.EstadoRacha
import com.aesir.odin.domain.racha.EstadoRachaDia
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlin.math.cos
import kotlin.math.sin

/** Piezas visuales del MOD-05: el sol, el contador de la racha y los avisos. */

internal val ColorSol = Color(0xFFFFC94A)
internal val ColorSolApagado = Color(0xFF6B5D45)

internal fun colorDe(estado: EstadoRachaDia?): Color = when (estado) {
    EstadoRachaDia.ENCENDIDA -> ColorSol
    EstadoRachaDia.PROTEGIDA -> OdinPaleta.Escarcha
    EstadoRachaDia.EN_RIESGO -> OdinPaleta.Incompleto
    else -> ColorSolApagado
}

/** Sol con rayos. Apagado se ve gris y sin rayos. */
@Composable
internal fun IconoSol(estado: EstadoRachaDia?, modifier: Modifier = Modifier, tamano: Dp = 20.dp) {
    Canvas(modifier.size(tamano)) { dibujarSol(center, size.minDimension / 2f, estado) }
}

internal fun DrawScope.dibujarSol(c: Offset, r: Float, estado: EstadoRachaDia?) {
    val color = colorDe(estado)
    val encendido = estado == EstadoRachaDia.ENCENDIDA || estado == EstadoRachaDia.PROTEGIDA
    if (encendido) {
        drawCircle(Brush.radialGradient(listOf(color.copy(alpha = .45f), Color.Transparent), center = c, radius = r), r, c)
    }
    if (estado != EstadoRachaDia.APAGADA && estado != null) {
        for (i in 0 until 8) {
            val a = Math.toRadians(i * 45.0)
            val desde = c + Offset((cos(a) * r * .6f).toFloat(), (sin(a) * r * .6f).toFloat())
            val hasta = c + Offset((cos(a) * r * .95f).toFloat(), (sin(a) * r * .95f).toFloat())
            drawLine(color, desde, hasta, r * .12f, StrokeCap.Round)
        }
    }
    drawCircle(color, r * .42f, c)
}

/** Contador de racha para la barra superior: sol + días seguidos. */
@Composable
fun ContadorRacha(estado: EstadoRacha?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val forma = CutCornerShape(6.dp)
    val dia = estado?.estado
    Row(
        modifier
            .clip(forma)
            .background(Color(0xE60B0907))
            .border(1.dp, colorDe(dia).copy(alpha = .9f), forma)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Racha de ${estado?.actual ?: 0} días. Abrir la carrera de Sól" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconoSol(dia, tamano = 20.dp)
        Spacer(Modifier.width(5.dp))
        Text("${estado?.actual ?: 0}", color = colorDe(dia), fontFamily = FuenteTitulo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

/**
 * Muestra uno por uno los avisos del MOD-05 (racha encendida, protegida, en
 * riesgo o perdida). [alSuscribir] corre en cuanto ya se escuchan los avisos;
 * ahí se avisa la apertura de la app para que su aviso no se pierda.
 */
@Composable
fun AvisosRacha(
    avisos: SharedFlow<AvisoRacha>,
    modifier: Modifier = Modifier,
    alSuscribir: suspend () -> Unit = {}
) {
    var actual by remember { mutableStateOf<AvisoRacha?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(avisos) {
        avisos
            .onSubscription { runCatching { alSuscribir() } }
            .collect { aviso ->
                actual = aviso
                visible = true
                delay(3600)
                visible = false
                delay(350)
            }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(260)) { it } + fadeIn(tween(260)),
        exit = slideOutVertically(tween(260)) { it } + fadeOut(tween(260)),
        modifier = modifier
    ) {
        actual?.let { aviso ->
            val forma = CutCornerShape(10.dp)
            val color = colorDe(aviso.estado.estado)
            Row(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .clip(forma)
                    .background(Color(0xF2120F0C))
                    .border(1.dp, color, forma)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconoSol(aviso.estado.estado, tamano = 34.dp)
                Spacer(Modifier.width(12.dp))
                Text(aviso.mensaje, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 14.sp, lineHeight = 19.sp, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Silueta sencilla de Sköll, el lobo que persigue a Sól. */
internal fun DrawScope.dibujarLobo(base: Offset, alto: Float, color: Color) {
    val w = alto * 1.6f
    val p = Path().apply {
        moveTo(base.x, base.y)
        lineTo(base.x + w * .08f, base.y - alto * .45f) // pata trasera y lomo
        lineTo(base.x + w * .55f, base.y - alto * .55f)
        lineTo(base.x + w * .72f, base.y - alto * .8f)  // cuello
        lineTo(base.x + w * .78f, base.y - alto)        // oreja
        lineTo(base.x + w * .84f, base.y - alto * .82f)
        lineTo(base.x + w, base.y - alto * .7f)         // hocico
        lineTo(base.x + w * .86f, base.y - alto * .58f)
        lineTo(base.x + w * .78f, base.y - alto * .45f)
        lineTo(base.x + w * .8f, base.y)                // pata delantera
        lineTo(base.x + w * .7f, base.y)
        lineTo(base.x + w * .62f, base.y - alto * .3f)
        lineTo(base.x + w * .25f, base.y - alto * .3f)
        lineTo(base.x + w * .18f, base.y)
        close()
    }
    drawPath(p, color)
    // Cola
    drawLine(color, base + Offset(w * .08f, -alto * .42f), base + Offset(-w * .12f, -alto * .62f), alto * .1f, StrokeCap.Round)
}
