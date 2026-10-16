package com.aesir.odin.ui.diagnostico

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aesir.odin.ui.components.odin.TextoAjustable
import com.aesir.odin.ui.components.odin.centroEn
import com.aesir.odin.ui.components.odin.pulsable
import com.aesir.odin.ui.components.odin.rememberTiempo
import com.aesir.odin.ui.ragnarok.Brasas
import com.aesir.odin.ui.ragnarok.RG
import com.aesir.odin.ui.ragnarok.caminoPuntas
import com.aesir.odin.ui.ragnarok.cinzel
import androidx.compose.material3.Text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

// Pozo de Mímir (mockup «Evaluación diagnóstica»): fondo de cueva, el pozo
// animado y las piezas de interfaz que comparten sus tres pantallas.
//
// Las medidas están en PÍXELES del mockup del pozo (500 × 1080) y se pasan al
// lienzo del Ragnarök con MI. El color de acento es el del mundo, así el pozo
// es dorado, turquesa, verde o naranja.

/** Píxeles del mockup del pozo → dp de diseño del lienzo del Ragnarök. */
object MI {
    const val K = 540f / 500f

    fun x(px: Float) = RG.x(px * K)
    fun y(px: Float) = RG.y(px * K)
    fun d(px: Float) = RG.d(px * K)

    /** Runas de la prueba de sabiduría (una por pregunta). */
    val runas = listOf("ᚠ", "ᚢ", "ᚦ", "ᚨ", "ᚱ", "ᚲ", "ᚷ", "ᚹ", "ᚺ", "ᚾ", "ᛁ", "ᛃ")
    val ordinales = listOf("primera", "segunda", "tercera", "cuarta", "quinta", "sexta", "séptima", "octava", "novena", "décima", "undécima", "duodécima")

    val Rojo = Color(0xFFE0675A)
    val Tinta = Color(0xFF1B130A)
    val Fondo = Color(0xFF0E0D0C)
    val Gris = Color(0xFFC9BEAB)

    fun runa(i: Int) = runas[max(i, 0) % runas.size]
}

/** Centra el elemento en (x, y) px del mockup del pozo. */
fun Modifier.enMI(x: Float, y: Float): Modifier = centroEn(MI.x(x), MI.y(y))

/** Tamaño en px del mockup del pozo. */
fun Modifier.tamMI(w: Float, h: Float): Modifier = this.then(Modifier.size(MI.d(w).dp, MI.d(h).dp))

/** Dibuja en px del mockup del pozo sobre un lienzo que ocupa todo el diseño. */
private inline fun DrawScope.enPozo(bloque: DrawScope.() -> Unit) {
    val f = size.width / 500f
    withTransform({
        scale(f, f, pivot = Offset.Zero)
        translate(0f, -RG.ARRIBA / MI.K)
    }) { bloque() }
}

private fun elipse(c: Offset, rx: Float, ry: Float) = Path().apply { addOval(Rect(c.x - rx, c.y - ry, c.x + rx, c.y + ry)) }

// ---------------------------------------------------------------------------
// Fondo
// ---------------------------------------------------------------------------

enum class VarianteFondoPozo { LLAMADO, PRUEBA, REVELACION }

/**
 * Fondo de cueva de las tres pantallas: resplandor del color del mundo,
 * esquirlas de roca en los costados, raíces y brasas que suben.
 */
@Composable
fun FondoPozo(acento: Color, variante: VarianteFondoPozo) {
    val tiempo = rememberTiempo()
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val t = tiempo.value
            enPozo { dibujarFondo(acento, variante, t) }
        }
        Brasas(
            cantidad = if (variante == VarianteFondoPozo.LLAMADO) 22 else 16,
            zona = Rect(0f, 140f * MI.K, 540f, (140f + 940f) * MI.K),
            color = acento,
            semilla = 31,
            modifier = Modifier.alpha(0.8f)
        )
    }
}

