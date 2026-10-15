package com.aesir.odin.ui.ragnarok

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.ReglasReto
import com.aesir.odin.domain.model.ResultadoRetoFinMundo
import com.aesir.odin.domain.model.RetoFinMundo
import com.aesir.odin.domain.model.TemaConErrores
import com.aesir.odin.ui.components.odin.FormaRombo
import com.aesir.odin.ui.components.odin.FuentesMockup
import com.aesir.odin.ui.components.odin.LienzoDiseno

private val runasBatalla = listOf("ᚠ", "ᚢ", "ᚦ", "ᚨ", "ᚱ", "ᚲ", "ᚷ", "ᚹ", "ᚺ", "ᛁ", "ᛃ", "ᛇ", "ᛈ", "ᛉ", "ᛊ")

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
    val huir: () -> Unit = {
        if (enCurso) {
            confirmarHuida = true
        } else {
            onVolverRoadmap()
        }
    }
    BackHandler {
        if (confirmarHuida) {
            confirmarHuida = false
        } else {
            huir()
        }
    }

    val orden = contexto?.mundo?.orden ?: 1
    val mundo = contexto?.mundo?.nombre.orEmpty()

    Box(modifier.fillMaxSize().background(RG.Fondo)) {
        Crossfade(targetState = ui, animationSpec = tween(350), label = "ragnarok") { estado ->
            when (estado) {
                RagnarokUi.Cargando -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(color = RG.Naranja, modifier = Modifier.align(Alignment.Center))
                }
                is RagnarokUi.Bloqueado -> PantallaAviso(
                    orden, mundo, "El Ragnarök aún duerme", textoBloqueado(estado.temasPendientes), onVolverRoadmap
                )
                is RagnarokUi.Error -> PantallaAviso(orden, mundo, "No se pudo abrir el reto", estado.mensaje, onVolverRoadmap)
                is RagnarokUi.Entrada -> PantallaEntrada(orden, mundo, estado.reto, viewModel::iniciarBatalla, huir)
                RagnarokUi.Batalla -> batalla?.let {
                    PantallaBatalla(orden, mundo, it, viewModel::responder, viewModel::siguiente, huir)
                }
                is RagnarokUi.Resultado -> if (estado.resultado.superado) {
                    PantallaSuperado(orden, mundo, estado.resultado, onVolverRoadmap, onIrAlMundo)
                } else {
                    PantallaNoSuperado(orden, mundo, estado.resultado, viewModel::reintentar, onRepasarLeccion, onVolverRoadmap)
                }
            }
        }

        AnimatedVisibility(confirmarHuida, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
            DialogoHuida(
                enBatalla = ui is RagnarokUi.Batalla,
                onSeguir = { confirmarHuida = false },
                onAbandonar = {
                    confirmarHuida = false
                    // MOD-01: salir antes de terminar es abandonar el nivel.
                    onSalir()
                }
            )
        }

        if (aviso != null) {
            AlertDialog(
                onDismissRequest = viewModel::limpiarAviso,
                containerColor = Color(0xFF16120F),
                titleContentColor = RG.Crema,
                textContentColor = RG.Crema,
                title = { Text("No puedes reintentarlo", fontFamily = FuentesMockup.alegreya(), fontWeight = FontWeight.Bold) },
                text = { Text(aviso.orEmpty(), fontFamily = FuentesMockup.alegreya()) },
                confirmButton = {
                    TextButton(onClick = viewModel::limpiarAviso) { Text("Entendido", color = RG.Naranja) }
                }
            )
        }
    }
}

/** A2.1: lista los temas que faltan para despertar el Ragnarök. */
private fun textoBloqueado(pendientes: List<String>): String {
    val base = "Completa todos los temas del mundo para despertarlo."
    if (pendientes.isEmpty()) return base
    return base + "\n\nTe faltan: " + pendientes.joinToString(", ") + "."
}

