package com.aesir.odin.ui.leccion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.SelloRunico
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.components.odin.grieta
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.components.odin.manchaOval
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Resumen de resultados de una lección completada (RF-21, RF-22).
 *
 * Animación de ~4.5 s: la runa del tema, tallada en una piedra ancestral, se
 * llena de luz; luego se enciende la inscripción del borde, hay un destello,
 * aparece el título y las estadísticas cuentan hacia arriba. Tocar la
 * pantalla salta al final.
 */
@Composable
fun ResumenLeccionScreen(
    leccionId: String,
    puntaje: Int,
    errores: Int,
    totalEjercicios: Int,
    appContainer: AppContainer,
    onVolverRoadmap: () -> Unit,
    onAbrirTema: (temaId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ResumenLeccionViewModel = viewModel(
        factory = ResumenLeccionViewModel.factory(appContainer, leccionId)
    )
    val contexto by viewModel.contexto.collectAsState()
    val fallo by viewModel.fallo.collectAsState()
    val ctx = contexto

    if (ctx == null && !fallo) {
        FondoMundo(estilo = null, modifier = modifier) {
            CircularProgressIndicator(color = OdinPaleta.Brasa, modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    val estilo = EstilosMundo.porOrden(ctx?.mundo?.orden ?: 1)
    ResumenAnimado(
        estilo = estilo,
        tema = ctx?.tema,
        temas = ctx?.temas.orEmpty(),
        siguiente = ctx?.siguiente,
        nombreMundo = ctx?.mundo?.nombre ?: "",
        puntaje = puntaje,
        errores = errores,
        totalEjercicios = totalEjercicios,
        onVolverRoadmap = onVolverRoadmap,
        onAbrirTema = onAbrirTema,
        modifier = modifier
    )
}

private fun tramo(t: Float, desde: Float, hasta: Float) = ((t - desde) / (hasta - desde)).coerceIn(0f, 1f)
private fun suave(x: Float) = 1f - (1f - x) * (1f - x) * (1f - x)

@Composable
private fun ResumenAnimado(
    estilo: EstiloMundo,
    tema: Tema?,
    temas: List<Tema>,
    siguiente: Tema?,
    nombreMundo: String,
    puntaje: Int,
    errores: Int,
    totalEjercicios: Int,
    onVolverRoadmap: () -> Unit,
    onAbrirTema: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val progreso = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, tween(durationMillis = 4500, easing = LinearEasing))
    }
    val t = progreso.value

    // Fases de la animación
    val luzRuna = suave(tramo(t, .08f, .40f))
    val luzBanda = suave(tramo(t, .25f, .55f))
    val destello = tramo(t, .42f, .50f) - tramo(t, .50f, .62f)
    val titulo = suave(tramo(t, .46f, .60f))
    val stats = suave(tramo(t, .58f, .78f))
    val conteo = suave(tramo(t, .62f, .82f))
    val tarjeta = suave(tramo(t, .76f, .86f))
    val botones = suave(tramo(t, .84f, .94f))

    val runa = estilo.runaDeTema(tema?.orden ?: 1)
    val aciertos = (totalEjercicios - errores).coerceAtLeast(0)
    val siguienteDisponible = siguiente != null && siguiente.desbloqueado

    FondoMundo(
        estilo = estilo,
        alturaPaisaje = 460.dp,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { scope.launch { progreso.snapTo(1f) } }
        }
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Column(
                Modifier.graphicsLayer { alpha = titulo; translationY = (1f - titulo) * 24f },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "LECCIÓN COMPLETADA",
                    color = estilo.acento,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = (3f + (1f - titulo) * 4f).sp
                )
                Text("RUNA DOMINADA", style = estiloTitulo(34.sp, Color(0xFFF3E2BC), brillo = estilo.acento), textAlign = TextAlign.Center)
                if (tema != null) {
                    Text(
                        "Dominaste ${tema.nombre}.",
                        color = Color(0xFFD8CFBE),
                        fontFamily = FuenteTexto,
                        fontStyle = FontStyle.Italic,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            PiedraRunica(
                estilo = estilo,
                runa = runa,
                t = t,
                luzRuna = luzRuna,
                luzBanda = luzBanda,
                destello = destello,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            // Runas del mundo: la del tema actual se enciende con el destello
            if (temas.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    temas.sortedBy { it.orden }.forEach { t2 ->
                        val esActual = t2.id == tema?.id
                        val encendida = if (esActual) titulo > .3f else t2.completado
                        SelloRunico(
                            runa = estilo.runaDeTema(t2.orden),
                            estilo = estilo,
                            tamano = 30.dp,
                            encendido = encendida,
                            resplandor = esActual && encendida
                        )
                    }
                }
            }

            // Estadísticas
            Estadisticas(
                estilo = estilo,
                aciertos = (aciertos * conteo).toInt(),
                puntaje = (puntaje * conteo).toInt(),
                errores = (errores * conteo).toInt(),
                fraccionPuntaje = puntaje / 100f * conteo,
                modifier = Modifier.graphicsLayer { alpha = stats; translationY = (1f - stats) * 40f }
            )

            Spacer(Modifier.height(10.dp))

            // Tema desbloqueado o mundo completado
            Box(Modifier.graphicsLayer { alpha = tarjeta; translationX = (1f - tarjeta) * -80f }) {
                when {
                    siguienteDisponible && siguiente != null -> TarjetaDesbloqueo(
                        estilo = estilo,
                        runa = estilo.runaDeTema(siguiente.orden),
                        etiqueta = "NUEVO TEMA DESBLOQUEADO",
                        texto = "Tema ${siguiente.orden}: ${siguiente.nombre}",
                        onClick = if (botones > .9f) ({ onAbrirTema(siguiente.id) }) else null
                    )
                    siguiente == null && tema != null -> TarjetaDesbloqueo(
                        estilo = estilo,
                        runa = runa,
                        etiqueta = "MUNDO COMPLETADO",
                        texto = "Dominaste todas las runas de $nombreMundo",
                        onClick = null
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Botones (solo responden cuando ya son visibles)
            Column(
                Modifier.graphicsLayer { alpha = botones; translationY = (1f - botones) * 30f },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (siguienteDisponible && siguiente != null) {
                    BotonOdin(
                        texto = "Siguiente tema",
                        onClick = { onAbrirTema(siguiente.id) },
                        acento = estilo.acento,
                        habilitado = botones > .9f,
                        iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = onVolverRoadmap, enabled = botones > .9f) {
                        Text("Volver a $nombreMundo", color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 15.sp)
                    }
                } else {
                    BotonOdin(
                        texto = "Volver al roadmap",
                        onClick = onVolverRoadmap,
                        acento = estilo.acento,
                        habilitado = botones > .9f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Piedra rúnica
// ---------------------------------------------------------------------------

private class Chispa(val x: Float, val y: Float, val subida: Float, val deriva: Float, val radio: Float, val retraso: Float)

@Composable
private fun PiedraRunica(
    estilo: EstiloMundo,
    runa: String,
    t: Float,
    luzRuna: Float,
    luzBanda: Float,
    destello: Float,
    modifier: Modifier = Modifier
) {
    val medidor = rememberTextMeasurer(cacheSize = 32)
    val chispas = remember {
        val r = Random(5)
        List(26) { Chispa(.28f + r.nextFloat() * .44f, .3f + r.nextFloat() * .5f, .2f + r.nextFloat() * .35f, (r.nextFloat() - .5f) * .2f, .6f + r.nextFloat() * 1.4f, r.nextFloat() * .2f) }
    }
    val inscripcion = remember(estilo) { estilo.runas + estilo.runas.reversed() }

    Canvas(modifier) {
        // Proporción de la piedra: 208 × 314
        val alto = min(size.height * .96f, size.width * .62f / .662f)
        val ancho = alto * .662f
        val left = (size.width - ancho) / 2f
        val top = size.height - alto - 6.dp.toPx()
        fun p(fx: Float, fy: Float) = Offset(left + fx * ancho, top + fy * alto)
        val centroRuna = p(.5f, .56f)

        val piedra = Path().apply {
            p(.067f, 1f).let { moveTo(it.x, it.y) }
            cubic(this, p(.019f, .834f), p(0f, .675f), p(.029f, .516f))
            cubic(this, p(.058f, .293f), p(.183f, .089f), p(.5f, 0f))
            cubic(this, p(.817f, .089f), p(.952f, .299f), p(.971f, .516f))
            cubic(this, p(1f, .675f), p(.981f, .834f), p(.933f, 1f))
            close()
        }
        val banda = Path().apply {
            p(.144f, .962f).let { moveTo(it.x, it.y) }
            cubic(this, p(.106f, .803f), p(.096f, .662f), p(.12f, .522f))
            cubic(this, p(.149f, .331f), p(.26f, .159f), p(.5f, .083f))
            cubic(this, p(.74f, .159f), p(.856f, .331f), p(.885f, .522f))
            cubic(this, p(.909f, .662f), p(.894f, .803f), p(.856f, .962f))
        }

        // Halo y sombra en el suelo
        mancha(centroRuna, ancho * 1.15f, estilo.acento, .5f * luzRuna + .45f * max(0f, destello))
        manchaOval(p(.5f, 1f), ancho * .62f, 14.dp.toPx(), Color.Black, .75f)

        // Cuerpo de piedra
        clipPath(piedra) {
            drawRect(Brush.linearGradient(listOf(Color(0xFF9A9488), Color(0xFF5F5A51), Color(0xFF26241F)), start = p(0f, 0f), end = p(1f, 1f)))
            drawRect(estilo.caraClara.copy(alpha = .22f))
            val rnd = Random(31)
            repeat(40) {
                mancha(p(rnd.nextFloat(), rnd.nextFloat()), ancho * (.04f + rnd.nextFloat() * .12f), if (rnd.nextBoolean()) Color.Black else Color.White, .06f + rnd.nextFloat() * .08f)
            }
            repeat(4) {
                val g = grieta(rnd, p(.15f + rnd.nextFloat() * .7f, .1f + rnd.nextFloat() * .8f), 3, ancho * .14f)
                drawPath(g, Color(0xB315120F), style = Stroke(1.4.dp.toPx()))
            }
            mancha(p(.2f, .93f), ancho * .3f, Color(0xFF4D5A2A), .55f)
            mancha(p(.85f, .9f), ancho * .25f, Color(0xFF4D5A2A), .5f)
            mancha(p(.5f, .05f), ancho * .15f, Color(0xFF4D5A2A), .35f)
            drawRect(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .05f), Color.Black.copy(alpha = .55f)), startX = left, endX = left + ancho))
            mancha(centroRuna, ancho * .8f, estilo.acento, .55f * luzRuna + .3f * max(0f, destello))
        }

        // Banda de la serpiente tallada
        drawPath(banda, Color(0xBF14110E), style = Stroke(ancho * .105f))
        drawPath(banda, Color(0xFF2A2621), style = Stroke(ancho * .085f))
        val medida = PathMeasure()
        medida.setPath(banda, false)
        val largo = medida.length
        if (luzBanda > 0f) {
            val tramoEncendido = Path()
            medida.getSegment(0f, largo * luzBanda, tramoEncendido, true)
            drawPath(tramoEncendido, estilo.acento.copy(alpha = .22f), style = Stroke(ancho * .085f, cap = StrokeCap.Round))
        }
        val tamRuna = (ancho * .06f).toSp()
        val estiloApagado = TextStyle(fontSize = tamRuna, fontWeight = FontWeight.Bold, color = Color(0xFF0C0A08))
        val estiloEncendido = TextStyle(fontSize = tamRuna, fontWeight = FontWeight.Bold, color = Color(0xFFFFE0A0))
        val paso = ancho * .068f
        var d = paso * .8f
        var i = 0
        while (d < largo - paso * .5f) {
            val pos = medida.getPosition(d)
            val tan = medida.getTangent(d)
            val angulo = Math.toDegrees(atan2(tan.y, tan.x).toDouble()).toFloat()
            val encendida = d <= largo * luzBanda
            val capa = medidor.measure(inscripcion[i % inscripcion.size], if (encendida) estiloEncendido else estiloApagado)
            rotate(angulo, pivot = pos) {
                drawText(
                    capa,
                    topLeft = pos - Offset(capa.size.width / 2f, capa.size.height / 2f),
                    shadow = if (encendida) Shadow(estilo.acento, Offset.Zero, 12f) else null
                )
            }
            d += paso
            i++
        }

        // Runa central tallada que se llena de luz de abajo hacia arriba
        val capaRuna = medidor.measure(runa, TextStyle(fontSize = (alto * .42f).toSp(), fontWeight = FontWeight.Bold))
        val origen = centroRuna - Offset(capaRuna.size.width / 2f, capaRuna.size.height / 2f)
        drawText(capaRuna, color = Color(0x59A49B8A), topLeft = origen + Offset(2f, 2f))
        drawText(capaRuna, color = Color(0xFF0B0907), topLeft = origen)
        if (luzRuna > 0f) {
            val inferior = origen.y + capaRuna.size.height
            clipRect(top = inferior - capaRuna.size.height * luzRuna) {
                drawText(capaRuna, color = Color(0xFFFFCF7A), topLeft = origen, shadow = Shadow(estilo.acento, Offset.Zero, 40f))
                drawText(capaRuna, color = Color(0xFFFFF3D0), topLeft = origen, alpha = .85f)
            }
        }

        // Destello
        if (destello > 0f) {
            mancha(centroRuna, ancho * (.45f + .9f * tramo(t, .42f, .62f)), Color(0xFFFFF6DC), destello)
        }

        // Chispas que suben
        val avance = tramo(t, .2f, 1f)
        chispas.forEach { ch ->
            val tt = avance - ch.retraso
            if (tt > 0f) {
                val opacidad = (1f - tt * 1.1f).coerceIn(0f, 1f) * (tt * 8f).coerceAtMost(1f)
                drawCircle(
                    Color(0xFFFFD27A).copy(alpha = opacidad),
                    ch.radio.dp.toPx(),
                    p(ch.x + ch.deriva * tt, ch.y - ch.subida * tt)
                )
            }
        }
    }
}

private fun cubic(path: Path, c1: Offset, c2: Offset, fin: Offset) {
    path.cubicTo(c1.x, c1.y, c2.x, c2.y, fin.x, fin.y)
}

// ---------------------------------------------------------------------------
// Estadísticas y tarjeta
// ---------------------------------------------------------------------------

@Composable
private fun Estadisticas(
    estilo: EstiloMundo,
    aciertos: Int,
    puntaje: Int,
    errores: Int,
    fraccionPuntaje: Float,
    modifier: Modifier = Modifier
) {
    val forma = CutCornerShape(10.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Color(0xDB0E0B08))
            .border(1.dp, estilo.metal.copy(alpha = .6f), forma)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dato(Modifier.weight(1f), "$aciertos", "Aciertos") {
            Box(Modifier.size(28.dp).border(1.6.dp, OdinPaleta.Correcto, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = OdinPaleta.Correcto, modifier = Modifier.size(18.dp))
            }
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(66.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(66.dp)) {
                    val grosor = 4.5.dp.toPx()
                    drawArc(OdinPaleta.Hierro, 0f, 360f, false, style = Stroke(grosor), topLeft = Offset(grosor / 2, grosor / 2), size = androidx.compose.ui.geometry.Size(size.width - grosor, size.height - grosor))
                    drawArc(
                        estilo.acento, -90f, 360f * fraccionPuntaje, false,
                        style = Stroke(grosor, cap = StrokeCap.Round),
                        topLeft = Offset(grosor / 2, grosor / 2),
                        size = androidx.compose.ui.geometry.Size(size.width - grosor, size.height - grosor)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$puntaje", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 22.sp)
                    Text("/ 100", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 10.sp, lineHeight = 11.sp)
                }
            }
            Text("Puntaje", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 13.sp)
        }
        Dato(Modifier.weight(1f), "$errores", "Errores") {
            Box(Modifier.size(28.dp).border(1.6.dp, OdinPaleta.Incorrecto, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Close, contentDescription = null, tint = OdinPaleta.Incorrecto, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun Dato(modifier: Modifier, valor: String, etiqueta: String, icono: @Composable () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        icono()
        Text(valor, color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontWeight = FontWeight.SemiBold, fontSize = 26.sp)
        Text(etiqueta, color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 13.sp)
    }
}

@Composable
private fun TarjetaDesbloqueo(
    estilo: EstiloMundo,
    runa: String,
    etiqueta: String,
    texto: String,
    onClick: (() -> Unit)?
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xDB0E0B08))
            .border(1.dp, estilo.metal.copy(alpha = .45f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelloRunico(runa = runa, estilo = estilo, tamano = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(etiqueta, color = estilo.acento, fontFamily = FuenteTitulo, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.2.sp)
            Text(texto, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 15.5.sp, maxLines = 2)
        }
        if (onClick != null) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = estilo.metal)
        }
    }
}