private fun DrawScope.dibujarFondo(acento: Color, variante: VarianteFondoPozo, t: Float) {
    val pulso = 0.85f + 0.15f * sin(t * 1.1f)
    drawRect(MI.Fondo, Offset(0f, 0f), Size(500f, 1080f))
    when (variante) {
        VarianteFondoPozo.LLAMADO -> {
            disco(acento, Offset(250f, 350f), 250f, 0.33f * pulso)
            drawRect(
                Brush.verticalGradient(0.45f to Color.Black.copy(alpha = 0f), 0.7f to Color.Black.copy(alpha = 0.55f), 1f to Color.Black.copy(alpha = 0.75f), startY = 0f, endY = 1080f),
                Offset.Zero, Size(500f, 1080f)
            )
            losas(acento, -1f)
            losas(acento, 1f)
            esquirlas(acento, -1f, 560f, 1080f, 3)
            esquirlas(acento, 1f, 560f, 1080f, 4)
        }
        VarianteFondoPozo.PRUEBA -> {
            resplandor(acento, Offset(250f, 1100f), 520f, 0.4f * pulso)
            anillos(acento, Offset(250f, 1100f), listOf(300f, 360f, 430f), 0.12f)
            raices(t)
            esquirlas(acento, -1f, 0f, 1080f, 5)
            esquirlas(acento, 1f, 0f, 1080f, 6)
        }
        VarianteFondoPozo.REVELACION -> {
            resplandor(acento, Offset(250f, 290f), 330f, 0.36f * pulso)
            anillos(acento, Offset(250f, 290f), listOf(215f, 330f), 0.1f)
            raices(t)
            esquirlas(acento, -1f, 0f, 1080f, 7)
            esquirlas(acento, 1f, 0f, 1080f, 8)
        }
    }
}

private fun DrawScope.resplandor(acento: Color, c: Offset, r: Float, alfa: Float) {
    drawCircle(
        Brush.radialGradient(0f to acento.copy(alpha = alfa), 0.45f to acento.copy(alpha = alfa * 0.55f), 1f to acento.copy(alpha = 0f), center = c, radius = r),
        r, c
    )
}

/** Resplandor con borde definido (el hueco iluminado de la cueva). */
private fun DrawScope.disco(acento: Color, c: Offset, r: Float, alfa: Float) {
    drawCircle(
        Brush.radialGradient(
            0f to acento.copy(alpha = alfa), 0.6f to acento.copy(alpha = alfa * 0.72f),
            0.88f to acento.copy(alpha = alfa * 0.3f), 1f to acento.copy(alpha = 0f), center = c, radius = r
        ),
        r, c
    )
}

private fun DrawScope.anillos(acento: Color, c: Offset, radios: List<Float>, alfa: Float) {
    radios.forEach { drawCircle(acento.copy(alpha = alfa), it, c, style = Stroke(1f)) }
}

/** Raíces finas que cuelgan del techo de la cueva. */
private fun DrawScope.raices(t: Float) {
    val rnd = Random(19)
    for (i in 0 until 9) {
        val x0 = 30f + rnd.nextFloat() * 440f
        val largo = 60f + rnd.nextFloat() * 120f
        val vaiven = sin(t * 0.5f + i) * 3f
        val c1 = Offset(x0 + (rnd.nextFloat() - 0.5f) * 40f, largo * 0.3f)
        val c2 = Offset(x0 + (rnd.nextFloat() - 0.5f) * 60f, largo * 0.7f)
        val fin = Offset(x0 + (rnd.nextFloat() - 0.5f) * 50f + vaiven, largo)
        val p = Path().apply { moveTo(x0, -5f); cubicTo(c1.x, c1.y, c2.x, c2.y, fin.x, fin.y) }
        drawPath(p, Color(0xFF8C8A84).copy(alpha = 0.18f), style = Stroke(1.4f, cap = StrokeCap.Round))
    }
}

