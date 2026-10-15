package com.aesir.odin.ui.navigation

import androidx.compose.runtime.Composable
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.ArbolHabilidades
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.ui.crucigrama.CrucigramaScreen
import com.aesir.odin.ui.crucigrama.CrucigramaViewModel
import com.aesir.odin.ui.crucigrama.ResultadoCrucigramaScreen
import com.aesir.odin.ui.flashcards.FlashcardsScreen
import com.aesir.odin.ui.flashcards.FlashcardsViewModel
import com.aesir.odin.ui.ragnarok.RagnarokScreen
import com.aesir.odin.ui.ragnarok.RagnarokViewModel
import com.aesir.odin.ui.retoarrastrar.ResumenRetoScreen
import com.aesir.odin.ui.retoarrastrar.RetoArrastrarScreen
import com.aesir.odin.ui.retoarrastrar.RetoArrastrarViewModel
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
    const val rutaCrucigrama = "crucigrama"
    const val rutaResultadoCrucigrama = "resultadoCrucigrama"
    const val rutaFlashcards = "flashcards"
    const val rutaRagnarok = "ragnarok"
    const val rutaRetoArrastrar = "retoArrastrar"
    const val rutaResumenReto = "resumenReto"

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
    private const val argTemas = "temas"
    private const val argAciertos = "aciertos"
    private const val argOrdenMundo = "ordenMundo"
    private const val argIntentos = "intentos"
    private const val argDesbloqueados = "desbloqueados"

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
        leccionId: String,
        puntaje: Int,
        errores: Int,
        total: Int
    ) = "$rutaResumenInteractivo/$leccionId/$puntaje/$errores/$total"

    /** Ruta del crucigrama de un nivel (CU-07) con los temas cuyos conceptos usa. */
    fun rutaCrucigramaCon(temaId: String, temasIncluidos: List<String>) =
        "$rutaCrucigrama/$temaId?$argTemas=${temasIncluidos.joinToString(",")}"

    fun rutaResultadoCrucigramaCon(temaId: String, ordenMundo: Int, puntaje: Int, aciertos: Int, errores: Int, total: Int) =
        "$rutaResultadoCrucigrama/$temaId/$ordenMundo/$puntaje/$aciertos/$errores/$total"

    /** Ruta de las flashcards de un nivel (CU-03) con los temas cuyos conceptos usa. */
    fun rutaFlashcardsCon(temaId: String, temasIncluidos: List<String>) =
        "$rutaFlashcards/$temaId?$argTemas=${temasIncluidos.joinToString(",")}"

    /** Ruta del reto de arrastrar y soltar (CU-09) con las lecciones cercanas para repasar. */
    fun rutaRetoArrastrarCon(temaId: String, temasIncluidos: List<String>) =
        "$rutaRetoArrastrar/$temaId?$argTemas=${temasIncluidos.joinToString(",")}"

    /** Resumen del reto aprobado; los nombres de los niveles abiertos van codificados. */
    fun rutaResumenRetoCon(puntaje: Int, intentos: Int, ordenMundo: Int, desbloqueados: List<String>) =
        "$rutaResumenReto/$puntaje/$intentos/$ordenMundo?$argDesbloqueados=${Uri.encode(desbloqueados.joinToString("|"))}"

    /** Ruta del reto de fin de mundo (CU-08) de un mundo, por el id de su tema Ragnarök. */
    fun rutaRagnarokCon(temaId: String) = "$rutaRagnarok/$temaId"

    /**
     * A dónde lleva entrar a un nivel: la introducción del tema; el reto de fin
     * de mundo si es el Ragnarök; o, si el nivel es un crucigrama o unas
     * flashcards, la actividad con los conceptos de las dos lecciones que
     * tiene más cerca en el árbol (ArbolHabilidades).
     */
    fun rutaNivelCon(tema: Tema, temasDelMundo: List<Tema>): String {
        if (tema.esRagnarok) return rutaRagnarokCon(tema.id)
        val lecciones = temasDelMundo.filter { !it.esRagnarok && !it.esMinijuego }.sortedBy { it.orden }
        if (tema.esArrastrar) {
            // Tercer anillo: las lecciones más cercanas por ángulo son las que se sugiere repasar (A7.2)
            val retos = temasDelMundo.filter { !it.esRagnarok && it.esReto }.sortedBy { it.orden }
            val j = retos.indexOfFirst { it.id == tema.id }
            val cercanas = if (j < 0) lecciones.indices.toList()
            else ArbolHabilidades.leccionesCercanasAReto(j, retos.size, lecciones.size)
            return rutaRetoArrastrarCon(tema.id, cercanas.map { lecciones[it].id })
        }
        if (!tema.esCrucigrama && !tema.esFlashcards) return rutaIntroTemaCon(tema.id)
        val minijuegos = temasDelMundo.filter { !it.esRagnarok && it.anillo == ArbolHabilidades.ANILLO_MINIJUEGOS }.sortedBy { it.orden }
        val k = minijuegos.indexOfFirst { it.id == tema.id }
        val cercanas = if (k < 0) lecciones.indices.toList()
        else ArbolHabilidades.leccionesCercanas(k, lecciones.size, minijuegos.size)
        val temas = cercanas.map { lecciones[it].id }
        return if (tema.esFlashcards) rutaFlashcardsCon(tema.id, temas) else rutaCrucigramaCon(tema.id, temas)
    }

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
                                leccionId,
                                resultado.puntaje,
                                resultado.erroresCometidos,
                                total
                            )
                        ) {
                            popUpTo("$rutaRoadmap/{$argMundoId}") {
                                inclusive = false
                            }
                        }
                    },

                    onLeccionFallida = volverAlRoadmap,

                    // MOD-01: salir antes de terminar es abandonar el nivel.
                    onSalir = { viewModel.abandonar(volverAlRoadmap) }
                )
            }

            // -------------------------
            // Resumen interactivo
            // -------------------------

            composable(
                route = "$rutaResumenInteractivo/{$argLeccionId}/{$argPuntaje}/{$argErrores}/{$argTotal}",
                arguments = listOf(
                    navArgument(argLeccionId) {
                        type = NavType.StringType
                    },
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
                    leccionId = args?.getString(argLeccionId).orEmpty(),
                    puntaje = args?.getInt(argPuntaje) ?: 0,
                    errores = args?.getInt(argErrores) ?: 0,
                    totalEjercicios = args?.getInt(argTotal) ?: 0,
                    appContainer = appContainer,
                    onVolverRoadmap = volverAlRoadmap,
                    onAbrirTema = { tema, temasDelMundo ->
                        // La entrada ya se cobró en el resumen (MOD-01). Abre el siguiente
                        // nivel dejando el roadmap debajo.
                        nav.navigate(rutaNivelCon(tema, temasDelMundo)) {
                            popUpTo("$rutaRoadmap/{$argMundoId}") {
                                inclusive = false
                            }
                        }
                    }
                )
            }

            // -------------------------
            // Crucigrama (CU-07)
            // -------------------------

            composable(
                route = "$rutaCrucigrama/{$argTemaId}?$argTemas={$argTemas}",
                arguments = listOf(
                    argumentoTemaId(),
                    navArgument(argTemas) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entrada ->
                val temaId = entrada.arguments?.getString(argTemaId).orEmpty()
                val temas = entrada.arguments?.getString(argTemas).orEmpty()
                    .split(",").filter { it.isNotBlank() }
                val viewModel: CrucigramaViewModel = viewModel(factory = CrucigramaViewModel.factory(appContainer))

                CrucigramaScreen(
                    viewModel = viewModel,
                    temaId = temaId,
                    temasIncluidos = temas,
                    onFinalizado = { resultado, total, ordenMundo ->
                        nav.navigate(
                            rutaResultadoCrucigramaCon(
                                temaId, ordenMundo, resultado.puntajeFinal,
                                resultado.aciertos, resultado.erroresCometidos, total
                            )
                        ) {
                            popUpTo("$rutaRoadmap/{$argMundoId}") { inclusive = false }
                        }
                    },
                    // MOD-01: salir antes de terminar es abandonar el nivel.
                    onSalir = { viewModel.abandonar(volverAlRoadmap) }
                )
            }

            composable(
                route = "$rutaResultadoCrucigrama/{$argTemaId}/{$argOrdenMundo}/{$argPuntaje}/{$argAciertos}/{$argErrores}/{$argTotal}",
                arguments = listOf(
                    argumentoTemaId(),
                    navArgument(argOrdenMundo) { type = NavType.IntType },
                    navArgument(argPuntaje) { type = NavType.IntType },
                    navArgument(argAciertos) { type = NavType.IntType },
                    navArgument(argErrores) { type = NavType.IntType },
                    navArgument(argTotal) { type = NavType.IntType }
                )
            ) { entrada ->
                val args = entrada.arguments
                ResultadoCrucigramaScreen(
                    puntaje = args?.getInt(argPuntaje) ?: 0,
                    aciertos = args?.getInt(argAciertos) ?: 0,
                    errores = args?.getInt(argErrores) ?: 0,
                    totalPalabras = args?.getInt(argTotal) ?: 0,
                    ordenMundo = args?.getInt(argOrdenMundo) ?: 1,
                    onVolverRoadmap = volverAlRoadmap
                )
            }

            // -------------------------
            // Flashcards (CU-03)
            // -------------------------

            composable(
                route = "$rutaFlashcards/{$argTemaId}?$argTemas={$argTemas}",
                arguments = listOf(
                    argumentoTemaId(),
                    navArgument(argTemas) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entrada ->
                val temaId = entrada.arguments?.getString(argTemaId).orEmpty()
                val temas = entrada.arguments?.getString(argTemas).orEmpty()
                    .split(",").filter { it.isNotBlank() }
                val viewModel: FlashcardsViewModel = viewModel(factory = FlashcardsViewModel.factory(appContainer))

                FlashcardsScreen(
                    viewModel = viewModel,
                    temaId = temaId,
                    temasIncluidos = temas,
                    onVolverRoadmap = volverAlRoadmap,
                    // A5.2: la lección del tema de la carta fallada; el roadmap queda debajo.
                    onRepasarLeccion = { temaLeccion ->
                        nav.navigate(rutaIntroTemaCon(temaLeccion)) {
                            popUpTo("$rutaRoadmap/{$argMundoId}") { inclusive = false }
                        }
                    },
                    // MOD-01: salir antes de terminar es abandonar el nivel.
                    onSalir = { viewModel.abandonar(volverAlRoadmap) }
                )
            }

            // -------------------------
            // Reto de arrastrar y soltar (CU-09)
            // -------------------------

            composable(
                route = "$rutaRetoArrastrar/{$argTemaId}?$argTemas={$argTemas}",
                arguments = listOf(
                    argumentoTemaId(),
                    navArgument(argTemas) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entrada ->
                val temaId = entrada.arguments?.getString(argTemaId).orEmpty()
                val temas = entrada.arguments?.getString(argTemas).orEmpty()
                    .split(",").filter { it.isNotBlank() }
                val viewModel: RetoArrastrarViewModel = viewModel(factory = RetoArrastrarViewModel.factory(appContainer))

                RetoArrastrarScreen(
                    viewModel = viewModel,
                    temaId = temaId,
                    temasIncluidos = temas,
                    onAprobado = { resultado, desbloqueados, ordenMundo ->
                        nav.navigate(rutaResumenRetoCon(resultado.puntaje, resultado.intentos, ordenMundo, desbloqueados)) {
                            popUpTo("$rutaRoadmap/{$argMundoId}") { inclusive = false }
                        }
                    },
                    onVolverRoadmap = volverAlRoadmap,
                    // A7.2: repasar la lección interactiva del tema; el roadmap queda debajo.
                    onRepasarLeccion = { temaLeccion ->
                        nav.navigate(rutaIntroTemaCon(temaLeccion)) {
                            popUpTo("$rutaRoadmap/{$argMundoId}") { inclusive = false }
                        }
                    },
                    // A4.1 y MOD-01: salir antes de terminar es abandonar el nivel.
                    onSalir = { viewModel.confirmarSalida(volverAlRoadmap) }
                )
            }

            composable(
                route = "$rutaResumenReto/{$argPuntaje}/{$argIntentos}/{$argOrdenMundo}?$argDesbloqueados={$argDesbloqueados}",
                arguments = listOf(
                    navArgument(argPuntaje) { type = NavType.IntType },
                    navArgument(argIntentos) { type = NavType.IntType },
                    navArgument(argOrdenMundo) { type = NavType.IntType },
                    navArgument(argDesbloqueados) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entrada ->
                val args = entrada.arguments
                ResumenRetoScreen(
                    puntaje = args?.getInt(argPuntaje) ?: 0,
                    intentos = args?.getInt(argIntentos) ?: 0,
                    desbloqueados = args?.getString(argDesbloqueados).orEmpty().split("|").filter { it.isNotBlank() },
                    ordenMundo = args?.getInt(argOrdenMundo) ?: 1,
                    onVolverRoadmap = volverAlRoadmap
                )
            }

            // -------------------------
            // Reto de fin de mundo (CU-08)
            // -------------------------

            composable(
                route = "$rutaRagnarok/{$argTemaId}",
                arguments = listOf(argumentoTemaId())
            ) { entrada ->
                val temaId = entrada.arguments?.getString(argTemaId).orEmpty()
                val viewModel: RagnarokViewModel = viewModel(factory = RagnarokViewModel.factory(appContainer))

                RagnarokScreen(
                    viewModel = viewModel,
                    temaId = temaId,
                    onVolverRoadmap = volverAlRoadmap,
                    // Al superar el reto se puede pasar directo al mundo que se desbloqueó.
                    onIrAlMundo = { mundoId ->
                        nav.navigate(rutaRoadmapCon(mundoId)) {
                            popUpTo(rutaMundos) { inclusive = false }
                        }
                    },
                    // A10.1: repasar la lección de un tema con errores.
                    onRepasarLeccion = { temaLeccion ->
                        nav.navigate(rutaIntroTemaCon(temaLeccion)) {
                            popUpTo("$rutaRoadmap/{$argMundoId}") { inclusive = false }
                        }
                    },
                    // MOD-01: salir antes de terminar es abandonar el nivel.
                    onSalir = { viewModel.abandonar(volverAlRoadmap) }
                )
            }
        }
    }
}