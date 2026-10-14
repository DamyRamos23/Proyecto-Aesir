package com.aesir.odin.ui.ragnarok

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.graphics.PathEffect
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
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.ReglasReto
import com.aesir.odin.domain.model.ResultadoRetoFinMundo
import com.aesir.odin.domain.model.RetoFinMundo
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.DivisorOrnamental
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.PanelTallado
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta

private val RUNAS_BATALLA = listOf("ᚠ", "ᚢ", "ᚦ", "ᚨ", "ᚱ", "ᚲ", "ᚷ", "ᚹ", "ᚺ", "ᚾ", "ᛁ", "ᛃ")

/**
 * CU-08 Resolver reto de fin de mundo: el Ragnarök.
 *
 * Entrada con las reglas, batalla de preguntas (una o varias opciones, con
 * retroalimentación inmediata) y resultado. Salir antes de terminar (A6.1)
 * pide confirmación, termina el reto con puntaje 0 y cuenta como abandonar
 * el nivel (MOD-01).
 */
@Composable
fun RagnarokScreen(
    viewModel: RagnarokViewModel,
    temaId: String,
    onVolverRoadmap: () -> Unit,
    onIrAlMundo: (String) -> Unit,
    onRepasarLeccion: (String) -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ui by viewModel.ui.collectAsState()
    val batalla by viewModel.batalla.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
    val aviso by viewModel.aviso.collectAsState()
    var confirmarHuida by remember { mutableStateOf(false) }

    LaunchedEffect(temaId) { viewModel.iniciar(temaId) }

    val enCurso = ui is RagnarokUi.Entrada || ui is RagnarokUi.Batalla
    // A6.1: salir antes de terminar pide confirmación.
    BackHandler(enabled = enCurso) { confirmarHuida = true }

    val estilo = EstilosMundo.porOrden(contexto?.mundo?.orden ?: 1)
    val nombreMundo = contexto?.mundo?.nombre.orEmpty()

    FondoMundo(estilo = estilo, modifier = modifier, alturaPaisaje = 260.dp) {
        when (val actual = ui) {
            RagnarokUi.Cargando -> CircularProgressIndicator(color = OdinPaleta.Brasa, modifier = Modifier.align(Alignment.Center))
            is RagnarokUi.Bloqueado -> PanelMensaje(
                estilo = estilo,
                titulo = "El Ragnarök aún duerme",
                texto = "Completa todos los temas del mundo para despertarlo." +
                    if (actual.temasPendientes.isEmpty()) "" else "\n\nTe faltan:\n" + actual.temasPendientes.joinToString("\n") { "• $it" },
                accion = "Volver al roadmap",
                onAccion = onVolverRoadmap
            )
            is RagnarokUi.Error -> PanelMensaje(
                estilo = estilo,
                titulo = "No se pudo abrir el reto",
                texto = actual.mensaje,
                accion = "Volver al roadmap",
                onAccion = onVolverRoadmap
            )
            is RagnarokUi.Entrada -> PantallaEntrada(
                estilo = estilo,
                nombreMundo = nombreMundo,
                reto = actual.reto,
                onDesatar = viewModel::iniciarBatalla,
                onSalir = { confirmarHuida = true }
            )
            RagnarokUi.Batalla -> batalla?.let {
                PantallaBatalla(
                    estilo = estilo,
                    nombreMundo = nombreMundo,
                    estado = it,
                    onConfirmar = viewModel::responder,
                    onSiguiente = viewModel::siguiente,
                    onSalir = { confirmarHuida = true }
                )
            }
            is RagnarokUi.Resultado -> {
                if (actual.resultado.superado) {
                    PantallaSuperado(estilo, nombreMundo, actual.resultado, onVolverRoadmap, onIrAlMundo)
                } else {
                    PantallaNoSuperado(
                        estilo = estilo,
                        resultado = actual.resultado,
                        onReintentar = viewModel::reintentar,
                        onRepasar = onRepasarLeccion,
                        onVolver = onVolverRoadmap
                    )
                }
            }
        }

        if (confirmarHuida) {
            DialogoHuida(
                estilo = estilo,
                antesDeEmpezar = ui is RagnarokUi.Entrada,
                onSeguir = { confirmarHuida = false },
                onAbandonar = {
                    confirmarHuida = false
                    onSalir()
                }
            )
        }
        if (aviso != null) {
            AlertDialog(
                onDismissRequest = viewModel::limpiarAviso,
                containerColor = OdinPaleta.HollinAlto,
                titleContentColor = OdinPaleta.Hueso,
                textContentColor = OdinPaleta.Hueso,
                title = { Text("No puedes reintentarlo", fontFamily = FuenteTitulo) },
                text = { Text(aviso.orEmpty(), fontFamily = FuenteTexto) },
                confirmButton = {
                    TextButton(onClick = viewModel::limpiarAviso) {
                        Text("Entendido", color = estilo.acento, fontFamily = FuenteTitulo)
                    }
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Entrada (paso 4: instrucciones y puntaje mínimo)
// ---------------------------------------------------------------------------

@Composable
private fun PantallaEntrada(
    estilo: EstiloMundo,
    nombreMundo: String,
    reto: RetoFinMundo,
    onDesatar: () -> Unit,
    onSalir: () -> Unit
) {
    val total = reto.preguntas.size
    val minimoAciertos = (ReglasReto.PUNTAJE_MINIMO * total + 99) / 100
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        BotonVolver(nombreMundo, onClick = onSalir)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Eclipse(estilo, Modifier.fillMaxWidth().height(190.dp))
            Text(
                "RETO DE FIN DE MUNDO",
                color = estilo.acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 3.sp
            )
            Text("RAGNARÖK", style = estiloTitulo(44.sp, Color(0xFFF3E2BC), brillo = estilo.acento))
            Text(
                "«Fenrir rompe sus cadenas y la serpiente emerge del mar. " +
                    "Demuestra lo aprendido en $nombreMundo o el mundo arderá.»",
                color = Color(0xFFD8CFBE),
                fontFamily = FuenteTexto,
                fontStyle = FontStyle.Italic,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)
            )
            PanelTallado(estilo = estilo, semilla = 41, modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Regla(estilo, "$total preguntas", "Al azar, de los ${reto.temasCubiertos} temas de $nombreMundo")
                    Regla(
                        estilo,
                        "Puntaje mínimo: ${ReglasReto.PUNTAJE_MINIMO}",
                        "Necesitas $minimoAciertos aciertos para superar el mundo"
                    )
                    Regla(estilo, "Sin retirada", "Si abandonas, el reto termina con puntaje 0")
                }
            }
        }
        BotonOdin(
            texto = "Desatar el Ragnarök",
            onClick = onDesatar,
            acento = estilo.acento,
            iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
        )
    }
}

@Composable
private fun Regla(estilo: EstiloMundo, titulo: String, detalle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(22.dp)) {
            val s = size.minDimension
            val rombo = Path().apply { moveTo(s / 2, 0f); lineTo(s, s / 2); lineTo(s / 2, s); lineTo(0f, s / 2); close() }
            drawPath(rombo, estilo.acento.copy(alpha = .2f))
            drawPath(rombo, estilo.acento, style = Stroke(1.5.dp.toPx()))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(titulo, color = Color(0xFFF3E2BC), fontFamily = FuenteTitulo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(detalle, color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 13.5.sp, lineHeight = 18.sp)
        }
    }
}

