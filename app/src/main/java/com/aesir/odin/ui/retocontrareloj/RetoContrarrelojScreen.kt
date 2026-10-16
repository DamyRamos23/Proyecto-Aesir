package com.aesir.odin.ui.retocontrareloj

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.ResultadoContrarreloj
import com.aesir.odin.domain.model.RetoContrareloj
import com.aesir.odin.domain.usecase.reto.AbandonarRetoContrarrelojUseCase
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.DivisorOrnamental
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.PanelTallado
import com.aesir.odin.ui.components.odin.SelloRunico
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import com.aesir.odin.ui.retocontrareloj.RetoContrarrelojViewModel.Estado

/** Segundos restantes a partir de los cuales el contador pasa a estado de urgencia. */
private const val SEGUNDOS_URGENCIA = 15

/**
 * Pantalla de CU-11. El reto mezcla temas de varios mundos, así que usa un
 * estilo fijo (el del primer mundo) en lugar del de un mundo concreto.
 */
@Composable
fun RetoContrarrelojScreen(
    viewModel: RetoContrarrelojViewModel,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsState()
    val pregunta by viewModel.preguntaActual.collectAsState()
    val numeroPregunta by viewModel.numeroPregunta.collectAsState()
    val seleccionadas by viewModel.seleccionadas.collectAsState()
    val tiempo by viewModel.tiempoRestanteSeg.collectAsState()
    val puntaje by viewModel.puntaje.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val saldo by viewModel.saldo.collectAsState()

    val estilo = EstilosMundo.porOrden(1)
    var confirmandoSalida by remember { mutableStateOf(false) }

    // Atrás durante el reto equivale a querer abandonarlo: pide confirmación.
    BackHandler(enabled = estado == Estado.EnCurso) { confirmandoSalida = true }

    FondoMundo(estilo = estilo, modifier = modifier, alturaPaisaje = 300.dp, paisajeInferior = true) {
        when (val e = estado) {
            Estado.Cargando, Estado.Guardando ->
                CircularProgressIndicator(color = OdinPaleta.Brasa, modifier = Modifier.align(Alignment.Center))

            is Estado.Listo -> PreparacionReto(
                estilo = estilo,
                saldo = saldo,
                costo = viewModel.costoEntrada,
                duracionSegundos = viewModel.duracionSegundos,
                aviso = e.aviso,
                onComenzar = viewModel::pagarYComenzar,
                onSalir = onSalir
            )

            Estado.EnCurso -> if (pregunta != null) {
                ContenidoReto(
                    estilo = estilo,
                    pregunta = pregunta!!,
                    numeroPregunta = numeroPregunta,
                    seleccionadas = seleccionadas,
                    tiempoRestanteSeg = tiempo,
                    duracionSegundos = viewModel.duracionSegundos,
                    puntaje = puntaje,
                    onSeleccionar = viewModel::seleccionarOpcion,
                    onConfirmar = viewModel::confirmarRespuesta,
                    onAbandonar = { confirmandoSalida = true }
                )
            }

            Estado.Finalizado, Estado.Abandonado ->
                resultado?.let { ResumenReto(estilo, it, onSalir) }

            Estado.SinTemasEstudiados -> MensajeReto(
                estilo, "Aún no hay runas estudiadas",
                "Completa la lección de al menos un tema para poder enfrentar el reto contrarreloj.", onSalir
            )

            Estado.SinPreguntas -> MensajeReto(
                estilo, "El pozo está vacío",
                "Los temas que estudiaste todavía no tienen preguntas para el reto.", onSalir
            )

            is Estado.Error -> MensajeReto(estilo, "Algo salió mal", e.mensaje, onSalir)
        }

        if (confirmandoSalida && estado == Estado.EnCurso) {
            DialogoAbandonar(
                estilo = estilo,
                onConfirmar = {
                    confirmandoSalida = false
                    viewModel.abandonarReto()
                },
                onSeguir = { confirmandoSalida = false }
            )
        }
    }
}

