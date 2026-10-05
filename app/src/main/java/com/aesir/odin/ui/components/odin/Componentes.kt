package com.aesir.odin.ui.components.odin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.math.max
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Formas
// ---------------------------------------------------------------------------

/** Botón con puntas a los lados, como los de los mockups. */
val FormaFlecha = GenericShape { size, _ ->
    val p = size.height * 0.24f
    moveTo(p, 0f)
    lineTo(size.width - p, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(size.width - p, size.height)
    lineTo(p, size.height)
    lineTo(0f, size.height / 2f)
    close()
}

/** Rombo para las letras de las opciones y los sellos pequeños. */
val FormaRombo = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(size.width / 2f, size.height)
    lineTo(0f, size.height / 2f)
    close()
}

// ---------------------------------------------------------------------------
// Estilos de texto
// ---------------------------------------------------------------------------

fun estiloTitulo(tamano: TextUnit, color: Color = OdinPaleta.Hueso, brillo: Color? = null) = TextStyle(
    fontFamily = FuenteTitulo,
    fontWeight = FontWeight.SemiBold,
    fontSize = tamano,
    letterSpacing = 1.sp,
    color = color,
    shadow = Shadow(brillo ?: Color.Black, Offset(0f, 2f), if (brillo != null) 18f else 8f)
)

/** Runa con resplandor del color del mundo. */
fun estiloRuna(tamano: TextUnit, color: Color, encendida: Boolean) = TextStyle(
    fontSize = tamano,
    fontWeight = FontWeight.Bold,
    color = if (encendida) Color(0xFFFFF6E0) else color,
    shadow = if (encendida) Shadow(color, Offset.Zero, 22f) else null,
    textAlign = TextAlign.Center
)

// ---------------------------------------------------------------------------
// Fondo con paisaje del mundo
// ---------------------------------------------------------------------------

/**
 * Fondo de pantalla: paisaje del mundo arriba (si [estilo] no es null), textura
 * de piedra, partículas propias del mundo y viñeta. El contenido va encima.
 */
@Composable
fun FondoMundo(
    estilo: EstiloMundo?,
    modifier: Modifier = Modifier,
    alturaPaisaje: Dp = 320.dp,
    paisajeInferior: Boolean = false,
    contenido: @Composable BoxScope.() -> Unit
) {
    val base = estilo?.fondo ?: Color(0xFF0D0B0A)
    Box(
        modifier
            .fillMaxSize()
            .background(base)
            .drawBehind {
                val w = size.width
                val h = size.height
                val rnd = Random(77)
                repeat(30) {
                    mancha(Offset(rnd.nextFloat() * w, rnd.nextFloat() * h), (30 + rnd.nextInt(70)).dp.toPx(), Color(0xFF8A6A4A), .05f)
                }
                val alto = alturaPaisaje.toPx()
                if (estilo != null) {
                    clipRect(bottom = alto) { inset(0f, 0f, 0f, h - alto) { dibujarPaisaje(estilo, 11) } }
                    drawRect(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = .5f), .45f to Color.Black.copy(alpha = .3f),
                            .85f to base.copy(alpha = .92f), 1f to base, endY = alto
                        ),
                        size = Size(w, alto)
                    )
                    if (paisajeInferior) {
                        // Franja inferior: se muestra la parte baja del paisaje (suelo, agua, lava).
                        val banda = alto * .9f
                        val inicio = h - banda
                        clipRect(top = inicio) {
                            inset(0f, h - banda / .6f, 0f, 0f) { dibujarPaisaje(estilo, 11) }
                        }
                        drawRect(
                            Brush.verticalGradient(0f to base, .55f to base.copy(alpha = .9f), 1f to Color.Black.copy(alpha = .2f), startY = inicio, endY = h),
                            topLeft = Offset(0f, inicio), size = Size(w, h - inicio)
                        )
                    }
                } else {
                    // Cielo nocturno genérico
                    drawRect(Brush.verticalGradient(listOf(Color(0xFF0B1220), base), endY = alto), size = Size(w, alto))
                    repeat(70) { drawCircle(Color.White.copy(alpha = .15f + rnd.nextFloat() * .5f), (rnd.nextFloat() * .7f + .2f).dp.toPx(), Offset(rnd.nextFloat() * w, rnd.nextFloat() * alto * .7f)) }
                    manchaOval(Offset(w * .5f, alto * .25f), w * .7f, alto * .12f, Color(0xFF3DD9A0), .07f)
                }
                val color = estilo?.particula ?: OdinPaleta.Brasa
                repeat(45) {
                    val px = rnd.nextFloat() * w
                    val py = rnd.nextFloat() * h
                    if (estilo?.escena == com.aesir.odin.ui.theme.Escena.TORMENTA) {
                        drawLine(color.copy(alpha = .08f + rnd.nextFloat() * .15f), Offset(px, py), Offset(px - 5.dp.toPx(), py + 16.dp.toPx()), .7.dp.toPx())
                    } else {
                        drawCircle(color.copy(alpha = .1f + rnd.nextFloat() * .4f), (rnd.nextFloat() * 1.1f + .3f).dp.toPx(), Offset(px, py))
                    }
                }
                drawRect(Brush.radialGradient(.55f to Color.Transparent, 1f to Color.Black.copy(alpha = .8f), center = Offset(w / 2, h * .45f), radius = max(w, h) * .75f))
            },
        content = contenido
    )
}

