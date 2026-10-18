package com.aesir.odin.ui.flashcards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.R
import com.aesir.odin.domain.usecase.crucigrama.normalizarPalabra
import com.aesir.odin.ui.components.odin.FuentesMockup
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.theme.Emblema
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

// Piezas del salón de flashcards, copiadas del mockup "Flashcards de taberna":
// tema visual de cada mundo, madera con textura (res/drawable-nodpi/madera_veta_*),
// formas de carta y tablilla, emblemas, montones y adornos de la mesa.
//
// Todas las medidas están en dp del lienzo de diseño de 390 de ancho (el
// área bajo la barra de navegación mide 390 × 774), igual que en el mockup.

// ---------------------------------------------------------------------------
// Tema de cada salón
// ---------------------------------------------------------------------------

enum class EmblemaSalon { VALKNUT, OJO, YGGDRASIL, MARTILLO }

enum class ClimaSalon { NINGUNO, NIEVE, HOJAS, BRASAS }

/** Colores, emblema y adornos de la taberna de cada mundo. */
data class SalonMundo(
    val lugar: String,
    val titulo: Color,
    val punto: Color,
    val acento: Color,
    val mesa: Color,
    val carta: Color,
    val tinta: Color,
    val tintaSuave: Color,
    val runaPlaca: Color,
    val monton: Color,
    val tintaMonton: Color,
    val emblema: EmblemaSalon,
    val gema: Color,
    val anillo: Color,
    val interiorEscudo: Color,
    val dado: Color,
    val tintaDado: Color,
    val brillo: Color,
    val clima: ClimaSalon
) {
    companion object {
        fun porOrden(orden: Int): SalonMundo = when ((orden - 1).coerceAtLeast(0) % 4) {
            1 -> REQUERIMIENTOS
            2 -> DISENO
            3 -> PRUEBAS
            else -> FUNDAMENTOS
        }

        val FUNDAMENTOS = SalonMundo(
            "SALÓN DE HIDROMIEL · MIDGARD", Color(0xFFE7BE7A), Color(0xFF3FB27A), Color(0xFFF0B85A),
            Color(0xFF8A6240), Color(0xFFE4BC86), Color(0xFF3A1A05), Color(0xFF6B4524), Color(0xFF4E2F14),
            Color(0xFF7A4C2A), Color(0xFF2A1608), EmblemaSalon.VALKNUT, Color(0xFF3FBF85),
            Color(0xFF9A6838), Color(0xFF2E1A0C), Color(0xFFEDE5D2), Color(0xFF3A2A1A), Color(0xFFFFF1C8), ClimaSalon.NINGUNO
        )
        val REQUERIMIENTOS = SalonMundo(
            "TABERNA HELADA · NIFLHEIM", Color(0xFFAAE2E4), Color(0xFF7FD0E3), Color(0xFF7FE0EA),
            Color(0xFF465F6E), Color(0xFFEAE9DF), Color(0xFF112220), Color(0xFF3B5A5E), Color(0xFF243C40),
            Color(0xFF9EA2A0), Color(0xFF2E3A3C), EmblemaSalon.OJO, Color(0xFF7FD3EC),
            Color(0xFF6B777C), Color(0xFF1C2A32), Color(0xFFDCE8EE), Color(0xFF2E3A40), Color(0xFFDFF6FF), ClimaSalon.NIEVE
        )
        val DISENO = SalonMundo(
            "SALÓN BAJO EL YGGDRASIL", Color(0xFF8DE1C0), Color(0xFF4FC883), Color(0xFF6FE0A6),
            Color(0xFF325456), Color(0xFFD4D8B6), Color(0xFF142810), Color(0xFF3E5A36), Color(0xFF233C20),
            Color(0xFF56664C), Color(0xFF1A2616), EmblemaSalon.YGGDRASIL, Color(0xFF4CC67F),
            Color(0xFF5E6C66), Color(0xFF16201C), Color(0xFFD8E0CC), Color(0xFF2A3424), Color(0xFFDFFFF0), ClimaSalon.HOJAS
        )
        val PRUEBAS = SalonMundo(
            "LA FORJA · MUSPELHEIM", Color(0xFFE89F6E), Color(0xFFF59A4E), Color(0xFFFF8A3A),
            Color(0xFF573423), Color(0xFF9A9086), Color(0xFF120600), Color(0xFFA04A1C), Color(0xFFE07A3A),
            Color(0xFF433D3A), Color(0xFFE0702A), EmblemaSalon.MARTILLO, Color(0xFFF08A30),
            Color(0xFF57524F), Color(0xFF4A1608), Color(0xFF4A4542), Color(0xFF8A8480), Color(0xFFFFE0B8), ClimaSalon.BRASAS
        )
    }
}