private fun numeroEnLetra(n: Int): String {
    val palabras = listOf("cero", "una", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez")
    return palabras.getOrNull(n) ?: n.toString()
}

// ---------------------------------------------------------------------------
// Piezas de interfaz
// ---------------------------------------------------------------------------

/** Lienzo del Ragnarök: escena de fondo + contenido, en px del mockup. */
@Composable
private fun LienzoRagnarok(escena: @Composable () -> Unit, contenido: @Composable BoxScope.() -> Unit) {
    LienzoDiseno(alto = RG.ALTO, altoUtil = RG.y(1140f)) {
        escena()
        contenido()
    }
}

/** Texto centrado en (x, y) px del mockup dentro de una caja de w × h px. */
@Composable
private fun Texto(
    texto: String,
    x: Float,
    y: Float,
    tamano: Float,
    color: Color,
    w: Float = 500f,
    h: Float = 60f,
    peso: FontWeight = FontWeight.Normal,
    cursiva: Boolean = false,
    cinzel: Boolean = false,
    espacio: Float = 0f,
    alineacion: Alignment = Alignment.Center,
    lineas: Int = 1,
    alto: TextUnit = TextUnit.Unspecified,
    subrayado: Boolean = false
) {
    Box(Modifier.enPx(x, y, w, h), contentAlignment = alineacion) {
        Text(
            texto,
            color = color,
            fontSize = RG.d(tamano).sp,
            fontWeight = peso,
            fontStyle = if (cursiva) FontStyle.Italic else FontStyle.Normal,
            fontFamily = if (cinzel) FuentesMockup.cinzel() else FuentesMockup.alegreya(),
            letterSpacing = RG.d(espacio).sp,
            textAlign = when (alineacion) {
                Alignment.CenterStart, Alignment.TopStart -> TextAlign.Start
                Alignment.CenterEnd -> TextAlign.End
                else -> TextAlign.Center
            },
            maxLines = lineas,
            overflow = TextOverflow.Ellipsis,
            lineHeight = alto,
            textDecoration = if (subrayado) TextDecoration.Underline else null
        )
    }
}

/** Título pequeño con letras espaciadas (Cinzel), p. ej. "RETO DE FIN DE MUNDO". */
@Composable
private fun Rotulo(texto: String, x: Float, y: Float, color: Color = RG.NaranjaTexto, tamano: Float = 13.5f, espacio: Float = 4f) =
    Texto(texto, x, y, tamano, color, w = 530f, h = 30f, peso = FontWeight.Bold, cinzel = true, espacio = espacio)

/** Título grande en versalitas (Cinzel): "Ragnarök", "El mundo renace". */
@Composable
private fun TituloRagnarok(texto: String, x: Float, y: Float, tamano: Float) {
    val conCinzel = FuentesMockup.hayCinzel()
    val sp = RG.d(tamano).sp
    Box(Modifier.enPx(x, y, 540f, tamano * 1.6f), contentAlignment = Alignment.Center) {
        Text(
            FuentesMockup.versalitas(texto, sp, conCinzel),
            color = RG.Crema,
            fontSize = sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FuentesMockup.cinzel(),
            maxLines = 1,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.7f), Offset(0f, 2f), 6f)
            )
        )
    }
}

/** Barra superior: volver + nombre del mundo y el medallón del mundo al centro. */
@Composable
private fun BarraRagnarok(mundo: String, orden: Int, onVolver: () -> Unit) {
    Row(
        Modifier
            .enPx(140f, 70f, 230f, 50f)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onVolver),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = RG.Crema, modifier = Modifier.size(RG.d(26f).dp))
        Text(
            mundo,
            color = RG.Crema,
            fontSize = RG.d(23f).sp,
            fontFamily = FuentesMockup.alegreya(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = RG.d(10f).dp)
        )
    }
    MedallonMundo(orden, 270f, 72f)
}

/** Panel oscuro de esquinas cortadas con borde fino (o discontinuo). */
@Composable
private fun PanelCortado(
    x: Float, y: Float, w: Float, h: Float,
    borde: Color = RG.BordePanel,
    corte: Float = 10f,
    relleno: Color = RG.Panel.copy(alpha = 0.9f),
    grosor: Float = 1f,
    discontinuo: Boolean = false
) {
    val forma = CutCornerShape(RG.d(corte).dp)
    Box(Modifier.enPx(x, y, w, h).bordeForma(forma, relleno, borde, grosor, discontinuo))
}

/** Rellena y perfila una forma (el borde puede ser discontinuo). */
private fun Modifier.bordeForma(forma: Shape, relleno: Color, borde: Color, grosor: Float, discontinuo: Boolean = false): Modifier =
    drawBehind {
        val contorno = forma.createOutline(size, layoutDirection, this)
        drawOutline(contorno, relleno)
        val efecto = if (discontinuo) PathEffect.dashPathEffect(floatArrayOf(RG.d(9f).dp.toPx(), RG.d(6f).dp.toPx())) else null
        drawOutline(contorno, borde, style = Stroke(grosor.dp.toPx(), pathEffect = efecto))
    }

