package com.aesir.odin.ui.flashcards

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.EstadoTarjetaFlashcard
import com.aesir.odin.domain.model.RespuestaFlashcard
import com.aesir.odin.domain.model.TarjetaFlashcard
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.FuentesMockup
import com.aesir.odin.ui.components.odin.LienzoDiseno
import com.aesir.odin.ui.components.odin.TextoAjustable
import com.aesir.odin.ui.components.odin.centroEn
import com.aesir.odin.ui.components.odin.pulsable
import com.aesir.odin.ui.ragnarok.MedallonVista
import com.aesir.odin.ui.theme.EstilosMundo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/** Medidas del lienzo de diseño (el área bajo la barra del mockup, 390 × 774). */
private object Lienzo {
    const val ANCHO = 390f
    const val ALTO = 774f
    val CARTA = Size(221f, 302f)
    val CENTRO_CARTA = Offset(195f, 241.15f)

    /** Centro de la carta superior de cada montón. */
    val CARTA_MAZO = Offset(46.3f, 277f)
    val CARTA_DOMINADAS = Offset(352.5f, 283.5f)
    const val ESCALA_MONTON = 70f / 221f
    val Y_OPCIONES = listOf(425.1f, 486.5f, 548.2f, 609.5f)
}

@Composable
private fun alegreya(tamano: Float, peso: FontWeight = FontWeight.Normal, italica: Boolean = false) = TextStyle(
    fontFamily = FuentesMockup.alegreya(),
    fontSize = tamano.sp,
    fontWeight = peso,
    fontStyle = if (italica) FontStyle.Italic else FontStyle.Normal
)

@Composable
private fun cinzel(tamano: Float, peso: FontWeight = FontWeight.Bold, espacio: Float = 0f) = TextStyle(
    fontFamily = FuentesMockup.cinzel(),
    fontSize = tamano.sp,
    fontWeight = peso,
    letterSpacing = espacio.sp
)

private val sombraTexto = Shadow(Color.Black.copy(alpha = 0.7f), Offset(0f, 1f), 3f)

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
    onRepasarLeccion: (String) -> Unit
) {
    val contexto by viewModel.contexto.collectAsState()
    val estado by viewModel.estado.collectAsState()
    val resumen by viewModel.resumen.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val sugerencia by viewModel.sugerenciaRepaso.collectAsState()
    val avisoRepaso by viewModel.avisoRepaso.collectAsState()
    val nombresTemas by viewModel.nombresTemas.collectAsState()
    val saldo by viewModel.saldo.collectAsState()
    var confirmarSalida by remember { mutableStateOf(false) }

    LaunchedEffect(temaId) { viewModel.iniciar(temaId, temasIncluidos) }

    val orden = contexto?.mundo?.orden ?: 1
    val salon = SalonMundo.porOrden(orden)

    /** Antes de terminar, salir pide confirmación (A4.1); al terminar, solo vuelve. */
    val salir: () -> Unit = {
        if (resumen != null || mensajeError != null) onVolverRoadmap() else confirmarSalida = true
    }
    BackHandler { salir() }

    Box(Modifier.fillMaxSize()) {
        MesaSalon(salon)
        Column(Modifier.fillMaxSize()) {
            BarraSuperior(
                izquierda = contexto?.mundo?.nombre ?: "Roadmap",
                derecha = contexto?.tema?.nombre.orEmpty(),
                orden = orden,
                saldo = saldo,
                onVolver = salir
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                LienzoDiseno(alto = Lienzo.ALTO) {
                    AdornosSalon(salon)
                    val r = resumen
                    val e = estado
                    val tarjeta = e?.tarjeta
                    when {
                        mensajeError != null -> PanelMensaje(salon, "Sin tarjetas", mensajeError.orEmpty(), "Volver al roadmap", onVolverRoadmap)
                        r != null -> PantallaCompletada(salon, r, e?.puntos.orEmpty(), onVolverRoadmap)
                        e != null && tarjeta != null -> Juego(
                            salon = salon,
                            estado = e,
                            tarjeta = tarjeta,
                            nombreTema = nombresTemas[tarjeta.temaId],
                            onResponder = { viewModel.responder(it) },
                            onContinuar = { viewModel.continuar() }
                        )
                        else -> CircularProgressIndicator(color = salon.titulo, modifier = Modifier.centroEn(195f, 300f))
                    }
                }
            }
        }
    }

    // A5.2: la misma tarjeta se falló tres veces.
    if (sugerencia != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Esta carta se te resiste") },
            text = {
                Text("Has fallado esta carta tres veces. Te sugerimos repasar la lección interactiva del tema.\n\nSi aceptas, las flashcards terminan sin puntaje y se abre la lección.")
            },
            confirmButton = { TextButton({ viewModel.repasarLeccion(onRepasarLeccion) }) { Text("Repasar lección") } },
            dismissButton = { TextButton({ viewModel.seguirConTarjetas() }) { Text("Seguir con las cartas") } }
        )
    }
    avisoRepaso?.let { motivo ->
        AlertDialog(
            onDismissRequest = { viewModel.limpiarAvisoRepaso() },
            title = { Text("No se pudo abrir la lección") },
            text = { Text(motivo) },
            confirmButton = { TextButton({ viewModel.limpiarAvisoRepaso() }) { Text("Seguir con las cartas") } }
        )
    }
    // A4.1: salir antes de terminar pide confirmación.
    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Abandonar las flashcards?") },
            text = { Text("Si sales ahora, la actividad termina sin puntaje y no se registra como completada. Si pagaste entrada de hidromiel, se pierde.") },
            confirmButton = {
                TextButton({
                    confirmarSalida = false
                    viewModel.abandonar(onVolverRoadmap)
                }) { Text("Abandonar", color = Color(0xFFE2614B)) }
            },
            dismissButton = { TextButton({ confirmarSalida = false }) { Text("Seguir jugando") } }
        )
    }
}

