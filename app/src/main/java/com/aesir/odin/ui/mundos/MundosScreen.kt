package com.aesir.odin.ui.mundos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.ui.components.HandPaintedTitle
import com.aesir.odin.ui.navigation.OdinNavGraph
import com.aesir.odin.ui.theme.ColorConexionActiva
import com.aesir.odin.ui.theme.ColorConexionInactiva
import com.aesir.odin.ui.theme.ColorFondoNodo
import com.aesir.odin.ui.theme.ColorNocheOscura

/**
 * Pantalla que lista los mundos del roadmap. Al tocar un mundo, navega a
 * RoadmapScreen pasando su id como "nivelId"; es RoadmapScreen quien valida
 * si ese nivel está desbloqueado, tal como lo describe el diagrama de
 * secuencia.
 */
@Composable
fun MundosScreen(
    navController: NavHostController,
    appContainer: AppContainer
) {
    val viewModel: MundosViewModel = viewModel(
        factory = MundosViewModel.factory(appContainer.obtenerMundosUseCase)
    )
    val mundos by viewModel.mundos.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarMundos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorNocheOscura)
            .padding(24.dp)
    ) {
        HandPaintedTitle(
            texto = "Mundos",
            modifier = Modifier.padding(bottom = 16.dp),
            tamano = 40.sp
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(mundos) { mundo ->
                MundoItem(
                    mundo = mundo,
                    onClick = {
                        navController.navigate(OdinNavGraph.rutaRoadmapCon(mundo.id))
                    }
                )
            }
        }
    }
}

@Composable
private fun MundoItem(mundo: Mundo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorFondoNodo)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(end = 32.dp)) {
                Text(
                    text = mundo.nombre,
                    color = if (mundo.desbloqueado) ColorConexionActiva else ColorConexionInactiva
                )
                Text(
                    text = mundo.descripcion,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            if (!mundo.desbloqueado) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Mundo bloqueado",
                    tint = ColorConexionInactiva,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}
