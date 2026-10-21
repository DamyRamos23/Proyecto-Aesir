package com.aesir.odin.ui.moneda

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.aesir.odin.R
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.components.odin.dibujarValknut
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.theme.Emblema
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Las otras caras del escudo de Mundos
// ---------------------------------------------------------------------------

/** Cómo se mueve una capa de la escena. Coordenadas en fracciones de la escena (0..1). */
internal sealed interface AnimCapa {
    /** Vaivén horizontal. */
    data class Deriva(val dx: Float, val periodo: Float, val fase: Float) : AnimCapa
    /** Vaivén vertical. */
    data class Flotar(val dy: Float, val periodo: Float, val fase: Float) : AnimCapa
    /** Opacidad que late entre [min] y [max]. */
    data class Latir(val min: Float, val max: Float, val periodo: Float, val fase: Float) : AnimCapa
    /** Giro continuo sobre un pivote. */
    data class Girar(val px: Float, val py: Float, val gradosPorSegundo: Float) : AnimCapa
    /** Se mece sobre un pivote. */
    data class Mecer(val px: Float, val py: Float, val grados: Float, val periodo: Float) : AnimCapa
    /** Martillo: sube despacio y cae de golpe ([curvaGolpe]). */
    data class Golpe(val px: Float, val py: Float, val grados: Float, val periodo: Float) : AnimCapa
    /** Destello sincronizado con el golpe ([curvaDestello]). */
    data class Destello(val periodo: Float) : AnimCapa
    /** Llama que tiembla desde su base. */
    data class Flama(val px: Float, val py: Float, val periodo: Float) : AnimCapa
    /** Cruza de izquierda a derecha y vuelve a empezar. */
    data class Vuelo(val periodo: Float) : AnimCapa
    /** Cae y se repite (nieve). */
    data class Caer(val periodo: Float) : AnimCapa
}

/** Una capa de la escena: su imagen y dónde va (fracciones de la escena). */
internal class CapaEscena(
    @DrawableRes val imagen: Int,
    val x: Float,
    val y: Float,
    val ancho: Float,
    val alto: Float,
    val anims: List<AnimCapa>
)

/**
 * Capas de la escena viva de cada cara, de atrás hacia adelante. Generadas desde
 * las ilustraciones (tools/caras/capas.py); las imágenes están en drawable-nodpi.
 */
internal fun capasDe(cara: CaraMoneda): List<CapaEscena> = CAPAS.getValue(cara)

/** Se arman una sola vez (no en cada cuadro). */
private val CAPAS: Map<CaraMoneda, List<CapaEscena>> by lazy { CaraMoneda.entries.associateWith { construirCapas(it) } }

