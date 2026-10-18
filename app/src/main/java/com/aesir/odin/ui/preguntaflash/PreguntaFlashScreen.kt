package com.aesir.odin.ui.preguntaflash

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aesir.odin.domain.model.EstadoPreguntaFlash
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.ResultadoPreguntaFlash
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.SelloRunico
import com.aesir.odin.ui.leccion.EstadoOpcion
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.ODINTheme
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * Si es false, la pregunta flash sale como pop-up (tarjeta centrada con el fondo
 * atenuado). Si es true, ocupa toda la pantalla como en el video de referencia.
 */
private const val PANTALLA_COMPLETA = false

/** Segundos finales en los que el reloj pasa a modo "¡RÁPIDO!" (rojo). */
private const val SEGUNDOS_URGENTES = 4

// Paleta del diseño de la pregunta flash
private val Oro = OdinPaleta.Brasa
private val Urgente = Color(0xFFE5552F)
private val FondoAlto = Color(0xFF1E150D)
private val FondoBajo = Color(0xFF0D0906)
private val Linea = Color(0xFF4A3D2E)
private val VerdeFondo = Color(0xF21C3320)
private val VerdeClaro = Color(0xFFBFE3B0)

// ---------------------------------------------------------------------------
// Pantalla (se monta una sola vez, encima del NavHost)
// ---------------------------------------------------------------------------

/**
 * CU-06. Observa [PreguntaFlashViewModel] y, cuando hay una pregunta activa,
 * la dibuja encima de Mundos o Roadmap.
 */
@Composable
fun PreguntaFlashScreen(viewModel: PreguntaFlashViewModel) {
    val pregunta by viewModel.pregunta.collectAsState()
    val seleccionadas by viewModel.seleccionadas.collectAsState()
    val segundos by viewModel.segundosRestantes.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val hidromiel by viewModel.hidromiel.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
    val racha by viewModel.racha.collectAsState()

    val actual = pregunta ?: return
    val estilo = EstilosMundo.porOrden(contexto?.mundo?.orden ?: 1)

    PreguntaFlashDialogo(
        pregunta = actual,
        estilo = estilo,
        runa = estilo.runaDeTema(contexto?.tema?.orden ?: 1),
        nombreTema = contexto?.tema?.nombre.orEmpty(),
        seleccionadas = seleccionadas.toSet(),
        segundosRestantes = segundos,
        resultado = resultado,
        puntosTotales = hidromiel,
        racha = racha,
        onAlternarOpcion = viewModel::alternarOpcion,
        onConfirmar = viewModel::confirmar,
        onContinuar = viewModel::cerrar,
        limiteSegundos = PreguntaFlashViewModel.TIEMPO_LIMITE_S
    )
}

/**
 * Diálogo de la pregunta flash: interrumpe la pantalla actual (Mundos o Roadmap),
 * no se puede cerrar con atrás ni tocando fuera, y al continuar el alumno regresa
 * a donde estaba (RF-77, RF-81).
 */
@Composable
fun PreguntaFlashDialogo(
    pregunta: PreguntaFlash,
    estilo: EstiloMundo,
    runa: String,
    nombreTema: String,
    seleccionadas: Set<Int>,
    segundosRestantes: Int,
    resultado: ResultadoPreguntaFlash?,
    puntosTotales: Int,
    racha: Int = 0,
    onAlternarOpcion: (Int) -> Unit,
    onConfirmar: () -> Unit,
    onContinuar: () -> Unit,
    limiteSegundos: Int = 10
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        ContenidoPreguntaFlash(
            pregunta = pregunta,
            estilo = estilo,
            runa = runa,
            nombreTema = nombreTema,
            seleccionadas = seleccionadas,
            segundosRestantes = segundosRestantes,
            resultado = resultado,
            puntosTotales = puntosTotales,
            racha = racha,
            onAlternarOpcion = onAlternarOpcion,
            onConfirmar = onConfirmar,
            onContinuar = onContinuar,
            limiteSegundos = limiteSegundos,
            pantallaCompleta = PANTALLA_COMPLETA
        )
    }
}

