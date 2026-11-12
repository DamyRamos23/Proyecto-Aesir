package com.aesir.odin.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.aesir.odin.R
import com.aesir.odin.domain.heimdall.Alineacion
import com.aesir.odin.domain.heimdall.EstiloNormal
import com.aesir.odin.domain.heimdall.PresentacionLectura
import com.aesir.odin.domain.heimdall.TipoEnfasis
import androidx.compose.material3.Text as TextoMaterial

/** MOD-03: presentación de lectura vigente; la provee MainActivity para toda la app. */
val LocalPresentacionLectura = compositionLocalOf { EstiloNormal().crearPresentacion() }

private val FuenteLexend = FontFamily(
    Font(R.font.lexend_regular, FontWeight.Normal),
    Font(R.font.lexend_bold, FontWeight.Bold)
)

private fun colorDe(hex: String) = Color(0xFF000000 or hex.removePrefix("#").toLong(16))

/**
 * Sustituye a `material3.Text` en todas las pantallas (mismo nombre y
 * parámetros, solo cambia el import). Con la presentación normal no toca
 * nada; con Visión de Heimdall aplica la tabla del MOD-03 sobre lo que haya
 * definido la pantalla.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val p = LocalPresentacionLectura.current
    if (!p.activa) {
        TextoMaterial(
            text = text, modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
            fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
            textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight, overflow = overflow,
            softWrap = softWrap, maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style
        )
        return
    }
    Text(
        text = AnnotatedString(text), modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
        fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
        textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight, overflow = overflow,
        softWrap = softWrap, maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style
    )
}

@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val p = LocalPresentacionLectura.current
    if (!p.activa) {
        TextoMaterial(
            text = text, modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
            fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
            textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight, overflow = overflow,
            softWrap = softWrap, maxLines = maxLines, minLines = minLines, inlineContent = inlineContent,
            onTextLayout = onTextLayout, style = style
        )
        return
    }
    val tamanoBase = when {
        fontSize.isSpecified -> fontSize
        style.fontSize.isSpecified -> style.fontSize
        else -> 16.sp
    }
    // Texto de una línea (rótulos, títulos, botones): suele vivir en una caja fija de
    // las pantallas maquetadas, así que no se parte; crece hasta la escala y se encoge si no cabe.
    if (maxLines == 1 || !softWrap) {
        val estilo = style.merge(
            TextStyle(
                color = color, fontSize = fontSize, fontWeight = fontWeight, fontStyle = fontStyle,
                fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration,
                textAlign = textAlign ?: TextAlign.Unspecified, lineHeight = lineHeight
            )
        )
        TextoEnCaja(text, estilo, modifier, maxLineas = 1, minEscala = 0.6f)
        return
    }
    val enfatizado = fontStyle == FontStyle.Italic || style.fontStyle == FontStyle.Italic || esMayusculas(text.text)
    val peso = if (enfatizado && p.enfasis == TipoEnfasis.NEGRITAS) FontWeight.Bold else fontWeight ?: style.fontWeight
    val colorTexto = p.colorTexto?.let(::colorDe) ?: color
    TextoMaterial(
        text = adaptar(text, p),
        modifier = p.colorFondo?.let { modifier.background(colorDe(it)) } ?: modifier,
        color = colorTexto,
        fontSize = tamanoBase * p.escalaTexto,
        fontStyle = FontStyle.Normal,
        fontWeight = peso,
        fontFamily = if (p.fuente != null) FuenteLexend else fontFamily,
        letterSpacing = p.espacioLetras.em,
        textDecoration = textDecoration,
        textAlign = if (p.alineacion == Alineacion.IZQUIERDA) TextAlign.Start else textAlign,
        lineHeight = p.interlineado?.em ?: lineHeight,
        overflow = TextOverflow.Clip,
        // E1: nunca se recorta, el texto pasa a la siguiente línea.
        softWrap = true,
        maxLines = if (p.permitirAjusteLinea) Int.MAX_VALUE else maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        // Con colores propios del modo, las sombras y brillos de los títulos ensucian el texto.
        style = if (p.colorTexto != null) style.copy(color = colorTexto, shadow = null) else style
    )
}

/**
 * Texto con Visión de Heimdall dentro de una caja de tamaño fijo (lo usan
 * `TextoAjustable` y los textos de una línea): Lexend, espaciado y negritas
 * del modo; el tamaño sube hasta la escala del modo y baja hasta [minEscala]
 * del original si no cabe. No se recorta ni invade a los vecinos.
 */
@Composable
fun TextoEnCaja(texto: AnnotatedString, estilo: TextStyle, modifier: Modifier, maxLineas: Int, minEscala: Float) {
    val p = LocalPresentacionLectura.current
    val tamano = if (estilo.fontSize.isSpecified) estilo.fontSize else 16.sp
    val enfatizado = estilo.fontStyle == FontStyle.Italic || esMayusculas(texto.text)
    BasicText(
        text = adaptar(texto, p),
        modifier = modifier,
        style = estilo.copy(
            color = estilo.color.takeOrElse { LocalContentColor.current },
            fontFamily = if (p.fuente != null) FuenteLexend else estilo.fontFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = if (enfatizado && p.enfasis == TipoEnfasis.NEGRITAS) FontWeight.Bold else estilo.fontWeight,
            letterSpacing = p.espacioLetras.em,
            lineHeight = if (maxLineas > 1) p.interlineado?.em ?: estilo.lineHeight else TextUnit.Unspecified
        ),
        maxLines = maxLineas,
        softWrap = maxLineas > 1,
        overflow = TextOverflow.Clip,
        autoSize = TextAutoSize.StepBased(
            minFontSize = tamano * minEscala,
            maxFontSize = tamano * p.escalaTexto,
            stepSize = 0.5.sp
        )
    )
}

/** Mayúsculas usadas como énfasis ("MUNDOS"): con el modo se cambian por negritas. */
private fun esMayusculas(texto: String): Boolean =
    texto.count { it.isLetter() } > 1 && texto.none { it.isLowerCase() } && texto.any { it.isUpperCase() }

/**
 * Espacio entre palabras (se suma a los espacios) y entre párrafos (una línea
 * vacía de [PresentacionLectura.espacioParrafos] veces el tamaño de la fuente).
 */
private fun adaptar(original: AnnotatedString, p: PresentacionLectura): AnnotatedString {
    val texto = if (esMayusculas(original.text)) AnnotatedString(oracion(original.text)) else original
    val espacio = SpanStyle(letterSpacing = (p.espacioLetras + p.espacioPalabras).em)
    // shortcut: el espacio entre párrafos solo se aplica a texto sin estilos por tramo;
    // si una pantalla los usa con varios párrafos, conserva sus estilos y el salto normal.
    if (!texto.text.contains('\n') || texto.spanStyles.isNotEmpty() || texto.paragraphStyles.isNotEmpty()) {
        return buildAnnotatedString {
            append(texto)
            texto.text.forEachIndexed { i, c -> if (c == ' ') addStyle(espacio, i, i + 1) }
        }
    }
    return buildAnnotatedString {
        val parrafos = texto.text.split('\n')
        parrafos.forEachIndexed { n, parrafo ->
            val inicio = length
            append(parrafo)
            parrafo.forEachIndexed { i, c -> if (c == ' ') addStyle(espacio, inicio + i, inicio + i + 1) }
            if (n < parrafos.lastIndex) {
                append('\n')
                withStyle(ParagraphStyle(lineHeight = p.espacioParrafos.em)) { append('​') }
                append('\n')
            }
        }
    }
}

private fun oracion(texto: String): String =
    texto.lowercase().replaceFirstChar { it.titlecase() }
