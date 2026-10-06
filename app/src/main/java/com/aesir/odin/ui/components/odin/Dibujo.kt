package com.aesir.odin.ui.components.odin

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.aesir.odin.ui.theme.Emblema
import com.aesir.odin.ui.theme.Escena
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.MaterialEscudo
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Utilidades básicas
// ---------------------------------------------------------------------------

/** Mancha difusa (simula desenfoque sin depender de APIs de Android 12+). */
internal fun DrawScope.mancha(centro: Offset, radio: Float, color: Color, alpha: Float = 1f) {
    if (radio <= 0f) return
    drawCircle(
        brush = Brush.radialGradient(
            listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
            center = centro,
            radius = radio
        ),
        radius = radio,
        center = centro
    )
}

/** Mancha ovalada: una mancha circular escalada en horizontal. */
internal fun DrawScope.manchaOval(centro: Offset, radioX: Float, radioY: Float, color: Color, alpha: Float) {
    if (radioY <= 0f) return
    withTransform({ scale(radioX / radioY, 1f, pivot = centro) }) {
        mancha(centro, radioY, color, alpha)
    }
}

/** Trazo con resplandor: varias pasadas del mismo path, de ancho a fino. */
internal fun DrawScope.trazoBrillante(path: Path, color: Color, ancho: Float, nucleo: Color = Color.White, intensidad: Float = 1f) {
    val redondo = Stroke(width = ancho * 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawPath(path, color.copy(alpha = 0.16f * intensidad), style = redondo)
    drawPath(path, color.copy(alpha = 0.32f * intensidad), style = Stroke(ancho * 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, color.copy(alpha = intensidad), style = Stroke(ancho, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, nucleo.copy(alpha = 0.8f * intensidad), style = Stroke(ancho * 0.35f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/** Línea quebrada aleatoria (grietas, fisuras de lava, vetas). */
internal fun grieta(rnd: Random, inicio: Offset, pasos: Int, alcance: Float): Path = Path().apply {
    var p = inicio
    moveTo(p.x, p.y)
    repeat(pasos) {
        p += Offset((rnd.nextFloat() - 0.5f) * alcance, (rnd.nextFloat() - 0.5f) * alcance)
        lineTo(p.x, p.y)
    }
}

/** Cresta montañosa por desplazamiento de punto medio: perfiles más naturales que un zigzag. */
private fun puntosCresta(rnd: Random, control: List<Offset>, amplitud: Float, profundidad: Int = 5): List<Offset> {
    var pts = control
    var amp = amplitud
    repeat(profundidad) {
        val nuevos = ArrayList<Offset>(pts.size * 2)
        for (i in 0 until pts.size - 1) {
            val a = pts[i]
            val b = pts[i + 1]
            nuevos.add(a)
            nuevos.add(Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f + (rnd.nextFloat() - 0.5f) * amp))
        }
        nuevos.add(pts.last())
        pts = nuevos
        amp *= 0.55f
    }
    return pts
}

/**
 * Dibuja una cordillera y devuelve su silueta (para reflejos).
 * [control] son fracciones (x, y) del tamaño del lienzo.
 */
internal fun DrawScope.cordillera(
    rnd: Random,
    control: List<Pair<Float, Float>>,
    amplitud: Float,
    arriba: Color,
    abajo: Color,
    nieve: Color? = null,
    lineaNieve: Float = 0f,
    alphaNieve: Float = 0.8f
): Path {
    val w = size.width
    val h = size.height
    val pts = puntosCresta(rnd, control.map { Offset(it.first * w, it.second * h) }, amplitud * h)
    val path = Path().apply {
        moveTo(0f, h)
        pts.forEach { lineTo(it.x, it.y) }
        lineTo(w, h)
        close()
    }
    val cima = pts.minOf { it.y }
    drawPath(path, Brush.verticalGradient(listOf(arriba, abajo), startY = cima, endY = h))
    if (nieve != null) {
        val ln = lineaNieve * h
        clipPath(path) {
            drawRect(nieve.copy(alpha = alphaNieve), size = Size(w, ln))
            // Borde irregular de la nieve
            var x = 0f
            while (x < w) {
                val ancho = 6f + rnd.nextFloat() * 14f
                val fondo = ln + 4f + rnd.nextFloat() * 18f
                val diente = Path().apply {
                    moveTo(x, ln); lineTo(x + ancho / 2f, fondo); lineTo(x + ancho, ln); close()
                }
                drawPath(diente, nieve.copy(alpha = alphaNieve))
                x += ancho
            }
        }
    }
    return path
}

/** Refleja siluetas en el agua por debajo de [nivel]. */
internal fun DrawScope.reflejo(siluetas: List<Path>, color: Color, nivel: Float, alpha: Float) {
    clipRect(top = nivel) {
        withTransform({ scale(1f, -1f, pivot = Offset(0f, nivel)) }) {
            siluetas.forEach { drawPath(it, color.copy(alpha = alpha)) }
        }
    }
}

// ---------------------------------------------------------------------------
// Paisajes de cada mundo
// ---------------------------------------------------------------------------

internal fun DrawScope.dibujarPaisaje(estilo: EstiloMundo, semilla: Int) {
    val rnd = Random(semilla)
    when (estilo.escena) {
        Escena.AMANECER -> amanecer(rnd)
        Escena.TORMENTA -> tormenta(rnd)
        Escena.AURORA -> aurora(rnd)
        Escena.VOLCAN -> volcan(rnd)
    }
}

private fun DrawScope.amanecer(rnd: Random) {
    val w = size.width
    val h = size.height
    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFF1C2A4D), 0.35f to Color(0xFF6B5A7A),
            0.58f to Color(0xFFF0A058), 0.68f to Color(0xFFFFD690), endY = h
        )
    )
    mancha(Offset(w * 0.62f, h * 0.55f), w * 0.5f, Color(0xFFFFC870), 0.75f)
    mancha(Offset(w * 0.62f, h * 0.55f), w * 0.09f, Color(0xFFFFF2C8), 1f)
    manchaOval(Offset(w * 0.3f, h * 0.2f), w * 0.35f, h * 0.05f, Color(0xFFF6B98A), 0.4f)
    manchaOval(Offset(w * 0.78f, h * 0.3f), w * 0.3f, h * 0.04f, Color(0xFFFFCF9A), 0.45f)

    val lejana = cordillera(
        rnd, listOf(0f to .5f, .22f to .3f, .45f to .42f, .7f to .22f, 1f to .46f), .12f,
        Color(0xFF9A8AA8), Color(0xFF5A5878), Color(0xFFFFE0C8), .36f
    )
    drawRect(
        Brush.verticalGradient(
            listOf(Color.Transparent, Color(0x80FFD8B0), Color.Transparent),
            startY = h * .46f, endY = h * .6f
        ),
        topLeft = Offset(0f, h * .46f), size = Size(w, h * .14f)
    )
    val media = cordillera(
        rnd, listOf(0f to .58f, .15f to .45f, .4f to .62f, 1f to .6f), .08f,
        Color(0xFF3D3F58), Color(0xFF252A3C), Color(0xFFF3C9A8), .5f, .6f
    )
    // Bosque de pinos en la orilla
    var x = 0f
    while (x < w * .48f) {
        val alto = h * .1f * (.6f + rnd.nextFloat() * .6f)
        val ancho = alto * .32f
        val pino = Path().apply {
            moveTo(x, h * .68f - alto); lineTo(x + ancho / 2, h * .68f); lineTo(x - ancho / 2, h * .68f); close()
        }
        drawPath(pino, Color(0xFF121A16))
        x += 4f + rnd.nextFloat() * 6f
    }
    val lago = h * .675f
    drawRect(
        Brush.verticalGradient(0f to Color(0xFFE8A060), .25f to Color(0xFF5A5A72), 1f to Color(0xFF16202E), startY = lago, endY = h),
        topLeft = Offset(0f, lago), size = Size(w, h - lago)
    )
    reflejo(listOf(lejana, media), Color(0xFF3D3F58), lago, .35f)
    manchaOval(Offset(w * .62f, h * .8f), w * .06f, h * .16f, Color(0xFFFFC878), .35f)
    repeat(7) { i ->
        val y = h * (.72f + i * .035f)
        drawLine(Color(0x59FFD9A0), Offset(w * (.45f + rnd.nextFloat() * .35f), y), Offset(w * (.5f + rnd.nextFloat() * .4f), y), 1.dp.toPx())
    }
    repeat(6) { drawCircle(Color(0xFFFFC060), 1.2.dp.toPx(), Offset(w * (.1f + rnd.nextFloat() * .3f), h * (.655f + rnd.nextFloat() * .015f))) }
}

private fun DrawScope.tormenta(rnd: Random) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(0f to Color(0xFF1B2428), .5f to Color(0xFF4F6A70), .62f to Color(0xFF9FB6B4), endY = h))
    repeat(9) { i ->
        manchaOval(
            Offset(rnd.nextFloat() * w, h * (.05f + rnd.nextFloat() * .3f)),
            w * (.15f + rnd.nextFloat() * .25f), h * (.05f + rnd.nextFloat() * .06f),
            if (i % 2 == 0) Color(0xFF141C20) else Color(0xFF2B383C), .8f
        )
    }
    repeat(40) {
        val px = rnd.nextFloat() * w * 1.2f
        val py = rnd.nextFloat() * h * .6f
        drawLine(Color(0x2ECFE0E4), Offset(px, py), Offset(px - 6.dp.toPx(), py + 18.dp.toPx()), 0.7.dp.toPx())
    }
    cordillera(rnd, listOf(0f to .48f, .3f to .4f, .55f to .5f, 1f to .44f), .06f, Color(0xFF5D7274), Color(0xFF3A4C4F))
    drawRect(
        Brush.verticalGradient(listOf(Color.Transparent, Color(0x8CC6D6D6), Color.Transparent), startY = h * .44f, endY = h * .6f),
        topLeft = Offset(0f, h * .44f), size = Size(w, h * .16f)
    )
    val mar = h * .56f
    drawRect(Brush.verticalGradient(listOf(Color(0xFF3F5F62), Color(0xFF0F2226)), startY = mar, endY = h), topLeft = Offset(0f, mar), size = Size(w, h - mar))
    repeat(10) {
        val fy = h * (.6f + rnd.nextFloat() * .25f)
        val fx = rnd.nextFloat() * w
        val ola = Path().apply {
            moveTo(fx, fy); quadraticTo(fx + 6.dp.toPx(), fy - 2.dp.toPx(), fx + 12.dp.toPx(), fy)
            quadraticTo(fx + 18.dp.toPx(), fy + 2.dp.toPx(), fx + 24.dp.toPx(), fy)
        }
        drawPath(ola, Color(0x59E8F2F2), style = Stroke(0.8.dp.toPx()))
    }
    cordillera(rnd, listOf(0f to .7f, .35f to .66f, .7f to .74f, 1f to .7f), .05f, Color(0xFF2F3B33), Color(0xFF151B17))
    // Menhires
    listOf(Triple(.22f, .28f, .07f), Triple(.36f, .36f, .06f), Triple(.52f, .24f, .08f), Triple(.68f, .33f, .055f), Triple(.82f, .4f, .05f))
        .forEach { (fx, fh, fw) ->
            val cx = w * fx
            val alto = h * fh
            val ancho = w * fw
            val base = h * .72f + rnd.nextFloat() * 4f
            val piedra = Path().apply {
                moveTo(cx - ancho / 2, base); lineTo(cx - ancho / 2 + 2f, base - alto)
                quadraticTo(cx, base - alto - ancho * .6f, cx + ancho / 2 - 1f, base - alto + 4f)
                lineTo(cx + ancho / 2, base); close()
            }
            drawPath(piedra, Brush.horizontalGradient(listOf(Color(0xFF3A4442), Color(0xFF22292A), Color(0xFF121617)), startX = cx - ancho / 2, endX = cx + ancho / 2))
        }
    drawRect(
        Brush.verticalGradient(listOf(Color.Transparent, Color(0x59B9CCCC), Color.Transparent), startY = h * .66f, endY = h * .8f),
        topLeft = Offset(0f, h * .66f), size = Size(w, h * .14f)
    )
}