// ---------------------------------------------------------------------------
// Barra y cabecera
// ---------------------------------------------------------------------------

/**
 * Barra de arriba integrada a la mesa (sin franja negra): volver con el
 * mundo y el nivel, el medallón del mundo al centro y el hidromiel.
 */
@Composable
private fun BarraSuperior(izquierda: String, derecha: String, orden: Int, saldo: Int, onVolver: () -> Unit) {
    val estilo = EstilosMundo.porOrden(orden)
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.25f), Color.Transparent)))
            .statusBarsPadding()
            .height(52.dp)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            Modifier.align(Alignment.CenterStart).widthIn(max = 150.dp).pulsable(alPulsar = onVolver),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ColoresSalon.Crema, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(izquierda, style = alegreya(17f).copy(shadow = sombraTexto), color = ColoresSalon.Crema, maxLines = 1)
                if (derecha.isNotEmpty()) {
                    Text(derecha, style = alegreya(12.5f, italica = true).copy(shadow = sombraTexto), color = ColoresSalon.Crema.copy(alpha = 0.7f), maxLines = 1)
                }
            }
        }
        MedallonVista(orden, 16f, Modifier.align(Alignment.Center))
        ContadorHidromiel(saldo = saldo, metal = estilo.metal, modifier = Modifier.align(Alignment.CenterEnd))
    }
}

/** Nombre del lugar y fila de puntos (uno por carta). */
@Composable
private fun CabeceraSalon(salon: SalonMundo, puntos: List<EstadoTarjetaFlashcard>, actual: Int, actualResuelta: EstadoTarjetaFlashcard?) {
    Text(
        salon.lugar,
        Modifier.centroEn(195f, 21.7f),
        style = cinzel(10.5f, espacio = 3f).copy(shadow = sombraTexto),
        color = salon.titulo,
        maxLines = 1,
        softWrap = false
    )
    PuntosSalon(salon, puntos, actual, actualResuelta, Modifier.centroEn(195f, 47.7f).requiredSize(Lienzo.ANCHO.dp, 24.dp))
}

