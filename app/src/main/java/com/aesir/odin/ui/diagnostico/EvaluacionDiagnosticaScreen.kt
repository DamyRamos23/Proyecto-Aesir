package com.aesir.odin.ui.diagnostico

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.ui.components.odin.FuentesMockup
import com.aesir.odin.ui.components.odin.TextoAjustable
import com.aesir.odin.ui.components.odin.centroEn
import com.aesir.odin.ui.components.odin.pulsable
import com.aesir.odin.ui.ragnarok.BarraRagnarok
import com.aesir.odin.ui.ragnarok.LienzoRagnarok
import com.aesir.odin.ui.ragnarok.RG
import com.aesir.odin.ui.ragnarok.RomboRuna
import com.aesir.odin.ui.ragnarok.alegreya
import com.aesir.odin.ui.ragnarok.caminoCortado
import com.aesir.odin.ui.ragnarok.cinzel
import com.aesir.odin.ui.theme.EstilosMundo

/**
 * CU-10: evaluación diagnóstica de un mundo según el mockup «El pozo de
 * Mímir». Es una sección aparte, como el Ragnarök, con tres pantallas:
 *
 * 1. El llamado de Mímir: el pozo animado, las reglas y «Omitir evaluación».
 * 2. Prueba de sabiduría: una pregunta a la vez; al revelar, el pozo dice si
 *    el eco coincide y la runa de esa pregunta se ilumina.
 * 3. Lo que el pozo ha reconocido ([PantallaRevelacion]).
 *
 * Salir antes de terminar descarta el avance (A6.1); omitir pide confirmación
 * y lleva al roadmap (A1.1). Con `viewModel.repaso` es el repaso del mundo.
 */
