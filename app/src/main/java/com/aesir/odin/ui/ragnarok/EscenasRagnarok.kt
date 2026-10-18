package com.aesir.odin.ui.ragnarok

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aesir.odin.R
import com.aesir.odin.ui.components.odin.rememberTiempo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

// Escenas animadas del Ragnarök (mockup "Ragnarök · reto de fin de mundo").
//
// El lienzo es la pantalla del mockup sin la barra de estado: 390 dp de ancho
// y (1168 − 40) px del mockup de alto. Las medidas de este archivo están en
// PÍXELES del mockup (540 × 1168) para compararlas directo con las imágenes.

/** Constantes y conversiones del lienzo del Ragnarök. */
object RG {
    /** px del mockup → dp de diseño. */
    const val K = 390f / 540f
    const val ARRIBA = 40f
    const val ANCHO = 390f
    val ALTO = (1168f - ARRIBA) * K

    fun x(px: Float) = px * K
    fun y(px: Float) = (px - ARRIBA) * K
    fun d(px: Float) = px * K

    val Fondo = Color(0xFF0B090A)
    val Crema = Color(0xFFF1E6D2)
    val CremaTenue = Color(0xFFC8BDAA)
    val GrisTexto = Color(0xFFBDB2A0)
    val Naranja = Color(0xFFE8853A)
    val NaranjaTexto = Color(0xFFE0823A)
    val Oro = Color(0xFFE9B96A)
    val Turquesa = Color(0xFF6FCBDB)
    val Verde = Color(0xFF5BD3A0)
    val Panel = Color(0xFF0D0B0A)
    val BordePanel = Color(0xFF6E5638)
}

/** Coloca un elemento centrado en (x, y) px del mockup, con tamaño en px del mockup. */
fun Modifier.enPx(x: Float, y: Float, w: Float, h: Float): Modifier =
    this
        .offset(x = (RG.x(x) - RG.d(w) / 2f).dp, y = (RG.y(y) - RG.d(h) / 2f).dp)
        .requiredSize(RG.d(w).dp, RG.d(h).dp)

// ---------------------------------------------------------------------------
// Trazos
// ---------------------------------------------------------------------------

/** Lee trazos "M x y L x y x y … Z" (solo líneas rectas). */
fun rutaDe(d: String): Path {
    val p = Path().apply { fillType = PathFillType.EvenOdd }
    var comando = 'M'
    var pendiente: Float? = null
    for (token in d.split(' ')) {
        if (token.isEmpty()) continue
        val c = token[0]
        if (c.isLetter()) {
            if (c == 'Z') {
                p.close()
                comando = 'M'
            } else {
                comando = c
            }
            continue
        }
        val v = token.toFloatOrNull() ?: 0f
        val x = pendiente
        if (x == null) {
            pendiente = v
        } else {
            pendiente = null
            if (comando == 'M') {
                p.moveTo(x, v)
                comando = 'L'
            } else {
                p.lineTo(x, v)
            }
        }
    }
    return p
}

private fun lineas(puntos: List<Offset>, cerrar: Boolean = false): Path = Path().apply {
    moveTo(puntos[0].x, puntos[0].y)
    for (i in 1 until puntos.size) lineTo(puntos[i].x, puntos[i].y)
    if (cerrar) close()
}

private fun suelo(puntos: List<Offset>, base: Float): Pair<Path, Path> {
    val borde = lineas(puntos)
    val relleno = lineas(puntos).apply {
        lineTo(540f, base)
        lineTo(0f, base)
        close()
    }
    return relleno to borde
}

/** Rutas ya leídas (se arman una sola vez). */
object RutasRagnarok {
    val lobo: List<Path> by lazy { ArteRagnarok.lobo.map { rutaDe(it.d) } }
    val serpiente: List<Path> by lazy { ArteRagnarok.serpiente.map { rutaDe(it.d) } }
    val cuerpoLobo: Path by lazy { rutaDe(ArteRagnarok.lobo[0].d) }