/** Colores comunes del salón. */
object ColoresSalon {
    val Crema = Color(0xFFEDE3CF)
    val Negro = Color(0xFF0D0A08)
    val Verde = Color(0xFF4CC38A)
    val VerdeIcono = Color(0xFF2F9E62)
    val Rojo = Color(0xFFD8432E)
    val RojoIcono = Color(0xFFCF3A28)
    val Salmon = Color(0xFFD98A62)
    val Fallada = Color(0xFFC8402F)
    val Hueco = Color(0xFF150B05)
}

/** Multiplica el color por [f] (f < 1 lo oscurece). */
fun Color.escalado(f: Float): Color =
    Color((red * f).coerceIn(0f, 1f), (green * f).coerceIn(0f, 1f), (blue * f).coerceIn(0f, 1f), alpha)

/** Mezcla lineal con otro color (t = 0 → este, t = 1 → otro). */
fun Color.mezclado(otro: Color, t: Float): Color = lerp(this, otro, t)

// ---------------------------------------------------------------------------
// Madera
// ---------------------------------------------------------------------------

/**
 * Madera: un color base multiplicado por una textura de veta en grises.
 * [contraste] repite la textura (vetas más oscuras); [semilla] elige qué
 * parte de la textura se ve, para que dos piezas no luzcan iguales.
 */
@Composable
fun Madera(
    tinte: Color,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
    variante: Int = 0,
    semilla: Int = 0,
    contraste: Int = 1,
    escala: Float = 1f,
    ambiente: Boolean = true
) {
    val id = when {
        variante % 2 == 0 && horizontal -> R.drawable.madera_veta_ah
        variante % 2 == 0 -> R.drawable.madera_veta_a
        horizontal -> R.drawable.madera_veta_bh
        else -> R.drawable.madera_veta_b
    }
    val textura = ImageBitmap.imageResource(id)
    Canvas(modifier) { dibujarMadera(textura, tinte, horizontal, semilla, contraste, escala, ambiente) }
}

/**
 * Pinta la madera en todo el DrawScope (para usarla dentro de otros dibujos).
 * [ambiente] la integra con la luz de la taberna: luz cálida arriba, sombra
 * abajo y viñeta en los bordes (la mesa ya trae la suya).
 */
