package com.aesir.odin.ui.forja

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.EtapaTemplar
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * Escenas animadas de las tres etapas de la forja (diseño «La forja de Brokkr»):
 * el fuego al calentar, el yunque con la mosca de Loki al martillar, la tina
 * del temple con el metal que cambia de color según se enfría, y la revelación
 * del artefacto al terminar. Todo se dibuja con Canvas (sin imágenes).
 */

private val Fuego = Color(0xFFFF9A3C)
private val FuegoHondo = Color(0xFFC2551A)
private val FuegoMedio = Color(0xFFFF7A2A)
private val FuegoClaro = Color(0xFFFFD35A)
private val FuegoBlanco = Color(0xFFFFE9A8)
private val Chispa = Color(0xFFFFE2A8)
private val Vapor = Color(0xFFDDEAF2)
private val AguaAlta = Color(0xFF2F5570)
private val AguaBaja = Color(0xFF0E1A24)
private val Mosca = Color(0xFF9FD8C8)

// ---------------------------------------------------------------------------
// Fases del temple: el color del metal dice cuánto tiempo queda
// ---------------------------------------------------------------------------

internal enum class FaseTemple(val nombre: String, val metal: Color, val acento: Color, val fondo: Color) {
    BLANCO("Al blanco", Color(0xFFFFF1C9), Color(0xFFFFE9A8), Color(0xFF3A2410)),
    PAJIZO("Amarillo pajizo", Color(0xFFFFD35A), Color(0xFFFFD35A), Color(0xFF372010)),
    NARANJA("Naranja", Color(0xFFFF8A2A), Color(0xFFFF9A3C), Color(0xFF331A0C)),
    CEREZA("Rojo cereza", Color(0xFFD93A1E), Color(0xFFF06A4A), Color(0xFF2A120C)),
    SOMBRA("Rojo sombra", Color(0xFF7A2418), Color(0xFFE2614B), Color(0xFF1C0E0B)),
    FRIO("Frío", Color(0xFF4A4F57), Color(0xFF9DB3C4), Color(0xFF12161B));

    companion object {
        /** Fase según los segundos que quedan (escalado a 60 s: 49, 37, 25, 13, 1). */
        fun de(segundos: Int, total: Int = EtapaTemplar.SEGUNDOS_ENFRIADO): FaseTemple {
            if (segundos <= 0) return FRIO
            val s = segundos * 60f / total.coerceAtLeast(1)
            return when {
                s >= 49 -> BLANCO
                s >= 37 -> PAJIZO
                s >= 25 -> NARANJA
                s >= 13 -> CEREZA
                else -> SOMBRA
            }
        }
    }
}

/** Fondo que acompaña el color del metal (cálido al principio, frío al final). */
@Composable
internal fun colorFondoTemple(segundos: Int): Color {
    val c by animateColorAsState(FaseTemple.de(segundos).fondo, tween(900), label = "fondoTemple")
    return c
}

// ---------------------------------------------------------------------------
// Riel de las tres etapas: Calentar, Martillar, Templar
// ---------------------------------------------------------------------------