    val sueloEntrada by lazy { suelo(ArteRagnarok.suelo, 760f) }
    val sueloCaido by lazy {
        suelo(
            listOf(
                Offset(0f, 492.5f), Offset(42.5f, 489f), Offset(90f, 480f), Offset(140f, 489f),
                Offset(200f, 485f), Offset(260f, 494f), Offset(295f, 482.5f), Offset(340f, 495f),
                Offset(380f, 480f), Offset(410f, 492.5f), Offset(450f, 484f), Offset(480f, 489f),
                Offset(540f, 484f)
            ), 560f
        )
    }
    val montanas by lazy {
        suelo(
            listOf(
                Offset(0f, 499f), Offset(25f, 463f), Offset(75f, 478f), Offset(112f, 487.5f),
                Offset(165f, 475f), Offset(213f, 451.5f), Offset(262f, 478f), Offset(277f, 461f),
                Offset(291f, 441f), Offset(318f, 430f), Offset(370f, 445f), Offset(410f, 476f),
                Offset(453f, 451f), Offset(485f, 463f), Offset(525f, 449.5f), Offset(540f, 452f)
            ), 600f
        )
    }

    /** Cuerpo de la serpiente que asoma abajo a la izquierda en la batalla. */
    val espiral by lazy {
        lineas(listOf(
            Offset(-4f, 842f), Offset(11f, 827.5f), Offset(70f, 882.5f), Offset(55f, 905f),
            Offset(52.5f, 930f), Offset(60f, 980f), Offset(67.5f, 1015f), Offset(75f, 1060f),
            Offset(80f, 1100f), Offset(82f, 1180f), Offset(-4f, 1180f)), cerrar = true
        )
    }
}

// ---------------------------------------------------------------------------
// Dibujo en coordenadas del mockup
// ---------------------------------------------------------------------------

/** Dibuja con unidades del mockup (px, con y desde el borde de arriba de la pantalla del mockup). */
private inline fun DrawScope.enPixeles(bloque: DrawScope.() -> Unit) {
    val f = size.width / 540f
    withTransform({
        scale(f, f, pivot = Offset.Zero)
        translate(0f, -RG.ARRIBA)
    }) { bloque() }
}

private fun DrawScope.circulo(c: Offset, r: Float, brush: Brush, blendMode: BlendMode = BlendMode.SrcOver) =
    drawCircle(brush = brush, radius = r, center = c, blendMode = blendMode)

/** Fenrir. [ojo] = intensidad del brillo del ojo (0…1.5). */
fun DrawScope.dibujarLobo(ojo: Float = 1f) {
    ArteRagnarok.lobo.forEachIndexed { i, parte ->
        val ruta = RutasRagnarok.lobo[i]
        when (parte.nombre) {
            "boca" -> drawPath(
                ruta,
                Brush.radialGradient(
                    0f to Color(0xFFF6984A), 0.45f to Color(0xFFCF5F22), 1f to Color(0xFF2C0C05),
                    center = Offset(305f, 396f), radius = 62f
                )
            )
            "brilloOjo" -> {
                val c = Offset(394f, 309f)
                val r = 36f * (0.85f + 0.15f * ojo)
                circulo(
                    c, r, Brush.radialGradient(
                        0f to Color(0xFFFF9A40).copy(alpha = (0.7f * ojo).coerceIn(0f, 1f)),
                        0.45f to Color(0xFFE2701E).copy(alpha = (0.25f * ojo).coerceIn(0f, 1f)),
                        1f to Color(0x00E2701E),
                        center = c, radius = r
                    )
                )
            }
            else -> parte.color?.let { drawPath(ruta, it, alpha = parte.opacidad) }
        }
    }
}

/** Jörmungandr. [ojo] = intensidad del brillo del ojo. */
fun DrawScope.dibujarSerpiente(ojo: Float = 1f) {
    ArteRagnarok.serpiente.forEachIndexed { i, parte ->
        val ruta = RutasRagnarok.serpiente[i]
        if (parte.nombre == "brilloOjo") {
            val c = Offset(102.5f, 282f)
            val r = 15f * (0.85f + 0.15f * ojo)
            circulo(
                c, r, Brush.radialGradient(
                    listOf(Color(0xFF9FF5DC).copy(alpha = (0.65f * ojo).coerceIn(0f, 1f)), Color(0x005FE0C0)),
                    center = c, radius = r
                )
            )
        } else {
            parte.color?.let { drawPath(ruta, it, alpha = parte.opacidad) }
        }
    }
}