@Composable
fun EvaluacionDiagnosticaScreen(
    viewModel: EvaluacionDiagnosticaViewModel,
    mundoId: String,
    /** Tras registrar el diagnóstico (u omitirlo) se pasa al mundo. */
    onIrAlMundo: () -> Unit,
    /** Regresa sin registrar nada (A6.1) o, en el repaso, al roadmap. */
    onVolver: () -> Unit
) {
    val e by viewModel.estado.collectAsState()
    var confirmarSalida by remember { mutableStateOf(false) }

    LaunchedEffect(mundoId) { viewModel.iniciar(mundoId) }
    // A1.1: comienza desde el primer nivel disponible
    LaunchedEffect(e.omitida) { if (e.omitida) onIrAlMundo() }

    val orden = e.mundo?.orden ?: 1
    val acento = EstilosMundo.porOrden(orden).acento
    val nombreMundo = e.mundo?.nombre ?: "Mundos"

    /** A6.1: regresa sin registrar nada. */
    val salir: () -> Unit = {
        viewModel.abandonar()
        onVolver()
    }
    /** Tras registrar el diagnóstico se pasa al mundo; el repaso solo regresa al roadmap. */
    val terminar: () -> Unit = { if (viewModel.repaso) onVolver() else onIrAlMundo() }
    val volver: () -> Unit = {
        when (e.fase) {
            EvaluacionDiagnosticaViewModel.Fase.LLAMADO -> salir()
            EvaluacionDiagnosticaViewModel.Fase.PRUEBA -> if (e.resultado == null) confirmarSalida = true else terminar()
            EvaluacionDiagnosticaViewModel.Fase.REVELACION -> terminar()
        }
    }
    BackHandler { volver() }

    Box(Modifier.fillMaxSize().background(MI.Fondo)) {
        val mensaje = e.mensajeError
        if (mensaje != null) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(mensaje, style = TextStyle(fontFamily = FuentesMockup.alegreya(), fontSize = 17.sp, textAlign = TextAlign.Center), color = RG.Crema)
                LienzoBoton { BotonPozo("VOLVER", acento, Modifier.centroEn(195f, 40f)) { salir() } }
            }
        } else if (e.preguntas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = acento) }
        } else {
            val variante = when (e.fase) {
                EvaluacionDiagnosticaViewModel.Fase.LLAMADO -> VarianteFondoPozo.LLAMADO
                EvaluacionDiagnosticaViewModel.Fase.PRUEBA -> VarianteFondoPozo.PRUEBA
                EvaluacionDiagnosticaViewModel.Fase.REVELACION -> VarianteFondoPozo.REVELACION
            }
            LienzoRagnarok(relleno = MI.Fondo, escena = {
                Crossfade(variante, animationSpec = tween(450), label = "fondo") { FondoPozo(acento, it) }
            }) {
                Crossfade(e.fase, animationSpec = tween(450), label = "fase") { fase ->
                    Box(Modifier.fillMaxSize()) {
                        when (fase) {
                            EvaluacionDiagnosticaViewModel.Fase.LLAMADO -> PantallaLlamado(
                                mundo = nombreMundo,
                                acento = acento,
                                total = e.preguntas.size,
                                repaso = viewModel.repaso,
                                onConsultar = { viewModel.consultar() },
                                onOmitir = { viewModel.solicitarOmision() }
                            )
                            EvaluacionDiagnosticaViewModel.Fase.PRUEBA -> PantallaPrueba(viewModel, e, acento)
                            EvaluacionDiagnosticaViewModel.Fase.REVELACION -> PantallaRevelacion(
                                acento = acento,
                                repaso = viewModel.repaso,
                                aciertos = e.aciertos,
                                total = e.preguntas.size,
                                temas = e.detalleTemas,
                                onContinuar = terminar
                            )
                        }
                    }
                }
                BarraRagnarok(nombreMundo, orden, volver, mostrarHidromiel = false)
            }
        }
    }

    if (e.confirmandoOmision) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarOmision() },
            title = { Text("¿Omitir la evaluación?") },
            // RF-93
            text = {
                Text("Si la omites, no tendrás otra oportunidad de presentar la evaluación diagnóstica de ${e.mundo?.nombre ?: "este mundo"} y comenzarás desde el primer nivel disponible.")
            },
            confirmButton = { TextButton({ viewModel.confirmarOmision() }) { Text("Omitir", color = Color(0xFFE2614B)) } },
            dismissButton = { TextButton({ viewModel.cancelarOmision() }) { Text("Presentar evaluación") } }
        )
    }
    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Dejar el pozo?") },
            text = {
                Text(
                    if (viewModel.repaso) "Si sales ahora se pierde este repaso. Puedes volver al pozo cuando quieras."
                    else "Es una sola consulta: si sales antes de terminar, se descarta tu avance y empezarás de nuevo la próxima vez."
                )
            },
            confirmButton = {
                TextButton({
                    confirmarSalida = false
                    salir()
                }) { Text("Salir", color = Color(0xFFE2614B)) }
            },
            dismissButton = { TextButton({ confirmarSalida = false }) { Text("Seguir consultando") } }
        )
    }
}

/** Pequeño lienzo para usar un botón del pozo fuera de la escena. */
@Composable
private fun LienzoBoton(contenido: @Composable () -> Unit) {
    Box(Modifier.padding(top = 20.dp).size(390.dp, 80.dp)) { contenido() }
}

// ---------------------------------------------------------------------------
// 1 · El llamado de Mímir
// ---------------------------------------------------------------------------

