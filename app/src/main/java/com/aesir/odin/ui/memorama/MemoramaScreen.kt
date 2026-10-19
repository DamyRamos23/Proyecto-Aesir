package com.aesir.odin.ui.memorama

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aesir.odin.domain.model.EstadoTarjeta
import com.aesir.odin.domain.model.ResultadoMemorama
import com.aesir.odin.domain.model.TarjetaMemorama
import com.aesir.odin.domain.model.TipoTarjeta
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.FuentesMockup
import com.aesir.odin.ui.components.odin.TextoAjustable
import com.aesir.odin.ui.components.odin.centroEn
import com.aesir.odin.ui.components.odin.pulsable
import com.aesir.odin.ui.ragnarok.LienzoRagnarok
import com.aesir.odin.ui.ragnarok.MedallonVista
import com.aesir.odin.ui.ragnarok.RG
import com.aesir.odin.ui.ragnarok.alegreya
import com.aesir.odin.ui.ragnarok.cinzel
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

/**
 * CU-04: tablero del memorama según el mockup «Memorama». Cada mundo tiene
 * su propia figura de carta: 1 lápidas de madera en arco, 2 rombos de
 * piedra, 3 hexágonos de hierro y 4 escudos de obsidiana. Las cartas se
 * voltean en 3D; salir con el memorama en curso pide confirmación (A9.1 /
 * A9.2). Al encontrar todos los pares pasa al resumen.
 *
 * Las medidas están en píxeles del mockup del memorama (375 de ancho) y se
 * pasan al lienzo del Ragnarök con [MK].
 */