/** Esquirlas de roca oscura en un costado (lado −1 izquierda, 1 derecha). */
private fun DrawScope.esquirlas(acento: Color, lado: Float, desde: Float, hasta: Float, semilla: Int) {
    val rnd = Random(semilla)
    var y = desde - 20f
    val tonos = listOf(Color(0xFF1B1B1A), Color(0xFF222221), Color(0xFF282827))
    val borde = if (lado < 0) -2f else 502f
    fun x(v: Float) = borde - lado * v
    while (y < hasta) {
        val alto = 70f + rnd.nextFloat() * 80f
        val a = 18f + rnd.nextFloat() * 55f
        val b = 30f + rnd.nextFloat() * 60f
        val p = Path().apply {
            moveTo(borde, y)
            lineTo(x(a), y + alto * 0.12f)
            lineTo(x(b), y + alto * 0.55f)
            lineTo(x(a * 0.7f), y + alto)
            lineTo(borde, y + alto * 1.05f)
            close()
        }
        drawPath(p, tonos[(rnd.nextFloat() * 2.99f).toInt()])
        drawPath(p, Color.Black.copy(alpha = 0.7f), style = Stroke(1f))
        drawLine(acento.copy(alpha = 0.1f), Offset(x(a), y + alto * 0.12f), Offset(x(b), y + alto * 0.55f), 1f)
        y += alto * (0.75f + rnd.nextFloat() * 0.3f)
    }
}

/** Losas de roca a los lados del pozo, iluminadas por el borde. */
private fun DrawScope.losas(acento: Color, lado: Float) {
    val rnd = Random(if (lado < 0) 41 else 42)
    var y = 182f
    val borde = if (lado < 0) -2f else 502f
    fun x(v: Float) = borde - lado * v
    while (y < 570f) {
        val alto = 46f + rnd.nextFloat() * 40f
        val ancho = 64f + rnd.nextFloat() * 52f
        val quiebre = 10f + rnd.nextFloat() * 22f
        val p = Path().apply {
            moveTo(borde, y)
            lineTo(x(ancho), y)
            lineTo(x(ancho + quiebre), y + alto * 0.45f)
            lineTo(x(ancho - quiebre * 0.4f), y + alto)
            lineTo(borde, y + alto)
            close()
        }
        val tono = 0x1B + (rnd.nextFloat() * 9f).toInt()
        drawPath(p, Color(tono, tono, tono - 1))
        drawPath(p, Color.Black.copy(alpha = 0.75f), style = Stroke(1.2f))
        // Luz del pozo en el canto
        val canto = Path().apply {
            moveTo(x(ancho), y)
            lineTo(x(ancho + quiebre), y + alto * 0.45f)
            lineTo(x(ancho - quiebre * 0.4f), y + alto)
        }
        drawPath(canto, acento.copy(alpha = 0.32f), style = Stroke(1.1f))
        y += alto + 2f
    }
}

// ---------------------------------------------------------------------------
// El pozo
// ---------------------------------------------------------------------------

/**
 * Ilustración animada del llamado: ramas, haz de luz, Mímir entre dos agujas,
 * el pozo de piedra con su agua luminosa, velas y el chorro que cae.
 */
@Composable
fun EscenaPozo(acento: Color, modifier: Modifier = Modifier) {
    val tiempo = rememberTiempo()
    Canvas(modifier.fillMaxSize()) {
        val t = tiempo.value
        enPozo { dibujarPozo(acento, t) }
    }
}