@Composable
private fun PantallaLlamado(
    mundo: String,
    acento: Color,
    total: Int,
    repaso: Boolean,
    onConsultar: () -> Unit,
    onOmitir: () -> Unit
) {
    val aparecer = remember { Animatable(0f) }
    LaunchedEffect(Unit) { aparecer.animateTo(1f, tween(900)) }

    EscenaPozo(
        acento,
        Modifier.graphicsLayer {
            alpha = aparecer.value
            val s = 0.96f + 0.04f * aparecer.value
            scaleX = s
            scaleY = s
            transformOrigin = TransformOrigin(0.5f, 0.4f)
        }
    )
    RotuloPozo(if (repaso) "REPASO DEL MUNDO" else "EVALUACIÓN DIAGNÓSTICA", acento, Modifier.enMI(250f, 127f))
    TituloPozo(if (repaso) "El eco de Mímir" else "El llamado de Mímir", 33f, Modifier.enMI(250f, 170f).width(MI.d(460f).dp))

    // Anclado por abajo: si la cita crece, sube en vez de meterse bajo el recuadro de reglas (y=692).
    Box(Modifier.enMI(250f, 588f).tamMI(400f, 180f), contentAlignment = Alignment.BottomCenter) {
    Column(
        Modifier.width(MI.d(400f).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (repaso) "«Recorriste este mundo de principio a fin. Bebe otra vez del pozo y descubre qué saberes siguen vivos en ti»."
            else "«El pozo guarda saberes que muchos buscan. Demuestra cuáles ya viven en ti y no tendrás que recorrer de nuevo los caminos que conoces».",
            style = alegreya(15.5f * MI.K, italica = true, color = Color(0xFFE6DCCB), interlineaPx = 5f * MI.K, alinear = TextAlign.Center)
                .copy(shadow = Shadow(Color.Black, Offset.Zero, 6f))
        )
        Text(
            "— Mímir, guardián del pozo",
            Modifier.padding(top = MI.d(10f).dp),
            style = alegreya(13.5f * MI.K, color = MI.Gris).copy(shadow = Shadow(Color.Black, Offset.Zero, 6f))
        )
    }
    }

    Box(
        Modifier.enMI(250.5f, 777f).tamMI(459f, 170f).drawBehind {
            val c = caminoCortado(Rect(Offset.Zero, size), MI.d(10f).dp.toPx())
            drawPath(c, Color(0xFF0D0B0A).copy(alpha = 0.9f))
            drawPath(c, acento.copy(alpha = 0.5f), style = Stroke(1.dp.toPx()))
        }
    )
    ReglaPozo("ᛁ", "$total preguntas", if (repaso) "Al azar, de los niveles de $mundo" else "Sobre los niveles de $mundo", acento, 727f)
    SeparadorPozo(759f)
    ReglaPozo(
        "ᛟ", if (repaso) "Consulta libre" else "Una sola consulta",
        if (repaso) "Vuelve al pozo cuantas veces quieras" else "Si sales antes de terminar, se descarta el avance", acento, 782f
    )
    SeparadorPozo(811f)
    ReglaPozo(
        "ᚷ", "No otorga puntos",
        if (repaso) "Ni cambia tu progreso: solo es un repaso" else "Desbloquea los niveles que demuestres dominar", acento, 833f
    )

    BotonPozo("CONSULTAR EL POZO", acento, Modifier.enMI(251f, 951f), accion = onConsultar)

    if (!repaso) {
        Text(
            "Omitir evaluación",
            Modifier.enMI(250f, 1006f).pulsable(alPulsar = onOmitir).padding(horizontal = 12.dp, vertical = 6.dp),
            style = alegreya(15f * MI.K, color = MI.Gris).copy(textDecoration = TextDecoration.Underline)
        )
    }
}

/** Título grande en versalitas, como «El llamado de Mímir». */
@Composable
fun TituloPozo(texto: String, tamano: Float, modifier: Modifier) {
    val conCinzel = FuentesMockup.hayCinzel()
    val estilo = cinzel(tamano * MI.K, FontWeight.Bold, 0f, RG.Crema).copy(
        textAlign = TextAlign.Center,
        shadow = Shadow(Color.Black.copy(alpha = 0.7f), Offset(0f, 2f), 6f)
    )
    Text(
        FuentesMockup.versalitas(texto, estilo.fontSize, conCinzel),
        modifier,
        style = estilo,
        maxLines = 1,
        softWrap = false
    )
}

