package com.aesir.odin

import com.aesir.odin.domain.sonido.pistaParaRuta
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aesir.odin.ui.theme.LocalPresentacionLectura
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
import com.aesir.odin.ui.racha.AvisosRacha
import com.aesir.odin.domain.racha.AplicacionAbierta
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private companion object {
        /** Lo que se limpia al abrir la app corre una vez por proceso. */
        var limpiezaAlAbrirHecha = false
    }


    private val appContainer by lazy { AppContainer(applicationContext) }

    // CU-06: vive en la actividad para contar el tiempo de uso de toda la app.
    private val preguntaFlashViewModel: PreguntaFlashViewModel by viewModels {
        PreguntaFlashViewModel.factory(appContainer)
    }

    // MOD-02, paso 8: al pasar a segundo plano (o si entra una llamada) se detienen
    // los efectos y no se reanudan al volver.
    override fun onStart() {
        super.onStart()
        appContainer.moduloSonido.alVolverAPrimerPlano()
    }

    override fun onStop() {
        appContainer.moduloSonido.alPasarASegundoPlano()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // MOD-03, paso 4: se lee la preferencia antes de dibujar la primera pantalla.
        appContainer.moduloVisionHeimdall.alAbrirAplicacion()
        // MOD-02, paso 1: catálogo de efectos y preferencias de sonido.
        appContainer.moduloSonido.alAbrirAplicacion()
        // MOD-02, disparador 4: cada movimiento de hidromiel suena (cuerno que se llena o se vacía).
        lifecycleScope.launch { appContainer.moduloHidromiel.avisos.collect { appContainer.moduloSonido.avisarHidromiel(it) } }
        // Solo al arrancar el proceso (no al girar la pantalla, cuando la partida sigue viva en su ViewModel):
        // CU-13 E7.2: un duelo que quedó en curso al cerrarse la app se registra como abandonado.
        // CU-02 RF-45: una forja que quedó en curso al cerrarse la app queda rota.
        if (!limpiezaAlAbrirHecha) {
            limpiezaAlAbrirHecha = true
            lifecycleScope.launch {
                runCatching { appContainer.abandonarDueloUseCase.cerrarDueloSinTerminar() }
                runCatching { appContainer.cerrarForjasInconclusasUseCase() }
            }
        }
        setContent {
            val presentacion by appContainer.moduloVisionHeimdall.presentacion.collectAsState()
            ODINTheme { CompositionLocalProvider(LocalPresentacionLectura provides presentacion) {
                val navController = rememberNavController()

                // La pregunta flash solo puede interrumpir Mundos y Roadmap (RF-39).
                val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route
                LaunchedEffect(rutaActual) {
                    preguntaFlashViewModel.onPantallaCambiada(rutaActual)
                    // MOD-02: cada tipo de pantalla tiene su música (con fundido al cambiar)
                    appContainer.moduloSonido.cambiarPista(pistaParaRuta(rutaActual))
                }
                LaunchedEffect(Unit) {
                    preguntaFlashViewModel.iniciarTemporizador()
                }

                Scaffold(modifier = Modifier.fillMaxSize(), containerColor = ColorNocheOscura) { innerPadding ->
                    Box(Modifier.fillMaxSize().padding(innerPadding)) {
                        OdinNavGraph.construirGrafo(
                            nav = navController,
                            appContainer = appContainer,
                            onInvocarPreguntaFlash = preguntaFlashViewModel::invocar
                        )
                        // MOD-01: avisos de hidromiel sobre cualquier pantalla.
                        // Al abrir la app en un nuevo día se entrega la recompensa diaria (E3).
                        AvisosHidromiel(
                            avisos = appContainer.moduloHidromiel.avisos,
                            alSuscribir = { appContainer.moduloHidromiel.reclamarRecompensaDiaria(USUARIO_LOCAL) },
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                        // MOD-05: avisos de la racha. Al abrir la app se revisa el día (RN-26: no suma racha).
                        AvisosRacha(
                            avisos = appContainer.moduloCarreraSol.avisos,
                            alSuscribir = {
                                avisosRachaListos = true
                                registrarAperturaRacha()
                            },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }

                PreguntaFlashScreen(preguntaFlashViewModel)
            } }
        }
    }

    /** Día en que ya se avisó la apertura al MOD-05 (se repite solo si cambia el día). */
    private var ultimaAperturaRacha: LocalDate? = null
    private var avisosRachaListos = false

    private suspend fun registrarAperturaRacha() {
        val hoy = LocalDate.now()
        if (ultimaAperturaRacha == hoy) return
        ultimaAperturaRacha = hoy
        appContainer.moduloCarreraSol.alAbrirAplicacion(AplicacionAbierta(USUARIO_LOCAL, hoy))
    }

    override fun onStart() {
        super.onStart()
        // Si la app volvió a primer plano en un nuevo día, el MOD-05 revisa la racha.
        if (avisosRachaListos) lifecycleScope.launch { registrarAperturaRacha() }
        preguntaFlashViewModel.reanudar()
    }

    override fun onStop() {
        preguntaFlashViewModel.pausar()
        super.onStop()
    }
}
