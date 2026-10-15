package com.aesir.odin.ui.diagnostico

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.model.ResultadoDiagnostico
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.PanelTallado
import com.aesir.odin.ui.components.odin.PuntosProgreso
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta

/**
 * CU-10: evaluación diagnóstica de un mundo (EvaluacionDiagnosticaScreen del
 * diagrama de clases). Una pregunta a la vez y sin retroalimentación.
 *
 * - "Omitir evaluación" solo aparece antes de responder la primera pregunta
 *   (A1.1 ocurre en el paso 1 del flujo normal) y pide confirmación.
 * - Salir con la flecha o con "atrás" abandona la evaluación (A6.1).
 */
@Composable
fun EvaluacionDiagnosticaScreen(
    viewModel: EvaluacionDiagnosticaViewModel,
    mundoId: String,
    onFinalizada: (ResultadoDiagnostico) -> Unit,
    onOmitida: () -> Unit,
    onAbandonar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preguntas by viewModel.preguntas.collectAsState()
    val indice by viewModel.indiceActual.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val confirmandoOmision by viewModel.confirmandoOmision.collectAsState()
    val omitida by viewModel.omitida.collectAsState()
    val mundo by viewModel.mundo.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()

    LaunchedEffect(mundoId) { viewModel.iniciar(mundoId) }
    LaunchedEffect(resultado) { resultado?.let(onFinalizada) }
    LaunchedEffect(omitida) { if (omitida) onOmitida() }

    val abandonar: () -> Unit = {
        viewModel.abandonar()
        onAbandonar()
    }
    BackHandler(enabled = resultado == null && !omitida, onBack = abandonar)

    val estilo = EstilosMundo.porOrden(mundo?.orden ?: 1)
    val pregunta = preguntas.getOrNull(indice)

    FondoMundo(estilo = estilo, modifier = modifier, alturaPaisaje = 260.dp) {
        when {
            mensajeError != null -> Column(
                Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(mensajeError.orEmpty(), color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 16.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                BotonOdin(texto = "Volver", onClick = abandonar, acento = estilo.acento)
            }
            pregunta == null -> CircularProgressIndicator(color = estilo.acento, modifier = Modifier.align(Alignment.Center))
            else -> ContenidoPregunta(
                estilo = estilo,
                nombreMundo = mundo?.nombre.orEmpty(),
                pregunta = pregunta,
                indice = indice,
                total = preguntas.size,
                puedeOmitir = indice == 0,
                onResponder = viewModel::responder,
                onOmitir = viewModel::solicitarOmision,
                onAbandonar = abandonar
            )
        }
    }

    if (confirmandoOmision) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarOmision,
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Hueso,
            textContentColor = OdinPaleta.Hueso,
            title = { Text("¿Omitir la evaluación?", fontFamily = FuenteTitulo) },
            text = {
                // RF-93
                Text(
                    "Si la omites, no tendrás otra oportunidad de presentar la evaluación diagnóstica de " +
                        "${mundo?.nombre ?: "este mundo"} y comenzarás desde el primer nivel disponible.",
                    fontFamily = FuenteTexto
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::cancelarOmision) {
                    Text("Presentar evaluación", color = estilo.acento, fontFamily = FuenteTitulo)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::confirmarOmision) {
                    Text("Omitir", color = OdinPaleta.Incorrecto, fontFamily = FuenteTitulo)
                }
            }
        )
    }
}