private fun DrawScope.dibujarPozo(acento: Color, t: Float) {
    val pulso = 0.82f + 0.18f * sin(t * 1.6f)
    ramas(t)
    estanque(acento, t)
    cilindro(Offset(250f, 468f), 188f, 44f, 40f, Color(0xFF3C3C3A), Color(0xFF4E4E4C), 2)
    chorro(acento, t)
    cilindro(Offset(250f, 410f), 146f, 40f, 50f, Color(0xFF474745), null, 2)
    brocal()
    agua(acento, t, pulso)
    vela(acento, Offset(125f, 386f), t, 0)
    vela(acento, Offset(375f, 386f), t, 1)
    haz(t)
    aspas()
    aguja(218f, -1f)
    aguja(282f, 1f)
    // Máscara: un poco más grande y abajo que el trazo base; flota despacio
    withTransform({
        translate(250f, 336f + sin(t * 1.2f) * 1.6f)
        scale(1.1f, 1.1f, pivot = Offset.Zero)
        translate(-250f, -330f)
    }) {
        dibujarRostroMimir(acento, pulso, 0.5f + 0.5f * sin(t * 0.9f))
    }
    vela(acento, Offset(195f, 426f), t, 2)
    vela(acento, Offset(306f, 426f), t, 3)
}

/** Ramas secas que entran desde los costados. */
private fun DrawScope.ramas(t: Float) {
    val rnd = Random(23)
    val color = Color(0xFF999B99).copy(alpha = 0.85f)
    fun rama(p: Offset, angulo: Float, largo: Float, grosor: Float, nivel: Int, fase: Float, caida: Float) {
        if (nivel <= 0 || largo <= 3f) return
        val vaiven = sin(t * 0.7f + fase) * 0.022f * (7 - nivel)
        val a = angulo + vaiven
        val fin = Offset(p.x + cos(a) * largo, p.y + sin(a) * largo)
        val trazo = Path().apply {
            moveTo(p.x, p.y)
            quadraticTo((p.x + fin.x) / 2f, (p.y + fin.y) / 2f - largo * 0.06f, fin.x, fin.y)
        }
        drawPath(trazo, color, style = Stroke(grosor, cap = StrokeCap.Round))
        // Una rama sigue casi derecha y otra se abre; las puntas cuelgan un poco
        val sigue = (rnd.nextFloat() - 0.5f) * 0.3f + caida
        rama(fin, a + sigue, largo * (0.74f + rnd.nextFloat() * 0.1f), max(grosor * 0.8f, 0.8f), nivel - 1, fase + 0.4f, caida)
        val lado = if (rnd.nextFloat() > 0.5f) 1f else -1f
        val abre = (0.45f + rnd.nextFloat() * 0.4f) * lado + caida * 2f
        rama(fin, a + abre, largo * (0.58f + rnd.nextFloat() * 0.12f), max(grosor * 0.68f, 0.8f), nivel - 1, fase + 1.1f, caida)
    }
    for (lado in listOf(-1f, 1f)) {
        val x0 = if (lado < 0) -6f else 506f
        val base = if (lado < 0) 0f else PI.toFloat()
        // Caída hacia abajo: en la izquierda sube el ángulo; en la derecha, baja
        val caida = 0.11f * lado * -1f
        rama(Offset(x0, 222f), base - lado * 0.1f, 78f, 7f, 5, 0f, caida)
        rama(Offset(x0, 250f), base + lado * 0.35f, 52f, 4.5f, 5, 1.3f, caida * 1.6f)
    }
}

/** Estanque bajo el pozo, con ondas que se abren. */
private fun DrawScope.estanque(acento: Color, t: Float) {
    val c = Offset(250f, 540f)
    // Degradado radial aplastado: se escala en y para que siga la elipse
    withTransform({ scale(1f, 38f / 210f, pivot = c) }) {
        drawCircle(
            Brush.radialGradient(listOf(acento.copy(alpha = 0.38f), acento.copy(alpha = 0.16f), acento.copy(alpha = 0f)), center = c, radius = 210f),
            210f, c
        )
    }
    for (i in 0 until 3) {
        val f = (t * 0.18f + i / 3f) % 1f
        val rx = 40f + f * 165f
        drawPath(elipse(c, rx, rx * 0.17f), acento.copy(alpha = 0.35f * (1f - f)), style = Stroke(1f))
    }
}

