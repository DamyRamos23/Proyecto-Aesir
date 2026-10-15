package com.aesir.odin.ui.memorama

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
 * CU-04, RF-16: muestra los puntos obtenidos y el tiempo empleado
 * (ResumenMemoramaScreen del diagrama de clases). Observa el resultado del
 * mismo MemoramaViewModel. Lo ganado en hidromiel lo muestra el aviso global
 * del MOD-01.
 */
@Composable
fun ResumenMemoramaScreen(
    viewModel: MemoramaViewModel,
    onVolverRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resultado by viewModel.resultado.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
    val estilo = EstilosMundo.porOrden(contexto?.mundo?.orden ?: 1)

    // El memorama ya terminó: atrás regresa al roadmap, no al tablero
    BackHandler(onBack = onVolverRoadmap)

    FondoMundo(estilo = estilo, alturaPaisaje = 420.dp, modifier = modifier) {
        val actual = resultado
        if (actual == null) {
            CircularProgressIndicator(color = estilo.acento, modifier = Modifier.align(Alignment.Center))
            return@FondoMundo
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(Modifier.size(96.dp)) {
                val r = size.minDimension / 2f
                mancha(center, r * 1.6f, estilo.acento, .35f)
                drawCircle(Color.Black.copy(alpha = .6f), r)
                drawCircle(estilo.metal, r, style = Stroke(2.dp.toPx()))
                dibujarEmblema(estilo.emblema, center, r * .7f, estilo.acento)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "MEMORAMA COMPLETADO",
                color = estilo.acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 3.sp
            )
            Text(
                "TODOS LOS PARES ENCONTRADOS",
                style = estiloTitulo(26.sp, Color(0xFFF3E2BC), brillo = estilo.acento),
                textAlign = TextAlign.Center
            )
            Text(
                mensajeSegun(actual.puntos),
                color = Color(0xFFD8CFBE),
                fontFamily = FuenteTexto,
                fontStyle = FontStyle.Italic,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(24.dp))
            PanelTallado(estilo = estilo, semilla = 17, modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Dato(estilo, "${actual.puntos}", "puntos")
                    Dato(estilo, "%d:%02d".format(actual.tiempoEmpleadoSeg / 60, actual.tiempoEmpleadoSeg % 60), "tiempo")
                }
            }
            Spacer(Modifier.height(28.dp))
            Box(Modifier.widthIn(max = 440.dp).fillMaxWidth()) {
                BotonOdin(texto = "Volver al roadmap", onClick = onVolverRoadmap, acento = estilo.acento, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun mensajeSegun(puntos: Int) = when {
    puntos >= 90 -> "Memoria de cuervo: Odín estaría orgulloso."
    puntos >= 50 -> "Buen ritmo. La próxima vez, aún más rápido."
    else -> "Lo lograste. Repasa los conceptos y vuelve a intentarlo."
}

@Composable
private fun Dato(estilo: EstiloMundo, valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = estilo.acento, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Text(etiqueta, color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 14.sp)
    }
}