@Composable
internal fun RielEtapas(actual: Int, modifier: Modifier = Modifier) {
    val nombres = listOf("CALENTAR", "MARTILLAR", "TEMPLAR")
    Row(
        modifier.fillMaxWidth().semantics { contentDescription = "Etapa $actual de 3" },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        nombres.forEachIndexed { i, nombre ->
            val numero = i + 1
            val hecha = numero < actual
            val enCurso = numero == actual
            val frio = numero == 3
            Column(Modifier.width(84.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    val tam = if (enCurso) 42.dp else 36.dp
                    val acento = if (frio) Color(0xFF9DB3C4) else Fuego
                    Canvas(Modifier.size(tam)) {
                        val r = size.minDimension / 2f
                        when {
                            enCurso -> {
                                drawCircle(acento.copy(alpha = .35f), r * 1.25f)
                                drawCircle(
                                    Brush.radialGradient(
                                        if (frio) listOf(Color(0xFF9DB3C4), Color(0xFF2F5570)) else listOf(Color(0xFFFFB24A), FuegoHondo),
                                        center, r
                                    ), r
                                )
                                drawCircle(if (frio) Vapor else Chispa, r - 1.dp.toPx(), style = Stroke(2.dp.toPx()))
                            }
                            hecha -> {
                                drawCircle(Fuego, r)
                                drawCircle(Chispa, r - .75.dp.toPx(), style = Stroke(1.5.dp.toPx()))
                            }
                            else -> {
                                drawCircle(Color(0xFF14100C), r)
                                drawCircle(ColorMarcoForja, r - .75.dp.toPx(), style = Stroke(1.5.dp.toPx()))
                            }
                        }
                        val tinta = when {
                            enCurso -> if (frio) Color(0xFF0E1A24) else Color(0xFF2A1206)
                            hecha -> Color(0xFF17110A)
                            else -> ColorMarcoForja
                        }
                        if (hecha) iconoCheck(tinta) else when (numero) {
                            1 -> iconoLlama(tinta)
                            2 -> iconoMartillo(tinta)
                            else -> iconoGota(tinta)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    nombre,
                    color = when {
                        enCurso && frio -> Vapor
                        enCurso -> Color(0xFFFFC266)
                        hecha -> Fuego
                        else -> Color(0xFF8C7A5C)
                    },
                    fontFamily = FuenteTitulo,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp
                )
            }
            if (i < nombres.lastIndex) {
                Box(
                    Modifier
                        .padding(top = 20.dp)
                        .width(22.dp)
                        .height(2.dp)
                        .background(if (numero < actual) Fuego else ColorMarcoForja.copy(alpha = .6f))
                )
            }
        }
    }
}

private fun DrawScope.iconoCheck(c: Color) {
    val s = size.minDimension
    val p = Path().apply {
        moveTo(s * .28f, s * .52f); lineTo(s * .44f, s * .67f); lineTo(s * .72f, s * .36f)
    }
    drawPath(p, c, style = Stroke(s * .09f, cap = StrokeCap.Round))
}

private fun DrawScope.iconoLlama(c: Color) {
    val s = size.minDimension
    val p = Path().apply {
        moveTo(s * .5f, s * .2f)
        cubicTo(s * .55f, s * .38f, s * .72f, s * .44f, s * .7f, s * .6f)
        cubicTo(s * .68f, s * .76f, s * .32f, s * .78f, s * .3f, s * .6f)
        cubicTo(s * .3f, s * .48f, s * .4f, s * .46f, s * .42f, s * .36f)
        cubicTo(s * .48f, s * .42f, s * .5f, s * .46f, s * .52f, s * .5f)
        cubicTo(s * .54f, s * .4f, s * .53f, s * .3f, s * .5f, s * .2f)
        close()
    }
    drawPath(p, c)
}

private fun DrawScope.iconoMartillo(c: Color) {
    val s = size.minDimension
    drawRect(c, Offset(s * .26f, s * .28f), Size(s * .36f, s * .16f), style = Stroke(s * .07f))
    drawLine(c, Offset(s * .44f, s * .44f), Offset(s * .44f, s * .76f), s * .08f, StrokeCap.Round)
    drawLine(c, Offset(s * .62f, s * .36f), Offset(s * .74f, s * .36f), s * .07f, StrokeCap.Round)
}

private fun DrawScope.iconoGota(c: Color) {
    val s = size.minDimension
    val p = Path().apply {
        moveTo(s * .5f, s * .22f)
        cubicTo(s * .62f, s * .38f, s * .72f, s * .5f, s * .72f, s * .6f)
        cubicTo(s * .72f, s * .74f, s * .62f, s * .8f, s * .5f, s * .8f)
        cubicTo(s * .38f, s * .8f, s * .28f, s * .74f, s * .28f, s * .6f)
        cubicTo(s * .28f, s * .5f, s * .38f, s * .38f, s * .5f, s * .22f)
        close()
    }
    drawPath(p, c)
}

// ---------------------------------------------------------------------------
// Escena 1: el metal se calienta sobre las brasas
// ---------------------------------------------------------------------------

/** Dibuja en unidades de diseño de 390 × 196 escaladas al ancho disponible. */
private inline fun DrawScope.enDiseno(ancho: Float = 390f, bloque: DrawScope.() -> Unit) {
    val k = size.width / ancho
    scale(k, k, pivot = Offset.Zero) { bloque() }
}

@Composable
internal fun EscenaCalentar(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "calentar")
    val llamaA by t.animateFloat(1f, 1.14f, infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "llamaA")
    val llamaB by t.animateFloat(1.08f, .9f, infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "llamaB")
    val brillo by t.animateFloat(.7f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "brillo")
    val chispas by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "chispas")

    Canvas(
        modifier.fillMaxWidth().aspectRatio(390f / 196f).semantics { contentDescription = "El metal se calienta sobre las brasas de la fragua" }
    ) {
        enDiseno {
            drawOval(
                Brush.radialGradient(listOf(Fuego.copy(alpha = .55f * brillo), Color.Transparent), Offset(195f, 196f), 200f),
                Offset(5f, 80f), Size(380f, 180f)
            )
            // Hogar de piedra
            val piedra = Path().apply {
                moveTo(40f, 196f); lineTo(40f, 110f)
                quadraticTo(40f, 84f, 66f, 80f); lineTo(324f, 80f)
                quadraticTo(350f, 84f, 350f, 110f); lineTo(350f, 196f); close()
            }
            drawPath(piedra, Brush.verticalGradient(listOf(Color(0xFF4A4038), Color(0xFF1E1A16)), 80f, 196f))
            val boca = Path().apply {
                moveTo(66f, 196f); lineTo(66f, 126f)
                quadraticTo(66f, 112f, 80f, 110f); lineTo(310f, 110f)
                quadraticTo(324f, 112f, 324f, 126f); lineTo(324f, 196f); close()
            }
            drawPath(boca, Color(0xFF0B0705))
            // Llamas
            translate(195f, 190f) {
                llama(-110f, 18f, 62f, FuegoHondo, llamaA)
                llama(-70f, 20f, 74f, FuegoMedio, llamaB)
                llama(-24f, 23f, 84f, Fuego, llamaA)
                llama(24f, 20f, 70f, FuegoMedio, llamaB)
                llama(70f, 17f, 58f, FuegoHondo, llamaA)
                llama(-40f, 14f, 48f, FuegoClaro, llamaB)
                llama(10f, 14f, 52f, FuegoBlanco, llamaA)
            }
            // Barra al rojo
            drawRoundRect(
                Brush.horizontalGradient(
                    0f to Color(0xFF5A3A2A), .35f to Color(0xFFFF5A1F), .6f to FuegoClaro, .8f to FuegoMedio, 1f to Color(0xFF5A3A2A),
                    startX = 104f, endX = 286f
                ),
                Offset(104f, 128f), Size(182f, 16f), CornerRadius(5f), alpha = brillo
            )
            drawRoundRect(Color.White.copy(alpha = .25f), Offset(104f, 128f), Size(182f, 5f), CornerRadius(2.5f))
            val tenaza = Path().apply { moveTo(286f, 133f); lineTo(356f, 133f); lineTo(370f, 125f) }
            drawPath(tenaza, Color(0xFF3A3229), style = Stroke(7f, cap = StrokeCap.Round))
            chispas(chispas, listOf(150f to 120f, 210f to 124f, 245f to 118f, 180f to 126f), 120f)
        }
    }
}