fun DrawScope.dibujarMadera(
    textura: ImageBitmap,
    tinte: Color,
    horizontal: Boolean,
    semilla: Int,
    contraste: Int,
    escala: Float = 1f,
    ambiente: Boolean = true
) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return
    val tw: Float
    val th: Float
    if (horizontal) {
        th = max(h, w * 0.5f) * 1.25f * escala
        tw = th * 2f
    } else {
        tw = max(w, h * 0.5f) * 1.25f * escala
        th = tw * 2f
    }
    val rnd = Random(semilla * 7919 + 17)
    val ox = (tw - w) * rnd.nextFloat()
    val oy = (th - h) * rnd.nextFloat()
    val sx = textura.width / tw
    val sy = textura.height / th
    drawRect(tinte)
    val srcOffset = IntOffset((ox * sx).toInt(), (oy * sy).toInt())
    val srcSize = IntSize(
        (w * sx).toInt().coerceIn(1, textura.width - srcOffset.x),
        (h * sy).toInt().coerceIn(1, textura.height - srcOffset.y)
    )
    repeat(contraste.coerceAtLeast(1)) {
        drawImage(
            textura, srcOffset = srcOffset, srcSize = srcSize,
            dstOffset = IntOffset.Zero, dstSize = IntSize(w.toInt(), h.toInt()),
            blendMode = BlendMode.Multiply
        )
    }
    if (ambiente) {
        drawRect(
            Brush.verticalGradient(
                0f to Color(1f, 0.85f, 0.6f, 0.10f),
                0.45f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.16f)
            )
        )
        drawRect(
            Brush.radialGradient(
                0.35f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.34f),
                center = Offset(w * 0.5f, h * 0.35f),
                radius = max(w, h) * 0.75f
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Formas
// ---------------------------------------------------------------------------

/** Carta tallada del mockup: cuerpo recto, hombros con chaflán y cúpula arriba. */
fun rutaCarta(r: Rect): Path {
    val w = r.width
    val h = r.height
    val hombro = h * 0.182f
    val chaflan = w * 0.032f
    val repisa = w * 0.131f
    val yRepisa = hombro - chaflan
    val rb = min(w, h) * 0.014f
    return Path().apply {
        moveTo(0f, h - rb)
        lineTo(0f, hombro)
        lineTo(chaflan, yRepisa)
        lineTo(repisa, yRepisa)
        cubicTo(repisa + w * 0.07f, yRepisa - h * 0.01f, w * 0.17f, 0f, w / 2f, 0f)
        cubicTo(w * 0.83f, 0f, w - repisa - w * 0.07f, yRepisa - h * 0.01f, w - repisa, yRepisa)
        lineTo(w - chaflan, yRepisa)
        lineTo(w, hombro)
        lineTo(w, h - rb)
        quadraticTo(w, h, w - rb, h)
        lineTo(rb, h)
        quadraticTo(0f, h, 0f, h - rb)
        close()
        translate(Offset(r.left, r.top))
    }
}

/** Tablilla con puntas a los lados (opciones, botones). */
fun rutaTablilla(r: Rect, puntaMax: Float): Path {
    val p = min(r.height * 0.26f, puntaMax)
    return Path().apply {
        moveTo(r.left + p, r.top)
        lineTo(r.right - p, r.top)
        lineTo(r.right, r.center.y)
        lineTo(r.right - p, r.bottom)
        lineTo(r.left + p, r.bottom)
        lineTo(r.left, r.center.y)
        close()
    }
}

class FormaCarta(private val margen: Float = 0f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val m = margen * density.density
        return Outline.Generic(rutaCarta(Rect(m, m, size.width - m, size.height - m)))
    }
}

class FormaTablilla(private val margen: Float = 0f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val m = margen * density.density
        return Outline.Generic(rutaTablilla(Rect(m, m, size.width - m, size.height - m), 14f * density.density))
    }
}

// ---------------------------------------------------------------------------
// Emblemas
// ---------------------------------------------------------------------------

/** Emblema del salón dentro de [rect] (como en las cartas del mockup). */
fun DrawScope.emblemaSalon(emblema: EmblemaSalon, rect: Rect, color: Color, grosor: Float) {
    val estilo = Stroke(grosor, cap = StrokeCap.Round)
    when (emblema) {
        EmblemaSalon.VALKNUT -> {
            // Tres triángulos con la punta hacia abajo: uno arriba y dos entrelazados abajo.
            val ancho = rect.width / 1.46f
            val alto = rect.height / 1.42f
            for ((dx, dy) in listOf(0f to 0f, -0.23f * ancho to 0.42f * alto, 0.23f * ancho to 0.42f * alto)) {
                val cx = rect.center.x + dx
                val top = rect.top + dy
                val tri = Path().apply {
                    moveTo(cx - ancho / 2f, top); lineTo(cx + ancho / 2f, top); lineTo(cx, top + alto); close()
                }
                drawPath(tri, color, style = Stroke(grosor))
            }
        }
        EmblemaSalon.OJO -> {
            val w = rect.width
            val h = rect.height
            val almendra = Path().apply {
                moveTo(rect.left, rect.center.y)
                quadraticTo(rect.center.x, rect.top - h * 0.25f, rect.right, rect.center.y)
                quadraticTo(rect.center.x, rect.bottom + h * 0.25f, rect.left, rect.center.y)
                close()
            }
            drawPath(almendra, color, style = estilo)
            val parpado = Path().apply {
                moveTo(rect.left + w * 0.12f, rect.center.y - h * 0.2f)
                quadraticTo(rect.center.x, rect.top - h * 0.12f, rect.right - w * 0.12f, rect.center.y - h * 0.2f)
            }
            drawPath(parpado, color, style = estilo)
            val c = Offset(rect.center.x, rect.center.y + h * 0.04f)
            drawCircle(color, h * 0.3f, c, style = estilo)
            drawCircle(color, h * 0.13f, c)
        }
        EmblemaSalon.YGGDRASIL -> {
            val r = min(rect.width, rect.height) / 2f
            dibujarEmblema(Emblema.YGGDRASIL, rect.center, r / 0.88f, color)
        }
        EmblemaSalon.MARTILLO -> {
            val w = rect.width
            val h = rect.height
            val cabeza = Path().apply {
                moveTo(rect.center.x - w * 0.5f, rect.top)
                lineTo(rect.center.x + w * 0.5f, rect.top)
                lineTo(rect.center.x + w * 0.4f, rect.top + h * 0.3f)
                lineTo(rect.center.x - w * 0.4f, rect.top + h * 0.3f)
                close()
            }
            drawPath(cabeza, color, style = estilo)
            drawRect(color, Offset(rect.center.x - w * 0.09f, rect.top + h * 0.3f), Size(w * 0.18f, h * 0.56f), style = estilo)
            drawCircle(color, w * 0.08f, Offset(rect.center.x, rect.bottom - h * 0.07f), style = estilo)
        }
    }
}