private fun DrawScope.dibujarSuelo(suelo: Pair<Path, Path>, borde: Color, ancho: Float = 1.1f) {
    drawPath(suelo.first, RG.Fondo)
    drawPath(suelo.second, borde, style = Stroke(width = ancho))
}

// ---------------------------------------------------------------------------
// Brasas
// ---------------------------------------------------------------------------

/** Brasas que suben en diagonal con una estela corta (como en el mockup). Zona en px del mockup. */
@Composable
fun Brasas(
    cantidad: Int = 34,
    zona: Rect = Rect(0f, 40f, 540f, 1168f),
    color: Color = Color(0xFFF2B567),
    semilla: Int = 7,
    modifier: Modifier = Modifier
) {
    val tiempo = rememberTiempo()
    Canvas(modifier.fillMaxSize()) {
        val t = tiempo.value
        enPixeles {
            val rnd = Random(semilla)
            repeat(cantidad) {
                val x0 = rnd.nextFloat()
                val fase = rnd.nextFloat()
                val velocidad = 0.035f + rnd.nextFloat() * 0.05f
                val deriva = 14f + rnd.nextFloat() * 30f
                val tam = 1.1f + rnd.nextFloat() * 1.9f
                val brillo = 0.45f + rnd.nextFloat() * 0.55f
                val avance = (t * velocidad + fase) % 1f
                val y = zona.bottom - avance * zona.height
                val x = zona.left + x0 * zona.width + avance * deriva + sin(t * 0.8f + x0 * 9f) * 4f
                val alpha = brillo * sin(avance * PI.toFloat())
                if (alpha > 0.02f) {
                    val p = Offset(x, y)
                    val cola = Offset(x - 4.4f, y + 9.6f)
                    drawLine(
                        Brush.linearGradient(listOf(color.copy(alpha = alpha * 0.55f), color.copy(alpha = 0f)), p, cola),
                        p, cola, strokeWidth = tam * 0.8f
                    )
                    circulo(p, tam * 2.2f, Brush.radialGradient(listOf(color.copy(alpha = alpha * 0.35f), color.copy(alpha = 0f)), p, tam * 2.2f))
                    drawCircle(Color(0xFFFFE2B0).copy(alpha = alpha), tam, p)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Escena de entrada
// ---------------------------------------------------------------------------

/** Fenrir y Jörmungandr frente al eclipse, el árbol y la espada en llamas. */
@Composable
fun EscenaEntrada() {
    val tiempo = rememberTiempo()
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.rag_fondo_entrada), null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.offset(y = RG.y(0f).dp).requiredSize(RG.ANCHO.dp, RG.d(712f).dp)
        )
        Canvas(Modifier.fillMaxSize()) {
            val t = tiempo.value
            enPixeles {
                dibujarRayo(t)
                dibujarFuego(t)
                dibujarEclipse(t)
                rotate(sin(t * 0.9f) * 0.6f, pivot = Offset(40f, 690f)) {
                    dibujarSerpiente(0.8f + 0.25f * sin(t * 2.3f))
                }
                scale(1f, 1f + 0.009f * sin(t * 1.7f), pivot = Offset(420f, 690f)) {
                    dibujarLobo(0.85f + 0.3f * max(0f, sin(t * 1.3f)))
                }
                dibujarSuelo(RutasRagnarok.sueloEntrada, Color(0xD9C98A5C))
            }
        }
        Brasas(cantidad = 38, zona = Rect(0f, 120f, 540f, 1120f))
    }
}

private fun DrawScope.dibujarEclipse(t: Float) {
    val c = Offset(227.5f, 330f)
    val pulso = 0.75f + 0.25f * sin(t * 1.4f)
    circulo(
        c, 100f, Brush.radialGradient(
            0.6f to Color(0x00FFE6B0),
            0.67f to Color(0xFFFFE6B0).copy(alpha = 0.22f * pulso),
            0.8f to Color(0xFFF0A050).copy(alpha = 0.10f * pulso),
            1f to Color(0x00F0A050),
            center = c, radius = 100f
        ), BlendMode.Plus
    )
    circulo(
        c, 64.5f, Brush.radialGradient(
            0f to Color(0xFF0C0505), 0.85f to Color(0xFF110606), 1f to Color(0xFF1C0806),
            center = c, radius = 64.5f
        )
    )
    drawCircle(Color(0xFFB3361E), 65.6f, c, style = Stroke(2.4f))
}

/** Llamas detrás de la espada: tres resplandores que titilan. */
private fun DrawScope.dibujarFuego(t: Float) {
    val focos = listOf(
        floatArrayOf(258f, 640f, 26f, 2.7f, 0f),
        floatArrayOf(284f, 630f, 22f, 3.4f, 1.7f),
        floatArrayOf(271f, 600f, 30f, 2.1f, 0.6f)
    )
    for (f in focos) {
        val (x, y, r) = Triple(f[0], f[1], f[2])
        val frec = f[3]
        val fase = f[4]
        val a = (0.16f + 0.12f * sin(t * frec + fase) + 0.05f * sin(t * frec * 2.3f + fase)).coerceIn(0f, 1f)
        val rr = r * (1f + 0.12f * sin(t * frec * 1.3f + fase))
        withTransform({ scale(1f, 2.2f, pivot = Offset(x, y)) }) {
            circulo(
                Offset(x, y), rr,
                Brush.radialGradient(listOf(Color(0xFFFF9A3C).copy(alpha = a), Color(0x00FF6A1A)), Offset(x, y), rr),
                BlendMode.Plus
            )
        }
    }
    val a = (0.35f + 0.25f * sin(t * 2.2f)).coerceIn(0f, 1f)
    circulo(
        Offset(270f, 474f), 14f,
        Brush.radialGradient(listOf(Color(0xFFFFE0A0).copy(alpha = a), Color(0x00FFB050)), Offset(270f, 474f), 14f),
        BlendMode.Plus
    )
}

/** Rayo de arriba: siempre tenue y, de vez en cuando, un destello. */
private fun DrawScope.dibujarRayo(t: Float) {
    val rayo = lineas(listOf(
        Offset(121f, 0f), Offset(123f, 48f), Offset(138f, 68f), Offset(133f, 92f),
        Offset(150f, 117f), Offset(164f, 140f), Offset(168f, 152f)
    ))
    val ciclo = t % 7f
    val destello = when {
        ciclo < 0.12f -> 1f
        ciclo > 0.2f && ciclo < 0.3f -> 0.7f
        else -> 0f
    }
    if (destello > 0f) {
        drawRect(
            Brush.verticalGradient(listOf(Color(0xFFB8C8FF).copy(alpha = 0.10f * destello), Color.Transparent), 0f, 420f),
            topLeft = Offset(0f, 0f), size = Size(540f, 420f), blendMode = BlendMode.Plus
        )
        drawPath(rayo, Color(0xFFCFD8FF).copy(alpha = 0.35f * destello), style = Stroke(6f, cap = StrokeCap.Round, join = StrokeJoin.Round), blendMode = BlendMode.Plus)
    }
    drawPath(rayo, Color(0xFFC9C6D2).copy(alpha = 0.55f + 0.45f * destello), style = Stroke(1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

// ---------------------------------------------------------------------------
// Escena "El crepúsculo te alcanzó"
// ---------------------------------------------------------------------------

/** Los monstruos ganaron: el árbol cayó y el eclipse se apagó. */
@Composable
fun EscenaCaida() {
    val tiempo = rememberTiempo()
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.rag_fondo_caido), null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.offset(y = RG.y(0f).dp).requiredSize(RG.ANCHO.dp, RG.d(520f).dp)
        )
        Canvas(Modifier.fillMaxSize()) {
            val t = tiempo.value
            enPixeles {
                translate(0f, -185f) {
                    rotate(sin(t * 0.9f) * 0.6f, pivot = Offset(40f, 690f)) {
                        dibujarSerpiente(0.9f + 0.3f * sin(t * 2.3f))
                    }
                    scale(1f, 1f + 0.009f * sin(t * 1.7f), pivot = Offset(420f, 690f)) {
                        dibujarLobo(1f + 0.35f * max(0f, sin(t * 1.3f)))
                    }
                }
                dibujarSuelo(RutasRagnarok.sueloCaido, Color(0x998A6A50), 1f)
                // Resplandor cálido de abajo
                drawRect(
                    Brush.verticalGradient(
                        0f to Color(0x003D2210), 0.75f to Color(0xD93D2210), 1f to Color(0xFF3D2210),
                        startY = 930f, endY = 1168f
                    ),
                    topLeft = Offset(0f, 930f), size = Size(540f, 238f)
                )
            }
        }
        Brasas(cantidad = 30, zona = Rect(0f, 40f, 540f, 1140f), semilla = 11)
    }
}

// ---------------------------------------------------------------------------
// Fondo de la batalla
// ---------------------------------------------------------------------------

/** Fondo oscuro de las preguntas: Fenrir acecha arriba a la derecha y la serpiente asoma abajo. */
@Composable
fun FondoBatalla() {
    val tiempo = rememberTiempo()
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF120D0C), 0.35f to Color(0xFF170F0E), 0.6f to Color(0xFF1A0F0C), 1f to Color(0xFF210F09)
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val t = tiempo.value
            enPixeles {
                translate(82.6f, -131f) {
                    scale(0.898f, 0.898f, pivot = Offset.Zero) {
                        scale(1f, 1f + 0.008f * sin(t * 1.6f), pivot = Offset(420f, 690f)) {
                            dibujarLobo(0f)
                            drawPath(RutasRagnarok.cuerpoLobo, Color.Black.copy(alpha = 0.35f))
                            val pulso = 0.8f + 0.25f * sin(t * 1.5f)
                            val c = Offset(394f, 309f)
                            circulo(
                                c, 26f,
                                Brush.radialGradient(listOf(Color(0xFFFF8A30).copy(alpha = (0.4f * pulso).coerceIn(0f, 1f)), Color(0x00FF6A10)), c, 26f),
                                BlendMode.Plus
                            )
                            drawOval(Color(0xFFFFB060).copy(alpha = (0.8f * pulso).coerceIn(0f, 1f)), Offset(c.x - 9f, c.y - 4f), Size(18f, 8f))
                        }
                    }
                }
                val espiral = RutasRagnarok.espiral
                drawPath(espiral, Color(0xFF0D0D0D))
                clipPath(espiral) { dibujarEscamas() }
                drawPath(lineas(listOf(Offset(-4f, 842f), Offset(11f, 827.5f), Offset(70f, 882.5f))), Color(0xBF6FA9AE), style = Stroke(2f))
                drawPath(
                    lineas(listOf(Offset(55f, 905f), Offset(52.5f, 930f), Offset(60f, 980f), Offset(67.5f, 1015f))),
                    Color(0x596FA9AE), style = Stroke(1f)
                )
            }
        }
        Brasas(cantidad = 26, zona = Rect(0f, 500f, 540f, 1160f), semilla = 3)
    }
}