private fun DrawScope.aurora(rnd: Random) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(0f to Color(0xFF03060F), .6f to Color(0xFF0D1D3A), .75f to Color(0xFF173052), endY = h))
    repeat(90) {
        drawCircle(Color.White.copy(alpha = .3f + rnd.nextFloat() * .7f), (rnd.nextFloat() * .8f + .25f).dp.toPx(), Offset(rnd.nextFloat() * w, rnd.nextFloat() * h * .55f))
    }
    repeat(3) { k ->
        val banda = Path().apply {
            moveTo(-10f, h * (.2f + k * .05f))
            var x = 0f
            while (x <= w + 20f) {
                quadraticTo(x + w / 12f, h * (.04f + k * .05f + rnd.nextFloat() * .1f), x + w / 6f, h * (.18f + k * .05f + rnd.nextFloat() * .06f))
                x += w / 6f
            }
            lineTo(w + 20f, h * .6f); lineTo(-10f, h * .6f); close()
        }
        drawPath(
            banda,
            Brush.verticalGradient(
                0f to Color(0x00C070FF), .2f to Color(0x8C9A6CFF), .45f to Color(0xE63DFFA8), .75f to Color(0x003DFFA8),
                startY = 0f, endY = h * .6f
            ),
            alpha = .9f - k * .2f
        )
    }
    val lejana = cordillera(
        rnd, listOf(0f to .55f, .2f to .36f, .5f to .5f, .8f to .34f, 1f to .5f), .1f,
        Color(0xFF2A3D5C), Color(0xFF101B30), Color(0xFFBFE8E0), .44f, .55f
    )
    val castillo = Color(0xFF0A0F1A)
    val acantilado = Path().apply {
        moveTo(w * .45f, h * .72f); lineTo(w * .5f, h * .5f); lineTo(w * .62f, h * .46f); lineTo(w, h * .44f); lineTo(w, h * .72f); close()
    }
    drawPath(acantilado, castillo)
    listOf(Triple(.56f, .36f, .05f), Triple(.62f, .3f, .04f), Triple(.68f, .38f, .09f), Triple(.78f, .33f, .04f), Triple(.86f, .4f, .07f))
        .forEach { (fx, fy, fw) ->
            drawRect(castillo, Offset(w * fx, h * fy), Size(w * fw, h * (.47f - fy)))
            val techo = Path().apply {
                moveTo(w * fx - 1f, h * fy); lineTo(w * (fx + fw / 2), h * (fy - .06f)); lineTo(w * (fx + fw) + 1f, h * fy); close()
            }
            drawPath(techo, castillo)
            repeat(2) { i -> drawRect(Color(0xFFFFC86A), Offset(w * (fx + fw * .3f), h * (fy + .03f + i * .035f)), Size(1.6.dp.toPx(), 2.4.dp.toPx())) }
        }
    drawRect(Color(0xB3D8F0FF), Offset(w * .53f, h * .52f), Size(2.5.dp.toPx(), h * .2f))
    val lago = h * .72f
    drawRect(Brush.verticalGradient(listOf(Color(0xFF173A48), Color(0xFF040810)), startY = lago, endY = h), topLeft = Offset(0f, lago), size = Size(w, h - lago))
    reflejo(listOf(lejana), Color(0xFF2A3D5C), lago, .3f)
    drawRect(
        Brush.verticalGradient(listOf(Color.Transparent, Color(0x40A8E8E0), Color.Transparent), startY = h * .68f, endY = h * .78f),
        topLeft = Offset(0f, h * .68f), size = Size(w, h * .1f)
    )
}