/** Forma del botón principal: puntas a los lados. */
private val PuntasBoton = GenericShape { size, _ ->
    val p = size.height * 0.5f * 0.62f
    moveTo(p, 0f)
    lineTo(size.width - p, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(size.width - p, size.height)
    lineTo(p, size.height)
    lineTo(0f, size.height / 2f)
    close()
}

/** Botón principal con degradado salvia → oro → naranja. */
@Composable
private fun BotonRagnarok(texto: String, x: Float = 270f, y: Float = 1058.5f, habilitado: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier
            .enPx(x, y, 474f, 63f)
            .graphicsLayer { alpha = if (habilitado) 1f else 0.45f }
            .shadow(if (habilitado) 10.dp else 0.dp, PuntasBoton, ambientColor = RG.Naranja, spotColor = RG.Naranja)
            .drawBehind {
                val contorno = PuntasBoton.createOutline(size, layoutDirection, this)
                drawOutline(
                    contorno,
                    Brush.horizontalGradient(listOf(Color(0xFFC6D3A2), Color(0xFFE6C46A), Color(0xFFEE9A44)))
                )
            }
            .bordeForma(PuntasBoton, Color.Transparent, Color(0xBFFFF0C8), 1f)
            .clickable(enabled = habilitado, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = Color(0xFF24140A),
            fontSize = RG.d(23f).sp,
            fontWeight = FontWeight.Black,
            fontFamily = FuentesMockup.cinzel(),
            letterSpacing = RG.d(3.5f).sp,
            maxLines = 1
        )
    }
}

/** Rombo con una runa adentro. Centro y lado en px del mockup. */
@Composable
private fun RomboRuna(runa: String, x: Float, y: Float, color: Color = RG.Naranja, lado: Float = 34f) {
    Box(Modifier.enPx(x, y, lado, lado).bordeForma(FormaRombo, color.copy(alpha = 0.14f), color.copy(alpha = 0.85f), 1.2f), contentAlignment = Alignment.Center) {
        Text(runa, color = color, fontSize = RG.d(lado * 0.42f).sp)
    }
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
        Rotulo("RETO DE FIN DE MUNDO", 270f, 133.5f)
        TituloRagnarok("Ragnarök", 270f, 176f, 54f)
        Texto(
            "«Fenrir rompe sus cadenas, la serpiente emerge del mar y Surtr enciende su espada. " +
                "Demuestra lo aprendido en $mundo o el mundo arderá».",
            270f, 754f, 19f, Color(0xFFE6DCCB), w = 490f, h = 110f, cursiva = true, lineas = 4, alto = RG.d(30f).sp
        )
        PanelCortado(270f, 907.5f, 496f, 171f, borde = RG.BordePanel.copy(alpha = 0.9f))
        Regla("ᛁ", "$total preguntas", "Al azar, de los ${reto.temasCubiertos} temas de $mundo", 858f)
        Separador(888f)
        Regla("ᛏ", "Puntaje mínimo: ${ReglasReto.PUNTAJE_MINIMO}", "Necesitas $minimoAciertos aciertos para superar el mundo", 911f)
        Separador(941f)
        Regla("ᚾ", "Sin retirada", "Si abandonas, el reto termina con puntaje 0", 963f)
        BotonRagnarok("DESATAR EL RAGNARÖK", onClick = onDesatar)
    }
}

@Composable
private fun Regla(runa: String, titulo: String, detalle: String, y: Float) {
    RomboRuna(runa, 58f, y, Color(0xFFC8662E), 32f)
    Texto(titulo, 92f + 205f, y - 11f, 19f, RG.Crema, w = 410f, h = 26f, peso = FontWeight.Bold, alineacion = Alignment.CenterStart)
    Texto(detalle, 92f + 205f, y + 12f, 16.5f, RG.CremaTenue, w = 410f, h = 24f, alineacion = Alignment.CenterStart)
}