private fun DrawScope.llama(x: Float, ancho: Float, alto: Float, color: Color, k: Float) {
    scale(1f, k, pivot = Offset(x + ancho / 2f, 0f)) {
        val p = Path().apply {
            moveTo(x, 0f)
            cubicTo(x + ancho * .1f, -alto * .4f, x + ancho * .9f, -alto * .55f, x + ancho * .5f, -alto)
            cubicTo(x + ancho * 1.3f, -alto * .7f, x + ancho * 1.6f, -alto * .35f, x + ancho * 1.4f, 0f)
            close()
        }
        drawPath(p, color)
    }
}

private fun DrawScope.chispas(fase: Float, origenes: List<Pair<Float, Float>>, subida: Float) {
    origenes.forEachIndexed { i, (x, y) ->
        val f = (fase + i / origenes.size.toFloat()) % 1f
        val a = when {
            f < .15f -> f / .15f
            else -> 1f - (f - .15f) / .85f
        }
        drawCircle(Chispa.copy(alpha = a.coerceIn(0f, 1f)), 2f, Offset(x + sin(f * 6f + i) * 6f, y - subida * f))
    }
}

// ---------------------------------------------------------------------------
// Escena 2: el yunque y la mosca de Loki
// ---------------------------------------------------------------------------

@Composable
internal fun EscenaMartillar(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "martillar")
    val vuelo by t.animateFloat(0f, 1f, infiniteRepeatable(tween(4200, easing = LinearEasing)), label = "vuelo")
    val golpe by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "golpe")
    val alas by t.animateFloat(1f, .25f, infiniteRepeatable(tween(40), RepeatMode.Reverse), label = "alas")
    val brillo by t.animateFloat(.7f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "brillo")

    Box(modifier.fillMaxWidth()) {
        Canvas(
            Modifier.fillMaxWidth().aspectRatio(390f / 196f)
                .semantics { contentDescription = "El martillo golpea el metal en el yunque mientras la mosca de Loki ronda" }
        ) {
            enDiseno {
                drawOval(
                    Brush.radialGradient(listOf(Fuego.copy(alpha = .45f * brillo), Color.Transparent), Offset(200f, 128f), 170f),
                    Offset(30f, 48f), Size(340f, 160f)
                )
                // Yunque
                val yunque = Path().apply {
                    moveTo(70f, 120f); lineTo(296f, 120f)
                    quadraticTo(326f, 120f, 336f, 106f); lineTo(320f, 136f); lineTo(250f, 136f)
                    lineTo(260f, 166f); lineTo(290f, 166f); lineTo(290f, 184f); lineTo(110f, 184f)
                    lineTo(110f, 166f); lineTo(140f, 166f); lineTo(150f, 136f); lineTo(86f, 136f)
                    quadraticTo(72f, 136f, 70f, 120f); close()
                }
                drawPath(yunque, Brush.verticalGradient(listOf(Color(0xFF6A645C), Color(0xFF3A3530), Color(0xFF16130F)), 106f, 184f))
                val filo = Path().apply { moveTo(70f, 120f); lineTo(296f, 120f); quadraticTo(326f, 120f, 336f, 106f) }
                drawPath(filo, Color(0xFF8C847A), style = Stroke(2f))
                // Pieza al rojo
                drawRoundRect(
                    Brush.horizontalGradient(listOf(FuegoHondo, Color(0xFFFFB24A), FuegoHondo), 150f, 246f),
                    Offset(150f, 106f), Size(96f, 14f), CornerRadius(4f), alpha = brillo
                )
                // Martillo: sube despacio y cae rápido
                val angulo = when {
                    golpe < .7f -> -38f
                    golpe < .82f -> -38f + 42f * ((golpe - .7f) / .12f)
                    else -> 4f - 42f * ((golpe - .82f) / .18f)
                }
                rotate(angulo, pivot = Offset(330f, 128f)) {
                    drawRoundRect(Color(0xFF6B4423), Offset(326f, 40f), Size(9f, 92f), CornerRadius(3f))
                    drawRoundRect(Color(0xFF7A7F88), Offset(300f, 26f), Size(62f, 28f), CornerRadius(5f))
                    drawRoundRect(Color(0xFFB8BDC4), Offset(300f, 26f), Size(62f, 7f), CornerRadius(3f))
                    drawRect(OdinPaleta.Oro, Offset(324f, 34f), Size(14f, 12f), style = Stroke(1.6f))
                }
                // Chispazo al golpear
                if (golpe in .8f..0.92f) {
                    val a = 1f - kotlin.math.abs(golpe - .84f) / .08f
                    listOf(
                        Offset(232f, 90f), Offset(252f, 82f), Offset(270f, 92f), Offset(224f, 112f)
                    ).forEach { fin -> drawLine(Chispa.copy(alpha = a.coerceIn(0f, 1f)), Offset(248f, 106f), fin, 2.4f, StrokeCap.Round) }
                }
                // La mosca: un ocho irregular por encima del yunque
                val ang = vuelo * 2f * PI.toFloat()
                val mx = 140f + 80f * sin(ang) + 18f * sin(ang * 3f)
                val my = 56f + 26f * sin(ang * 2f) + 6f * cos(ang * 5f)
                val dx = 80f * cos(ang) + 54f * cos(ang * 3f)
                // Estela punteada detrás de la mosca
                for (i in 1..7) {
                    val a2 = ang - i * .09f
                    val ex = 140f + 80f * sin(a2) + 18f * sin(a2 * 3f)
                    val ey = 56f + 26f * sin(a2 * 2f) + 6f * cos(a2 * 5f)
                    drawCircle(Mosca.copy(alpha = .3f - i * .035f), 1.4f, Offset(ex, ey))
                }
                translate(mx, my) {
                    rotate(if (dx >= 0) 8f else -8f, pivot = Offset.Zero) {
                        scale(if (dx >= 0) 1f else -1f, 1f, pivot = Offset.Zero) { mosca(alas) }
                    }
                }
            }
        }
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = 2.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xB306070D))
                .border(1.dp, Mosca.copy(alpha = .35f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Mosca))
            Spacer(Modifier.width(6.dp))
            Text("La mosca de Loki ronda el yunque", color = Mosca, fontFamily = FuenteTexto, fontSize = 12.sp)
        }
    }
}

