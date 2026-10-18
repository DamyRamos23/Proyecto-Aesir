package com.aesir.odin.ui.retoarrastrar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.PanelTallado
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo

/**
 * Paso 9 del CU-09: resultado del reto aprobado con el puntaje, el número de
 * intentos y los niveles que se abrieron. Lo ganado en hidromiel lo muestra
 * el aviso global del MOD-01.
 */
@Composable
fun ResumenRetoScreen(
    puntaje: Int,
    intentos: Int,
    desbloqueados: List<String>,
    ordenMundo: Int,
    onVolverRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estilo = EstilosMundo.porOrden(ordenMundo)
    FondoMundo(estilo = estilo, alturaPaisaje = 420.dp, modifier = modifier) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(24.dp))
            Canvas(Modifier.size(96.dp)) {
                val r = size.minDimension / 2f
                mancha(center, r * 1.6f, estilo.acento, .35f)
                drawCircle(Color.Black.copy(alpha = .6f), r)
                drawCircle(estilo.metal, r, style = Stroke(2.dp.toPx()))
                dibujarEmblema(estilo.emblema, center, r * .7f, estilo.acento)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "RETO SUPERADO",
                color = estilo.acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 3.sp
            )
            Text(
                "LAS RUNAS ENCAJARON",
                style = estiloTitulo(28.sp, Color(0xFFF3E2BC), brillo = estilo.acento),
                textAlign = TextAlign.Center
            )
            Text(
                if (intentos <= 1) "Todo en su lugar al primer intento." else "Lo lograste reacomodando las piedras.",
                color = Color(0xFFD8CFBE),
                fontFamily = FuenteTexto,
                fontStyle = FontStyle.Italic,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(24.dp))
            PanelTallado(estilo = estilo, semilla = 33, modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Dato(estilo, "$puntaje", "puntaje")
                    Dato(estilo, "$intentos", if (intentos == 1) "intento" else "intentos")
                }
            }
            if (desbloqueados.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                PanelTallado(estilo = estilo, semilla = 34, modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "NIVELES DESBLOQUEADOS",
                            color = estilo.acento,
                            fontFamily = FuenteTitulo,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp
                        )
                        desbloqueados.forEach { nombre ->
                            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = estilo.acento, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(nombre, color = Color(0xFFE9E1D0), fontFamily = FuenteTexto, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Box(Modifier.widthIn(max = 440.dp).fillMaxWidth()) {
                BotonOdin(texto = "Volver al roadmap", onClick = onVolverRoadmap, acento = estilo.acento, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Dato(estilo: EstiloMundo, valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = estilo.acento, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Text(etiqueta, color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 14.sp)
    }
}
