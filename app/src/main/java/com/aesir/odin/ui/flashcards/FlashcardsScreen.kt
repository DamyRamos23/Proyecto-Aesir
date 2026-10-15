package com.aesir.odin.ui.flashcards

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.R
import com.aesir.odin.domain.model.EstadoTarjeta
import com.aesir.odin.domain.model.RespuestaFlashcard
import com.aesir.odin.domain.model.TarjetaFlashcard
import com.aesir.odin.ui.components.odin.FuentesMockup
import com.aesir.odin.ui.components.odin.LienzoDiseno
import com.aesir.odin.ui.components.odin.en
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Alto de la barra de navegación del mockup (57 px del mockup). */
private const val ALTO_BARRA = 41.17f

/** Medidas del área de juego (390 × 774, bajo la barra). */
private object Area {
    const val ANCHO = 390f
    const val ALTO = 774f
    const val CARTA_ANCHO = 221f
    const val CARTA_ALTO = 302f
    val centroCarta = Offset(195f, 241.15f)
    /** Centro de la carta superior de cada montón. */
    val cartaMazo = Offset(46.3f, 277f)
    val cartaDominadas = Offset(352.5f, 283.5f)
    const val ESCALA_MONTON = 70f / 221f
    val yOpciones = listOf(425.1f, 486.5f, 548.2f, 609.5f)
}

/**
 * CU-03 Repasar conceptos con flashcards, con el diseño del mockup
 * "Flashcards de taberna" (uno por mundo).
 *
 * La carta muestra la DESCRIPCIÓN y el alumno elige el concepto entre cuatro
 * tablillas. Si acierta, la carta se voltea y revela el concepto; si falla,
 * solo se marca su opción (la correcta no se revela, porque la carta volverá
 * a salir) y la carta sale volando al mazo. Salir antes de vaciar la pila
 * (A4.1) pide confirmación y cuenta como abandonar el nivel (MOD-01).
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
    val nombresTemas by viewModel.nombresTemas.collectAsState()
    var confirmarSalida by remember { mutableStateOf(false) }

    LaunchedEffect(temaId) { viewModel.iniciar(temaId, temasIncluidos) }

    // A4.1: salir antes de terminar pide confirmación.
    BackHandler(enabled = resumen == null && mensajeError == null) { confirmarSalida = true }
    val salir: () -> Unit = {
        if (resumen != null || mensajeError != null) {
            onVolverRoadmap()
        } else {
            confirmarSalida = true
        }
    }

    val orden = contexto?.mundo?.orden ?: 1
    val salon = SalonMundo.porOrden(orden)

    Box(modifier.fillMaxSize()) {
        MesaSalon(salon)
        LienzoDiseno(alto = ALTO_BARRA + Area.ALTO) {
            BarraSuperior(
                izquierda = contexto?.mundo?.nombre ?: "Roadmap",
                derecha = contexto?.tema?.nombre.orEmpty(),
                onVolver = salir
            )
            Box(Modifier.offset(y = ALTO_BARRA.dp).requiredSize(Area.ANCHO.dp, Area.ALTO.dp)) {
                AdornosSalon(salon)
                val resumenActual = resumen
                val estadoActual = estado
                val tarjeta = estadoActual?.tarjeta
                when {
                    mensajeError != null -> PanelMensaje(salon, "Sin tarjetas", mensajeError.orEmpty(), "Volver al roadmap", onVolverRoadmap)
                    resumenActual != null -> PantallaCompletada(salon, resumenActual, estadoActual?.puntos.orEmpty(), onVolverRoadmap)
                    estadoActual != null && tarjeta != null -> Juego(
                        salon = salon,
                        estado = estadoActual,
                        tarjeta = tarjeta,
                        nombreTema = nombresTemas[tarjeta.temaId],
                        onResponder = viewModel::responder,
                        onContinuar = viewModel::continuar
                    )
                    else -> CircularProgressIndicator(color = salon.titulo, modifier = Modifier.en(195f, 300f, 40f, 40f))
                }
            }
        }

        if (sugerencia != null) {
            // A5.2: la misma tarjeta se falló tres veces (sin decir cuál es el concepto).
            AlertDialog(
                onDismissRequest = viewModel::seguirConTarjetas,
                containerColor = Color(0xFF1E1A15),
                titleContentColor = ColoresSalon.Crema,
                textContentColor = ColoresSalon.Crema,
                title = { Text("Esta carta se te resiste", fontFamily = FuentesMockup.alegreya(), fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Has fallado esta carta tres veces. Te sugerimos repasar la lección interactiva del tema.\n\n" +
                            "Si aceptas, las flashcards terminan sin puntaje y se abre la lección.",
                        fontFamily = FuentesMockup.alegreya()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.repasarLeccion(onRepasarLeccion) }) {
                        Text("Repasar lección", color = salon.titulo)
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::seguirConTarjetas) { Text("Seguir con las cartas", color = ColoresSalon.Crema) }
                }
            )
        } else if (avisoRepaso != null) {
            AlertDialog(
                onDismissRequest = viewModel::limpiarAvisoRepaso,
                containerColor = Color(0xFF1E1A15),
                titleContentColor = ColoresSalon.Crema,
                textContentColor = ColoresSalon.Crema,
                title = { Text("No se pudo abrir la lección", fontFamily = FuentesMockup.alegreya(), fontWeight = FontWeight.Bold) },
                text = { Text(avisoRepaso.orEmpty(), fontFamily = FuentesMockup.alegreya()) },
                confirmButton = {
                    TextButton(onClick = viewModel::limpiarAvisoRepaso) { Text("Seguir con las cartas", color = salon.titulo) }
                }
            )
        } else if (confirmarSalida) {
            AlertDialog(
                onDismissRequest = { confirmarSalida = false },
                containerColor = Color(0xFF1E1A15),
                titleContentColor = ColoresSalon.Crema,
                textContentColor = ColoresSalon.Crema,
                title = { Text("¿Abandonar las flashcards?", fontFamily = FuentesMockup.alegreya(), fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Si sales ahora, la actividad termina sin puntaje y no se registra como completada. " +
                            "Si pagaste entrada de hidromiel, se pierde.",
                        fontFamily = FuentesMockup.alegreya()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { confirmarSalida = false }) { Text("Seguir jugando", color = salon.titulo) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        confirmarSalida = false
                        onSalir()
                    }) { Text("Abandonar", color = ColoresSalon.Rojo) }
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Barra y cabecera
// ---------------------------------------------------------------------------

@Composable
private fun BarraSuperior(izquierda: String, derecha: String, onVolver: () -> Unit) {
    Row(
        Modifier
            .requiredSize(390.dp, ALTO_BARRA.dp)
            .background(Color(0xFF0B0907))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onVolver),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = ColoresSalon.Crema, modifier = Modifier.size(19.dp))
            Text(
                izquierda, color = ColoresSalon.Crema, fontSize = 16.5.sp, fontFamily = FuentesMockup.alegreya(),
                maxLines = 1, modifier = Modifier.padding(start = 8.dp)
            )
        }
        Box(Modifier.weight(1f))
        Text(
            derecha, color = ColoresSalon.Crema.copy(alpha = 0.85f), fontSize = 14.5.sp,
            fontFamily = FuentesMockup.alegreya(), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

/** Texto centrado en (x, y) dentro de una caja de w × h. */
@Composable
private fun Texto(
    texto: String, x: Float, y: Float, tamano: Float, color: Color,
    w: Float = 360f, h: Float = 30f,
    peso: FontWeight = FontWeight.Normal, cursiva: Boolean = false, cinzel: Boolean = false,
    espacio: Float = 0f, alineacion: Alignment = Alignment.Center, lineas: Int = 1, alto: Float = 0f
) {
    Box(Modifier.en(x, y, w, h), contentAlignment = alineacion) {
        Text(
            texto,
            color = color,
            fontSize = tamano.sp,
            fontWeight = peso,
            fontStyle = if (cursiva) FontStyle.Italic else FontStyle.Normal,
            fontFamily = if (cinzel) FuentesMockup.cinzel() else FuentesMockup.alegreya(),
            letterSpacing = espacio.sp,
            textAlign = if (alineacion == Alignment.CenterStart) TextAlign.Start else TextAlign.Center,
            maxLines = lineas,
            overflow = TextOverflow.Ellipsis,
            lineHeight = if (alto > 0f) alto.sp else androidx.compose.ui.unit.TextUnit.Unspecified
        )
    }
}

