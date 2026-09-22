package com.aesir.odin.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.theme.ColorRosaPintado
import com.aesir.odin.ui.theme.ColorSombraPintado

/**
 * Título con efecto "pintado a mano con brocha", como en la referencia
 * "LA BOLA NEGRA": letras gruesas, ligeramente irregulares, con una sombra
 * desplazada que simula la textura del trazo.
 *
 * TODO: importar una tipografía tipo brocha/pincel y colocarla en res/font,
 * por ejemplo "Permanent Marker", "Caveat" o "Kalam" (Google Fonts). Mientras
 * tanto se usa FontFamily.Default como placeholder.
 */
private val FuentePincel = FontFamily.Default // TODO: reemplazar por la fuente pincel real

@Composable
fun HandPaintedTitle(
    texto: String,
    modifier: Modifier = Modifier,
    tamano: androidx.compose.ui.unit.TextUnit = 56.sp,
    colorTexto: Color = ColorRosaPintado,
    colorSombra: Color = ColorSombraPintado
) {
    Box(modifier = modifier) {
        // Capa de sombra, desplazada, simula grosor/textura del trazo
        Text(
            text = texto,
            style = TextStyle(
                fontFamily = FuentePincel,
                fontSize = tamano,
                color = colorSombra
            ),
            modifier = Modifier.offset(x = 3.dp, y = 3.dp)
        )
        // Capa principal
        Text(
            text = texto,
            style = TextStyle(
                fontFamily = FuentePincel,
                fontSize = tamano,
                color = colorTexto
            )
        )
    }
}