@Composable
private fun ReglaPozo(runa: String, titulo: String, detalle: String, acento: Color, y: Float) {
    RomboRuna(runa, Modifier.enMI(55f, y + 2f), color = acento.copy(alpha = 0.85f), lado = MI.K * 27f)
    TextoAjustable(titulo, alegreya(15.5f * MI.K, FontWeight.Bold), Modifier.enMI(85f + 187.5f, y - 9f).width(MI.d(375f).dp), minEscala = 0.8f)
    TextoAjustable(detalle, alegreya(13.5f * MI.K, color = Color(0xFFC8BDAA)), Modifier.enMI(85f + 187.5f, y + 13f).width(MI.d(375f).dp), minEscala = 0.75f)
}

@Composable
private fun SeparadorPozo(y: Float) {
    Box(Modifier.enMI(85f + 186f, y).size(MI.d(372f).dp, 1.dp).background(Color(0xFF3A3028)))
}

// ---------------------------------------------------------------------------
// 2 · Prueba de sabiduría
// ---------------------------------------------------------------------------

private enum class EstadoOpcionPozo { NEUTRA, ELEGIDA, CORRECTA, INCORRECTA, APAGADA }

@Composable
private fun PantallaPrueba(viewModel: EvaluacionDiagnosticaViewModel, e: EvaluacionDiagnosticaViewModel.Estado, acento: Color) {
    val pregunta = e.preguntaActual ?: return
    val indice = e.indiceActual
    val total = e.preguntas.size
    val revelada = e.revelada

    RotuloPozo("PRUEBA DE SABIDURÍA · %02d/%02d".format(indice + 1, total), acento, Modifier.enMI(250f, 127f))
    RunasSabiduria(total, indice, e.aciertos, revelada, acento, Modifier.enMI(250f, 168f))

    AnimatedContent(
        targetState = pregunta,
        transitionSpec = {
            (slideInHorizontally(tween(350)) { it } + fadeIn(tween(350))) togetherWith
                (slideOutHorizontally(tween(350)) { -it } + fadeOut(tween(250)))
        },
        contentKey = { it.id },
        // Tope antes del botón (y≈905): con preguntas largas se desplaza en vez de taparlo.
        modifier = Modifier.offset(y = MI.d(212f - 40f / MI.K).dp).width(RG.ANCHO.dp).heightIn(max = MI.d(690f).dp),
        label = "pregunta"
    ) { p ->
        CuerpoPregunta(viewModel, e, p, acento)
    }

    BotonPozo(
        if (revelada) (if (e.esUltima) "VER LA REVELACIÓN" else "SIGUIENTE") else "REVELAR RESPUESTA",
        acento,
        Modifier.enMI(251f, 951f),
        habilitado = revelada || e.seleccion.isNotEmpty()
    ) {
        if (revelada) viewModel.siguiente() else viewModel.revelar()
    }
}