/** Eclipse sobre el árbol del mundo: la ilustración de entrada al Ragnarök. */
@Composable
private fun Eclipse(estilo: EstiloMundo, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val r = size.height * .32f
        val c = Offset(size.width / 2f, size.height / 2f)
        mancha(c, r * 2.6f, estilo.acento, .28f)
        mancha(c, r * 1.7f, estilo.acento, .35f)
        drawCircle(Color(0xFF050303), r, c)
        drawCircle(estilo.acento, r, c, style = Stroke(3.dp.toPx()))
        drawCircle(Color.White.copy(alpha = .7f), r + 5.dp.toPx(), c, style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 14f))))
        // Rayos de la corona
        for (k in 0 until 16) {
            val a = Math.toRadians(k * 22.5)
            val dir = Offset(Math.cos(a).toFloat(), Math.sin(a).toFloat())
            drawLine(
                estilo.acento.copy(alpha = .5f),
                c + dir * (r * 1.12f), c + dir * (r * (if (k % 2 == 0) 1.55f else 1.35f)),
                1.5.dp.toPx()
            )
        }
        dibujarEmblema(estilo.emblema, c, r * .55f, estilo.acento.copy(alpha = .45f))
    }
}

// ---------------------------------------------------------------------------
// Batalla (pasos 5 a 8)
// ---------------------------------------------------------------------------