/** Quita color a lo que dibuja el contenido (mundos o temas bloqueados). */
fun Modifier.desaturar(activo: Boolean, saturacion: Float = 0.2f): Modifier =
    if (!activo) this else drawWithContent {
        val paint = Paint().apply {
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturacion) })
        }
        drawIntoCanvas { canvas ->
            canvas.saveLayer(Rect(Offset.Zero, size), paint)
            drawContent()
            canvas.restore()
        }
    }

// ---------------------------------------------------------------------------
// Piezas pequeñas
// ---------------------------------------------------------------------------

@Composable
fun BotonVolver(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(FormaFlecha)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = OdinPaleta.Hueso, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, color = OdinPaleta.Hueso, fontSize = 16.sp, fontFamily = FuenteTitulo)
    }
}

/** Sello rúnico circular (nodo de tema): aro metálico y runa al centro. */
@Composable
fun SelloRunico(
    runa: String,
    estilo: EstiloMundo,
    modifier: Modifier = Modifier,
    tamano: Dp = 52.dp,
    encendido: Boolean = true,
    resplandor: Boolean = encendido
) {
    Box(modifier.size(tamano), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            if (resplandor) mancha(center, r * 1.35f, estilo.acento, .35f)
            drawCircle(Color.Black.copy(alpha = .6f), r, center + Offset(0f, 2.dp.toPx()))
            drawCircle(
                Brush.linearGradient(
                    listOf(estilo.metalClaro, estilo.metalOscuro, estilo.metalClaro, estilo.metalOscuro),
                    start = Offset.Zero, end = Offset(size.width, size.height)
                ),
                r
            )
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFF2A2622), Color(0xFF070605)), center = center - Offset(r * .2f, r * .3f), radius = r * 1.2f),
                r * .86f
            )
            drawCircle(
                if (encendido) estilo.acento else estilo.metalOscuro,
                r * .86f,
                style = Stroke(if (encendido) 2.dp.toPx() else 1.dp.toPx())
            )
        }
        Text(
            runa,
            style = estiloRuna((tamano.value * 0.5f).sp, if (encendido) estilo.acento else Color(0xFF8A8478), encendido)
        )
    }
}

/** Separador con rombo central. */
@Composable
fun DivisorOrnamental(color: Color, acento: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(12.dp)) {
        val cy = size.height / 2
        val cx = size.width / 2
        val d = 6.dp.toPx()
        drawLine(color.copy(alpha = .6f), Offset(size.width * .15f, cy), Offset(cx - d * 2, cy), 1f)
        drawLine(color.copy(alpha = .6f), Offset(cx + d * 2, cy), Offset(size.width * .85f, cy), 1f)
        val rombo = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, cy - d); lineTo(cx + d, cy); lineTo(cx, cy + d); lineTo(cx - d, cy); close()
        }
        drawPath(rombo, acento, style = Stroke(1.dp.toPx()))
        drawCircle(acento, 1.6.dp.toPx(), Offset(cx, cy))
    }
}

/**
 * Botón principal (relleno con el color del mundo) o secundario (contorno metálico).
 */