@Composable
private fun PuntosSalon(
    salon: SalonMundo,
    puntos: List<EstadoTarjetaFlashcard>,
    actual: Int,
    actualResuelta: EstadoTarjetaFlashcard?,
    modifier: Modifier
) {
    Canvas(modifier) {
        val d = density
        val n = puntos.size
        if (n == 0) return@Canvas
        val paso = min(26f, 320f / (n - 1).coerceAtLeast(1)) * d
        val x0 = size.width / 2f - paso * (n - 1) / 2f
        val cy = size.height / 2f
        fun esfera(c: Offset, color: Color) {
            drawCircle(Color.Black.copy(alpha = 0.45f), 5.8f * d, c + Offset(0f, 1.2f * d))
            drawCircle(
                Brush.radialGradient(listOf(color.escalado(1.5f), color, color.escalado(0.5f)), c - Offset(1.8f * d, 2f * d), 7.5f * d),
                5.8f * d, c
            )
            drawCircle(Color.White.copy(alpha = 0.65f), 1.5f * d, c - Offset(1.8f * d, 2.2f * d))
        }
        for (i in 0 until n) {
            val c = Offset(x0 + paso * i, cy)
            var estado = puntos[i]
            if (i == actual) {
                if (actualResuelta != null) {
                    estado = actualResuelta
                } else {
                    val aro = salon.titulo.mezclado(Color.White, 0.3f)
                    drawCircle(Color.Black.copy(alpha = 0.35f), 9.5f * d, c + Offset(0f, 1f * d))
                    drawCircle(aro, 8.7f * d, c, style = Stroke(2f * d))
                    drawCircle(aro, 3.3f * d, c)
                    continue
                }
            }
            when (estado) {
                EstadoTarjetaFlashcard.DOMINADA -> esfera(c, salon.punto)
                EstadoTarjetaFlashcard.FALLADA -> esfera(c, ColoresSalon.Fallada)
                EstadoTarjetaFlashcard.PENDIENTE -> {
                    drawCircle(ColoresSalon.Hueco, 4.6f * d, c)
                    drawArc(
                        Color.White.copy(alpha = 0.14f), 20f, 140f, false,
                        c - Offset(4.6f * d, 4.6f * d), Size(9.2f * d, 9.2f * d), style = Stroke(0.8f * d)
                    )
                }
            }
        }
    }
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

/** Montón al que vuela la carta. */
private enum class DestinoCarta(val punto: Offset, val giro: Float) {
    MAZO(Lienzo.CARTA_MAZO, -12f),
    DOMINADAS(Lienzo.CARTA_DOMINADAS, 10f)
}

private val runasOpciones = listOf("ᚠ", "ᛒ", "ᚲ", "ᛗ")
private val curvaVuelo = CubicBezierEasing(0.42f, 0f, 0.22f, 1f)
private val curvaVolteo = CubicBezierEasing(0.3f, 0f, 0.2f, 1f)

@Composable
private fun Juego(
    salon: SalonMundo,
    estado: EstadoFlashcards,
    tarjeta: TarjetaFlashcard,
    nombreTema: String?,
    onResponder: (Int) -> Unit,
    onContinuar: () -> Unit
) {
    val alcance = rememberCoroutineScope()
    /** 0 = en la mesa, 1 = encima del montón de destino. */
    val vuelo = remember { Animatable(0f) }
    /** 0 = cara arriba, 1 = boca abajo. */
    val boca = remember { Animatable(0f) }
    /** 0 = descripción, 1 = concepto. */
    val volteo = remember { Animatable(0f) }
    val negar = remember { Animatable(0f) }
    val temblor = remember { Animatable(0f) }
    val aplastarMazo = remember { Animatable(0f) }
    val aplastarDominadas = remember { Animatable(0f) }
    val polvoMazo = remember { Animatable(1f) }
    val polvoDominadas = remember { Animatable(1f) }
    val destello = remember { Animatable(1f) }
    var destino by remember { mutableStateOf(DestinoCarta.MAZO) }
    /** La carta fallada ya cayó en el mazo (el contador sube). */
    var enMazo by remember { mutableStateOf(false) }
    /** La carta acertada ya cayó en su montón. */
    var enDominadas by remember { mutableStateOf(false) }
    /** Punto verde de la carta acertada. */
    var dominada by remember { mutableStateOf(false) }
    /** Banner de retroalimentación y botón visibles. */
    var retro by remember { mutableStateOf(false) }
    var ocupado by remember { mutableStateOf(false) }
    var alMazo by remember { mutableStateOf(false) }

    val respuesta = estado.respuesta
    val fallo = respuesta?.let { !it.correcta } ?: false
    val resuelta: EstadoTarjetaFlashcard? = when {
        respuesta == null -> null
        respuesta.correcta -> if (dominada) EstadoTarjetaFlashcard.DOMINADA else null
        else -> EstadoTarjetaFlashcard.FALLADA
    }
    val cantMazo = estado.enMazo + if (enMazo) 1 else 0
    val cantDominadas = estado.dominadas + if (enDominadas) 1 else 0

    suspend fun golpe(aplastar: Animatable<Float, *>, polvo: Animatable<Float, *>) {
        alcance.launch {
            polvo.snapTo(0f)
            polvo.animateTo(1f, tween(550, easing = LinearEasing))
        }
        aplastar.animateTo(0.09f, tween(70))
        aplastar.animateTo(0f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow))
    }

    /** Toma la carta de arriba del mazo (boca abajo) y la deja en la mesa, volteándola en el camino. */
    fun repartir(continuar: Boolean) {
        ocupado = true
        alcance.launch {
            destino = DestinoCarta.MAZO
            vuelo.snapTo(1f)
            boca.snapTo(1f)
            volteo.snapTo(0f)
            enMazo = false
            enDominadas = false
            dominada = false
            alMazo = false
            if (continuar) onContinuar()
            delay(30)
            launch { vuelo.animateTo(0f, spring(dampingRatio = 0.78f, stiffness = 110f)) }
            launch {
                delay(200)
                boca.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
            }
            delay(520)
            ocupado = false
        }
    }

    fun responder(i: Int) {
        if (estado.respuesta != null || ocupado) return
        ocupado = true
        val correcta = i == tarjeta.indiceCorrecto
        onResponder(i)
        alcance.launch {
            if (correcta) {
                // Se levanta y se voltea para revelar el concepto, con un destello dorado.
                delay(180)
                launch { volteo.animateTo(1f, tween(700, easing = curvaVolteo)) }
                delay(260)
                launch { destello.snapTo(0f); destello.animateTo(1f, tween(950, easing = LinearEasing)) }
                delay(380)
                dominada = true
                retro = true
                ocupado = false
            } else {
                // La carta "dice que no", da la vuelta en el aire y cae al mazo.
                launch {
                    negar.animateTo(0f, keyframes {
                        durationMillis = 480
                        -6f at 80
                        5f at 190
                        -3.5f at 290
                        2f at 380
                        0f at 480
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
                        0f at 380
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
                launch { golpe(aplastarMazo, polvoMazo) }
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
        if (r.correcta) {
            // La carta dominada se da la vuelta en el aire y cae en su montón.
            destino = DestinoCarta.DOMINADAS
            retro = false
            alcance.launch {
                launch { vuelo.animateTo(1f, tween(720, easing = curvaVuelo)) }
                launch {
                    delay(80)
                    boca.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
                }
                delay(690)
                launch { golpe(aplastarDominadas, polvoDominadas) }
                enDominadas = true
                delay(140)
                repartir(true)
            }
        } else {
            retro = false
            alMazo = false
            repartir(true)
        }
    }

    /** Al aparecer: si hay una respuesta pendiente deja la mesa como quedó; si no, reparte la primera carta. */
    LaunchedEffect(Unit) {
        val r = estado.respuesta
        if (r == null) {
            repartir(false)
        } else if (r.correcta) {
            volteo.snapTo(1f)
            dominada = true
            retro = true
        } else {
            destino = DestinoCarta.MAZO
            vuelo.snapTo(1f)
            boca.snapTo(1f)
            enMazo = true
            alMazo = true
            retro = true
        }
    }

    CabeceraSalon(salon, estado.puntos, estado.posicionActual, resuelta)
    Text(
        "Carta ${estado.numero} de ${estado.total}" + if (fallo) " · saldrá de nuevo" else "",
        Modifier.centroEn(195f, 67.9f),
        style = alegreya(12.5f).copy(shadow = sombraTexto),
        color = ColoresSalon.Crema,
        maxLines = 1,
        softWrap = false
    )

    // Montones: se aplastan un poco al recibir una carta.
    MontonSalon(
        salon, cantMazo, 11,
        Modifier.centroEn(Lienzo.CARTA_MAZO.x - 3f, Lienzo.CARTA_MAZO.y + 4f).aplastado(aplastarMazo.value)
    )
    Polvo(polvoMazo.value, salon.titulo, Modifier.centroEn(Lienzo.CARTA_MAZO.x, Lienzo.CARTA_MAZO.y + 50f).requiredSize(150.dp, 60.dp))
    EtiquetaMonton("Mazo", cantMazo, salon.acento, Modifier.centroEn(40.1f, 353.5f))
    MontonSalon(
        salon, cantDominadas, 31,
        Modifier.centroEn(Lienzo.CARTA_DOMINADAS.x - 3f, Lienzo.CARTA_DOMINADAS.y + 4f).aplastado(aplastarDominadas.value)
    )
    Polvo(polvoDominadas.value, salon.titulo, Modifier.centroEn(Lienzo.CARTA_DOMINADAS.x, Lienzo.CARTA_DOMINADAS.y + 50f).requiredSize(150.dp, 60.dp))
    EtiquetaMonton("Dominadas", cantDominadas, salon.acento, Modifier.centroEn(350f, 353.5f))

    // Destello dorado al acertar (detrás de la carta).
    Destello(destello.value, salon.titulo, Modifier.centroEn(Lienzo.CENTRO_CARTA.x, Lienzo.CENTRO_CARTA.y).requiredSize(Lienzo.ANCHO.dp, 420.dp))

    // Carta en juego
    CartaEnJuego(
        vuelo.value, boca.value, volteo.value, destino, tarjeta, salon, nombreTema,
        Modifier
            .centroEn(Lienzo.CENTRO_CARTA.x, Lienzo.CENTRO_CARTA.y)
            .graphicsLayer {
                rotationZ = negar.value
                translationX = negar.value * 1.4f * density
            }
    )

    AnimatedVisibility(
        alMazo,
        modifier = Modifier.centroEn(92f, 196f),
        enter = scaleIn(spring(dampingRatio = 0.55f), initialScale = 1.9f) + fadeIn(),
        exit = fadeOut(tween(200))
    ) {
        Text(
            "¡Al mazo!",
            Modifier.rotate(-8f),
            style = cinzel(15f).copy(shadow = Shadow(Color.Black.copy(alpha = 0.7f), Offset(0f, 1f), 4f)),
            color = ColoresSalon.Rojo
        )
    }

    // Conceptos
    tarjeta.opciones.forEachIndexed { i, concepto ->
        val estadoOpcion = estadoTablilla(i, respuesta)
        TablillaOpcion(
            salon = salon,
            runa = runasOpciones[i % 4],
            texto = concepto,
            estado = estadoOpcion,
            habilitada = respuesta == null && !ocupado,
            semilla = 101 + i * 7,
            modifier = Modifier
                .centroEn(195.3f, Lienzo.Y_OPCIONES[min(i, 3)])
                .graphicsLayer { translationX = if (estadoOpcion == EstadoTablilla.INCORRECTA) temblor.value * density else 0f },
            accion = { responder(i) }
        )
    }

    Banner(
        texto = when {
            respuesta == null || !retro -> "Elige el concepto correcto"
            respuesta.correcta -> "¡Correcto! La carta pasa a tu pila de dominadas"
            else -> "Incorrecto · La carta vuelve al fondo del mazo"
        },
        linea = when {
            respuesta == null || !retro -> null
            respuesta.correcta -> ColoresSalon.Verde
            else -> ColoresSalon.Rojo
        },
        modifier = Modifier.centroEn(195.3f, 672.4f)
    )

    AnimatedVisibility(
        retro && respuesta != null,
        modifier = Modifier.centroEn(195f, 714.5f),
        enter = fadeIn(tween(250)) + slideInVertically(tween(300)) { it / 3 } + scaleIn(initialScale = 0.92f),
        exit = fadeOut(tween(180))
    ) {
        val correcta = respuesta?.correcta == true
        BotonTablilla(
            salon = salon,
            texto = if (correcta) "Siguiente carta" else "Continuar",
            icono = Icons.Filled.KeyboardArrowRight,
            colorIcono = if (correcta) ColoresSalon.VerdeIcono else ColoresSalon.RojoIcono,
            ancho = 188f,
            alto = 43f,
            accion = { avanzar() }
        )
    }
}

/** El montón se aplasta y rebota al recibir una carta (desde la base). */
private fun Modifier.aplastado(a: Float) = graphicsLayer {
    scaleX = 1f + a
    scaleY = 1f - a
    transformOrigin = TransformOrigin(0.5f, 1f)
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
    vuelo: Float,
    boca: Float,
    volteo: Float,
    destino: DestinoCarta,
    tarjeta: TarjetaFlashcard,
    salon: SalonMundo,
    nombreTema: String?,
    modifier: Modifier
) {
    val t = vuelo
    val fin = destino.punto - Lienzo.CENTRO_CARTA
    // Arco: sube por encima de la mesa y baja sobre el montón.
    val control = Offset(fin.x * 0.45f, min(fin.y, 0f) - 125f)
    val u = 1f - t
    val x = 2f * u * t * control.x + t * t * fin.x
    val y = 2f * u * t * control.y + t * t * fin.y
    val arco = sin(t.coerceIn(0f, 1f) * PI.toFloat())
    val escala = 1f + (Lienzo.ESCALA_MONTON - 1f) * t + 0.12f * arco
    val pirueta = (if (destino == DestinoCarta.MAZO) -16f else 16f) * arco
    val giro = destino.giro * vuelo + pirueta
    val elevacion = max(arco, max(sin(volteo * PI.toFloat()), sin(boca * PI.toFloat())))
    val levante = 1f + 0.07f * sin(volteo * PI.toFloat())

    Box(
        modifier
            .requiredSize(Lienzo.CARTA.width.dp, Lienzo.CARTA.height.dp)
            .graphicsLayer {
                translationX = x * density
                translationY = y * density
                rotationZ = giro
                scaleX = escala * levante
                scaleY = escala * levante
            }
            .drawBehind {
                // Sombra que se aleja de la mesa al levantar la carta
                val desp = (7f + 18f * elevacion) * density
                val radio = (8f + 16f * elevacion) * density
                val alfa = 0.5f - 0.18f * elevacion
                for (k in 3 downTo 1) {
                    val inflar = radio * k / 3f
                    drawRoundRect(
                        Color.Black.copy(alpha = alfa / 4f),
                        Offset(-inflar / 2f, desp - inflar / 2f),
                        Size(size.width + inflar, size.height + inflar),
                        CornerRadius(inflar)
                    )
                }
            }
            .graphicsLayer {
                rotationY = boca * 180f
                cameraDistance = 9f * density
            }
    ) {
        val oscurecer = 0.16f * sin(boca * PI.toFloat())
        if (boca >= 0.5f) {
            Box(Modifier.fillMaxSize().graphicsLayer { scaleX = -1f }) {
                DorsoCarta(salon, Lienzo.CARTA.width, Lienzo.CARTA.height, semilla = 7, tamanoEmblema = 0.4f)
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = volteo * 180f
                        cameraDistance = 9f * density
                    }
            ) {
                if (volteo >= 0.5f) {
                    Box(Modifier.fillMaxSize().graphicsLayer { scaleX = -1f }) { CaraConcepto(tarjeta, salon) }
                } else {
                    CaraDescripcion(tarjeta, salon, nombreTema)
                }
                val sombraVolteo = 0.16f * sin(volteo * PI.toFloat())
                if (sombraVolteo > 0.005f) {
                    Box(Modifier.fillMaxSize().clip(FormaCarta()).background(Color.Black.copy(alpha = sombraVolteo)))
                }
            }
        }
        if (oscurecer > 0.005f) {
            Box(Modifier.fillMaxSize().clip(FormaCarta()).background(Color.Black.copy(alpha = oscurecer)))
        }
    }
}

/** Polvito que levanta la carta al caer en un montón ([p] de 0 a 1). */
@Composable
private fun Polvo(p: Float, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        if (p <= 0f || p >= 1f) return@Canvas
        val d = density
        val e = 1f - (1f - p).pow(3)
        val base = center
        for (k in 0 until 12) {
            val lado = if (k % 2 == 0) -1f else 1f
            val f = (k / 2) / 5f
            val c = Offset(
                base.x + lado * (14f + 46f * f) * e * d,
                base.y - (4f + 18f * (1f - f)) * e * d + 10f * p * p * d
            )
            val r = (2.6f - 1.2f * f) * (1f - 0.5f * p) * d
            drawCircle(color.copy(alpha = 0.75f * (1f - p)), r, c)
        }
    }
}

/** Destello al acertar: un halo que se abre detrás de la carta y chispas en forma de estrella. */
@Composable
private fun Destello(p: Float, color: Color, modifier: Modifier) {
    Box(modifier) {
        if (p > 0f && p < 1f) {
            val e = 1f - (1f - p).pow(3)
            // Halo borroso
            Canvas(Modifier.fillMaxSize().blur(10.dp)) {
                val d = density
                val w = Lienzo.CARTA.width * (1f + 0.3f * e) * d
                val h = Lienzo.CARTA.height * (1f + 0.18f * e) * d
                drawRoundRect(color.copy(alpha = 0.55f * (1f - p)), center - Offset(w / 2f, h / 2f), Size(w, h), CornerRadius(30f * d))
            }
            Canvas(Modifier.fillMaxSize()) {
                val d = density
                val c = center
                val w = Lienzo.CARTA.width * (1f + 0.3f * e) * d
                val h = Lienzo.CARTA.height * (1f + 0.18f * e) * d
                drawRoundRect(
                    color.copy(alpha = 0.7f * (1f - p)), c - Offset(w / 2f, h / 2f), Size(w, h), CornerRadius(30f * d),
                    style = Stroke((2f + 4f * (1f - p)) * d)
                )
                for (k in 0 until 20) {
                    val a = k / 20f * 2f * PI.toFloat() + if (k % 2 == 0) 0.1f else -0.06f
                    val lejos = if (k % 3 == 0) 1.35f else 1f
                    val rx = (Lienzo.CARTA.width * 0.5f + (10f + 62f * e) * lejos) * d
                    val ry = (Lienzo.CARTA.height * 0.5f + (8f + 52f * e) * lejos) * d
                    val pos = Offset(c.x + cos(a) * rx, c.y + sin(a) * ry)
                    val r = (if (k % 3 == 0) 7f else 4.5f) * (1f - 0.55f * p) * d
                    drawPath(estrella(pos, r), Color.White.copy(alpha = 0.9f * (1f - p)))
                    drawPath(estrella(pos, r * 0.75f), color.copy(alpha = 1f - p))
                }
            }
        }
    }
}

/** Estrella de cuatro puntas. */
private fun estrella(c: Offset, r: Float): Path {
    val i = r * 0.28f
    return Path().apply {
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
    Box(Modifier.requiredSize(Lienzo.CARTA.width.dp, Lienzo.CARTA.height.dp)) {
        CuerpoCarta(salon.carta, salon.tinta, Modifier.fillMaxSize(), semilla = tarjeta.id.hashCode() and 0xFFFF)
        EmblemaVista(salon.emblema, salon.tinta, Modifier.centroEn(110.5f, 43.3f).requiredSize(31.dp, 26.5.dp), grosor = 1.6f)
        Text("DESCRIPCIÓN", Modifier.centroEn(110.5f, 74.4f), style = cinzel(10f, espacio = 1f), color = salon.tinta, maxLines = 1, softWrap = false)
        LineaTallada(salon.tinta, Modifier.centroEn(110.5f, 86.7f).requiredSize(156.dp, 10.dp), rombo = false)
        TextoAjustable(
            tarjeta.descripcion,
            alegreya(13f).copy(textAlign = TextAlign.Center, lineHeight = 14.7.sp),
            Modifier.centroEn(110.5f, 164f).requiredSize(177.dp, 138.dp).wrapContentHeight(Alignment.CenterVertically),
            minEscala = 0.6f,
            maxLineas = 9,
            color = salon.tinta
        )
        LineaTallada(salon.tinta, Modifier.centroEn(110.5f, 241f).requiredSize(156.dp, 10.dp), rombo = false)
        TextoAjustable(
            nombreTema ?: "¿Qué concepto es?",
            alegreya(9.4f, italica = true).copy(textAlign = TextAlign.Center),
            Modifier.centroEn(110.5f, 257.8f).width(180.dp),
            minEscala = 0.7f,
            color = salon.tintaSuave
        )
    }
}

/** Reverso: al acertar se revela el concepto, con su nombre grabado en runas. */
@Composable
private fun CaraConcepto(tarjeta: TarjetaFlashcard, salon: SalonMundo) {
    Box(Modifier.requiredSize(Lienzo.CARTA.width.dp, Lienzo.CARTA.height.dp)) {
        CuerpoCarta(salon.carta, salon.tinta, Modifier.fillMaxSize(), semilla = (tarjeta.id.hashCode() and 0xFFFF) + 1)
        EmblemaVista(salon.emblema, salon.tinta, Modifier.centroEn(110.5f, 45.5f).requiredSize(52.dp, 42.dp), grosor = 2.3f)
        LineaTallada(salon.tinta, Modifier.centroEn(110.5f, 75.8f).requiredSize(177.6.dp, 10.dp))
        Text("CONCEPTO", Modifier.centroEn(110.5f, 93.2f), style = cinzel(8f, espacio = 0.6f), color = salon.tintaSuave, maxLines = 1, softWrap = false)
        TextoAjustable(
            tarjeta.concepto.uppercase(),
            cinzel(19.5f, FontWeight.Black).copy(
                textAlign = TextAlign.Center,
                lineHeight = 21.5.sp,
                shadow = Shadow(Color.White.copy(alpha = 0.3f), Offset(0f, 1f), 0f)
            ),
            Modifier.centroEn(110.5f, 149.2f).requiredSize(192.dp, 84.dp).wrapContentHeight(Alignment.CenterVertically),
            minEscala = 0.55f,
            maxLineas = 3,
            color = salon.tinta
        )
        Text("¡Concepto dominado!", Modifier.centroEn(110.5f, 198.6f), style = alegreya(10f, italica = true), color = salon.tintaSuave, maxLines = 1, softWrap = false)
        LineaTallada(salon.tinta, Modifier.centroEn(110.5f, 221f).requiredSize(177.6.dp, 10.dp))
        PlacaRunas(tarjeta.concepto, salon, Modifier.centroEn(110.5f, 256.4f).requiredSize(166.2.dp, 31.8.dp))
    }
}

/** Placa rebajada con el concepto escrito en runas. */
@Composable
private fun PlacaRunas(texto: String, salon: SalonMundo, modifier: Modifier) {
    Box(
        modifier.drawBehind {
            val d = density
            drawRect(salon.carta.escalado(0.66f))
            drawRect(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.18f), 0.5f to Color.Transparent))
            drawRect(salon.tinta.copy(alpha = 0.85f), style = Stroke(1.5f * d))
            drawRect(salon.tinta.copy(alpha = 0.6f), Offset(3.2f * d, 3.2f * d), Size(size.width - 6.4f * d, size.height - 6.4f * d), style = Stroke(1f * d))
        },
        contentAlignment = Alignment.Center
    ) {
        TextoAjustable(
            aRunas(texto),
            TextStyle(fontSize = 11.sp, letterSpacing = 1.5.sp),
            Modifier.padding(horizontal = 10.dp),
            minEscala = 0.5f,
            color = salon.runaPlaca
        )
    }
}

// ---------------------------------------------------------------------------
// Opciones, banner y botón
// ---------------------------------------------------------------------------

@Composable
private fun TablillaOpcion(
    salon: SalonMundo,
    runa: String,
    texto: String,
    estado: EstadoTablilla,
    habilitada: Boolean,
    semilla: Int,
    modifier: Modifier,
    accion: () -> Unit
) {
    val tinte by animateColorAsState(
        when (estado) {
            EstadoTablilla.CORRECTA -> Color(0xFF5DBB78).copy(alpha = 0.36f)
            EstadoTablilla.INCORRECTA -> ColoresSalon.Salmon.copy(alpha = 0.74f)
            EstadoTablilla.APAGADA -> Color.Black.copy(alpha = 0.36f)
            EstadoTablilla.NEUTRA -> Color.Transparent
        }, tween(200), label = "tinte"
    )
    val apagada = estado == EstadoTablilla.APAGADA
    Box(modifier.requiredSize(343.dp, 55.dp).pulsable(habilitada, accion)) {
        FondoTablilla(salon, semilla)
        Canvas(Modifier.fillMaxSize()) {
            val forma = rutaTablilla(Rect(Offset.Zero, size), 14f * density)
            drawPath(forma, tinte)
            when (estado) {
                EstadoTablilla.CORRECTA -> {
                    drawPath(rutaTablilla(Rect(Offset.Zero, size).inflate(3f * density), 16f * density), ColoresSalon.Verde.copy(alpha = 0.25f), style = Stroke(5f * density))
                    drawPath(forma, ColoresSalon.Verde, style = Stroke(2.5f * density))
                }
                EstadoTablilla.INCORRECTA -> drawPath(forma, ColoresSalon.Rojo, style = Stroke(2.5f * density))
                else -> Unit
            }
        }
        Box(
            Modifier
                .centroEn(30.2f, 27.5f)
                .requiredSize(31.8.dp)
                .alpha(if (apagada) 0.6f else 1f)
                .drawBehind {
                    drawCircle(salon.carta.escalado(0.8f))
                    drawCircle(salon.tinta.copy(alpha = 0.8f), style = Stroke(1.3f * density))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(runa, style = TextStyle(fontSize = 13.sp), color = salon.tinta.copy(alpha = 0.85f))
        }
        TextoAjustable(
            texto,
            alegreya(13f, FontWeight.Medium).copy(lineHeight = 13.5.sp),
            Modifier.centroEn(55.2f + 118f, 27.5f).requiredSize(236.dp, 50.dp).wrapContentHeight(Alignment.CenterVertically),
            minEscala = 0.75f,
            maxLineas = 2,
            color = salon.tinta.copy(alpha = if (apagada) 0.38f else 1f)
        )
        AnimatedVisibility(
            estado == EstadoTablilla.CORRECTA || estado == EstadoTablilla.INCORRECTA,
            modifier = Modifier.centroEn(315.2f, 27.5f),
            enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn(),
            exit = fadeOut()
        ) {
            if (estado == EstadoTablilla.INCORRECTA) MarcaSalon(Icons.Filled.Close, ColoresSalon.RojoIcono)
            else MarcaSalon(Icons.Filled.Check, ColoresSalon.VerdeIcono)
        }
    }
}

/** Tablilla de madera con su canto, para opciones y botones. */
@Composable
private fun FondoTablilla(salon: SalonMundo, semilla: Int) {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val d = density
            val r = Rect(Offset.Zero, size)
            // Canto y sombra
            drawPath(rutaTablilla(r.translate(0f, 6f * d), 14f * d), Color.Black.copy(alpha = 0.22f))
            drawPath(rutaTablilla(r.translate(0f, 3f * d), 14f * d), salon.carta.escalado(0.4f))
        }
        Madera(salon.carta, Modifier.fillMaxSize().clip(FormaTablilla()), horizontal = true, variante = 0, semilla = semilla)
        Canvas(Modifier.fillMaxSize()) {
            val d = density
            val r = Rect(Offset.Zero, size)
            val forma = rutaTablilla(r, 14f * d)
            drawPath(forma, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent, Color.Black.copy(alpha = 0.12f))))
            drawPath(rutaTablilla(r.deflate(1.5f * d), 13f * d), Color.White.copy(alpha = 0.25f), style = Stroke(1f * d))
            drawPath(forma, salon.tinta.copy(alpha = 0.7f), style = Stroke(1.3f * d))
        }
    }
}