/** Nombre del lugar y fila de puntos (uno por carta). */
@Composable
private fun CabeceraSalon(salon: SalonMundo, puntos: List<EstadoTarjeta>, actual: Int, actualResuelta: EstadoTarjeta?) {
    Texto(salon.lugar, 195f, 21.7f, 10.5f, salon.titulo, w = 380f, h = 20f, peso = FontWeight.Bold, cinzel = true, espacio = 3f)
    Canvas(Modifier.en(195f, 47.7f, 390f, 24f)) {
        val d = density
        val n = puntos.size
        if (n == 0) return@Canvas
        val paso = min(26f, 320f / max(n - 1, 1)) * d
        val x0 = size.width / 2f - paso * (n - 1) / 2f
        val cy = size.height / 2f
        for (i in 0 until n) {
            val c = Offset(x0 + paso * i, cy)
            var e = puntos[i]
            if (i == actual) {
                if (actualResuelta != null) {
                    e = actualResuelta
                } else {
                    val aro = salon.titulo.mezclado(Color.White, 0.3f)
                    drawCircle(Color.Black.copy(alpha = 0.35f), 9.5f * d, c + Offset(0f, d))
                    drawCircle(aro, 8.7f * d, c, style = Stroke(2f * d))
                    drawCircle(aro, 3.3f * d, c)
                    continue
                }
            }
            when (e) {
                EstadoTarjeta.DOMINADA -> esfera(c, salon.punto, d)
                EstadoTarjeta.FALLADA -> esfera(c, ColoresSalon.Fallada, d)
                EstadoTarjeta.PENDIENTE -> {
                    drawCircle(ColoresSalon.Hueco, 4.6f * d, c)
                    drawArc(
                        Color.White.copy(alpha = 0.14f), 20f, 140f, false,
                        Offset(c.x - 4.6f * d, c.y - 4.6f * d), androidx.compose.ui.geometry.Size(9.2f * d, 9.2f * d), style = Stroke(0.8f * d)
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.esfera(c: Offset, color: Color, d: Float) {
    drawCircle(Color.Black.copy(alpha = 0.45f), 5.8f * d, c + Offset(0f, 1.2f * d))
    drawCircle(
        Brush.radialGradient(listOf(color.escalado(1.5f), color, color.escalado(0.5f)), c - Offset(1.8f * d, 2f * d), 7.5f * d),
        5.8f * d, c
    )
    drawCircle(Color.White.copy(alpha = 0.65f), 1.5f * d, c - Offset(1.8f * d, 2.2f * d))
}

// ---------------------------------------------------------------------------
// Juego
// ---------------------------------------------------------------------------

private enum class EstadoTablilla { NEUTRA, CORRECTA, INCORRECTA, APAGADA }

/**
 * La correcta solo se revela cuando el alumno acierta: si falla, la carta
 * vuelve al mazo y saldrá de nuevo, así que no se le da la respuesta.
 */
private fun estadoTablilla(indice: Int, respuesta: RespuestaFlashcard?): EstadoTablilla = when {
    respuesta == null -> EstadoTablilla.NEUTRA
    indice == respuesta.seleccionada -> if (respuesta.correcta) EstadoTablilla.CORRECTA else EstadoTablilla.INCORRECTA
    else -> EstadoTablilla.APAGADA
}

private val runasOpciones = listOf("ᚠ", "ᛒ", "ᚲ", "ᛗ")

/** Montón al que vuela la carta y el giro con el que queda encima. */
private enum class DestinoCarta(val punto: Offset, val giro: Float) {
    MAZO(Offset(46.3f, 277f), -12f),
    DOMINADAS(Offset(352.5f, 283.5f), 10f)
}

private val curvaVuelo = CubicBezierEasing(0.42f, 0f, 0.22f, 1f)
private val curvaVolteo = CubicBezierEasing(0.3f, 0f, 0.2f, 1f)

/** El montón se aplasta y rebota al recibir una carta. */
private suspend fun Animatable<Float, AnimationVector1D>.aplastar() {
    snapTo(0f)
    animateTo(0.09f, tween(70))
    animateTo(0f, spring(dampingRatio = 0.35f, stiffness = 500f))
}

/** Corre una animación de 0 a 1 (polvo, destello). */
private suspend fun Animatable<Float, AnimationVector1D>.disparar(ms: Int) {
    snapTo(0f)
    animateTo(1f, tween(ms, easing = LinearEasing))
}

@Composable
private fun Juego(
    salon: SalonMundo,
    estado: EstadoFlashcards,
    tarjeta: TarjetaFlashcard,
    nombreTema: String?,
    onResponder: (Int) -> Unit,
    onContinuar: () -> Unit
) {
    val escena = rememberCoroutineScope()
    // 0 = en la mesa, 1 = encima del montón de destino.
    val vuelo = remember { Animatable(0f) }
    // 0 = cara arriba, 1 = boca abajo.
    val boca = remember { Animatable(0f) }
    // 0 = descripción, 1 = concepto.
    val volteo = remember { Animatable(0f) }
    val meneo = remember { Animatable(0f) }
    val temblor = remember { Animatable(0f) }
    val destello = remember { Animatable(1f) }
    val golpeMazo = remember { Animatable(0f) }
    val golpeDominadas = remember { Animatable(0f) }
    val polvoMazo = remember { Animatable(1f) }
    val polvoDominadas = remember { Animatable(1f) }
    var destino by remember { mutableStateOf(DestinoCarta.MAZO) }
    var enMazo by remember { mutableStateOf(false) }
    var enDominadas by remember { mutableStateOf(false) }
    var dominada by remember { mutableStateOf(false) }
    var retro by remember { mutableStateOf(false) }
    var ocupado by remember { mutableStateOf(false) }
    var alMazo by remember { mutableStateOf(false) }

    val respuesta = estado.respuesta
    val fallo = respuesta != null && !respuesta.correcta

    /** Toma la carta de arriba del mazo (boca abajo) y la deja en la mesa, volteándola en el camino. */
    suspend fun repartir(continuar: Boolean) {
        ocupado = true
        destino = DestinoCarta.MAZO
        vuelo.snapTo(1f)
        boca.snapTo(1f)
        volteo.snapTo(0f)
        enMazo = false
        enDominadas = false
        dominada = false
        alMazo = false
        if (continuar) onContinuar()
        escena.launch { vuelo.animateTo(0f, spring(dampingRatio = 0.78f, stiffness = 120f)) }
        escena.launch {
            delay(200)
            boca.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
        }
        delay(520)
        ocupado = false
    }

    // Al aparecer: si hay una respuesta pendiente deja la mesa como quedó; si no, reparte.
    LaunchedEffect(Unit) {
        val r = estado.respuesta
        when {
            r == null -> repartir(continuar = false)
            r.correcta -> {
                volteo.snapTo(1f)
                dominada = true
                retro = true
            }
            else -> {
                destino = DestinoCarta.MAZO
                vuelo.snapTo(1f)
                boca.snapTo(1f)
                enMazo = true
                alMazo = true
                retro = true
            }
        }
    }

    fun responder(i: Int) {
        if (estado.respuesta != null || ocupado) return
        ocupado = true
        val correcta = i == tarjeta.indiceCorrecto
        onResponder(i)
        escena.launch {
            if (correcta) {
                // Se levanta y se voltea para revelar el concepto, con un destello dorado.
                delay(180)
                launch { volteo.animateTo(1f, tween(700, easing = curvaVolteo)) }
                delay(260)
                launch { destello.disparar(950) }
                delay(380)
                dominada = true
                retro = true
                ocupado = false
            } else {
                // La carta "dice que no", da la vuelta en el aire y cae al mazo.
                launch {
                    meneo.animateTo(0f, keyframes {
                        durationMillis = 480
                        -6f at 80
                        5f at 190
                        -3.5f at 290
                        2f at 380
                    })
                }
                launch {
                    temblor.animateTo(0f, keyframes {
                        durationMillis = 380
                        -9f at 50
                        8f at 120
                        -6f at 190
                        4f at 260
                        -2f at 320
                    })
                }
                delay(620)
                destino = DestinoCarta.MAZO
                launch { vuelo.animateTo(1f, tween(800, easing = curvaVuelo)) }
                launch {
                    delay(60)
                    boca.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
                }
                delay(760)
                launch { golpeMazo.aplastar() }
                launch { polvoMazo.disparar(550) }
                enMazo = true
                alMazo = true
                retro = true
                ocupado = false
            }
        }
    }

    fun avanzar() {
        val r = estado.respuesta ?: return
        if (!retro || ocupado) return
        ocupado = true
        escena.launch {
            if (r.correcta) {
                // La carta dominada se da la vuelta en el aire y cae en su montón.
                destino = DestinoCarta.DOMINADAS
                retro = false
                launch { vuelo.animateTo(1f, tween(720, easing = curvaVuelo)) }
                launch {
                    delay(80)
                    boca.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
                }
                delay(690)
                launch { golpeDominadas.aplastar() }
                launch { polvoDominadas.disparar(550) }
                enDominadas = true
                delay(140)
            } else {
                retro = false
                alMazo = false
            }
            repartir(continuar = true)
        }
    }

    val resuelta: EstadoTarjeta? = when {
        respuesta == null -> null
        respuesta.correcta -> if (dominada) EstadoTarjeta.DOMINADA else null
        else -> EstadoTarjeta.FALLADA
    }
    val cantMazo = estado.enMazo + if (enMazo) 1 else 0
    val cantDominadas = estado.dominadas + if (enDominadas) 1 else 0

    CabeceraSalon(salon, estado.puntos, estado.posicionActual, resuelta)
    Texto(
        "Carta ${estado.numero} de ${estado.total}" + if (fallo) " · saldrá de nuevo" else "",
        195f, 67.9f, 12.5f, ColoresSalon.Crema, w = 300f, h = 22f
    )

    // Montones: se aplastan un poco al recibir una carta.
    MontonSalon(
        salon, cantMazo, 11,
        Modifier
            .en(Area.cartaMazo.x - 3f, Area.cartaMazo.y + 4f, 76f, 108f)
            .graphicsLayer {
                val g = golpeMazo.value
                scaleX = 1f + g
                scaleY = 1f - g
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
    )
    Polvo(polvoMazo, salon.titulo, Modifier.en(Area.cartaMazo.x, Area.cartaMazo.y + 50f, 150f, 60f))
    EtiquetaMonton("Mazo", cantMazo, salon.acento, Modifier.en(40.1f, 353.5f, 73f, 35f))
    MontonSalon(
        salon, cantDominadas, 31,
        Modifier
            .en(Area.cartaDominadas.x - 3f, Area.cartaDominadas.y + 4f, 76f, 108f)
            .graphicsLayer {
                val g = golpeDominadas.value
                scaleX = 1f + g
                scaleY = 1f - g
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
    )
    Polvo(polvoDominadas, salon.titulo, Modifier.en(Area.cartaDominadas.x, Area.cartaDominadas.y + 50f, 150f, 60f))
    EtiquetaMonton("Dominadas", cantDominadas, salon.acento, Modifier.en(350f, 353.5f, 73f, 35f))

    // Destello dorado al acertar (detrás de la carta).
    Destello(destello, salon.titulo, Modifier.en(Area.centroCarta.x, Area.centroCarta.y, Area.ANCHO, 420f))

    // Carta en juego
    CartaEnJuego(
        vuelo = vuelo, boca = boca, volteo = volteo, meneo = meneo, destino = destino,
        tarjeta = tarjeta, salon = salon, nombreTema = nombreTema
    )

    AnimatedVisibility(
        visible = alMazo,
        enter = scaleIn(spring(dampingRatio = 0.5f, stiffness = 420f), initialScale = 1.9f) + fadeIn(tween(120)),
        exit = fadeOut(tween(150)),
        modifier = Modifier.en(92f, 196f, 140f, 30f)
    ) {
        Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = -8f }, contentAlignment = Alignment.Center) {
            Text(
                "¡Al mazo!", color = ColoresSalon.Rojo, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                fontFamily = FuentesMockup.cinzel(), maxLines = 1
            )
        }
    }

    // Conceptos
    tarjeta.opciones.take(4).forEachIndexed { i, concepto ->
        val estadoOpcion = estadoTablilla(i, respuesta)
        TablillaOpcion(
            salon = salon,
            runa = runasOpciones[i % 4],
            texto = concepto,
            estado = estadoOpcion,
            habilitada = respuesta == null && !ocupado,
            semilla = 101 + i * 7,
            y = Area.yOpciones[min(i, 3)],
            onClick = { responder(i) },
            modifier = Modifier.graphicsLayer {
                translationX = if (estadoOpcion == EstadoTablilla.INCORRECTA) temblor.value * density else 0f
            }
        )
    }

    val textoBanner = when {
        respuesta == null || !retro -> "Elige el concepto correcto"
        respuesta.correcta -> "¡Correcto! La carta pasa a tu pila de dominadas"
        else -> "Incorrecto · La carta vuelve al fondo del mazo"
    }
    val lineaBanner = when {
        respuesta == null || !retro -> null
        respuesta.correcta -> ColoresSalon.Verde
        else -> ColoresSalon.Rojo
    }
    Banner(textoBanner, lineaBanner, Modifier.en(195.3f, 672.4f, 343f, 33.2f))

    AnimatedVisibility(
        visible = retro && respuesta != null,
        enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 4 } + scaleIn(tween(250), initialScale = 0.92f),
        exit = fadeOut(tween(150)),
        modifier = Modifier.en(195f, 714.5f, 188f, 43f)
    ) {
        val correcta = respuesta?.correcta == true
        BotonTablilla(
            salon = salon,
            texto = if (correcta) "Siguiente carta" else "Continuar",
            icono = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            colorIcono = if (correcta) ColoresSalon.VerdeIcono else ColoresSalon.RojoIcono,
            ancho = 188f, alto = 43f,
            onClick = { avanzar() }
        )
    }
}

// ---------------------------------------------------------------------------
// Animaciones
// ---------------------------------------------------------------------------

/**
 * Carta en juego: vuela en arco entre la mesa y un montón, y se voltea en 3D
 * ([volteo] descripción → concepto, [boca] cara → dorso).
 */
@Composable
private fun CartaEnJuego(
    vuelo: Animatable<Float, AnimationVector1D>,
    boca: Animatable<Float, AnimationVector1D>,
    volteo: Animatable<Float, AnimationVector1D>,
    meneo: Animatable<Float, AnimationVector1D>,
    destino: DestinoCarta,
    tarjeta: TarjetaFlashcard,
    salon: SalonMundo,
    nombreTema: String?
) {
    val pi = PI.toFloat()
    Box(
        Modifier
            .en(Area.centroCarta.x, Area.centroCarta.y, Area.CARTA_ANCHO, Area.CARTA_ALTO)
            .graphicsLayer {
                val t = vuelo.value
                val fin = destino.punto - Area.centroCarta
                // Arco: sube por encima de la mesa y baja sobre el montón.
                val control = Offset(fin.x * 0.45f, min(fin.y, 0f) - 125f)
                val u = 1f - t
                val x = 2f * u * t * control.x + t * t * fin.x
                val y = 2f * u * t * control.y + t * t * fin.y
                val arco = sin(t.coerceIn(0f, 1f) * pi)
                val escala = 1f + (Area.ESCALA_MONTON - 1f) * t + 0.12f * arco
                val levante = 1f + 0.07f * sin(volteo.value * pi)
                val pirueta = (if (destino == DestinoCarta.MAZO) -16f else 16f) * arco
                translationX = (x + meneo.value * 1.4f) * density
                translationY = y * density
                scaleX = escala * levante
                scaleY = escala * levante
                rotationZ = destino.giro * t + pirueta + meneo.value
            }
            .graphicsLayer {
                val elevacion = maxOf(
                    sin(vuelo.value.coerceIn(0f, 1f) * pi),
                    sin(volteo.value * pi),
                    sin(boca.value * pi)
                )
                rotationY = boca.value * 180f
                cameraDistance = 14f * density
                shadowElevation = (8f + 16f * elevacion) * density
                shape = FormaCarta()
                clip = false
            }
    ) {
        if (boca.value >= 0.5f) {
            DorsoCarta(
                salon, Area.CARTA_ANCHO, Area.CARTA_ALTO,
                Modifier.graphicsLayer { scaleX = -1f },
                semilla = 7, tamanoEmblema = 0.4f
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = volteo.value * 180f
                        cameraDistance = 14f * density
                    }
            ) {
                if (volteo.value >= 0.5f) {
                    Box(Modifier.fillMaxSize().graphicsLayer { scaleX = -1f }) { CaraConcepto(tarjeta, salon) }
                } else {
                    CaraDescripcion(tarjeta, salon, nombreTema)
                }
                // Se oscurece un poco al pasar de canto.
                Canvas(Modifier.fillMaxSize()) {
                    val sombra = 0.16f * sin(volteo.value * pi)
                    if (sombra > 0.01f) drawPath(rutaCarta(Rect(Offset.Zero, size)), Color.Black.copy(alpha = sombra))
                }
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            val sombra = 0.16f * sin(boca.value * pi)
            if (sombra > 0.01f) drawPath(rutaCarta(Rect(Offset.Zero, size)), Color.Black.copy(alpha = sombra))
        }
    }
}

/** Polvito que levanta la carta al caer en un montón. */
@Composable
private fun Polvo(progreso: Animatable<Float, AnimationVector1D>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val p = progreso.value
        if (p <= 0f || p >= 1f) return@Canvas
        val d = density
        val e = 1f - (1f - p) * (1f - p) * (1f - p)
        val base = Offset(size.width / 2f, size.height / 2f)
        for (k in 0 until 12) {
            val lado = if (k % 2 == 0) -1f else 1f
            val f = (k / 2) / 5f
            val c = Offset(
                base.x + lado * (14f + 46f * f) * e * d,
                base.y + (-(4f + 18f * (1f - f)) * e + 10f * p * p) * d
            )
            val r = (2.6f - 1.2f * f) * (1f - 0.5f * p) * d
            drawCircle(color.copy(alpha = 0.75f * (1f - p)), r, c)
        }
    }
}

/** Destello al acertar: un halo que se abre detrás de la carta y chispas en forma de estrella. */
@Composable
private fun Destello(progreso: Animatable<Float, AnimationVector1D>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val p = progreso.value
        if (p <= 0f || p >= 1f) return@Canvas
        val d = density
        val e = 1f - (1f - p) * (1f - p) * (1f - p)
        val c = Offset(size.width / 2f, size.height / 2f)
        // Halo
        val w = Area.CARTA_ANCHO * (1f + 0.3f * e) * d
        val h = Area.CARTA_ALTO * (1f + 0.18f * e) * d
        val radio = androidx.compose.ui.geometry.CornerRadius(30f * d)
        for (k in 3 downTo 1) {
            val g = k * 7f * d
            drawRoundRect(
                color.copy(alpha = 0.16f * (1f - p)), Offset(c.x - w / 2f - g, c.y - h / 2f - g),
                androidx.compose.ui.geometry.Size(w + 2f * g, h + 2f * g), radio
            )
        }
        drawRoundRect(
            color.copy(alpha = 0.7f * (1f - p)), Offset(c.x - w / 2f, c.y - h / 2f),
            androidx.compose.ui.geometry.Size(w, h), radio, style = Stroke((2f + 4f * (1f - p)) * d)
        )
        // Chispas
        for (k in 0 until 20) {
            val a = k / 20f * 2f * PI.toFloat() + if (k % 2 == 0) 0.1f else -0.06f
            val lejos = if (k % 3 == 0) 1.35f else 1f
            val rx = (Area.CARTA_ANCHO * 0.5f + (10f + 62f * e) * lejos) * d
            val ry = (Area.CARTA_ALTO * 0.5f + (8f + 52f * e) * lejos) * d
            val pos = Offset(c.x + cos(a) * rx, c.y + sin(a) * ry)
            val r = (if (k % 3 == 0) 7f else 4.5f) * (1f - 0.55f * p) * d
            drawPath(estrella(pos, r), Color.White.copy(alpha = 0.9f * (1f - p)))
            drawPath(estrella(pos, r * 0.75f), color.copy(alpha = 1f - p))
        }
    }
}

/** Estrella de cuatro puntas. */
private fun estrella(c: Offset, r: Float): androidx.compose.ui.graphics.Path {
    val i = r * 0.28f
    return androidx.compose.ui.graphics.Path().apply {
        moveTo(c.x, c.y - r)
        lineTo(c.x + i, c.y - i)
        lineTo(c.x + r, c.y)
        lineTo(c.x + i, c.y + i)
        lineTo(c.x, c.y + r)
        lineTo(c.x - i, c.y + i)
        lineTo(c.x - r, c.y)
        lineTo(c.x - i, c.y - i)
        close()
    }
}

// ---------------------------------------------------------------------------
// Carta
// ---------------------------------------------------------------------------

/** Frente: la descripción; el concepto queda oculto hasta acertar. */
@Composable
private fun CaraDescripcion(tarjeta: TarjetaFlashcard, salon: SalonMundo, nombreTema: String?) {
    Box(Modifier.fillMaxSize()) {
        CuerpoCarta(salon.carta, salon.tinta, Modifier.fillMaxSize(), semilla = tarjeta.id.hashCode() and 0xFFFF)
        EmblemaVista(salon.emblema, salon.tinta, Modifier.en(110.5f, 43.3f, 31f, 26.5f), grosor = 1.6f)
        Texto("DESCRIPCIÓN", 110.5f, 74.4f, 10f, salon.tinta, w = 200f, h = 18f, peso = FontWeight.Bold, cinzel = true, espacio = 1f)
        LineaTallada(salon.tinta, Modifier.en(110.5f, 86.7f, 156f, 10f), rombo = false)
        Texto(tarjeta.descripcion, 110.5f, 164f, 13f, salon.tinta, w = 177f, h = 138f, lineas = 8, alto = 17.3f)
        LineaTallada(salon.tinta, Modifier.en(110.5f, 241f, 156f, 10f), rombo = false)
        Texto(nombreTema ?: "¿Qué concepto es?", 110.5f, 257.8f, 9.4f, salon.tintaSuave, w = 180f, h = 16f, cursiva = true)
    }
}

/** Reverso: al acertar se revela el concepto, con su nombre grabado en runas. */
@Composable
private fun CaraConcepto(tarjeta: TarjetaFlashcard, salon: SalonMundo) {
    Box(Modifier.fillMaxSize()) {
        CuerpoCarta(salon.carta, salon.tinta, Modifier.fillMaxSize(), semilla = (tarjeta.id.hashCode() and 0xFFFF) + 1)
        EmblemaVista(salon.emblema, salon.tinta, Modifier.en(110.5f, 45.5f, 52f, 42f), grosor = 2.3f)
        LineaTallada(salon.tinta, Modifier.en(110.5f, 75.8f, 177.6f, 10f))
        Texto("CONCEPTO", 110.5f, 93.2f, 8f, salon.tintaSuave, w = 120f, h = 14f, peso = FontWeight.Bold, cinzel = true, espacio = 0.6f)
        Box(Modifier.en(110.5f, 149.2f, 192f, 84f), contentAlignment = Alignment.Center) {
            Text(
                tarjeta.concepto.uppercase(),
                color = salon.tinta,
                fontSize = 19.5.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FuentesMockup.cinzel(),
                textAlign = TextAlign.Center,
                lineHeight = 23.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Texto("¡Concepto dominado!", 110.5f, 198.6f, 10f, salon.tintaSuave, w = 180f, h = 16f, cursiva = true)
        LineaTallada(salon.tinta, Modifier.en(110.5f, 221f, 177.6f, 10f))
        Box(
            Modifier
                .en(110.5f, 256.4f, 166.2f, 31.8f)
                .drawBehind {
                    val d = density
                    drawRect(salon.carta.escalado(0.66f))
                    drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.18f), Color.Transparent), 0f, size.height / 2f))
                    drawRect(salon.tinta.copy(alpha = 0.85f), style = Stroke(1.5f * d))
                    drawRect(
                        salon.tinta.copy(alpha = 0.6f), Offset(3.2f * d, 3.2f * d),
                        androidx.compose.ui.geometry.Size(size.width - 6.4f * d, size.height - 6.4f * d), style = Stroke(d)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(aRunas(tarjeta.concepto), color = salon.runaPlaca, fontSize = 11.sp, letterSpacing = 1.5.sp, maxLines = 1)
        }
    }
}

// ---------------------------------------------------------------------------
// Opciones, banner y botón
// ---------------------------------------------------------------------------

/** Tablilla de madera con su canto, para opciones y botones. */
@Composable
private fun FondoTablilla(salon: SalonMundo, semilla: Int, modifier: Modifier = Modifier) {
    val textura = ImageBitmap.imageResource(R.drawable.madera_veta_ah)
    Canvas(modifier) {
        val d = density
        val todo = Rect(Offset.Zero, size)
        val forma = rutaTablilla(todo, 14f * d)
        withTransform({ translate(0f, 3f * d) }) {
            drawPath(forma, Color.Black.copy(alpha = 0.35f))
            drawPath(forma, salon.carta.escalado(0.4f))
        }
        withTransform({ clipPath(forma) }) {
            dibujarMadera(textura, salon.carta, true, semilla, 1)
            drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent, Color.Black.copy(alpha = 0.12f))))
        }
        drawPath(rutaTablilla(todo.deflate(1.5f * d), 14f * d), Color.White.copy(alpha = 0.25f), style = Stroke(d))
        drawPath(forma, salon.tinta.copy(alpha = 0.7f), style = Stroke(1.3f * d))
    }
}