/** Emblema del salón como vista. */
@Composable
fun EmblemaVista(emblema: EmblemaSalon, color: Color, modifier: Modifier = Modifier, grosor: Float = 2.2f, giro: Float = 0f) {
    Canvas(modifier) {
        val g = grosor * density
        rotate(giro) {
            emblemaSalon(emblema, Rect(g, g, size.width - g, size.height - g), color, g)
        }
    }
}

// ---------------------------------------------------------------------------
// Runas
// ---------------------------------------------------------------------------

/** Transcribe un texto al futhark antiguo, para la placa grabada de la carta. */
fun aRunas(texto: String): String {
    val tabla = mapOf(
        'A' to "ᚨ", 'B' to "ᛒ", 'C' to "ᚲ", 'D' to "ᛞ", 'E' to "ᛖ", 'F' to "ᚠ", 'G' to "ᚷ", 'H' to "ᚺ", 'I' to "ᛁ",
        'J' to "ᛃ", 'K' to "ᚲ", 'L' to "ᛚ", 'M' to "ᛗ", 'N' to "ᚾ", 'Ñ' to "ᚾᛃ", 'O' to "ᛟ", 'P' to "ᛈ", 'Q' to "ᚲ",
        'R' to "ᚱ", 'S' to "ᛊ", 'T' to "ᛏ", 'U' to "ᚢ", 'V' to "ᚹ", 'W' to "ᚹ", 'X' to "ᚲᛊ", 'Y' to "ᛃ", 'Z' to "ᛉ"
    )
    return texto.split(" ").joinToString("") { palabra -> normalizarPalabra(palabra).map { tabla[it] ?: "" }.joinToString("") }
}

// ---------------------------------------------------------------------------
// Piezas de carta
// ---------------------------------------------------------------------------

/** Línea tallada (con rombo opcional al centro). */
@Composable
fun LineaTallada(color: Color, modifier: Modifier = Modifier, rombo: Boolean = true) {
    Canvas(modifier) {
        val cy = size.height / 2f
        val d = density
        if (rombo) {
            drawLine(color, Offset(0f, cy), Offset(size.width / 2f - 5f * d, cy), 1.3f * d)
            drawLine(color, Offset(size.width / 2f + 5f * d, cy), Offset(size.width, cy), 1.3f * d)
            val r = Path().apply {
                moveTo(size.width / 2f, cy - 4f * d); lineTo(size.width / 2f + 5f * d, cy)
                lineTo(size.width / 2f, cy + 4f * d); lineTo(size.width / 2f - 5f * d, cy); close()
            }
            drawPath(r, Color(0xFFE9CFA0))
            drawPath(r, color, style = Stroke(1f * d))
        } else {
            drawLine(color, Offset(0f, cy), Offset(size.width, cy), 1.2f * d)
        }
        drawLine(Color.White.copy(alpha = 0.18f), Offset(0f, cy + 1.3f * d), Offset(size.width, cy + 1.3f * d), 0.8f * d)
    }
}