@Composable
fun BotonOdin(
    texto: String,
    onClick: () -> Unit,
    acento: Color,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    primario: Boolean = true,
    metal: Color = OdinPaleta.Oro,
    iconoFinal: ImageVector? = null
) {
    val fondo = if (primario) acento else Color(0xF20C0A08)
    val textoColor = if (primario) OdinPaleta.Hollin else OdinPaleta.Hueso
    Row(
        modifier = modifier
            .height(52.dp)
            .alpha(if (habilitado) 1f else .38f)
            .clip(FormaFlecha)
            .background(fondo)
            .then(if (!primario) Modifier.border(1.5.dp, metal, FormaFlecha) else Modifier)
            .clickable(enabled = habilitado, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            texto.uppercase(),
            color = textoColor,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            letterSpacing = 1.2.sp,
            maxLines = 1
        )
        if (iconoFinal != null) {
            Spacer(Modifier.width(8.dp))
            Icon(iconoFinal, contentDescription = null, tint = textoColor, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * Panel con marco metálico, cara del material del mundo y una placa superior
 * con las runas del mundo. Se adapta al alto de su contenido.
 */
@Composable
fun PanelTallado(
    estilo: EstiloMundo,
    modifier: Modifier = Modifier,
    semilla: Int = 5,
    contenido: @Composable BoxScope.() -> Unit
) {
    val medidor = androidx.compose.ui.text.rememberTextMeasurer()
    val placa = androidx.compose.runtime.remember(estilo) {
        medidor.measure(
            estilo.runas.joinToString(" "),
            TextStyle(color = estilo.acento, fontSize = 12.sp, letterSpacing = 3.sp)
        )
    }
    Box(
        modifier
            .padding(top = 10.dp)
            .drawBehind {
                val corte = 14.dp.toPx()
                val marco = rectCortado(Rect(Offset.Zero, size), corte)
                drawPath(marco, Color.Black.copy(alpha = .6f))
                drawPath(
                    marco,
                    Brush.linearGradient(
                        listOf(estilo.metalClaro, estilo.metalOscuro, estilo.metalClaro, estilo.metalOscuro, estilo.metalClaro),
                        start = Offset.Zero, end = Offset(size.width, size.height)
                    )
                )
                val m = 6.dp.toPx()
                val interior = rectCortado(Rect(m, m, size.width - m, size.height - m), corte - 3.dp.toPx())
                clipPath(interior) { dibujarMaterial(estilo, semilla) }
                drawPath(interior, estilo.metalClaro.copy(alpha = .35f), style = Stroke(1.dp.toPx()))
                listOf(
                    Offset(corte, corte), Offset(size.width - corte, corte),
                    Offset(corte, size.height - corte), Offset(size.width - corte, size.height - corte)
                ).forEach {
                    drawCircle(Brush.radialGradient(listOf(estilo.metalClaro, Color(0xFF0A0806)), center = it - Offset(1.5f, 1.5f), radius = 5.dp.toPx()), 4.dp.toPx(), it)
                }
                // Placa superior con runas
                val pw = placa.size.width + 36.dp.toPx()
                val ph = 22.dp.toPx()
                val pr = Rect(size.width / 2 - pw / 2, -ph / 2, size.width / 2 + pw / 2, ph / 2)
                drawPath(rectCortado(pr, 8.dp.toPx()), estilo.metal)
                drawPath(rectCortado(pr.deflate(2.dp.toPx()), 7.dp.toPx()), Color(0xFF0B0907))
                drawText(placa, topLeft = Offset(size.width / 2 - placa.size.width / 2f, -placa.size.height / 2f))
            }
            .padding(horizontal = 20.dp, vertical = 22.dp),
        content = contenido
    )
}

/** Fila de puntos de progreso (preguntas respondidas). */
@Composable
fun PuntosProgreso(total: Int, actual: Int, acento: Color, metal: Color, modifier: Modifier = Modifier) {
    val paso = 20.dp
    Canvas(modifier.width(paso * (total - 1) + 14.dp).height(14.dp)) {
        val p = paso.toPx()
        val r0 = 7.dp.toPx()
        drawLine(metal.copy(alpha = .5f), Offset(r0, size.height / 2), Offset(r0 + p * (total - 1), size.height / 2), 1.5.dp.toPx())
        for (i in 0 until total) {
            val c = Offset(r0 + p * i, size.height / 2)
            val hecho = i < actual
            val enCurso = i == actual
            if (hecho && i < total - 1) drawLine(acento, c, c + Offset(p, 0f), 2.dp.toPx())
            drawCircle(if (hecho) acento else OdinPaleta.Hollin, if (enCurso) 6.dp.toPx() else 5.dp.toPx(), c)
            drawCircle(if (hecho || enCurso) acento else metal, if (enCurso) 6.dp.toPx() else 5.dp.toPx(), c, style = Stroke(if (enCurso) 2.dp.toPx() else 1.2.dp.toPx()))
            if (enCurso) drawCircle(acento, 2.5.dp.toPx(), c)
        }
    }
}

/** Contador de errores: círculos vacíos que se marcan con una X. */
@Composable
fun MarcadorErrores(errores: Int, limite: Int, metal: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.width((limite * 21).dp).height(16.dp)) {
        for (i in 0 until limite) {
            val c = Offset((8 + i * 21).dp.toPx(), size.height / 2)
            val usado = i < errores
            val r = 7.dp.toPx()
            drawCircle(if (usado) Color(0x59A02819) else Color(0x66000000), r, c)
            drawCircle(if (usado) OdinPaleta.Incorrecto else metal.copy(alpha = .6f), r, c, style = Stroke(1.3.dp.toPx()))
            if (usado) {
                val d = 3.dp.toPx()
                drawLine(Color(0xFFFF8A70), c + Offset(-d, -d), c + Offset(d, d), 1.6.dp.toPx())
                drawLine(Color(0xFFFF8A70), c + Offset(d, -d), c + Offset(-d, d), 1.6.dp.toPx())
            }
        }
    }
}
