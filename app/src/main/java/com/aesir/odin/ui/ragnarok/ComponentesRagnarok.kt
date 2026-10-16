package com.aesir.odin.ui.ragnarok

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.FuentesMockup
import com.aesir.odin.ui.components.odin.TextoAjustable
import com.aesir.odin.ui.components.odin.centroEn
import com.aesir.odin.ui.components.odin.pulsable
import com.aesir.odin.ui.navigation.LocalAppContainer
import com.aesir.odin.ui.theme.EstilosMundo
import kotlin.math.min

// Piezas de interfaz del Ragnarök (barra, rótulos, botones). Medidas en px del
// mockup (540 de ancho); se convierten con RG.

/** Centra el elemento en (x, y) px del mockup. */
fun Modifier.enRG(x: Float, y: Float): Modifier = centroEn(RG.x(x), RG.y(y))

/** Bloque de [ancho] px alineado a la izquierda desde [x], centrado en [y]. */
fun Modifier.desdeRG(x: Float, y: Float, ancho: Float): Modifier =
    centroEn(RG.x(x + ancho / 2f), RG.y(y)).width(RG.d(ancho).dp)

/** Bloque de [ancho] px alineado a la derecha que termina en [x], centrado en [y]. */
fun Modifier.hastaRG(x: Float, y: Float, ancho: Float): Modifier =
    centroEn(RG.x(x - ancho / 2f), RG.y(y)).width(RG.d(ancho).dp)

/** Tamaño en px del mockup. */
fun Modifier.tamRG(w: Float, h: Float): Modifier = requiredSize(RG.d(w).dp, RG.d(h).dp)

@Composable
fun cinzel(tamanoPx: Float, peso: FontWeight = FontWeight.Bold, espacioPx: Float = 0f, color: Color = RG.Crema) = TextStyle(
    fontFamily = FuentesMockup.cinzel(),
    fontWeight = peso,
    fontSize = RG.d(tamanoPx).sp,
    letterSpacing = RG.d(espacioPx).sp,
    color = color
)

@Composable
fun alegreya(
    tamanoPx: Float,
    peso: FontWeight = FontWeight.Normal,
    italica: Boolean = false,
    color: Color = RG.Crema,
    interlineaPx: Float? = null,
    alinear: TextAlign = TextAlign.Start
) = TextStyle(
    fontFamily = FuentesMockup.alegreya(),
    fontWeight = peso,
    fontStyle = if (italica) FontStyle.Italic else FontStyle.Normal,
    fontSize = RG.d(tamanoPx).sp,
    lineHeight = interlineaPx?.let { RG.d(tamanoPx + it).sp } ?: androidx.compose.ui.unit.TextUnit.Unspecified,
    color = color,
    textAlign = alinear
)

/** Rectángulo con esquinas cortadas en diagonal. */
class FormaCortada(private val corte: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(caminoCortado(Rect(Offset.Zero, size), with(density) { corte.dp.toPx() }))
}

fun caminoCortado(r: Rect, corte: Float): Path {
    val k = min(corte, min(r.width / 2f, r.height / 2f))
    return Path().apply {
        moveTo(r.left + k, r.top)
        lineTo(r.right - k, r.top)
        lineTo(r.right, r.top + k)
        lineTo(r.right, r.bottom - k)
        lineTo(r.right - k, r.bottom)
        lineTo(r.left + k, r.bottom)
        lineTo(r.left, r.bottom - k)
        lineTo(r.left, r.top + k)
        close()
    }
}

/** Contorno de un botón con puntas a los lados. */
fun caminoPuntas(r: Rect): Path {
    val p = r.height * 0.5f * 0.62f
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

/** Panel oscuro de esquinas cortadas con borde fino (px del mockup en [cortePx]). */
fun DrawScope.panelCortado(
    borde: Color = RG.BordePanel,
    cortePx: Float = 10f,
    opacidad: Float = 0.9f,
    discontinuo: Boolean = false,
    grosor: Float = 1f
) {
    val c = caminoCortado(Rect(Offset.Zero, size), RG.d(cortePx).dp.toPx())
    drawPath(c, RG.Panel.copy(alpha = opacidad))
    drawPath(
        c, borde,
        style = Stroke(
            width = grosor.dp.toPx(),
            pathEffect = if (discontinuo) PathEffect.dashPathEffect(floatArrayOf(RG.d(9f).dp.toPx(), RG.d(6f).dp.toPx())) else null
        )
    )
}

@Composable
fun PanelCortado(
    modifier: Modifier,
    borde: Color = RG.BordePanel,
    cortePx: Float = 10f,
    opacidad: Float = 0.9f,
    discontinuo: Boolean = false
) {
    Canvas(modifier) { panelCortado(borde, cortePx, opacidad, discontinuo) }
}

/** Barra superior: volver + nombre del mundo, el medallón del mundo al centro y el hidromiel. */
@Composable
fun BarraRagnarok(mundo: String, orden: Int, onVolver: () -> Unit, mostrarHidromiel: Boolean = true) {
    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .desdeRG(30f, 70f, 220f)
                .pulsable(alPulsar = onVolver)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = RG.Crema, modifier = Modifier.size(RG.d(24f).dp))
            Box(Modifier.width(RG.d(10f).dp))
            TextoAjustable(mundo, alegreya(23f), minEscala = 0.7f)
        }
        MedallonMundo(orden, 270f, 72f, radio = 26f)
        if (mostrarHidromiel) {
            val contenedor = LocalAppContainer.current
            val saldo by contenedor.moduloHidromiel.observarSaldo(USUARIO_LOCAL).collectAsState(initial = 0)
            ContadorHidromiel(
                saldo = saldo,
                metal = EstilosMundo.porOrden(orden).metal,
                modifier = Modifier.hastaRG(510f, 72f, 200f).wrapContentWidth(Alignment.End)
            )
        }
    }
}

