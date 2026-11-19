package com.aesir.odin.ui.moneda

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.components.odin.rememberTiempo
import com.aesir.odin.ui.components.odin.ChipInicio
import com.aesir.odin.ui.components.odin.EscenaModo
import com.aesir.odin.ui.components.odin.PantallaInicioModo
import androidx.compose.ui.graphics.Color
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.math.sin

/**
 * Entrada de un caso de uso de la moneda que todavía no está implementado.
 * Muestra su cara del escudo meciéndose; cuando el modo exista, en
 * OdinNavGraph se cambia esta pantalla por la real.
 */
@Composable
fun CaraMonedaScreen(cara: CaraMoneda, onVolver: () -> Unit) {
    // Los modos con pantalla de inicio aprobada ya muestran su escena, aunque
    // el juego todavía no esté disponible.
    when (cara) {
        CaraMoneda.APUESTA_BROKKR -> return PantallaInicioModo(
            escena = EscenaModo.BROKKR,
            titulo = "La forja",
            lema = "Eitri martilla y Brokkr aviva el fuego. Forja un tesoro de los dioses… si la mosca de Loki te deja.",
            saldo = null,
            chips = listOf(ChipInicio("La apuesta de Brokkr", borde = Color(0x80FFB24A))),
            textoBoton = "Próximamente",
            botonHabilitado = false,
            onComenzar = {},
            onVolver = onVolver,
            aviso = "Este modo todavía se está forjando."
        )
        else -> Unit
    }
    val tiempo = rememberTiempo()
    val medidor = rememberTextMeasurer()
    val escenas = rememberEscenasMoneda()
    val runas = remember(medidor, cara) { cara.runas.map { medidor.measure(it.toString(), TextStyle(fontSize = 13.sp)) } }

    FondoMundo(estilo = null) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BotonVolver("Volver", onVolver, Modifier.align(Alignment.Start))
            Spacer(Modifier.weight(0.4f))
            // Resplandor
            Canvas(
                Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth(0.78f)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        val t = tiempo.value
                        translationY = sin(t * 1.4f) * size.height * 0.02f
                        rotationX = sin(t * 0.9f) * 30f
                        cameraDistance = 48f * density
                    }
            ) {
                val r = size.minDimension / 2f
                drawCircle(
                    Brush.radialGradient(
                        0.7f to cara.acento.copy(alpha = 0f),
                        0.85f to cara.acento.copy(alpha = 0.3f),
                        1f to cara.acento.copy(alpha = 0f),
                        center = center, radius = r
                    ),
                    r, center, blendMode = BlendMode.Plus
                )
                val m = r * 0.12f
                // La cara ocupa el lienzo menos el margen del resplandor
                inset(m) { dibujarCaraEscudo(cara, runas, escenas[cara], tiempo.value) }
            }
            Spacer(Modifier.height(28.dp))
            Text(cara.titulo.uppercase(), style = estiloTitulo(28.sp, OdinPaleta.Hueso, brillo = cara.acento).copy(textAlign = TextAlign.Center))
            Text(cara.subtitulo, color = cara.acento, fontFamily = FuenteTexto, fontStyle = FontStyle.Italic, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                cara.descripcion,
                color = OdinPaleta.HuesoTenue,
                fontFamily = FuenteTexto,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 420.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Este modo todavía se está forjando.",
                color = OdinPaleta.Escarcha,
                fontFamily = FuenteTexto,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(0.6f))
            BotonOdin(
                texto = "Volver al escudo",
                onClick = onVolver,
                acento = cara.acento,
                metal = cara.metal,
                primario = false,
                modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp)
            )
        }
    }
}
