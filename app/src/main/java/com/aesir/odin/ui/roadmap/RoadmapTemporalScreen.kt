package com.aesir.odin.ui.roadmap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.ui.components.HandPaintedTitle
import com.aesir.odin.ui.theme.ColorChispasDoradas
import com.aesir.odin.ui.theme.ColorFondoNodo
import com.aesir.odin.ui.theme.ColorNocheOscura
import com.aesir.odin.ui.theme.ColorNodoBloqueado
import com.aesir.odin.ui.theme.ColorNodoCompletado
import com.aesir.odin.ui.theme.ColorNodoDesbloqueado
import com.aesir.odin.ui.theme.ColorTextoClaro
import com.aesir.odin.ui.theme.ColorTextoTenue

/**
 * TEMPORAL: lista simple de temas para poder entrar a las lecciones y probar CU-05.
 * Se reemplaza por el RoadmapScreen de CU-01 al integrar las ramas.
 */
@Composable
fun RoadmapTemporalScreen(
    appContainer: AppContainer,
    onIniciarLeccion: (leccionId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Se vuelve a consultar cada vez que la pantalla regresa, para ver el avance actualizado.
    var temas by remember { mutableStateOf<List<Pair<Tema, String?>>>(emptyList()) }
    LaunchedEffect(Unit) {
        temas = appContainer.roadmapRepository.obtenerTemas().map { tema ->
            tema to appContainer.leccionRepository.obtenerLeccionesPorTema(tema.id).firstOrNull()?.id
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorNocheOscura)
            .padding(16.dp)
    ) {
        HandPaintedTitle(texto = "Roadmap", tamano = 40.sp)
        Text("Selecciona una lección desbloqueada", color = ColorTextoTenue, fontSize = 14.sp)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            items(temas, key = { it.first.id }) { (tema, leccionId) ->
                TarjetaTema(
                    tema = tema,
                    onClick = { if (tema.desbloqueado && leccionId != null) onIniciarLeccion(leccionId) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaTema(tema: Tema, onClick: () -> Unit) {
    val (color, estado) = when {
        tema.leccionCompletada -> ColorNodoCompletado to "Completado ✓"
        tema.desbloqueado -> ColorNodoDesbloqueado to "Iniciar lección"
        else -> ColorNodoBloqueado to "Bloqueado 🔒"
    }
    Surface(
        onClick = onClick,
        enabled = tema.desbloqueado,
        shape = RoundedCornerShape(12.dp),
        color = ColorFondoNodo,
        border = BorderStroke(2.dp, color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Tema ${tema.orden}", color = ColorTextoTenue, fontSize = 12.sp)
                Text(
                    tema.nombre,
                    color = if (tema.desbloqueado) ColorTextoClaro else ColorTextoTenue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(estado, color = if (tema.desbloqueado) ColorChispasDoradas else ColorTextoTenue, fontSize = 13.sp)
        }
    }
}
