package com.aesir.odin.ui.volva

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.SelloRunico
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.OdinPaleta
import com.aesir.odin.ui.theme.Text
import kotlin.math.cos
import kotlin.math.sin

/**
 * CU-12, paso 1: cara de la moneda de Mundos que lleva a «El caldero del
 * destino». Sigue el mismo trazo que el reverso del duelo de enigmas, con el
 * estilo de hierro de la pantalla de la Völva y la runa Perthro (el destino).
 */
@Composable
fun CaraMonedaVolva(onRetar: () -> Unit) {
    val estilo = EstilosMundo.Hierro
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Disco de la moneda: el caldero visto desde arriba
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            drawCircle(Brush.radialGradient(listOf(Color(0xFF2A2420), Color(0xFF0E0C0B)), center, r), r)
            drawCircle(estilo.metal, r - 3.dp.toPx(), style = Stroke(5.dp.toPx()))
            drawCircle(estilo.metal.copy(alpha = .4f), r - 16.dp.toPx(), style = Stroke(1.5.dp.toPx()))
            // Brasas alrededor del borde
            for (k in 0 until 24) {
                val a = Math.toRadians(k * 15.0)
                val p = Offset(center.x + (r - 10.dp.toPx()) * cos(a).toFloat(), center.y + (r - 10.dp.toPx()) * sin(a).toFloat())
                drawCircle(OdinPaleta.Brasa.copy(alpha = if (k % 2 == 0) .75f else .35f), 2.dp.toPx(), p)
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SelloRunico(runa = "ᛈ", estilo = estilo, tamano = 64.dp)
            Spacer(Modifier.height(10.dp))
            Text("EL CALDERO DEL DESTINO", style = estiloTitulo(12.sp, color = estilo.metal), maxLines = 1)
            Text("Reta a la Völva", style = estiloTitulo(24.sp, brillo = estilo.acento), maxLines = 1)
            Text(
                "Apuesta hidromiel y lee las visiones de las tres Nornas.",
                color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 14.sp, textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
            )
            BotonOdin("Retar a la bruja", onRetar, OdinPaleta.Brasa, metal = estilo.metal)
        }
    }
}