@Composable
fun MemoramaScreen(
    viewModel: MemoramaViewModel,
    mundoId: String,
    nivelId: String,
    onFinalizado: (ResultadoMemorama) -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tarjetas by viewModel.tarjetas.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()

    var confirmarAbandono by remember { mutableStateOf(false) }

    LaunchedEffect(mundoId, nivelId) { viewModel.iniciar(mundoId, nivelId) }
    LaunchedEffect(resultado) {
        resultado?.let {
            // Un momento para ver el tablero completo antes del resumen
            delay(1100)
            onFinalizado(it)
        }
    }

    val enCurso = tarjetas.isNotEmpty() && resultado == null
    BackHandler { if (enCurso) confirmarAbandono = true else onSalir() }

    val orden = contexto?.mundo?.orden ?: 1
    val estilo = EstilosMundo.porOrden(orden)

    FondoMundo(estilo = if (contexto != null) estilo else null, modifier = modifier, alturaPaisaje = 240.dp) {
        when {
            mensajeError != null -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(mensajeError.orEmpty(), color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 16.sp, textAlign = TextAlign.Center)
                BotonOdin("Volver al roadmap", onClick = onSalir, acento = estilo.acento, modifier = Modifier.padding(top = 20.dp).widthIn(max = 320.dp))
            }
            tarjetas.isEmpty() -> CircularProgressIndicator(color = estilo.acento, modifier = Modifier.align(Alignment.Center))
            else -> LienzoRagnarok(relleno = Color.Transparent, altoUtil = 1125f, escena = {}) {
                TableroMemoramaMundo(viewModel, orden, estilo, onAbandonar = { if (enCurso) confirmarAbandono = true else onSalir() })
            }
        }
    }

    if (confirmarAbandono) {
        AlertDialog(
            onDismissRequest = { confirmarAbandono = false },
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Hueso,
            textContentColor = OdinPaleta.Hueso,
            title = { Text("¿Abandonar el memorama?", fontFamily = FuenteTitulo) },
            text = {
                Text(
                    "Se descartará el intento actual y no obtendrás puntos. Si pagaste entrada de hidromiel, se pierde.",
                    fontFamily = FuenteTexto
                )
            },
            // A9.2: continuar desde donde estaba
            confirmButton = {
                TextButton(onClick = { confirmarAbandono = false }) {
                    Text("Continuar", color = estilo.acento, fontFamily = FuenteTitulo)
                }
            },
            // A9.1: confirmar el abandono
            dismissButton = {
                TextButton(onClick = {
                    confirmarAbandono = false
                    viewModel.abandonar(onSalir)
                }) { Text("Abandonar", color = OdinPaleta.Incorrecto, fontFamily = FuenteTitulo) }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Medidas del mockup
// ---------------------------------------------------------------------------

/** Píxeles del mockup del memorama (375 × 812) → dp de diseño del lienzo. */
private object MK {
    const val K = 540f / 375f
    fun x(px: Float) = RG.x(px * K)
    fun y(px: Float) = RG.y(px * K)
    fun d(px: Float) = RG.d(px * K)
}

private fun Modifier.enMK(x: Float, y: Float) = centroEn(MK.x(x), MK.y(y))

/** Runas para los reversos y las franjas decorativas. */
private val FUTHARK = listOf("ᚠ", "ᚢ", "ᚦ", "ᚨ", "ᚱ", "ᚲ", "ᚷ", "ᚹ", "ᚺ", "ᚾ", "ᛁ", "ᛃ", "ᛇ", "ᛈ", "ᛉ", "ᛊ", "ᛏ", "ᛒ", "ᛖ", "ᛗ", "ᛚ", "ᛜ", "ᛞ", "ᛟ")

// ---------------------------------------------------------------------------
// Figura de cada mundo
// ---------------------------------------------------------------------------

/** Figura y colocación de las cartas de un mundo. */
private enum class FiguraCarta {
    ARCO, ROMBO, HEXAGONO, ESCUDO;

    companion object {
        fun del(orden: Int) = when ((orden - 1).coerceAtLeast(0) % 4) {
            1 -> ROMBO
            2 -> HEXAGONO
            3 -> ESCUDO
            else -> ARCO
        }
    }

    /** Tamaño de una carta (px del mockup). */
    val tamano: Size
        get() = when (this) {
            ARCO -> Size(96f, 118f)
            ROMBO -> Size(110f, 142f)
            HEXAGONO -> Size(96f, 110f)
            ESCUDO -> Size(100f, 118f)
        }

    /** Centro de cada una de las 12 cartas (px del mockup). */
    val centros: List<Offset>
        get() = when (this) {
            ARCO -> listOf(254f, 389f, 524f, 659f).flatMap { y -> listOf(78f, 188f, 298f).map { Offset(it, y) } }
            ROMBO -> listOf(
                301f to listOf(128f, 248f), 379f to listOf(67f, 188f, 308f), 457f to listOf(128f, 248f),
                535f to listOf(67f, 188f, 308f), 613f to listOf(128f, 248f)
            ).flatMap { (y, xs) -> xs.map { Offset(it, y) } }
            HEXAGONO -> listOf(324f, 412f, 500f, 588f).flatMapIndexed { i, y ->
                (if (i % 2 == 0) listOf(62f, 162f, 262f) else listOf(112f, 212f, 312f)).map { Offset(it, y) }
            }
            // Columnas de los lados alineadas; la del centro, medio escalón más abajo
            ESCUDO -> (0 until 4).flatMap { f ->
                val y = 244f + f * 128.5f
                listOf(Offset(76f, y), Offset(188f, y + 43f), Offset(300f, y))
            }
        }

    /** Zona del texto del anverso (fracciones del rectángulo de la carta). */
    val zonaTexto: Rect
        get() = when (this) {
            ARCO -> Rect(0.12f, 0.26f, 0.88f, 0.88f)
            ROMBO -> Rect(0.21f, 0.3f, 0.79f, 0.7f)
            HEXAGONO -> Rect(0.1f, 0.26f, 0.9f, 0.76f)
            ESCUDO -> Rect(0.1f, 0.22f, 0.9f, 0.72f)
        }

    /** Altura (fracción) de la palomita de las cartas encontradas. */
    val alturaMarca: Float get() = if (this == ROMBO) 0.2f else 0.12f

    /** Altura (fracción) de los tres puntos del reverso. */
    val alturaPuntos: Float
        get() = when (this) {
            ARCO -> 0.89f
            ROMBO -> 0.8f
            HEXAGONO -> 0.86f
            ESCUDO -> 0.74f
        }

    /** Color del anverso según el estado. */
    fun anverso(estado: EstadoTarjeta): Color {
        val encontrada = estado == EstadoTarjeta.ENCONTRADA
        return when (this) {
            ARCO -> if (encontrada) Color(0xFFF4DCA4) else Color(0xFFF2E7CF)
            ROMBO -> if (encontrada) Color(0xFFC3ECE5) else Color(0xFFE6F3F1)
            HEXAGONO -> if (encontrada) Color(0xFFCDF0DA) else Color(0xFFE6EAF4)
            ESCUDO -> if (encontrada) Color(0xFFF7CBAE) else Color(0xFFF6E8DC)
        }
    }

    val tinta: Color
        get() = when (this) {
            ARCO -> Color(0xFF2E2213)
            ROMBO -> Color(0xFF1D2A2A)
            HEXAGONO -> Color(0xFF1B2333)
            ESCUDO -> Color(0xFF2E1A12)
        }

    /** Contorno de la carta en [r]. [escala] < 1 da el filete interior. */
    fun contorno(r: Rect, escala: Float = 1f): Path {
        val w = r.width
        val h = r.height
        fun q(x: Float, y: Float) = Offset(x * w, y * h)
        val p = Path()
        when (this) {
            ARCO -> {
                val rad = w / 2f
                val k = 0.5523f
                p.moveTo(0f, h)
                p.lineTo(0f, rad)
                p.cubicTo(0f, rad - rad * k, rad - rad * k, 0f, rad, 0f)
                p.cubicTo(rad + rad * k, 0f, w, rad - rad * k, w, rad)
                p.lineTo(w, h)
                p.close()
            }
            ROMBO -> {
                p.moveTo(q(0.5f, 0f).x, 0f); p.lineTo(w, h / 2f); p.lineTo(w / 2f, h); p.lineTo(0f, h / 2f); p.close()
            }
            HEXAGONO -> {
                p.moveTo(w / 2f, 0f); p.lineTo(w, h * 0.25f); p.lineTo(w, h * 0.75f)
                p.lineTo(w / 2f, h); p.lineTo(0f, h * 0.75f); p.lineTo(0f, h * 0.25f); p.close()
            }
            ESCUDO -> {
                p.moveTo(0f, 0f)
                p.quadraticTo(q(0.5f, 0.09f).x, q(0.5f, 0.09f).y, w, 0f)
                p.lineTo(w, h * 0.5f)
                p.quadraticTo(q(0.98f, 0.84f).x, q(0.98f, 0.84f).y, w / 2f, h)
                p.quadraticTo(q(0.02f, 0.84f).x, q(0.02f, 0.84f).y, 0f, h * 0.5f)
                p.close()
            }
        }
        // Escala alrededor del centro y se coloca en el rectángulo dado
        val m = Matrix()
        m.translate(r.center.x, r.center.y)
        m.scale(escala, escala)
        m.translate(-w / 2f, -h / 2f)
        p.transform(m)
        return p
    }
}

private class FormaCartaMemorama(private val figura: FiguraCarta) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
        Outline.Generic(figura.contorno(Rect(Offset.Zero, size)))
}

// ---------------------------------------------------------------------------
// Tablero
// ---------------------------------------------------------------------------

@Composable
private fun BoxScope.TableroMemoramaMundo(viewModel: MemoramaViewModel, orden: Int, estilo: EstiloMundo, onAbandonar: () -> Unit) {
    val tarjetas by viewModel.tarjetas.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val fallo by viewModel.fallo.collectAsState()
    val aviso by viewModel.aviso.collectAsState()
    val figura = FiguraCarta.del(orden)
    val centros = figura.centros
    val tam = figura.tamano

    EncabezadoMemoramaMundo(viewModel, orden, estilo, onAbandonar)
    FranjaRunica(estilo, 0, false, Modifier.enMK(187.5f, 163f))

    tarjetas.forEachIndexed { i, tarjeta ->
        if (i < centros.size) {
            val f = fallo
            CartaMemoramaMundo(
                figura = figura,
                estilo = estilo,
                tarjeta = tarjeta,
                runa = FUTHARK[(i * 7 + orden * 3) % FUTHARK.size],
                tiembla = if (f != null && (f.primera == i || f.segunda == i)) f.numero else 0,
                celebra = resultado != null,
                retrasoMs = i * 50L,
                onTocar = { viewModel.seleccionarTarjeta(i) },
                modifier = Modifier
                    .enMK(centros[i].x, centros[i].y)
                    .size(MK.d(tam.width).dp, MK.d(tam.height).dp)
                    // La carta volteada queda encima de sus vecinas (rombos y hexágonos se enciman)
                    .zIndex(if (tarjeta.estado == EstadoTarjeta.VISIBLE) 1f else 0f)
            )
        }
    }

    FranjaRunica(estilo, 7, true, Modifier.enMK(187.5f, 737f))

    AnimatedContent(
        targetState = aviso,
        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
        modifier = Modifier.enMK(187.5f, 768f),
        label = "aviso"
    ) { a ->
        Text(
            when (a) {
                MemoramaViewModel.Aviso.ELIGE_UNA -> "Elige una tarjeta"
                MemoramaViewModel.Aviso.ELIGE_SEGUNDA -> "Elige una segunda tarjeta"
                MemoramaViewModel.Aviso.PAREJA -> "¡Pareja encontrada!"
                MemoramaViewModel.Aviso.NO_SON_PAREJA -> "No son pareja: recuerda dónde estaban"
                MemoramaViewModel.Aviso.COMPLETO -> "¡Encontraste todos los pares!"
            },
            style = alegreya(14.5f * MK.K, italica = true, color = Color(0xFFCFC6B6)),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun EncabezadoMemoramaMundo(viewModel: MemoramaViewModel, orden: Int, estilo: EstiloMundo, onAbandonar: () -> Unit) {
    val contexto by viewModel.contexto.collectAsState()
    val pares by viewModel.paresEncontrados.collectAsState()
    val tarjetas by viewModel.tarjetas.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val saldo by viewModel.saldo.collectAsState()
    val totalPares = tarjetas.size / 2
    var segundos by remember { mutableLongStateOf(0L) }

    // Cronómetro visible: se calcula desde el inicio registrado (RF-07)
    LaunchedEffect(resultado) {
        while (resultado == null) {
            if (viewModel.tiempoInicio > 0) segundos = (System.currentTimeMillis() - viewModel.tiempoInicio) / 1_000
            delay(500)
        }
        resultado?.let { segundos = it.tiempoEmpleadoSeg }
    }

    Row(
        Modifier
            .centroEn(MK.x(20f + 65f), MK.y(48f))
            .width(MK.d(130f).dp)
            .pulsable(alPulsar = onAbandonar)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = RG.Crema, modifier = Modifier.size(MK.d(16f).dp))
        TextoAjustable(
            contexto?.mundo?.nombre ?: "Roadmap",
            alegreya(15f * MK.K),
            Modifier.padding(start = MK.d(6f).dp),
            minEscala = 0.7f
        )
    }
    MedallonVista(orden, MK.d(18f), Modifier.enMK(187.5f, 48f))
    ContadorHidromiel(
        saldo = saldo,
        metal = estilo.metal,
        modifier = Modifier.centroEn(MK.x(357f) - 30f, MK.y(48f)).graphicsLayer { scaleX = 0.9f; scaleY = 0.9f }
    )

    val conCinzel = FuentesMockup.hayCinzel()
    val estiloTitulo = cinzel(25f * MK.K, FontWeight.Bold, 0f, RG.Crema).copy(shadow = Shadow(Color.Black.copy(alpha = 0.7f), Offset(0f, 2f), 6f))
    Text(FuentesMockup.versalitas("Memorama", estiloTitulo.fontSize, conCinzel), Modifier.enMK(187.5f, 92f), style = estiloTitulo, maxLines = 1, softWrap = false)

    val tema = contexto?.tema
    TextoAjustable(
        if (tema != null) "Nivel ${tema.orden} · ${tema.nombre}" else contexto?.mundo?.nombre.orEmpty(),
        alegreya(12.5f * MK.K, color = Color(0xFFC9BEAB), alinear = TextAlign.Center),
        Modifier.enMK(187.5f, 118f).width(MK.d(320f).dp),
        minEscala = 0.7f
    )

    Text(
        "%02d:%02d".format(segundos / 60, segundos % 60),
        Modifier.enMK(44f, 137f),
        style = alegreya(12.5f * MK.K, color = Color(0xFFDCD2C0))
    )
    val avance by animateFloatAsState(pares.toFloat() / totalPares.coerceAtLeast(1), spring(dampingRatio = 0.8f, stiffness = 200f), label = "avance")
    Box(
        Modifier
            .enMK(143.5f, 137f)
            .size(MK.d(153f).dp, MK.d(5f).dp)
            .drawBehind {
                drawRect(Color.Black.copy(alpha = 0.45f))
                drawRect(estilo.acento, size = Size(size.width * avance, size.height))
                drawRect(Color(0xFF6A6058).copy(alpha = 0.7f), style = Stroke(0.8.dp.toPx()))
            }
    )
    Text(
        "$pares de $totalPares pares",
        Modifier.centroEn(MK.x(231f + 45f), MK.y(137f)).width(MK.d(90f).dp),
        style = alegreya(12.5f * MK.K, color = Color(0xFFDCD2C0)),
        maxLines = 1,
        softWrap = false
    )
}

/** Línea con un rombo al centro y una hilera tenue de runas. */
@Composable
private fun FranjaRunica(estilo: EstiloMundo, desplazamiento: Int, abajo: Boolean, modifier: Modifier) {
    val runas = (0 until 34).joinToString(" ") { FUTHARK[(it + desplazamiento) % FUTHARK.size] }
    Box(modifier.size(MK.d(340f).dp, MK.d(22f).dp), contentAlignment = Alignment.Center) {
        Box(Modifier.width(MK.d(331f).dp).clip(androidx.compose.ui.graphics.RectangleShape), contentAlignment = Alignment.Center) {
            Text(
                runas,
                Modifier.graphicsLayer { translationY = MK.d(if (abajo) -6f else 6f) * density },
                style = TextStyle(fontSize = MK.d(7f).sp, color = estilo.metal.copy(alpha = 0.32f)),
                maxLines = 1,
                softWrap = false
            )
        }
        Box(Modifier.size(MK.d(331f).dp, 0.8.dp).background(estilo.metal.copy(alpha = 0.35f)))
        Box(
            Modifier
                .size(MK.d(6f).dp)
                .graphicsLayer { rotationZ = 45f }
                .background(Color(0xFF0E0C0B))
                .border(0.8.dp, estilo.metal.copy(alpha = 0.75f))
        )
    }
}

// ---------------------------------------------------------------------------
// Carta
// ---------------------------------------------------------------------------

@Composable
private fun CartaMemoramaMundo(
    figura: FiguraCarta,
    estilo: EstiloMundo,
    tarjeta: TarjetaMemorama,
    runa: String,
    /** Número del último fallo en que participó (cambia para volver a temblar). */
    tiembla: Int,
    celebra: Boolean,
    retrasoMs: Long,
    onTocar: () -> Unit,
    modifier: Modifier
) {
    val descubierta = tarjeta.estado != EstadoTarjeta.OCULTA
    val encontrada = tarjeta.estado == EstadoTarjeta.ENCONTRADA
    val giro by animateFloatAsState(if (descubierta) 180f else 0f, spring(dampingRatio = 0.78f, stiffness = 180f), label = "giro")
    val temblor = remember { Animatable(0f) }
    val brinco = remember { Animatable(1f) }
    val aparicion = remember { Animatable(0f) }
    val interaccion = remember { MutableInteractionSource() }
    val presionada by interaccion.collectIsPressedAsState()
    val hundir by animateFloatAsState(if (presionada) 0.95f else 1f, tween(120), label = "hundir")

    LaunchedEffect(Unit) {
        delay(retrasoMs)
        aparicion.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = 160f))
    }
    LaunchedEffect(tiembla) {
        if (tiembla > 0) {
            temblor.snapTo(0f)
            temblor.animateTo(1f, tween(420, easing = LinearEasing))
            temblor.snapTo(0f)
        }
    }
    LaunchedEffect(encontrada) {
        if (encontrada) {
            brinco.animateTo(1.07f, spring(dampingRatio = 0.5f, stiffness = 800f))
            brinco.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 400f))
        }
    }
    LaunchedEffect(celebra) {
        if (celebra) {
            delay(retrasoMs)
            brinco.animateTo(1.07f, spring(dampingRatio = 0.5f, stiffness = 900f))
            brinco.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 400f))
        }
    }

    Box(
        modifier
            .graphicsLayer {
                // Sacude la carta de lado a lado mientras avanza el temblor
                val fase = temblor.value
                val resto = fase - floor(fase)
                translationX = if (fase > 0f && fase < 1f) sin(fase * PI.toFloat() * 6f) * 7f * (1f - resto * 0.4f) * density else 0f
                val escala = (0.55f + 0.45f * aparicion.value) * brinco.value * hundir
                scaleX = escala
                scaleY = escala
                alpha = aparicion.value.coerceIn(0f, 1f)
            }
            .graphicsLayer {
                rotationY = giro
                cameraDistance = 9f * density
            }
            .clickable(interaccion, indication = null, enabled = tarjeta.estado == EstadoTarjeta.OCULTA, onClick = onTocar)
    ) {
        // Cambia de cara justo a la mitad del giro, para que el texto nunca se vea en espejo
        if (giro > 90f) {
            Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) { AnversoCarta(figura, estilo, tarjeta) }
        } else {
            ReversoCarta(figura, estilo, runa)
        }
    }
}

