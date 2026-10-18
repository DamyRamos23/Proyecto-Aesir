package com.aesir.odin.ui.ragnarok

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.ReglasReto
import com.aesir.odin.domain.model.ResultadoRetoFinMundo
import com.aesir.odin.domain.model.RetoFinMundo
import com.aesir.odin.domain.model.TemaConErrores
import com.aesir.odin.ui.components.odin.LienzoDiseno
import com.aesir.odin.ui.components.odin.TextoAjustable
import com.aesir.odin.ui.components.odin.centroEn
import com.aesir.odin.ui.components.odin.pulsable

private val runasBatalla = listOf("ᚠ", "ᚢ", "ᚦ", "ᚨ", "ᚱ", "ᚲ", "ᚷ", "ᚹ", "ᚺ", "ᛁ", "ᛃ", "ᛇ", "ᛈ", "ᛉ", "ᛊ")

/** Alto que debe caber en pantalla (px del mockup, sin la barra de estado). */
private const val ALTO_UTIL = 1100f

/** Lienzo de las pantallas del Ragnarök: escena de fondo y contenido en px del mockup. */
@Composable
fun LienzoRagnarok(
    relleno: Color = RG.Fondo,
    altoUtil: Float = ALTO_UTIL,
    escena: @Composable () -> Unit,
    contenido: @Composable () -> Unit
) {
    Box(Modifier.fillMaxSize().background(relleno)) {
        LienzoDiseno(alto = RG.ALTO, altoUtil = RG.d(altoUtil - RG.ARRIBA)) {
            escena()
            Box(Modifier.fillMaxSize()) { contenido() }
        }
    }
}

/**
 * CU-08 Resolver reto de fin de mundo: el Ragnarök, con el diseño del mockup
 * (escenas animadas, mismo estilo para los cuatro mundos).
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
    onRepasar: (String) -> Unit
) {
    val ui by viewModel.ui.collectAsState()
    val batalla by viewModel.batalla.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
    val aviso by viewModel.aviso.collectAsState()
    var confirmarHuida by remember { mutableStateOf(false) }

    LaunchedEffect(temaId) { viewModel.iniciar(temaId) }

    val orden = contexto?.mundo?.orden ?: 1
    val mundo = contexto?.mundo?.nombre.orEmpty()

    /** A6.1: durante el reto, salir pide confirmación; fuera de él solo vuelve. */
    val huir: () -> Unit = { if (viewModel.enCurso) confirmarHuida = true else onVolverRoadmap() }
    BackHandler { if (confirmarHuida) confirmarHuida = false else huir() }

    Box(Modifier.fillMaxSize().background(RG.Fondo)) {
        Crossfade(targetState = ui, animationSpec = tween(350), label = "ragnarok") { estado ->
            when (estado) {
                RagnarokUi.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RG.Naranja)
                }
                is RagnarokUi.Bloqueado -> PantallaAviso(
                    orden, mundo, "El Ragnarök aún duerme", textoBloqueado(estado.pendientes), onVolverRoadmap
                )
                is RagnarokUi.Error -> PantallaAviso(orden, mundo, "No se pudo abrir el reto", estado.mensaje, onVolverRoadmap)
                is RagnarokUi.Entrada -> PantallaEntrada(orden, mundo, estado.reto, { viewModel.iniciarBatalla() }, huir)
                RagnarokUi.Batalla -> batalla?.let {
                    PantallaBatalla(
                        orden, mundo, it,
                        onConfirmar = { opciones -> viewModel.responder(opciones) },
                        onSiguiente = { viewModel.siguiente() },
                        onSalir = huir
                    )
                }
                is RagnarokUi.Resultado -> if (estado.resultado.superado) {
                    PantallaSuperado(orden, mundo, estado.resultado, onVolverRoadmap, onIrAlMundo)
                } else {
                    PantallaNoSuperado(
                        orden, mundo, estado.resultado,
                        onReintentar = { viewModel.reintentar() },
                        // A10.1: repasar la lección de un tema con errores.
                        onRepasar = onRepasar,
                        onVolver = onVolverRoadmap
                    )
                }
            }
        }
        AnimatedVisibility(confirmarHuida, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
            DialogoHuida(
                enBatalla = ui == RagnarokUi.Batalla,
                onSeguir = { confirmarHuida = false },
                // MOD-01: salir antes de terminar es abandonar el nivel.
                onAbandonar = { viewModel.abandonar(onVolverRoadmap) }
            )
        }
    }

    aviso?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { viewModel.limpiarAviso() },
            confirmButton = { TextButton({ viewModel.limpiarAviso() }) { Text("Entendido") } },
            title = { Text("No puedes reintentarlo") },
            text = { Text(mensaje) }
        )
    }
}

/** A2.1: lista los temas que faltan para despertar el Ragnarök. */
private fun textoBloqueado(pendientes: List<String>): String {
    val base = "Completa todos los temas del mundo para despertarlo."
    return if (pendientes.isEmpty()) base else base + "\n\nTe faltan: " + pendientes.joinToString(", ") + "."
}