@Composable
private fun Separador(y: Float, desde: Float = 92f, hasta: Float = 495f) {
    Box(Modifier.enPx((desde + hasta) / 2f, y, hasta - desde, 1.4f).background(Color(0xFF3A3028)))
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
    var seleccion by remember(pregunta.id) { mutableStateOf(emptySet<Int>()) }
    val elegidas = if (respuesta != null) estado.seleccionadas.toSet() else seleccion

    LienzoRagnarok(escena = { FondoBatalla() }) {
        BarraRagnarok(mundo, orden, onSalir)
        Rotulo("BATALLA FINAL · %02d/%02d".format(estado.numero, estado.total), 270f, 133.5f)
        RunasProgreso(estado)
        MarcadorBatalla(estado)
        Texto("Tema · ${pregunta.temaNombre}", 270f, 283f, 17.5f, RG.GrisTexto, w = 480f, h = 28f, cursiva = true)
        Texto(
            pregunta.enunciado, 270f, 336f, 30f, RG.Crema, w = 480f, h = 104f,
            peso = FontWeight.Medium, lineas = 3, alto = RG.d(41f).sp
        )
        Rotulo(
            if (pregunta.esSeleccionMultiple) "SELECCIONA TODAS LAS QUE APLIQUEN" else "SELECCIONA UNA RESPUESTA",
            270f, 430f, tamano = 11.5f, espacio = 3.2f
        )
        pregunta.opciones.take(4).forEachIndexed { i, texto ->
            OpcionBatalla(
                texto = texto,
                multiple = pregunta.esSeleccionMultiple,
                estado = estadoOpcion(i, i in elegidas, pregunta.respuestasCorrectas, respuesta != null),
                bloqueada = respuesta != null,
                y = 499f + i * 100f,
                onClick = {
                    seleccion = if (pregunta.esSeleccionMultiple) {
                        if (i in seleccion) seleccion - i else seleccion + i
                    } else {
                        setOf(i)
                    }
                }
            )
        }
        AnimatedVisibility(
            visible = respuesta != null,
            enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 20 },
            exit = fadeOut(tween(150))
        ) {
            respuesta?.let {
                Box(Modifier.fillMaxSize()) {
                    Retroalimentacion(it.estado, pregunta.opciones, pregunta.respuestasCorrectas, elegidas)
                }
            }
        }
        if (respuesta == null) {
            BotonRagnarok("CONFIRMAR", habilitado = seleccion.isNotEmpty()) { onConfirmar(seleccion.sorted()) }
        } else {
            BotonRagnarok(if (estado.esUltima) "VER RESULTADO" else "SIGUIENTE PREGUNTA", onClick = onSiguiente)
        }
    }
}

