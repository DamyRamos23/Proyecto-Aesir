package com.aesir.odin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aesir.odin.di.AppContainer
import com.aesir.odin.ui.leccion.LeccionInteractivaScreen
import com.aesir.odin.ui.leccion.LeccionInteractivaViewModel
import com.aesir.odin.ui.leccion.ResumenLeccionScreen
import com.aesir.odin.ui.roadmap.RoadmapTemporalScreen

object OdinNavGraph {
    const val rutaRoadmap = "roadmap"
    const val rutaLeccion = "leccion/{leccionId}"
    const val rutaResumenLeccion = "resumen_leccion/{puntaje}/{errores}/{total}"

    fun leccion(leccionId: String) = "leccion/$leccionId"
    fun resumenLeccion(puntaje: Int, errores: Int, total: Int) = "resumen_leccion/$puntaje/$errores/$total"

    @Composable
    fun construirGrafo(nav: NavHostController, appContainer: AppContainer, modifier: Modifier = Modifier) {
        val volverAlRoadmap: () -> Unit = { nav.popBackStack(rutaRoadmap, inclusive = false) }

        NavHost(navController = nav, startDestination = rutaRoadmap, modifier = modifier) {
            composable(rutaRoadmap) {
                RoadmapTemporalScreen(
                    appContainer = appContainer,
                    onIniciarLeccion = { leccionId -> nav.navigate(leccion(leccionId)) }
                )
            }

            composable(
                rutaLeccion,
                arguments = listOf(navArgument("leccionId") { type = NavType.StringType })
            ) { entrada ->
                val leccionId = entrada.arguments?.getString("leccionId").orEmpty()
                val viewModel: LeccionInteractivaViewModel =
                    viewModel(factory = LeccionInteractivaViewModel.factory(appContainer, leccionId))
                LeccionInteractivaScreen(
                    viewModel = viewModel,
                    onLeccionFinalizada = { resultado, total ->
                        nav.navigate(resumenLeccion(resultado.puntaje, resultado.erroresCometidos, total)) {
                            // Al salir del resumen no se debe poder regresar a la lección terminada.
                            popUpTo(rutaRoadmap)
                        }
                    },
                    onLeccionFallida = volverAlRoadmap,
                    onSalir = volverAlRoadmap
                )
            }

            composable(
                rutaResumenLeccion,
                arguments = listOf(
                    navArgument("puntaje") { type = NavType.IntType },
                    navArgument("errores") { type = NavType.IntType },
                    navArgument("total") { type = NavType.IntType }
                )
            ) { entrada ->
                val args = entrada.arguments
                ResumenLeccionScreen(
                    puntaje = args?.getInt("puntaje") ?: 0,
                    errores = args?.getInt("errores") ?: 0,
                    totalEjercicios = args?.getInt("total") ?: 0,
                    onVolverRoadmap = volverAlRoadmap
                )
            }
        }
    }
}