/** Cilindro de piedra: cara frontal con hiladas y la tapa elíptica. */
private fun DrawScope.cilindro(c: Offset, rx: Float, ry: Float, alto: Float, cara: Color, tapa: Color?, hiladas: Int) {
    fun frente(y: Float): List<Offset> = (0..40).map { k ->
        val s = k / 40f
        val th = PI * (1 - s)
        Offset(c.x + rx * cos(th).toFloat(), y + ry * sin(th).toFloat())
    }
    val lado = Path().apply {
        moveTo(c.x - rx, c.y)
        lineTo(c.x - rx, c.y + alto)
        frente(c.y + alto).forEach { lineTo(it.x, it.y) }
        lineTo(c.x + rx, c.y)
        frente(c.y).reversed().forEach { lineTo(it.x, it.y) }
        close()
    }
    drawPath(lado, cara)
    drawPath(
        lado,
        Brush.horizontalGradient(
            0f to Color.Black.copy(alpha = 0.55f), 0.35f to Color.Black.copy(alpha = 0f),
            0.65f to Color.Black.copy(alpha = 0f), 1f to Color.Black.copy(alpha = 0.55f),
            startX = c.x - rx, endX = c.x + rx
        )
    )
    // Hiladas de bloques
    val junta = Color.Black.copy(alpha = 0.45f)
    for (f in 1 until hiladas) {
        val pts = frente(c.y + alto * f / hiladas)
        val linea = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(linea, junta, style = Stroke(1f))
    }
    for (f in 0 until hiladas) {
        val desfase = if (f % 2 == 0) 0.0 else 0.5
        val y0 = c.y + alto * f / hiladas
        val y1 = c.y + alto * (f + 1) / hiladas
        for (j in 0 until 12) {
            val th = PI * (j + 0.5 + desfase) / 12.5
            if (th <= 0.08 || th >= PI - 0.08) continue
            val x = c.x + rx * cos(th).toFloat()
            val s = ry * sin(th).toFloat()
            drawLine(junta, Offset(x, y0 + s), Offset(x, y1 + s), 1f)
        }
    }
    if (tapa != null) {
        drawPath(elipse(c, rx, ry), tapa)
        drawPath(elipse(c, rx, ry), Color.Black.copy(alpha = 0.4f), style = Stroke(1f))
    }
}

/** Brocal: anillo de piedra con juntas radiales. */
private fun DrawScope.brocal() {
    val c = Offset(250f, 410f)
    val anillo = Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(Rect(c.x - 146f, c.y - 40f, c.x + 146f, c.y + 40f))
        addOval(Rect(c.x - 122f, c.y - 31f, c.x + 122f, c.y + 31f))
    }
    drawPath(anillo, Color(0xFF5A5A58))
    for (i in 0 until 20) {
        val a = i * PI / 10
        drawLine(
            Color.Black.copy(alpha = 0.35f),
            Offset(c.x + 122f * cos(a).toFloat(), c.y + 31f * sin(a).toFloat()),
            Offset(c.x + 146f * cos(a).toFloat(), c.y + 40f * sin(a).toFloat()), 1f
        )
    }
    drawPath(elipse(c, 146f, 40f), Color.Black.copy(alpha = 0.5f), style = Stroke(1f))
}

/** Agua luminosa con anillos que se abren desde el centro. */
private fun DrawScope.agua(acento: Color, t: Float, pulso: Float) {
    val c = Offset(250f, 408f)
    val forma = elipse(c, 122f, 31f)
    drawPath(forma, acento.copy(alpha = 0.8f))
    withTransform({ scale(1f, 31f / 122f, pivot = c) }) {
        drawCircle(
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.26f * pulso), Color.White.copy(alpha = 0.05f), Color.Black.copy(alpha = 0.3f)),
                center = c, radius = 122f
            ),
            122f, c
        )
    }
    clipPath(forma) {
        for (r in listOf(96f, 70f)) drawPath(elipse(c, r, r * 0.25f), Color.Black.copy(alpha = 0.14f), style = Stroke(1f))
        for (i in 0 until 3) {
            val f = (t * 0.32f + i / 3f) % 1f
            val r = 10f + f * 115f
            drawPath(elipse(c, r, r * 0.25f), Color.White.copy(alpha = 0.4f * (1f - f)), style = Stroke(1.2f))
        }
    }
    // Resplandor sobre el agua
    withTransform({ scale(1f, 60f / 150f, pivot = c) }) {
        drawCircle(
            Brush.radialGradient(listOf(acento.copy(alpha = 0.16f * pulso), acento.copy(alpha = 0f)), center = c, radius = 150f),
            150f, c, blendMode = BlendMode.Plus
        )
    }
}

