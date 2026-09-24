package com.aesir.odin.ui.roadmap

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.ui.components.HandPaintedTitle
import com.aesir.odin.ui.navigation.OdinNavGraph
import com.aesir.odin.ui.theme.ColorConexionActiva
import com.aesir.odin.ui.theme.ColorConexionInactiva
import com.aesir.odin.ui.theme.ColorFondoNodo
import com.aesir.odin.ui.theme.ColorNocheOscura
import com.aesir.odin.ui.theme.ColorNodoCompletado
import kotlinx.coroutines.launch

/**
 * Pantalla del roadmap de un mundo: valida el acceso al nivel al entrar,
 * carga sus temas si está desbloqueado, y al tocar un tema valida su
 * acceso antes de navegar a la introducción. Reproduce el diagrama de
 * secuencia de CU-1.
 */
@Composable
fun RoadmapScreen(
    navController: NavHostController,
    mundoId: String,
    appContainer: AppContainer
) {
    val viewModel: RoadmapViewModel = viewModel(
        factory = RoadmapViewModel.factory(
            appContainer.obtenerTemasPorMundoUseCase,
            appContainer.validarAccesoTemaUseCase,
            appContainer.validarAccesoNivelUseCase
        )
    )
    val temas by viewModel.temas.collectAsState()
    val mensajeBloqueo by viewModel.mensajeBloqueo.collectAsState()
    val scope = rememberCoroutineScope()

    var nivelDesbloqueado by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(mundoId) {
        val desbloqueado = viewModel.validarAccesoNivel(mundoId)
        nivelDesbloqueado = desbloqueado
        if (desbloqueado) {
            viewModel.cargarRoadmap(mundoId)
        }
    }

    if (mensajeBloqueo != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.limpiarMensajeBloqueo()
                if (nivelDesbloqueado == false) navController.popBackStack()
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.limpiarMensajeBloqueo()
                    if (nivelDesbloqueado == false) navController.popBackStack()
                }) {
                    Text("Entendido")
                }
            },
            title = { Text("Bloqueado") },
            text = { Text(mensajeBloqueo ?: "") }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorNocheOscura)
            .padding(24.dp)
    ) {
        HandPaintedTitle(
            texto = "Roadmap",
            modifier = Modifier.padding(bottom = 16.dp),
            tamano = 40.sp
        )

        if (nivelDesbloqueado == true) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(temas) { tema ->
                    TemaItem(
                        tema = tema,
                        onClick = {
                            scope.launch {
                                val abierto = viewModel.intentarAbrirTema(tema.id)
                                if (abierto) {
                                    navController.navigate(OdinNavGraph.rutaIntroTemaCon(tema.id))
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemaItem(tema: Tema, onClick: () -> Unit) {
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
                    text = tema.nombre,
                    color = if (tema.desbloqueado) ColorConexionActiva else ColorConexionInactiva
                )
                Text(
                    text = tema.descripcion,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            val icono = when {
                tema.completado -> Icons.Filled.CheckCircle
                !tema.desbloqueado -> Icons.Filled.Lock
                else -> null
            }
            if (icono != null) {
                Icon(
                    imageVector = icono,
                    contentDescription = if (tema.completado) "Tema completado" else "Tema bloqueado",
                    tint = if (tema.completado) ColorNodoCompletado else ColorConexionInactiva,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}