private fun construirCapas(cara: CaraMoneda): List<CapaEscena> = when (cara) {
    CaraMoneda.MUNDOS -> emptyList()
    CaraMoneda.HASTA_EL_ALBA -> listOf(
        CapaEscena(R.drawable.moneda_alba_fondo, 0f, 0f, 1f, 1f, listOf()),
        CapaEscena(R.drawable.moneda_alba_estrellas, 0.1133f, 0.0443f, 0.7839f, 0.2721f, listOf(AnimCapa.Latir(0.3f, 1f, 2.6f, 0f))),
        CapaEscena(R.drawable.moneda_alba_rayos, -0.2f, -0.05f, 1.4f, 1.4f, listOf(AnimCapa.Girar(0.5f, 0.66f, 6f), AnimCapa.Latir(0.6f, 1f, 4f, 0f))),
        CapaEscena(R.drawable.moneda_alba_sol, 0.0482f, 0.2083f, 0.9036f, 0.7917f, listOf(AnimCapa.Latir(0.85f, 1f, 3f, 0f))),
        CapaEscena(R.drawable.moneda_alba_nubes, 0f, 0.4427f, 0.543f, 0.069f, listOf(AnimCapa.Deriva(0.07f, 12f, 0f))),
        CapaEscena(R.drawable.moneda_alba_nubes2, 0.5872f, 0.3516f, 0.4128f, 0.0703f, listOf(AnimCapa.Deriva(0.06f, 16f, 0.5f))),
        CapaEscena(R.drawable.moneda_alba_aves, 0.3932f, 0.2383f, 0.1328f, 0.099f, listOf(AnimCapa.Vuelo(14f), AnimCapa.Flotar(0.015f, 1.4f, 0f))),
        CapaEscena(R.drawable.moneda_alba_montes, 0f, 0.4674f, 1f, 0.5326f, listOf()),
        CapaEscena(R.drawable.moneda_alba_guerrero1, 0.2161f, 0.5273f, 0.1823f, 0.2357f, listOf(AnimCapa.Mecer(0.29f, 0.76f, 4f, 3.2f))),
        CapaEscena(R.drawable.moneda_alba_guerrero2, 0.5469f, 0.5273f, 0.224f, 0.2357f, listOf(AnimCapa.Mecer(0.71f, 0.76f, -4f, 3.6f))),
        CapaEscena(R.drawable.moneda_alba_frente, 0f, 0.7669f, 1f, 0.2331f, listOf()),
    )
    CaraMoneda.APUESTA_BROKKR -> listOf(
        CapaEscena(R.drawable.moneda_brokkr_fondo, 0f, 0f, 1f, 1f, listOf()),
        CapaEscena(R.drawable.moneda_brokkr_brasa, 0.1081f, 0.4375f, 0.4036f, 0.4049f, listOf(AnimCapa.Latir(0.65f, 1f, 0.9f, 0f))),
        CapaEscena(R.drawable.moneda_brokkr_fuego, 0.2357f, 0.5169f, 0.1471f, 0.1862f, listOf(AnimCapa.Flama(0.31f, 0.7f, 0.45f))),
        CapaEscena(R.drawable.moneda_brokkr_yunque, 0.5169f, 0.6068f, 0.4193f, 0.276f, listOf()),
        CapaEscena(R.drawable.moneda_brokkr_martillo, 0.668f, 0.4961f, 0.2383f, 0.2891f, listOf(AnimCapa.Golpe(0.89f, 0.77f, 34f, 1.6f))),
        CapaEscena(R.drawable.moneda_brokkr_destello, 0.5924f, 0.4583f, 0.2995f, 0.3034f, listOf(AnimCapa.Destello(1.6f))),
    )
    CaraMoneda.VOLVA -> listOf(
        CapaEscena(R.drawable.moneda_volva_fondo, 0f, 0f, 1f, 1f, listOf()),
        CapaEscena(R.drawable.moneda_volva_estrellas, 0.0938f, 0.1016f, 0.7656f, 0.4049f, listOf(AnimCapa.Latir(0.2f, 1f, 2.2f, 0f))),
        CapaEscena(R.drawable.moneda_volva_estrellas2, 0.2227f, 0.0729f, 0.694f, 0.3346f, listOf(AnimCapa.Latir(0.2f, 1f, 2.9f, 0.5f))),
        CapaEscena(R.drawable.moneda_volva_aurora, 0f, 0.2253f, 1f, 0.2109f, listOf(AnimCapa.Deriva(0.08f, 9f, 0f), AnimCapa.Latir(0.5f, 1f, 5f, 0f))),
        CapaEscena(R.drawable.moneda_volva_luna, 0.3385f, 0f, 0.6029f, 0.5013f, listOf(AnimCapa.Latir(0.85f, 1f, 4f, 0f))),
        CapaEscena(R.drawable.moneda_volva_bosque, 0f, 0.4466f, 1f, 0.5534f, listOf()),
        CapaEscena(R.drawable.moneda_volva_resplandor, 0.1289f, 0.4479f, 0.7031f, 0.5521f, listOf(AnimCapa.Latir(0.6f, 1f, 0.8f, 0f))),
        CapaEscena(R.drawable.moneda_volva_humo, 0.4779f, 0.3776f, 0.0651f, 0.2852f, listOf(AnimCapa.Flotar(-0.04f, 3.5f, 0f), AnimCapa.Latir(0.3f, 1f, 3.5f, 0.25f), AnimCapa.Deriva(0.02f, 5f, 0f))),
        CapaEscena(R.drawable.moneda_volva_fuego, 0.4245f, 0.6771f, 0.1211f, 0.1654f, listOf(AnimCapa.Flama(0.48f, 0.84f, 0.4f))),
        CapaEscena(R.drawable.moneda_volva_lena, 0.2669f, 0.819f, 0.2943f, 0.1445f, listOf()),
        CapaEscena(R.drawable.moneda_volva_volva, 0.5365f, 0.4167f, 0.2969f, 0.4831f, listOf(AnimCapa.Mecer(0.73f, 0.88f, 1.5f, 4.5f))),
        CapaEscena(R.drawable.moneda_volva_orbe, 0.5326f, 0.4128f, 0.0755f, 0.0742f, listOf(AnimCapa.Mecer(0.73f, 0.88f, 1.5f, 4.5f), AnimCapa.Latir(0.45f, 1f, 1.8f, 0f))),
        CapaEscena(R.drawable.moneda_volva_ojos, 0.6953f, 0.5404f, 0.0547f, 0.0195f, listOf(AnimCapa.Mecer(0.73f, 0.88f, 1.5f, 4.5f), AnimCapa.Latir(0f, 1f, 3.3f, 0f))),
    )
    CaraMoneda.POR_REVELAR -> listOf(
        CapaEscena(R.drawable.moneda_duelo_fondo, 0f, 0f, 1f, 1f, listOf()),
        CapaEscena(R.drawable.moneda_duelo_gigante, 0.1758f, 0.224f, 0.6484f, 0.776f, listOf(AnimCapa.Flotar(0.012f, 5f, 0f))),
        CapaEscena(R.drawable.moneda_duelo_ojos, 0.2487f, 0.1185f, 0.5026f, 0.5026f, listOf(AnimCapa.Flotar(0.012f, 5f, 0f), AnimCapa.Latir(0.4f, 1f, 2.6f, 0f))),
        CapaEscena(R.drawable.moneda_duelo_carta1, 0.1536f, 0.6602f, 0.1289f, 0.1602f, listOf(AnimCapa.Flotar(0.025f, 3f, 0f), AnimCapa.Mecer(0.22f, 0.74f, 5f, 4f))),
        CapaEscena(R.drawable.moneda_duelo_carta2, 0.724f, 0.6497f, 0.1315f, 0.1602f, listOf(AnimCapa.Flotar(0.025f, 3.4f, 0.5f), AnimCapa.Mecer(0.79f, 0.72f, -5f, 4.4f))),
        CapaEscena(R.drawable.moneda_duelo_suelo, 0f, 0.8464f, 1f, 0.1536f, listOf()),
        CapaEscena(R.drawable.moneda_duelo_antorcha, 0.168f, 0.638f, 0.4036f, 0.362f, listOf(AnimCapa.Latir(0.55f, 1f, 0.7f, 0f))),
        CapaEscena(R.drawable.moneda_duelo_heroe, 0.3372f, 0.7891f, 0.1081f, 0.1445f, listOf()),
        CapaEscena(R.drawable.moneda_duelo_llama, 0.4336f, 0.7617f, 0.0299f, 0.0664f, listOf(AnimCapa.Flama(0.45f, 0.825f, 0.35f))),
        CapaEscena(R.drawable.moneda_duelo_nieve, 0.0521f, 0.0339f, 0.9049f, 0.9036f, listOf(AnimCapa.Caer(9f), AnimCapa.Deriva(0.02f, 4f, 0f))),
    )
}