private fun DrawScope.dibujarEscamas() {
    val color = Color(0xFF3A4246)
    var fila = 0
    var y = 840f
    while (y < 1180f) {
        var x = -8f + if (fila % 2 == 0) 0f else 8f
        while (x < 90f) {
            drawArc(
                color, startAngle = 30f, sweepAngle = 120f, useCenter = false,
                topLeft = Offset(x - 7f, y - 11f), size = Size(14f, 14f), style = Stroke(1.1f)
            )
            x += 16f
        }
        y += 13f
        fila++
    }
}

// ---------------------------------------------------------------------------
// Escena "El mundo renace"
// ---------------------------------------------------------------------------

private val ordenesMedallones = listOf(4, 1, 3, 2)
private val centrosMedallones = listOf(Offset(199.3f, 241.7f), Offset(341.7f, 241.7f), Offset(199.3f, 383.3f), Offset(341.7f, 383.3f))

/** El sol renace dentro del uróboros, rodeado por los cuatro mundos. */
@Composable
fun EscenaRenace() {
    val tiempo = rememberTiempo()
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .offset(y = RG.y(480f).dp)
                .requiredSize(RG.ANCHO.dp, RG.d(1168f - 480f).dp)
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFF080808), 0.08f to Color(0xFF080808), 0.17f to Color(0xFF1D140F),
                        0.3f to Color(0xFF291B18), 0.45f to Color(0xFF242015), 0.75f to Color(0xFF2E2213),
                        1f to Color(0xFF35250E)
                    )
                )
        )
        Image(
            painterResource(R.drawable.rag_fondo_renace), null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.offset(y = RG.y(0f).dp).requiredSize(RG.ANCHO.dp, RG.d(505f).dp)
        )
        Canvas(Modifier.fillMaxSize()) {
            val t = tiempo.value
            enPixeles {
                dibujarSol(t)
                dibujarUroboros(t)
                dibujarArbolito(t)
                drawPath(RutasRagnarok.montanas.first, Color(0xFF0A0909))
                drawPath(RutasRagnarok.montanas.second, Color(0xD9B9E3C0), style = Stroke(1.2f))
                for (i in 0 until 28) {
                    drawRoundRect(
                        Color(0xFF151413), topLeft = Offset(i * 20f + 4f, 538f), size = Size(14f, 22f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f), style = Stroke(1f)
                    )
                }
                ordenesMedallones.forEachIndexed { i, orden ->
                    dibujarMedallon(orden, centrosMedallones[i], 26f, brilla = true, t = t)
                }
            }
        }
        Brasas(cantidad = 30, zona = Rect(0f, 150f, 540f, 1150f), color = Color(0xFFF0C070), semilla = 21)
    }
}