@Composable
private fun CuerpoPregunta(
    viewModel: EvaluacionDiagnosticaViewModel,
    e: EvaluacionDiagnosticaViewModel.Estado,
    pregunta: PreguntaDiagnostica,
    acento: Color
) {
    // Solo el estado de la pregunta que se muestra (la que sale conserva el suyo)
    val esActual = e.preguntaActual?.id == pregunta.id
    val revelada = esActual && e.revelada
    val seleccion = if (esActual) e.seleccion else emptySet()

    fun estadoOpcion(i: Int): EstadoOpcionPozo {
        val elegida = i in seleccion
        if (!revelada) return if (elegida) EstadoOpcionPozo.ELEGIDA else EstadoOpcionPozo.NEUTRA
        if (i in pregunta.respuestasCorrectas) return EstadoOpcionPozo.CORRECTA
        return if (elegida) EstadoOpcionPozo.INCORRECTA else EstadoOpcionPozo.APAGADA
    }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            e.nombreTema(pregunta.temaId),
            Modifier.padding(bottom = MI.d(18f).dp),
            style = alegreya(15f * MI.K, italica = true, color = MI.Gris),
            maxLines = 1
        )
        Text(
            pregunta.enunciado,
            Modifier.width(MI.d(440f).dp),
            style = alegreya(
                (if (pregunta.enunciado.length > 90) 19f else 22f) * MI.K,
                interlineaPx = 3f * MI.K,
                alinear = TextAlign.Center
            )
        )
        if (pregunta.esSeleccionMultiple) {
            Text(
                "Selecciona todas las correctas.",
                Modifier.padding(top = MI.d(6f).dp),
                style = alegreya(13.5f * MI.K, italica = true, color = acento)
            )
        }
        DivisorPozo(acento, Modifier.padding(vertical = MI.d(22f).dp))
        Column(verticalArrangement = Arrangement.spacedBy(MI.d(13f).dp)) {
            pregunta.opciones.forEachIndexed { i, texto ->
                OpcionPozo(('A' + i).toString(), texto, estadoOpcion(i), acento) { viewModel.alternarOpcion(i) }
            }
        }
        val correcta = if (esActual) e.actualCorrecta else null
        AnimatedVisibility(
            revelada && correcta != null,
            modifier = Modifier.padding(top = MI.d(26f).dp),
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
        ) {
            val bien = correcta == true
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RotuloPozo(
                    if (bien) "EL POZO RECONOCE ESTE SABER" else "EL ECO NO COINCIDE",
                    if (bien) acento else MI.Rojo, tamano = 11f, espacio = 3.2f
                )
                TextoAjustable(
                    if (bien) "La ${MI.ordinales[minOf(e.indiceActual, MI.ordinales.size - 1)]} runa se ilumina en tu camino."
                    else textoRespuesta(pregunta),
                    alegreya(14.5f * MI.K, italica = true, color = Color(0xFFDCD2C0), alinear = TextAlign.Center),
                    Modifier.padding(top = MI.d(6f).dp).width(MI.d(460f).dp),
                    minEscala = 0.8f,
                    maxLineas = 2
                )
            }
        }
    }
}

/** «La respuesta era la B: …» (o las letras si hay varias correctas). */
private fun textoRespuesta(pregunta: PreguntaDiagnostica): String {
    val correctas = pregunta.respuestasCorrectas.sorted()
    val letras = correctas.map { "la " + ('A' + it) }
    if (correctas.size == 1 && correctas[0] in pregunta.opciones.indices) {
        val texto = pregunta.opciones[correctas[0]]
        val inicial = texto.take(1).lowercase() + texto.drop(1)
        return "La respuesta era ${letras[0]}: $inicial" + if (texto.endsWith(".")) "" else "."
    }
    val lista = if (letras.size > 1) letras.dropLast(1).joinToString(", ") + " y " + letras.last() else letras.joinToString("")
    return "Las respuestas eran $lista."
}