@Composable
private fun ContenidoPregunta(
    estilo: EstiloMundo,
    nombreMundo: String,
    pregunta: PreguntaDiagnostica,
    indice: Int,
    total: Int,
    puedeOmitir: Boolean,
    onResponder: (List<Int>) -> Unit,
    onOmitir: () -> Unit,
    onAbandonar: () -> Unit
) {
    // La selección se reinicia con cada pregunta
    var seleccion by remember(pregunta.id) { mutableStateOf(emptySet<Int>()) }
    val multiple = pregunta.esSeleccionMultiple

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.fillMaxWidth()) {
            BotonVolver(nombreMundo.ifEmpty { "Mundos" }, onClick = onAbandonar, modifier = Modifier.align(Alignment.CenterStart))
        }
        Text(
            "EVALUACIÓN DIAGNÓSTICA",
            color = estilo.acento,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            "Pregunta ${indice + 1} de $total",
            style = estiloTitulo(26.sp, OdinPaleta.Hueso, brillo = estilo.acento)
        )
        Spacer(Modifier.height(10.dp))
        PuntosProgreso(total = total, actual = indice, acento = estilo.acento, metal = estilo.metal)

        if (puedeOmitir) {
            Text(
                "Responde lo que sepas: los temas que demuestres dominar se marcarán como conocidos " +
                    "y se abrirá el camino desde ahí. No otorga hidromiel.",
                color = OdinPaleta.HuesoTenue,
                fontFamily = FuenteTexto,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .padding(top = 12.dp)
            )
        }

        Spacer(Modifier.height(18.dp))
        PanelTallado(estilo = estilo, semilla = 9 + indice, modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
            Column(Modifier.padding(vertical = 4.dp)) {
                Text(pregunta.enunciado, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 18.sp, lineHeight = 24.sp)
                if (multiple) {
                    Text(
                        "Selecciona todas las correctas.",
                        color = estilo.acento,
                        fontFamily = FuenteTexto,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Column(
            Modifier.widthIn(max = 520.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            pregunta.opciones.forEachIndexed { i, opcion ->
                OpcionDiagnostico(
                    letra = ('A' + i).toString(),
                    texto = opcion,
                    multiple = multiple,
                    seleccionada = i in seleccion,
                    estilo = estilo,
                    onClick = {
                        seleccion = when {
                            !multiple -> setOf(i)
                            i in seleccion -> seleccion - i
                            else -> seleccion + i
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        BotonOdin(
            texto = if (indice + 1 == total) "Terminar evaluación" else "Responder",
            onClick = { onResponder(seleccion.toList()) },
            acento = estilo.acento,
            habilitado = seleccion.isNotEmpty(),
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()
        )
        if (puedeOmitir) {
            Spacer(Modifier.height(10.dp))
            BotonOdin(
                texto = "Omitir evaluación",
                onClick = onOmitir,
                acento = estilo.acento,
                primario = false,
                metal = estilo.metal,
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OpcionDiagnostico(
    letra: String,
    texto: String,
    multiple: Boolean,
    seleccionada: Boolean,
    estilo: EstiloMundo,
    onClick: () -> Unit
) {
    val forma = CutCornerShape(8.dp)
    val color = if (seleccionada) estilo.acento else estilo.metal
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clip(forma)
            .background(if (seleccionada) estilo.acento.copy(alpha = .16f) else Color(0x9E060504))
            .border(if (seleccionada) 2.dp else 1.dp, color.copy(alpha = if (seleccionada) 1f else .5f), forma)
            .semantics {
                contentDescription = "Opción $letra: $texto"
                selected = seleccionada
            }
            .clickable(role = if (multiple) Role.Checkbox else Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rombo (una respuesta) o cuadro (varias respuestas), como en las lecciones
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(36.dp)) {
                val s = size.minDimension
                val figura = if (multiple) {
                    Path().apply { moveTo(s * .12f, s * .12f); lineTo(s * .88f, s * .12f); lineTo(s * .88f, s * .88f); lineTo(s * .12f, s * .88f); close() }
                } else {
                    Path().apply { moveTo(s / 2, s * .04f); lineTo(s * .96f, s / 2); lineTo(s / 2, s * .96f); lineTo(s * .04f, s / 2); close() }
                }
                drawPath(figura, if (seleccionada) estilo.acento.copy(alpha = .25f) else Color.Black.copy(alpha = .45f))
                drawPath(figura, color, style = Stroke(1.5.dp.toPx()))
            }
            Text(letra, color = if (seleccionada) estilo.acento else OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontSize = 15.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(texto, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 16.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
    }
}
