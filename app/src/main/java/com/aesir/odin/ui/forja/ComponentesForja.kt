package com.aesir.odin.ui.forja

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.IconoCuerno
import com.aesir.odin.ui.components.odin.PuntosProgreso
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta

/** Piezas visuales de la forja de Brokkr: marco dorado, pergamino e íconos de artefactos. */

internal val ColorPergamino = Color(0xFFEBDDBA)
internal val ColorPergaminoSombra = Color(0xFFCDB98C)
internal val ColorMarcoForja = Color(0xFF8C6A32)

/** Pantalla de la forja: fondo oscuro con marco dorado y contenido desplazable. */
@Composable
internal fun MarcoForja(contenido: @Composable ColumnScope.() -> Unit) {
    FondoMundo(estilo = null) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(10.dp)
                .border(1.5.dp, ColorMarcoForja, CutCornerShape(10.dp))
                .padding(4.dp)
                .border(0.8.dp, ColorMarcoForja.copy(alpha = .45f), CutCornerShape(8.dp))
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = contenido
                )
            }
        }
    }
}

/** Fila superior: volver (Menú o Atrás) a la izquierda y el hidromiel a la derecha. */
@Composable
internal fun BarraForja(textoVolver: String?, onVolver: () -> Unit, saldo: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (textoVolver != null) BotonVolver(textoVolver.uppercase(), onVolver)
        Spacer(Modifier.weight(1f))
        ContadorHidromiel(saldo)
    }
}

