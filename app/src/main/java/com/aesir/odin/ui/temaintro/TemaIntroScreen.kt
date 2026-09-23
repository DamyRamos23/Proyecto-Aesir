package com.aesir.odin.ui.temaintro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
fun TemaIntroScreen(
    temaId: String,
    onVolverAtras: () -> Unit,
    viewModel: TemaIntroViewModel = viewModel(
        factory = TemaIntroViewModelFactory(
            obtenerIntroduccionTemaUseCase = TemaIntroDependencyProvider.obtenerIntroduccionTemaUseCase,
            registrarIntroduccionVistaUseCase = TemaIntroDependencyProvider.registrarIntroduccionVistaUseCase
        )
    )
) {
    LaunchedEffect(temaId) {
        viewModel.cargarIntroduccion(temaId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
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
                    Text(
                        text = state.nombre,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.descripcion,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        TextButton(onClick = { viewModel.onCerrarPulsado(onVolverAtras) }) {
                            Text("Cerrar", color = Color.White)
                        }
                        TextButton(onClick = { viewModel.onOmitirPulsado(onVolverAtras) }) {
                            Text("Omitir", color = Color.White)
                        }
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
