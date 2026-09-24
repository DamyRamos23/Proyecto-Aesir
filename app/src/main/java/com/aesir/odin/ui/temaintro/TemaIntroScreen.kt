package com.aesir.odin.ui.temaintro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.aesir.odin.di.AppContainer
import com.aesir.odin.ui.components.HandPaintedTitle
import com.aesir.odin.ui.navigation.OdinNavGraph
import com.aesir.odin.ui.theme.ColorNocheOscura
import com.aesir.odin.ui.theme.ColorNodoDesbloqueado
import com.aesir.odin.ui.theme.ColorTextoClaro
import com.aesir.odin.ui.theme.ColorTextoTenue

@Composable
fun TemaIntroScreen(
    navController: NavHostController,
    temaId: String,
    appContainer: AppContainer
) {
    val viewModel: TemaIntroViewModel = viewModel(
        factory = TemaIntroViewModel.factory(
            appContainer.obtenerIntroduccionTemaUseCase,
            appContainer.registrarIntroduccionVistaUseCase,
            appContainer.leccionRepository
        )
    )

    LaunchedEffect(temaId) {
        viewModel.cargarIntroduccion(temaId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorNocheOscura),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is TemaIntroUiState.Cargando -> {
                CircularProgressIndicator(color = Color.White)
            }
            is TemaIntroUiState.Contenido -> {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HandPaintedTitle(
                        texto = state.nombre,
                        tamano = 32.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = state.descripcion,
                        color = ColorTextoClaro,
                        fontSize = 18.sp,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Button(
                        onClick = { 
                            viewModel.onComenzarLeccionPulsado { leccionId ->
                                navController.navigate(OdinNavGraph.rutaLeccionIntroCon(leccionId))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorNodoDesbloqueado,
                            contentColor = ColorTextoClaro
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Comenzar lección", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.onCerrarPulsado { navController.popBackStack() } }) {
                        Text("Volver al Roadmap", color = ColorTextoTenue)
                    }
                }
            }
            is TemaIntroUiState.Error -> {
                Text(
                    text = "No se pudo cargar la introduccion del tema",
                    color = Color.White
                )
            }
        }
    }
}