/** Martillo: 0 = sobre el yunque, 1 = arriba. Sube despacio, espera y cae de golpe en u = 0.76. */
internal fun curvaGolpe(u: Float): Float = when {
    u < 0.55f -> { val v = u / 0.55f; 1f - (1f - v) * (1f - v) }
    u < 0.68f -> 1f
    u < 0.76f -> { val v = (u - 0.68f) / 0.08f; 1f - v * v }
    else -> { val s = u - 0.76f; 0.06f * exp(-s * 30f) * sin(s * 60f) }
}

/** Destello del golpe: aparece justo cuando el martillo cae y se apaga rápido. */
internal fun curvaDestello(u: Float): Float = if (u >= 0.76f) exp(-(u - 0.76f) * 14f) else 0f

/** Las imágenes de las capas, cargadas una vez. */
@Composable
internal fun rememberEscenasMoneda(): Map<CaraMoneda, List<ImageBitmap>> {
    val mapa = LinkedHashMap<CaraMoneda, List<ImageBitmap>>()
    for (cara in CaraMoneda.entries) {
        val capas = capasDe(cara)
        if (capas.isEmpty()) continue
        val imagenes = ArrayList<ImageBitmap>(capas.size)
        for (capa in capas) imagenes.add(ImageBitmap.imageResource(capa.imagen))
        mapa[cara] = imagenes
    }
    return mapa
}

/** Chispas de la escena: de dónde salen (fracciones) y de qué color. */
private fun chispasDe(cara: CaraMoneda): Pair<Offset, Color>? = when (cara) {
    CaraMoneda.APUESTA_BROKKR -> Offset(0.31f, 0.62f) to Color(0xFFFFE6A0)
    CaraMoneda.VOLVA -> Offset(0.48f, 0.8f) to Color(0xFFFFE6A0)
    else -> null
}

/**
 * Cara de un caso de uso con el mismo aro rúnico del escudo de Mundos (mismo
 * grosor, colores y runas), y en el centro el campo y el emblema del caso.
 * Ocupa todo el lienzo (cuadrado).
 */