/** Título pequeño con letras espaciadas (Cinzel), p. ej. "RETO DE FIN DE MUNDO". */
@Composable
fun Rotulo(texto: String, modifier: Modifier, color: Color = RG.NaranjaTexto, tamano: Float = 13.5f, espacio: Float = 4f) {
    Text(texto, modifier, style = cinzel(tamano, FontWeight.Bold, espacio, color), maxLines = 1, softWrap = false)
}

/** Título grande en versalitas (Cinzel): "Ragnarök", "El mundo renace". */
@Composable
fun TituloRagnarok(texto: String, modifier: Modifier, tamano: Float = 52f, color: Color = RG.Crema) {
    val conCinzel = FuentesMockup.hayCinzel()
    Text(
        FuentesMockup.versalitas(texto, RG.d(tamano).sp, conCinzel),
        modifier,
        style = cinzel(tamano, FontWeight.Bold, 0f, color).copy(
            shadow = Shadow(Color.Black.copy(alpha = 0.7f), Offset(0f, 2f), 6f)
        ),
        maxLines = 1,
        softWrap = false
    )
}

/** Botón principal con puntas a los lados y degradado salvia → oro → naranja. */
@Composable
fun BotonRagnarok(texto: String, modifier: Modifier, habilitado: Boolean = true, accion: () -> Unit) {
    Box(
        modifier
            .tamRG(474f, 63f)
            .alpha(if (habilitado) 1f else 0.45f)
            .drawBehind {
                val r = Rect(Offset.Zero, size)
                val camino = caminoPuntas(r)
                if (habilitado) {
                    drawPath(caminoPuntas(r.inflate(6.dp.toPx())), Color(0xFFE8853A).copy(alpha = 0.10f))
                }
                drawPath(
                    camino,
                    Brush.horizontalGradient(listOf(Color(0xFFC6D3A2), Color(0xFFE6C46A), Color(0xFFEE9A44)))
                )
                drawPath(camino, Color(0xFFFFF0C8).copy(alpha = 0.75f), style = Stroke(1.dp.toPx()))
            }
            .pulsable(habilitado, accion),
        contentAlignment = Alignment.Center
    ) {
        TextoAjustable(
            texto,
            cinzel(23f, FontWeight.Black, 3.5f, Color(0xFF24140A)),
            Modifier.padding(horizontal = RG.d(46f).dp),
            minEscala = 0.7f
        )
    }
}

/** Rombo con una runa adentro (reglas de la entrada, temas por reforzar, diálogo). */
@Composable
fun RomboRuna(runa: String, modifier: Modifier, color: Color = RG.Naranja, lado: Float = 34f) {
    Box(
        modifier
            .tamRG(lado, lado)
            .drawBehind {
                val c = center
                val r = size.width / 2f
                val rombo = Path().apply {
                    moveTo(c.x, c.y - r); lineTo(c.x + r, c.y); lineTo(c.x, c.y + r); lineTo(c.x - r, c.y); close()
                }
                drawPath(rombo, color.copy(alpha = 0.14f))
                drawPath(rombo, color.copy(alpha = 0.85f), style = Stroke(1.2.dp.toPx()))
            },
        contentAlignment = Alignment.Center
    ) {
        Text(runa, style = TextStyle(fontSize = RG.d(lado * 0.42f).sp, color = color))
    }
}

/** Medallón del mundo para cualquier pantalla (radio en dp), p. ej. la barra de las flashcards. */
@Composable
fun MedallonVista(orden: Int, radio: Float, modifier: Modifier = Modifier, brilla: Boolean = false) {
    val tiempo = com.aesir.odin.ui.components.odin.rememberTiempo()
    Canvas(modifier.requiredSize((radio * 3f).dp)) {
        dibujarMedallon(orden, center, radio.dp.toPx(), brilla, if (brilla) tiempo.value else 0f)
    }
}