/** Una runa por pregunta: oro si se acertó, turquesa tachada si se falló, con aro la que está en juego. */
@Composable
private fun RunasProgreso(estado: EstadoBatalla) {
    val n = estado.progreso.size.coerceAtLeast(1)
    val paso = minOf(42.8f, 400f / (n - 1).coerceAtLeast(1))
    val x0 = 270f - paso * (n - 1) / 2f
    estado.progreso.forEachIndexed { i, resultado ->
        val actual = i == estado.numero - 1
        val color = when {
            resultado == EstadoRespuesta.CORRECTA -> RG.Oro
            resultado != null -> RG.Turquesa
            actual -> RG.Crema
            else -> Color(0xFF5A534A)
        }
        Box(
            Modifier
                .enPx(x0 + paso * i, 176f, 34f, 34f)
                .drawBehind {
                    if (actual && resultado == null) {
                        drawCircle(RG.Naranja.copy(alpha = 0.14f))
                        drawCircle(RG.Naranja, size.minDimension / 2f - 1.5f, style = Stroke(RG.d(2.4f).dp.toPx()))
                    }
                    if (resultado != null && resultado != EstadoRespuesta.CORRECTA) {
                        val c = center
                        val d = Offset(5f, -11f) * (size.width / 34f)
                        drawLine(RG.Turquesa, c - d, c + d, strokeWidth = RG.d(2f).dp.toPx())
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(runasBatalla[i % runasBatalla.size], color = color, fontSize = RG.d(20f).sp)
        }
    }
}

/** "Aciertos 4 · Errores 1" y la barra segmentada con la marca del mínimo. */
@Composable
private fun MarcadorBatalla(estado: EstadoBatalla) {
    Texto("Aciertos ${estado.aciertos}", 26f + 55f, 228f, 17.5f, RG.Oro, w = 110f, h = 28f, peso = FontWeight.Bold, alineacion = Alignment.CenterStart)
    Texto("Errores ${estado.errores}", 134f + 55f, 228f, 17.5f, RG.Turquesa, w = 110f, h = 28f, peso = FontWeight.Bold, alineacion = Alignment.CenterStart)
    Canvas(Modifier.enPx(384f, 229.5f, 264f, 14f)) {
        val n = estado.total.coerceAtLeast(1)
        val hueco = size.width * 3f / 264f
        val seg = (size.width - hueco * (n - 1)) / n
        val h = size.height * 8.5f / 14f
        val y = (size.height - h) / 2f
        for (i in 0 until n) {
            val color = when {
                i < estado.aciertos -> RG.Oro
                i >= n - estado.errores -> RG.Turquesa
                else -> Color(0xFF2C2724)
            }
            drawRect(color, Offset(i * (seg + hueco), y), androidx.compose.ui.geometry.Size(seg, h))
        }
    }
    val xMin = 252f + 264f * ReglasReto.PUNTAJE_MINIMO / 100f
    Box(Modifier.enPx(xMin, 229.5f, 2.2f, 27f).background(RG.Naranja))
    Texto("${ReglasReto.PUNTAJE_MINIMO}", xMin, 254f, 13f, RG.Naranja, w = 40f, h = 20f, peso = FontWeight.Bold, cinzel = true)
}

@Composable
private fun OpcionBatalla(
    texto: String,
    multiple: Boolean,
    estado: EstadoOpcionBatalla,
    bloqueada: Boolean,
    y: Float,
    onClick: () -> Unit
) {
    val borde = when (estado) {
        EstadoOpcionBatalla.SELECCIONADA -> RG.Naranja
        EstadoOpcionBatalla.CORRECTA, EstadoOpcionBatalla.FALTO -> RG.Oro
        EstadoOpcionBatalla.INCORRECTA -> RG.Turquesa
        EstadoOpcionBatalla.NEUTRA -> Color(0xFF4A433C)
        EstadoOpcionBatalla.APAGADA -> Color(0xFF2C2724)
    }
    val relleno = when (estado) {
        EstadoOpcionBatalla.SELECCIONADA -> Color(0xFF3E2A1F)
        EstadoOpcionBatalla.CORRECTA -> Color(0xFF423628)
        EstadoOpcionBatalla.INCORRECTA -> Color(0xFF12292D)
        EstadoOpcionBatalla.FALTO -> Color(0xFF231E1B)
        EstadoOpcionBatalla.NEUTRA -> Color(0xFF221E1D)
        EstadoOpcionBatalla.APAGADA -> Color(0xFF151412)
    }
    val marcada = estado == EstadoOpcionBatalla.SELECCIONADA || estado == EstadoOpcionBatalla.CORRECTA || estado == EstadoOpcionBatalla.INCORRECTA
    val grueso = estado != EstadoOpcionBatalla.NEUTRA && estado != EstadoOpcionBatalla.APAGADA
    val forma = CutCornerShape(RG.d(10f).dp)
    Box(
        Modifier
            .enPx(270f, y, 492f, 86f)
            .shadow(if (estado == EstadoOpcionBatalla.SELECCIONADA) 8.dp else 0.dp, forma, ambientColor = RG.Naranja, spotColor = RG.Naranja)
            .bordeForma(forma, relleno.copy(alpha = 0.94f), borde, if (grueso) RG.d(2.6f) else 1f, estado == EstadoOpcionBatalla.FALTO)
            .clickable(enabled = !bloqueada, onClick = onClick)
            .semantics { contentDescription = "Opción: $texto" }
    ) {
        // Casilla (cuadro si son varias respuestas, rombo si es una)
        Canvas(
            Modifier
                .padding(start = RG.d(21f).dp)
                .size(RG.d(32f).dp)
                .align(Alignment.CenterStart)
        ) {
            val s = size.width
            val contorno = when {
                estado == EstadoOpcionBatalla.NEUTRA -> Color(0xFF8A8278)
                estado == EstadoOpcionBatalla.APAGADA -> Color(0xFF3A3530)
                else -> borde
            }
            val forma2 = if (multiple) {
                androidx.compose.ui.graphics.Path().apply { addRect(androidx.compose.ui.geometry.Rect(1f, 1f, s - 1f, s - 1f)) }
            } else {
                androidx.compose.ui.graphics.Path().apply {
                    moveTo(s / 2f, 1f); lineTo(s - 1f, s / 2f); lineTo(s / 2f, s - 1f); lineTo(1f, s / 2f); close()
                }
            }
            drawPath(forma2, if (marcada) borde else Color(0xFF0F0D0C))
            drawPath(forma2, contorno, style = Stroke(RG.d(1.8f).dp.toPx()))
            if (marcada) {
                val trazo = Stroke(RG.d(2.6f).dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val marca = androidx.compose.ui.graphics.Path().apply {
                    if (estado == EstadoOpcionBatalla.INCORRECTA) {
                        moveTo(s * 0.3f, s * 0.3f); lineTo(s * 0.7f, s * 0.7f)
                        moveTo(s * 0.7f, s * 0.3f); lineTo(s * 0.3f, s * 0.7f)
                    } else {
                        moveTo(s * 0.28f, s * 0.52f); lineTo(s * 0.44f, s * 0.67f); lineTo(s * 0.73f, s * 0.36f)
                    }
                }
                drawPath(marca, Color(0xFF1A0E06), style = trazo)
            }
        }
        Text(
            texto,
            color = if (estado == EstadoOpcionBatalla.APAGADA) Color(0xFF6F675E) else RG.Crema,
            fontSize = RG.d(21.5f).sp,
            fontFamily = FuentesMockup.alegreya(),
            lineHeight = RG.d(25f).sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = RG.d(72f).dp, end = RG.d(75f).dp)
        )
        if (estado == EstadoOpcionBatalla.FALTO) {
            Text(
                "FALTÓ",
                color = RG.Oro,
                fontSize = RG.d(11f).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FuentesMockup.cinzel(),
                letterSpacing = RG.d(1.5f).sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = RG.d(9f).dp, end = RG.d(14f).dp)
            )
        }
    }
}

/** Retroalimentación inmediata: correcta, incompleta o incorrecta (paso 7 y A7.1). */
@Composable
private fun Retroalimentacion(estado: EstadoRespuesta, opciones: List<String>, correctas: List<Int>, elegidas: Set<Int>) {
    fun lista(indices: List<Int>) = indices.filter { it < opciones.size }.joinToString(" y ") { "«${opciones[it]}»" }
    val color = when (estado) {
        EstadoRespuesta.CORRECTA -> RG.Verde
        EstadoRespuesta.INCOMPLETA -> RG.Turquesa
        EstadoRespuesta.INCORRECTA -> Color(0xFFE0684A)
    }
    val rotulo = when (estado) {
        EstadoRespuesta.CORRECTA -> "RESPUESTA CORRECTA"
        EstadoRespuesta.INCOMPLETA -> "RESPUESTA INCOMPLETA"
        EstadoRespuesta.INCORRECTA -> "RESPUESTA INCORRECTA"
    }
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
        EstadoRespuesta.INCORRECTA -> (if (correctas.size > 1) "Las correctas eran " else "La correcta era ") +
            "${lista(correctas)}. Este tema se anotará para repasar."
    }
    PanelCortado(270f, 930f, 496f, 150f, borde = color.copy(alpha = 0.9f), corte = 12f, relleno = RG.Panel.copy(alpha = 0.93f))
    Box(Modifier.enPx(55f, 886f, 27f, 27f).background(color, CircleShape), contentAlignment = Alignment.Center) {
        Icon(
            when (estado) {
                EstadoRespuesta.CORRECTA -> Icons.Filled.Check
                EstadoRespuesta.INCORRECTA -> Icons.Filled.Close
                EstadoRespuesta.INCOMPLETA -> Icons.Filled.Check
            },
            contentDescription = null,
            tint = Color(0xFF0B0908),
            modifier = Modifier.size(RG.d(17f).dp).graphicsLayer { alpha = if (estado == EstadoRespuesta.INCOMPLETA) 0f else 1f }
        )
        if (estado == EstadoRespuesta.INCOMPLETA) {
            Text("!", color = Color(0xFF0B0908), fontSize = RG.d(17f).sp, fontWeight = FontWeight.Black)
        }
    }
    Texto(rotulo, 86f + 200f, 881f, 12.5f, color, w = 400f, h = 22f, peso = FontWeight.Bold, cinzel = true, espacio = 2.6f, alineacion = Alignment.CenterStart)
    Texto(titulo, 86f + 207.5f, 905f, 19f, RG.Crema, w = 415f, h = 26f, peso = FontWeight.Bold, alineacion = Alignment.CenterStart)
    Texto(detalle, 86f + 207.5f, 958f, 17.5f, RG.CremaTenue, w = 415f, h = 76f, alineacion = Alignment.TopStart, lineas = 3, alto = RG.d(23.5f).sp)
}