/** Cuerpo de madera de una carta, con bisel y marco interior. */
@Composable
fun CuerpoCarta(
    tinte: Color,
    tinta: Color,
    modifier: Modifier = Modifier,
    semilla: Int = 0,
    contraste: Int = 1,
    variante: Int = 0,
    margen: Float = 8f
) {
    val textura = ImageBitmap.imageResource(if (variante % 2 == 0) R.drawable.madera_veta_a else R.drawable.madera_veta_b)
    Canvas(modifier) {
        val d = density
        val todo = Rect(Offset.Zero, size)
        val forma = rutaCarta(todo)
        withTransform({ clipPath(forma) }) {
            dibujarMadera(textura, tinte, false, semilla, contraste)
            drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent, Color.Black.copy(alpha = 0.14f))))
        }
        drawPath(rutaCarta(todo.deflate(1.6f * d)), Color.White.copy(alpha = 0.28f), style = Stroke(1.2f * d))
        drawPath(forma, tinta.copy(alpha = tinta.alpha * 0.85f), style = Stroke(1.4f * d))
        drawPath(rutaCarta(todo.deflate(margen * d)), tinta.copy(alpha = tinta.alpha * 0.75f), style = Stroke(1.2f * d))
        drawPath(rutaCarta(todo.deflate((margen + 1.3f) * d)), Color.White.copy(alpha = 0.22f), style = Stroke(0.8f * d))
    }
}

/** Dorso de carta (montones y abanico final): madera oscura con el emblema. */
@Composable
fun DorsoCarta(
    salon: SalonMundo,
    ancho: Float,
    alto: Float,
    modifier: Modifier = Modifier,
    semilla: Int = 0,
    conEmblema: Boolean = true,
    tamanoEmblema: Float = 0.42f
) {
    Box(modifier.requiredSize(ancho.dp, alto.dp)) {
        CuerpoCarta(
            salon.monton, Color.Black.copy(alpha = 0.75f), Modifier.fillMaxSize(),
            semilla = semilla, contraste = 3, variante = 1, margen = max(ancho * 0.04f, 3f)
        )
        if (conEmblema) {
            EmblemaVista(
                salon.emblema, salon.tintaMonton,
                Modifier
                    .align(Alignment.Center)
                    .requiredSize((ancho * tamanoEmblema).dp, (ancho * tamanoEmblema * 0.85f).dp),
                grosor = max(ancho * 0.018f, 1.3f)
            )
        }
    }
}

/** Montón de cartas (Mazo, Dominadas) con sus capas. Mide 76 × 108. */
@Composable
fun MontonSalon(salon: SalonMundo, cantidad: Int, semilla: Int, modifier: Modifier = Modifier) {
    val capas = cantidad.coerceIn(1, 4)
    Box(modifier.requiredSize(76.dp, 108.dp).graphicsLayer { alpha = if (cantidad == 0) 0.3f else 1f }) {
        for (k in (capas - 1) downTo 0) {
            DorsoCarta(
                salon, 70f, 100f,
                Modifier.offset(x = (6f - k * 2f).dp, y = (k * 2.6f).dp),
                semilla = semilla + k, conEmblema = k == 0
            )
        }
    }
}

/** Etiqueta negra de un montón ("Mazo 7"). */
@Composable
fun EtiquetaMonton(texto: String, cantidad: Int, acento: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.requiredSize(73.dp, 35.dp).background(ColoresSalon.Negro.copy(alpha = 0.95f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(texto, color = ColoresSalon.Crema, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = FuentesMockup.alegreya(), lineHeight = 14.sp)
        Text("$cantidad", color = acento, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = FuentesMockup.cinzel(), lineHeight = 14.sp)
    }
}

// ---------------------------------------------------------------------------
// Mesa
// ---------------------------------------------------------------------------

/** Mesa de tablones con su textura, viñeta y el clima del mundo (pantalla completa). */
@Composable
fun MesaSalon(salon: SalonMundo, modifier: Modifier = Modifier) {
    val textura = ImageBitmap.imageResource(R.drawable.madera_veta_ah)
    Canvas(modifier.fillMaxSize()) {
        val d = density
        dibujarMadera(textura, salon.mesa, true, 4, 2, 1.1f, ambiente = false)
        var y = 26f * d
        while (y < size.height) {
            drawLine(Color.Black.copy(alpha = 0.5f), Offset(0f, y), Offset(size.width, y), 1.6f * d)
            drawLine(Color.White.copy(alpha = 0.07f), Offset(0f, y + 1.6f * d), Offset(size.width, y + 1.6f * d), 1f * d)
            y += 58f * d
        }
        val c = Offset(size.width / 2f, size.height * 0.42f)
        val r = max(size.width, size.height) * 0.75f
        drawRect(Brush.radialGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f), center = c, radius = r))
        dibujarClima(salon, d)
    }
}

