package com.aesir.odin.ui.leccion

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.Ejercicio
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.ResultadoLeccion
import com.aesir.odin.domain.model.ResultadoRespuesta
import com.aesir.odin.domain.usecase.leccion.RegistrarErrorLeccionUseCase
import com.aesir.odin.ui.theme.ColorChispasDoradas
import com.aesir.odin.ui.theme.ColorConexionInactiva
import com.aesir.odin.ui.theme.ColorFondoNodo
import com.aesir.odin.ui.theme.ColorNocheOscura
import com.aesir.odin.ui.theme.ColorNocheProfunda
import com.aesir.odin.ui.theme.ColorNodoDesbloqueado
import com.aesir.odin.ui.theme.ColorRespuestaCorrecta
import com.aesir.odin.ui.theme.ColorRespuestaIncompleta
import com.aesir.odin.ui.theme.ColorRespuestaIncorrecta
import com.aesir.odin.ui.theme.ColorTextoClaro
import com.aesir.odin.ui.theme.ColorTextoTenue
import com.aesir.odin.ui.theme.ODINTheme

@Composable
fun LeccionInteractivaScreen(
    viewModel: LeccionInteractivaViewModel,
    onLeccionFinalizada: (ResultadoLeccion, totalEjercicios: Int) -> Unit,
    onLeccionFallida: () -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val leccion by viewModel.leccion.collectAsState()
    val ejercicioActual by viewModel.ejercicioActual.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val ultimoResultado by viewModel.ultimoResultado.collectAsState()
    val resultadoFinal by viewModel.resultadoFinal.collectAsState()
    val leccionFallida by viewModel.leccionFallida.collectAsState()

    LaunchedEffect(resultadoFinal) {
        val resultado = resultadoFinal ?: return@LaunchedEffect
        onLeccionFinalizada(resultado, leccion?.ejercicios?.size ?: 0)
    }

    val leccionActual = leccion
    val ejercicio = leccionActual?.ejercicios?.getOrNull(ejercicioActual)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorNocheOscura)
    ) {
        if (leccionActual == null || ejercicio == null) {
            CircularProgressIndicator(
                color = ColorChispasDoradas,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            ContenidoLeccion(
                titulo = leccionActual.titulo,
                ejercicio = ejercicio,
                numeroEjercicio = ejercicioActual + 1,
                totalEjercicios = leccionActual.ejercicios.size,
                errores = errores,
                ultimoResultado = ultimoResultado,
                onComprobar = viewModel::responder,
                onContinuar = viewModel::siguienteEjercicio,
                onSalir = onSalir
            )
        }

        if (leccionFallida) {
            DialogoLeccionFallida(onVolverRoadmap = onLeccionFallida)
        }
    }
}