/** Reverso: material del mundo, doble filete, la runa y tres puntos. */
@Composable
private fun ReversoCarta(figura: FiguraCarta, estilo: EstiloMundo, runa: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = Rect(Offset.Zero, size)
            val forma = figura.contorno(r)
            val interior = figura.contorno(r, if (figura == FiguraCarta.ROMBO) 0.8f else 0.86f)
            // Sombra suave
            for (k in 3 downTo 1) {
                drawPath(figura.contorno(r.translate(0f, 3.dp.toPx()), 1f + 0.02f * k), Color.Black.copy(alpha = 0.12f))
            }
            clipPath(forma) { dibujarMaterialReverso(figura, estilo, runa) }
            // Borde exterior y filete interior
            when (figura) {
                FiguraCarta.ROMBO -> {
                    drawPath(forma, Color(0xFF56605F), style = Stroke(MK.d(5f).dp.toPx()))
                    drawPath(interior, estilo.acento.copy(alpha = 0.9f), style = Stroke(1.1.dp.toPx()))
                }
                FiguraCarta.HEXAGONO -> {
                    drawPath(forma, Color(0xFF3E4A66), style = Stroke(MK.d(5f).dp.toPx()))
                    drawPath(interior, estilo.acento.copy(alpha = 0.85f), style = Stroke(1.1.dp.toPx()))
                }
                else -> {
                    drawPath(forma, estilo.metal.copy(alpha = 0.85f), style = Stroke(1.2.dp.toPx()))
                    drawPath(interior, estilo.metal.copy(alpha = 0.7f), style = Stroke(1.dp.toPx()))
                }
            }
            // Tres puntos al pie
            val yPuntos = size.height * figura.alturaPuntos
            for (k in -1..1) {
                drawCircle(estilo.metal.copy(alpha = 0.75f), 1.2.dp.toPx(), Offset(size.width / 2f + k * MK.d(4.5f).dp.toPx(), yPuntos))
            }
        }
        Text(
            runa,
            style = TextStyle(
                fontSize = MK.d(if (figura == FiguraCarta.ROMBO) 26f else 30f).sp,
                color = estilo.acento,
                shadow = Shadow(estilo.acento.copy(alpha = 0.45f), Offset.Zero, 8f)
            )
        )
    }
}