private fun DrawScope.dibujarSol(t: Float) {
    val c = Offset(270f, 318.3f)
    val pulso = 1f + 0.012f * sin(t * 1.2f)
    circulo(
        c, 150f, Brush.radialGradient(
            0.35f to Color(0xFFFFD27A).copy(alpha = 0.30f + 0.06f * sin(t * 1.2f)),
            0.65f to Color(0x1AE89A40), 1f to Color(0x00E89A40),
            center = c, radius = 150f
        ), BlendMode.Plus
    )
    val r1 = 71.5f * pulso
    circulo(c, r1, Brush.radialGradient(0.7f to Color(0xFFE8BA60), 0.86f to Color(0xFFC79A4C), 1f to Color(0xFF9A7A3E), center = c, radius = r1))
    val r2 = 52f * pulso
    circulo(c, r2, Brush.radialGradient(0f to Color(0xFFFAE9BB), 0.4f to Color(0xFFF5DCA0), 1f to Color(0xFFEEC27A), center = c, radius = r2))
}

/** La serpiente que se muerde la cola, girando despacio. */
private fun DrawScope.dibujarUroboros(t: Float) {
    val c = Offset(270f, 318.3f)
    val rm = 79.5f
    rotate((t * 4f) % 360f, pivot = c) {
        fun arco(r: Float, color: Color, ancho: Float) = drawArc(
            color, startAngle = -68f, sweepAngle = 324f, useCenter = false,
            topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2), style = Stroke(ancho)
        )
        arco(rm, Color(0xFF1B1714), 19f)
        arco(rm + 9.5f, Color(0xCC6A5236), 1f)
        arco(rm - 9.5f, Color(0x996A5236), 1f)
        var a = -62f
        while (a < 250f) {
            rotate(a, pivot = c) {
                drawPath(
                    lineas(listOf(Offset(c.x + rm - 6f, c.y - 3.5f), Offset(c.x + rm, c.y + 2.5f), Offset(c.x + rm + 6f, c.y - 3.5f))),
                    Color(0xFFA48A5E), style = Stroke(1.3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            a += 11f
        }
        rotate(-68f, pivot = c) {
            val punta = lineas(listOf(Offset(c.x + rm - 9.5f, c.y), Offset(c.x + rm + 9.5f, c.y), Offset(c.x + rm + 1f, c.y - 15f)), cerrar = true)
            drawPath(punta, Color(0xFF1B1714))
            drawPath(punta, Color(0xCC6A5236), style = Stroke(1f))
        }
        rotate(-104f, pivot = c) {
            drawRoundRect(Color(0xFF201B17), Offset(c.x + rm - 9f, c.y - 2f), Size(18f, 30f), androidx.compose.ui.geometry.CornerRadius(6f))
            drawRoundRect(Color(0xFF6A5236), Offset(c.x + rm - 9f, c.y - 2f), Size(18f, 30f), androidx.compose.ui.geometry.CornerRadius(6f), style = Stroke(1f))
            drawCircle(Color(0xFFE9C46A), 3.5f, Offset(c.x + rm, c.y + 20.5f))
        }
    }
}

private fun DrawScope.dibujarArbolito(t: Float) {
    rotate(sin(t * 0.8f) * 1.2f, pivot = Offset(270f, 500f)) {
        val brillo = 0.75f + 0.2f * sin(t * 1.6f)
        val hojas = listOf(
            floatArrayOf(238f, 414f, 17f, 7f), floatArrayOf(254f, 402f, 15f, 8f), floatArrayOf(286f, 402f, 15f, 8f),
            floatArrayOf(302f, 414f, 17f, 7f), floatArrayOf(270f, 398f, 12f, 9f)
        )
        for (h in hojas) {
            val c = Offset(h[0], h[1])
            withTransform({ scale(h[2] / h[3], 1f, pivot = c) }) {
                circulo(
                    c, h[3] * 1.8f,
                    Brush.radialGradient(listOf(Color(0xFF3FD6A0).copy(alpha = (0.6f * brillo).coerceIn(0f, 1f)), Color(0x003FD6A0)), c, h[3] * 1.8f),
                    BlendMode.Plus
                )
            }
        }
        drawPath(lineas(listOf(Offset(264.5f, 505f), Offset(268f, 432f), Offset(272.5f, 432f), Offset(275.5f, 505f)), cerrar = true), Color(0xFF0E0C0B))
        for ((x, y) in listOf(244f to 412f, 256f to 404f, 270f to 400f, 284f to 404f, 296f to 412f)) {
            drawLine(Color(0xFF0E0C0B), Offset(270f, 440f), Offset(x, y), strokeWidth = 2.2f, cap = StrokeCap.Round)
        }
    }
}

// ---------------------------------------------------------------------------
// Emblemas de los mundos
// ---------------------------------------------------------------------------

/** Color del emblema de cada mundo: 1 copo (oro), 2 ojo, 3 árbol-runa, 4 martillo. */
fun colorEmblemaRagnarok(orden: Int): Color = when ((orden - 1).coerceAtLeast(0) % 4) {
    1 -> Color(0xFF4FD3D3)
    2 -> Color(0xFF3FD6A6)
    3 -> Color(0xFFE8793A)
    else -> Color(0xFFE9B96A)
}

/** Ícono del emblema centrado en [c]; [s] = radio del medallón (en las unidades del DrawScope). */
fun DrawScope.dibujarEmblemaRagnarok(orden: Int, c: Offset, s: Float, color: Color) {
    val k = s / 26f
    fun q(x: Float, y: Float) = Offset(c.x + x * k, c.y + y * k)
    val trazo = Stroke(1.7f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when ((orden - 1).coerceAtLeast(0) % 4) {
        1 -> { // ojo
            val ojo = Path().apply {
                moveTo(q(-15f, 0f).x, q(-15f, 0f).y)
                quadraticTo(q(0f, -15f).x, q(0f, -15f).y, q(15f, 0f).x, q(15f, 0f).y)
                quadraticTo(q(0f, 15f).x, q(0f, 15f).y, q(-15f, 0f).x, q(-15f, 0f).y)
                close()
            }
            drawPath(ojo, color, style = trazo)
            drawCircle(color, 6.5f * k, c, style = trazo)
            drawCircle(color, 3f * k, c)
        }
        2 -> { // árbol-runa
            drawCircle(color, 13f * k, c, style = trazo)
            val p = Path().apply {
                fun l(a: Offset, b: Offset) { moveTo(a.x, a.y); lineTo(b.x, b.y) }
                l(q(0f, -13f), q(0f, 13f))
                l(q(0f, -2f), q(-8.5f, -9f)); l(q(0f, -2f), q(8.5f, -9f))
                l(q(-8.5f, -3f), q(8.5f, -3f))
                l(q(0f, 5f), q(-8f, 11f)); l(q(0f, 5f), q(8f, 11f))
            }
            drawPath(p, color, style = trazo)
        }
        3 -> { // martillo
            val cabeza = lineas(listOf(q(-13.5f, -13f), q(12.5f, -13f), q(8.8f, -3.8f), q(-10f, -3.8f)), cerrar = true)
            drawPath(cabeza, color, style = trazo)
            drawLine(color, q(-9.5f, -8f), q(7.5f, -8f), strokeWidth = trazo.width, cap = StrokeCap.Round)
            drawRect(color, Offset(c.x - 2.8f * k, c.y - 3.8f * k), Size(5.6f * k, 17f * k), style = trazo)
            drawOval(color, Offset(c.x - 5f * k, c.y + 13f * k), Size(10f * k, 4.5f * k), style = trazo)
        }
        else -> { // copo
            val p = Path()
            for (i in 0 until 6) {
                val a = (i * 60f - 90f) * PI.toFloat() / 180f
                val d = Offset(cos(a), sin(a))
                val n = Offset(-d.y, d.x)
                fun r(largo: Float, lado: Float) = q(d.x * largo + n.x * lado, d.y * largo + n.y * lado)
                fun l(a1: Offset, b1: Offset) { p.moveTo(a1.x, a1.y); p.lineTo(b1.x, b1.y) }
                l(q(0f, 0f), r(14f, 0f))
                l(r(8.5f, 0f), r(12.5f, 4f)); l(r(8.5f, 0f), r(12.5f, -4f))
                l(r(4.5f, 0f), r(7f, 2.6f)); l(r(4.5f, 0f), r(7f, -2.6f))
            }
            drawPath(p, color, style = Stroke(2.3f * k, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/** Medallón redondo con el emblema de un mundo. */
fun DrawScope.dibujarMedallon(orden: Int, c: Offset, r: Float, brilla: Boolean, t: Float, color: Color = colorEmblemaRagnarok(orden)) {
    if (brilla) {
        val pulso = 0.75f + 0.25f * sin(t * 1.8f + orden)
        circulo(
            c, r * 1.5f, Brush.radialGradient(
                0.55f to color.copy(alpha = 0f), 0.68f to color.copy(alpha = 0.35f * pulso), 1f to color.copy(alpha = 0f),
                center = c, radius = r * 1.5f
            ), BlendMode.Plus
        )
    }
    drawCircle(Color(0xFF0E0C0B), r, c)
    drawCircle(color, r - 1f, c, style = Stroke(r * 0.075f))
    drawCircle(color.copy(alpha = 0.4f), r * 0.82f, c, style = Stroke(max(r * 0.03f, 0.7f)))
    dibujarEmblemaRagnarok(orden, c, r * 0.82f, color)
}

/** Medallón como vista (barra de navegación, panel de desbloqueo); centro y radio en px del mockup. */
@Composable
fun MedallonMundo(orden: Int, x: Float, y: Float, radio: Float = 26f, brilla: Boolean = false) {
    val tiempo = rememberTiempo()
    Canvas(Modifier.enPx(x, y, radio * 3f, radio * 3f)) {
        val f = size.width / (radio * 3f)
        dibujarMedallon(orden, center, radio * f, brilla, tiempo.value)
    }
}