// ---------------------------------------------------------------------------
// ¿Huir del Ragnarök? (A6.1)
// ---------------------------------------------------------------------------

@Composable
private fun DialogoHuida(enBatalla: Boolean, onSeguir: () -> Unit, onAbandonar: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    ) {
        LienzoDiseno(alto = RG.ALTO, altoUtil = RG.y(1140f)) {
            PanelCortado(270f, 574.5f, 474f, 401f, borde = Color(0xFFC8702E), corte = 14f, relleno = Color(0xFF110E0C))
            RomboRuna("ᚾ", 270f, 426f, RG.Naranja, 44f)
            Texto("¿Huir del Ragnarök?", 270f, 486f, 29f, RG.Crema, w = 460f, h = 44f, peso = FontWeight.Bold)
            Texto(
                (if (enBatalla) "Si abandonas ahora" else "Si te retiras ahora") +
                    ", el reto terminará con puntaje 0 y tendrás que enfrentarlo de nuevo desde el inicio.",
                270f, 554f, 18.5f, Color(0xFFD8CEBC), w = 420f, h = 96f, lineas = 3, alto = RG.d(28f).sp
            )
            BotonRagnarok("SEGUIR LUCHANDO", y = 657f, onClick = onSeguir)
            Box(
                Modifier
                    .enPx(270f, 729f, 430f, 52f)
                    .bordeForma(CutCornerShape(RG.d(8f).dp), Color(0xFF0E0C0B), Color(0xFF5A5048), 1f)
                    .clickable(onClick = onAbandonar),
                contentAlignment = Alignment.Center
            ) {
                Text("Abandonar", color = RG.Crema, fontSize = RG.d(21f).sp, fontFamily = FuentesMockup.alegreya())
            }
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
        Rotulo("RAGNARÖK SUPERADO", 270f, 133.5f, color = RG.Verde)
        TituloRagnarok("El mundo renace", 270f, 182f, 40f)
        Texto("${resultado.puntaje}", 270f, 630f, 104f, Color(0xFFE8C27A), w = 400f, h = 140f, peso = FontWeight.Black, cinzel = true)
        Texto("de 100 puntos · mínimo ${ReglasReto.PUNTAJE_MINIMO}", 270f, 688f, 19.5f, RG.Crema, w = 480f, h = 30f)
        FichaResultado("Aciertos", "${resultado.aciertos}", RG.Oro, 108.5f, 762.5f, 149f, 85f)
        FichaResultado("Errores", "${resultado.errores}", RG.Turquesa, 270f, 762.5f, 150f, 85f)
        FichaResultado("Mínimo", "${ReglasReto.PUNTAJE_MINIMO}", RG.Naranja, 432f, 762.5f, 150f, 85f)
        PanelDesbloqueo(orden, mundo, resultado)
        val siguienteId = resultado.mundoDesbloqueadoId
        if (siguienteId != null) {
            BotonRagnarok("IR A " + (resultado.mundoDesbloqueadoNombre ?: "").uppercase()) { onIrAlMundo(siguienteId) }
            EnlaceVolver(onVolver)
        } else {
            BotonRagnarok("VOLVER AL MAPA", onClick = onVolver)
        }
    }
}