/** Hilera de runas: una por pregunta. Se ilumina al acertar; la actual va en un círculo. */
@Composable
private fun RunasSabiduria(total: Int, actual: Int, aciertos: List<Boolean>, revelada: Boolean, acento: Color, modifier: Modifier) {
    Row(modifier) {
        for (i in 0 until total) {
            val respondida = i < aciertos.size
            val acierto = respondida && aciertos[i]
            val esActual = i == actual
            val escala by animateFloatAsState(
                if (acierto && esActual && revelada) 1.25f else 1f,
                spring(dampingRatio = 0.5f, stiffness = 300f), label = "runa"
            )
            val color by animateColorAsState(
                when {
                    acierto -> acento
                    respondida -> acento.copy(alpha = 0.28f)
                    esActual -> acento
                    else -> Color(0xFF6B5E4A).copy(alpha = 0.55f)
                }, tween(300), label = "colorRuna"
            )
            Box(
                Modifier
                    .size(MI.d(40f).dp)
                    .drawBehind {
                        if (esActual) {
                            val r = MI.d(17f).dp.toPx()
                            if (acierto) drawCircle(acento.copy(alpha = 0.25f), r + 5.dp.toPx())
                            drawCircle(acento, r, style = Stroke(1.4.dp.toPx()))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    MI.runa(i),
                    Modifier.scale(escala),
                    style = TextStyle(
                        fontSize = MI.d(17f).sp,
                        color = color,
                        shadow = if (acierto) Shadow(acento.copy(alpha = 0.7f), Offset.Zero, 10f) else null
                    )
                )
            }
        }
    }
}

/** Opción de piedra con la letra en un recuadro (mockup «Prueba de sabiduría»). */
@Composable
private fun OpcionPozo(letra: String, texto: String, estado: EstadoOpcionPozo, acento: Color, accion: () -> Unit) {
    val color: Color? = when (estado) {
        EstadoOpcionPozo.ELEGIDA, EstadoOpcionPozo.CORRECTA -> acento
        EstadoOpcionPozo.INCORRECTA -> MI.Rojo
        else -> null
    }
    val apagada = estado == EstadoOpcionPozo.APAGADA
    val bloqueada = estado == EstadoOpcionPozo.CORRECTA || estado == EstadoOpcionPozo.INCORRECTA || apagada
    Row(
        Modifier
            .width(MI.d(455f).dp)
            .heightIn(min = MI.d(74f).dp)
            .drawBehind {
                val c = caminoCortado(Rect(Offset.Zero, size), MI.d(9f).dp.toPx())
                val sombra = (color ?: Color.Black).copy(alpha = if (color != null) 0.18f else 0.25f)
                drawPath(caminoCortado(Rect(Offset(0f, 2.dp.toPx()), size).inflate(3.dp.toPx()), MI.d(11f).dp.toPx()), sombra)
                val fondoAlfa = if (apagada) 0.6f else 1f
                drawPath(c, Brush.verticalGradient(listOf(Color(0xFF3D3C39).copy(alpha = fondoAlfa), Color(0xFF2E2D2A).copy(alpha = fondoAlfa))))
                if (color != null) drawPath(c, color.copy(alpha = if (estado == EstadoOpcionPozo.INCORRECTA) 0.28f else 0.22f))
                drawPath(c, color ?: Color(0xFF55534E), style = Stroke(if (color != null) 2.dp.toPx() else 1.dp.toPx()))
            }
            .pulsable(!bloqueada, accion)
            .padding(start = MI.d(16f).dp, end = MI.d(18f).dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MI.d(20f).dp)
    ) {
        Box(
            Modifier
                .size(MI.d(46f).dp)
                .background(color ?: Color(0xFF1C1B19))
                .then(if (color == null) Modifier.border(1.dp, Color(0xFF6A675F)) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                letra,
                style = cinzel(19f * MI.K, FontWeight.Bold, 0f, if (color != null) MI.Tinta else RG.Crema.copy(alpha = if (apagada) 0.55f else 1f))
            )
        }
        TextoAjustable(
            texto,
            alegreya(17f * MI.K, color = RG.Crema.copy(alpha = if (apagada) 0.5f else 1f)),
            Modifier.weight(1f),
            minEscala = 0.75f,
            maxLineas = 3
        )
        AnimatedVisibility(
            estado == EstadoOpcionPozo.CORRECTA || estado == EstadoOpcionPozo.INCORRECTA,
            enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn()
        ) {
            Box(Modifier.size(MI.d(28f).dp).background(color ?: acento, CircleShape), contentAlignment = Alignment.Center) {
                Icon(
                    if (estado == EstadoOpcionPozo.CORRECTA) Icons.Filled.Check else Icons.Filled.Close,
                    null, tint = MI.Tinta, modifier = Modifier.size(MI.d(18f).dp)
                )
            }
        }
    }
}