private fun DrawScope.dibujarMaterialReverso(figura: FiguraCarta, estilo: EstiloMundo, runa: String) {
    val semillaRuna = runa.firstOrNull()?.code ?: 0
    when (figura) {
        FiguraCarta.ARCO -> {
            drawRect(Brush.linearGradient(listOf(Color(0xFF3A2716), Color(0xFF24170D)), Offset.Zero, Offset(size.width, size.height)))
            // Vetas verticales de la madera
            val rnd = Random(size.width.toInt() + semillaRuna % 97)
            repeat(26) {
                val x = rnd.nextFloat() * size.width
                val veta = Path().apply {
                    moveTo(x, 0f)
                    cubicTo(x + 2f, size.height * 0.3f, x - 2f, size.height * 0.7f, x + (rnd.nextFloat() - 0.5f) * 3f, size.height)
                }
                drawPath(veta, Color(0xFF120A04).copy(alpha = 0.25f + rnd.nextFloat() * 0.35f), style = Stroke((0.4f + rnd.nextFloat() * 0.6f) * density))
            }
        }
        FiguraCarta.ROMBO -> {
            drawRect(Brush.linearGradient(listOf(Color(0xFF34393B), Color(0xFF25292A)), Offset.Zero, Offset(size.width, size.height)))
            // Grietas de la piedra
            val rnd = Random(size.height.toInt() + semillaRuna % 89)
            repeat(2) {
                var p = Offset(size.width * (0.3f + rnd.nextFloat() * 0.4f), size.height * (0.15f + rnd.nextFloat() * 0.2f))
                val grieta = Path().apply { moveTo(p.x, p.y) }
                repeat(4) {
                    p = Offset(p.x + (rnd.nextFloat() - 0.5f) * 12f * density, p.y + (8f + rnd.nextFloat() * 10f) * density)
                    grieta.lineTo(p.x, p.y)
                }
                drawPath(grieta, Color.Black.copy(alpha = 0.45f), style = Stroke(0.8f * density))
            }
        }
        FiguraCarta.HEXAGONO -> {
            drawRect(Brush.radialGradient(listOf(Color(0xFF243055), Color(0xFF171F38)), center, size.width * 0.7f))
            // Rayos finos desde el centro
            for (i in 0 until 36) {
                val a = i * PI / 18
                drawLine(
                    Color.White.copy(alpha = 0.035f), center,
                    Offset(center.x + cos(a).toFloat() * size.width, center.y + sin(a).toFloat() * size.width), 0.6f * density
                )
            }
        }
        FiguraCarta.ESCUDO -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF2E1A12), Color(0xFF1A0E09))))
            val c = Offset(size.width / 2f, size.height * 0.8f)
            drawRect(Brush.radialGradient(listOf(estilo.acento.copy(alpha = 0.12f), Color.Transparent), c, size.width * 0.6f))
        }
    }
}

