package com.aesir.odin.ui.mundos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.desaturar
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.components.odin.dibujarPaisaje
import com.aesir.odin.ui.components.odin.dibujarValknut
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.components.odin.grieta
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.navigation.OdinNavGraph
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.random.Random

/**
 * Pantalla que lista los mundos del roadmap como un escudo dividido en
 * fragmentos. Al entrar a un mundo navega a RoadmapScreen pasando su id;
 * es RoadmapScreen quien valida si ese nivel está desbloqueado, tal como lo
 * describe el diagrama de secuencia.
 */
@Composable
fun MundosScreen(
    navController: NavHostController,
    appContainer: AppContainer
) {
    val viewModel: MundosViewModel = viewModel(
        factory = MundosViewModel.factory(appContainer.obtenerMundosUseCase)
    )
    val mundos by viewModel.mundos.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarMundos()
    }

    var seleccionadoId by rememberSaveable { mutableStateOf<String?>(null) }
    val visibles = mundos.take(4)
    val seleccionado = visibles.firstOrNull { it.id == seleccionadoId }
        ?: visibles.firstOrNull { it.desbloqueado }
        ?: visibles.firstOrNull()

    FondoMundo(estilo = null) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("MUNDOS", style = estiloTitulo(36.sp))
            Spacer(Modifier.height(6.dp))
            Text(
                "Cada mundo es un tema de Ingeniería de Software. Termina uno para abrir el siguiente.",
                color = OdinPaleta.HuesoTenue,
                fontFamily = FuenteTexto,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(24.dp))

            if (visibles.isNotEmpty()) {
                EscudoMundos(
                    mundos = visibles,
                    seleccionadoId = seleccionado?.id,
                    onSeleccionar = { seleccionadoId = it },
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(Modifier.height(24.dp))

            if (seleccionado != null) {
                AnimatedContent(
                    targetState = seleccionado,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                    contentKey = { it.id },
                    label = "detalleMundo"
                ) { mundo ->
                    PanelMundo(
                        mundo = mundo,
                        anterior = visibles.getOrNull(visibles.indexOfFirst { it.id == mundo.id } - 1),
                        onEntrar = { navController.navigate(OdinNavGraph.rutaRoadmapCon(mundo.id)) }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Escudo
// ---------------------------------------------------------------------------

private enum class Cuadrante(val angulo: Float) {
    SUPERIOR_IZQ(-135f), SUPERIOR_DER(-45f), INFERIOR_IZQ(135f), INFERIOR_DER(45f)
}

@Composable
private fun EscudoMundos(
    mundos: List<Mundo>,
    seleccionadoId: String?,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.aspectRatio(1f)) {
        val lado = maxWidth
        val anillo = lado * 0.08f
        val separacion = 8.dp
        val cuarto = (lado - separacion) / 2
        val indiceSel = mundos.indexOfFirst { it.id == seleccionadoId }.coerceAtLeast(0)
        // +90° porque el marcador del anillo se dibuja arriba.
        val rotacion = rotacionDial(Cuadrante.entries[indiceSel].angulo + 90f)

        AnilloRunico(rotacion, anillo, Modifier.size(lado))

        mundos.forEachIndexed { i, mundo ->
            val cuadrante = Cuadrante.entries[i]
            val derecha = cuadrante == Cuadrante.SUPERIOR_DER || cuadrante == Cuadrante.INFERIOR_DER
            val abajo = cuadrante == Cuadrante.INFERIOR_IZQ || cuadrante == Cuadrante.INFERIOR_DER
            FragmentoMundo(
                mundo = mundo,
                estilo = EstilosMundo.porOrden(mundo.orden),
                cuadrante = cuadrante,
                seleccionado = mundo.id == seleccionadoId,
                forma = FormaCuadrante(cuadrante, separacion, anillo),
                onClick = { onSeleccionar(mundo.id) },
                modifier = Modifier
                    .offset(x = if (derecha) cuarto + separacion else 0.dp, y = if (abajo) cuarto + separacion else 0.dp)
                    .size(cuarto)
            )
        }

        Canvas(Modifier.align(Alignment.Center).size(lado * 0.22f)) {
            val r = size.minDimension / 2f
            drawCircle(Color.Black.copy(alpha = .6f), r * 1.08f, center + Offset(0f, 3.dp.toPx()))
            drawCircle(Brush.radialGradient(listOf(Color(0xFF2E271F), Color(0xFF12100D)), center = center, radius = r), r)
            drawCircle(OdinPaleta.Hierro, r - 1.5.dp.toPx(), style = Stroke(3.dp.toPx()))
            drawCircle(OdinPaleta.Oro.copy(alpha = .35f), r - 7.dp.toPx(), style = Stroke(1.dp.toPx()))
            dibujarValknut(center, r * .5f, OdinPaleta.Oro, 1.6.dp.toPx())
        }
    }
}

@Composable
private fun FragmentoMundo(
    mundo: Mundo,
    estilo: EstiloMundo,
    cuadrante: Cuadrante,
    seleccionado: Boolean,
    forma: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bloqueado = !mundo.desbloqueado
    val colorBorde by animateColorAsState(
        when {
            seleccionado -> estilo.acento
            bloqueado -> OdinPaleta.Escarcha.copy(alpha = .45f)
            else -> estilo.metal.copy(alpha = .7f)
        },
        label = "bordeFragmento"
    )
    val grosor by animateDpAsState(if (seleccionado) 2.5.dp else 1.dp, label = "grosorFragmento")

    val alineacion = when (cuadrante) {
        Cuadrante.SUPERIOR_IZQ -> BiasAlignment(0.12f, 0.12f)
        Cuadrante.SUPERIOR_DER -> BiasAlignment(-0.12f, 0.12f)
        Cuadrante.INFERIOR_IZQ -> BiasAlignment(0.12f, -0.12f)
        Cuadrante.INFERIOR_DER -> BiasAlignment(-0.12f, -0.12f)
    }

    Box(
        modifier
            .clip(forma)
            .semantics(mergeDescendants = true) {
                contentDescription = "${mundo.nombre}, ${if (bloqueado) "bloqueado" else "disponible"}"
                selected = seleccionado
            }
            .clickable(role = Role.Tab, onClick = onClick)
            .border(grosor, colorBorde, forma)
    ) {
        // Paisaje del mundo (desaturado si está bloqueado)
        Canvas(Modifier.matchParentSize().desaturar(bloqueado, 0.75f)) { dibujarPaisaje(estilo, 11) }
        // Velo de hielo con grietas para los mundos bloqueados
        if (bloqueado) {
            Canvas(Modifier.matchParentSize()) {
                drawRect(Brush.verticalGradient(listOf(Color(0x1ACFE2EE), Color(0x1F233240))))
                val rnd = Random(mundo.id.hashCode())
                repeat(3) {
                    val g = grieta(rnd, Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height), 4, size.width * .35f)
                    drawPath(g, Color(0x47E6F2FA), style = Stroke(.7.dp.toPx()))
                }
            }
        }
        // Sombra suave detrás del texto
        Canvas(Modifier.matchParentSize()) {
            val c = Offset(
                size.width * (0.5f + alineacion.horizontalBias * 0.5f),
                size.height * (0.5f + alineacion.verticalBias * 0.5f)
            )
            mancha(c, size.width * .42f, Color.Black, .45f)
        }
        Column(
            Modifier.align(alineacion).fillMaxWidth(0.82f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OdinPaleta.Hollin.copy(alpha = .75f))
                    .border(1.dp, (if (bloqueado) OdinPaleta.Escarcha else estilo.metal).copy(alpha = .7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (bloqueado) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFD4E4EE), modifier = Modifier.size(18.dp))
                } else {
                    Canvas(Modifier.size(26.dp)) { dibujarEmblema(estilo.emblema, center, size.minDimension / 2f, estilo.acento) }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                mundo.nombre,
                style = estiloTitulo(13.sp, if (bloqueado) Color(0xFFD4E4EE) else OdinPaleta.Hueso),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/** Recorta una caja cuadrada al cuarto de círculo que le toca dentro del escudo. */
private data class FormaCuadrante(
    private val cuadrante: Cuadrante,
    private val separacion: Dp,
    private val anillo: Dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val g = with(density) { separacion.toPx() }
        val a = with(density) { anillo.toPx() }
        val mitad = size.width + g / 2f
        val radio = mitad - a
        val centro = when (cuadrante) {
            Cuadrante.SUPERIOR_IZQ -> Offset(mitad, mitad)
            Cuadrante.SUPERIOR_DER -> Offset(-g / 2f, mitad)
            Cuadrante.INFERIOR_IZQ -> Offset(mitad, -g / 2f)
            Cuadrante.INFERIOR_DER -> Offset(-g / 2f, -g / 2f)
        }
        val circulo = Path().apply { addOval(Rect(centro, radio)) }
        val caja = Path().apply { addRect(Rect(Offset.Zero, size)) }
        return Outline.Generic(Path.combine(PathOperation.Intersect, circulo, caja))
    }
}

private const val RUNAS_ANILLO = "ᚠᚢᚦᚨᚱᚲᚷᚹᚺᚾᛁᛃᛇᛈᛉᛊᛏᛒᛖᛗᛚᛜᛞᛟ"

@Composable
private fun AnilloRunico(rotacion: Float, anillo: Dp, modifier: Modifier = Modifier) {
    val medidor = rememberTextMeasurer()
    val runas = remember(medidor) {
        RUNAS_ANILLO.map {
            medidor.measure(it.toString(), TextStyle(color = OdinPaleta.Oro.copy(alpha = .8f), fontSize = 13.sp))
        }
    }
    Canvas(modifier) {
        val r = size.minDimension / 2f
        val a = anillo.toPx()
        drawCircle(Color(0xFF0F0D0A), r)
        drawCircle(
            Brush.linearGradient(
                listOf(Color(0xFF4A4034), Color(0xFF2E2720), Color(0xFF3E352B)),
                start = Offset.Zero, end = Offset(size.width, size.height)
            ),
            r - a / 2f, style = Stroke(a)
        )
        drawCircle(OdinPaleta.Oro.copy(alpha = .6f), r - .75.dp.toPx(), style = Stroke(1.5.dp.toPx()))
        drawCircle(OdinPaleta.Oro.copy(alpha = .4f), r - a, style = Stroke(1.dp.toPx()))
        rotate(rotacion) {
            val n = 30
            for (i in 1 until n) { // la posición 0 queda libre para el marcador
                val runa = runas[i % runas.size]
                rotate(360f / n * i) {
                    drawText(
                        runa,
                        topLeft = Offset(center.x - runa.size.width / 2f, center.y - r + a / 2f - runa.size.height / 2f)
                    )
                }
            }
            val tope = center.y - r + a / 2f
            val m = a * .34f
            mancha(Offset(center.x, tope), m * 2.4f, OdinPaleta.Brasa, .5f)
            val rombo = Path().apply {
                moveTo(center.x, tope - m); lineTo(center.x + m * .7f, tope)
                lineTo(center.x, tope + m); lineTo(center.x - m * .7f, tope); close()
            }
            drawPath(rombo, OdinPaleta.Brasa)
        }
    }
}

/** Gira siempre por el camino más corto hacia el nuevo ángulo. */
@Composable
private fun rotacionDial(objetivo: Float): Float {
    val acumulado = remember { floatArrayOf(objetivo) }
    val destino = remember(objetivo) {
        val delta = ((objetivo - acumulado[0]) % 360f + 540f) % 360f - 180f
        acumulado[0] += delta
        acumulado[0]
    }
    val valor by animateFloatAsState(destino, tween(650, easing = FastOutSlowInEasing), label = "dial")
    return valor
}

// ---------------------------------------------------------------------------
// Panel inferior
// ---------------------------------------------------------------------------

@Composable
private fun PanelMundo(mundo: Mundo, anterior: Mundo?, onEntrar: () -> Unit) {
    val estilo = EstilosMundo.porOrden(mundo.orden)
    val forma = CutCornerShape(8.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Color(0xE61E1A15))
            .border(1.dp, (if (mundo.desbloqueado) estilo.metal else OdinPaleta.Hierro).copy(alpha = .7f), forma)
            .padding(20.dp)
    ) {
        Text(
            mundo.nombre.uppercase(),
            style = estiloTitulo(22.sp, if (mundo.desbloqueado) OdinPaleta.Hueso else OdinPaleta.Escarcha)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            mundo.descripcion,
            color = OdinPaleta.HuesoTenue,
            fontFamily = FuenteTexto,
            fontSize = 15.sp,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(18.dp))
        if (mundo.desbloqueado) {
            BotonOdin(
                texto = "Entrar",
                onClick = onEntrar,
                acento = estilo.acento,
                iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = OdinPaleta.Escarcha, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    if (anterior != null) "Se abre al terminar ${anterior.nombre}." else "Este mundo todavía está bloqueado.",
                    color = OdinPaleta.Escarcha,
                    fontFamily = FuenteTexto,
                    fontSize = 15.sp
                )
            }
        }
    }
}
