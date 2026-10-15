package com.aesir.odin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.ui.components.odin.AvisosHidromiel
import com.aesir.odin.ui.navigation.OdinNavGraph
import com.aesir.odin.ui.preguntaflash.PreguntaFlashScreen
import com.aesir.odin.ui.preguntaflash.PreguntaFlashViewModel
import com.aesir.odin.ui.theme.ColorNocheOscura
import com.aesir.odin.ui.theme.ODINTheme

class MainActivity : ComponentActivity() {

    private val appContainer by lazy { AppContainer(applicationContext) }

    // CU-06: vive en la actividad para contar el tiempo de uso de toda la app.
    private val preguntaFlashViewModel: PreguntaFlashViewModel by viewModels {
        PreguntaFlashViewModel.factory(appContainer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ODINTheme {
                val navController = rememberNavController()

                // La pregunta flash solo puede interrumpir Mundos y Roadmap (RF-39).
                val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route
                LaunchedEffect(rutaActual) {
                    preguntaFlashViewModel.onPantallaCambiada(rutaActual)
                }
                LaunchedEffect(Unit) {
                    preguntaFlashViewModel.iniciarTemporizador()
                }

                Scaffold(modifier = Modifier.fillMaxSize(), containerColor = ColorNocheOscura) { innerPadding ->
                    Box(Modifier.fillMaxSize().padding(innerPadding)) {
                        OdinNavGraph.construirGrafo(
                            nav = navController,
                            appContainer = appContainer
                        )
                        // MOD-01: avisos de hidromiel sobre cualquier pantalla.
                        // Al abrir la app en un nuevo día se entrega la recompensa diaria (E3).
                        AvisosHidromiel(
                            avisos = appContainer.moduloHidromiel.avisos,
                            alSuscribir = { appContainer.moduloHidromiel.reclamarRecompensaDiaria(USUARIO_LOCAL) },
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }

                PreguntaFlashScreen(preguntaFlashViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        preguntaFlashViewModel.reanudar()
    }

    override fun onStop() {
        preguntaFlashViewModel.pausar()
        super.onStop()
    }
}
