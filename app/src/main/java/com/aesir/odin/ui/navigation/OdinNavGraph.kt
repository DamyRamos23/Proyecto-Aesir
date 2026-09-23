package com.aesir.odin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aesir.odin.di.AppContainer
import com.aesir.odin.ui.leccion.LeccionScreen
import com.aesir.odin.ui.leccion.ResumenLeccionScreen
import com.aesir.odin.ui.mundos.MundosScreen
import com.aesir.odin.ui.preguntaflash.PreguntaFlashScreen
import com.aesir.odin.ui.roadmap.RoadmapScreen
import com.aesir.odin.ui.temaintro.TemaIntroScreen

/**
 * Grafo de navegación de ODÍN. Corresponde a `OdinNavGraph` de la capa
 * Navegación del diagrama de clases: define las rutas y arma el NavHost
 * sobre el NavController real de Compose/Android (<<Android API>> en el
 * diagrama).
 */
object OdinNavGraph {
    const val rutaMundos = "mundos"
    const val rutaRoadmap = "roadmap"
    const val rutaIntroTema = "introTema"
    const val rutaLeccion = "leccion"
    const val rutaResumenLeccion = "resumenLeccion"
    const val rutaPreguntaFlash = "preguntaFlash"

    private const val argMundoId = "mundoId"
    private const val argTemaId = "temaId"

    /** Ruta concreta de RoadmapScreen para un mundo/nivel dado. */
    fun rutaRoadmapCon(mundoId: String) = "$rutaRoadmap/$mundoId"

    /** Ruta concreta de TemaIntroScreen para un tema dado. */
    fun rutaIntroTemaCon(temaId: String) = "$rutaIntroTema/$temaId"

    private fun argumentoMundoId(): NamedNavArgument =
        navArgument(argMundoId) { type = NavType.StringType }

    private fun argumentoTemaId(): NamedNavArgument =
        navArgument(argTemaId) { type = NavType.StringType }

    @Composable
    fun construirGrafo(
        nav: NavHostController,
        appContainer: AppContainer,
        modifier: Modifier = Modifier
    ) {
        NavHost(
            navController = nav,
            startDestination = rutaMundos,
            modifier = modifier
        ) {
            composable(rutaMundos) {
                MundosScreen(nav, appContainer)
            }
            composable(
                route = "$rutaRoadmap/{$argMundoId}",
                arguments = listOf(argumentoMundoId())
            ) { backStackEntry ->
                val mundoId = backStackEntry.arguments?.getString(argMundoId).orEmpty()
                RoadmapScreen(nav, mundoId, appContainer)
            }
            composable(
                route = "$rutaIntroTema/{$argTemaId}",
                arguments = listOf(argumentoTemaId())
            ) { backStackEntry ->
                val temaId = backStackEntry.arguments?.getString(argTemaId).orEmpty()
                TemaIntroScreen(nav, temaId, appContainer)
            }
            composable(rutaLeccion) {
                LeccionScreen(nav)
            }
            composable(rutaResumenLeccion) {
                ResumenLeccionScreen(nav)
            }
            composable(rutaPreguntaFlash) {
                PreguntaFlashScreen(nav)
            }
        }
    }
}
