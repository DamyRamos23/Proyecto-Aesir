package com.aesir.odin.ui.diagnostico

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.components.odin.TextoAjustable
import com.aesir.odin.ui.components.odin.centroEn
import com.aesir.odin.ui.ragnarok.RG
import com.aesir.odin.ui.ragnarok.alegreya
import com.aesir.odin.ui.ragnarok.caminoCortado
import com.aesir.odin.ui.ragnarok.cinzel
import kotlinx.coroutines.delay

/**
 * CU-10, RF-91: «Lo que el pozo ha reconocido», la revelación del diagnóstico
 * (mockup «El pozo de Mímir», tercera pantalla). Muestra los ecos
 * reconocidos, la hilera de runas encendidas y cada tema del mundo: los que
 * ya domina (superados por diagnóstico, sin hidromiel, RF-99) y los que
 * quedan por despertar, marcando dónde comienza.
 */
@Composable
fun PantallaRevelacion(
    acento: Color,
    repaso: Boolean,
    aciertos: List<Boolean>,
    total: Int,
    temas: List<EvaluacionDiagnosticaViewModel.DetalleTema>,
    onContinuar: () -> Unit
) {
    val dominados = temas.filter { it.conocido }
    val porDespertar = temas.filter { !it.conocido }
    val reconocidos = aciertos.count { it }
    var contador by remember { mutableIntStateOf(0) }
    var runasEncendidas by remember { mutableIntStateOf(0) }
    val aparecer = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // El número cuenta hacia arriba y las runas se encienden una a una
        for (i in 0 until total) {
            delay(110)
            runasEncendidas = i + 1
            contador = aciertos.take(i + 1).count { it }
        }
    }
    LaunchedEffect(Unit) { aparecer.animateTo(1f, tween(600)) }

    RotuloPozo(if (repaso) "ECO DEL REPASO" else "REVELACIÓN DEL DIAGNÓSTICO", acento, Modifier.enMI(250f, 127f))
    Column(Modifier.enMI(250f, 183f), horizontalAlignment = Alignment.CenterHorizontally) {
        TituloPozo(if (repaso) "Lo que aún" else "Lo que el pozo", 30f, Modifier)
        TituloPozo(if (repaso) "vive en ti" else "ha reconocido", 30f, Modifier.padding(top = MI.d(2f).dp))
    }

    Row(Modifier.enMI(252f, 278f), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$contador",
            style = cinzel(62f * MI.K, FontWeight.Bold, 0f, acento).copy(shadow = Shadow(acento.copy(alpha = 0.45f), Offset.Zero, 20f))
        )
        Column(Modifier.padding(start = MI.d(8f).dp)) {
            Text("de $total", style = cinzel(18f * MI.K, FontWeight.Bold, 0f, RG.Crema))
            Text(if (reconocidos == 1) "eco reconocido" else "ecos reconocidos", style = alegreya(16f * MI.K, color = MI.Gris))
        }
    }

    Row(Modifier.enMI(250f, 348f)) {
        for (i in 0 until total) {
            val encendida = i < runasEncendidas && i < aciertos.size && aciertos[i]
            val escala by animateFloatAsState(if (encendida) 1f else 0.92f, spring(dampingRatio = 0.55f, stiffness = 300f), label = "runa")
            val color by animateColorAsState(if (encendida) acento else Color(0xFF5C5246).copy(alpha = 0.6f), tween(250), label = "color")
            Column(
                Modifier.width(MI.d(38.5f).dp).scale(escala),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    MI.runa(i),
                    style = TextStyle(
                        fontSize = MI.d(17f).sp,
                        color = color,
                        shadow = if (encendida) Shadow(acento.copy(alpha = 0.7f), Offset.Zero, 10f) else null
                    )
                )
                Box(
                    Modifier
                        .padding(top = MI.d(5f).dp)
                        .size(MI.d(19f).dp, MI.d(2f).dp)
                        .background(if (encendida) acento else Color(0xFF3A342C).copy(alpha = if (i < aciertos.size) 1f else 0.4f))
                )
            }
        }
    }

    Column(
        Modifier
            .centroEn(RG.ANCHO / 2f, MI.y(380f + 260f))
            .size(RG.ANCHO.dp, MI.d(520f).dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MI.d(21f).dp)
            .padding(bottom = MI.d(12f).dp),
        verticalArrangement = Arrangement.spacedBy(MI.d(9f).dp)
    ) {
        EncabezadoSeccion((if (repaso) "SABERES FIRMES · " else "SABERES QUE YA DOMINAS · ") + "${dominados.size}", acento)
        if (dominados.isEmpty()) {
            Text(
                if (repaso) "Ningún saber quedó firme esta vez. Vuelve a beber del pozo." else "El pozo aún no reconoce saberes de este mundo.",
                Modifier.padding(start = MI.d(2f).dp),
                style = alegreya(14f * MI.K, italica = true, color = MI.Gris)
            )
        }
        dominados.forEachIndexed { i, tema ->
            FilaSaber(tema, TipoSaber.DOMINADO, repaso, acento, Modifier.entrada(aparecer.value, i))
        }
        Spacer(Modifier.height(MI.d(6f).dp))
        EncabezadoSeccion((if (repaso) "SABERES POR REFORZAR · " else "SABERES POR DESPERTAR · ") + "${porDespertar.size}", Color(0xFFC9BEAB))
        porDespertar.forEachIndexed { i, tema ->
            FilaSaber(
                tema, if (i == 0) TipoSaber.COMIENZO else TipoSaber.PENDIENTE, repaso, acento,
                Modifier.entrada(aparecer.value, dominados.size + i + 1)
            )
        }
        Text(
            if (repaso) "El repaso no otorga hidromiel ni cambia tu progreso." else "La evaluación diagnóstica no otorga hidromiel.",
            Modifier.fillMaxWidth().padding(top = MI.d(8f).dp),
            style = alegreya(13f * MI.K, italica = true, color = MI.Gris.copy(alpha = 0.8f), alinear = TextAlign.Center)
        )
    }

    BotonPozo(if (repaso) "VOLVER AL MUNDO" else "CONTINUAR MI CAMINO", acento, Modifier.enMI(251f, 951f), accion = onContinuar)
}