internal fun DrawScope.dibujarCaraEscudo(
    cara: CaraMoneda,
    runas: List<TextLayoutResult>,
    escena: List<ImageBitmap>? = null,
    t: Float = 0f
) {
    val c = center
    val r = size.minDimension / 2f
    val a = size.minDimension * 0.08f
    // Aro rúnico, igual que AnilloRunico de MundosScreen
    drawCircle(Color(0xFF0F0D0A), r, c)
    drawCircle(
        Brush.linearGradient(
            listOf(Color(0xFF4A4034), Color(0xFF2E2720), Color(0xFF3E352B)),
            start = Offset.Zero, end = Offset(size.width, size.height)
        ),
        r - a / 2f, c, style = Stroke(a)
    )
    drawCircle(OdinPaleta.Oro.copy(alpha = .6f), r - .75.dp.toPx(), c, style = Stroke(1.5.dp.toPx()))
    drawCircle(OdinPaleta.Oro.copy(alpha = .4f), r - a, c, style = Stroke(1.dp.toPx()))
    if (runas.isNotEmpty()) {
        val n = 30
        for (i in 0 until n) {
            val runa = runas[i % runas.size]
            rotate(360f / n * i, pivot = c) {
                drawText(
                    runa,
                    color = cara.acento.copy(alpha = .85f),
                    topLeft = Offset(c.x - runa.size.width / 2f, c.y - r + a / 2f - runa.size.height / 2f)
                )
            }
        }
    }
    val ri = r - a - 4.dp.toPx()
    val capas = capasDe(cara)
    if (escena != null && escena.size == capas.size && capas.isNotEmpty()) {
        dibujarEscenaViva(c, ri, capas, escena, cara, t)
        return
    }
    // Campo del caso de uso (sin ilustración)
    drawCircle(
        Brush.radialGradient(listOf(cara.campoClaro, cara.campoOscuro), center = c + Offset(0f, -ri * 0.2f), radius = ri * 1.05f),
        ri, c
    )
    drawCircle(cara.metal.copy(alpha = .7f), ri, c, style = Stroke(1.5.dp.toPx()))
    drawCircle(cara.metal.copy(alpha = .25f), ri * 0.93f, c, style = Stroke(1.dp.toPx()))
    mancha(c, ri * 0.85f, cara.acento, .18f)
    // Emblema en relieve
    val s = ri * 0.58f
    dibujarEmblemaCara(cara, c + Offset(s * .04f, s * .06f), s, Color.Black.copy(alpha = .55f), Color.Black.copy(alpha = .55f))
    dibujarEmblemaCara(cara, c, s, cara.metalClaro, cara.campoOscuro)
}

/**
 * Ventana con la escena del modo dentro del aro rúnico: cada capa se mueve por
 * su cuenta (fuego, martillo, aurora, nieve...), salen chispas si hay fuego y
 * cada pocos segundos le cruza un brillo, como vidrio.
 */
private fun DrawScope.dibujarEscenaViva(
    c: Offset, ri: Float, capas: List<CapaEscena>, imagenes: List<ImageBitmap>, cara: CaraMoneda, t: Float
) {
    val lado = 2f * ri
    val origen = Offset(c.x - ri, c.y - ri)
    val ventana = Path().apply { addOval(Rect(c, ri)) }
    clipPath(ventana) {
        capas.forEachIndexed { i, capa -> dibujarCapa(capa, imagenes[i], origen, lado, t) }
        chispasDe(cara)?.let { (punto, color) ->
            val luz = origen + Offset(punto.x * lado, punto.y * lado)
            for (k in 0 until 9) {
                val fase = (t * 0.35f + k / 9f) % 1f
                val x = luz.x + ri * 0.16f * sin(k * 2.1f + t * 1.3f) * (0.3f + fase)
                val y = luz.y - ri * 0.7f * fase
                drawCircle(color.copy(alpha = (1f - fase) * 0.9f), ri * 0.013f * (1.4f - fase), Offset(x, y))
            }
        }
        // Brillo que cruza la cara cada 5.5 s
        val ciclo = (t % 5.5f) / 1.3f
        if (ciclo < 1f) {
            val d = Offset(0.7071f, 0.7071f)
            val p = c + d * ((-1.4f + 2.8f * ciclo) * ri)
            val w = ri * 0.28f
            drawCircle(
                Brush.linearGradient(
                    0f to Color.Transparent, 0.5f to Color.White.copy(alpha = 0.28f), 1f to Color.Transparent,
                    start = p - d * w, end = p + d * w
                ),
                ri, c
            )
        }
        // Viñeta para que la escena se hunda en el aro
        drawCircle(Brush.radialGradient(0.62f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f), center = c, radius = ri), ri, c)
    }
    // Bisel de metal
    drawCircle(
        Brush.linearGradient(
            listOf(cara.metalClaro, cara.metal, cara.metalOscuro, cara.metal, cara.metalClaro),
            start = Offset(c.x - ri, c.y - ri), end = Offset(c.x + ri, c.y + ri)
        ),
        ri, c, style = Stroke(ri * 0.045f)
    )
    drawCircle(Color.Black.copy(alpha = 0.5f), ri * 0.975f, c, style = Stroke(1.dp.toPx()))
}