private fun DrawScope.volcan(rnd: Random) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(0f to Color(0xFF120605), .45f to Color(0xFF4A120C), .62f to Color(0xFFC2421C), .7f to Color(0xFFFF8A3A), endY = h))
    repeat(8) {
        manchaOval(Offset(w * (.3f + rnd.nextFloat() * .6f), h * (.08f + rnd.nextFloat() * .25f)), w * (.12f + rnd.nextFloat() * .2f), h * (.05f + rnd.nextFloat() * .05f), Color(0xFF1A0B08), .85f)
    }
    val cono = Path().apply {
        moveTo(w * .25f, h * .66f); lineTo(w * .52f, h * .3f); lineTo(w * .6f, h * .31f); lineTo(w * .92f, h * .66f); close()
    }
    drawPath(cono, Brush.verticalGradient(listOf(Color(0xFF2A1612), Color(0xFF070404)), startY = h * .3f, endY = h * .66f))
    mancha(Offset(w * .56f, h * .3f), w * .09f, Color(0xFFFFB040), .9f)
    manchaOval(Offset(w * .56f, h * .18f), w * .08f, h * .12f, Color(0xFF2A120C), .75f)
    val lava = Path().apply {
        moveTo(w * .555f, h * .31f)
        quadraticTo(w * .52f, h * .43f, w * .58f, h * .51f)
        quadraticTo(w * .62f, h * .58f, w * .54f, h * .66f)
    }
    trazoBrillante(lava, Color(0xFFFF7A1A), 1.6.dp.toPx(), Color(0xFFFFD070))
    cordillera(rnd, listOf(0f to .62f, .15f to .48f, .3f to .64f, 1f to .66f), .07f, Color(0xFF1E0E0B), Color(0xFF0A0505))
    drawRect(Color(0xFF0B0605), Offset(0f, h * .64f), Size(w, h * .36f))
    mancha(Offset(w * .5f, h), w * .6f, Color(0xFFFF7A20), .7f)
    repeat(6) {
        val inicio = Offset(rnd.nextFloat() * w, h * (.7f + rnd.nextFloat() * .25f))
        val p = Path().apply {
            moveTo(inicio.x, inicio.y)
            var px = inicio.x
            var py = inicio.y
            repeat(4) { px += 8.dp.toPx() + rnd.nextFloat() * 10.dp.toPx(); py += (rnd.nextFloat() - .5f) * 8.dp.toPx(); lineTo(px, py) }
        }
        trazoBrillante(p, Color(0xFFFF6A10), 1.2.dp.toPx(), Color(0xFFFFD080))
    }
    val arco = Path().apply {
        moveTo(w * .05f, h); lineTo(w * .08f, h * .62f)
        quadraticTo(w * .1f, h * .42f, w * .26f, h * .44f)
        quadraticTo(w * .38f, h * .47f, w * .4f, h * .66f)
        lineTo(w * .42f, h); lineTo(w * .33f, h); lineTo(w * .32f, h * .66f)
        quadraticTo(w * .3f, h * .53f, w * .24f, h * .53f)
        quadraticTo(w * .16f, h * .54f, w * .15f, h * .66f)
        lineTo(w * .15f, h); close()
    }
    drawPath(arco, Color(0xFF090404))
    repeat(30) {
        drawCircle(
            (if (rnd.nextBoolean()) Color(0xFFFFB050) else Color(0xFFFF6020)).copy(alpha = .4f + rnd.nextFloat() * .6f),
            (.4f + rnd.nextFloat() * .8f).dp.toPx(), Offset(rnd.nextFloat() * w, rnd.nextFloat() * h * .8f)
        )
    }
}