@Composable
private fun ContenidoLeccion(
    titulo: String,
    ejercicio: Ejercicio,
    numeroEjercicio: Int,
    totalEjercicios: Int,
    errores: Int,
    ultimoResultado: ResultadoRespuesta?,
    onComprobar: (List<Int>) -> Unit,
    onContinuar: () -> Unit,
    onSalir: () -> Unit
) {
    // La selección se reinicia al cambiar de ejercicio.
    var seleccion by remember(ejercicio.id) { mutableStateOf(emptySet<Int>()) }
    val respondido = ultimoResultado != null
    val esUltimo = numeroEjercicio == totalEjercicios

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        EncabezadoLeccion(
            progreso = (numeroEjercicio - if (respondido) 0 else 1).toFloat() / totalEjercicios,
            errores = errores,
            onSalir = onSalir
        )

        Spacer(Modifier.height(16.dp))
        Text(titulo, color = ColorChispasDoradas, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text("Ejercicio $numeroEjercicio de $totalEjercicios", color = ColorTextoTenue, fontSize = 13.sp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(ejercicio.enunciado, color = ColorTextoClaro, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (ejercicio.esSeleccionMultiple) {
                Text("Selecciona todas las respuestas correctas", color = ColorTextoTenue, fontSize = 13.sp)
            }
            Spacer(Modifier.height(4.dp))

            ejercicio.opciones.forEachIndexed { indice, opcion ->
                OpcionRespuesta(
                    texto = opcion,
                    seleccionada = indice in seleccion,
                    estado = estadoOpcion(indice, seleccion, ultimoResultado),
                    habilitada = !respondido,
                    onClick = {
                        seleccion = when {
                            ejercicio.esSeleccionMultiple && indice in seleccion -> seleccion - indice
                            ejercicio.esSeleccionMultiple -> seleccion + indice
                            else -> setOf(indice)
                        }
                    }
                )
            }
        }

        if (ultimoResultado != null) {
            PanelRetroalimentacion(
                resultado = ultimoResultado,
                ejercicio = ejercicio,
                textoBoton = if (esUltimo) "Ver resultados" else "Continuar",
                onContinuar = onContinuar
            )
        } else {
            Button(
                onClick = { onComprobar(seleccion.sorted()) },
                enabled = seleccion.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorNodoDesbloqueado,
                    contentColor = ColorTextoClaro,
                    disabledContainerColor = ColorConexionInactiva,
                    disabledContentColor = ColorTextoTenue
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Comprobar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EncabezadoLeccion(progreso: Float, errores: Int, onSalir: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onSalir) {
            Text("✕", color = ColorTextoTenue, fontSize = 20.sp)
        }
        LinearProgressIndicator(
            progress = { progreso },
            color = ColorChispasDoradas,
            trackColor = ColorConexionInactiva,
            modifier = Modifier.weight(1f).height(8.dp)
        )
        Spacer(Modifier.padding(horizontal = 6.dp))
        // Vidas restantes antes de que se interrumpa la lección.
        val limite = RegistrarErrorLeccionUseCase.LIMITE_ERRORES
        Text(
            text = "❤".repeat((limite - errores).coerceAtLeast(0)) + "♡".repeat(errores.coerceAtMost(limite)),
            color = ColorRespuestaIncorrecta,
            fontSize = 18.sp
        )
    }
}

private enum class EstadoOpcion { NEUTRA, CORRECTA, INCORRECTA, FALTANTE }

private fun estadoOpcion(indice: Int, seleccion: Set<Int>, resultado: ResultadoRespuesta?): EstadoOpcion {
    if (resultado == null) return EstadoOpcion.NEUTRA
    val esCorrecta = indice in resultado.opcionesCorrectas
    val fueSeleccionada = indice in seleccion
    return when {
        esCorrecta && fueSeleccionada -> EstadoOpcion.CORRECTA
        esCorrecta -> EstadoOpcion.FALTANTE
        fueSeleccionada -> EstadoOpcion.INCORRECTA
        else -> EstadoOpcion.NEUTRA
    }
}

@Composable
private fun OpcionRespuesta(
    texto: String,
    seleccionada: Boolean,
    estado: EstadoOpcion,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    val colorBorde = when (estado) {
        EstadoOpcion.CORRECTA -> ColorRespuestaCorrecta
        EstadoOpcion.INCORRECTA -> ColorRespuestaIncorrecta
        EstadoOpcion.FALTANTE -> ColorRespuestaIncompleta
        EstadoOpcion.NEUTRA -> if (seleccionada) ColorChispasDoradas else ColorConexionInactiva
    }
    Surface(
        onClick = onClick,
        enabled = habilitada,
        shape = RoundedCornerShape(12.dp),
        color = if (seleccionada) ColorNocheProfunda else ColorFondoNodo,
        border = BorderStroke(if (seleccionada || estado != EstadoOpcion.NEUTRA) 2.dp else 1.dp, colorBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(texto, color = ColorTextoClaro, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun PanelRetroalimentacion(
    resultado: ResultadoRespuesta,
    ejercicio: Ejercicio,
    textoBoton: String,
    onContinuar: () -> Unit
) {
    val (titulo, color) = when (resultado.estado) {
        EstadoRespuesta.CORRECTA -> "¡Correcto!" to ColorRespuestaCorrecta
        EstadoRespuesta.INCOMPLETA -> "Respuesta incompleta" to ColorRespuestaIncompleta
        EstadoRespuesta.INCORRECTA -> "Incorrecto" to ColorRespuestaIncorrecta
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (resultado.estado != EstadoRespuesta.CORRECTA) {
                val correctas = resultado.opcionesCorrectas.joinToString("\n") { "• ${ejercicio.opciones[it]}" }
                Text("Respuesta correcta:\n$correctas", color = ColorTextoClaro, fontSize = 14.sp)
            }
            Button(
                onClick = onContinuar,
                colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(textoBoton, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Flujo alternativo A4.1: se alcanzó el límite de errores. */
@Composable
private fun DialogoLeccionFallida(onVolverRoadmap: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = ColorNocheProfunda,
        titleContentColor = ColorRespuestaIncorrecta,
        textContentColor = ColorTextoClaro,
        title = { Text("Límite de intentos superado") },
        text = {
            Text(
                "Cometiste ${RegistrarErrorLeccionUseCase.LIMITE_ERRORES} errores, así que la lección " +
                    "terminó sin recompensa.\n\nTe sugerimos repasar la introducción del tema y " +
                    "practicar con preguntas flash antes de volver a intentarlo."
            )
        },
        confirmButton = {
            TextButton(onClick = onVolverRoadmap) {
                Text("Volver al roadmap", color = ColorChispasDoradas)
            }
        }
    )
}

@Preview
@Composable
private fun ContenidoLeccionPreview() {
    ODINTheme(darkTheme = true, dynamicColor = false) {
        Box(Modifier.background(ColorNocheOscura)) {
            ContenidoLeccion(
                titulo = "Fundamentos de la Ingeniería de Software",
                ejercicio = Ejercicio(
                    id = "e1", leccionId = "l1",
                    enunciado = "Selecciona las características de calidad del software:",
                    opciones = listOf("Mantenibilidad", "Color de la interfaz", "Seguridad", "Usabilidad"),
                    respuestasCorrectas = listOf(0, 2, 3), orden = 1
                ),
                numeroEjercicio = 3,
                totalEjercicios = 5,
                errores = 1,
                ultimoResultado = ResultadoRespuesta(EstadoRespuesta.INCOMPLETA, listOf(0, 2, 3)),
                onComprobar = {}, onContinuar = {}, onSalir = {}
            )
        }
    }
}