/** Dibuja una capa con sus animaciones en el instante [t]. */
private fun DrawScope.dibujarCapa(capa: CapaEscena, imagen: ImageBitmap, origen: Offset, lado: Float, t: Float) {
    val tau = 2f * PI.toFloat()
    var dx = 0f
    var dy = 0f
    var giro = 0f
    var pivoteGiro = Offset.Zero
    var sx = 1f
    var sy = 1f
    var pivoteEscala = Offset.Zero
    var alfa = 1f
    var repetirArriba = false
    for (a in capa.anims) when (a) {
        is AnimCapa.Deriva -> dx += a.dx * lado * sin(tau * (t / a.periodo + a.fase))
        is AnimCapa.Flotar -> dy += a.dy * lado * sin(tau * (t / a.periodo + a.fase))
        is AnimCapa.Latir -> alfa *= a.min + (a.max - a.min) * (0.5f + 0.5f * sin(tau * (t / a.periodo + a.fase)))
        is AnimCapa.Girar -> { giro += a.gradosPorSegundo * t; pivoteGiro = Offset(a.px, a.py) }
        is AnimCapa.Mecer -> { giro += a.grados * sin(tau * t / a.periodo); pivoteGiro = Offset(a.px, a.py) }
        is AnimCapa.Golpe -> { giro += a.grados * curvaGolpe((t / a.periodo) % 1f); pivoteGiro = Offset(a.px, a.py) }
        is AnimCapa.Destello -> alfa *= curvaDestello((t / a.periodo) % 1f)
        is AnimCapa.Flama -> {
            val w = tau * t / a.periodo
            sy = 1f + 0.09f * sin(w) + 0.04f * sin(w * 2.7f)
            sx = 1f - 0.05f * sin(w * 1.3f)
            pivoteEscala = Offset(a.px, a.py)
        }
        is AnimCapa.Vuelo -> dx += (-0.75f + 1.5f * ((t / a.periodo) % 1f)) * lado
        is AnimCapa.Caer -> { dy += ((t / a.periodo) % 1f) * lado; repetirArriba = true }
    }
    if (alfa <= 0.01f) return
    val destino = IntOffset((origen.x + capa.x * lado).roundToInt(), (origen.y + capa.y * lado).roundToInt())
    val tam = IntSize((capa.ancho * lado).roundToInt(), (capa.alto * lado).roundToInt())
    for (copia in 0 until if (repetirArriba) 2 else 1) {
        val extra = -lado * copia
        withTransform({
            translate(dx, dy + extra)
            if (giro != 0f) rotate(giro, origen + Offset(pivoteGiro.x * lado, pivoteGiro.y * lado))
            if (sx != 1f || sy != 1f) scale(sx, sy, origen + Offset(pivoteEscala.x * lado, pivoteEscala.y * lado))
        }) {
            drawImage(imagen, dstOffset = destino, dstSize = tam, alpha = alfa.coerceIn(0f, 1f), filterQuality = FilterQuality.High)
        }
    }
}

/**
 * Canto del escudo cuando se pone de lado al girar (se dibuja detrás de la
 * cara, sin la rotación). [desplazamiento] es cuánto se corre la cara visible
 * hacia arriba o abajo por el grosor; la cara se dibuja con ese mismo corrimiento.
 */
internal fun DrawScope.dibujarCantoEscudo(anguloGrados: Float, escala: Float, alturaPx: Float) {
    val rad = anguloGrados / 180f * PI.toFloat()
    val cs = cos(rad)
    val r = size.minDimension / 2f * escala
    val radioY = r * abs(cs).coerceAtLeast(0.02f)
    val d = desplazamientoCanto(anguloGrados, r)
    if (abs(d) * 2f < 0.5f) return
    val c = center + Offset(0f, alturaPx)
    val oculto = c - Offset(0f, d)
    val visible = c + Offset(0f, d)
    val brocha = Brush.horizontalGradient(
        // Canto dorado (como la moneda del ícono): oscuro, se leía como un parpadeo negro al girar
        listOf(Color(0xFF6E5427), OdinPaleta.Oro, Color(0xFFF3D894), OdinPaleta.Oro, Color(0xFF6E5427)),
        startX = c.x - r, endX = c.x + r
    )
    drawOval(brocha, Offset(oculto.x - r, oculto.y - radioY), Size(r * 2f, radioY * 2f))
    drawRect(brocha, Offset(c.x - r, min(oculto.y, visible.y)), Size(r * 2f, abs(visible.y - oculto.y)))
    val lado = if (d > 0f) -1f else 1f
    val n = 40
    for (i in 1 until n) {
        val phi = PI.toFloat() * i / n
        val x = c.x - r * cos(phi)
        val e = lado * radioY * sin(phi)
        drawLine(Color(0xFF4A3A1C).copy(alpha = .5f * sin(phi)), Offset(x, oculto.y + e), Offset(x, visible.y + e), r * 0.008f)
    }
}