/** Las filas entran una tras otra, subiendo un poco. */
private fun Modifier.entrada(progreso: Float, i: Int): Modifier = graphicsLayer {
    val local = ((progreso * 1.6f) - i * 0.08f).coerceIn(0f, 1f)
    alpha = local
    translationY = (1f - local) * MI.d(14f) * density
}

@Composable
private fun EncabezadoSeccion(texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RotuloPozo(texto, color, tamano = 11f, espacio = 3.2f)
        Box(Modifier.padding(start = MI.d(12f).dp).weight(1f).height(1.dp).background(Color(0xFF3A3028)))
    }
}

private enum class TipoSaber { DOMINADO, COMIENZO, PENDIENTE }

@Composable
private fun FilaSaber(
    tema: EvaluacionDiagnosticaViewModel.DetalleTema,
    tipo: TipoSaber,
    repaso: Boolean,
    acento: Color,
    modifier: Modifier
) {
    val apagada = tipo == TipoSaber.PENDIENTE
    val subtitulo = when (tipo) {
        TipoSaber.DOMINADO -> if (repaso) "Firme · lo recuerdas bien" else "Dominado · nivel desbloqueado"
        TipoSaber.COMIENZO -> if (repaso) "Por reforzar · repasa aquí" else "Por aprender · comienzas aquí"
        TipoSaber.PENDIENTE -> if (repaso) "Por reforzar" else "Por aprender"
    }
    Row(
        modifier
            .fillMaxWidth()
            .height(MI.d(60f).dp)
            .alpha(if (apagada) 0.6f else 1f)
            .drawBehind {
                val c = caminoCortado(Rect(Offset.Zero, size), MI.d(9f).dp.toPx())
                if (tipo == TipoSaber.COMIENZO) {
                    drawPath(caminoCortado(Rect(Offset.Zero, size).inflate(4.dp.toPx()), MI.d(11f).dp.toPx()), acento.copy(alpha = 0.12f))
                }
                drawPath(c, if (tipo == TipoSaber.DOMINADO) acento.copy(alpha = 0.1f) else Color(0xFF141210).copy(alpha = 0.85f))
                drawPath(
                    c,
                    if (tipo == TipoSaber.PENDIENTE) Color(0xFF3A3631) else acento.copy(alpha = if (tipo == TipoSaber.DOMINADO) 0.75f else 1f),
                    style = Stroke(if (tipo == TipoSaber.COMIENZO) 1.6.dp.toPx() else 1.dp.toPx())
                )
            }
            .padding(horizontal = MI.d(13f).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(MI.d(36f).dp)
                .drawBehind { drawCircle(if (apagada) Color(0xFF4A443B) else acento, style = Stroke(1.4.dp.toPx())) },
            contentAlignment = Alignment.Center
        ) {
            Text(MI.runa(tema.orden - 1), style = TextStyle(fontSize = MI.d(15f).sp, color = if (apagada) Color(0xFF6B6255) else acento))
        }
        Column(Modifier.padding(start = MI.d(14f).dp).weight(1f)) {
            TextoAjustable(tema.nombre, alegreya(15.5f * MI.K, FontWeight.Bold), minEscala = 0.75f)
            Text(
                subtitulo,
                style = alegreya(13.5f * MI.K, color = if (tipo == TipoSaber.PENDIENTE) MI.Gris else Color(0xFFD8C79F)),
                maxLines = 1
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(MI.d(5f).dp)) {
            Text("${tema.aciertos}/${tema.total}", style = cinzel(15f * MI.K, FontWeight.Bold, 0f, RG.Crema))
            Row(horizontalArrangement = Arrangement.spacedBy(MI.d(4f).dp)) {
                repeat(tema.total.coerceAtLeast(0)) { i ->
                    val lleno = i < tema.aciertos
                    Box(Modifier.size(MI.d(12f).dp), contentAlignment = Alignment.Center) {
                        Box(
                            Modifier
                                .size(MI.d(8f).dp)
                                .rotate(45f)
                                .drawBehind {
                                    if (lleno) drawRect(acento) else drawRect(Color(0xFF6B6255), style = Stroke(0.8.dp.toPx()))
                                }
                        )
                    }
                }
            }
        }
    }
}