@Composable
private fun TablillaOpcion(
    salon: SalonMundo,
    runa: String,
    texto: String,
    estado: EstadoTablilla,
    habilitada: Boolean,
    semilla: Int,
    y: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apagada = estado == EstadoTablilla.APAGADA
    val tinte = when (estado) {
        EstadoTablilla.CORRECTA -> Color(0xFF5DBB78).copy(alpha = 0.36f)
        EstadoTablilla.INCORRECTA -> ColoresSalon.Salmon.copy(alpha = 0.74f)
        EstadoTablilla.APAGADA -> Color.Black.copy(alpha = 0.36f)
        EstadoTablilla.NEUTRA -> Color.Transparent
    }
    Box(
        Modifier
            .en(195.3f, y, 343f, 55f)
            .then(modifier)
            .clickable(enabled = habilitada, interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .semantics { contentDescription = "Concepto: $texto" }
    ) {
        FondoTablilla(salon, semilla, Modifier.fillMaxSize())
        Canvas(Modifier.fillMaxSize()) {
            val d = density
            val forma = rutaTablilla(Rect(Offset.Zero, size), 14f * d)
            drawPath(forma, tinte)
            when (estado) {
                EstadoTablilla.CORRECTA -> {
                    drawPath(forma, ColoresSalon.Verde.copy(alpha = 0.35f), style = Stroke(7f * d))
                    drawPath(forma, ColoresSalon.Verde, style = Stroke(2.5f * d))
                }
                EstadoTablilla.INCORRECTA -> drawPath(forma, ColoresSalon.Rojo, style = Stroke(2.5f * d))
                else -> Unit
            }
        }
        Box(
            Modifier
                .en(30.2f, 27.5f, 31.8f, 31.8f)
                .graphicsLayer { alpha = if (apagada) 0.6f else 1f }
                .drawBehind {
                    drawCircle(salon.carta.escalado(0.8f))
                    drawCircle(salon.tinta.copy(alpha = 0.8f), style = Stroke(1.3f * density))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(runa, color = salon.tinta.copy(alpha = 0.85f), fontSize = 13.sp)
        }
        Box(Modifier.en(55.2f + 118f, 27.5f, 236f, 50f), contentAlignment = Alignment.CenterStart) {
            Text(
                texto,
                color = salon.tinta.copy(alpha = if (apagada) 0.38f else 1f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FuentesMockup.alegreya(),
                lineHeight = 15.5.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (estado == EstadoTablilla.CORRECTA || estado == EstadoTablilla.INCORRECTA) {
            MarcaSalon(
                correcta = estado == EstadoTablilla.CORRECTA,
                color = if (estado == EstadoTablilla.CORRECTA) ColoresSalon.VerdeIcono else ColoresSalon.RojoIcono,
                modifier = Modifier.en(315.2f, 27.5f, 24.5f, 24.5f)
            )
        }
    }
}

@Composable
private fun MarcaSalon(correcta: Boolean, color: Color, modifier: Modifier = Modifier, diametro: Float = 24.5f) {
    Box(
        modifier
            .shadow(2.dp, CircleShape)
            .background(color, CircleShape)
            .drawBehind { drawCircle(Color.White.copy(alpha = 0.85f), style = Stroke(1.5f * density)) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (correcta) Icons.Filled.Check else Icons.Filled.Close, null,
            tint = Color.White, modifier = Modifier.size((diametro * 0.62f).dp)
        )
    }
}

/** Franja negra con la indicación o la retroalimentación. */
@Composable
private fun Banner(texto: String, linea: Color?, modifier: Modifier = Modifier) {
    Box(modifier.background(ColoresSalon.Negro.copy(alpha = 0.95f)), contentAlignment = Alignment.Center) {
        Text(
            texto, color = ColoresSalon.Crema, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            fontFamily = FuentesMockup.alegreya(), maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        if (linea != null) {
            Box(Modifier.align(Alignment.TopCenter).requiredSize(343.dp, 3.dp).background(linea))
        }
    }
}

/** Botón de tablilla con un ícono redondo a la izquierda. */
@Composable
private fun BotonTablilla(
    salon: SalonMundo,
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorIcono: Color,
    ancho: Float,
    alto: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier.requiredSize(ancho.dp, alto.dp).clickable(onClick = onClick)) {
        FondoTablilla(salon, 77, Modifier.fillMaxSize())
        Box(
            Modifier
                .en(30f, alto / 2f, 26f, 26f)
                .shadow(2.dp, CircleShape)
                .background(colorIcono, CircleShape)
                .drawBehind { drawCircle(Color.White.copy(alpha = 0.85f), style = Stroke(1.5f * density)) },
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Box(Modifier.en(51f + (ancho - 64f) / 2f, alto / 2f, ancho - 64f, alto), contentAlignment = Alignment.CenterStart) {
            Text(
                texto, color = salon.tinta, fontSize = 15.5.sp, fontWeight = FontWeight.Bold,
                fontFamily = FuentesMockup.alegreya(), maxLines = 1
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Nivel completado (paso 9)
// ---------------------------------------------------------------------------

@Composable
private fun PantallaCompletada(salon: SalonMundo, resumen: ResumenFlashcards, puntos: List<EstadoTarjeta>, onVolver: () -> Unit) {
    val r = resumen.resultado
    val filas = resumen.masDificiles.take(3)
    val altoPanel = 149.5f - (3 - max(filas.size, 1)) * 32f
    val yPanel = 416f
    val yTexto = yPanel + altoPanel + 23.8f
    val yBoton = yTexto + 54.5f

    CabeceraSalon(salon, puntos, -1, null)
    AbanicoSalon(salon, r.totalTarjetas.coerceIn(3, 12))
    Banner("¡Skål! Dominaste las ${r.totalTarjetas} cartas", salon.acento, Modifier.en(195.3f, 88.6f, 343f, 35.4f))

    Row(Modifier.en(194.2f, 373f, 328f, 54f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Ficha("${r.totalTarjetas}", "Cartas")
        Ficha("${r.errores}", if (r.errores == 1) "Fallo" else "Fallos")
        Ficha("${resumen.precision}%", "Precisión")
    }

    PanelDificiles(salon, filas, Modifier.en(195f, yPanel + altoPanel / 2f, 329.3f, altoPanel))

    Texto(
        if (r.errores == 0) "Nivel completado: todas las cartas fueron contestadas bien."
        else "Nivel completado: repetiste las falladas hasta dominarlas.",
        195f, yTexto, 12f, ColoresSalon.Crema.copy(alpha = 0.9f), w = 350f, h = 20f, cursiva = true
    )
    BotonTablilla(
        salon = salon,
        texto = "Volver al mundo",
        icono = Icons.Filled.Check,
        colorIcono = ColoresSalon.VerdeIcono,
        ancho = 269f, alto = 48f,
        modifier = Modifier.en(195.3f, yBoton, 269f, 48f),
        onClick = onVolver
    )
}

/** Abanico con las cartas dominadas; la del frente lleva el emblema. */
@Composable
private fun AbanicoSalon(salon: SalonMundo, cantidad: Int) {
    val ancho = 125f
    val alto = 171f
    val pivote = Offset(205f, 520f)
    val radio = 250f
    for (i in 0 until cantidad) {
        val angulo = if (cantidad > 1) -22f + 50f * i / (cantidad - 1) else 0f
        DorsoCarta(
            salon, ancho, alto,
            Modifier
                .en(pivote.x, pivote.y - radio, ancho, alto)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0.5f + radio / alto)
                    rotationZ = angulo
                }
                .shadow(3.dp, FormaCarta(), clip = false),
            semilla = 50 + i,
            conEmblema = i == cantidad - 1,
            tamanoEmblema = 0.55f
        )
    }
}

@Composable
private fun Ficha(valor: String, etiqueta: String) {
    Column(
        Modifier
            .width(104.dp)
            .height(54.dp)
            .background(ColoresSalon.Negro.copy(alpha = 0.95f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(valor, color = ColoresSalon.Crema, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FuentesMockup.cinzel(), lineHeight = 20.sp)
        Text(etiqueta, color = ColoresSalon.Crema.copy(alpha = 0.8f), fontSize = 11.5.sp, fontFamily = FuentesMockup.alegreya(), lineHeight = 14.sp)
    }
}

@Composable
private fun PanelDificiles(salon: SalonMundo, dificiles: List<Pair<String, Int>>, modifier: Modifier = Modifier) {
    Box(modifier.shadow(5.dp)) {
        Madera(salon.carta, Modifier.fillMaxSize(), horizontal = true, variante = 1, semilla = 91)
        Canvas(Modifier.fillMaxSize()) {
            val d = density
            drawRect(salon.tinta.copy(alpha = 0.85f), style = Stroke(1.5f * d))
            drawRect(
                salon.tinta.copy(alpha = 0.35f), Offset(4f * d, 4f * d),
                androidx.compose.ui.geometry.Size(size.width - 8f * d, size.height - 8f * d), style = Stroke(d)
            )
        }
        Texto("LAS QUE MÁS TE COSTARON", 164.65f, 20.2f, 9.5f, salon.tintaSuave, w = 300f, h = 16f, peso = FontWeight.Bold, cinzel = true, espacio = 0.6f)
        if (dificiles.isEmpty()) {
            Texto("Ninguna: no fallaste ninguna carta", 164.65f, 48.2f, 13.7f, salon.tinta, w = 300f, h = 22f, peso = FontWeight.Bold)
        }
        dificiles.forEachIndexed { i, (concepto, fallos) ->
            val y = 48.2f + i * 31.8f
            if (i > 0) Box(Modifier.en(164.65f, y - 15.9f, 294.6f, 1f).background(salon.tinta.copy(alpha = 0.4f)))
            Texto("ᚱ", 21.7f, y, 12f, salon.tintaSuave, w = 20f, h = 20f)
            Texto(concepto, 38.3f + 105f, y, 13.7f, salon.tinta, w = 210f, h = 22f, peso = FontWeight.Bold, alineacion = Alignment.CenterStart)
            Box(Modifier.en(310.7f - 35f, y, 70f, 20f), contentAlignment = Alignment.CenterEnd) {
                Text(
                    if (fallos == 1) "1 fallo" else "$fallos fallos",
                    color = salon.tintaSuave, fontSize = 11.sp, fontFamily = FuentesMockup.alegreya()
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Mensajes
// ---------------------------------------------------------------------------

@Composable
private fun PanelMensaje(salon: SalonMundo, titulo: String, texto: String, accion: String, onAccion: () -> Unit) {
    Box(Modifier.en(195f, 320f, 330f, 260f).shadow(5.dp)) {
        Madera(salon.carta, Modifier.fillMaxSize(), horizontal = true, semilla = 5)
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(titulo, color = salon.tinta, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FuentesMockup.cinzel())
            Text(texto, color = salon.tinta, fontSize = 14.sp, fontFamily = FuentesMockup.alegreya(), textAlign = TextAlign.Center)
            BotonTablilla(
                salon = salon, texto = accion, icono = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                colorIcono = ColoresSalon.VerdeIcono, ancho = 230f, alto = 44f, onClick = onAccion
            )
        }
    }
}