/** Haz de luz que baja al pozo. */
private fun DrawScope.haz(t: Float) {
    val arriba = 188f
    val abajo = 402f
    val forma = Path().apply {
        moveTo(232f, arriba); lineTo(268f, arriba); lineTo(293f, abajo); lineTo(207f, abajo); close()
    }
    drawPath(
        forma,
        Brush.verticalGradient(
            0f to Color(0xFF9A9DA2).copy(alpha = 0f), 0.2f to Color(0xFF8E9196).copy(alpha = 0.62f),
            1f to Color(0xFF7A7D82).copy(alpha = 0.5f), startY = arriba, endY = abajo
        )
    )
    clipPath(forma) {
        // Facetas verticales que titilan
        for (i in 0 until 7) {
            val f = (i + 1) / 8f
            val brillo = 0.12f + 0.12f * (0.5f + 0.5f * sin(t * 1.3f + i * 1.7f))
            drawLine(Color.White.copy(alpha = brillo), Offset(232f + 36f * f, arriba), Offset(207f + 86f * f, abajo), 1f)
        }
        // Motas de luz que bajan
        val rnd = Random(57)
        repeat(12) {
            val fase = rnd.nextFloat()
            val x0 = rnd.nextFloat()
            val avance = (t * 0.07f + fase) % 1f
            val y = arriba + 10f + avance * (abajo - arriba - 10f)
            val ancho = 36f + (y - arriba) / (abajo - arriba) * 50f
            val x = 250f + (x0 - 0.5f) * ancho
            val alfa = sin(avance * PI.toFloat()) * 0.8f
            drawCircle(Color.White.copy(alpha = alfa.coerceIn(0f, 1f)), 1.1f, Offset(x, y))
        }
    }
    for (lado in listOf(-1f, 1f)) {
        drawLine(Color.White.copy(alpha = 0.32f), Offset(250f + lado * 18f, arriba + 20f), Offset(250f + lado * 43f, abajo), 1f)
    }
}

/** Aspas oscuras sobre el agua, al pie de Mímir. */
private fun DrawScope.aspas() {
    val c = Offset(250f, 400f)
    val puntas = listOf(
        Offset(158f, 397f) to Offset(210f, 388f),
        Offset(342f, 397f) to Offset(290f, 388f),
        Offset(200f, 428f) to Offset(220f, 410f),
        Offset(300f, 428f) to Offset(280f, 410f)
    )
    for ((punta, control) in puntas) {
        val p = Path().apply {
            moveTo(c.x, c.y - 6f)
            quadraticTo(control.x, control.y, punta.x, punta.y)
            quadraticTo((punta.x + c.x) / 2f, (punta.y + c.y) / 2f + 9f, c.x, c.y + 8f)
            close()
        }
        drawPath(p, Color(0xFF2E3135))
        drawPath(p, Color(0xFF4A4E55), style = Stroke(1f))
    }
}