// ---------------------------------------------------------------------------
// Materiales (cara del escudo, paneles, piedra rúnica)
// ---------------------------------------------------------------------------

internal fun DrawScope.dibujarMaterial(estilo: EstiloMundo, semilla: Int, centroVetas: Offset = center) {
    val w = size.width
    val h = size.height
    val rnd = Random(semilla)
    drawRect(
        Brush.radialGradient(
            listOf(estilo.caraClara, estilo.caraOscura),
            center = Offset(w * .42f, h * .38f),
            radius = max(w, h) * .8f
        )
    )
    when (estilo.material) {
        MaterialEscudo.MADERA -> {
            repeat(70) {
                val gx = rnd.nextFloat() * w
                val veta = Path().apply {
                    moveTo(gx, 0f)
                    var y = 0f
                    var xx = gx
                    while (y < h) { y += 18.dp.toPx(); xx += (rnd.nextFloat() - .5f) * 3.dp.toPx(); lineTo(xx, y) }
                }
                drawPath(veta, Color(0xFF0E0804).copy(alpha = .05f + rnd.nextFloat() * .1f), style = Stroke((0.6f + rnd.nextFloat()).dp.toPx()))
            }
            val tabla = 44.dp.toPx()
            var x = tabla
            while (x < w) {
                drawLine(Color(0xFF0E0804), Offset(x, 0f), Offset(x, h), 2.5.dp.toPx())
                drawLine(Color(0x40A87A50), Offset(x + 1.6.dp.toPx(), 0f), Offset(x + 1.6.dp.toPx(), h), 1.dp.toPx())
                x += tabla
            }
            repeat(3) { manchaOval(Offset(rnd.nextFloat() * w, rnd.nextFloat() * h), 6.dp.toPx(), 12.dp.toPx(), Color(0xFF0E0804), .5f) }
        }
        MaterialEscudo.PIEDRA -> {
            repeat(45) {
                mancha(
                    Offset(rnd.nextFloat() * w, rnd.nextFloat() * h),
                    (10 + rnd.nextInt(36)).dp.toPx(),
                    if (rnd.nextBoolean()) Color.Black else Color.White,
                    .05f + rnd.nextFloat() * .07f
                )
            }
            repeat(5) {
                val g = grieta(rnd, Offset(rnd.nextFloat() * w, rnd.nextFloat() * h), 5, 60.dp.toPx())
                drawPath(g, Color(0xB3050808), style = Stroke(1.4.dp.toPx()))
                withTransform({ translate(1f, 1f) }) { drawPath(g, Color(0x3399AABB), style = Stroke(0.5.dp.toPx())) }
            }
        }
        MaterialEscudo.HIERRO -> {
            var y = 0f
            while (y < h) {
                drawLine(Color.White.copy(alpha = rnd.nextFloat() * .05f), Offset(0f, y), Offset(w, y), 1f)
                y += 2f + rnd.nextFloat() * 3f
            }
            repeat(8) { i ->
                val a = (i * 45f + 22.5f) * (Math.PI / 180f).toFloat()
                drawLine(Color(0xFF03060D), centroVetas, centroVetas + Offset(cos(a), sin(a)) * max(w, h), 2.5.dp.toPx())
            }
            manchaOval(Offset(w * .5f, h * .15f), w * .7f, h * .12f, estilo.acento, .1f)
        }
        MaterialEscudo.OBSIDIANA -> {
            repeat(40) {
                mancha(Offset(rnd.nextFloat() * w, rnd.nextFloat() * h), (8 + rnd.nextInt(30)).dp.toPx(), Color(0xFF3A1A14), .25f)
            }
            repeat(7) {
                val g = grieta(rnd, Offset(rnd.nextFloat() * w, rnd.nextFloat() * h), 5, 55.dp.toPx())
                trazoBrillante(g, Color(0xFFFF5A10), 1.2.dp.toPx(), Color(0xFFFFB060), .7f)
            }
        }
    }
    drawRect(
        Brush.radialGradient(
            0.6f to Color.Transparent, 1f to Color.Black.copy(alpha = .55f),
            center = center, radius = max(w, h) * .75f
        )
    )
}