/** Corrimiento vertical de la cara visible por el grosor del escudo. */
internal fun desplazamientoCanto(anguloGrados: Float, radio: Float): Float {
    val rad = anguloGrados / 180f * PI.toFloat()
    val grosor = radio * 0.08f
    return (if (cos(rad) >= 0f) 1f else -1f) * grosor / 2f * sin(rad)
}

/**
 * Rotación que se le aplica a la cara visible: el frente usa el ángulo tal
 * cual y el reverso el ángulo menos 180°, así siempre se ve derecho.
 * Queda entre -90° y 90°.
 */
internal fun rotacionCaraVisible(anguloGrados: Float): Float {
    val rad = anguloGrados / 180f * PI.toFloat()
    val cs = cos(rad)
    val sn = sin(rad)
    return (kotlin.math.atan2(if (cs >= 0f) sn else -sn, abs(cs)) * 180f / PI.toFloat())
}

/** Sombra en el suelo mientras el escudo vuela; más chica y tenue cuanto más alto va. */
internal fun DrawScope.dibujarSombraMoneda(centroSuelo: Offset, radio: Float, cercania: Float) {
    val k = cercania.coerceIn(0.25f, 1f)
    val ancho = radio * 1.5f * k
    val alto = radio * 0.2f * k
    withTransform({ scale(ancho / alto, 1f, pivot = centroSuelo) }) {
        mancha(centroSuelo, alto, Color.Black, 0.6f * k)
    }
}

/** Chispas que salen al caer ([t] = segundos desde que tocó el suelo). */
internal fun DrawScope.dibujarChispas(origen: Offset, radio: Float, t: Float, color: Color, semilla: Int) {
    if (t < 0f || t > 0.75f) return
    val rnd = Random(semilla)
    val vida = t / 0.75f
    repeat(18) {
        val ang = PI.toFloat() * (1f + rnd.nextFloat())
        val vel = radio * (0.6f + rnd.nextFloat() * 0.9f)
        val d = vel * sqrt(vida)
        val p = origen + Offset(cos(ang) * d * 1.3f, sin(ang) * d * 0.7f + radio * 0.5f * vida * vida)
        val a = (1f - vida) * (0.5f + rnd.nextFloat() * 0.5f)
        mancha(p, radio * 0.06f, color, a * 0.8f)
        drawCircle(Color(0xFFFFF3D0).copy(alpha = a), radio * (0.012f + rnd.nextFloat() * 0.012f), p)
    }
    drawOval(
        color.copy(alpha = 0.5f * (1f - vida)),
        Offset(origen.x - radio * (0.6f + vida), origen.y - radio * 0.12f * (0.6f + vida)),
        Size(radio * 2f * (0.6f + vida), radio * 0.24f * (0.6f + vida)),
        style = Stroke(radio * 0.02f)
    )
}

// ---------------------------------------------------------------------------
// Emblemas
// ---------------------------------------------------------------------------

