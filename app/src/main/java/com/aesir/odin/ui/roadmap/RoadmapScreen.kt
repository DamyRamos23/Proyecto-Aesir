package com.aesir.odin.ui.roadmap

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.components.odin.dibujarMaterial
import com.aesir.odin.ui.components.odin.estiloRuna
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.navigation.OdinNavGraph
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pantalla del roadmap de un mundo: valida el acceso al nivel al entrar,
 * carga sus temas si está desbloqueado, y al tocar un tema valida su
 * acceso antes de navegar a la introducción. Reproduce el diagrama de
 * secuencia de CU-1. Visualmente es un escudo con un nodo rúnico por tema.
 */
@Composable
fun RoadmapScreen(
    navController: NavHostController,
    mundoId: String,
    appContainer: AppContainer
) {
    val viewModel: RoadmapViewModel = viewModel(
        factory = RoadmapViewModel.factory(
            appContainer.obtenerTemasPorMundoUseCase,
            appContainer.validarAccesoTemaUseCase,
            appContainer.validarAccesoNivelUseCase,
            appContainer.obtenerMundoUseCase
        )
    )
    val temas by viewModel.temas.collectAsState()
    val mundo by viewModel.mundo.collectAsState()
    val mensajeBloqueo by viewModel.mensajeBloqueo.collectAsState()
    val scope = rememberCoroutineScope()

    var nivelDesbloqueado by remember { mutableStateOf<Boolean?>(null) }
    var seleccionadoId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(mundoId) {
        val desbloqueado = viewModel.validarAccesoNivel(mundoId)
        nivelDesbloqueado = desbloqueado
        if (desbloqueado) {
            viewModel.cargarRoadmap(mundoId)
        }
    }

    val estilo = EstilosMundo.porOrden(mundo?.orden ?: 1)

    if (mensajeBloqueo != null) {
        val cerrar = {
            viewModel.limpiarMensajeBloqueo()
            if (nivelDesbloqueado == false) navController.popBackStack()
        }
        AlertDialog(
            onDismissRequest = { cerrar() },
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Escarcha,
            textContentColor = OdinPaleta.Hueso,
            icon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = OdinPaleta.Escarcha) },
            confirmButton = {
                TextButton(onClick = { cerrar() }) { Text("Entendido", color = estilo.acento, fontFamily = FuenteTitulo) }
            },
            title = { Text("Bloqueado", fontFamily = FuenteTitulo) },
            text = { Text(mensajeBloqueo ?: "", fontFamily = FuenteTexto) }
        )
    }

    val mundoActual = mundo
    FondoMundo(estilo = if (mundoActual != null) estilo else null, alturaPaisaje = 340.dp) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            BotonVolver("Mundos", onClick = { navController.popBackStack() })

            if (nivelDesbloqueado == true && mundoActual != null && temas.isNotEmpty()) {
                val completados = temas.count { it.completado }
                val seleccionado = temas.firstOrNull { it.id == seleccionadoId }
                    ?: temas.firstOrNull { it.desbloqueado && !it.completado }
                    ?: temas.lastOrNull { it.completado }
                    ?: temas.first()

                // Cabecera
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.size(42.dp)) {
                        val r = size.minDimension / 2f
                        drawCircle(Color.Black.copy(alpha = .55f), r)
                        drawCircle(estilo.metal, r, style = Stroke(1.5.dp.toPx()))
                        dibujarEmblema(estilo.emblema, center, r * .75f, estilo.acento)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        mundoActual.nombre.uppercase(),
                        style = estiloTitulo(30.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        mundoActual.descripcion,
                        color = Color(0xFFD8CFBE),
                        fontFamily = FuenteTexto,
                        fontSize = 14.5.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                        Box(
                            Modifier
                                .width(140.dp)
                                .height(7.dp)
                                .background(Color.Black.copy(alpha = .55f))
                                .border(1.dp, estilo.metal)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(completados.toFloat() / temas.size)
                                    .height(7.dp)
                                    .background(estilo.acento)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("$completados de ${temas.size} temas", color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(18.dp))

                EscudoTemas(
                    temas = temas,
                    estilo = estilo,
                    seleccionadoId = seleccionado.id,
                    onSeleccionar = { seleccionadoId = it },
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )

                // Las placas del nodo inferior sobresalen del escudo
                Spacer(Modifier.height(56.dp))

                AnimatedContent(
                    targetState = seleccionado,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                    contentKey = { it.id },
                    label = "detalleTema"
                ) { tema ->
                    PanelTema(
                        tema = tema,
                        total = temas.size,
                        anterior = temas.lastOrNull { it.orden < tema.orden },
                        estilo = estilo,
                        onAbrir = {
                            scope.launch {
                                val abierto = viewModel.intentarAbrirTema(tema.id)
                                if (abierto) {
                                    navController.navigate(OdinNavGraph.rutaIntroTemaCon(tema.id))
                                }
                            }
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Escudo de temas
// ---------------------------------------------------------------------------

private enum class EstadoNodo { COMPLETADO, ACTUAL, BLOQUEADO }

private fun estadoDe(tema: Tema) = when {
    tema.completado -> EstadoNodo.COMPLETADO
    tema.desbloqueado -> EstadoNodo.ACTUAL
    else -> EstadoNodo.BLOQUEADO
}

/** Ángulo (grados) del nodo i: empieza arriba y avanza en sentido antihorario. */
private fun anguloNodo(i: Int, total: Int) = -90f - i * 360f / total

@Composable
private fun EscudoTemas(
    temas: List<Tema>,
    estilo: EstiloMundo,
    seleccionadoId: String,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val medidor = rememberTextMeasurer()
    val runasAro = remember(estilo) {
        estilo.runas.map { medidor.measure(it, TextStyle(color = estilo.metal, fontSize = 13.sp, fontWeight = FontWeight.Bold)) }
    }

    BoxWithConstraints(modifier.aspectRatio(1f)) {
        val lado = maxWidth
        val radioNodos = lado * 0.33f
        val tamNodo = 58.dp

        Canvas(Modifier.size(lado)) {
            val c = center
            val r = size.minDimension / 2f
            val aro = 26.dp.toPx()
            val cara = r - aro
            val rn = radioNodos.toPx()

            // Sombra y aro metálico
            mancha(c + Offset(0f, 10.dp.toPx()), r * 1.05f, Color.Black, .75f)
            drawCircle(
                Brush.linearGradient(
                    listOf(estilo.metalClaro, estilo.metalOscuro, estilo.metalClaro.copy(alpha = .85f), estilo.metalOscuro),
                    start = Offset.Zero, end = Offset(size.width, size.height)
                ),
                r
            )
            drawCircle(Color(0x8C0B0907), r - 3.dp.toPx())
            drawCircle(estilo.metalClaro.copy(alpha = .7f), r - .8.dp.toPx(), style = Stroke(1.5.dp.toPx()))

            // Runas del aro y remaches
            for (i in 0 until 40) {
                if (i % 5 == 0) continue
                val runa = runasAro[i % runasAro.size]
                rotate(i * 9f, pivot = c) {
                    drawText(runa, topLeft = Offset(c.x - runa.size.width / 2f, c.y - r + aro / 2f - runa.size.height / 2f))
                }
            }
            for (i in 0 until 8) {
                val a = Math.toRadians(i * 45.0 - 90.0)
                val p = c + Offset(cos(a).toFloat(), sin(a).toFloat()) * (r - aro / 2f)
                drawCircle(Brush.radialGradient(listOf(estilo.metalClaro, Color(0xFF0A0806)), center = p - Offset(2f, 2f), radius = 7.dp.toPx()), 6.dp.toPx(), p)
                drawCircle(Color.White.copy(alpha = .35f), 1.5.dp.toPx(), p - Offset(1.5.dp.toPx(), 1.8.dp.toPx()))
            }

            // Cara del escudo con el material del mundo
            val circulo = Path().apply { addOval(androidx.compose.ui.geometry.Rect(c, cara)) }
            clipPath(circulo) { dibujarMaterial(estilo, 9) }
            drawCircle(estilo.metalOscuro, cara, c, style = Stroke(3.dp.toPx()))
            drawCircle(
                estilo.metal.copy(alpha = .35f), cara - 5.dp.toPx(), c,
                style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 5.dp.toPx())))
            )

            // Radios y camino entre temas
            val pos = temas.indices.map { i ->
                val a = Math.toRadians(anguloNodo(i, temas.size).toDouble())
                c + Offset(cos(a).toFloat(), sin(a).toFloat()) * rn
            }
            drawCircle(estilo.metal.copy(alpha = .18f), rn, c, style = Stroke(1.dp.toPx()))
            pos.forEach { drawLine(estilo.metal.copy(alpha = .22f), c, it, 1.2.dp.toPx()) }
            for (i in 0 until pos.size - 1) {
                val encendido = temas[i].completado
                if (encendido) {
                    drawLine(estilo.acento.copy(alpha = .35f), pos[i], pos[i + 1], 6.dp.toPx())
                    drawLine(estilo.acento, pos[i], pos[i + 1], 2.dp.toPx())
                } else {
                    drawLine(
                        estilo.metal.copy(alpha = .5f), pos[i], pos[i + 1], 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 5.dp.toPx()))
                    )
                }
                val m = (pos[i] + pos[i + 1]) / 2f
                val d = 4.dp.toPx()
                val rombo = Path().apply { moveTo(m.x, m.y - d); lineTo(m.x + d, m.y); lineTo(m.x, m.y + d); lineTo(m.x - d, m.y); close() }
                drawPath(rombo, if (encendido) estilo.acento else estilo.metalOscuro)
            }

            // Umbo central con el emblema del mundo
            val umbo = r * .2f
            mancha(c + Offset(0f, 3.dp.toPx()), umbo * 1.25f, Color.Black, .7f)
            drawCircle(
                Brush.linearGradient(listOf(estilo.metalClaro, estilo.metalOscuro, estilo.metalClaro), start = c - Offset(umbo, umbo), end = c + Offset(umbo, umbo)),
                umbo, c
            )
            drawCircle(
                Brush.radialGradient(listOf(estilo.metalClaro, estilo.metalOscuro, Color(0xFF050404)), center = c - Offset(umbo * .3f, umbo * .35f), radius = umbo * 1.3f),
                umbo * .86f, c
            )
            mancha(c, umbo * .9f, estilo.acento, .35f)
            dibujarEmblema(estilo.emblema, c, umbo * .58f, estilo.acento)
        }

        temas.forEachIndexed { i, tema ->
            val a = Math.toRadians(anguloNodo(i, temas.size).toDouble())
            val x = lado / 2 + radioNodos * cos(a).toFloat()
            val y = lado / 2 + radioNodos * sin(a).toFloat()
            NodoTema(
                tema = tema,
                runa = estilo.runaDeTema(tema.orden),
                estilo = estilo,
                seleccionado = tema.id == seleccionadoId,
                tamNodo = tamNodo,
                onClick = { onSeleccionar(tema.id) },
                modifier = Modifier.offset(x = x - 56.dp, y = y - tamNodo / 2)
            )
        }
    }
}

@Composable
private fun NodoTema(
    tema: Tema,
    runa: String,
    estilo: EstiloMundo,
    seleccionado: Boolean,
    tamNodo: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado = estadoDe(tema)
    val activo = estado != EstadoNodo.BLOQUEADO
    Column(
        modifier
            .width(112.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Tema ${tema.orden}: ${tema.nombre}, " + when (estado) {
                    EstadoNodo.COMPLETADO -> "completado"
                    EstadoNodo.ACTUAL -> "disponible"
                    EstadoNodo.BLOQUEADO -> "bloqueado"
                }
                selected = seleccionado
            }
            .clickable(role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(tamNodo), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(tamNodo)) {
                val r = size.minDimension / 2f
                if (estado == EstadoNodo.ACTUAL || seleccionado) mancha(center, r * 1.5f, estilo.acento, if (seleccionado) .5f else .3f)
                mancha(center + Offset(0f, 2.dp.toPx()), r * 1.1f, Color.Black, .6f)
                drawCircle(
                    Brush.linearGradient(listOf(estilo.metalClaro, estilo.metalOscuro, estilo.metalClaro, estilo.metalOscuro), start = Offset.Zero, end = Offset(size.width, size.height)),
                    r
                )
                drawCircle(Brush.radialGradient(listOf(Color(0xFF2A2622), Color(0xFF070605)), center = center - Offset(r * .2f, r * .3f), radius = r * 1.2f), r * .86f)
                drawCircle(
                    if (activo) estilo.acento else estilo.metalOscuro, r * .86f,
                    style = Stroke(if (activo) 2.dp.toPx() else 1.dp.toPx())
                )
            }
            Text(runa, style = estiloRuna(28.sp, if (activo) estilo.acento else Color(0xFF8A8478), activo))
            // Insignia: palomita, número del tema o candado
            when (estado) {
                EstadoNodo.COMPLETADO -> Insignia(estilo.acento, Modifier.align(Alignment.BottomEnd)) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = OdinPaleta.Hollin, modifier = Modifier.size(12.dp))
                }
                EstadoNodo.ACTUAL -> Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 9.dp)
                        .size(19.dp)
                        .clip(CircleShape)
                        .background(OdinPaleta.Hollin)
                        .border(1.5.dp, estilo.acento, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${tema.orden}", color = estilo.acento, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                EstadoNodo.BLOQUEADO -> Insignia(Color(0xFF0B0907), Modifier.align(Alignment.BottomEnd), borde = estilo.metal) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFC8C0B0), modifier = Modifier.size(11.dp))
                }
            }
        }
        Spacer(Modifier.height(if (estado == EstadoNodo.ACTUAL) 12.dp else 6.dp))
        val formaPlaca = CutCornerShape(5.dp)
        Text(
            tema.nombre,
            color = when {
                seleccionado -> Color(0xFF2A1D10)
                activo -> Color(0xFFEFE6D4)
                else -> Color(0xFFB5AD9E)
            },
            fontFamily = FuenteTexto,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .clip(formaPlaca)
                .background(if (seleccionado) Color(0xFFE6D6AE) else Color(0xE00A0806))
                .border(1.dp, if (seleccionado) estilo.acento else estilo.metal.copy(alpha = .6f), formaPlaca)
                .padding(horizontal = 6.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun Insignia(
    fondo: Color,
    modifier: Modifier = Modifier,
    borde: Color? = null,
    contenido: @Composable () -> Unit
) {
    Box(
        modifier
            .size(19.dp)
            .clip(CircleShape)
            .background(fondo)
            .then(if (borde != null) Modifier.border(1.dp, borde.copy(alpha = .7f), CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) { contenido() }
}

// ---------------------------------------------------------------------------
// Panel inferior
// ---------------------------------------------------------------------------

@Composable
private fun PanelTema(
    tema: Tema,
    total: Int,
    anterior: Tema?,
    estilo: EstiloMundo,
    onAbrir: () -> Unit
) {
    val forma = CutCornerShape(10.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Color(0xE6120F0C))
            .border(1.dp, estilo.metal, forma)
            .padding(16.dp)
    ) {
        Text(
            "TEMA ${tema.orden} DE $total",
            color = estilo.acento,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp
        )
        Text(tema.nombre, style = estiloTitulo(20.sp), modifier = Modifier.padding(top = 2.dp))
        Text(
            tema.descripcion,
            color = Color(0xFFB8AE9C),
            fontFamily = FuenteTexto,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(14.dp))
        if (tema.desbloqueado) {
            BotonOdin(
                texto = when {
                    tema.completado -> "Repasar tema"
                    tema.introduccionVista -> "Continuar tema"
                    else -> "Comenzar tema"
                },
                onClick = onAbrir,
                acento = estilo.acento,
                iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = OdinPaleta.Escarcha, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    if (anterior != null) "Se abre al terminar ${anterior.nombre}." else "Este tema todavía está bloqueado.",
                    color = OdinPaleta.Escarcha,
                    fontFamily = FuenteTexto,
                    fontSize = 14.sp
                )
            }
        }
    }
}
