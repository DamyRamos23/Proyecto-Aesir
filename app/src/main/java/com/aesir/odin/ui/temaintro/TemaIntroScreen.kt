package com.aesir.odin.ui.temaintro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.ui.components.HandPaintedTitle
import com.aesir.odin.ui.theme.ColorNocheOscura

/**
 * Destino al que navega RoadmapScreen cuando un tema está desbloqueado
 * (rutaIntroTema). Es una versión mínima: CU-02 "Consultar introducción
 * del tema" queda fuera del alcance de estos dos diagramas (CU-1), así que
 * aquí solo se muestra la información del tema y un botón para volver.
 */
@Composable
fun TemaIntroScreen(
    navController: NavHostController,
    temaId: String,
    appContainer: AppContainer
) {
    var tema by remember { mutableStateOf<Tema?>(null) }

    LaunchedEffect(temaId) {
        tema = appContainer.roadmapRepository.obtenerTema(temaId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorNocheOscura)
            .padding(24.dp)
    ) {
        HandPaintedTitle(
            texto = tema?.nombre ?: "Cargando...",
            modifier = Modifier.padding(bottom = 16.dp),
            tamano = 32.sp
        )
        Text(
            text = tema?.descripcion ?: "",
            color = Color.White.copy(alpha = 0.85f)
        )
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Volver al roadmap")
        }
    }
}
