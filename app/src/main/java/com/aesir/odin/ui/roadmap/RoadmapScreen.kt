package com.aesir.odin.ui.roadmap

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.draw.alpha
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
import com.aesir.odin.domain.hidromiel.EstadoNivel
import com.aesir.odin.domain.hidromiel.ResultadoAcceso
import com.aesir.odin.domain.model.Actividad
import com.aesir.odin.domain.model.ArbolHabilidades
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.ColorHidromiel
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.IconoCuerno
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
 * Pantalla del roadmap de un mundo: valida el acceso al mundo al entrar,
 * carga sus temas con el estado y costo que da el MOD-01 (Sistema de
 * Hidromiel) y, al entrar a un tema, cobra la entrada antes de navegar a la
 * introducción. Visualmente es un escudo con un nodo rúnico por tema; el
 * Ragnarök va oculto en el centro y se enciende cuando ya es accesible.
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
            appContainer.validarAccesoNivelUseCase,
            appContainer.obtenerMundoUseCase,
            appContainer.moduloHidromiel
        )
    )
    val temas by viewModel.temas.collectAsState()
    val ragnarok by viewModel.ragnarok.collectAsState()
    val accesos by viewModel.accesos.collectAsState()
    val saldo by viewModel.saldo.collectAsState()
    var confirmarEntrada by remember { mutableStateOf<Pair<Tema, ResultadoAcceso>?>(null) }
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
            title = { Text(if (nivelDesbloqueado == false) "Bloqueado" else "No puedes entrar", fontFamily = FuenteTitulo) },
            text = { Text(mensajeBloqueo ?: "", fontFamily = FuenteTexto) }
        )
    }

    // Fase 2 del MOD-01: se muestra el costo antes de cobrar la entrada.
    confirmarEntrada?.let { (tema, acceso) ->
        AlertDialog(
            onDismissRequest = { confirmarEntrada = null },
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Hueso,
            textContentColor = OdinPaleta.Hueso,
            icon = { IconoCuerno(tamano = 34.dp) },
            title = { Text(if (tema.esRagnarok) "¿Enfrentar el Ragnarök?" else "¿Entrar a ${tema.nombre}?", fontFamily = FuenteTitulo) },
            text = {
                Text(
                    "La entrada cuesta ${acceso.costo} de hidromiel (tienes $saldo). " +
                        "Si no superas el nivel o lo abandonas, la entrada se pierde.",
                    fontFamily = FuenteTexto
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEntrada = null
                    scope.launch {
                        if (viewModel.intentarAbrirTema(tema.id)) {
                            navController.navigate(OdinNavGraph.rutaNivelCon(tema, temas))
                        }
                    }
                }) { Text("Pagar ${acceso.costo}", color = estilo.acento, fontFamily = FuenteTitulo) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEntrada = null }) {
                    Text("Cancelar", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTitulo)
                }
            }
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
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BotonVolver("Mundos", onClick = { navController.popBackStack() })
                Spacer(Modifier.weight(1f))
                ContadorHidromiel(saldo = saldo, metal = estilo.metal)
            }

            if (nivelDesbloqueado == true && mundoActual != null && temas.isNotEmpty()) {
                val jugables = temas.filter { it.actividad.jugable }
                val completados = jugables.count { accesos[it.id]?.estado == EstadoNivel.SUPERADO }
                val accesoRagnarok = ragnarok?.let { accesos[it.id] }
                val seleccionado = temas.firstOrNull { it.id == seleccionadoId }
                    ?: ragnarok?.takeIf { it.id == seleccionadoId && accesoRagnarok?.estado != EstadoNivel.BLOQUEADO }
                    ?: temas.firstOrNull { accesos[it.id]?.estado.let { e -> e == EstadoNivel.DISPONIBLE || e == EstadoNivel.GRATUITO } }
                    ?: ragnarok?.takeIf { accesoRagnarok?.estado == EstadoNivel.DISPONIBLE }
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
                                    .fillMaxWidth(completados.toFloat() / jugables.size.coerceAtLeast(1))
                                    .height(7.dp)
                                    .background(estilo.acento)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("$completados de ${jugables.size} niveles", color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(18.dp))

                EscudoTemas(
                    temas = temas,
                    accesos = accesos,
                    estadoRagnarok = accesoRagnarok?.estado,
                    estilo = estilo,
                    seleccionadoId = seleccionado.id,
                    onSeleccionar = { seleccionadoId = it },
                    onSeleccionarRagnarok = { ragnarok?.let { seleccionadoId = it.id } },
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(20.dp))

                AnimatedContent(
                    targetState = seleccionado,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                    contentKey = { it.id },
                    label = "detalleTema"
                ) { tema ->
                    val acceso = accesos[tema.id]
                    PanelTema(
                        tema = tema,
                        total = temas.count { !it.esMinijuego },
                        acceso = acceso,
                        saldo = saldo,
                        estilo = estilo,
                        onAbrir = {
                            if (acceso != null && viewModel.requierePago(tema.id)) {
                                confirmarEntrada = tema to acceso
                            } else {
                                scope.launch {
                                    if (viewModel.intentarAbrirTema(tema.id)) {
                                        navController.navigate(OdinNavGraph.rutaNivelCon(tema, temas))
                                    }
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

private enum class EstadoNodo { COMPLETADO, ACTUAL, BLOQUEADO, PROXIMAMENTE }

private fun estadoDe(tema: Tema, acceso: ResultadoAcceso?) = when {
    !tema.actividad.jugable -> EstadoNodo.PROXIMAMENTE
    else -> when (acceso?.estado) {
        EstadoNivel.SUPERADO -> EstadoNodo.COMPLETADO
        EstadoNivel.DISPONIBLE, EstadoNivel.GRATUITO -> EstadoNodo.ACTUAL
        EstadoNivel.BLOQUEADO, null -> EstadoNodo.BLOQUEADO
    }
}

/** Runa del nodo: las lecciones usan la de su tema; cada minijuego, una propia. */
private fun runaDe(tema: Tema, estilo: EstiloMundo): String = when (tema.actividad) {
    Actividad.LECCION -> estilo.runaDeTema(tema.orden)
    Actividad.CRUCIGRAMA -> estilo.runas[4]
    Actividad.MEMORAMA -> estilo.runas[5]
    Actividad.FLASHCARDS -> estilo.runas[6]
}

@Composable
private fun EscudoTemas(
    temas: List<Tema>,
    accesos: Map<String, ResultadoAcceso>,
    estadoRagnarok: EstadoNivel?,
    estilo: EstiloMundo,
    seleccionadoId: String,
    onSeleccionar: (String) -> Unit,
    onSeleccionarRagnarok: () -> Unit,
    modifier: Modifier = Modifier
) {
    // El Ragnarök sigue oculto mientras esté bloqueado. Cuando ya es accesible,
    // rayos de luz van de cada nivel al centro y el centro parpadea lento.
    val ragnarokVisible = estadoRagnarok != null && estadoRagnarok != EstadoNivel.BLOQUEADO
    val ragnarokSuperado = estadoRagnarok == EstadoNivel.SUPERADO
    val pulso = rememberInfiniteTransition(label = "ragnarok").animateFloat(
        initialValue = .25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulsoRagnarok"
    )
    // Se lee solo al dibujar, para que el parpadeo no recomponga la pantalla.
    val brilloCentro: () -> Float = {
        when {
            ragnarokSuperado -> .8f
            ragnarokVisible -> pulso.value
            else -> 0f
        }
    }
    // Dos anillos (ArbolHabilidades): lecciones adentro y minijuegos afuera
    val lecciones = temas.filter { !it.esMinijuego }.sortedBy { it.orden }
    val minijuegos = temas.filter { it.esMinijuego }.sortedBy { it.orden }
    val n = lecciones.size
    val m = minijuegos.size
    val conexiones = remember(lecciones.map { it.id }, minijuegos.map { it.id }) {
        ArbolHabilidades.conexiones(lecciones.map { it.id }, minijuegos.map { it.id })
    }
    val medidor = rememberTextMeasurer()
    val runasAro = remember(estilo) {
        estilo.runas.map { medidor.measure(it, TextStyle(color = estilo.metal, fontSize = 13.sp, fontWeight = FontWeight.Bold)) }
    }

    BoxWithConstraints(modifier.aspectRatio(1f)) {
        val lado = maxWidth
        val radioInterior = if (m > 0) lado * 0.2f else lado * 0.33f
        val radioExterior = lado * 0.35f
        val tamLeccion = if (m > 0) 48.dp else 58.dp
        val tamMinijuego = 42.dp
        // Ángulo y radio de cada nivel, por id
        val ubicacion: Map<String, Pair<Float, Dp>> =
            lecciones.mapIndexed { i, t -> t.id to (ArbolHabilidades.anguloInterior(i, n) to radioInterior) }.toMap() +
                minijuegos.mapIndexed { k, t -> t.id to (ArbolHabilidades.anguloExterior(k, n, m) to radioExterior) }

        Canvas(Modifier.size(lado)) {
            val c = center
            val r = size.minDimension / 2f
            val aro = 26.dp.toPx()
            val cara = r - aro

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

            // Posición de cada nivel
            val pos: Map<String, Offset> = ubicacion.mapValues { (_, ubic) ->
                val a = Math.toRadians(ubic.first.toDouble())
                c + Offset(cos(a).toFloat(), sin(a).toFloat()) * ubic.second.toPx()
            }
            val jugable = temas.associate { it.id to it.actividad.jugable }
            val superado = { id: String -> accesos[id]?.estado == EstadoNivel.SUPERADO }

            drawCircle(estilo.metal.copy(alpha = .18f), radioInterior.toPx(), c, style = Stroke(1.dp.toPx()))
            if (m > 0) drawCircle(estilo.metal.copy(alpha = .12f), radioExterior.toPx(), c, style = Stroke(1.dp.toPx()))
            lecciones.forEach { t -> pos[t.id]?.let { drawLine(estilo.metal.copy(alpha = .22f), c, it, 1.2.dp.toPx()) } }

            // Rayos del Ragnarök: de cada nivel jugable al centro
            val brillo = brilloCentro()
            if (brillo > 0f) {
                temas.filter { jugable[it.id] == true }.forEach { t ->
                    val p = pos[t.id] ?: return@forEach
                    drawLine(estilo.acento.copy(alpha = .25f * brillo), c, p, 7.dp.toPx())
                    drawLine(estilo.acento.copy(alpha = .9f * brillo), c, p, 1.6.dp.toPx())
                }
            }

            // Conexiones del árbol. Un tramo se enciende cuando alguno de sus
            // extremos ya se superó; hacia un minijuego que aún no existe, apenas se insinúa.
            val tramos = conexiones.flatMap { (a, vecinos) -> vecinos.filter { a < it }.map { a to it } }
            for ((a, b) in tramos) {
                val pa = pos[a] ?: continue
                val pb = pos[b] ?: continue
                val activo = jugable[a] == true && jugable[b] == true
                val encendido = activo && (superado(a) || superado(b))
                when {
                    encendido -> {
                        drawLine(estilo.acento.copy(alpha = .35f), pa, pb, 6.dp.toPx())
                        drawLine(estilo.acento, pa, pb, 2.dp.toPx())
                    }
                    activo -> drawLine(
                        estilo.metal.copy(alpha = .5f), pa, pb, 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 5.dp.toPx()))
                    )
                    else -> drawLine(
                        estilo.metal.copy(alpha = .18f), pa, pb, 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 6.dp.toPx()))
                    )
                }
                // Rombo en los tramos entre lecciones
                if (a in lecciones.map { it.id } && b in lecciones.map { it.id }) {
                    val medio = (pa + pb) / 2f
                    val d = 4.dp.toPx()
                    val rombo = Path().apply { moveTo(medio.x, medio.y - d); lineTo(medio.x + d, medio.y); lineTo(medio.x, medio.y + d); lineTo(medio.x - d, medio.y); close() }
                    drawPath(rombo, if (encendido) estilo.acento else estilo.metalOscuro)
                }
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
            if (brillo > 0f) mancha(c, umbo * 1.6f, estilo.acento, .55f * brillo)
            dibujarEmblema(estilo.emblema, c, umbo * .58f, estilo.acento)
        }

        // Centro del escudo: entrada al Ragnarök (solo responde cuando ya es accesible)
        if (ragnarokVisible) {
            val tamUmbo = lado * 0.2f
            Box(
                Modifier
                    .offset(x = lado / 2 - tamUmbo / 2, y = lado / 2 - tamUmbo / 2)
                    .size(tamUmbo)
                    .clip(CircleShape)
                    .semantics {
                        contentDescription = "Ragnarök, " + if (ragnarokSuperado) "superado" else "disponible"
                    }
                    .clickable(role = Role.Button, onClick = onSeleccionarRagnarok)
            )
        }

        temas.forEach { tema ->
            val (angulo, radio) = ubicacion[tema.id] ?: return@forEach
            val a = Math.toRadians(angulo.toDouble())
            val tam = if (tema.esMinijuego) tamMinijuego else tamLeccion
            val x = lado / 2 + radio * cos(a).toFloat()
            val y = lado / 2 + radio * sin(a).toFloat()
            NodoTema(
                tema = tema,
                acceso = accesos[tema.id],
                runa = runaDe(tema, estilo),
                estilo = estilo,
                seleccionado = tema.id == seleccionadoId,
                tamNodo = tam,
                onClick = { onSeleccionar(tema.id) },
                modifier = Modifier.offset(x = x - tam / 2, y = y - tam / 2)
            )
        }
    }
}

/**
 * Nodo del escudo: círculo para las lecciones y rombo para los minijuegos.
 * Sin placa de texto (con dos anillos no caben); el nombre se ve en el panel
 * de abajo al seleccionarlo.
 */
@Composable
private fun NodoTema(
    tema: Tema,
    acceso: ResultadoAcceso?,
    runa: String,
    estilo: EstiloMundo,
    seleccionado: Boolean,
    tamNodo: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado = estadoDe(tema, acceso)
    val activo = estado == EstadoNodo.ACTUAL || estado == EstadoNodo.COMPLETADO
    val rombo = tema.esMinijuego
    Box(
        modifier
            .size(tamNodo)
            .alpha(if (estado == EstadoNodo.PROXIMAMENTE) .45f else 1f)
            .semantics(mergeDescendants = true) {
                contentDescription = "${tema.nombre}, " + when (estado) {
                    EstadoNodo.COMPLETADO -> "superado"
                    EstadoNodo.ACTUAL -> "disponible"
                    EstadoNodo.BLOQUEADO -> "bloqueado"
                    EstadoNodo.PROXIMAMENTE -> "próximamente"
                }
                selected = seleccionado
            }
            .clickable(role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(tamNodo)) {
            val r = size.minDimension / 2f
            if (estado == EstadoNodo.ACTUAL || seleccionado) mancha(center, r * 1.5f, estilo.acento, if (seleccionado) .55f else .3f)
            mancha(center + Offset(0f, 2.dp.toPx()), r * 1.1f, Color.Black, .6f)
            val metal = Brush.linearGradient(
                listOf(estilo.metalClaro, estilo.metalOscuro, estilo.metalClaro, estilo.metalOscuro),
                start = Offset.Zero, end = Offset(size.width, size.height)
            )
            val interior = Brush.radialGradient(listOf(Color(0xFF2A2622), Color(0xFF070605)), center = center - Offset(r * .2f, r * .3f), radius = r * 1.2f)
            val borde = if (activo) estilo.acento else estilo.metalOscuro
            val grosor = if (activo || seleccionado) 2.dp.toPx() else 1.dp.toPx()
            if (rombo) {
                fun diamante(s: Float) = Path().apply {
                    moveTo(center.x, center.y - s); lineTo(center.x + s, center.y)
                    lineTo(center.x, center.y + s); lineTo(center.x - s, center.y); close()
                }
                drawPath(diamante(r), metal)
                drawPath(diamante(r * .84f), interior)
                drawPath(diamante(r * .84f), if (seleccionado) estilo.acento else borde, style = Stroke(grosor))
            } else {
                drawCircle(metal, r)
                drawCircle(interior, r * .86f)
                drawCircle(if (seleccionado) estilo.acento else borde, r * .86f, style = Stroke(grosor))
            }
        }
        Text(runa, style = estiloRuna((tamNodo.value * .44f).sp, if (activo) estilo.acento else Color(0xFF8A8478), activo))
        // Insignia: palomita, número de la lección o candado
        when (estado) {
            EstadoNodo.COMPLETADO -> Insignia(estilo.acento, Modifier.align(Alignment.BottomEnd)) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = OdinPaleta.Hollin, modifier = Modifier.size(11.dp))
            }
            EstadoNodo.ACTUAL -> if (!rombo) Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 8.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(OdinPaleta.Hollin)
                    .border(1.5.dp, estilo.acento, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("${tema.orden}", color = estilo.acento, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            EstadoNodo.BLOQUEADO -> Insignia(Color(0xFF0B0907), Modifier.align(Alignment.BottomEnd), borde = estilo.metal) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFC8C0B0), modifier = Modifier.size(10.dp))
            }
            EstadoNodo.PROXIMAMENTE -> Unit
        }
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
    acceso: ResultadoAcceso?,
    saldo: Int,
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
            when {
                tema.esRagnarok -> "FIN DEL MUNDO"
                tema.esMinijuego -> "MINIJUEGO · ${tema.actividad.nombre.uppercase()}"
                else -> "LECCIÓN ${tema.orden} DE $total"
            },
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
        if (!tema.actividad.jugable) {
            Text(
                "Próximamente: este minijuego llega en una siguiente versión.",
                color = OdinPaleta.Escarcha,
                fontFamily = FuenteTexto,
                fontSize = 14.sp
            )
            return@Column
        }
        if (acceso == null) return@Column
        when (acceso.estado) {
            EstadoNivel.BLOQUEADO -> Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = OdinPaleta.Escarcha, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(acceso.motivo, color = OdinPaleta.Escarcha, fontFamily = FuenteTexto, fontSize = 14.sp)
            }
            else -> {
                val falta = if (acceso.estado == EstadoNivel.DISPONIBLE) (acceso.costo - saldo).coerceAtLeast(0) else 0
                LineaEntrada(acceso, falta)
                Spacer(Modifier.height(10.dp))
                BotonOdin(
                    texto = when {
                        acceso.estado == EstadoNivel.SUPERADO -> "Repasar"
                        tema.esRagnarok -> "Enfrentar"
                        acceso.estado == EstadoNivel.DISPONIBLE && acceso.costo > 0 -> "Entrar · ${acceso.costo}"
                        tema.esCrucigrama -> "Resolver crucigrama"
                        tema.introduccionVista -> "Continuar tema"
                        else -> "Comenzar tema"
                    },
                    onClick = onAbrir,
                    acento = estilo.acento,
                    habilitado = falta == 0,
                    iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Costo de entrada del nivel según el MOD-01 (E1, E2). */
@Composable
private fun LineaEntrada(acceso: ResultadoAcceso, falta: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconoCuerno(tamano = 20.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            when (acceso.estado) {
                EstadoNivel.GRATUITO -> "Entrada gratuita: primer nivel del mundo."
                EstadoNivel.SUPERADO -> "Ya superado: repetirlo es gratis, pero no da hidromiel."
                else -> when {
                    acceso.costo == 0 -> "Ya pagaste la entrada: puedes continuar."
                    falta > 0 -> "La entrada cuesta ${acceso.costo}. Te faltan $falta de hidromiel."
                    else -> "Entrada: ${acceso.costo} de hidromiel. Si no lo superas, se pierde."
                }
            },
            color = if (falta > 0) OdinPaleta.Incorrecto else ColorHidromiel,
            fontFamily = FuenteTexto,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
    }
}