private fun DrawScope.mosca(alas: Float) {
    // Alas translúcidas que baten
    scale(1f, alas, pivot = Offset(-2f, 0f)) {
        drawOval(Color(0x8CC8E6F0), Offset(-8f, -18f), Size(12f, 18f))
    }
    scale(1f, alas, pivot = Offset(4f, 0f)) {
        drawOval(Color(0x73C8E6F0), Offset(-1f, -17f), Size(10f, 16f))
    }
    drawOval(Color(0xFF1B2A22), Offset(-9f, -6f), Size(18f, 12f))
    drawCircle(Color(0xFF2E4A3A), 4.2f, Offset(8f, -1f))
    drawCircle(Color(0xFFE2614B), 1.6f, Offset(10f, -2f))
    listOf(-6f to -9f, 0f to 0f, 5f to 8f).forEach { (x, x2) ->
        drawLine(Color(0xFF1B2A22), Offset(x, 5f), Offset(x2, 10f), 1.2f, StrokeCap.Round)
    }
}

// ---------------------------------------------------------------------------
// Escena 3: el temple. El color del metal y el vapor siguen al reloj
// ---------------------------------------------------------------------------

@Composable
internal fun EscenaTemplar(segundos: Int, modifier: Modifier = Modifier) {
    val total = EtapaTemplar.SEGUNDOS_ENFRIADO
    val fase = FaseTemple.de(segundos, total)
    val calor = (segundos.coerceAtLeast(0) / total.toFloat()).coerceIn(0f, 1f)
    val metal by animateColorAsState(fase.metal, tween(900), label = "metal")
    val acento by animateColorAsState(fase.acento, tween(900), label = "acento")
    val t = rememberInfiniteTransition(label = "templar")
    val vapor by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "vapor")
    val ola by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ola")

    Box(modifier.fillMaxWidth()) {
        Canvas(
            Modifier.fillMaxWidth().aspectRatio(390f / 178f)
                .semantics { contentDescription = "El metal se enfría en la tina: ${fase.nombre}" }
        ) {
            enDiseno {
                drawOval(
                    Brush.radialGradient(listOf(metal.copy(alpha = .15f + .55f * calor), Color.Transparent), Offset(160f, 92f), 140f),
                    Offset(20f, 36f), Size(280f, 112f)
                )
                // Vapor que sube mientras el metal está caliente
                if (segundos > 0) {
                    listOf(120f to 7f, 170f to 9f, 215f to 6f).forEachIndexed { i, (x, grosor) ->
                        val f = (vapor + i * .33f) % 1f
                        val a = (if (f < .25f) f / .25f else 1f - (f - .25f) / .75f) * (.3f + .7f * calor) * .5f
                        translate(0f, -70f * f) {
                            scale(1f + f * .8f, 1f, pivot = Offset(x, 92f)) {
                                val p = Path().apply {
                                    moveTo(x, 92f); cubicTo(x - 8f, 80f, x + 8f, 70f, x, 56f)
                                }
                                drawPath(p, Vapor.copy(alpha = a.coerceIn(0f, 1f)), style = Stroke(grosor, cap = StrokeCap.Round))
                            }
                        }
                    }
                }
                // Tina con agua
                val tina = Path().apply { moveTo(46f, 104f); lineTo(306f, 104f); lineTo(292f, 170f); lineTo(60f, 170f); close() }
                drawPath(tina, Color(0xFF2A241D))
                drawPath(tina, Color(0xFF5A4E40), style = Stroke(2f))
                val agua = Path().apply { moveTo(54f, 112f); lineTo(298f, 112f); lineTo(288f, 164f); lineTo(64f, 164f); close() }
                drawPath(agua, Brush.verticalGradient(listOf(AguaAlta, AguaBaja), 112f, 164f))
                val onda = Path().apply {
                    moveTo(54f + ola * 6f, 116f)
                    var x = 54f + ola * 6f
                    while (x < 290f) {
                        quadraticTo(x + 10f, 110f, x + 20f, 116f)
                        quadraticTo(x + 30f, 122f, x + 40f, 116f)
                        x += 40f
                    }
                }
                drawPath(onda, Color(0x809DB3C4), style = Stroke(2f))
                // El metal
                drawRoundRect(metal, Offset(88f, 84f), Size(150f, 16f), CornerRadius(5f))
                drawRoundRect(Color.White.copy(alpha = .08f + .3f * calor), Offset(88f, 84f), Size(150f, 5f), CornerRadius(2.5f))
                val tenaza = Path().apply { moveTo(238f, 92f); lineTo(300f, 92f); lineTo(318f, 62f) }
                drawPath(tenaza, Color(0xFF3A3229), style = Stroke(7f, cap = StrokeCap.Round))
            }
        }
        RelojTemple(
            segundos, calor, acento,
            Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 4.dp)
        )
    }
}