private enum class EstadoOpcionBatalla { NEUTRA, SELECCIONADA, CORRECTA, INCORRECTA, FALTANTE }

@Composable
private fun PantallaBatalla(
    estilo: EstiloMundo,
    nombreMundo: String,
    estado: EstadoBatalla,
    onConfirmar: (List<Int>) -> Unit,
    onSiguiente: () -> Unit,
    onSalir: () -> Unit
) {
    val pregunta = estado.pregunta
    var seleccion by remember(pregunta.id) { mutableStateOf(emptySet<Int>()) }
    val respuesta = estado.respuesta
    val elegidas = if (respuesta != null) estado.seleccionadas.toSet() else seleccion

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        BotonVolver(nombreMundo, onClick = onSalir)
        Text(
            "BATALLA FINAL · ${estado.numero.toString().padStart(2, '0')}/${estado.total}",
            color = estilo.acento,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 2.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        RunasProgreso(estado, estilo, Modifier.fillMaxWidth().padding(top = 6.dp))
        BarraBatalla(estado, estilo, Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp))

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Tema · ${pregunta.temaNombre}",
                color = OdinPaleta.HuesoTenue,
                fontFamily = FuenteTexto,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp
            )
            Text(
                pregunta.enunciado,
                style = estiloTitulo(21.sp).copy(fontWeight = FontWeight.Normal, letterSpacing = 0.sp, lineHeight = 27.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
            Text(
                if (pregunta.esSeleccionMultiple) "SELECCIONA TODAS LAS QUE APLIQUEN" else "SELECCIONA UNA RESPUESTA",
                color = estilo.acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(10.dp))
            pregunta.opciones.forEachIndexed { i, texto ->
                OpcionBatalla(
                    texto = texto,
                    multiple = pregunta.esSeleccionMultiple,
                    estado = estadoOpcion(i, i in elegidas, pregunta.respuestasCorrectas, respuesta != null),
                    bloqueada = respuesta != null,
                    estilo = estilo,
                    onClick = {
                        seleccion = when {
                            pregunta.esSeleccionMultiple && i in seleccion -> seleccion - i
                            pregunta.esSeleccionMultiple -> seleccion + i
                            else -> setOf(i)
                        }
                    }
                )
                Spacer(Modifier.height(9.dp))
            }
            AnimatedVisibility(visible = respuesta != null, enter = fadeIn() + expandVertically()) {
                if (respuesta != null) Retroalimentacion(respuesta.estado, pregunta.opciones, pregunta.respuestasCorrectas, elegidas)
            }
            Spacer(Modifier.height(8.dp))
        }

        if (respuesta == null) {
            BotonOdin(
                texto = "Confirmar",
                onClick = { onConfirmar(seleccion.sorted()) },
                acento = estilo.acento,
                habilitado = seleccion.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        } else {
            BotonOdin(
                texto = if (estado.esUltima) "Ver resultado" else "Siguiente pregunta",
                onClick = onSiguiente,
                acento = estilo.acento,
                iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        }
    }
}

private fun estadoOpcion(indice: Int, elegida: Boolean, correctas: List<Int>, respondida: Boolean): EstadoOpcionBatalla {
    if (!respondida) return if (elegida) EstadoOpcionBatalla.SELECCIONADA else EstadoOpcionBatalla.NEUTRA
    val esCorrecta = indice in correctas
    return when {
        esCorrecta && elegida -> EstadoOpcionBatalla.CORRECTA
        esCorrecta -> EstadoOpcionBatalla.FALTANTE
        elegida -> EstadoOpcionBatalla.INCORRECTA
        else -> EstadoOpcionBatalla.NEUTRA
    }
}

/** Una runa por pregunta: encendida si se acertó, roja si se falló, con aro la que está en juego. */
@Composable
private fun RunasProgreso(estado: EstadoBatalla, estilo: EstiloMundo, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        estado.progreso.forEachIndexed { i, resultado ->
            val actual = i == estado.numero - 1
            val color = when {
                resultado == EstadoRespuesta.CORRECTA -> estilo.acento
                resultado != null -> OdinPaleta.Incorrecto
                actual -> Color(0xFFFFF6E0)
                else -> Color(0xFF5E564A)
            }
            Box(
                Modifier
                    .padding(horizontal = 1.dp)
                    .size(if (estado.total > 10) 22.dp else 26.dp)
                    .then(if (actual) Modifier.border(1.5.dp, estilo.acento, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(RUNAS_BATALLA[i % RUNAS_BATALLA.size], color = color, fontSize = if (estado.total > 10) 14.sp else 16.sp)
            }
        }
    }
}

/** Aciertos a la izquierda, errores a la derecha y la línea del puntaje mínimo. */
@Composable
private fun BarraBatalla(estado: EstadoBatalla, estilo: EstiloMundo, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("Aciertos ${estado.aciertos}", color = estilo.acento, fontFamily = FuenteTexto, fontSize = 12.5.sp)
        Spacer(Modifier.width(8.dp))
        Canvas(
            Modifier
                .weight(1f)
                .height(22.dp)
                .semantics { contentDescription = "Aciertos ${estado.aciertos}, errores ${estado.errores}, mínimo ${ReglasReto.PUNTAJE_MINIMO} por ciento" }
        ) {
            val h = 8.dp.toPx()
            val y = 4.dp.toPx()
            val ancho = size.width
            val seg = ancho / estado.total.coerceAtLeast(1)
            drawRect(Color(0xFF1C1814), Offset(0f, y), androidx.compose.ui.geometry.Size(ancho, h))
            drawRect(estilo.acento, Offset(0f, y), androidx.compose.ui.geometry.Size(seg * estado.aciertos, h))
            drawRect(
                OdinPaleta.Incorrecto,
                Offset(ancho - seg * estado.errores, y),
                androidx.compose.ui.geometry.Size(seg * estado.errores, h)
            )
            val xMin = ancho * ReglasReto.PUNTAJE_MINIMO / 100f
            drawLine(Color(0xFFF3E2BC), Offset(xMin, 0f), Offset(xMin, y * 2 + h), 1.5.dp.toPx())
        }
        Spacer(Modifier.width(8.dp))
        Text("Errores ${estado.errores}", color = OdinPaleta.Incorrecto, fontFamily = FuenteTexto, fontSize = 12.5.sp)
    }
}

@Composable
private fun OpcionBatalla(
    texto: String,
    multiple: Boolean,
    estado: EstadoOpcionBatalla,
    bloqueada: Boolean,
    estilo: EstiloMundo,
    onClick: () -> Unit
) {
    val color = when (estado) {
        EstadoOpcionBatalla.CORRECTA -> OdinPaleta.Correcto
        EstadoOpcionBatalla.INCORRECTA -> OdinPaleta.Incorrecto
        EstadoOpcionBatalla.FALTANTE -> OdinPaleta.Incompleto
        EstadoOpcionBatalla.SELECCIONADA -> estilo.acento
        EstadoOpcionBatalla.NEUTRA -> estilo.metal
    }
    val resaltada = estado != EstadoOpcionBatalla.NEUTRA
    val fondo = when (estado) {
        EstadoOpcionBatalla.CORRECTA -> Color(0x383CA050)
        EstadoOpcionBatalla.INCORRECTA -> Color(0x38B4321E)
        EstadoOpcionBatalla.FALTANTE -> Color(0x30E0A33A)
        EstadoOpcionBatalla.SELECCIONADA -> estilo.acento.copy(alpha = .16f)
        EstadoOpcionBatalla.NEUTRA -> Color(0x9E060504)
    }
    val atenuada = bloqueada && estado == EstadoOpcionBatalla.NEUTRA
    val forma = CutCornerShape(8.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clip(forma)
            .background(fondo)
            .border(if (resaltada) 2.dp else 1.dp, color.copy(alpha = if (resaltada) 1f else .5f), forma)
            .semantics {
                contentDescription = "Opción: $texto"
                selected = estado == EstadoOpcionBatalla.SELECCIONADA || estado == EstadoOpcionBatalla.CORRECTA || estado == EstadoOpcionBatalla.INCORRECTA
            }
            .clickable(enabled = !bloqueada, role = if (multiple) Role.Checkbox else Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            // Cuadro (varias respuestas) o rombo (una sola)
            Canvas(Modifier.size(24.dp)) {
                val s = size.minDimension
                val forma = if (multiple) {
                    Path().apply { moveTo(s * .1f, s * .1f); lineTo(s * .9f, s * .1f); lineTo(s * .9f, s * .9f); lineTo(s * .1f, s * .9f); close() }
                } else {
                    Path().apply { moveTo(s / 2, 0f); lineTo(s, s / 2); lineTo(s / 2, s); lineTo(0f, s / 2); close() }
                }
                val marcada = estado == EstadoOpcionBatalla.SELECCIONADA || estado == EstadoOpcionBatalla.CORRECTA || estado == EstadoOpcionBatalla.INCORRECTA
                if (marcada) drawPath(forma, color)
                drawPath(forma, color, style = Stroke(1.6.dp.toPx()))
                if (marcada) {
                    val palomita = Path().apply { moveTo(s * .27f, s * .52f); lineTo(s * .43f, s * .67f); lineTo(s * .74f, s * .34f) }
                    drawPath(palomita, Color(0xFF120E0B), style = Stroke(2.2.dp.toPx()))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    texto,
                    color = if (atenuada) Color(0xFF8F877A) else Color(0xFFE4DCCB),
                    fontFamily = FuenteTexto,
                    fontSize = 15.5.sp,
                    lineHeight = 20.sp
                )
                if (estado == EstadoOpcionBatalla.FALTANTE) {
                    Text(
                        "FALTÓ",
                        color = OdinPaleta.Incompleto,
                        fontFamily = FuenteTitulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
            when (estado) {
                EstadoOpcionBatalla.INCORRECTA -> MarcaCirculo(Icons.Filled.Close, OdinPaleta.Incorrecto)
                EstadoOpcionBatalla.CORRECTA -> MarcaCirculo(Icons.Filled.Check, OdinPaleta.Correcto)
                else -> Unit
            }
        }
    }
}

@Composable
private fun MarcaCirculo(icono: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Box(Modifier.size(22.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Icon(icono, contentDescription = null, tint = Color(0xFF120E0B), modifier = Modifier.size(15.dp))
    }
}

/** Retroalimentación inmediata: correcta, incompleta o incorrecta (paso 7 y A7.1). */
@Composable
private fun Retroalimentacion(estado: EstadoRespuesta, opciones: List<String>, correctas: List<Int>, elegidas: Set<Int>) {
    val (titulo, color) = when (estado) {
        EstadoRespuesta.CORRECTA -> "RESPUESTA CORRECTA" to OdinPaleta.Correcto
        EstadoRespuesta.INCOMPLETA -> "RESPUESTA INCOMPLETA" to OdinPaleta.Incompleto
        EstadoRespuesta.INCORRECTA -> "RESPUESTA INCORRECTA" to OdinPaleta.Incorrecto
    }
    val detalle = when (estado) {
        EstadoRespuesta.CORRECTA -> "Bien peleado: sigue adelante."
        EstadoRespuesta.INCOMPLETA -> {
            val faltan = correctas.filter { it !in elegidas }.joinToString(" · ") { "«${opciones[it]}»" }
            "Elegiste ${elegidas.size} de las ${correctas.size} correctas. Faltó: $faltan. Este tema se anotará para repasar."
        }
        EstadoRespuesta.INCORRECTA -> {
            val correctasTexto = correctas.joinToString(" · ") { "«${opciones[it]}»" }
            (if (correctas.size > 1) "Las respuestas correctas son: " else "La respuesta correcta es: ") +
                "$correctasTexto. Este tema se anotará para repasar."
        }
    }
    val forma = CutCornerShape(6.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(color.copy(alpha = .14f))
            .border(1.dp, color, forma)
            .padding(12.dp)
    ) {
        Text(titulo, color = color, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.5.sp)
        Text(detalle, color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 14.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

// ---------------------------------------------------------------------------
// Resultados (pasos 9 a 11, A10.1 y A10.2)
// ---------------------------------------------------------------------------

@Composable
private fun PantallaSuperado(
    estilo: EstiloMundo,
    nombreMundo: String,
    resultado: ResultadoRetoFinMundo,
    onVolver: () -> Unit,
    onIrAlMundo: (String) -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(Modifier.size(150.dp)) {
            val r = size.minDimension * .3f
            mancha(center, r * 2.4f, Color(0xFFFFD36B), .45f)
            drawCircle(Color(0xFFFFE9A8), r, center)
            drawCircle(estilo.metal, r + 4.dp.toPx(), center, style = Stroke(3.dp.toPx()))
            dibujarEmblema(estilo.emblema, center, r * .6f, Color(0xFF4A3010))
        }
        Text(
            "RAGNARÖK SUPERADO",
            color = estilo.acento,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 3.sp
        )
        Text("EL MUNDO RENACE", style = estiloTitulo(30.sp, Color(0xFFF3E2BC), brillo = estilo.acento))
        Puntaje(resultado.puntaje, estilo)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Ficha(Modifier.weight(1f), "Aciertos", "${resultado.aciertos}", estilo.acento)
            Ficha(Modifier.weight(1f), "Errores", "${resultado.errores}", OdinPaleta.Incorrecto)
            Ficha(Modifier.weight(1f), "Mínimo", "${ReglasReto.PUNTAJE_MINIMO}", estilo.metalClaro)
        }
        Spacer(Modifier.height(14.dp))
        PanelTallado(estilo = estilo, semilla = 43, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("$nombreMundo completado", color = Color(0xFFF3E2BC), fontFamily = FuenteTitulo, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                if (resultado.mundoDesbloqueadoNombre != null) {
                    Text(
                        "Se abre el siguiente mundo: ${resultado.mundoDesbloqueadoNombre}",
                        color = estilo.acento,
                        fontFamily = FuenteTexto,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (resultado.ultimoMundo) {
                    Text(
                        "¡Completaste todos los mundos disponibles!",
                        color = estilo.acento,
                        fontFamily = FuenteTexto,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        val siguienteId = resultado.mundoDesbloqueadoId
        if (siguienteId != null) {
            BotonOdin(
                texto = "Ir a ${resultado.mundoDesbloqueadoNombre.orEmpty()}",
                onClick = { onIrAlMundo(siguienteId) },
                acento = estilo.acento,
                iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onVolver) { Text("Volver al mapa", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo) }
        } else {
            BotonOdin(texto = "Volver al mapa", onClick = onVolver, acento = estilo.acento, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun PantallaNoSuperado(
    estilo: EstiloMundo,
    resultado: ResultadoRetoFinMundo,
    onReintentar: () -> Unit,
    onRepasar: (String) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Eclipse(estilo, Modifier.fillMaxWidth().height(140.dp))
        Text(
            "RAGNARÖK NO SUPERADO",
            color = OdinPaleta.Incorrecto,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 3.sp
        )
        Text("EL CREPÚSCULO TE ALCANZÓ", style = estiloTitulo(24.sp, Color(0xFFF3E2BC), brillo = OdinPaleta.Incorrecto), textAlign = TextAlign.Center)
        Puntaje(resultado.puntaje, estilo, detalle = "de 100 · necesitas ${ReglasReto.PUNTAJE_MINIMO}")
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Ficha(Modifier.weight(1f), "Aciertos", "${resultado.aciertos}", estilo.acento)
            Ficha(Modifier.weight(1f), "Errores", "${resultado.errores}", OdinPaleta.Incorrecto)
        }
        if (resultado.temasConErrores.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "TEMAS POR REFORZAR · ${resultado.temasConErrores.size}",
                color = estilo.acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            resultado.temasConErrores.forEach { tema ->
                val forma = CutCornerShape(8.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(forma)
                        .background(Color(0xB3120F0C))
                        .border(1.dp, estilo.metal.copy(alpha = .7f), forma)
                        .clickable(role = Role.Button) { onRepasar(tema.temaId) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tema.nombre, color = Color(0xFFF3E2BC), fontFamily = FuenteTitulo, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp)
                        Text(
                            if (tema.errores == 1) "1 error en el reto" else "${tema.errores} errores en el reto",
                            color = OdinPaleta.HuesoTenue,
                            fontFamily = FuenteTexto,
                            fontSize = 13.sp
                        )
                    }
                    Text("Repasar lección ›", color = estilo.acento, fontFamily = FuenteTitulo, fontSize = 13.5.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        BotonOdin(
            texto = "Reintentar el Ragnarök · ${PoliticaCosto.C * PoliticaCosto.MULT_RAGNAROK}",
            onClick = onReintentar,
            acento = estilo.acento,
            iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Cada intento cuesta la entrada del Ragnarök, con un nuevo conjunto de preguntas.",
            color = OdinPaleta.HuesoTenue,
            fontFamily = FuenteTexto,
            fontSize = 12.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
        TextButton(onClick = onVolver) { Text("Volver al mapa", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo) }
    }
}

@Composable
private fun Puntaje(puntaje: Int, estilo: EstiloMundo, detalle: String = "de 100 puntos · mínimo ${ReglasReto.PUNTAJE_MINIMO}") {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
        Text("$puntaje", color = Color(0xFFF3E2BC), fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 64.sp)
        Text(detalle, color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 14.sp)
    }
}

@Composable
private fun Ficha(modifier: Modifier, etiqueta: String, valor: String, color: Color) {
    val forma = CutCornerShape(8.dp)
    Column(
        modifier.clip(forma).background(Color(0xB3120F0C)).border(1.dp, color.copy(alpha = .6f), forma).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(etiqueta, color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 12.sp)
        Text(valor, color = color, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}

// ---------------------------------------------------------------------------
// Diálogos y mensajes
// ---------------------------------------------------------------------------

/** A6.1: confirmación de huida. */
@Composable
private fun DialogoHuida(estilo: EstiloMundo, antesDeEmpezar: Boolean, onSeguir: () -> Unit, onAbandonar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onSeguir,
        containerColor = OdinPaleta.HollinAlto,
        titleContentColor = OdinPaleta.Hueso,
        textContentColor = OdinPaleta.Hueso,
        title = { Text("¿Huir del Ragnarök?", fontFamily = FuenteTitulo) },
        text = {
            Text(
                (if (antesDeEmpezar) "Si te retiras ahora" else "Si abandonas ahora") +
                    ", el reto terminará con puntaje 0 y tendrás que enfrentarlo de nuevo desde el inicio. " +
                    "La entrada de hidromiel que pagaste se pierde.",
                fontFamily = FuenteTexto
            )
        },
        confirmButton = {
            TextButton(onClick = onSeguir) { Text("Seguir luchando", color = estilo.acento, fontFamily = FuenteTitulo) }
        },
        dismissButton = {
            TextButton(onClick = onAbandonar) { Text("Abandonar", color = OdinPaleta.Incorrecto, fontFamily = FuenteTitulo) }
        }
    )
}

@Composable
private fun BoxScope.PanelMensaje(
    estilo: EstiloMundo,
    titulo: String,
    texto: String,
    accion: String,
    onAccion: () -> Unit
) {
    Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PanelTallado(estilo = estilo, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(titulo, style = estiloTitulo(22.sp), textAlign = TextAlign.Center)
                Text(
                    texto,
                    color = Color(0xFFD8CFBE),
                    fontFamily = FuenteTexto,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                BotonOdin(texto = accion, onClick = onAccion, acento = estilo.acento, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