/** Título de etapa: «ETAPA 1 DE 3» y su nombre, con los tres puntos de progreso. */
@Composable
internal fun EncabezadoEtapa(numero: Int, nombre: String) {
    Text(
        "ETAPA $numero DE 3",
        color = OdinPaleta.Oro,
        fontFamily = FuenteTitulo,
        fontSize = 14.sp,
        letterSpacing = 2.sp
    )
    Text(nombre.uppercase(), style = estiloTitulo(26.sp, OdinPaleta.Brasa), textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    PuntosProgreso(total = 3, actual = numero - 1, acento = OdinPaleta.Brasa, metal = OdinPaleta.Oro)
    Spacer(Modifier.height(16.dp))
}

/** Tarjeta de pergamino con texto oscuro. */
@Composable
internal fun Pergamino(
    modifier: Modifier = Modifier,
    resaltado: Boolean = false,
    contenido: @Composable BoxScope.() -> Unit
) {
    val forma = CutCornerShape(6.dp)
    Box(
        modifier
            .clip(forma)
            .background(Brush.verticalGradient(listOf(ColorPergamino, ColorPergaminoSombra)))
            .border(if (resaltado) 2.5.dp else 1.dp, if (resaltado) OdinPaleta.Brasa else ColorMarcoForja, forma)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
        content = contenido
    )
}

/** Recuadro oscuro con borde dorado (indicadores como «Forjas de hoy»). */
@Composable
internal fun PlacaOscura(modifier: Modifier = Modifier, contenido: @Composable BoxScope.() -> Unit) {
    val forma = CutCornerShape(6.dp)
    Box(
        modifier
            .clip(forma)
            .background(Color(0xE60B0907))
            .border(1.dp, OdinPaleta.Oro.copy(alpha = .8f), forma)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
        content = contenido
    )
}

@Composable
internal fun TextoPergamino(texto: String, tamano: Int = 16, negrita: Boolean = false, modifier: Modifier = Modifier) {
    Text(
        texto,
        color = OdinPaleta.Tinta,
        fontFamily = FuenteTexto,
        fontSize = tamano.sp,
        lineHeight = (tamano + 5).sp,
        fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
internal fun TextoClaro(texto: String, tamano: Int = 15, color: Color = OdinPaleta.Hueso, modifier: Modifier = Modifier) {
    Text(
        texto,
        color = color,
        fontFamily = FuenteTexto,
        fontSize = tamano.sp,
        lineHeight = (tamano + 6).sp,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/** Cantidad de hidromiel con el cuerno: «120 HIDROMIEL», «+54 HIDROMIEL». */
@Composable
internal fun LineaHidromiel(texto: String, color: Color = OdinPaleta.Hueso) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        IconoCuerno(tamano = 22.dp)
        Spacer(Modifier.size(8.dp))
        Text(texto.uppercase(), color = color, fontFamily = FuenteTitulo, fontSize = 17.sp, letterSpacing = 1.sp)
    }
}

// ---------------------------------------------------------------------------
// Íconos de los artefactos
// ---------------------------------------------------------------------------

private val MetalClaro = Color(0xFFD9DCDF)
private val MetalMedio = Color(0xFF9EA4AA)
private val MetalOscuro = Color(0xFF555B61)
private val Cuero = Color(0xFF5A3A22)

/**
 * Dibujo del artefacto: Mjölnir (martillo), Gungnir (lanza), Draupnir
 * (anillo) o Skíðblaðnir (barco). Si [roto], aparece partido y agrietado.
 * Si [artefactoId] es null (forja solo por hidromiel) se dibuja el cuerno.
 */
@Composable
internal fun IconoArtefacto(artefactoId: String?, modifier: Modifier = Modifier, tamano: Dp = 120.dp, roto: Boolean = false, apagado: Boolean = false) {
    if (artefactoId == null) {
        Box(modifier.size(tamano), contentAlignment = Alignment.Center) { IconoCuerno(tamano = tamano * .7f) }
        return
    }
    Canvas(modifier.size(tamano)) {
        val alpha = if (apagado) .35f else 1f
        // Resplandor de la forja detrás del artefacto
        if (!apagado) {
            drawCircle(
                Brush.radialGradient(listOf(OdinPaleta.Brasa.copy(alpha = if (roto) .12f else .35f), Color.Transparent)),
                size.minDimension * .5f
            )
        }
        rotate(if (roto) -12f else -20f) {
            when (artefactoId) {
                "art_mjolnir" -> martillo(alpha, roto)
                "art_gungnir" -> lanza(alpha, roto)
                "art_draupnir" -> anillo(alpha, roto)
                else -> barco(alpha, roto)
            }
        }
        if (roto) {
            val grieta = Path().apply {
                moveTo(size.width * .38f, size.height * .18f)
                lineTo(size.width * .5f, size.height * .38f)
                lineTo(size.width * .43f, size.height * .5f)
                lineTo(size.width * .58f, size.height * .66f)
                lineTo(size.width * .52f, size.height * .82f)
            }
            drawPath(grieta, Color(0xFF1A1410), style = Stroke(size.minDimension * .035f, cap = StrokeCap.Round))
            drawPath(grieta, OdinPaleta.Incorrecto.copy(alpha = .6f), style = Stroke(size.minDimension * .012f, cap = StrokeCap.Round))
        }
    }
}

private fun metal(alpha: Float, w: Float) = Brush.linearGradient(
    listOf(MetalClaro.copy(alpha = alpha), MetalMedio.copy(alpha = alpha), MetalOscuro.copy(alpha = alpha)),
    start = Offset.Zero, end = Offset(w, w)
)

private fun DrawScope.martillo(alpha: Float, roto: Boolean) {
    val w = size.width
    val h = size.height
    // Mango con tiras de cuero
    drawRoundRect(Cuero.copy(alpha = alpha), Offset(w * .45f, h * .38f), Size(w * .1f, h * .5f), androidx.compose.ui.geometry.CornerRadius(w * .03f))
    for (i in 0..4) {
        drawLine(OdinPaleta.Oro.copy(alpha = .7f * alpha), Offset(w * .45f, h * (.45f + i * .08f)), Offset(w * .55f, h * (.42f + i * .08f)), w * .012f)
    }
    drawCircle(OdinPaleta.Oro.copy(alpha = alpha), w * .045f, Offset(w * .5f, h * .9f))
    // Cabeza
    val desplazamiento = if (roto) w * .05f else 0f
    drawRoundRect(metal(alpha, w), Offset(w * .2f - desplazamiento, h * .14f), Size(w * .6f, h * .26f), androidx.compose.ui.geometry.CornerRadius(w * .04f))
    drawRoundRect(MetalOscuro.copy(alpha = alpha), Offset(w * .2f - desplazamiento, h * .14f), Size(w * .6f, h * .26f), androidx.compose.ui.geometry.CornerRadius(w * .04f), style = Stroke(w * .015f))
    // Nudo rúnico al centro
    drawRect(OdinPaleta.Oro.copy(alpha = .85f * alpha), Offset(w * .43f - desplazamiento, h * .2f), Size(w * .14f, h * .14f), style = Stroke(w * .012f))
}

private fun DrawScope.lanza(alpha: Float, roto: Boolean) {
    val w = size.width
    val h = size.height
    val corte = if (roto) h * .06f else 0f
    drawLine(Cuero.copy(alpha = alpha), Offset(w * .5f, h * .36f + corte), Offset(w * .5f, h * .95f), w * .05f, StrokeCap.Round)
    drawLine(OdinPaleta.Oro.copy(alpha = alpha), Offset(w * .44f, h * .36f + corte), Offset(w * .56f, h * .36f + corte), w * .03f, StrokeCap.Round)
    val punta = Path().apply {
        moveTo(w * .5f, h * .04f)
        lineTo(w * .6f, h * .3f)
        lineTo(w * .5f, h * .36f)
        lineTo(w * .4f, h * .3f)
        close()
    }
    drawPath(punta, metal(alpha, w))
    drawPath(punta, MetalOscuro.copy(alpha = alpha), style = Stroke(w * .012f))
    drawLine(MetalOscuro.copy(alpha = alpha), Offset(w * .5f, h * .08f), Offset(w * .5f, h * .34f), w * .01f)
}

private fun DrawScope.anillo(alpha: Float, roto: Boolean) {
    val w = size.width
    val c = Offset(w * .5f, size.height * .55f)
    val r = w * .28f
    if (roto) {
        drawArc(OdinPaleta.Oro.copy(alpha = alpha), 20f, 300f, false, c - Offset(r, r), Size(r * 2, r * 2), style = Stroke(w * .09f, cap = StrokeCap.Round))
    } else {
        drawCircle(OdinPaleta.Oro.copy(alpha = alpha), r, c, style = Stroke(w * .09f))
    }
    drawCircle(Color(0xFFFFE9A8).copy(alpha = .6f * alpha), r, c, style = Stroke(w * .02f))
    // Gema
    drawCircle(Color(0xFFB23B3B).copy(alpha = alpha), w * .07f, c - Offset(0f, r))
    drawCircle(Color.White.copy(alpha = .5f * alpha), w * .02f, c - Offset(w * .02f, r + w * .02f))
}

private fun DrawScope.barco(alpha: Float, roto: Boolean) {
    val w = size.width
    val h = size.height
    val casco = Path().apply {
        moveTo(w * .1f, h * .6f)
        quadraticTo(w * .5f, h * .9f, w * .9f, h * .6f)
        lineTo(w * .78f, h * .6f)
        quadraticTo(w * .5f, h * .74f, w * .22f, h * .6f)
        close()
    }
    drawPath(casco, Cuero.copy(alpha = alpha))
    drawPath(casco, OdinPaleta.Oro.copy(alpha = alpha), style = Stroke(w * .012f))
    // Proa y popa curvas
    drawArc(OdinPaleta.Oro.copy(alpha = alpha), 180f, 120f, false, Offset(w * .02f, h * .42f), Size(w * .18f, h * .2f), style = Stroke(w * .025f))
    drawArc(OdinPaleta.Oro.copy(alpha = alpha), 240f, 120f, false, Offset(w * .8f, h * .42f), Size(w * .18f, h * .2f), style = Stroke(w * .025f))
    // Mástil y vela
    drawLine(Cuero.copy(alpha = alpha), Offset(w * .5f, h * .66f), Offset(w * .5f, h * .12f), w * .03f)
    val vela = Path().apply {
        moveTo(w * .28f, h * .2f)
        lineTo(w * .72f, h * .2f)
        lineTo(if (roto) w * .62f else w * .72f, h * .52f)
        lineTo(w * .28f, h * .52f)
        close()
    }
    drawPath(vela, metal(alpha, w))
    drawLine(Color(0xFFB23B3B).copy(alpha = alpha), Offset(w * .28f, h * .36f), Offset(w * .72f, h * .36f), w * .02f)
}