@Composable
private fun FichaResultado(
    etiqueta: String, valor: String, color: Color,
    x: Float, y: Float, w: Float, h: Float,
    arriba: Float = 25f, abajo: Float = 62f
) {
    PanelCortado(x, y, w, h, borde = color.copy(alpha = 0.45f), relleno = RG.Panel.copy(alpha = 0.92f))
    val top = y - h / 2f
    Texto(etiqueta, x, top + arriba, 17f, RG.GrisTexto, w = w, h = 26f)
    Texto(valor, x, top + abajo, 25f, color, w = w, h = 34f, peso = FontWeight.Bold, cinzel = true)
}

/** "Fundamentos · Completado  ◆◆◆◆◆▶  Requerimientos · Desbloqueado". */
@Composable
private fun PanelDesbloqueo(orden: Int, mundo: String, resultado: ResultadoRetoFinMundo) {
    val siguiente = orden + 1
    PanelCortado(270f, 908f, 496f, 154f, borde = Color(0xCC9FD7A0), corte = 12f, relleno = RG.Panel.copy(alpha = 0.92f))
    val nombre = resultado.mundoDesbloqueadoNombre
    if (nombre != null) {
        MedallonMundo(orden, 158f, 885f)
        Box(Modifier.enPx(173f, 867f, 21f, 21f).background(RG.Oro, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Check, null, tint = Color(0xFF1A0E06), modifier = Modifier.size(RG.d(14f).dp))
        }
        for (i in 0 until 5) {
            Box(Modifier.enPx(230f + i * 22f, 888f, 8f, 8f).background(RG.Oro.copy(alpha = 0.75f), FormaRombo))
        }
        Icon(Icons.Filled.PlayArrow, null, tint = RG.Oro.copy(alpha = 0.9f), modifier = Modifier.enPx(338f, 888f, 16f, 16f))
        MedallonMundo(siguiente, 396f, 888f, brilla = true)
        Texto(mundo, 158f, 937f, 19f, RG.Crema, w = 200f, h = 26f, peso = FontWeight.Bold)
        Texto("Completado", 158f, 962f, 17.5f, RG.GrisTexto, w = 200f, h = 24f)
        Texto(nombre, 396f, 937f, 19f, RG.Crema, w = 200f, h = 26f, peso = FontWeight.Bold)
        Texto("Desbloqueado", 396f, 962f, 17.5f, colorEmblemaRagnarok(siguiente), w = 200f, h = 24f)
    } else {
        MedallonMundo(orden, 270f, 876f, brilla = true)
        Texto(
            if (resultado.ultimoMundo) "¡Conquistaste todos los mundos!" else "$mundo completado",
            270f, 935f, 20f, RG.Crema, w = 470f, h = 28f, peso = FontWeight.Bold
        )
        Texto("Los nueve reinos vuelven a florecer.", 270f, 962f, 17f, RG.GrisTexto, w = 470f, h = 24f, cursiva = true)
    }
}

