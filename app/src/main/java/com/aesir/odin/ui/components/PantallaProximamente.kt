package com.aesir.odin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aesir.odin.ui.theme.ColorNocheOscura

/**
 * Placeholder compartido para los destinos del grafo que todavía no
 * corresponden a CU-1 (lección, resumen de lección, pregunta flash).
 */
@Composable
fun PantallaProximamente(navController: NavHostController, titulo: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorNocheOscura)
            .padding(24.dp)
    ) {
        Text(text = titulo, color = Color.White)
        Text(text = "Próximamente", color = Color.White.copy(alpha = 0.6f))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Volver")
        }
    }
}