@Composable
private fun MarcaSalon(icono: androidx.compose.ui.graphics.vector.ImageVector, color: Color, diametro: Float = 24.5f) {
    Box(
        Modifier
            .requiredSize(diametro.dp)
            .shadow(1.5.dp, CircleShape)
            .background(color, CircleShape)
            .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, null, tint = Color.White, modifier = Modifier.size((diametro * 0.6f).dp))
    }
}

/** Franja negra con la indicación o la retroalimentación. */
@Composable
private fun Banner(texto: String, linea: Color?, modifier: Modifier, alto: Float = 33.2f) {
    Box(
        modifier
            .requiredSize(343.dp, alto.dp)
            .background(ColoresSalon.Negro.copy(alpha = 0.95f))
            .drawBehind { if (linea != null) drawRect(linea, size = Size(size.width, 3f * density)) },
        contentAlignment = Alignment.Center
    ) {
        TextoAjustable(texto, alegreya(13f, FontWeight.Bold), Modifier.padding(horizontal = 10.dp), minEscala = 0.75f, color = ColoresSalon.Crema)
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
    accion: () -> Unit
) {
    Box(Modifier.requiredSize(ancho.dp, alto.dp).pulsable(alPulsar = accion)) {
        FondoTablilla(salon, 77)
        Box(Modifier.centroEn(30f, alto / 2f)) { MarcaSalon(icono, colorIcono, 26f) }
        TextoAjustable(
            texto,
            alegreya(15.5f, FontWeight.Bold),
            Modifier.centroEn(51f + (ancho - 64f) / 2f, alto / 2f).width((ancho - 64f).dp),
            minEscala = 0.8f,
            color = salon.tinta
        )
    }
}

// ---------------------------------------------------------------------------
// Nivel completado (paso 9)
// ---------------------------------------------------------------------------

@Composable
private fun PantallaCompletada(
    salon: SalonMundo,
    resumen: ResumenFlashcards,
    puntos: List<EstadoTarjetaFlashcard>,
    onVolver: () -> Unit
) {
    val r = resumen.resultado
    val filas = resumen.masDificiles.take(3)
    val altoPanel = 149.5f - (3 - filas.size.coerceAtLeast(1)) * 32f
    val yPanel = 416f
    val yTexto = yPanel + altoPanel + 23.8f
    val yBoton = yTexto + 54.5f

    CabeceraSalon(salon, puntos, -1, null)
    AbanicoSalon(salon, r.totalTarjetas.coerceIn(3, 12))
    Banner("¡Skål! Dominaste las ${r.totalTarjetas} cartas", salon.acento, Modifier.centroEn(195.3f, 88.6f), alto = 35.4f)
    Row(Modifier.centroEn(194.2f, 373f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Ficha("${r.totalTarjetas}", "Cartas")
        Ficha("${r.errores}", if (r.errores == 1) "Fallo" else "Fallos")
        Ficha("${resumen.precision}%", "Precisión")
    }
    PanelDificiles(salon, filas, Modifier.centroEn(195f, yPanel + altoPanel / 2f).requiredSize(329.3.dp, altoPanel.dp))
    TextoAjustable(
        if (r.errores == 0) "Nivel completado: todas las cartas fueron contestadas bien."
        else "Nivel completado: repetiste las falladas hasta dominarlas.",
        alegreya(12f, italica = true).copy(textAlign = TextAlign.Center, shadow = Shadow(Color.Black.copy(alpha = 0.8f), Offset(0f, 1f), 4f)),
        Modifier.centroEn(195f, yTexto).width(350.dp),
        minEscala = 0.8f,
        color = ColoresSalon.Crema.copy(alpha = 0.9f)
    )
    Box(Modifier.centroEn(195.3f, yBoton)) {
        BotonTablilla(salon, "Volver al mundo", Icons.Filled.Check, ColoresSalon.VerdeIcono, 269f, 48f, onVolver)
    }
}

/** Abanico con las cartas dominadas; la del frente lleva el emblema. */
@Composable
private fun AbanicoSalon(salon: SalonMundo, cantidad: Int) {
    val carta = Size(125f, 171f)
    val pivote = Offset(205f, 520f)
    val radio = 250f
    for (i in 0 until cantidad) {
        val angulo = if (cantidad > 1) -22f + 50f * i / (cantidad - 1) else 0f
        Box(
            Modifier
                .centroEn(pivote.x, pivote.y - radio)
                .requiredSize(carta.width.dp, carta.height.dp)
                .graphicsLayer {
                    rotationZ = angulo
                    transformOrigin = TransformOrigin(0.5f, 0.5f + radio / carta.height)
                }
                .drawBehind {
                    drawRoundRect(Color.Black.copy(alpha = 0.3f), Offset(-2f * density, 2f * density), size, CornerRadius(6f * density))
                }
        ) {
            DorsoCarta(salon, carta.width, carta.height, semilla = 50 + i, conEmblema = i == cantidad - 1, tamanoEmblema = 0.55f)
        }
    }
}

@Composable
private fun Ficha(valor: String, etiqueta: String) {
    Column(
        Modifier.requiredSize(104.dp, 54.dp).background(ColoresSalon.Negro.copy(alpha = 0.95f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(valor, style = cinzel(18f), color = ColoresSalon.Crema)
        Text(etiqueta, style = alegreya(11.5f), color = ColoresSalon.Crema.copy(alpha = 0.8f))
    }
}

@Composable
private fun PanelDificiles(salon: SalonMundo, dificiles: List<ResumenFlashcards.ConceptoDificil>, modifier: Modifier) {
    Box(
        modifier
            .shadow(5.dp, androidx.compose.ui.graphics.RectangleShape)
            .clip(androidx.compose.ui.graphics.RectangleShape)
    ) {
        Madera(salon.carta, Modifier.fillMaxSize(), horizontal = true, variante = 1, semilla = 91)
        Canvas(Modifier.fillMaxSize()) {
            val d = density
            drawRect(salon.tinta.copy(alpha = 0.85f), style = Stroke(1.5f * d))
            drawRect(salon.tinta.copy(alpha = 0.35f), Offset(4f * d, 4f * d), Size(size.width - 8f * d, size.height - 8f * d), style = Stroke(1f * d))
        }
        Text("LAS QUE MÁS TE COSTARON", Modifier.centroEn(164.65f, 20.2f), style = cinzel(9.5f, espacio = 0.6f), color = salon.tintaSuave, maxLines = 1, softWrap = false)
        if (dificiles.isEmpty()) {
            Text("Ninguna: no fallaste ninguna carta", Modifier.centroEn(164.65f, 48.2f), style = alegreya(13.7f, FontWeight.Bold), color = salon.tinta, maxLines = 1, softWrap = false)
        }
        dificiles.forEachIndexed { i, d ->
            val y = 48.2f + i * 31.8f
            if (i > 0) {
                Box(Modifier.centroEn(164.65f, y - 15.9f).requiredSize(294.6.dp, 1.dp).background(salon.tinta.copy(alpha = 0.4f)))
            }
            Text("ᚱ", Modifier.centroEn(21.7f, y), style = TextStyle(fontSize = 12.sp), color = salon.tintaSuave)
            TextoAjustable(d.concepto, alegreya(13.7f, FontWeight.Bold), Modifier.centroEn(38.3f + 105f, y).width(210.dp), minEscala = 0.7f, color = salon.tinta)
            Text(
                if (d.fallos == 1) "1 fallo" else "${d.fallos} fallos",
                Modifier.centroEn(310.7f - 35f, y).width(70.dp),
                style = alegreya(11f).copy(textAlign = TextAlign.End),
                color = salon.tintaSuave,
                maxLines = 1
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Mensajes
// ---------------------------------------------------------------------------

@Composable
private fun PanelMensaje(salon: SalonMundo, titulo: String, texto: String, accion: String, onAccion: () -> Unit) {
    Box(Modifier.centroEn(195f, 320f).width(330.dp).shadow(5.dp)) {
        Madera(salon.carta, Modifier.matchParentSize(), horizontal = true, semilla = 5)
        Column(
            Modifier
                .border(1.5.dp, salon.tinta.copy(alpha = 0.8f))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(titulo, style = cinzel(18f), color = salon.tinta)
            Text(texto, Modifier.padding(vertical = 12.dp), style = alegreya(14f).copy(textAlign = TextAlign.Center), color = salon.tinta)
            BotonTablilla(salon, accion, Icons.Filled.KeyboardArrowRight, ColoresSalon.VerdeIcono, 230f, 44f, onAccion)
        }
    }
}