@Composable
private fun EnlaceVolver(onVolver: () -> Unit) {
    Box(Modifier.enPx(270f, 1119f, 220f, 40f).clickable(onClick = onVolver), contentAlignment = Alignment.Center) {
        Text(
            "Volver al mapa",
            color = RG.Crema,
            fontSize = RG.d(20f).sp,
            fontFamily = FuentesMockup.alegreya(),
            textDecoration = TextDecoration.Underline
        )
    }
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
        Rotulo("RAGNARÖK NO SUPERADO", 270f, 526f, color = RG.Turquesa)
        TituloRagnarok("El crepúsculo te alcanzó", 270f, 562f, 37f)
        Texto("${resultado.puntaje}", 115f, 643f, 84f, RG.Crema, w = 160f, h = 110f, peso = FontWeight.Black, cinzel = true)
        Texto("de 100", 172f + 70f, 623f, 19.5f, RG.Crema, w = 140f, h = 26f, peso = FontWeight.Bold, alineacion = Alignment.CenterStart)
        Texto("necesitabas ${ReglasReto.PUNTAJE_MINIMO}", 172f + 75f, 648f, 17f, RG.GrisTexto, w = 150f, h = 24f, alineacion = Alignment.CenterStart)
        FichaResultado("Aciertos", "${resultado.aciertos}", RG.Oro, 369.5f, 641f, 85f, 74f, arriba = 18f, abajo = 52f)
        FichaResultado("Errores", "${resultado.errores}", RG.Turquesa, 466.5f, 641f, 85f, 74f, arriba = 18f, abajo = 52f)
        if (temas.isNotEmpty()) {
            Texto(
                "TEMAS POR REFORZAR · ${resultado.temasConErrores.size}", 24f + 130f, 715f, 13f, RG.NaranjaTexto,
                w = 260f, h = 22f, peso = FontWeight.Bold, cinzel = true, espacio = 3f, alineacion = Alignment.CenterStart
            )
            Box(Modifier.enPx(393f, 715f, 246f, 1.4f).background(RG.Naranja.copy(alpha = 0.75f)))
        }
        temas.forEachIndexed { i, tema ->
            FilaReforzar(tema, runasBatalla[(i + 4) % runasBatalla.size], 766f + i * 78f) { onRepasar(tema.temaId) }
        }
        BotonRagnarok("REINTENTAR EL RAGNARÖK", onClick = onReintentar)
        EnlaceVolver(onVolver)
    }
}

@Composable
private fun FilaReforzar(tema: TemaConErrores, runa: String, y: Float, onClick: () -> Unit) {
    val forma = CutCornerShape(RG.d(10f).dp)
    Box(
        Modifier
            .enPx(270f, y, 496f, 66f)
            .bordeForma(forma, RG.Panel.copy(alpha = 0.92f), Color(0xFF2F4A50), 1f)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Repasar ${tema.nombre}" }
    )
    RomboRuna(runa, 55f, y, Color(0xFF4F8A92), 30f)
    Texto(tema.nombre, 86f + 125f, y - 11f, 19.5f, RG.Crema, w = 250f, h = 26f, peso = FontWeight.Bold, alineacion = Alignment.CenterStart)
    Texto(
        if (tema.errores == 1) "1 error en el reto" else "${tema.errores} errores en el reto",
        86f + 125f, y + 12f, 16.5f, RG.GrisTexto, w = 250f, h = 24f, alineacion = Alignment.CenterStart
    )
    Row(
        Modifier.enPx(497f - 90f, y, 180f, 30f),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Repasar lección", color = RG.Oro, fontSize = RG.d(17.5f).sp, fontWeight = FontWeight.Bold, fontFamily = FuentesMockup.alegreya())
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = RG.Oro, modifier = Modifier.size(RG.d(22f).dp))
    }
}

// ---------------------------------------------------------------------------
// Avisos (A2.1 y errores)
// ---------------------------------------------------------------------------

@Composable
private fun PantallaAviso(orden: Int, mundo: String, titulo: String, texto: String, onVolver: () -> Unit) {
    LienzoRagnarok(escena = { FondoBatalla() }) {
        BarraRagnarok(mundo, orden, onVolver)
        PanelCortado(270f, 560f, 474f, 400f, borde = Color(0xFFC8702E), corte = 14f, relleno = RG.Panel.copy(alpha = 0.95f))
        RomboRuna("ᚾ", 270f, 412f, RG.Naranja, 44f)
        Texto(titulo, 270f, 470f, 28f, RG.Crema, w = 430f, h = 80f, peso = FontWeight.Bold, lineas = 2)
        Texto(texto, 270f, 590f, 18f, Color(0xFFD8CEBC), w = 420f, h = 200f, lineas = 8, alto = RG.d(26f).sp)
        BotonRagnarok("VOLVER AL MAPA", onClick = onVolver)
    }
}
