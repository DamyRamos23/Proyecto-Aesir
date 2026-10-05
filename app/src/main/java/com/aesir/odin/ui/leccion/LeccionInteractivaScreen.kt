package com.aesir.odin.ui.leccion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.Ejercicio
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.ResultadoLeccion
import com.aesir.odin.domain.model.ResultadoRespuesta
import com.aesir.odin.domain.usecase.leccion.RegistrarErrorLeccionUseCase
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.DivisorOrnamental
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.MarcadorErrores
import com.aesir.odin.ui.components.odin.PanelTallado
import com.aesir.odin.ui.components.odin.PuntosProgreso
import com.aesir.odin.ui.components.odin.SelloRunico
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.ODINTheme
import com.aesir.odin.ui.theme.OdinPaleta

@Composable
fun LeccionInteractivaScreen(
    viewModel: LeccionInteractivaViewModel,
    onLeccionFinalizada: (ResultadoLeccion, totalEjercicios: Int) -> Unit,
    onLeccionFallida: () -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val leccion by viewModel.leccion.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
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
    val ctx = contexto
    val estilo = EstilosMundo.porOrden(ctx?.mundo?.orden ?: 1)

    FondoMundo(
        estilo = if (leccionActual != null) estilo else null,
        modifier = modifier,
        alturaPaisaje = 300.dp,
        paisajeInferior = true
    ) {
        if (leccionActual == null || ejercicio == null) {
            CircularProgressIndicator(color = OdinPaleta.Brasa, modifier = Modifier.align(Alignment.Center))
        } else {
            ContenidoLeccion(
                estilo = estilo,
                nombreMundo = ctx?.mundo?.nombre ?: "",
                runa = estilo.runaDeTema(ctx?.tema?.orden ?: 1),
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
            DialogoLeccionFallida(estilo = estilo, onVolverRoadmap = onLeccionFallida)
        }
    }
}

@Composable
private fun ContenidoLeccion(
    estilo: EstiloMundo,
    nombreMundo: String,
    runa: String,
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

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        BotonVolver("Salir", onClick = onSalir)

        // Cabecera: sello del tema, mundo y título de la lección
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            SelloRunico(runa = runa, estilo = estilo, tamano = 52.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    nombreMundo.uppercase(),
                    color = estilo.acento,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp
                )
                Text(titulo, style = estiloTitulo(19.sp), maxLines = 2)
            }
        }

        // Progreso y errores
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pregunta $numeroEjercicio de $totalEjercicios", color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 13.5.sp)
            Spacer(Modifier.width(8.dp))
            PuntosProgreso(
                total = totalEjercicios,
                actual = numeroEjercicio - 1 + if (respondido) 1 else 0,
                acento = estilo.acento,
                metal = estilo.metal
            )
            Spacer(Modifier.weight(1f))
            MarcadorErrores(
                errores,
                RegistrarErrorLeccionUseCase.LIMITE_ERRORES,
                estilo.metal,
                Modifier.semantics { contentDescription = "Errores: $errores de ${RegistrarErrorLeccionUseCase.LIMITE_ERRORES}" }
            )
        }

        // Panel de la pregunta
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
        ) {
            PanelTallado(estilo = estilo, modifier = Modifier.fillMaxWidth(), semilla = numeroEjercicio) {
                Column {
                    Text(
                        ejercicio.enunciado,
                        style = estiloTitulo(20.sp).copy(fontWeight = FontWeight.Normal, letterSpacing = 0.sp, lineHeight = 26.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (ejercicio.esSeleccionMultiple) {
                        Text(
                            "Selecciona todas las respuestas correctas",
                            color = estilo.acento,
                            fontFamily = FuenteTexto,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                    DivisorOrnamental(estilo.metal, estilo.acento, Modifier.padding(vertical = 12.dp))

                    ejercicio.opciones.forEachIndexed { indice, opcion ->
                        OpcionRunica(
                            letra = ('A' + indice).toString(),
                            texto = opcion,
                            multiple = ejercicio.esSeleccionMultiple,
                            seleccionada = indice in seleccion,
                            estado = estadoOpcion(indice, seleccion, ultimoResultado),
                            respondido = respondido,
                            estilo = estilo,
                            onClick = {
                                seleccion = when {
                                    ejercicio.esSeleccionMultiple && indice in seleccion -> seleccion - indice
                                    ejercicio.esSeleccionMultiple -> seleccion + indice
                                    else -> setOf(indice)
                                }
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    AnimatedVisibility(visible = ultimoResultado != null, enter = fadeIn() + expandVertically()) {
                        if (ultimoResultado != null) {
                            Retroalimentacion(ultimoResultado, ejercicio)
                        }
                    }
                }
            }
        }

        // Botón inferior
        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            if (respondido) {
                BotonOdin(
                    texto = if (esUltimo) "Ver resultados" else "Continuar",
                    onClick = onContinuar,
                    acento = estilo.acento,
                    iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                BotonOdin(
                    texto = "Comprobar",
                    onClick = { onComprobar(seleccion.sorted()) },
                    acento = estilo.acento,
                    habilitado = seleccion.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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
private fun OpcionRunica(
    letra: String,
    texto: String,
    multiple: Boolean,
    seleccionada: Boolean,
    estado: EstadoOpcion,
    respondido: Boolean,
    estilo: EstiloMundo,
    onClick: () -> Unit
) {
    val color = when (estado) {
        EstadoOpcion.CORRECTA -> OdinPaleta.Correcto
        EstadoOpcion.INCORRECTA -> OdinPaleta.Incorrecto
        EstadoOpcion.FALTANTE -> OdinPaleta.Incompleto
        EstadoOpcion.NEUTRA -> if (seleccionada && !respondido) estilo.acento else estilo.metal
    }
    val resaltada = estado != EstadoOpcion.NEUTRA || (seleccionada && !respondido)
    val atenuada = respondido && estado == EstadoOpcion.NEUTRA
    val fondo = when {
        estado == EstadoOpcion.CORRECTA -> Color(0x383CA050)
        estado == EstadoOpcion.INCORRECTA -> Color(0x38B4321E)
        estado == EstadoOpcion.FALTANTE -> Color(0x30E0A33A)
        seleccionada && !respondido -> estilo.acento.copy(alpha = .16f)
        else -> Color(0x9E060504)
    }
    val forma = CutCornerShape(8.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .clip(forma)
            .background(fondo)
            .border(if (resaltada) 2.dp else 1.dp, color.copy(alpha = if (resaltada) 1f else .5f), forma)
            .semantics {
                contentDescription = "Opción $letra: $texto"
                selected = seleccionada
            }
            .clickable(enabled = !respondido, role = if (multiple) Role.Checkbox else Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rombo (una respuesta) o cuadro (varias respuestas) con la letra
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(40.dp)) {
                val s = size.minDimension
                val relleno = when {
                    estado == EstadoOpcion.CORRECTA -> OdinPaleta.Correcto.copy(alpha = .3f)
                    estado == EstadoOpcion.INCORRECTA -> OdinPaleta.Incorrecto.copy(alpha = .3f)
                    seleccionada && !respondido -> estilo.acento.copy(alpha = .22f)
                    else -> Color.Black.copy(alpha = .45f)
                }
                val exterior = if (multiple) {
                    Path().apply { moveTo(s * .12f, s * .12f); lineTo(s * .88f, s * .12f); lineTo(s * .88f, s * .88f); lineTo(s * .12f, s * .88f); close() }
                } else {
                    Path().apply { moveTo(s / 2, s * .04f); lineTo(s * .96f, s / 2); lineTo(s / 2, s * .96f); lineTo(s * .04f, s / 2); close() }
                }
                val interior = if (multiple) {
                    Path().apply { moveTo(s * .22f, s * .22f); lineTo(s * .78f, s * .22f); lineTo(s * .78f, s * .78f); lineTo(s * .22f, s * .78f); close() }
                } else {
                    Path().apply { moveTo(s / 2, s * .17f); lineTo(s * .83f, s / 2); lineTo(s / 2, s * .83f); lineTo(s * .17f, s / 2); close() }
                }
                drawPath(exterior, relleno)
                drawPath(exterior, color, style = Stroke(if (resaltada) 2.dp.toPx() else 1.3.dp.toPx()))
                drawPath(interior, color.copy(alpha = .45f), style = Stroke(.8.dp.toPx()))
            }
            Text(
                letra,
                color = when {
                    atenuada -> Color(0xFF8F877A)
                    resaltada -> Color(0xFFFFF6E0)
                    else -> OdinPaleta.Hueso
                },
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            texto,
            color = if (atenuada) Color(0xFF8F877A) else Color(0xFFE4DCCB),
            fontFamily = FuenteTexto,
            fontSize = 15.5.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
        val marca = when (estado) {
            EstadoOpcion.CORRECTA -> Icons.Filled.Check to OdinPaleta.Correcto
            EstadoOpcion.INCORRECTA -> Icons.Filled.Close to OdinPaleta.Incorrecto
            EstadoOpcion.FALTANTE -> Icons.Filled.Check to OdinPaleta.Incompleto
            EstadoOpcion.NEUTRA -> null
        }
        if (marca != null) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(22.dp).clip(CircleShape).background(marca.second),
                contentAlignment = Alignment.Center
            ) {
                Icon(marca.first, contentDescription = null, tint = Color(0xFF120E0B), modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun Retroalimentacion(resultado: ResultadoRespuesta, ejercicio: Ejercicio) {
    val (titulo, color) = when (resultado.estado) {
        EstadoRespuesta.CORRECTA -> "¡Correcto!" to OdinPaleta.Correcto
        EstadoRespuesta.INCOMPLETA -> "Te faltó alguna" to OdinPaleta.Incompleto
        EstadoRespuesta.INCORRECTA -> "Casi" to OdinPaleta.Incorrecto
    }
    val forma = CutCornerShape(6.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .clip(forma)
            .background(color.copy(alpha = .16f))
            .border(1.dp, color, forma)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(Modifier.size(30.dp)) {
            val s = size.minDimension
            val rombo = Path().apply { moveTo(s / 2, 0f); lineTo(s, s / 2); lineTo(s / 2, s); lineTo(0f, s / 2); close() }
            drawPath(rombo, color)
            if (resultado.estado == EstadoRespuesta.CORRECTA) {
                val palomita = Path().apply { moveTo(s * .3f, s * .52f); lineTo(s * .44f, s * .65f); lineTo(s * .7f, s * .38f) }
                drawPath(palomita, Color(0xFF0D1A10), style = Stroke(2.4.dp.toPx()))
            } else {
                drawLine(Color(0xFF1A0805), Offset(s / 2, s * .27f), Offset(s / 2, s * .57f), 2.6.dp.toPx())
                drawCircle(Color(0xFF1A0805), 1.5.dp.toPx(), Offset(s / 2, s * .7f))
            }
        }
        Column {
            Text(titulo, color = color, fontFamily = FuenteTitulo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            val explicacion = if (resultado.estado == EstadoRespuesta.CORRECTA) {
                "Bien hecho, sigue así."
            } else {
                val correctas = resultado.opcionesCorrectas.joinToString("\n") { "• ${ejercicio.opciones[it]}" }
                if (resultado.opcionesCorrectas.size > 1) "Las respuestas correctas son:\n$correctas" else "La respuesta correcta es:\n$correctas"
            }
            Text(explicacion, color = Color(0xFFCFC6B5), fontFamily = FuenteTexto, fontSize = 14.sp, lineHeight = 18.sp)
        }
    }
}

/** Flujo alternativo A4.1: se alcanzó el límite de errores. */
@Composable
private fun DialogoLeccionFallida(estilo: EstiloMundo, onVolverRoadmap: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = OdinPaleta.HollinAlto,
        titleContentColor = OdinPaleta.Incorrecto,
        textContentColor = OdinPaleta.Hueso,
        title = { Text("La runa se resistió", fontFamily = FuenteTitulo) },
        text = {
            Text(
                "Cometiste ${RegistrarErrorLeccionUseCase.LIMITE_ERRORES} errores, así que la lección " +
                    "terminó sin recompensa.\n\nTe sugerimos repasar la introducción del tema y " +
                    "practicar con preguntas flash antes de volver a intentarlo.",
                fontFamily = FuenteTexto
            )
        },
        confirmButton = {
            TextButton(onClick = onVolverRoadmap) {
                Text("Volver al roadmap", color = estilo.acento, fontFamily = FuenteTitulo)
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0C1214, heightDp = 852)
@Composable
private fun ContenidoLeccionPreview() {
    ODINTheme(darkTheme = true, dynamicColor = false) {
        FondoMundo(estilo = EstilosMundo.Piedra, paisajeInferior = true) {
            ContenidoLeccion(
                estilo = EstilosMundo.Piedra,
                nombreMundo = "Casos de Uso",
                runa = "ᚾ",
                titulo = "Include y Extend",
                ejercicio = Ejercicio(
                    id = "e1", leccionId = "l1",
                    enunciado = "Selecciona escenarios correctos para usar <<include>>:",
                    opciones = listOf(
                        "Reutilizar 'Iniciar Sesión' en 'Realizar Compra' y 'Ver Perfil'",
                        "Agregar seguro de viaje si el usuario marca la casilla",
                        "Evitar duplicar los mismos pasos en múltiples casos de uso",
                        "Manejar un error de red"
                    ),
                    respuestasCorrectas = listOf(0, 2), orden = 5
                ),
                numeroEjercicio = 5,
                totalEjercicios = 5,
                errores = 1,
                ultimoResultado = ResultadoRespuesta(EstadoRespuesta.INCOMPLETA, listOf(0, 2)),
                onComprobar = {}, onContinuar = {}, onSalir = {}
            )
        }
    }
}