/** Anverso: pergamino del color del mundo con el concepto o la definición. */
@Composable
private fun AnversoCarta(figura: FiguraCarta, estilo: EstiloMundo, tarjeta: TarjetaMemorama) {
    val encontrada = tarjeta.estado == EstadoTarjeta.ENCONTRADA
    val esConcepto = tarjeta.tipo == TipoTarjeta.CONCEPTO
    val zona = figura.zonaTexto
    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                val r = Rect(Offset.Zero, size)
                val forma = figura.contorno(r)
                if (encontrada) {
                    drawPath(figura.contorno(r.translate(0f, 2.dp.toPx()), 1.02f), Color.Black.copy(alpha = 0.3f))
                } else {
                    // Recién volteada: brilla con el color del mundo
                    for (k in 3 downTo 1) drawPath(figura.contorno(r, 1f + 0.04f * k), estilo.acento.copy(alpha = 0.12f), blendMode = BlendMode.Plus)
                }
                drawPath(forma, figura.anverso(tarjeta.estado))
                drawPath(figura.contorno(r, if (figura == FiguraCarta.ROMBO) 0.86f else 0.9f), figura.tinta.copy(alpha = 0.18f), style = Stroke(1.dp.toPx()))
                if (!encontrada) drawPath(forma, estilo.acento, style = Stroke(2.4.dp.toPx()))
            }
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = maxWidth
            val h = maxHeight
            AnimatedVisibility(
                encontrada,
                modifier = Modifier.centroEn(w.value / 2f, h.value * figura.alturaMarca),
                enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn()
            ) {
                Icon(Icons.Filled.Check, null, tint = figura.tinta, modifier = Modifier.size(MK.d(14f).dp))
            }
            TextoAjustable(
                if (esConcepto) tarjeta.contenido.uppercase() else tarjeta.contenido,
                if (esConcepto) cinzel(11f * MK.K, FontWeight.Bold, 0.6f, figura.tinta).copy(textAlign = TextAlign.Center)
                else alegreya(11f * MK.K, color = figura.tinta, alinear = TextAlign.Center),
                Modifier
                    .centroEn(w.value * zona.center.x, h.value * zona.center.y)
                    .size(w * zona.width, h * zona.height)
                    .wrapContentHeight(Alignment.CenterVertically),
                minEscala = 0.55f,
                maxLineas = 6
            )
        }
    }
}
