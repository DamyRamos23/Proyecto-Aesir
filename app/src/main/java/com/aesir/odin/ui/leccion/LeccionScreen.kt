package com.aesir.odin.ui.leccion

import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import com.aesir.odin.ui.components.PantallaProximamente

/**
 * Destino de rutaLeccion. Fuera del alcance de CU-1 (los diagramas
 * entregados solo cubren seleccionar un tema, no la lección en sí);
 * se deja como placeholder para que el grafo de navegación quede completo
 * y no falle si algo navega hacia aquí.
 */
@Composable
fun LeccionScreen(navController: NavHostController) {
    PantallaProximamente(navController, "Lección")
}

/**
 * Destino de rutaResumenLeccion. Mismo caso que LeccionScreen.
 */
@Composable
fun ResumenLeccionScreen(navController: NavHostController) {
    PantallaProximamente(navController, "Resumen de la lección")
}