private fun DrawScope.dibujarClima(salon: SalonMundo, d: Float) {
    val rnd = Random(97)
    when (salon.clima) {
        ClimaSalon.NINGUNO -> Unit
        ClimaSalon.NIEVE -> {
            repeat(160) {
                val p = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
                drawCircle(Color.White.copy(alpha = 0.3f + rnd.nextFloat() * 0.5f), (0.6f + rnd.nextFloat() * 1.4f) * d, p)
            }
            repeat(9) {
                val c = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
                val r = (6f + rnd.nextFloat() * 7f) * d
                for (k in 0 until 3) {
                    val a = k * PI.toFloat() / 3f + 0.3f
                    val v = Offset(cos(a) * r, sin(a) * r)
                    drawLine(Color.White.copy(alpha = 0.65f), c - v, c + v, 1f * d, cap = StrokeCap.Round)
                }
            }
        }
        ClimaSalon.HOJAS -> {
            repeat(14) { i ->
                val c = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
                val largo = (9f + rnd.nextFloat() * 7f) * d
                rotate(rnd.nextFloat() * 360f, pivot = c) {
                    val hoja = Path().apply {
                        moveTo(c.x - largo, c.y)
                        quadraticTo(c.x, c.y - largo * 0.55f, c.x + largo, c.y)
                        quadraticTo(c.x, c.y + largo * 0.55f, c.x - largo, c.y)
                    }
                    val color = if (i % 3 == 0) Color(0xFF8BC34A) else Color(0xFF4E9A48)
                    drawPath(hoja, color.copy(alpha = 0.85f))
                    drawLine(Color.Black.copy(alpha = 0.25f), Offset(c.x - largo, c.y), Offset(c.x + largo * 0.8f, c.y), 0.7f * d)
                }
            }
        }
        ClimaSalon.BRASAS -> {
            repeat(9) {
                var p = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
                val grieta = Path().apply { moveTo(p.x, p.y) }
                repeat(2 + rnd.nextInt(3)) {
                    p += Offset((rnd.nextFloat() - 0.5f) * 30f * d, (rnd.nextFloat() - 0.3f) * 26f * d)
                    grieta.lineTo(p.x, p.y)
                }
                drawPath(grieta, Color(0xFFE8601A).copy(alpha = 0.2f), style = Stroke(5f * d, cap = StrokeCap.Round))
                drawPath(grieta, Color(0xFFE8601A).copy(alpha = 0.85f), style = Stroke(1.4f * d, cap = StrokeCap.Round))
                drawPath(grieta, Color(0xFFFFC070).copy(alpha = 0.7f), style = Stroke(0.5f * d, cap = StrokeCap.Round))
            }
            repeat(70) {
                val p = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
                val r = (0.7f + rnd.nextFloat() * 1.5f) * d
                drawCircle(Brush.radialGradient(listOf(Color(0x59FF7A26), Color(0x00FF7A26)), p, r * 3f), r * 3f, p)
                drawCircle(Color(0xFFFFA040), r, p)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Adornos de la mesa
// ---------------------------------------------------------------------------

/** Cuerno, brújula, escudo, dados, gemas y moneda, en dp del área de 390 × 774. */
@Composable
fun AdornosSalon(salon: SalonMundo, modifier: Modifier = Modifier) {
    val medidor = rememberTextMeasurer()
    Canvas(modifier.requiredSize(390.dp, 774.dp)) {
        val d = density
        withTransform({ scale(d, d, pivot = Offset.Zero) }) {
            cuerno()
            brujula(salon, Offset(359.6f, 105.4f))
            escudo(salon, Offset(7f, 713.6f), 79f)
            for ((c, lado, giro, marca) in listOf(
                Quad(Offset(32f, 745f), 25f, -8f, "←"), Quad(Offset(11f, 761f), 26f, 10f, "ᛉ")
            )) {
                rotate(giro, pivot = c) {
                    val cara = Rect(c.x - lado / 2f, c.y - lado / 2f, c.x + lado / 2f, c.y + lado / 2f)
                    drawRoundRect(Color.Black.copy(alpha = 0.4f), cara.topLeft + Offset(1f, 2.5f), cara.size, androidx.compose.ui.geometry.CornerRadius(4f))
                    drawRoundRect(
                        Brush.linearGradient(listOf(salon.dado, salon.dado.escalado(0.85f)), cara.topLeft, cara.bottomRight),
                        cara.topLeft, cara.size, androidx.compose.ui.geometry.CornerRadius(4f)
                    )
                    drawRoundRect(Color.Black.copy(alpha = 0.3f), cara.topLeft, cara.size, androidx.compose.ui.geometry.CornerRadius(4f), style = Stroke(0.8f))
                    val texto = medidor.measure(marca, TextStyle(color = salon.tintaDado, fontSize = 14.sp))
                    // el texto se mide en px de la densidad actual: se dibuja sin escalar
                    withTransform({ scale(1f / d, 1f / d, pivot = c) }) {
                        drawText(texto, topLeft = Offset(c.x - texto.size.width / 2f, c.y - texto.size.height / 2f))
                    }
                }
            }
            moneda(Offset(352.4f, 663f), 16f)
            for (g in listOf(Offset(351.7f, 689.6f), Offset(371.9f, 681f), Offset(339.4f, 711f), Offset(364f, 704.8f))) {
                gema(salon, g, 5.8f)
            }
        }
    }
}

private data class Quad(val c: Offset, val lado: Float, val giro: Float, val marca: String)

/** Cuerno de hidromiel que entra por la izquierda (en dp del área). */
private fun DrawScope.cuerno() {
    val a = Offset(-34f, 96.5f)
    val b = Offset(89f, 69f)
    val largo = (b - a).getDistance()
    val grados = Math.toDegrees(kotlin.math.atan2((b.y - a.y).toDouble(), (b.x - a.x).toDouble())).toFloat()
    fun borde(t: Float) = 4.5f + 12.5f * t.pow(1.6f)
    withTransform({
        translate(a.x, a.y)
        rotate(grados, pivot = Offset.Zero)
    }) {
        val cuerpo = Path().apply {
            moveTo(0f, -borde(0f))
            var t = 0f
            while (t <= 1.0001f) {
                lineTo(largo * t, -borde(t) - sin(t * PI.toFloat()) * 2f)
                t += 0.05f
            }
            t = 1f
            while (t >= -0.0001f) {
                lineTo(largo * t, borde(t))
                t -= 0.05f
            }
            close()
        }
        withTransform({ translate(3f, 6f) }) { drawPath(cuerpo, Color.Black.copy(alpha = 0.35f)) }
        drawPath(
            cuerpo,
            Brush.verticalGradient(listOf(Color(0xFFF2E6C8), Color(0xFFE2D2AC), Color(0xFFB59E74)), -16f, 16f)
        )
        drawPath(cuerpo, Color(0x996E5A3A), style = Stroke(0.8f))
        for (tb in listOf(0.42f, 0.66f, 0.84f)) {
            val x = largo * tb
            val r = borde(tb)
            drawLine(Color(0xFFD9A53A), Offset(x, -r - 1.5f), Offset(x, r), 2.4f)
            drawLine(Color(0xB3F5D27A), Offset(x + 1.2f, -r - 1.5f), Offset(x + 1.2f, r), 0.7f)
        }
        val boca = Rect(largo - 6f, -borde(1f) - 1f, largo + 7f, borde(1f))
        drawOval(Brush.radialGradient(listOf(Color(0xFF2A1808), Color(0xFF5A3A18)), boca.center, boca.height / 2f), boca.topLeft, boca.size)
        drawOval(Color(0xFFE0B04A), boca.topLeft, boca.size, style = Stroke(2.2f))
    }
}

private fun DrawScope.brujula(salon: SalonMundo, c: Offset) {
    val exterior = Rect(c.x - 29f, c.y - 21f, c.x + 29f, c.y + 21f)
    drawOval(Color.Black.copy(alpha = 0.4f), exterior.topLeft + Offset(1f, 3f), exterior.size)
    drawOval(Brush.verticalGradient(listOf(Color(0xFF3A3632), Color(0xFF17140F)), exterior.top, exterior.bottom), exterior.topLeft, exterior.size)
    drawOval(Color(0xFF6A645C), exterior.topLeft + Offset(1f, 1f), Size(exterior.width - 2f, exterior.height - 2f), style = Stroke(1.2f))
    val interior = Rect(c.x - 19f, c.y - 14f, c.x + 19f, c.y + 14f)
    drawOval(Brush.radialGradient(listOf(salon.brillo, salon.brillo.escalado(0.75f)), c, 19f), interior.topLeft, interior.size)
    for ((p0, p1) in listOf(
        Offset(c.x - 17f, c.y - 10f) to Offset(c.x - 10f, c.y - 6f),
        Offset(c.x - 17f, c.y + 10f) to Offset(c.x - 10f, c.y + 6f),
        Offset(c.x + 21f, c.y) to Offset(c.x + 12f, c.y)
    )) drawLine(Color(0xFF8A847C), p0, p1, 2.2f, cap = StrokeCap.Round)
    drawCircle(Brush.radialGradient(listOf(salon.brillo.copy(alpha = 0.95f), salon.brillo.copy(alpha = 0f)), c, 13f), 13f, c)
    drawCircle(Color.White, 5f, c)
}

private fun DrawScope.escudo(salon: SalonMundo, c: Offset, r: Float) {
    drawCircle(Color.Black.copy(alpha = 0.45f), r, c + Offset(2f, 6f))
    drawCircle(
        Brush.radialGradient(listOf(salon.anillo.escalado(1.15f), salon.anillo, salon.anillo.escalado(0.7f)), c - Offset(r * 0.3f, r * 0.3f), r * 1.3f),
        r, c
    )
    for (k in 0 until 5) drawCircle(Color.Black.copy(alpha = 0.12f), r - 2.5f - k * 3f, c, style = Stroke(0.8f))
    drawCircle(Color.Black.copy(alpha = 0.55f), r - 0.5f, c, style = Stroke(1.2f))
    val ri = r - 16f
    drawCircle(Brush.radialGradient(listOf(salon.interiorEscudo.escalado(1.25f), salon.interiorEscudo), c + Offset(r * 0.2f, -r * 0.2f), ri), ri, c)
    drawCircle(Color.Black.copy(alpha = 0.6f), ri, c, style = Stroke(1.5f))
    val rr = r - 8f
    for (grados in listOf(-75f, -45f, -15f, 15f, 45f)) {
        val rad = grados * PI.toFloat() / 180f
        val p = Offset(c.x + cos(rad) * rr, c.y + sin(rad) * rr)
        drawArc(Color(0xFFA0461C), 40f, 280f, false, Offset(p.x - 3.6f, p.y - 3.6f), Size(7.2f, 7.2f), style = Stroke(1.4f))
    }
}

private fun DrawScope.moneda(c: Offset, r: Float) {
    drawCircle(Color.Black.copy(alpha = 0.45f), r, c + Offset(1f, 2.5f))
    drawCircle(Brush.radialGradient(listOf(Color(0xFFEDEDED), Color(0xFFB4B4B4), Color(0xFF7C7C7C)), c - Offset(r * 0.35f, r * 0.35f), r * 1.4f), r, c)
    drawCircle(Color(0xFF6E6E6E), r - 3f, c, style = Stroke(1f))
    // Runa ᛟ dibujada con líneas
    val k = r * 0.45f
    val runa = Path().apply {
        moveTo(c.x - k * 0.7f, c.y + k); lineTo(c.x + k * 0.5f, c.y - k * 0.1f); lineTo(c.x, c.y - k * 0.7f)
        lineTo(c.x - k * 0.5f, c.y - k * 0.1f); lineTo(c.x + k * 0.7f, c.y + k)
    }
    drawPath(runa, Color(0xFF5E5E5E), style = Stroke(1.4f))
}

private fun DrawScope.gema(salon: SalonMundo, c: Offset, r: Float) {
    drawCircle(Color.Black.copy(alpha = 0.5f), r, c + Offset(0.8f, 2f))
    drawCircle(Brush.radialGradient(listOf(salon.gema.escalado(1.45f), salon.gema, salon.gema.escalado(0.5f)), c - Offset(r * 0.35f, r * 0.4f), r * 1.3f), r, c)
    drawCircle(Color.White.copy(alpha = 0.75f), r * 0.28f, c - Offset(r * 0.35f, r * 0.4f))
}
