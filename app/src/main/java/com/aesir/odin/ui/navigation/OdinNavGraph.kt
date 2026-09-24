package com.aesir.odin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aesir.odin.di.AppContainer
import com.aesir.odin.ui.leccion.LeccionInteractivaScreen
import com.aesir.odin.ui.leccion.LeccionInteractivaViewModel
import com.aesir.odin.ui.leccion.LeccionScreen
import com.aesir.odin.ui.leccion.ResumenLeccionPlaceholder
import com.aesir.odin.ui.leccion.ResumenLeccionScreen
import com.aesir.odin.ui.mundos.MundosScreen
import com.aesir.odin.ui.preguntaflash.PreguntaFlashScreen
import com.aesir.odin.ui.roadmap.RoadmapScreen
import com.aesir.odin.ui.temaintro.TemaIntroScreen

/**
 * Grafo de navegación de ODÍN.
 *
 * Define las rutas principales de la aplicación y configura el NavHost
 * utilizando el NavController de Navigation Compose.
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

    // Rutas para lecciones interactivas
    const val rutaLeccionIntro = "leccionIntro"
    private const val rutaLeccionInteractiva = "leccionInteractiva"
    private const val rutaResumenInteractivo = "resumenInteractivo"

    private const val argLeccionId = "leccionId"
    private const val argPuntaje = "puntaje"
    private const val argErrores = "errores"
    private const val argTotal = "total"

    /** Ruta concreta del roadmap para un mundo determinado. */
    fun rutaRoadmapCon(mundoId: String) =
        "$rutaRoadmap/$mundoId"

    /** Ruta concreta de introducción para un tema. */
    fun rutaIntroTemaCon(temaId: String) =
        "$rutaIntroTema/$temaId"

    /** Ruta concreta de una lección interactiva. */
    fun leccion(leccionId: String) =
        "$rutaLeccionInteractiva/$leccionId"

    /** Ruta concreta de la intro de lección. */
    fun rutaLeccionIntroCon(leccionId: String) =
        "$rutaLeccionIntro/$leccionId"

    /** Ruta concreta del resumen de una lección interactiva. */
    fun resumenLeccion(
        puntaje: Int,
        errores: Int,
        total: Int
    ) = "$rutaResumenInteractivo/$puntaje/$errores/$total"

    private fun argumentoMundoId(): NamedNavArgument =
        navArgument(argMundoId) {
            type = NavType.StringType
        }

    private fun argumentoTemaId(): NamedNavArgument =
        navArgument(argTemaId) {
            type = NavType.StringType
        }

    @Composable
    fun construirGrafo(
        nav: NavHostController,
        appContainer: AppContainer,
        modifier: Modifier = Modifier
    ) {

        val volverAlRoadmap: () -> Unit = {
            nav.popBackStack(
                "$rutaRoadmap/{$argMundoId}",
                inclusive = false
            )
        }

        NavHost(
            navController = nav,
            startDestination = rutaMundos,
            modifier = modifier
        ) {

            // -------------------------
            // Mundos
            // -------------------------

            composable(rutaMundos) {
                MundosScreen(
                    nav,
                    appContainer
                )
            }

            // -------------------------
            // Roadmap
            // -------------------------

            composable(
                route = "$rutaRoadmap/{$argMundoId}",
                arguments = listOf(argumentoMundoId())
            ) { backStackEntry ->

                val mundoId =
                    backStackEntry.arguments
                        ?.getString(argMundoId)
                        .orEmpty()

                RoadmapScreen(
                    nav,
                    mundoId,
                    appContainer
                )
            }

            // -------------------------
            // Introducción de tema
            // -------------------------

            composable(
                route = "$rutaIntroTema/{$argTemaId}",
                arguments = listOf(argumentoTemaId())
            ) { backStackEntry ->

                val temaId =
                    backStackEntry.arguments
                        ?.getString(argTemaId)
                        .orEmpty()

                TemaIntroScreen(
                    nav,
                    temaId,
                    appContainer
                )
            }

            // -------------------------
            // Lección
            // -------------------------

            composable(rutaLeccion) {
                LeccionScreen(nav)
            }

            // -------------------------
            // Resumen de lección
            // -------------------------

            composable(rutaResumenLeccion) {
                ResumenLeccionPlaceholder(nav)
            }

            // -------------------------
            // Pregunta flash
            // -------------------------

            composable(rutaPreguntaFlash) {
                PreguntaFlashScreen(nav)
            }

            // -------------------------
            // Introducción de lección
            // -------------------------

            composable(
                route = "$rutaLeccionIntro/{$argLeccionId}",
                arguments = listOf(
                    navArgument(argLeccionId) {
                        type = NavType.StringType
                    }
                )
            ) { entrada ->

                val leccionId =
                    entrada.arguments
                        ?.getString(argLeccionId)
                        .orEmpty()

                TemaIntroScreen(
                    navController = nav,
                    temaId = leccionId,
                    appContainer = appContainer
                )
            }

            // -------------------------
            // Lección interactiva
            // -------------------------

            composable(
                route = "$rutaLeccionInteractiva/{$argLeccionId}",
                arguments = listOf(
                    navArgument(argLeccionId) {
                        type = NavType.StringType
                    }
                )
            ) { entrada ->

                val leccionId =
                    entrada.arguments
                        ?.getString(argLeccionId)
                        .orEmpty()

                val viewModel: LeccionInteractivaViewModel =
                    viewModel(
                        factory = LeccionInteractivaViewModel.factory(
                            appContainer,
                            leccionId
                        )
                    )

                LeccionInteractivaScreen(
                    viewModel = viewModel,

                    onLeccionFinalizada = { resultado, total ->

                        nav.navigate(
                            resumenLeccion(
                                resultado.puntaje,
                                resultado.erroresCometidos,
                                total
                            )
                        ) {
                            popUpTo(rutaRoadmap)
                        }
                    },

                    onLeccionFallida = volverAlRoadmap,

                    onSalir = volverAlRoadmap
                )
            }

            // -------------------------
            // Resumen interactivo
            // -------------------------

            composable(
                route = "$rutaResumenInteractivo/{$argPuntaje}/{$argErrores}/{$argTotal}",
                arguments = listOf(
                    navArgument(argPuntaje) {
                        type = NavType.IntType
                    },
                    navArgument(argErrores) {
                        type = NavType.IntType
                    },
                    navArgument(argTotal) {
                        type = NavType.IntType
                    }
                )
            ) { entrada ->

                val args = entrada.arguments

                ResumenLeccionScreen(
                    puntaje = args?.getInt(argPuntaje) ?: 0,
                    errores = args?.getInt(argErrores) ?: 0,
                    totalEjercicios = args?.getInt(argTotal) ?: 0,
                    onVolverRoadmap = volverAlRoadmap
                )
            }
        }
    }
}