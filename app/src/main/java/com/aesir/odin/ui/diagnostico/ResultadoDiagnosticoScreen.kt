package com.aesir.odin.ui.diagnostico

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.aesir.odin.ui.theme.OdinPaleta

/**
 * CU-10, RF-91: resultado de la evaluación y temas reconocidos como
 * conocidos (ResultadoDiagnosticoScreen del diagrama de clases). Observa el
 * resultado del mismo EvaluacionDiagnosticaViewModel. No muestra puntos
 * porque el diagnóstico no los otorga (RF-99).
 */
@Composable
fun ResultadoDiagnosticoScreen(
    viewModel: EvaluacionDiagnosticaViewModel,
    onIrAlMundo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resultado by viewModel.resultado.collectAsState()
    val mundo by viewModel.mundo.collectAsState()
    val nombresTemas by viewModel.nombresTemas.collectAsState()
    val estilo = EstilosMundo.porOrden(mundo?.orden ?: 1)

    // La evaluación ya se registró: atrás lleva al mundo, no a las preguntas
    BackHandler(onBack = onIrAlMundo)

    FondoMundo(estilo = estilo, alturaPaisaje = 420.dp, modifier = modifier) {
        val actual = resultado
        if (actual == null) {
            CircularProgressIndicator(color = estilo.acento, modifier = Modifier.align(Alignment.Center))
            return@FondoMundo
        }
        val conocidos = actual.temasConocidos.map { nombresTemas[it] ?: it }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(Modifier.size(88.dp)) {
                val r = size.minDimension / 2f
                mancha(center, r * 1.6f, estilo.acento, .35f)
                drawCircle(Color.Black.copy(alpha = .6f), r)
                drawCircle(estilo.metal, r, style = Stroke(2.dp.toPx()))
                dibujarEmblema(estilo.emblema, center, r * .7f, estilo.acento)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "EVALUACIÓN COMPLETADA",
                color = estilo.acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 3.sp
            )
            Text(
                (mundo?.nombre ?: "Diagnóstico").uppercase(),
                style = estiloTitulo(28.sp, Color(0xFFF3E2BC), brillo = estilo.acento),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            PanelTallado(estilo = estilo, semilla = 13, modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${actual.aciertos}/${actual.totalPreguntas}", color = estilo.acento, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 34.sp)
                    Text("respuestas correctas", color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(18.dp))
            Column(Modifier.widthIn(max = 460.dp).fillMaxWidth()) {
                if (conocidos.isEmpty()) {
                    // A7.1 / RF-98
                    Text(
                        "No se reconocieron temas adicionales. Comenzarás desde el primer nivel disponible del mundo.",
                        color = OdinPaleta.HuesoTenue,
                        fontFamily = FuenteTexto,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        if (conocidos.size == 1) "Tema reconocido como conocido:" else "Temas reconocidos como conocidos:",
                        color = OdinPaleta.Hueso,
                        fontFamily = FuenteTitulo,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    conocidos.forEach { TemaConocido(estilo, it) }
                    Text(
                        "Ya cuentan como superados y los niveles conectados quedan abiertos.",
                        color = OdinPaleta.HuesoTenue,
                        fontFamily = FuenteTexto,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                Text(
                    "La evaluación diagnóstica no otorga hidromiel.",
                    color = OdinPaleta.HuesoTenue,
                    fontFamily = FuenteTexto,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                )
            }

            Spacer(Modifier.height(26.dp))
            Box(Modifier.widthIn(max = 460.dp).fillMaxWidth()) {
                BotonOdin(texto = "Ir al mundo", onClick = onIrAlMundo, acento = estilo.acento, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun TemaConocido(estilo: EstiloMundo, nombre: String) {
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(10.dp)) { drawCircle(estilo.acento) }
        Spacer(Modifier.width(10.dp))
        Text(nombre, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 16.sp)
    }
}