private fun numeroEnLetra(n: Int): String {
    val palabras = listOf("cero", "una", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez")
    return palabras.getOrNull(n) ?: "$n"
}

// ---------------------------------------------------------------------------
// Entrada (paso 4: instrucciones y puntaje mínimo)
// ---------------------------------------------------------------------------

@Composable
private fun PantallaEntrada(orden: Int, mundo: String, reto: RetoFinMundo, onDesatar: () -> Unit, onSalir: () -> Unit) {
    val total = reto.preguntas.size
    val minimoAciertos = (ReglasReto.PUNTAJE_MINIMO * total + 99) / 100
    LienzoRagnarok(escena = { EscenaEntrada() }) {
        BarraRagnarok(mundo, orden, onSalir)
        Rotulo("RETO DE FIN DE MUNDO", Modifier.enRG(270f, 133.5f))
        TituloRagnarok("Ragnarök", Modifier.enRG(270f, 176f), tamano = 54f)
        TextoAjustable(
            "«Fenrir rompe sus cadenas, la serpiente emerge del mar y Surtr enciende su espada. Demuestra lo aprendido en $mundo o el mundo arderá».",
            alegreya(19f, italica = true, color = Color(0xFFE6DCCB), interlineaPx = 8f, alinear = TextAlign.Center)
                .copy(shadow = Shadow(Color.Black, Offset(0f, 0f), 4f)),
            Modifier.enRG(270f, 754f).tamRG(490f, 110f),
            minEscala = 0.8f,
            maxLineas = 4
        )
        PanelCortado(Modifier.enRG(270f, 907.5f).tamRG(496f, 171f), borde = RG.BordePanel.copy(alpha = 0.9f))
        Regla("ᛁ", "$total preguntas", "Al azar, de los ${reto.temasCubiertos} temas de $mundo", 858f)
        Separador(888f)
        Regla("ᛏ", "Puntaje mínimo: ${ReglasReto.PUNTAJE_MINIMO}", "Necesitas $minimoAciertos aciertos para superar el mundo", 911f)
        Separador(941f)
        Regla("ᚾ", "Sin retirada", "Si abandonas, el reto termina con puntaje 0", 963f)
        BotonRagnarok("DESATAR EL RAGNARÖK", Modifier.enRG(270f, 1058.5f), accion = onDesatar)
    }
}

@Composable
private fun Regla(runa: String, titulo: String, detalle: String, y: Float) {
    RomboRuna(runa, Modifier.enRG(58f, y), color = Color(0xFFC8662E), lado = 32f)
    TextoAjustable(titulo, alegreya(19f, FontWeight.Bold), Modifier.desdeRG(92f, y - 11f, 410f), minEscala = 0.8f)
    TextoAjustable(detalle, alegreya(17.5f, color = Color(0xFFC8BDAA)), Modifier.desdeRG(92f, y + 12f, 410f), minEscala = 0.75f)
}

@Composable
private fun Separador(y: Float, desde: Float = 92f, hasta: Float = 495f) {
    Box(
        Modifier
            .enRG((desde + hasta) / 2f, y)
            .width(RG.d(hasta - desde).dp)
            .size(RG.d(hasta - desde).dp, 1.dp)
            .background(Color(0xFF3A3028))
    )
}

// ---------------------------------------------------------------------------
// Batalla (pasos 5 a 8)
// ---------------------------------------------------------------------------

private enum class EstadoOpcionBatalla { NEUTRA, SELECCIONADA, CORRECTA, INCORRECTA, FALTO, APAGADA }

private fun estadoOpcion(indice: Int, elegida: Boolean, correctas: List<Int>, respondida: Boolean): EstadoOpcionBatalla {
    if (!respondida) return if (elegida) EstadoOpcionBatalla.SELECCIONADA else EstadoOpcionBatalla.NEUTRA
    val esCorrecta = indice in correctas
    return when {
        esCorrecta && elegida -> EstadoOpcionBatalla.CORRECTA
        esCorrecta -> EstadoOpcionBatalla.FALTO
        elegida -> EstadoOpcionBatalla.INCORRECTA
        else -> EstadoOpcionBatalla.APAGADA
    }
}

@Composable
private fun PantallaBatalla(
    orden: Int,
    mundo: String,
    estado: EstadoBatalla,
    onConfirmar: (List<Int>) -> Unit,
    onSiguiente: () -> Unit,
    onSalir: () -> Unit
) {
    val pregunta = estado.pregunta
    val respuesta = estado.respuesta
    var seleccion by rememberSaveable(pregunta.id) { mutableStateOf(listOf<Int>()) }
    val elegidas: Set<Int> = if (respuesta != null) estado.seleccionadas.toSet() else seleccion.toSet()

    LienzoRagnarok(escena = { FondoBatalla() }) {
        BarraRagnarok(mundo, orden, onSalir)
        Rotulo("BATALLA FINAL · " + "%02d/%02d".format(estado.numero, estado.total), Modifier.enRG(270f, 133.5f))
        RunasProgreso(estado)
        MarcadorBatalla(estado)
        TextoAjustable(
            "Tema · ${pregunta.temaNombre}",
            alegreya(17.5f, italica = true, color = Color(0xFFBDB2A0), alinear = TextAlign.Center),
            Modifier.enRG(270f, 283f).width(RG.d(480f).dp),
            minEscala = 0.8f
        )
        TextoAjustable(
            pregunta.enunciado,
            alegreya(30f, FontWeight.Medium, interlineaPx = 5f, alinear = TextAlign.Center)
                .copy(shadow = Shadow(Color.Black, Offset(0f, 1f), 6f)),
            Modifier.enRG(270f, 336f).tamRG(480f, 104f),
            minEscala = 0.6f,
            maxLineas = 3
        )
        Rotulo(
            if (pregunta.esSeleccionMultiple) "SELECCIONA TODAS LAS QUE APLIQUEN" else "SELECCIONA UNA RESPUESTA",
            Modifier.enRG(270f, 430f), tamano = 11.5f, espacio = 3.2f
        )
        pregunta.opciones.take(4).forEachIndexed { i, texto ->
            OpcionBatalla(
                texto = texto,
                multiple = pregunta.esSeleccionMultiple,
                estado = estadoOpcion(i, i in elegidas, pregunta.respuestasCorrectas, respuesta != null),
                bloqueada = respuesta != null,
                modifier = Modifier.enRG(270f, 499f + i * 100f)
            ) {
                seleccion = if (pregunta.esSeleccionMultiple) {
                    if (i in seleccion) seleccion - i else seleccion + i
                } else {
                    listOf(i)
                }
            }
        }
        AnimatedVisibility(
            respuesta != null,
            enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { 12 },
            exit = fadeOut(tween(150))
        ) {
            if (respuesta != null) {
                Box(Modifier.fillMaxSize()) {
                    Retroalimentacion(respuesta.estado, pregunta.opciones, pregunta.respuestasCorrectas, elegidas)
                }
            }
        }
        if (respuesta == null) {
            BotonRagnarok("CONFIRMAR", Modifier.enRG(270f, 1058.5f), habilitado = seleccion.isNotEmpty()) {
                onConfirmar(seleccion.sorted())
            }
        } else {
            BotonRagnarok(if (estado.esUltima) "VER RESULTADO" else "SIGUIENTE PREGUNTA", Modifier.enRG(270f, 1058.5f), accion = onSiguiente)
        }
    }
}

/** Una runa por pregunta: oro si se acertó, turquesa tachada si se falló, con aro naranja la que está en juego. */
@Composable
private fun RunasProgreso(estado: EstadoBatalla) {
    val n = estado.progreso.size.coerceAtLeast(1)
    val paso = minOf(42.8f, 400f / (n - 1).coerceAtLeast(1))
    val x0 = 270f - paso * (n - 1) / 2f
    estado.progreso.forEachIndexed { i, resultado ->
        RunaProgreso(
            runasBatalla[i % runasBatalla.size],
            resultado,
            actual = i == estado.numero - 1,
            modifier = Modifier.enRG(x0 + paso * i, 176f)
        )
    }
}

@Composable
private fun RunaProgreso(runa: String, resultado: EstadoRespuesta?, actual: Boolean, modifier: Modifier) {
    val color = when {
        resultado == EstadoRespuesta.CORRECTA -> RG.Oro
        resultado != null -> RG.Turquesa
        actual -> RG.Crema
        else -> Color(0xFF5A534A)
    }
    Box(
        modifier
            .tamRG(34f, 34f)
            .drawBehind {
                if (actual && resultado == null) {
                    drawCircle(RG.Naranja.copy(alpha = 0.14f))
                    drawCircle(RG.Naranja.copy(alpha = 0.25f), radius = size.minDimension / 2f + 3.dp.toPx(), style = Stroke(3.dp.toPx()))
                    drawCircle(RG.Naranja, style = Stroke(RG.d(2.4f).dp.toPx()))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(runa, style = TextStyle(fontSize = RG.d(20f).sp, color = color))
        if (resultado != null && resultado != EstadoRespuesta.CORRECTA) {
            Box(Modifier.size(RG.d(2f).dp, RG.d(26f).dp).rotate(35f).background(RG.Turquesa))
        }
    }
}

/** "Aciertos 4 · Errores 1" y la barra segmentada con la marca del mínimo. */
@Composable
private fun MarcadorBatalla(estado: EstadoBatalla) {
    Text("Aciertos ${estado.aciertos}", Modifier.desdeRG(26f, 228f, 110f), style = alegreya(17.5f, FontWeight.Bold, color = RG.Oro), maxLines = 1, softWrap = false)
    Text("Errores ${estado.errores}", Modifier.desdeRG(134f, 228f, 110f), style = alegreya(17.5f, FontWeight.Bold, color = RG.Turquesa), maxLines = 1, softWrap = false)
    Canvas(Modifier.enRG(384f, 229.5f).tamRG(264f, 14f)) {
        val n = estado.total.coerceAtLeast(1)
        val hueco = RG.d(3f).dp.toPx()
        val seg = (size.width - hueco * (n - 1)) / n
        val h = RG.d(8.5f).dp.toPx()
        val y = (size.height - h) / 2f
        for (i in 0 until n) {
            val color = when {
                i < estado.aciertos -> RG.Oro
                i >= n - estado.errores -> RG.Turquesa
                else -> Color(0xFF2C2724)
            }
            drawRect(color, Offset(i * (seg + hueco), y), Size(seg, h))
        }
    }
    val xMin = 252f + 264f * ReglasReto.PUNTAJE_MINIMO / 100f
    Box(Modifier.enRG(xMin, 229.5f).tamRG(2.2f, 27f).background(RG.Naranja))
    Text("${ReglasReto.PUNTAJE_MINIMO}", Modifier.enRG(xMin, 254f), style = cinzel(13f, color = RG.Naranja))
}

@Composable
private fun OpcionBatalla(
    texto: String,
    multiple: Boolean,
    estado: EstadoOpcionBatalla,
    bloqueada: Boolean,
    modifier: Modifier,
    accion: () -> Unit
) {
    val borde by animateColorAsState(
        when (estado) {
            EstadoOpcionBatalla.SELECCIONADA -> RG.Naranja
            EstadoOpcionBatalla.CORRECTA, EstadoOpcionBatalla.FALTO -> RG.Oro
            EstadoOpcionBatalla.INCORRECTA -> RG.Turquesa
            EstadoOpcionBatalla.NEUTRA -> Color(0xFF4A433C)
            EstadoOpcionBatalla.APAGADA -> Color(0xFF2C2724)
        }, tween(180), label = "borde"
    )
    val relleno by animateColorAsState(
        when (estado) {
            EstadoOpcionBatalla.SELECCIONADA -> Color(0xFF3E2A1F)
            EstadoOpcionBatalla.CORRECTA -> Color(0xFF423628)
            EstadoOpcionBatalla.INCORRECTA -> Color(0xFF12292D)
            EstadoOpcionBatalla.FALTO -> Color(0xFF231E1B)
            EstadoOpcionBatalla.NEUTRA -> Color(0xFF221E1D)
            EstadoOpcionBatalla.APAGADA -> Color(0xFF151412)
        }, tween(180), label = "relleno"
    )
    val colorTexto = if (estado == EstadoOpcionBatalla.APAGADA) Color(0xFF6F675E) else RG.Crema
    val marcada = estado == EstadoOpcionBatalla.SELECCIONADA || estado == EstadoOpcionBatalla.CORRECTA || estado == EstadoOpcionBatalla.INCORRECTA
    val delgado = estado == EstadoOpcionBatalla.NEUTRA || estado == EstadoOpcionBatalla.APAGADA

    Box(
        modifier
            .tamRG(492f, 86f)
            .drawBehind {
                val c = caminoCortado(Rect(Offset.Zero, size), RG.d(10f).dp.toPx())
                if (estado == EstadoOpcionBatalla.SELECCIONADA) {
                    drawPath(caminoCortado(Rect(Offset.Zero, size).inflate(3.dp.toPx()), RG.d(12f).dp.toPx()), RG.Naranja.copy(alpha = 0.18f))
                }
                drawPath(c, relleno.copy(alpha = 0.94f))
                drawPath(
                    c, borde,
                    style = Stroke(
                        width = if (delgado) 1.dp.toPx() else RG.d(2.6f).dp.toPx(),
                        pathEffect = if (estado == EstadoOpcionBatalla.FALTO)
                            PathEffect.dashPathEffect(floatArrayOf(RG.d(9f).dp.toPx(), RG.d(6f).dp.toPx())) else null
                    )
                )
            }
            .pulsable(!bloqueada, accion)
    ) {
        Casilla(multiple, estado, marcada, borde, Modifier.centroEnLocal(RG.d(37f), RG.d(43f)))
        TextoAjustable(
            texto,
            alegreya(21.5f, color = colorTexto, interlineaPx = 1.5f),
            Modifier.centroEnLocal(RG.d(72f + 172.5f), RG.d(43f)).tamRG(345f, 80f).wrapVertical(),
            minEscala = 0.7f,
            maxLineas = 2
        )
        if (estado == EstadoOpcionBatalla.FALTO) {
            Text("FALTÓ", Modifier.centroEnLocal(RG.d(464f), RG.d(17f)), style = cinzel(11f, espacioPx = 1.5f, color = RG.Oro))
        }
    }
}

/** centroEn dentro de un elemento con tamaño propio (dp de diseño). */
private fun Modifier.centroEnLocal(x: Float, y: Float) = this.centroEn(x, y)

/** Centra verticalmente el texto dentro de su caja. */
private fun Modifier.wrapVertical() = this.wrapContentHeight(Alignment.CenterVertically)

/** Cuadro (varias respuestas) o rombo (una sola). */
@Composable
private fun Casilla(multiple: Boolean, estado: EstadoOpcionBatalla, marcada: Boolean, color: Color, modifier: Modifier) {
    Canvas(modifier.tamRG(32f, 32f)) {
        val s = size.width
        val forma = if (multiple) {
            Path().apply { addRect(Rect(0.5f, 0.5f, s - 0.5f, s - 0.5f)) }
        } else {
            val c = Offset(s / 2f, s / 2f)
            val r = s / 2f - 0.5f
            Path().apply { moveTo(c.x, c.y - r); lineTo(c.x + r, c.y); lineTo(c.x, c.y + r); lineTo(c.x - r, c.y); close() }
        }
        val borde = when (estado) {
            EstadoOpcionBatalla.NEUTRA -> Color(0xFF8A8278)
            EstadoOpcionBatalla.APAGADA -> Color(0xFF3A3530)
            else -> color
        }
        drawPath(forma, if (marcada) color else Color(0xFF0F0D0C))
        drawPath(forma, borde, style = Stroke(RG.d(1.8f).dp.toPx()))
        if (marcada) {
            val marca = Path().apply {
                if (estado == EstadoOpcionBatalla.INCORRECTA) {
                    moveTo(s * 0.3f, s * 0.3f); lineTo(s * 0.7f, s * 0.7f)
                    moveTo(s * 0.7f, s * 0.3f); lineTo(s * 0.3f, s * 0.7f)
                } else {
                    moveTo(s * 0.28f, s * 0.52f); lineTo(s * 0.44f, s * 0.67f); lineTo(s * 0.73f, s * 0.36f)
                }
            }
            drawPath(marca, Color(0xFF1A0E06), style = Stroke(RG.d(2.6f).dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/** Retroalimentación inmediata: correcta, incompleta o incorrecta (paso 7 y A7.1). */
@Composable
private fun Retroalimentacion(estado: EstadoRespuesta, opciones: List<String>, correctas: List<Int>, elegidas: Set<Int>) {
    val rotulo = when (estado) {
        EstadoRespuesta.CORRECTA -> "RESPUESTA CORRECTA"
        EstadoRespuesta.INCOMPLETA -> "RESPUESTA INCOMPLETA"
        EstadoRespuesta.INCORRECTA -> "RESPUESTA INCORRECTA"
    }
    val color = when (estado) {
        EstadoRespuesta.CORRECTA -> RG.Verde
        EstadoRespuesta.INCOMPLETA -> RG.Turquesa
        EstadoRespuesta.INCORRECTA -> Color(0xFFE0684A)
    }
    val lista: (List<Int>) -> String = { indices -> indices.filter { it < opciones.size }.joinToString(" y ") { "«${opciones[it]}»" } }
    val titulo = when (estado) {
        EstadoRespuesta.CORRECTA -> "¡Golpe certero!"
        EstadoRespuesta.INCOMPLETA -> {
            val bien = correctas.count { it in elegidas }
            "Elegiste ${numeroEnLetra(bien)} de las ${numeroEnLetra(correctas.size)} correctas."
        }
        EstadoRespuesta.INCORRECTA -> "Ese golpe no alcanzó."
    }
    val detalle = when (estado) {
        EstadoRespuesta.CORRECTA -> "Bien peleado: sigue adelante, el mundo aún resiste."
        EstadoRespuesta.INCOMPLETA -> "Faltó: ${lista(correctas.filter { it !in elegidas })}. Este tema se anotará para repasar."
        EstadoRespuesta.INCORRECTA ->
            (if (correctas.size > 1) "Las correctas eran " else "La correcta era ") + "${lista(correctas)}. Este tema se anotará para repasar."
    }
    PanelCortado(Modifier.enRG(270f, 930f).tamRG(496f, 150f), borde = color.copy(alpha = 0.9f), cortePx = 12f, opacidad = 0.93f)
    Box(Modifier.enRG(55f, 886f).tamRG(27f, 27f).background(color, CircleShape), contentAlignment = Alignment.Center) {
        Icon(
            when (estado) {
                EstadoRespuesta.CORRECTA -> Icons.Filled.Check
                EstadoRespuesta.INCOMPLETA -> Icons.Filled.PlayArrow
                EstadoRespuesta.INCORRECTA -> Icons.Filled.Close
            },
            null, tint = Color(0xFF0B0908), modifier = Modifier.size(RG.d(17f).dp)
        )
    }
    Text(rotulo, Modifier.desdeRG(86f, 881f, 400f), style = cinzel(12.5f, espacioPx = 2.6f, color = color), maxLines = 1, softWrap = false)
    TextoAjustable(titulo, alegreya(19f, FontWeight.Bold), Modifier.desdeRG(86f, 905f, 415f), minEscala = 0.75f)
    TextoAjustable(
        detalle,
        alegreya(17.5f, color = Color(0xFFC8BDAA), interlineaPx = 3f),
        Modifier.enRG(86f + 207.5f, 920f + 38f).tamRG(415f, 76f),
        minEscala = 0.7f,
        maxLineas = 3
    )
}

// ---------------------------------------------------------------------------
// ¿Huir del Ragnarök? (A6.1)
// ---------------------------------------------------------------------------

@Composable
private fun DialogoHuida(enBatalla: Boolean, onSeguir: () -> Unit, onAbandonar: () -> Unit) {
    LienzoRagnarok(relleno = Color.Black.copy(alpha = 0.72f), escena = {
        // Bloquea los toques de la pantalla de abajo
        Box(Modifier.fillMaxSize().pulsable { })
    }) {
        Box(
            Modifier
                .enRG(270f, 574.5f)
                .tamRG(474f, 401f)
                .drawBehind {
                    val c = caminoCortado(Rect(Offset.Zero, size), RG.d(14f).dp.toPx())
                    drawPath(caminoCortado(Rect(Offset.Zero, size).inflate(8.dp.toPx()), RG.d(18f).dp.toPx()), Color(0xFFE8853A).copy(alpha = 0.06f))
                    drawPath(c, Color(0xFF110E0C))
                    drawPath(c, Color(0xFFC8702E), style = Stroke(1.dp.toPx()))
                }
        )
        RomboRuna("ᚾ", Modifier.enRG(270f, 426f), color = RG.Naranja, lado = 44f)
        Text("¿Huir del Ragnarök?", Modifier.enRG(270f, 486f), style = alegreya(29f, FontWeight.Bold), maxLines = 1, softWrap = false)
        Text(
            (if (enBatalla) "Si abandonas ahora" else "Si te retiras ahora") +
                ", el reto terminará con puntaje 0 y tendrás que enfrentarlo de nuevo desde el inicio.",
            Modifier.enRG(270f, 554f).tamRG(420f, 96f),
            style = alegreya(18.5f, color = Color(0xFFD8CEBC), interlineaPx = 6f, alinear = TextAlign.Center)
        )
        BotonRagnarok("SEGUIR LUCHANDO", Modifier.enRG(270f, 657f), accion = onSeguir)
        Box(
            Modifier
                .enRG(270f, 729f)
                .tamRG(430f, 52f)
                .drawBehind {
                    val c = caminoCortado(Rect(Offset.Zero, size), RG.d(8f).dp.toPx())
                    drawPath(c, Color(0xFF0E0C0B))
                    drawPath(c, Color(0xFF5A5048), style = Stroke(1.dp.toPx()))
                }
                .pulsable(alPulsar = onAbandonar),
            contentAlignment = Alignment.Center
        ) {
            Text("Abandonar", style = alegreya(21f))
        }
    }
}

// ---------------------------------------------------------------------------
// Resultado: el mundo renace (pasos 9 a 11, A10.2)
// ---------------------------------------------------------------------------

@Composable
private fun PantallaSuperado(
    orden: Int,
    mundo: String,
    resultado: ResultadoRetoFinMundo,
    onVolver: () -> Unit,
    onIrAlMundo: (String) -> Unit
) {
    LienzoRagnarok(escena = { EscenaRenace() }) {
        BarraRagnarok(mundo, orden, onVolver)
        Rotulo("RAGNARÖK SUPERADO", Modifier.enRG(270f, 133.5f), color = RG.Verde)
        TituloRagnarok("El mundo renace", Modifier.enRG(270f, 182f), tamano = 40f)
        Text(
            "${resultado.puntaje}",
            Modifier.enRG(270f, 630f),
            style = cinzel(104f, FontWeight.Black, color = Color(0xFFE8C27A))
                .copy(shadow = Shadow(Color(0xFFE8C27A).copy(alpha = 0.35f), Offset.Zero, 24f))
        )
        Text("de 100 puntos · mínimo ${ReglasReto.PUNTAJE_MINIMO}", Modifier.enRG(270f, 688f), style = alegreya(19.5f), maxLines = 1, softWrap = false)
        FichaResultado("Aciertos", "${resultado.aciertos}", RG.Oro, 149f, 85f, Modifier.enRG(108.5f, 762.5f))
        FichaResultado("Errores", "${resultado.errores}", RG.Turquesa, 150f, 85f, Modifier.enRG(270f, 762.5f))
        FichaResultado("Mínimo", "${ReglasReto.PUNTAJE_MINIMO}", RG.Naranja, 150f, 85f, Modifier.enRG(432f, 762.5f))
        PanelDesbloqueo(orden, mundo, resultado)
        val siguienteId = resultado.mundoDesbloqueadoId
        if (siguienteId != null) {
            BotonRagnarok("IR A " + resultado.mundoDesbloqueadoNombre.orEmpty().uppercase(), Modifier.enRG(270f, 1058.5f)) {
                onIrAlMundo(siguienteId)
            }
            EnlaceVolver(Modifier.enRG(270f, 1119f), onVolver)
        } else {
            BotonRagnarok("VOLVER AL MAPA", Modifier.enRG(270f, 1058.5f), accion = onVolver)
        }
    }
}

@Composable
private fun FichaResultado(
    etiqueta: String,
    valor: String,
    color: Color,
    ancho: Float,
    alto: Float,
    modifier: Modifier,
    arriba: Float = 25f,
    abajo: Float = 62f
) {
    Box(modifier.tamRG(ancho, alto).drawBehind { panelCortado(color.copy(alpha = 0.45f), 10f, 0.92f) }) {
        Text(etiqueta, Modifier.centroEnLocal(RG.d(ancho / 2f), RG.d(arriba)), style = alegreya(17f, color = Color(0xFFBDB2A0)), maxLines = 1, softWrap = false)
        Text(valor, Modifier.centroEnLocal(RG.d(ancho / 2f), RG.d(abajo)), style = cinzel(25f, color = color), maxLines = 1, softWrap = false)
    }
}

/** "Fundamentos · Completado  ◆◆◆◆◆▶  Requerimientos · Desbloqueado". */
@Composable
private fun PanelDesbloqueo(orden: Int, mundo: String, resultado: ResultadoRetoFinMundo) {
    val siguiente = orden + 1
    PanelCortado(Modifier.enRG(270f, 908f).tamRG(496f, 154f), borde = Color(0xFF9FD7A0).copy(alpha = 0.8f), cortePx = 12f, opacidad = 0.92f)
    val nombre = resultado.mundoDesbloqueadoNombre
    if (nombre != null) {
        MedallonMundo(orden, 158f, 885f, radio = 26f)
        Box(Modifier.enRG(173f, 867f).tamRG(21f, 21f).background(RG.Oro, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Check, null, tint = Color(0xFF1A0E06), modifier = Modifier.size(RG.d(14f).dp))
        }
        repeat(5) { i ->
            Canvas(Modifier.enRG(230f + i * 22f, 888f).tamRG(8f, 8f)) {
                val c = center
                val r = size.width / 2f
                drawPath(Path().apply { moveTo(c.x, 0f); lineTo(size.width, c.y); lineTo(c.x, size.height); lineTo(0f, c.y); close() }, RG.Oro.copy(alpha = 0.75f))
                if (r < 0f) drawCircle(Color.Transparent)
            }
        }
        Icon(Icons.Filled.PlayArrow, null, tint = RG.Oro.copy(alpha = 0.9f), modifier = Modifier.enRG(338f, 888f).size(RG.d(16f).dp))
        MedallonMundo(siguiente, 396f, 888f, radio = 26f, brilla = true)
        TextoAjustable(nombre(mundo), alegreya(19f, FontWeight.Bold, alinear = TextAlign.Center), Modifier.enRG(158f, 937f).width(RG.d(200f).dp), minEscala = 0.7f)
        Text("Completado", Modifier.enRG(158f, 962f), style = alegreya(17.5f, color = Color(0xFFBDB2A0)), maxLines = 1, softWrap = false)
        TextoAjustable(nombre, alegreya(19f, FontWeight.Bold, alinear = TextAlign.Center), Modifier.enRG(396f, 937f).width(RG.d(200f).dp), minEscala = 0.7f)
        Text("Desbloqueado", Modifier.enRG(396f, 962f), style = alegreya(17.5f, color = colorEmblemaRagnarok(siguiente)), maxLines = 1, softWrap = false)
    } else {
        MedallonMundo(orden, 270f, 876f, radio = 26f, brilla = true)
        Text(
            if (resultado.ultimoMundo) "¡Conquistaste todos los mundos!" else "$mundo completado",
            Modifier.enRG(270f, 935f), style = alegreya(20f, FontWeight.Bold), maxLines = 1, softWrap = false
        )
        Text("Los nueve reinos vuelven a florecer.", Modifier.enRG(270f, 962f), style = alegreya(17f, italica = true, color = Color(0xFFBDB2A0)), maxLines = 1, softWrap = false)
    }
}

private fun nombre(texto: String) = texto

@Composable
private fun EnlaceVolver(modifier: Modifier, accion: () -> Unit) {
    Text(
        "Volver al mapa",
        modifier.pulsable(alPulsar = accion).padding(horizontal = 12.dp, vertical = 6.dp),
        style = alegreya(20f).copy(textDecoration = TextDecoration.Underline)
    )
}

// ---------------------------------------------------------------------------
// Resultado: el crepúsculo te alcanzó (A10.1)
// ---------------------------------------------------------------------------

@Composable
private fun PantallaNoSuperado(
    orden: Int,
    mundo: String,
    resultado: ResultadoRetoFinMundo,
    onReintentar: () -> Unit,
    onRepasar: (String) -> Unit,
    onVolver: () -> Unit
) {
    val temas = resultado.temasConErrores.sortedByDescending { it.errores }.take(3)
    LienzoRagnarok(escena = { EscenaCaida() }) {
        BarraRagnarok(mundo, orden, onVolver)
        Rotulo("RAGNARÖK NO SUPERADO", Modifier.enRG(270f, 526f), color = RG.Turquesa)
        TituloRagnarok("El crepúsculo te alcanzó", Modifier.enRG(270f, 562f), tamano = 37f)
        Text("${resultado.puntaje}", Modifier.enRG(115f, 643f), style = cinzel(84f, FontWeight.Black))
        Text("de 100", Modifier.desdeRG(172f, 623f, 140f), style = alegreya(19.5f, FontWeight.Bold), maxLines = 1, softWrap = false)
        Text("necesitabas ${ReglasReto.PUNTAJE_MINIMO}", Modifier.desdeRG(172f, 648f, 150f), style = alegreya(17f, color = Color(0xFFBDB2A0)), maxLines = 1, softWrap = false)
        FichaResultado("Aciertos", "${resultado.aciertos}", RG.Oro, 85f, 74f, Modifier.enRG(369.5f, 641f), arriba = 18f, abajo = 52f)
        FichaResultado("Errores", "${resultado.errores}", RG.Turquesa, 85f, 74f, Modifier.enRG(466.5f, 641f), arriba = 18f, abajo = 52f)
        if (temas.isNotEmpty()) {
            Text(
                "TEMAS POR REFORZAR · ${resultado.temasConErrores.size}",
                Modifier.desdeRG(24f, 715f, 260f),
                style = cinzel(13f, espacioPx = 3f, color = RG.NaranjaTexto), maxLines = 1, softWrap = false
            )
            Box(Modifier.enRG(393f, 715f).size(RG.d(246f).dp, 1.dp).background(RG.Naranja.copy(alpha = 0.75f)))
        }
        temas.forEachIndexed { i, tema ->
            FilaReforzar(tema, runasBatalla[(i + 4) % runasBatalla.size], Modifier.enRG(270f, 766f + i * 78f)) { onRepasar(tema.temaId) }
        }
        BotonRagnarok("REINTENTAR EL RAGNARÖK", Modifier.enRG(270f, 1058.5f), accion = onReintentar)
        EnlaceVolver(Modifier.enRG(270f, 1119f), onVolver)
    }
}

@Composable
private fun FilaReforzar(tema: TemaConErrores, runa: String, modifier: Modifier, accion: () -> Unit) {
    Box(
        modifier
            .tamRG(496f, 66f)
            .drawBehind { panelCortado(Color(0xFF2F4A50), 10f, 0.92f) }
            .pulsable(alPulsar = accion)
    ) {
        RomboRuna(runa, Modifier.centroEnLocal(RG.d(33f), RG.d(33f)), color = Color(0xFF4F8A92), lado = 30f)
        TextoAjustable(tema.nombre, alegreya(19.5f, FontWeight.Bold), Modifier.centroEnLocal(RG.d(64f + 125f), RG.d(22f)).width(RG.d(250f).dp), minEscala = 0.75f)
        Text(
            if (tema.errores == 1) "1 error en el reto" else "${tema.errores} errores en el reto",
            Modifier.centroEnLocal(RG.d(64f + 125f), RG.d(45f)).width(RG.d(250f).dp),
            style = alegreya(16.5f, color = Color(0xFFBDB2A0)), maxLines = 1
        )
        Row(Modifier.centroEnLocal(RG.d(475f - 90f), RG.d(33f)), verticalAlignment = Alignment.CenterVertically) {
            Text("Repasar lección", style = alegreya(17.5f, FontWeight.Bold, color = RG.Oro), maxLines = 1, softWrap = false)
            Icon(Icons.Filled.KeyboardArrowRight, null, tint = RG.Oro, modifier = Modifier.size(RG.d(20f).dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Avisos (A2.1 y errores)
// ---------------------------------------------------------------------------

@Composable
private fun PantallaAviso(orden: Int, mundo: String, titulo: String, texto: String, onVolver: () -> Unit) {
    LienzoRagnarok(escena = { FondoBatalla() }) {
        BarraRagnarok(mundo, orden, onVolver)
        PanelCortado(Modifier.enRG(270f, 560f).tamRG(474f, 400f), borde = Color(0xFFC8702E), cortePx = 14f, opacidad = 0.95f)
        RomboRuna("ᚾ", Modifier.enRG(270f, 412f), color = RG.Naranja, lado = 44f)
        Text(titulo, Modifier.enRG(270f, 470f).width(RG.d(430f).dp), style = alegreya(28f, FontWeight.Bold, alinear = TextAlign.Center))
        TextoAjustable(
            texto,
            alegreya(18f, color = Color(0xFFD8CEBC), interlineaPx = 5f, alinear = TextAlign.Center),
            Modifier.enRG(270f, 590f).tamRG(420f, 200f),
            minEscala = 0.6f,
            maxLineas = 9
        )
        BotonRagnarok("VOLVER AL MAPA", Modifier.enRG(270f, 1058.5f), accion = onVolver)
    }
}