@Composable
private fun ContenidoReto(
    estilo: EstiloMundo,
    pregunta: PreguntaFlash,
    numeroPregunta: Int,
    seleccionadas: Set<Int>,
    tiempoRestanteSeg: Int,
    duracionSegundos: Int,
    puntaje: Int,
    onSeleccionar: (Int) -> Unit,
    onConfirmar: () -> Unit,
    onAbandonar: () -> Unit
) {
    // Orden visual aleatorio de las opciones, estable por pregunta. La selección
    // se guarda con los índices originales para que la validación no cambie.
    val permutacion = remember(pregunta.id) { pregunta.opciones.indices.shuffled() }
    val urgente = tiempoRestanteSeg <= SEGUNDOS_URGENCIA

    Column(Modifier.fillMaxHeight().fillMaxWidth().padding(horizontal = 14.dp)) {
        BotonVolver("Abandonar", onClick = onAbandonar)

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            SelloRunico(runa = "ᛏ", estilo = estilo, tamano = 52.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "RETO CONTRARRELOJ",
                    color = estilo.acento,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp
                )
                Text("Temas estudiados", style = estiloTitulo(19.sp))
            }
        }

        // Contador general, pregunta actual y puntaje acumulado
        Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    EtiquetaPequena("PREGUNTA")
                    Text("$numeroPregunta", style = estiloTitulo(24.sp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EtiquetaPequena("TIEMPO RESTANTE")
                    val colorTiempo = if (urgente) OdinPaleta.Incorrecto else Color(0xFFFFF6E0)
                    Text(
                        formatearTiempo(tiempoRestanteSeg),
                        style = estiloTitulo(46.sp, color = colorTiempo, brillo = if (urgente) OdinPaleta.Incorrecto else estilo.acento),
                        modifier = Modifier.semantics { contentDescription = "Tiempo restante: $tiempoRestanteSeg segundos" }
                    )
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    EtiquetaPequena("HIDROMIEL")
                    Text("+$puntaje", style = estiloTitulo(24.sp, color = estilo.acento))
                }
            }
            Spacer(Modifier.height(8.dp))
            BarraTiempo(
                fraccion = tiempoRestanteSeg.toFloat() / duracionSegundos.coerceAtLeast(1),
                color = if (urgente) OdinPaleta.Incorrecto else estilo.acento,
                metal = estilo.metal
            )
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
            PanelTallado(estilo = estilo, modifier = Modifier.fillMaxWidth(), semilla = numeroPregunta) {
                Column {
                    Text(
                        pregunta.enunciado,
                        style = estiloTitulo(20.sp).copy(fontWeight = FontWeight.Normal, letterSpacing = 0.sp, lineHeight = 26.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pregunta.esSeleccionMultiple) {
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

                    permutacion.forEachIndexed { posVisual, indiceOriginal ->
                        OpcionReto(
                            letra = ('A' + posVisual).toString(),
                            texto = pregunta.opciones[indiceOriginal],
                            multiple = pregunta.esSeleccionMultiple,
                            seleccionada = indiceOriginal in seleccionadas,
                            estilo = estilo,
                            onClick = { onSeleccionar(indiceOriginal) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }

        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            BotonOdin(
                texto = "Confirmar",
                onClick = onConfirmar,
                acento = estilo.acento,
                habilitado = seleccionadas.isNotEmpty(),
                iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EtiquetaPequena(texto: String) {
    Text(
        texto,
        color = OdinPaleta.HuesoTenue,
        fontFamily = FuenteTitulo,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp,
        letterSpacing = 1.4.sp
    )
}

private fun formatearTiempo(segundos: Int): String {
    val s = segundos.coerceAtLeast(0)
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}

/** Barra con marco metálico que se vacía conforme corre el tiempo general. */
@Composable
private fun BarraTiempo(fraccion: Float, color: Color, metal: Color) {
    val forma = CutCornerShape(5.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(forma)
            .background(Color(0xFF0C0A08))
            .border(2.dp, metal, forma)
            .padding(2.dp)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .background(Brush.horizontalGradient(listOf(metal, color)))
        )
    }
}

/** Opción de respuesta: rombo (una respuesta) o cuadro (varias) con la letra. Sin retroalimentación. */
@Composable
private fun OpcionReto(
    letra: String,
    texto: String,
    multiple: Boolean,
    seleccionada: Boolean,
    estilo: EstiloMundo,
    onClick: () -> Unit
) {
    val color = if (seleccionada) estilo.acento else estilo.metal
    val fondo = if (seleccionada) estilo.acento.copy(alpha = .16f) else Color(0x9E060504)
    val forma = CutCornerShape(8.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clip(forma)
            .background(fondo)
            .border(if (seleccionada) 2.dp else 1.dp, color.copy(alpha = if (seleccionada) 1f else .5f), forma)
            .semantics {
                contentDescription = "Opción $letra: $texto"
                selected = seleccionada
            }
            .clickable(role = if (multiple) Role.Checkbox else Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(40.dp)) {
                val s = size.minDimension
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
                drawPath(exterior, if (seleccionada) estilo.acento.copy(alpha = .22f) else Color.Black.copy(alpha = .45f))
                drawPath(exterior, color, style = Stroke(if (seleccionada) 2.dp.toPx() else 1.3.dp.toPx()))
                drawPath(interior, color.copy(alpha = .45f), style = Stroke(.8.dp.toPx()))
            }
            Text(
                letra,
                color = if (seleccionada) Color(0xFFFFF6E0) else OdinPaleta.Hueso,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            texto,
            color = Color(0xFFE4DCCB),
            fontFamily = FuenteTexto,
            fontSize = 15.5.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Pasos 9 y 10 (resultado) y E3.1 (abandono con penalización). */
@Composable
private fun ResumenReto(estilo: EstiloMundo, resultado: ResultadoContrarreloj, onSalir: () -> Unit) {
    Column(Modifier.fillMaxHeight().fillMaxWidth().padding(horizontal = 14.dp, vertical = 24.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
            PanelTallado(estilo = estilo, modifier = Modifier.fillMaxWidth(), semilla = 3) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (resultado.abandonado) "Reto abandonado" else "Fin del reto",
                        style = estiloTitulo(22.sp),
                        textAlign = TextAlign.Center
                    )
                    DivisorOrnamental(estilo.metal, estilo.acento, Modifier.padding(vertical = 12.dp))
                    EtiquetaPequena(if (resultado.abandonado) "HIDROMIEL PERDIDO" else "HIDROMIEL GANADO")
                    Text(
                        if (resultado.abandonado) "−${resultado.penalizacion}" else "+${resultado.puntaje}",
                        style = estiloTitulo(56.sp, color = Color(0xFFFFF6E0), brillo = estilo.acento)
                    )
                    Spacer(Modifier.height(8.dp))
                    if (resultado.abandonado) {
                        Text(
                            "Perdiste lo ganado en el reto y se restaron ${resultado.penalizacion} de hidromiel.",
                            color = OdinPaleta.Incorrecto,
                            fontFamily = FuenteTexto,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            DatoResumen("ACIERTOS", resultado.aciertos, estilo)
                            DatoResumen("RESPONDIDAS", resultado.respondidas, estilo)
                        }
                    }
                }
            }
        }
        BotonOdin(
            texto = "Volver",
            onClick = onSalir,
            acento = estilo.acento,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DatoResumen(etiqueta: String, valor: Int, estilo: EstiloMundo) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        EtiquetaPequena(etiqueta)
        Text("$valor", style = estiloTitulo(26.sp, color = estilo.acento))
    }
}

/** Reglas del reto y pago de la entrada antes de empezar. */
@Composable
private fun PreparacionReto(
    estilo: EstiloMundo,
    saldo: Int,
    costo: Int,
    duracionSegundos: Int,
    aviso: String?,
    onComenzar: () -> Unit,
    onSalir: () -> Unit
) {
    val alcanza = saldo >= costo
    Column(Modifier.fillMaxHeight().fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BotonVolver("Volver", onClick = onSalir, modifier = Modifier.weight(1f))
            ContadorHidromiel(saldo = saldo)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            PanelTallado(estilo = estilo, modifier = Modifier.fillMaxWidth(), semilla = 5) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    SelloRunico(runa = "ᛏ", estilo = estilo, tamano = 60.dp)
                    Spacer(Modifier.height(10.dp))
                    Text("Reto contrarreloj", style = estiloTitulo(24.sp), textAlign = TextAlign.Center)
                    DivisorOrnamental(estilo.metal, estilo.acento, Modifier.padding(vertical = 12.dp))
                    ReglaReto("Tienes ${formatearTiempo(duracionSegundos)} para responder preguntas de los temas que ya estudiaste.")
                    ReglaReto("Cada respuesta correcta te da +${RetoContrareloj.PUNTOS_POR_ACIERTO} de hidromiel al terminar.")
                    ReglaReto("Entrar cuesta $costo de hidromiel.")
                    ReglaReto("Si abandonas pierdes lo ganado y se restan ${AbandonarRetoContrarrelojUseCase.PENALIZACION} de hidromiel.")
                    val nota = aviso ?: if (!alcanza) "Te faltan ${costo - saldo} de hidromiel para entrar." else null
                    if (nota != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            nota,
                            color = OdinPaleta.Incorrecto,
                            fontFamily = FuenteTexto,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        BotonOdin(
            texto = "Pagar $costo y comenzar",
            onClick = onComenzar,
            acento = estilo.acento,
            habilitado = alcanza,
            iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ReglaReto(texto: String) {
    Text(
        "᛫ $texto",
        color = Color(0xFFE4DCCB),
        fontFamily = FuenteTexto,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    )
}

/** Reto no disponible (sin temas estudiados, sin preguntas o error). */
@Composable
private fun MensajeReto(estilo: EstiloMundo, titulo: String, mensaje: String, onSalir: () -> Unit) {
    Column(Modifier.fillMaxHeight().fillMaxWidth().padding(horizontal = 14.dp, vertical = 24.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            PanelTallado(estilo = estilo, modifier = Modifier.fillMaxWidth(), semilla = 7) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(titulo, style = estiloTitulo(21.sp), textAlign = TextAlign.Center)
                    DivisorOrnamental(estilo.metal, estilo.acento, Modifier.padding(vertical = 12.dp))
                    Text(
                        mensaje,
                        color = Color(0xFFE4DCCB),
                        fontFamily = FuenteTexto,
                        fontSize = 15.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        BotonOdin(
            texto = "Volver",
            onClick = onSalir,
            acento = estilo.acento,
            primario = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DialogoAbandonar(estilo: EstiloMundo, onConfirmar: () -> Unit, onSeguir: () -> Unit) {
    AlertDialog(
        onDismissRequest = onSeguir,
        containerColor = OdinPaleta.HollinAlto,
        titleContentColor = OdinPaleta.Incorrecto,
        textContentColor = OdinPaleta.Hueso,
        title = { Text("¿Abandonar el reto?", fontFamily = FuenteTitulo) },
        text = {
            Text(
                "Si abandonas, pierdes el hidromiel ganado en el reto y se restan " +
                    "${AbandonarRetoContrarrelojUseCase.PENALIZACION} de hidromiel. El tiempo sigue corriendo mientras decides.",
                fontFamily = FuenteTexto
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Abandonar", color = OdinPaleta.Incorrecto, fontFamily = FuenteTitulo)
            }
        },
        dismissButton = {
            TextButton(onClick = onSeguir) {
                Text("Seguir luchando", color = estilo.acento, fontFamily = FuenteTitulo)
            }
        }
    )
}
