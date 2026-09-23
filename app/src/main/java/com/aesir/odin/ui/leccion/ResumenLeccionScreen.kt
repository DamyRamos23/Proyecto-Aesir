package com.aesir.odin.ui.leccion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.components.HandPaintedTitle
import com.aesir.odin.ui.theme.ColorChispasDoradas
import com.aesir.odin.ui.theme.ColorFondoNodo
import com.aesir.odin.ui.theme.ColorNocheOscura
import com.aesir.odin.ui.theme.ColorNodoDesbloqueado
import com.aesir.odin.ui.theme.ColorRespuestaCorrecta
import com.aesir.odin.ui.theme.ColorRespuestaIncorrecta
import com.aesir.odin.ui.theme.ColorTextoClaro
import com.aesir.odin.ui.theme.ColorTextoTenue
import com.aesir.odin.ui.theme.ODINTheme

/** Resumen de resultados de una lección completada (RF-21, RF-22). */
@Composable
fun ResumenLeccionScreen(
    puntaje: Int,
    errores: Int,
    totalEjercicios: Int,
    onVolverRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aciertos = (totalEjercicios - errores).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorNocheOscura)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HandPaintedTitle(texto = "¡Lección completada!", tamano = 34.sp)
        Spacer(Modifier.height(32.dp))

        Text("Puntaje", color = ColorTextoTenue, fontSize = 16.sp)
        Text("$puntaje", color = ColorChispasDoradas, fontSize = 72.sp, fontWeight = FontWeight.Bold)
        Text("de 100", color = ColorTextoTenue, fontSize = 14.sp)

        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            TarjetaDato("Aciertos", "$aciertos", ColorRespuestaCorrecta, Modifier.weight(1f))
            TarjetaDato("Errores", "$errores", ColorRespuestaIncorrecta, Modifier.weight(1f))
            TarjetaDato("Ejercicios", "$totalEjercicios", ColorTextoClaro, Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Tu avance en el roadmap se actualizó y el siguiente tema quedó desbloqueado.",
            color = ColorTextoTenue,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onVolverRoadmap,
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorNodoDesbloqueado,
                contentColor = ColorTextoClaro
            ),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Volver al roadmap", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TarjetaDato(etiqueta: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(12.dp), color = ColorFondoNodo, modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(valor, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(etiqueta, color = ColorTextoTenue, fontSize = 13.sp)
        }
    }
}

@Preview
@Composable
private fun ResumenLeccionPreview() {
    ODINTheme(darkTheme = true, dynamicColor = false) {
        ResumenLeccionScreen(puntaje = 80, errores = 1, totalEjercicios = 5, onVolverRoadmap = {})
    }
}