/** Agujas doradas a los lados de Mímir. */
private fun DrawScope.aguja(x: Float, lado: Float) {
    val cuerpo = Path().apply {
        moveTo(x - 9f, 392f); lineTo(x - 9f, 331f); lineTo(x, 312f); lineTo(x + 9f, 331f); lineTo(x + 9f, 392f); close()
    }
    drawPath(cuerpo, Brush.horizontalGradient(listOf(Color(0xFFF0C25E), Color(0xFFB9852F)), startX = x - 9f, endX = x + 9f))
    drawPath(cuerpo, Color(0xFF5C3F12), style = Stroke(1.1f))
    val detalle = Color(0xFF5C3F12).copy(alpha = 0.8f)
    drawLine(detalle, Offset(x, 330f), Offset(x, 388f), 1f)
    drawLine(detalle, Offset(x - 9f, 350f), Offset(x + 9f, 350f), 1f)
    drawLine(detalle, Offset(x - 9f, 370f), Offset(x + 9f, 370f), 1f)
    drawLine(Color(0xFFD9A440), Offset(x + lado * 2f, 316f), Offset(x + lado * 8f, 296f), 1.6f, cap = StrokeCap.Round)
}

/**
 * Máscara de Mímir (cuernos, corona, rostro y ojos) centrada en (250, 330)
 * del mockup. [ojos] de 0 a 1: cuánto brillan con el color del mundo.
 */
fun DrawScope.dibujarRostroMimir(acento: Color, pulso: Float, ojos: Float) {
    val c = Offset(250f, 330f)
    // Halo
    drawCircle(Brush.radialGradient(listOf(acento.copy(alpha = 0.35f * pulso), acento.copy(alpha = 0f)), center = c, radius = 46f), 46f, c)
    // Cuernos
    for (lado in listOf(-1f, 1f)) {
        val cuerno = Path().apply {
            moveTo(250f + lado * 12f, 302f)
            quadraticTo(250f + lado * 30f, 301f, 250f + lado * 24f, 284f)
        }
        drawPath(cuerno, Color(0xFFF3E4B5), style = Stroke(3.6f, cap = StrokeCap.Round))
    }
    // Corona
    val picos = listOf(232f to 305f, 235f to 293f, 241f to 300f, 246f to 288f, 250f to 297f, 254f to 288f, 259f to 300f, 265f to 293f, 268f to 305f)
    val corona = Path().apply {
        moveTo(picos[0].first, picos[0].second)
        picos.drop(1).forEach { lineTo(it.first, it.second) }
        close()
    }
    drawPath(corona, Color(0xFFE7B24A))
    drawPath(corona, Color(0xFF6B4A14), style = Stroke(1f))
    // Rostro
    val puntos = listOf(232f to 304f, 268f to 304f, 272f to 318f, 268f to 340f, 256f to 352f, 250f to 357f, 244f to 352f, 232f to 340f, 228f to 318f)
    val cara = Path().apply {
        moveTo(puntos[0].first, puntos[0].second)
        puntos.drop(1).forEach { lineTo(it.first, it.second) }
        close()
    }
    drawPath(cara, Brush.verticalGradient(listOf(Color(0xFFF4C962), Color(0xFFD49A38), Color(0xFFB27A26)), startY = 304f, endY = 357f))
    drawPath(cara, Color(0xFF6B4A14), style = Stroke(1.2f))
    // Ojos (se encienden poco a poco con el color del mundo)
    val oscuro = Color(0xFF2A1C08)
    for (lado in listOf(-1f, 1f)) {
        val ojo = Path().apply {
            moveTo(250f + lado * 15f, 318f); lineTo(250f + lado * 4f, 322f); lineTo(250f + lado * 12f, 327f); close()
        }
        drawPath(ojo, oscuro)
        drawPath(ojo, acento.copy(alpha = (0.75f * ojos * ojos).coerceIn(0f, 1f)))
        drawLine(oscuro, Offset(250f + lado * 17f, 313f), Offset(250f + lado * 5f, 318f), 1.8f, cap = StrokeCap.Round)
    }
    drawLine(oscuro.copy(alpha = 0.85f), Offset(250f, 324f), Offset(250f, 335f), 1.4f, cap = StrokeCap.Round)
    drawLine(oscuro.copy(alpha = 0.85f), Offset(243f, 342f), Offset(257f, 342f), 1.4f, cap = StrokeCap.Round)
}