/** Emblema de cada cara. [fondo] separa piezas que se encimarían (espadas sobre el sol, etc.). */
internal fun DrawScope.dibujarEmblemaCara(cara: CaraMoneda, c: Offset, s: Float, color: Color, fondo: Color) {
    val w = s * 0.09f
    fun p(x: Float, y: Float) = Offset(c.x + x * s, c.y + y * s)
    fun st(f: Float) = Stroke(width = w * f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (cara) {
        CaraMoneda.MUNDOS -> {
            // El escudo de los mundos en miniatura con el Valknut al centro
            drawCircle(color, s * 0.95f, c, style = st(0.8f))
            drawCircle(color, s * 0.38f, c, style = st(0.8f))
            drawLine(color, p(-0.95f, 0f), p(-0.38f, 0f), w * 0.8f)
            drawLine(color, p(0.38f, 0f), p(0.95f, 0f), w * 0.8f)
            drawLine(color, p(0f, -0.95f), p(0f, -0.38f), w * 0.8f)
            drawLine(color, p(0f, 0.38f), p(0f, 0.95f), w * 0.8f)
            dibujarValknut(c, s * 0.36f, color, w * 0.55f)
            val emblemas = listOf(Emblema.VEGVISIR, Emblema.OJO, Emblema.YGGDRASIL, Emblema.MJOLNIR)
            listOf(-135f, -45f, 135f, 45f).forEachIndexed { i, grados ->
                val a = grados / 180f * PI.toFloat()
                dibujarEmblema(emblemas[i], c + Offset(cos(a), sin(a)) * (s * 0.66f), s * 0.19f, color)
            }
        }

        CaraMoneda.HASTA_EL_ALBA -> {
            // Sol saliendo en el horizonte y dos espadas cruzadas: la batalla sin fin hasta el alba
            val y0 = 0.3f
            drawArc(color, 180f, 180f, true, p(-0.45f, y0 - 0.45f), Size(0.9f * s, 0.9f * s))
            for (k in 0 until 7) {
                val a = (195f + k * 25f) / 180f * PI.toFloat()
                val base = p(0f, y0)
                drawLine(color, base + Offset(cos(a), sin(a)) * (s * 0.58f), base + Offset(cos(a), sin(a)) * (s * 0.86f), w * 0.7f, StrokeCap.Round)
            }
            drawLine(color, p(-0.95f, y0), p(0.95f, y0), w, StrokeCap.Round)
            espada(p(-0.62f, 0.8f), p(0.58f, -0.66f), s, w, color, fondo)
            espada(p(0.62f, 0.8f), p(-0.58f, -0.66f), s, w, color, fondo)
        }

        CaraMoneda.APUESTA_BROKKR -> {
            // Yunque, martillo en pleno golpe y chispas
            val yunque = Path().apply {
                fun m(x: Float, y: Float) = moveTo(c.x + x * s, c.y + y * s)
                fun l(x: Float, y: Float) = lineTo(c.x + x * s, c.y + y * s)
                fun q(x1: Float, y1: Float, x2: Float, y2: Float) = quadraticTo(c.x + x1 * s, c.y + y1 * s, c.x + x2 * s, c.y + y2 * s)
                m(-0.95f, 0.04f); q(-0.6f, -0.04f, -0.35f, -0.04f); l(0.8f, -0.04f); l(0.8f, 0.16f); l(0.38f, 0.2f)
                q(0.24f, 0.32f, 0.26f, 0.48f); l(0.55f, 0.6f); l(0.55f, 0.72f); l(-0.45f, 0.72f); l(-0.45f, 0.6f)
                l(-0.18f, 0.48f); q(-0.16f, 0.32f, -0.3f, 0.2f); q(-0.6f, 0.16f, -0.95f, 0.04f); close()
            }
            drawPath(yunque, color)
            drawLine(fondo, p(-0.3f, 0.06f), p(0.72f, 0.06f), w * 0.35f)
            val cabeza = p(0.22f, -0.3f)
            rotate(-25f, pivot = cabeza) {
                drawLine(color, cabeza + Offset(0f, -0.1f * s), cabeza + Offset(0f, -0.72f * s), w * 1.1f, StrokeCap.Round)
                val rect = Rect(cabeza.x - 0.3f * s, cabeza.y - 0.13f * s, cabeza.x + 0.3f * s, cabeza.y + 0.13f * s)
                drawRect(fondo, rect.topLeft - Offset(w * 0.5f, w * 0.5f), Size(rect.width + w, rect.height + w))
                drawRect(color, rect.topLeft, rect.size)
                drawLine(fondo, Offset(rect.left + 0.12f * s, rect.top), Offset(rect.left + 0.12f * s, rect.bottom), w * 0.35f)
                drawLine(fondo, Offset(rect.right - 0.12f * s, rect.top), Offset(rect.right - 0.12f * s, rect.bottom), w * 0.35f)
            }
            val golpe = p(0.12f, -0.08f)
            listOf(-170f, -140f, -112f, -48f, -15f).forEachIndexed { i, grados ->
                val a = grados / 180f * PI.toFloat()
                val d = Offset(cos(a), sin(a))
                drawLine(color, golpe + d * (s * 0.14f), golpe + d * (s * (0.3f + 0.06f * (i % 2))), w * 0.5f, StrokeCap.Round)
            }
        }

        CaraMoneda.VOLVA -> {
            // Luna creciente, báculo del seiðr, estrellas y tres runas echadas
            val luna = Path.combine(
                PathOperation.Difference,
                Path().apply { addOval(Rect(p(-0.18f, -0.12f), 0.55f * s)) },
                Path().apply { addOval(Rect(p(0.06f, -0.3f), 0.47f * s)) }
            )
            drawPath(luna, color)
            drawLine(color, p(0.5f, -0.64f), p(0.5f, 0.92f), w, StrokeCap.Round)
            drawCircle(color, 0.13f * s, p(0.5f, -0.78f), style = st(0.8f))
            drawLine(color, p(0.4f, -0.55f), p(0.6f, -0.55f), w * 0.6f, StrokeCap.Round)
            drawLine(color, p(0.42f, -0.45f), p(0.58f, -0.45f), w * 0.6f, StrokeCap.Round)
            destello(p(0.12f, -0.78f), 0.1f * s, color)
            destello(p(-0.76f, 0.38f), 0.07f * s, color)
            destello(p(0.16f, 0.32f), 0.08f * s, color)
            listOf(-0.62f, -0.3f, 0.02f).forEachIndexed { i, x ->
                val tl = p(x, 0.56f)
                drawRoundRect(color, tl, Size(0.24f * s, 0.3f * s), androidx.compose.ui.geometry.CornerRadius(0.07f * s))
                val m = tl + Offset(0.12f * s, 0.06f * s)
                drawLine(fondo, m, m + Offset(0f, 0.18f * s), w * 0.4f, StrokeCap.Round)
                val dx = if (i == 1) -0.07f else 0.07f
                drawLine(fondo, m + Offset(0f, 0.04f * s), m + Offset(dx * s, 0.1f * s), w * 0.4f, StrokeCap.Round)
            }
        }

        CaraMoneda.POR_REVELAR -> {
            // Perthro: la runa del azar y de lo que aún no se revela
            val runa = Path().apply {
                moveTo(p(-0.32f, -0.8f).x, p(-0.32f, -0.8f).y); lineTo(p(-0.32f, 0.8f).x, p(-0.32f, 0.8f).y)
                moveTo(p(-0.32f, -0.8f).x, p(-0.32f, -0.8f).y); lineTo(p(0.12f, -0.36f).x, p(0.12f, -0.36f).y); lineTo(p(0.42f, -0.78f).x, p(0.42f, -0.78f).y)
                moveTo(p(-0.32f, 0.8f).x, p(-0.32f, 0.8f).y); lineTo(p(0.12f, 0.36f).x, p(0.12f, 0.36f).y); lineTo(p(0.42f, 0.78f).x, p(0.42f, 0.78f).y)
            }
            drawPath(runa, color, style = st(1.3f))
            for (i in 0 until 16) {
                val a = 2f * PI.toFloat() * i / 16
                drawCircle(color, w * 0.35f, c + Offset(cos(a), sin(a)) * (s * 1.2f))
            }
        }
    }
}

private fun DrawScope.espada(empunadura: Offset, punta: Offset, s: Float, w: Float, color: Color, fondo: Color) {
    val v = punta - empunadura
    val largo = v.getDistance()
    val dir = v / largo
    val perp = Offset(-dir.y, dir.x)
    val guarda = empunadura + dir * (largo * 0.24f)
    val hoja = Path().apply {
        val a = guarda + perp * (w * 0.75f)
        val b = guarda - perp * (w * 0.75f)
        val antes = punta - dir * (w * 2.2f)
        moveTo(a.x, a.y)
        lineTo(antes.x + perp.x * w * 0.75f, antes.y + perp.y * w * 0.75f)
        lineTo(punta.x, punta.y)
        lineTo(antes.x - perp.x * w * 0.75f, antes.y - perp.y * w * 0.75f)
        lineTo(b.x, b.y)
        close()
    }
    drawPath(hoja, fondo, style = Stroke(w * 0.9f, join = StrokeJoin.Round))
    drawPath(hoja, color)
    drawLine(fondo, guarda + dir * (w * 0.6f), punta - dir * (w * 3f), w * 0.3f, StrokeCap.Round)
    drawLine(fondo, guarda + perp * (s * 0.24f), guarda - perp * (s * 0.24f), w * 1.8f, StrokeCap.Round)
    drawLine(color, guarda + perp * (s * 0.22f), guarda - perp * (s * 0.22f), w, StrokeCap.Round)
    drawLine(color, empunadura, guarda, w * 0.9f, StrokeCap.Round)
    drawCircle(color, w * 0.9f, empunadura)
}

/** Estrella de cuatro puntas. */
private fun DrawScope.destello(c: Offset, k: Float, color: Color) {
    val path = Path().apply {
        moveTo(c.x, c.y - k)
        quadraticTo(c.x, c.y, c.x + k, c.y)
        quadraticTo(c.x, c.y, c.x, c.y + k)
        quadraticTo(c.x, c.y, c.x - k, c.y)
        quadraticTo(c.x, c.y, c.x, c.y - k)
        close()
    }
    drawPath(path, color)
}
