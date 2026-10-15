package com.aesir.odin.ui.flashcards

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
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
    val volteo = remember { Animatable(0f) }
    val dx = remember { Animatable(0f) }
    val dy = remember { Animatable(0f) }
    val escala = remember { Animatable(1f) }
    val giro = remember { Animatable(0f) }
    var enMazo by remember { mutableStateOf(false) }
    var dominada by remember { mutableStateOf(false) }
    var lineas by remember { mutableStateOf(false) }
    var retro by remember { mutableStateOf(false) }
    var ocupado by remember { mutableStateOf(false) }
    var primerTurno by remember { mutableStateOf<Int?>(null) }

    val respuesta = estado.respuesta
    val fallo = respuesta != null && !respuesta.correcta

    suspend fun irA(x: Float, y: Float, s: Float, r: Float, ms: Int, curva: androidx.compose.animation.core.Easing) = coroutineScope {
        launch { dx.animateTo(x, tween(ms, easing = curva)) }
        launch { dy.animateTo(y, tween(ms, easing = curva)) }
        launch { escala.animateTo(s, tween(ms, easing = curva)) }
        launch { giro.animateTo(r, tween(ms, easing = curva)) }
    }

    val mazo = Area.cartaMazo - Area.centroCarta
    val pilaDominadas = Area.cartaDominadas - Area.centroCarta

    // Cada carta nueva sale del mazo (la primera aparece en la mesa).
    LaunchedEffect(estado.turno) {
        volteo.snapTo(0f)
        enMazo = false
        dominada = false
        lineas = false
        retro = false
        val r = estado.respuesta
        if (r != null) {
            // Se volvió a mostrar la pantalla con una respuesta pendiente: dejar la mesa como quedó.
            if (r.correcta) {
                volteo.snapTo(1f)
                dominada = true
            } else {
                dx.snapTo(mazo.x); dy.snapTo(mazo.y); escala.snapTo(Area.ESCALA_MONTON); giro.snapTo(-15f)
                enMazo = true
            }
            retro = true
            primerTurno = estado.turno
            return@LaunchedEffect
        }
        if (primerTurno == null) {
            primerTurno = estado.turno
            dx.snapTo(0f); dy.snapTo(0f); escala.snapTo(1f); giro.snapTo(0f)
        } else {
            dx.snapTo(mazo.x); dy.snapTo(mazo.y); escala.snapTo(Area.ESCALA_MONTON * 0.9f); giro.snapTo(-10f)
            val muelle = spring<Float>(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
            coroutineScope {
                launch { dx.animateTo(0f, muelle) }
                launch { dy.animateTo(0f, muelle) }
                launch { escala.animateTo(1f, muelle) }
                launch { giro.animateTo(0f, muelle) }
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
                // Volteo: la carta se aplana, cambia de cara y se abre de nuevo.
                delay(240)
                volteo.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
                dominada = true
                retro = true
                ocupado = false
            } else {
                // La opción se marca, la carta se levanta y sale volando al mazo.
                delay(600)
                irA(0f, 0f, 1.05f, -3f, 150, FastOutSlowInEasing)
                lineas = true
                irA(mazo.x, mazo.y, Area.ESCALA_MONTON, -15f, 550, CubicBezierEasing(0.45f, 0f, 0.55f, 1f))
                enMazo = true
                retro = true
                ocupado = false
                delay(700)
                lineas = false
            }
        }
    }

    fun avanzar() {
        val r = estado.respuesta ?: return
        if (!retro || ocupado) return
        ocupado = true
        retro = false
        escena.launch {
            if (r.correcta) {
                // La carta dominada vuela a su montón y la siguiente sale del mazo.
                irA(pilaDominadas.x, pilaDominadas.y, Area.ESCALA_MONTON, 10f, 350, FastOutLinearInEasing)
            }
            lineas = false
            onContinuar()
            ocupado = false
        }
    }

    val resuelta: EstadoTarjeta? = when {
        respuesta == null -> null
        respuesta.correcta -> if (dominada) EstadoTarjeta.DOMINADA else null
        else -> EstadoTarjeta.FALLADA
    }
    val cantMazo = estado.enMazo + if (enMazo) 1 else 0
    val cantDominadas = estado.dominadas + if (dominada) 1 else 0

    CabeceraSalon(salon, estado.puntos, estado.posicionActual, resuelta)
    Texto(
        "Carta ${estado.numero} de ${estado.total}" + if (fallo) " · saldrá de nuevo" else "",
        195f, 67.9f, 12.5f, ColoresSalon.Crema, w = 300f, h = 22f
    )

    // Montones
    MontonSalon(salon, cantMazo, 11, Modifier.en(Area.cartaMazo.x - 3f, Area.cartaMazo.y + 4f, 76f, 108f))
    EtiquetaMonton("Mazo", cantMazo, salon.acento, Modifier.en(40.1f, 353.5f, 73f, 35f))
    MontonSalon(salon, cantDominadas, 31, Modifier.en(Area.cartaDominadas.x - 3f, Area.cartaDominadas.y + 4f, 76f, 108f))
    EtiquetaMonton("Dominadas", cantDominadas, salon.acento, Modifier.en(350f, 353.5f, 73f, 35f))

    if (fallo && (lineas || enMazo)) {
        Texto("¡Al mazo!", 231f, 126.4f, 14f, ColoresSalon.Rojo, w = 120f, h = 24f, peso = FontWeight.Bold, cinzel = true)
    }
    AnimatedVisibility(lineas, enter = fadeIn(tween(120)), exit = fadeOut(tween(400))) {
        Canvas(Modifier.requiredSize(Area.ANCHO.dp, Area.ALTO.dp)) {
            val d = density
            for (k in 0 until 5) {
                val a = Offset(196.4f + k * 5.8f, 180.6f + k * 22.2f) * d
                val b = Offset(254.2f + k * 10.1f, 187.8f + k * 21.85f) * d
                drawLine(Color(0xE6F6EEDC), a, b, 2.2f * d, cap = StrokeCap.Round)
            }
        }
    }

    // Carta en juego
    Box(
        Modifier
            .en(Area.centroCarta.x, Area.centroCarta.y, Area.CARTA_ANCHO, Area.CARTA_ALTO)
            .graphicsLayer {
                translationX = dx.value * density
                translationY = dy.value * density
                rotationZ = giro.value
                val p = volteo.value
                val ancho = max(abs(cos(p * PI.toFloat())), 0.002f)
                val levante = 1f + 0.07f * sin(p * PI.toFloat())
                scaleX = escala.value * ancho * levante
                scaleY = escala.value * levante
            }
    ) {
        val p = volteo.value
        val elevacion = sin(p * PI.toFloat())
        Box(
            Modifier
                .fillMaxSize()
                .shadow((8f + 10f * elevacion).dp, FormaCarta(), clip = false)
                .graphicsLayer {
                    // se oscurece un poco al pasar de canto
                    alpha = 1f
                }
        ) {
            if (p >= 0.5f) CaraConcepto(tarjeta, salon) else CaraDescripcion(tarjeta, salon, nombreTema)
            val sombra = 0.22f * (1f - max(abs(cos(p * PI.toFloat())), 0.002f))
            if (sombra > 0.01f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = sombra), FormaCarta()))
        }
    }

    // Conceptos
    tarjeta.opciones.take(4).forEachIndexed { i, concepto ->
        TablillaOpcion(
            salon = salon,
            runa = runasOpciones[i % 4],
            texto = concepto,
            estado = estadoTablilla(i, respuesta),
            habilitada = respuesta == null && !ocupado,
            semilla = estado.turno * 7 + i,
            y = Area.yOpciones[min(i, 3)],
            onClick = { responder(i) }
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
        enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 4 },
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
    onClick: () -> Unit
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