/** Velas sobre el brocal. */
private fun DrawScope.vela(acento: Color, base: Offset, t: Float, i: Int) {
    drawRect(Color(0xFFEDE6D6), Offset(base.x - 2.6f, base.y - 12f), Size(5.2f, 12f))
    val tiembla = 0.75f + 0.25f * sin(t * 9f + i * 2.1f) * sin(t * 5.3f + i)
    val llama = Offset(base.x + sin(t * 6f + i) * 0.6f, base.y - 16f)
    drawCircle(
        Brush.radialGradient(listOf(acento.copy(alpha = 0.45f * tiembla), acento.copy(alpha = 0f)), center = llama, radius = 13f),
        13f, llama, blendMode = BlendMode.Plus
    )
    drawOval(Color(0xFFFFE7A8), Offset(llama.x - 2.1f, llama.y - 3.8f * tiembla), Size(4.2f, 7.6f * tiembla))
}

/** Chorro que cae del pozo al estanque. */
private fun DrawScope.chorro(acento: Color, t: Float) {
    for (i in 0 until 5) {
        val x = 238f + i * 6f
        drawLine(
            acento.copy(alpha = 0.75f), Offset(x, 500f), Offset(x, 548f), 1.2f, cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 4f), (t * 26f - i * 3f) % 11f + 11f)
        )
    }
}

// ---------------------------------------------------------------------------
// Piezas de interfaz
// ---------------------------------------------------------------------------

/** Botón principal del pozo, con puntas y del color del mundo. */
@Composable
fun BotonPozo(texto: String, acento: Color, modifier: Modifier, habilitado: Boolean = true, accion: () -> Unit) {
    Box(
        modifier
            .tamMI(438f, 59f)
            .alpha(if (habilitado) 1f else 0.42f)
            .drawBehind {
                val r = Rect(Offset.Zero, size)
                if (habilitado) drawPath(caminoPuntas(r.inflate(8.dp.toPx())), acento.copy(alpha = 0.10f))
                drawPath(caminoPuntas(r), acento)
                drawPath(caminoPuntas(r), Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.14f), Color.Transparent, Color.Black.copy(alpha = 0.1f))))
            }
            .pulsable(habilitado, accion),
        contentAlignment = Alignment.Center
    ) {
        TextoAjustable(
            texto,
            cinzel(18.5f * MI.K, FontWeight.Black, 3.4f * MI.K, MI.Tinta),
            Modifier.padding(horizontal = MI.d(44f).dp),
            minEscala = 0.7f
        )
    }
}

/** Rótulo pequeño con letras espaciadas (Cinzel), del color dado. */
@Composable
fun RotuloPozo(texto: String, color: Color, modifier: Modifier = Modifier, tamano: Float = 11.5f, espacio: Float = 3.8f) {
    Text(texto, modifier, style = cinzel(tamano * MI.K, FontWeight.Bold, espacio * MI.K, color), maxLines = 1, softWrap = false)
}

/** Línea con un rombo hueco al centro. */
@Composable
fun DivisorPozo(color: Color, modifier: Modifier = Modifier, ancho: Float = 307f) {
    Canvas(modifier.tamMI(ancho, 10f)) {
        val cy = size.height / 2f
        val hueco = MI.d(8f).dp.toPx()
        drawLine(color.copy(alpha = 0.5f), Offset(0f, cy), Offset(size.width / 2f - hueco, cy), 1.dp.toPx())
        drawLine(color.copy(alpha = 0.5f), Offset(size.width / 2f + hueco, cy), Offset(size.width, cy), 1.dp.toPx())
        val r = MI.d(5f).dp.toPx()
        val c = center
        val rombo = Path().apply { moveTo(c.x, c.y - r); lineTo(c.x + r, c.y); lineTo(c.x, c.y + r); lineTo(c.x - r, c.y); close() }
        drawPath(rombo, color.copy(alpha = 0.8f), style = Stroke(1.dp.toPx()))
    }
}