@Composable
private fun RelojTemple(segundos: Int, calor: Float, acento: Color, modifier: Modifier) {
    Box(
        modifier.size(84.dp).semantics { contentDescription = "Quedan $segundos segundos antes de que se enfríe el metal" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val g = 6.dp.toPx()
            val r = size.minDimension / 2f - g / 2f
            drawCircle(Color(0xFF100D0A), r)
            drawCircle(OdinPaleta.Hierro, r, style = Stroke(g))
            drawArc(
                acento, -90f, 360f * calor, false,
                topLeft = Offset(center.x - r, center.y - r), size = Size(r * 2, r * 2),
                style = Stroke(g, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${segundos.coerceAtLeast(0)}", color = acento, fontFamily = FuenteTitulo, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("SEGUNDOS", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 9.sp, letterSpacing = 1.sp)
        }
    }
}

/** Franja con las seis fases del temple y una marca en la fase actual. */
@Composable
internal fun BarraFasesTemple(segundos: Int, modifier: Modifier = Modifier) {
    val total = EtapaTemplar.SEGUNDOS_ENFRIADO
    val fase = FaseTemple.de(segundos, total)
    val acento by animateColorAsState(fase.acento, tween(900), label = "acentoBarra")
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text("COLOR DEL METAL", color = OdinPaleta.Oro, fontFamily = FuenteTitulo, fontSize = 11.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.weight(1f))
            Text(fase.nombre, color = acento, fontFamily = FuenteTitulo, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Canvas(Modifier.fillMaxWidth().height(22.dp)) {
            val alto = 12.dp.toPx()
            val franjas = listOf(FaseTemple.FRIO, FaseTemple.SOMBRA, FaseTemple.CEREZA, FaseTemple.NARANJA, FaseTemple.PAJIZO, FaseTemple.BLANCO)
            val pesos = listOf(1f, 12f, 12f, 12f, 12f, 12f)
            val suma = pesos.sum()
            var x = 0f
            franjas.forEachIndexed { i, f ->
                val w = size.width * pesos[i] / suma
                drawRect(f.metal, Offset(x, 0f), Size(w, alto))
                x += w
            }
            drawRoundRect(OdinPaleta.Hierro, Offset.Zero, Size(size.width, alto), CornerRadius(alto / 2), style = Stroke(1.dp.toPx()))
            val s60 = segundos.coerceAtLeast(0) * 60f / total
            val mx = size.width * ((if (s60 <= 0f) .5f else s60 + 1f) / 61f)
            val tri = Path().apply {
                moveTo(mx, alto + 1.dp.toPx())
                lineTo(mx - 6.dp.toPx(), alto + 9.dp.toPx())
                lineTo(mx + 6.dp.toPx(), alto + 9.dp.toPx())
                close()
            }
            drawPath(tri, OdinPaleta.Hueso)
        }
    }
}

// ---------------------------------------------------------------------------
// Revelación del artefacto forjado
// ---------------------------------------------------------------------------

/** Trazos de runas del futhark antiguo en una caja de 10 × 16. */
private val Runas: List<List<List<Pair<Float, Float>>>> = listOf(
    listOf(listOf(3f to 0f, 3f to 16f), listOf(3f to 4f, 9f to 1f), listOf(3f to 8f, 9f to 5f)),
    listOf(listOf(2f to 16f, 2f to 0f, 8f to 5f, 8f to 16f)),
    listOf(listOf(3f to 0f, 3f to 16f), listOf(3f to 4f, 8f to 8f, 3f to 12f)),
    listOf(listOf(3f to 0f, 3f to 16f), listOf(3f to 0f, 9f to 4f), listOf(3f to 5f, 9f to 9f)),
    listOf(listOf(3f to 16f, 3f to 0f, 8f to 4f, 3f to 8f, 8f to 16f)),
    listOf(listOf(8f to 2f, 3f to 8f, 8f to 14f)),
    listOf(listOf(1f to 1f, 9f to 15f), listOf(9f to 1f, 1f to 15f)),
    listOf(listOf(3f to 16f, 3f to 0f, 8f to 4f, 3f to 8f)),
    listOf(listOf(2f to 0f, 2f to 16f), listOf(8f to 0f, 8f to 16f), listOf(2f to 5f, 8f to 11f)),
    listOf(listOf(5f to 0f, 5f to 16f), listOf(2f to 5f, 8f to 11f)),
    listOf(listOf(5f to 0f, 5f to 16f)),
    listOf(listOf(4f to 2f, 1f to 6f, 4f to 10f), listOf(6f to 6f, 9f to 10f, 6f to 14f)),
    listOf(listOf(5f to 0f, 5f to 16f), listOf(5f to 0f, 8f to 3f), listOf(5f to 16f, 2f to 13f)),
    listOf(listOf(5f to 0f, 5f to 16f), listOf(5f to 6f, 1f to 1f), listOf(5f to 6f, 9f to 1f)),
    listOf(listOf(7f to 0f, 2f to 6f, 8f to 10f, 3f to 16f)),
    listOf(listOf(5f to 0f, 5f to 16f), listOf(5f to 0f, 1f to 5f), listOf(5f to 0f, 9f to 5f))
)

private fun DrawScope.runa(trazos: List<List<Pair<Float, Float>>>, origen: Offset, k: Float, color: Color, grosor: Float) {
    trazos.forEach { linea ->
        val p = Path()
        linea.forEachIndexed { i, (x, y) ->
            if (i == 0) p.moveTo(origen.x + x * k, origen.y + y * k) else p.lineTo(origen.x + x * k, origen.y + y * k)
        }
        drawPath(p, color, style = Stroke(grosor, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}

/**
 * Héroe de la pantalla de resultado: destello inicial, rayos de luz, dos
 * anillos de runas que giran, brasas que suben y el artefacto que aparece y
 * queda flotando.
 */
@Composable
internal fun RevelacionArtefacto(artefactoId: String?, modifier: Modifier = Modifier) {
    val entrada = remember { Animatable(0f) }
    val destello = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        destello.animateTo(1f, tween(1100))
    }
    LaunchedEffect(Unit) {
        entrada.animateTo(1f, tween(1300, easing = androidx.compose.animation.core.CubicBezierEasing(.2f, .8f, .2f, 1f)))
    }
    val t = rememberInfiniteTransition(label = "revelacion")
    val giro by t.animateFloat(0f, 360f, infiniteRepeatable(tween(60000, easing = LinearEasing)), label = "giro")
    val pulso by t.animateFloat(.55f, .95f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulso")
    val flota by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(2250, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "flota")
    val brasas by t.animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "brasas")
    val runaActiva by t.animateFloat(0f, 16f, infiniteRepeatable(tween(4800, easing = LinearEasing)), label = "runa")

    Box(modifier.fillMaxWidth().aspectRatio(390f / 312f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            enDiseno {
                val c = Offset(195f, 160f)
                // Rayos de luz
                rotate(giro / 2f, pivot = c) {
                    for (i in 0 until 12) {
                        rotate(i * 30f, pivot = c) {
                            val rayo = Path().apply { moveTo(195f, 160f); lineTo(183f, -40f); lineTo(207f, -40f); close() }
                            drawPath(
                                rayo,
                                Brush.verticalGradient(
                                    0f to Color.Transparent, .55f to Chispa.copy(alpha = .35f * pulso), 1f to Color.Transparent,
                                    startY = -40f, endY = 160f
                                )
                            )
                        }
                    }
                }
                drawCircle(
                    Brush.radialGradient(
                        0f to Color(0xBFFFD98A), .45f to Fuego.copy(alpha = .28f), 1f to Color.Transparent,
                        center = c, radius = 150f
                    ), 150f, c
                )
                // Anillo de runas
                rotate(giro, pivot = c) {
                    drawCircle(OdinPaleta.Oro.copy(alpha = .3f), 142f, c, style = Stroke(1.6f))
                    drawCircle(OdinPaleta.Oro.copy(alpha = .3f), 118f, c, style = Stroke(1.6f))
                    Runas.forEachIndexed { i, trazos ->
                        rotate(i * 22.5f, pivot = c) {
                            val cerca = kotlin.math.abs(((runaActiva - i + 16f) % 16f)) < 1.2f
                            runa(trazos, Offset(190f, 22f), 1f, if (cerca) Chispa else OdinPaleta.Oro.copy(alpha = .8f), 1.6f)
                        }
                    }
                }
                rotate(-giro * .66f, pivot = c) {
                    for (i in 0 until 8) {
                        val a = i * PI.toFloat() / 4f
                        drawCircle(Color(0xFFFFC266), 2.4f, Offset(c.x + 102f * cos(a), c.y + 102f * sin(a)))
                    }
                }
                // Destello de entrada
                if (destello.value < 1f) {
                    val r = 90f * (.2f + 2.2f * destello.value)
                    drawCircle(
                        Brush.radialGradient(
                            0f to Color.White.copy(alpha = 1f - destello.value), .4f to Color(0xFFFFF1C9).copy(alpha = .9f * (1f - destello.value)),
                            1f to Color.Transparent, center = c, radius = r
                        ), r, c
                    )
                }
                // Sombra en el suelo
                drawOval(Color.Black.copy(alpha = .45f * entrada.value), Offset(125f, 284f), Size(140f, 16f))
                // Brasas que suben
                listOf(120f, 160f, 230f, 272f, 98f, 300f, 200f).forEachIndexed { i, x ->
                    val f = (brasas + i / 7f) % 1f
                    val a = if (f < .1f) f / .1f else 1f - (f - .1f) / .9f
                    drawCircle(Chispa.copy(alpha = a), 1.4f + (i % 3) * .4f, Offset(x + 18f * f, 296f - 230f * f))
                }
            }
        }
        val e = entrada.value
        IconoArtefacto(
            artefactoId,
            tamano = 200.dp,
            modifier = Modifier.graphicsLayer {
                alpha = (e * 1.6f).coerceAtMost(1f)
                val s = .55f + .45f * e
                scaleX = s
                scaleY = s
                translationY = (1f - e) * 60.dp.toPx() + flota * 9.dp.toPx() * e
                rotationZ = flota * 2f * e
            }
        )
    }
}

/** Nombre del artefacto en dorado con un brillo que lo recorre. */
@Composable
internal fun TituloDorado(texto: String, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "dorado")
    val x by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "x")
    val ancho = 900f
    Text(
        texto.uppercase(),
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = TextStyle(
            brush = Brush.linearGradient(
                0f to Color(0xFFB8822E), .25f to Color(0xFFFFE2A8), .35f to Color(0xFFFFF6DE),
                .5f to Color(0xFFFFC266), .75f to Color(0xFFB8822E), 1f to Color(0xFFFFE2A8),
                start = Offset(x * ancho, 0f), end = Offset(x * ancho + ancho, 0f)
            ),
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.Black,
            fontSize = 40.sp,
            letterSpacing = 4.sp,
            lineHeight = 44.sp
        )
    )
}

/** Aparición escalonada (sube y se desvanece) para los bloques del resultado. */
@Composable
internal fun Modifier.apareceTras(retrasoMs: Int): Modifier {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(retrasoMs.toLong())
        a.animateTo(1f, tween(600))
    }
    return this.graphicsLayer {
        alpha = a.value
        translationY = (1f - a.value) * 16.dp.toPx()
    }
}

/** Fondo radial cálido detrás de la revelación. */
internal fun Modifier.fondoRevelacion(): Modifier = drawBehind {
    drawRect(
        Brush.radialGradient(
            0f to Color(0xFF4A2A0E), .5f to Color(0xFF1E120A), 1f to Color.Transparent,
            center = Offset(size.width / 2f, size.height * .26f), radius = size.maxDimension * .6f
        )
    )
}

/** Tarjeta pequeña de dato del resultado («CALOR SOBRANTE», «HIDROMIEL»…). */
@Composable
internal fun DatoResultado(valor: String, etiqueta: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color(0xD90B0907))
            .border(1.dp, OdinPaleta.Hierro)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, color = color, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(etiqueta, color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 11.sp, letterSpacing = .5.sp, textAlign = TextAlign.Center)
    }
}
