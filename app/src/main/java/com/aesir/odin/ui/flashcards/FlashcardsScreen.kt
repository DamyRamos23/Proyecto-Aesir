package com.aesir.odin.ui.flashcards

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.EstadoTarjeta
import com.aesir.odin.domain.model.RespuestaFlashcard
import com.aesir.odin.domain.model.TarjetaFlashcard
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.DivisorOrnamental
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.IconoCuerno
import com.aesir.odin.ui.components.odin.PanelTallado
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta

/** Lugar de los nueve mundos donde ocurre cada salón de flashcards. */
private fun lugarDe(orden: Int): String = when (((orden - 1).coerceAtLeast(0)) % 4) {
    0 -> "SALÓN DE HIDROMIEL · MIDGARD"
    1 -> "EL RISCO · JOTUNHEIM"
    2 -> "LAS RAÍCES · YGGDRASIL"
    else -> "LA FORJA · MUSPELHEIM"
}

/**
 * CU-03 Repasar conceptos con flashcards.
 *
 * Cada carta muestra la descripción de un concepto y el alumno elige, entre
 * cuatro opciones, el concepto al que corresponde. Salir antes de vaciar la
 * pila (A4.1) pide confirmación y cuenta como abandonar el nivel (MOD-01).
 */
@Composable
fun FlashcardsScreen(
    viewModel: FlashcardsViewModel,
    temaId: String,
    temasIncluidos: List<String>,
    onVolverRoadmap: () -> Unit,
    onRepasarLeccion: (String) -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsState()
    val resumen by viewModel.resumen.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
    val sugerencia by viewModel.sugerenciaRepaso.collectAsState()
    val avisoRepaso by viewModel.avisoRepaso.collectAsState()
    val saldo by viewModel.saldo.collectAsState()
    var confirmarSalida by remember { mutableStateOf(false) }

    LaunchedEffect(temaId) { viewModel.iniciar(temaId, temasIncluidos) }

    // A4.1: salir antes de terminar pide confirmación.
    BackHandler(enabled = resumen == null && mensajeError == null) { confirmarSalida = true }

    val orden = contexto?.mundo?.orden ?: 1
    val estilo = EstilosMundo.porOrden(orden)

    FondoMundo(estilo = estilo, modifier = modifier, alturaPaisaje = 300.dp, paisajeInferior = true) {
        val resumenActual = resumen
        val estadoActual = estado
        when {
            mensajeError != null -> PanelMensaje(
                estilo = estilo,
                titulo = "Sin tarjetas",
                texto = mensajeError.orEmpty(),
                accion = "Volver al roadmap",
                onAccion = onVolverRoadmap
            )
            resumenActual != null -> PantallaCompletada(estilo, resumenActual, onVolverRoadmap)
            estadoActual != null -> Juego(
                estilo = estilo,
                orden = orden,
                nombreMundo = contexto?.mundo?.nombre.orEmpty(),
                saldo = saldo,
                estado = estadoActual,
                onResponder = viewModel::responder,
                onContinuar = viewModel::continuar,
                onSalir = { confirmarSalida = true }
            )
            else -> CircularProgressIndicator(color = OdinPaleta.Brasa, modifier = Modifier.align(Alignment.Center))
        }

        if (sugerencia != null) {
            DialogoSugerencia(
                estilo = estilo,
                concepto = sugerencia?.concepto.orEmpty(),
                onRepasar = { viewModel.repasarLeccion(onRepasarLeccion) },
                onSeguir = viewModel::seguirConTarjetas
            )
        } else if (avisoRepaso != null) {
            AlertDialog(
                onDismissRequest = viewModel::limpiarAvisoRepaso,
                containerColor = OdinPaleta.HollinAlto,
                titleContentColor = OdinPaleta.Hueso,
                textContentColor = OdinPaleta.Hueso,
                title = { Text("No se pudo abrir la lección", fontFamily = FuenteTitulo) },
                text = { Text(avisoRepaso.orEmpty(), fontFamily = FuenteTexto) },
                confirmButton = {
                    TextButton(onClick = viewModel::limpiarAvisoRepaso) {
                        Text("Seguir con las cartas", color = estilo.acento, fontFamily = FuenteTitulo)
                    }
                }
            )
        } else if (confirmarSalida) {
            AlertDialog(
                onDismissRequest = { confirmarSalida = false },
                containerColor = OdinPaleta.HollinAlto,
                titleContentColor = OdinPaleta.Hueso,
                textContentColor = OdinPaleta.Hueso,
                title = { Text("¿Abandonar las flashcards?", fontFamily = FuenteTitulo) },
                text = {
                    Text(
                        "Si sales ahora, la actividad termina sin puntaje y no se registra como completada. " +
                            "Si pagaste entrada de hidromiel, se pierde.",
                        fontFamily = FuenteTexto
                    )
                },
                confirmButton = {
                    TextButton(onClick = { confirmarSalida = false }) {
                        Text("Seguir jugando", color = estilo.acento, fontFamily = FuenteTitulo)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        confirmarSalida = false
                        onSalir()
                    }) { Text("Abandonar", color = OdinPaleta.Incorrecto, fontFamily = FuenteTitulo) }
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Juego
// ---------------------------------------------------------------------------

private enum class EstadoOpcionFlash { NEUTRA, CORRECTA, INCORRECTA, ATENUADA }

@Composable
private fun Juego(
    estilo: EstiloMundo,
    orden: Int,
    nombreMundo: String,
    saldo: Int,
    estado: EstadoFlashcards,
    onResponder: (Int) -> Unit,
    onContinuar: () -> Unit,
    onSalir: () -> Unit
) {
    val tarjeta = estado.tarjeta ?: return
    val respuesta = estado.respuesta

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonVolver(nombreMundo, onClick = onSalir)
            Spacer(Modifier.weight(1f))
            ContadorHidromiel(saldo = saldo, metal = estilo.metal)
        }

        Text(
            lugarDe(orden),
            color = estilo.acento,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 2.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
        )
        PuntosCartas(
            puntos = estado.puntos,
            actual = estado.posicionActual,
            estilo = estilo,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Text(
            "Carta ${estado.numero} de ${estado.total}" +
                if (respuesta != null && !respuesta.correcta) " · saldrá de nuevo" else "",
            color = Color(0xFFD8CFBE),
            fontFamily = FuenteTexto,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PilaLateral("Mazo", estado.enMazo, estilo)
                AnimatedContent(
                    targetState = tarjeta,
                    transitionSpec = {
                        ContentTransform(
                            targetContentEnter = slideInHorizontally(tween(260)) { it / 2 } + fadeIn(tween(260)),
                            initialContentExit = slideOutHorizontally(tween(200)) { -it / 2 } + fadeOut(tween(160))
                        )
                    },
                    contentKey = { it.id },
                    label = "carta",
                    modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                ) { t ->
                    CartaDescripcion(estilo, t, if (t.id == tarjeta.id) respuesta else null)
                }
                PilaLateral("Dominadas", estado.dominadas, estilo)
            }

            tarjeta.opciones.forEachIndexed { i, concepto ->
                OpcionConcepto(
                    runa = estilo.runas[i % estilo.runas.size],
                    texto = concepto,
                    estado = estadoOpcion(i, respuesta),
                    habilitada = respuesta == null,
                    estilo = estilo,
                    onClick = { onResponder(i) }
                )
            }
        }

        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            BannerEstado(respuesta, estilo)
            AnimatedVisibility(visible = respuesta != null) {
                BotonOdin(
                    texto = if (respuesta?.correcta == true) "Siguiente carta" else "Continuar",
                    onClick = onContinuar,
                    acento = if (respuesta?.correcta == true) OdinPaleta.Correcto else estilo.acento,
                    iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                )
            }
        }
    }
}

private fun estadoOpcion(indice: Int, respuesta: RespuestaFlashcard?): EstadoOpcionFlash = when {
    respuesta == null -> EstadoOpcionFlash.NEUTRA
    indice == respuesta.indiceCorrecto -> EstadoOpcionFlash.CORRECTA
    indice == respuesta.seleccionada -> EstadoOpcionFlash.INCORRECTA
    else -> EstadoOpcionFlash.ATENUADA
}

/** Fila de puntos: verde = dominada, roja = fallada, anillo = carta en juego. */
@Composable
private fun PuntosCartas(puntos: List<EstadoTarjeta>, actual: Int, estilo: EstiloMundo, modifier: Modifier = Modifier) {
    Canvas(modifier.height(16.dp)) {
        val n = puntos.size
        if (n == 0) return@Canvas
        val paso = minOf(20.dp.toPx(), (size.width - 16.dp.toPx()) / (n - 1).coerceAtLeast(1))
        val ancho = paso * (n - 1)
        val x0 = (size.width - ancho) / 2f
        val cy = size.height / 2f
        puntos.forEachIndexed { i, p ->
            val c = Offset(x0 + paso * i, cy)
            val relleno = when (p) {
                EstadoTarjeta.DOMINADA -> OdinPaleta.Correcto
                EstadoTarjeta.FALLADA -> OdinPaleta.Incorrecto
                EstadoTarjeta.PENDIENTE -> Color(0xFF1B1611)
            }
            drawCircle(relleno, 4.5.dp.toPx(), c)
            drawCircle(
                if (p == EstadoTarjeta.PENDIENTE) estilo.metal.copy(alpha = .6f) else Color.Black.copy(alpha = .4f),
                4.5.dp.toPx(), c, style = Stroke(1.dp.toPx())
            )
            if (i == actual) {
                drawCircle(estilo.acento, 7.5.dp.toPx(), c, style = Stroke(2.dp.toPx()))
                drawCircle(estilo.acento, 2.dp.toPx(), c)
            }
        }
    }
}

/** Montón de cartas a un lado de la carta en juego, con su contador. */
@Composable
private fun PilaLateral(etiqueta: String, cantidad: Int, estilo: EstiloMundo) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
        Canvas(Modifier.size(width = 50.dp, height = 64.dp)) {
            val capas = cantidad.coerceIn(0, 3)
            for (k in 0 until capas.coerceAtLeast(1)) {
                val dx = (k * 3).dp.toPx()
                val dy = -(k * 3).dp.toPx()
                val tl = Offset(dx, size.height * .1f + dy + 6.dp.toPx())
                val tam = Size(size.width - 8.dp.toPx(), size.height * .8f)
                drawRoundRect(
                    Brush.verticalGradient(listOf(estilo.caraClara, estilo.caraOscura), startY = tl.y, endY = tl.y + tam.height),
                    tl, tam, androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 8.dp.toPx()),
                    alpha = if (cantidad == 0) .3f else 1f
                )
                drawRoundRect(
                    estilo.metal.copy(alpha = if (cantidad == 0) .3f else .9f), tl, tam,
                    androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 8.dp.toPx()), style = Stroke(1.2.dp.toPx())
                )
            }
        }
        Column(
            Modifier
                .padding(top = 4.dp)
                .clip(CutCornerShape(4.dp))
                .background(Color(0xE60B0907))
                .padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(etiqueta, color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 10.sp, maxLines = 1, softWrap = false)
            Text("$cantidad", color = estilo.acento, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

/**
 * Carta en juego: muestra la DESCRIPCIÓN del concepto. Cuando el alumno
 * responde, la ranura inferior revela el concepto correcto.
 */
@Composable
private fun CartaDescripcion(estilo: EstiloMundo, tarjeta: TarjetaFlashcard, respuesta: RespuestaFlashcard?) {
    val inclinacion by animateFloatAsState(
        targetValue = if (respuesta != null && !respuesta.correcta) -4f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "inclinacion"
    )
    val forma = RoundedCornerShape(topStart = 70.dp, topEnd = 70.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 250.dp)
            .rotate(inclinacion)
            .clip(forma)
            .background(Brush.verticalGradient(listOf(estilo.metalClaro, estilo.metal)))
            .border(3.dp, estilo.metalOscuro, forma)
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .semantics { contentDescription = "Carta: ${tarjeta.descripcion}" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(Modifier.size(34.dp)) { dibujarEmblema(estilo.emblema, center, size.minDimension * .42f, estilo.metalOscuro) }
        Text(
            "DESCRIPCIÓN",
            color = estilo.metalOscuro,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Box(Modifier.weight(1f, fill = false).heightIn(min = 90.dp), contentAlignment = Alignment.Center) {
            Text(
                tarjeta.descripcion,
                color = OdinPaleta.TintaOscura,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            )
        }
        DivisorOrnamental(estilo.metalOscuro, estilo.metalOscuro, Modifier.padding(vertical = 6.dp))
        // Ranura del concepto: runas mientras no se responde; luego, la respuesta correcta
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .clip(CutCornerShape(6.dp))
                .background(OdinPaleta.TintaOscura.copy(alpha = .35f))
                .border(1.dp, estilo.metalOscuro, CutCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (respuesta == null) {
                Text(
                    "¿Qué concepto es?",
                    color = OdinPaleta.Tinta,
                    fontFamily = FuenteTexto,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    tarjeta.concepto,
                    color = if (respuesta.correcta) Color(0xFF0F3D1A) else OdinPaleta.TintaRoja,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OpcionConcepto(
    runa: String,
    texto: String,
    estado: EstadoOpcionFlash,
    habilitada: Boolean,
    estilo: EstiloMundo,
    onClick: () -> Unit
) {
    val borde = when (estado) {
        EstadoOpcionFlash.CORRECTA -> OdinPaleta.Correcto
        EstadoOpcionFlash.INCORRECTA -> OdinPaleta.Incorrecto
        else -> estilo.metal
    }
    val fondo = when (estado) {
        EstadoOpcionFlash.CORRECTA -> Color(0xFF1F4A2A)
        EstadoOpcionFlash.INCORRECTA -> Color(0xFF5A2418)
        EstadoOpcionFlash.ATENUADA -> Color(0x66120F0C)
        EstadoOpcionFlash.NEUTRA -> Color(0xCC120F0C)
    }
    val forma = CutCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(forma)
            .background(fondo)
            .border(if (estado == EstadoOpcionFlash.NEUTRA || estado == EstadoOpcionFlash.ATENUADA) 1.dp else 2.dp, borde, forma)
            .semantics { contentDescription = "Concepto: $texto" }
            .clickable(enabled = habilitada, role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(Color.Black.copy(alpha = .45f)).border(1.dp, borde, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(runa, color = if (estado == EstadoOpcionFlash.ATENUADA) Color(0xFF8F877A) else estilo.acento, fontSize = 17.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            texto,
            color = if (estado == EstadoOpcionFlash.ATENUADA) Color(0xFF8F877A) else Color(0xFFEFE6D2),
            fontFamily = FuenteTexto,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            modifier = Modifier.weight(1f)
        )
        when (estado) {
            EstadoOpcionFlash.CORRECTA -> Marca(Icons.Filled.Check, OdinPaleta.Correcto)
            EstadoOpcionFlash.INCORRECTA -> Marca(Icons.Filled.Close, OdinPaleta.Incorrecto)
            else -> Unit
        }
    }
}

@Composable
private fun Marca(icono: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Box(Modifier.size(24.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Icon(icono, contentDescription = null, tint = Color(0xFF120E0B), modifier = Modifier.size(16.dp))
    }
}

/** Mensaje bajo las opciones: indicación, o retroalimentación de la respuesta. */
@Composable
private fun BannerEstado(respuesta: RespuestaFlashcard?, estilo: EstiloMundo) {
    val (texto, color) = when {
        respuesta == null -> "Elige el concepto que corresponde a la descripción" to estilo.metal
        respuesta.correcta -> "¡Correcto! La carta pasa a tu pila de dominadas" to OdinPaleta.Correcto
        else -> "Incorrecto · La carta vuelve al fondo del mazo" to OdinPaleta.Incorrecto
    }
    Text(
        texto,
        color = Color(0xFFF3EBDA),
        fontFamily = FuenteTexto,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xF2080605))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    )
}

// ---------------------------------------------------------------------------
// Nivel completado (paso 9)
// ---------------------------------------------------------------------------

@Composable
private fun PantallaCompletada(estilo: EstiloMundo, resumen: ResumenFlashcards, onVolver: () -> Unit) {
    val r = resumen.resultado
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconoCuerno(tamano = 56.dp, metal = estilo.metal)
        Spacer(Modifier.height(10.dp))
        Text(
            "NIVEL COMPLETADO",
            color = estilo.acento,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 3.sp
        )
        Text(
            "¡SKÅL! DOMINASTE LAS ${r.totalTarjetas} CARTAS",
            style = estiloTitulo(24.sp, Color(0xFFF3E2BC), brillo = estilo.acento),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(20.dp))
        PanelTallado(estilo = estilo, semilla = 31, modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Dato(estilo, "${r.totalTarjetas}", "Cartas")
                    Dato(estilo, "${r.errores}", if (r.errores == 1) "Fallo" else "Fallos")
                    Dato(estilo, "${resumen.precision}%", "Precisión")
                }
                DivisorOrnamental(estilo.metal, estilo.acento, Modifier.padding(vertical = 12.dp))
                Text(
                    "Puntaje: ${r.puntaje} de 100",
                    color = Color(0xFFF3E2BC),
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "${r.aciertosPrimerIntento} de ${r.totalTarjetas} cartas al primer intento",
                    color = Color(0xFFD8CFBE),
                    fontFamily = FuenteTexto,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (resumen.masDificiles.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            PanelTallado(estilo = estilo, semilla = 32, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "LAS QUE MÁS TE COSTARON",
                        color = estilo.acento,
                        fontFamily = FuenteTitulo,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    resumen.masDificiles.forEach { (concepto, fallos) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(concepto, color = Color(0xFFEFE6D2), fontFamily = FuenteTitulo, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text(
                                if (fallos == 1) "1 fallo" else "$fallos fallos",
                                color = OdinPaleta.HuesoTenue,
                                fontFamily = FuenteTexto,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            if (r.errores == 0) "Nivel completado: todas las cartas fueron contestadas bien."
            else "Nivel completado: repetiste las cartas que fallaste hasta dominarlas.",
            color = Color(0xFFD8CFBE),
            fontFamily = FuenteTexto,
            fontStyle = FontStyle.Italic,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        BotonOdin(texto = "Volver al mundo", onClick = onVolver, acento = estilo.acento, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Dato(estilo: EstiloMundo, valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = estilo.acento, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Text(etiqueta, color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 14.sp)
    }
}

// ---------------------------------------------------------------------------
// Diálogos
// ---------------------------------------------------------------------------

/** A5.2: la misma tarjeta se falló tres veces. */
@Composable
private fun DialogoSugerencia(estilo: EstiloMundo, concepto: String, onRepasar: () -> Unit, onSeguir: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = OdinPaleta.HollinAlto,
        titleContentColor = OdinPaleta.Hueso,
        textContentColor = OdinPaleta.Hueso,
        title = { Text("Esta carta se te resiste", fontFamily = FuenteTitulo) },
        text = {
            Text(
                "Has fallado «$concepto» tres veces. Te sugerimos repasar la lección interactiva del tema.\n\n" +
                    "Si aceptas, las flashcards terminan sin puntaje y se abre la lección.",
                fontFamily = FuenteTexto
            )
        },
        confirmButton = {
            TextButton(onClick = onRepasar) { Text("Repasar lección", color = estilo.acento, fontFamily = FuenteTitulo) }
        },
        dismissButton = {
            TextButton(onClick = onSeguir) { Text("Seguir con las cartas", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTitulo) }
        }
    )
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.PanelMensaje(
    estilo: EstiloMundo,
    titulo: String,
    texto: String,
    accion: String,
    onAccion: () -> Unit
) {
    Column(
        Modifier.align(Alignment.Center).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