// ---------------------------------------------------------------------------
// Emblemas
// ---------------------------------------------------------------------------

/** Dibuja el emblema centrado en [c]; [s] es el radio aproximado. */
internal fun DrawScope.dibujarEmblema(emblema: Emblema, c: Offset, s: Float, color: Color) {
    fun st(f: Float) = Stroke(width = s * f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when (emblema) {
        Emblema.VEGVISIR -> {
            drawCircle(color, s * .14f, c, style = st(.05f))
            for (i in 0 until 8) {
                rotate(i * 45f, pivot = c) {
                    drawLine(color, c + Offset(0f, -s * .14f), c + Offset(0f, -s * .9f), s * .06f, StrokeCap.Round)
                    when (i % 3) {
                        0 -> drawPath(Path().apply {
                            moveTo(c.x - s * .16f, c.y - s * .62f); lineTo(c.x, c.y - s * .78f); lineTo(c.x + s * .16f, c.y - s * .62f)
                        }, color, style = st(.05f))
                        1 -> {
                            drawLine(color, c + Offset(-s * .16f, -s * .6f), c + Offset(s * .16f, -s * .6f), s * .05f, StrokeCap.Round)
                            drawCircle(color, s * .07f, c + Offset(0f, -s * .9f), style = st(.04f))
                        }
                        else -> {
                            drawLine(color, c + Offset(-s * .14f, -s * .5f), c + Offset(s * .14f, -s * .5f), s * .05f, StrokeCap.Round)
                            drawLine(color, c + Offset(-s * .14f, -s * .72f), c + Offset(s * .14f, -s * .72f), s * .05f, StrokeCap.Round)
                        }
                    }
                }
            }
        }
        Emblema.OJO -> {
            val ojo = Path().apply {
                moveTo(c.x - s * .85f, c.y)
                quadraticTo(c.x, c.y - s * .75f, c.x + s * .85f, c.y)
                quadraticTo(c.x, c.y + s * .75f, c.x - s * .85f, c.y)
                close()
            }
            drawPath(ojo, color, style = st(.07f))
            drawCircle(color, s * .3f, c, style = st(.07f))
            drawCircle(color, s * .12f, c)
            listOf(-50f, -25f, 0f, 25f, 50f).forEach { a ->
                rotate(a, pivot = c) { drawLine(color, c + Offset(0f, -s * .52f), c + Offset(0f, -s * .8f), s * .06f, StrokeCap.Round) }
            }
        }
        Emblema.YGGDRASIL -> {
            drawCircle(color, s * .88f, c, style = st(.06f))
            drawLine(color, c + Offset(0f, -s * .25f), c + Offset(0f, s * .35f), s * .11f, StrokeCap.Round)
            val ramas = Path().apply {
                moveTo(c.x, c.y - s * .15f); cubicTo(c.x - s * .1f, c.y - s * .4f, c.x - s * .4f, c.y - s * .4f, c.x - s * .6f, c.y - s * .6f)
                moveTo(c.x, c.y - s * .15f); cubicTo(c.x + s * .1f, c.y - s * .4f, c.x + s * .4f, c.y - s * .4f, c.x + s * .6f, c.y - s * .6f)
                moveTo(c.x, c.y - s * .22f); lineTo(c.x, c.y - s * .82f)
                moveTo(c.x, c.y - s * .05f); cubicTo(c.x - s * .25f, c.y - s * .15f, c.x - s * .55f, c.y - s * .1f, c.x - s * .78f, c.y - s * .3f)
                moveTo(c.x, c.y - s * .05f); cubicTo(c.x + s * .25f, c.y - s * .15f, c.x + s * .55f, c.y - s * .1f, c.x + s * .78f, c.y - s * .3f)
                moveTo(c.x, c.y + s * .3f); cubicTo(c.x - s * .15f, c.y + s * .45f, c.x - s * .45f, c.y + s * .45f, c.x - s * .6f, c.y + s * .62f)
                moveTo(c.x, c.y + s * .3f); cubicTo(c.x + s * .15f, c.y + s * .45f, c.x + s * .45f, c.y + s * .45f, c.x + s * .6f, c.y + s * .62f)
                moveTo(c.x, c.y + s * .35f); lineTo(c.x, c.y + s * .85f)
            }
            drawPath(ramas, color, style = st(.07f))
        }
        Emblema.MJOLNIR -> {
            val cabeza = Path().apply {
                moveTo(c.x - s * .62f, c.y - s * .72f); lineTo(c.x + s * .62f, c.y - s * .72f)
                lineTo(c.x + s * .52f, c.y - s * .18f); lineTo(c.x - s * .52f, c.y - s * .18f); close()
            }
            drawPath(cabeza, color, style = st(.07f))
            val nudo = Path().apply {
                moveTo(c.x - s * .36f, c.y - s * .6f); lineTo(c.x - s * .12f, c.y - s * .3f)
                moveTo(c.x + s * .36f, c.y - s * .6f); lineTo(c.x + s * .12f, c.y - s * .3f)
                moveTo(c.x - s * .12f, c.y - s * .6f); lineTo(c.x + s * .12f, c.y - s * .3f)
                moveTo(c.x + s * .12f, c.y - s * .6f); lineTo(c.x - s * .12f, c.y - s * .3f)
            }
            drawPath(nudo, color, style = st(.04f))
            drawLine(color, c + Offset(-s * .09f, -s * .18f), c + Offset(-s * .09f, s * .6f), s * .06f)
            drawLine(color, c + Offset(s * .09f, -s * .18f), c + Offset(s * .09f, s * .6f), s * .06f)
            drawLine(color, c + Offset(-s * .11f, s * .12f), c + Offset(s * .11f, s * .12f), s * .05f)
            drawLine(color, c + Offset(-s * .11f, s * .32f), c + Offset(s * .11f, s * .32f), s * .05f)
            drawCircle(color, s * .13f, c + Offset(0f, s * .75f), style = st(.06f))
        }
    }
}

/** Valknut: tres triángulos entrelazados. */
internal fun DrawScope.dibujarValknut(c: Offset, s: Float, color: Color, grosor: Float) {
    listOf(-90.0, 30.0, 150.0).forEach { grados ->
        val rad = Math.toRadians(grados)
        val centro = Offset(c.x + cos(rad).toFloat() * s * .32f, c.y + sin(rad).toFloat() * s * .32f)
        val tri = Path()
        listOf(-90.0, 30.0, 150.0).forEachIndexed { i, g ->
            val r = Math.toRadians(g)
            val px = centro.x + cos(r).toFloat() * s * .62f
            val py = centro.y + sin(r).toFloat() * s * .62f
            if (i == 0) tri.moveTo(px, py) else tri.lineTo(px, py)
        }
        tri.close()
        drawPath(tri, color, style = Stroke(grosor, join = StrokeJoin.Miter))
    }
}

/** Rectángulo de esquinas cortadas (paneles, placas). */
internal fun rectCortado(r: Rect, corte: Float): Path = Path().apply {
    moveTo(r.left + corte, r.top); lineTo(r.right - corte, r.top); lineTo(r.right, r.top + corte)
    lineTo(r.right, r.bottom - corte); lineTo(r.right - corte, r.bottom); lineTo(r.left + corte, r.bottom)
    lineTo(r.left, r.bottom - corte); lineTo(r.left, r.top + corte); close()
}