@Composable
fun ContenidoPreguntaFlash(
    pregunta: PreguntaFlash,
    estilo: EstiloMundo,
    runa: String,
    nombreTema: String,
    seleccionadas: Set<Int>,
    segundosRestantes: Int,
    resultado: ResultadoPreguntaFlash?,
    puntosTotales: Int,
    racha: Int = 0,
    onAlternarOpcion: (Int) -> Unit,
    onConfirmar: () -> Unit,
    onContinuar: () -> Unit,
    limiteSegundos: Int = 10,
    pantallaCompleta: Boolean = false
) {
    // Mismo mecanismo que la lección: las opciones se barajan una vez por pregunta
    // y la selección y las correctas siguen usando los índices ORIGINALES.
    val permutacion = remember(pregunta.id) { pregunta.opciones.indices.shuffled() }
    val respondido = resultado != null
    val urgente = !respondido && segundosRestantes <= SEGUNDOS_URGENTES
    val acento by animateColorAsState(if (urgente) Urgente else Oro, tween(400), label = "acentoFlash")

    // Resplandor rojo que late en los últimos segundos
    val latido = rememberInfiniteTransition(label = "latidoFlash")
    val pulso by latido.animateFloat(
        initialValue = .35f, targetValue = .7f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulsoFlash"
    )
    val intensidadRoja by animateFloatAsState(if (urgente) 1f else 0f, tween(500), label = "rojoFlash")

    val forma = RoundedCornerShape(if (pantallaCompleta) 0.dp else 12.dp)
    Box(
        Modifier
            .then(if (pantallaCompleta) Modifier.fillMaxSize() else Modifier.fillMaxWidth().padding(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .then(
                    if (pantallaCompleta) Modifier.fillMaxSize()
                    else Modifier.widthIn(max = 440.dp).heightIn(max = 780.dp).fillMaxWidth()
                )
                .clip(forma)
                .background(Brush.verticalGradient(listOf(FondoAlto, FondoBajo)))
                .drawBehind {
                    if (intensidadRoja > 0f) {
                        drawRect(
                            Brush.radialGradient(
                                listOf(Urgente.copy(alpha = .55f * pulso * intensidadRoja), Color.Transparent),
                                center = Offset(size.width / 2f, size.height * .22f),
                                radius = size.maxDimension * .75f
                            )
                        )
                    }
                }
                .then(if (pantallaCompleta) Modifier else Modifier.border(1.dp, estilo.metal.copy(alpha = .6f), forma))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            CabeceraFlash(estilo, runa, nombreTema, puntosTotales, racha, acento)

            RelojRunico(
                segundos = segundosRestantes,
                limite = limiteSegundos,
                runas = estilo.runas,
                acento = acento,
                urgente = urgente,
                detenido = respondido,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
            )

            // Panel de la pregunta; el banner de resultado se monta encima de su borde superior
            Box(Modifier.weight(1f, fill = false).padding(top = 4.dp)) {
                PanelPregunta(
                    pregunta = pregunta,
                    permutacion = permutacion,
                    seleccionadas = seleccionadas,
                    resultado = resultado,
                    runasPlaca = estilo.runas.take(7).joinToString(""),
                    metal = estilo.metal,
                    acento = acento,
                    onAlternarOpcion = onAlternarOpcion
                )
                val aparicion by animateFloatAsState(
                    if (resultado != null) 1f else 0f, tween(300, easing = FastOutSlowInEasing), label = "bannerFlash"
                )
                if (resultado != null) {
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-6).dp)
                            .graphicsLayer {
                                alpha = aparicion
                                scaleX = .85f + .15f * aparicion
                                scaleY = .85f + .15f * aparicion
                            }
                    ) { BannerResultado(resultado, racha) }
                }
            }

            Spacer(Modifier.height(14.dp))
            BotonOdin(
                texto = if (respondido) "Siguiente" else "Confirmar",
                onClick = if (respondido) onContinuar else onConfirmar,
                acento = acento,
                habilitado = respondido || (seleccionadas.isNotEmpty() && segundosRestantes > 0),
                iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth().height(58.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Piezas
// ---------------------------------------------------------------------------

@Composable
private fun CabeceraFlash(
    estilo: EstiloMundo,
    runa: String,
    nombreTema: String,
    puntosTotales: Int,
    racha: Int,
    acento: Color
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SelloRunico(runa = runa, estilo = estilo, tamano = 40.dp, resplandor = false)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "PREGUNTA FLASH",
                color = acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Text(
                nombreTema,
                color = OdinPaleta.Hueso,
                fontFamily = FuenteTexto,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.semantics { contentDescription = "Racha $racha, hidromiel: $puntosTotales" }
        ) {
            Text(
                if (racha > 0) "RACHA ×$racha · HIDROMIEL" else "HIDROMIEL",
                color = OdinPaleta.HuesoTenue,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp
            )
            Text(
                "$puntosTotales",
                color = Oro,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}

/**
 * Reloj circular: un anillo de runas que se apagan una a una, un arco que se
 * vacía y los segundos al centro. En los últimos segundos se vuelve rojo y late;
 * al responder se congela ("DETENIDO").
 */
@Composable
private fun RelojRunico(
    segundos: Int,
    limite: Int,
    runas: List<String>,
    acento: Color,
    urgente: Boolean,
    detenido: Boolean,
    modifier: Modifier = Modifier
) {
    val fraccion by animateFloatAsState(
        targetValue = segundos.coerceIn(0, limite) / limite.toFloat(),
        animationSpec = tween(durationMillis = 950, easing = LinearEasing),
        label = "fraccionReloj"
    )
    val latido = rememberInfiniteTransition(label = "latidoReloj")
    val escala by latido.animateFloat(
        initialValue = 1f, targetValue = if (urgente) 1.06f else 1f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "escalaReloj"
    )
    val medidor = rememberTextMeasurer()
    val anillo = remember(runas) {
        val base = runas.ifEmpty { listOf("ᚠ") }
        List(TOTAL_RUNAS) { base[it % base.size] }.map {
            medidor.measure(it, TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold))
        }
    }
    val etiqueta = when {
        detenido -> "DETENIDO"
        urgente -> "¡RÁPIDO!"
        else -> "SEGUNDOS"
    }

    Box(
        modifier
            .size(150.dp)
            .scale(escala)
            .semantics { contentDescription = "Tiempo restante: $segundos segundos" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val radioRunas = r * .86f
            val encendidas = ceil(TOTAL_RUNAS * fraccion).toInt()
            anillo.forEachIndexed { i, texto ->
                val angulo = Math.toRadians(-90.0 + i * 360.0 / TOTAL_RUNAS)
                val c = Offset(center.x + radioRunas * cos(angulo).toFloat(), center.y + radioRunas * sin(angulo).toFloat())
                drawText(
                    texto,
                    color = acento,
                    alpha = if (i < encendidas) 1f else .1f,
                    topLeft = c - Offset(texto.size.width / 2f, texto.size.height / 2f)
                )
            }
            // Disco central con brillo
            val rDisco = r * .62f
            drawCircle(
                Brush.radialGradient(listOf(acento.copy(alpha = .28f), Color.Transparent), radius = rDisco * 1.5f),
                rDisco * 1.5f
            )
            drawCircle(Color(0xFF120D09), rDisco)
            // Arco de tiempo
            val grosor = 5.dp.toPx()
            val rArco = rDisco + grosor / 2f
            drawCircle(acento.copy(alpha = .15f), rArco, style = Stroke(grosor))
            drawArc(
                color = acento,
                startAngle = -90f,
                sweepAngle = 360f * fraccion,
                useCenter = false,
                topLeft = center - Offset(rArco, rArco),
                size = Size(rArco * 2, rArco * 2),
                style = Stroke(grosor, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "%02d".format(segundos.coerceAtLeast(0)),
                color = acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 44.sp,
                lineHeight = 44.sp
            )
            Text(
                etiqueta,
                color = if (urgente) acento else OdinPaleta.HuesoTenue,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

private const val TOTAL_RUNAS = 24

@Composable
private fun PanelPregunta(
    pregunta: PreguntaFlash,
    permutacion: List<Int>,
    seleccionadas: Set<Int>,
    resultado: ResultadoPreguntaFlash?,
    runasPlaca: String,
    metal: Color,
    acento: Color,
    onAlternarOpcion: (Int) -> Unit
) {
    val forma = RoundedCornerShape(8.dp)
    val respondido = resultado != null
    val correctas = resultado?.opcionesCorrectas.orEmpty()

    Box(Modifier.padding(top = 14.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(forma)
                .background(Color(0xE6120D09))
                .border(1.dp, metal.copy(alpha = .7f), forma)
                .verticalScroll(rememberScrollState())
                .padding(start = 12.dp, end = 12.dp, top = 26.dp, bottom = 14.dp)
        ) {
            Text(
                pregunta.enunciado,
                color = OdinPaleta.Hueso,
                fontFamily = FuenteTexto,
                fontSize = 20.sp,
                lineHeight = 27.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
            )
            if (pregunta.esSeleccionMultiple) {
                Text(
                    "Selecciona todas las respuestas correctas",
                    color = acento,
                    fontFamily = FuenteTexto,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
            DivisorCuadro(Modifier.padding(vertical = 12.dp))

            permutacion.forEachIndexed { posVisual, original ->
                val estado = when {
                    !respondido -> EstadoOpcion.NEUTRA
                    original in correctas && original in seleccionadas -> EstadoOpcion.CORRECTA
                    original in correctas -> EstadoOpcion.FALTANTE
                    original in seleccionadas -> EstadoOpcion.INCORRECTA
                    else -> EstadoOpcion.NEUTRA
                }
                OpcionFlash(
                    letra = ('A' + posVisual).toString(),
                    texto = pregunta.opciones[original],
                    seleccionada = original in seleccionadas,
                    estado = estado,
                    respondido = respondido,
                    acento = acento,
                    onClick = { onAlternarOpcion(original) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
        // Placa de runas sobre el borde superior del panel
        Text(
            runasPlaca,
            color = Oro,
            fontSize = 12.sp,
            letterSpacing = 4.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF15100B))
                .border(1.dp, metal, RoundedCornerShape(3.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        )
    }
}

/** Línea delgada con un cuadrito al centro. */
@Composable
private fun DivisorCuadro(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(8.dp)) {
        val y = size.height / 2f
        val lado = 7.dp.toPx()
        val color = Linea
        drawLine(color, Offset(0f, y), Offset(size.width / 2f - lado, y), 1.dp.toPx())
        drawLine(color, Offset(size.width / 2f + lado, y), Offset(size.width, y), 1.dp.toPx())
        drawRect(color, Offset(size.width / 2f - lado / 2f, y - lado / 2f), Size(lado, lado), style = Stroke(1.dp.toPx()))
    }
}

@Composable
private fun OpcionFlash(
    letra: String,
    texto: String,
    seleccionada: Boolean,
    estado: EstadoOpcion,
    respondido: Boolean,
    acento: Color,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(6.dp)
    val formaLetra = RoundedCornerShape(3.dp)
    val (color, marca) = when (estado) {
        EstadoOpcion.CORRECTA -> OdinPaleta.Correcto to "✓"
        EstadoOpcion.FALTANTE -> OdinPaleta.Correcto to "✓"
        EstadoOpcion.INCORRECTA -> OdinPaleta.Incorrecto to "✗"
        EstadoOpcion.NEUTRA -> (if (seleccionada && !respondido) acento else null) to null
    }
    val fondo = when (estado) {
        EstadoOpcion.CORRECTA -> VerdeFondo
        EstadoOpcion.FALTANTE -> Color(0x661C3320)
        EstadoOpcion.INCORRECTA -> Color(0x993A1510)
        EstadoOpcion.NEUTRA -> if (color != null) acento.copy(alpha = .16f) else Color(0x80080605)
    }
    val rellenarLetra = estado == EstadoOpcion.CORRECTA || estado == EstadoOpcion.INCORRECTA ||
        (estado == EstadoOpcion.NEUTRA && color != null)
    val atenuada = respondido && estado == EstadoOpcion.NEUTRA

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (atenuada) .45f else 1f)
            .clip(forma)
            .background(fondo)
            .border(if (color != null) 1.5.dp else 1.dp, color ?: Linea, forma)
            .clickable(enabled = !respondido, role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(formaLetra)
                .background(if (rellenarLetra) (color ?: acento).copy(alpha = if (estado == EstadoOpcion.NEUTRA) 1f else .75f) else Color(0xFF0E0A07))
                .border(1.dp, if (rellenarLetra) Color.Transparent else OdinPaleta.HuesoTenue.copy(alpha = .6f), formaLetra),
            contentAlignment = Alignment.Center
        ) {
            Text(
                letra,
                color = if (rellenarLetra) OdinPaleta.Hollin else OdinPaleta.Hueso,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            texto,
            color = OdinPaleta.Hueso,
            fontFamily = FuenteTexto,
            fontSize = 17.sp,
            lineHeight = 21.sp,
            modifier = Modifier.weight(1f)
        )
        if (marca != null) {
            Text(marca, color = color ?: OdinPaleta.Hueso, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 6.dp))
        }
    }
}

/** Banner "¡VICTORIA!" (o el resultado que toque) que aparece sobre el panel. */
@Composable
private fun BannerResultado(resultado: ResultadoPreguntaFlash, racha: Int) {
    val (titulo, color, fondo) = when (resultado.estado) {
        EstadoPreguntaFlash.CORRECTA -> Triple("¡VICTORIA!", VerdeClaro, VerdeFondo)
        EstadoPreguntaFlash.INCOMPLETA -> Triple("¡CASI!", OdinPaleta.Incompleto, Color(0xF23A2A0E))
        EstadoPreguntaFlash.INCORRECTA -> Triple("DERROTA", OdinPaleta.Incorrecto, Color(0xF23A1510))
        EstadoPreguntaFlash.SIN_RESPUESTA -> Triple("¡SE ACABÓ EL TIEMPO!", OdinPaleta.Incorrecto, Color(0xF23A1510))
    }
    val puntos = when {
        resultado.puntos > 0 -> "+${resultado.puntos} de hidromiel"
        resultado.puntos < 0 -> "−${-resultado.puntos} de hidromiel"
        else -> "Tu hidromiel no cambió"
    }
    val detalle = when (resultado.estado) {
        EstadoPreguntaFlash.CORRECTA -> if (racha > 1) "$puntos · racha ×$racha" else puntos
        EstadoPreguntaFlash.INCOMPLETA -> "$puntos · te faltó alguna respuesta"
        EstadoPreguntaFlash.INCORRECTA -> "$puntos · la correcta está en verde"
        EstadoPreguntaFlash.SIN_RESPUESTA -> "$puntos · la correcta está en verde"
    }
    val forma = RoundedCornerShape(8.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(fondo)
            .border(1.5.dp, color, forma)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(titulo, color = color, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 1.sp)
        Text(detalle, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

// ---------------------------------------------------------------------------
// Vistas previas
// ---------------------------------------------------------------------------

private val preguntaUnica = PreguntaFlash(
    id = "pf1",
    enunciado = "¿Qué relación UML se usa para reutilizar un caso de uso común en otros?",
    temaId = "tema_cu_2",
    opciones = listOf("«include»", "«extend»", "Generalización", "Asociación"),
    respuestasCorrectas = listOf(0)
)

private val preguntaMultiple = PreguntaFlash(
    id = "pf2",
    enunciado = "¿Cuáles son modelos tradicionales de desarrollo de software?",
    temaId = "tema_mt_1",
    opciones = listOf("Cascada", "Espiral", "Modelo en V", "Diagrama de clases"),
    respuestasCorrectas = listOf(0, 1, 2)
)

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 852)
@Composable
private fun PreguntaFlashEnCursoPreview() {
    ODINTheme(darkTheme = true, dynamicColor = false) {
        ContenidoPreguntaFlash(
            pregunta = preguntaUnica, estilo = EstilosMundo.Piedra, runa = "ᚾ", nombreTema = "Casos de uso",
            seleccionadas = setOf(1), segundosRestantes = 7, resultado = null, puntosTotales = 120, racha = 3,
            onAlternarOpcion = {}, onConfirmar = {}, onContinuar = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 852)
@Composable
private fun PreguntaFlashUrgentePreview() {
    ODINTheme(darkTheme = true, dynamicColor = false) {
        ContenidoPreguntaFlash(
            pregunta = preguntaUnica, estilo = EstilosMundo.Piedra, runa = "ᚾ", nombreTema = "Casos de uso",
            seleccionadas = setOf(0), segundosRestantes = 3, resultado = null, puntosTotales = 120, racha = 3,
            onAlternarOpcion = {}, onConfirmar = {}, onContinuar = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 852)
@Composable
private fun PreguntaFlashVictoriaPreview() {
    ODINTheme(darkTheme = true, dynamicColor = false) {
        ContenidoPreguntaFlash(
            pregunta = preguntaUnica, estilo = EstilosMundo.Piedra, runa = "ᚾ", nombreTema = "Casos de uso",
            seleccionadas = setOf(0), segundosRestantes = 2,
            resultado = ResultadoPreguntaFlash(EstadoPreguntaFlash.CORRECTA, listOf(0), puntos = 5, totalPuntos = 125),
            puntosTotales = 125, racha = 4,
            onAlternarOpcion = {}, onConfirmar = {}, onContinuar = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 852)
@Composable
private fun PreguntaFlashIncompletaPreview() {
    ODINTheme(darkTheme = true, dynamicColor = false) {
        ContenidoPreguntaFlash(
            pregunta = preguntaMultiple, estilo = EstilosMundo.Madera, runa = "ᚠ", nombreTema = "Modelo en Cascada",
            seleccionadas = setOf(0), segundosRestantes = 5,
            resultado = ResultadoPreguntaFlash(EstadoPreguntaFlash.INCOMPLETA, listOf(0, 1, 2), puntos = 0, totalPuntos = 120),
            puntosTotales = 120,
            onAlternarOpcion = {}, onConfirmar = {}, onContinuar = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 852)
@Composable
private fun PreguntaFlashTiempoAgotadoPreview() {
    ODINTheme(darkTheme = true, dynamicColor = false) {
        ContenidoPreguntaFlash(
            pregunta = preguntaUnica, estilo = EstilosMundo.Obsidiana, runa = "ᚼ", nombreTema = "Casos de uso",
            seleccionadas = emptySet(), segundosRestantes = 0,
            resultado = ResultadoPreguntaFlash(EstadoPreguntaFlash.SIN_RESPUESTA, listOf(0), puntos = -2, totalPuntos = 118),
            puntosTotales = 118,
            onAlternarOpcion = {}, onConfirmar = {}, onContinuar = {}
        )
    }
}
