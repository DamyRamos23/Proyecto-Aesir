package com.aesir.odin.ui.crucigrama

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.model.Celda
import com.aesir.odin.domain.model.Crucigrama
import com.aesir.odin.domain.model.Direccion
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.PalabraCrucigrama
import com.aesir.odin.domain.model.ResultadoCrucigrama
import com.aesir.odin.domain.model.ResultadoPalabra
import com.aesir.odin.domain.usecase.crucigrama.ActualizarPuntajeUseCase
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.ColorHidromiel
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.FormaFlecha
import com.aesir.odin.ui.components.odin.IconoCuerno
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * CU-07 Resolver crucigrama de conceptos, con el diseño del mockup: panel oscuro
 * con runas, casillas de madera que se desplazan si no caben, la pista en un
 * globo junto a la palabra, las ayudas que cuestan hidromiel y un teclado con Ñ.
 */
@Composable
fun CrucigramaScreen(
    viewModel: CrucigramaViewModel,
    temaId: String,
    temasIncluidos: List<String>,
    onFinalizado: (ResultadoCrucigrama, totalPalabras: Int, ordenMundo: Int) -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val crucigrama by viewModel.crucigrama.collectAsState()
    val seleccionadaId by viewModel.palabraSeleccionada.collectAsState()
    val ultimoResultado by viewModel.ultimoResultado.collectAsState()
    val resultadoFinal by viewModel.resultadoFinal.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val entradas by viewModel.entradas.collectAsState()
    val celdaActual by viewModel.celdaActual.collectAsState()
    val bloqueadas by viewModel.bloqueadas.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
    val avisoAyuda by viewModel.avisoAyuda.collectAsState()
    val saldo by viewModel.saldo.collectAsState()

    var confirmarSalida by remember { mutableStateOf(false) }
    var segundos by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(temaId) {
        if (viewModel.crucigrama.value == null) viewModel.cargarCrucigrama(temaId, temasIncluidos)
    }
    LaunchedEffect(crucigrama != null, resultadoFinal == null) {
        while (crucigrama != null && resultadoFinal == null) {
            delay(1_000)
            segundos++
        }
    }
    LaunchedEffect(resultadoFinal) {
        val resultado = resultadoFinal ?: return@LaunchedEffect
        onFinalizado(resultado, crucigrama?.palabras?.size ?: 0, contexto?.mundo?.orden ?: 1)
    }

    // Salir con un crucigrama en curso es abandonar el nivel (MOD-01)
    val enCurso = crucigrama != null && resultadoFinal == null
    BackHandler(enabled = enCurso) { confirmarSalida = true }
    val salir: () -> Unit = {
        if (enCurso) {
            confirmarSalida = true
        } else {
            onSalir()
        }
    }

    val estilo = EstilosMundo.porOrden(contexto?.mundo?.orden ?: 1)

    FondoMundo(estilo = estilo, modifier = modifier, alturaPaisaje = 260.dp) {
        val actual = crucigrama
        when {
            mensajeError != null -> ErrorGeneracion(
                estilo = estilo,
                mensaje = mensajeError ?: "",
                onReintentar = viewModel::reintentarGeneracion,
                onVolver = onSalir,
                modifier = Modifier.align(Alignment.Center)
            )
            actual == null -> CircularProgressIndicator(color = estilo.acento, modifier = Modifier.align(Alignment.Center))
            else -> ContenidoCrucigrama(
                estilo = estilo,
                crucigrama = actual,
                nombreMundo = contexto?.mundo?.nombre ?: "",
                nivel = contexto?.tema?.orden,
                seleccionadaId = seleccionadaId,
                entradas = entradas,
                celdaActual = celdaActual,
                bloqueadas = bloqueadas,
                ultimoResultado = ultimoResultado,
                saldo = saldo,
                segundos = segundos,
                onVolver = salir,
                onCelda = viewModel::seleccionarCelda,
                onLetra = viewModel::escribir,
                onBorrar = viewModel::borrar,
                onRevelarLetra = viewModel::revelarLetra,
                onRevelarPalabra = viewModel::revelarPalabra
            )
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Hueso,
            textContentColor = OdinPaleta.Hueso,
            title = { Text("¿Abandonar el crucigrama?", fontFamily = FuenteTitulo) },
            text = {
                Text(
                    "Si sales ahora, el nivel cuenta como abandonado: si pagaste entrada de hidromiel, se pierde.",
                    fontFamily = FuenteTexto
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text("Seguir jugando", color = estilo.acento, fontFamily = FuenteTitulo)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    onSalir()
                }) { Text("Abandonar", color = OdinPaleta.Incorrecto, fontFamily = FuenteTitulo) }
            }
        )
    }

    avisoAyuda?.let { motivo ->
        AlertDialog(
            onDismissRequest = viewModel::limpiarAvisoAyuda,
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Hueso,
            textContentColor = OdinPaleta.Hueso,
            icon = { IconoCuerno(tamano = 32.dp) },
            title = { Text("Hidromiel insuficiente", fontFamily = FuenteTitulo) },
            text = { Text(motivo, fontFamily = FuenteTexto) },
            confirmButton = {
                TextButton(onClick = viewModel::limpiarAvisoAyuda) {
                    Text("Entendido", color = estilo.acento, fontFamily = FuenteTitulo)
                }
            }
        )
    }
}

@Composable
private fun ContenidoCrucigrama(
    estilo: EstiloMundo,
    crucigrama: Crucigrama,
    nombreMundo: String,
    nivel: Int?,
    seleccionadaId: String?,
    entradas: Map<Celda, Char>,
    celdaActual: Celda?,
    bloqueadas: Set<Celda>,
    ultimoResultado: ResultadoPalabra?,
    saldo: Int,
    segundos: Int,
    onVolver: () -> Unit,
    onCelda: (Celda) -> Unit,
    onLetra: (Char) -> Unit,
    onBorrar: () -> Unit,
    onRevelarLetra: () -> Unit,
    onRevelarPalabra: () -> Unit
) {
    val seleccionada = crucigrama.palabras.firstOrNull { it.id == seleccionadaId }
    val editable = seleccionada != null && !seleccionada.resuelta

    Column(
        Modifier
            .fillMaxSize()
            .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EncabezadoCrucigrama(estilo, crucigrama, nombreMundo, nivel, saldo, segundos, onVolver)

        PanelCuadricula(
            estilo = estilo,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            AreaCuadricula(
                estilo = estilo,
                crucigrama = crucigrama,
                seleccionada = seleccionada,
                entradas = entradas,
                celdaActual = celdaActual,
                bloqueadas = bloqueadas,
                ultimoResultado = ultimoResultado,
                onCelda = onCelda
            )
        }

        // Ayudas (MOD-01, E5)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BotonAyuda(
                texto = "Revelar letra",
                runa = estilo.runas[4],
                costo = PoliticaCosto.AYUDA_LETRA,
                alcanza = saldo >= PoliticaCosto.AYUDA_LETRA,
                estilo = estilo,
                habilitado = editable,
                onClick = onRevelarLetra
            )
            BotonAyuda(
                texto = "Revelar palabra",
                runa = estilo.runas[3],
                costo = PoliticaCosto.AYUDA_PALABRA,
                alcanza = saldo >= PoliticaCosto.AYUDA_PALABRA,
                estilo = estilo,
                habilitado = editable,
                onClick = onRevelarPalabra
            )
        }

        Teclado(
            estilo = estilo,
            onLetra = onLetra,
            onBorrar = onBorrar,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Encabezado
// ---------------------------------------------------------------------------

private val ColorTextoClaro = Color(0xFFD8CFBE)
private val ColorPergamino = Color(0xFFF3E2BC)

@Composable
private fun EncabezadoCrucigrama(
    estilo: EstiloMundo,
    crucigrama: Crucigrama,
    nombreMundo: String,
    nivel: Int?,
    saldo: Int,
    segundos: Int,
    onVolver: () -> Unit
) {
    val total = crucigrama.palabras.size
    val resueltas = crucigrama.resueltas
    val fraccion by animateFloatAsState(resueltas.toFloat() / maxOf(total, 1), tween(400), label = "progreso")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth()) {
            BotonVolver(
                nombreMundo.ifEmpty { "Roadmap" },
                onClick = onVolver,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .widthIn(max = 150.dp)
            )
            Canvas(
                Modifier
                    .size(46.dp)
                    .align(Alignment.Center)
            ) {
                val r = size.minDimension / 2f
                mancha(center, r * 1.5f, estilo.acento, .25f)
                drawCircle(Color(0xFF0D0A07), r)
                drawCircle(estilo.metal, r - 1.dp.toPx(), style = Stroke(2.dp.toPx()))
                drawCircle(estilo.metal.copy(alpha = .4f), r - 5.dp.toPx(), style = Stroke(1.dp.toPx()))
                dibujarEmblema(estilo.emblema, center, r * .62f, estilo.acento)
            }
            ContadorHidromiel(saldo = saldo, metal = estilo.metal, modifier = Modifier.align(Alignment.CenterEnd))
        }

        // "Crucigrama" en versalitas, como en el mockup
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 36.sp)) { append("C") }
                withStyle(SpanStyle(fontSize = 27.sp)) { append("RUCIGRAMA") }
            },
            style = estiloTitulo(36.sp, color = ColorPergamino).copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            listOfNotNull(nivel?.let { "Nivel $it" }, nombreMundo.ifEmpty { null }).joinToString(" · "),
            color = ColorTextoClaro,
            fontFamily = FuenteTexto,
            fontSize = 16.sp,
            maxLines = 1
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("%02d:%02d".format(segundos / 60, segundos % 60), color = ColorTextoClaro, fontFamily = FuenteTexto, fontSize = 15.sp)
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .weight(1f)
                    .height(7.dp)
                    .background(Color.Black.copy(alpha = .55f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraccion)
                        .fillMaxHeight()
                        .background(estilo.acento)
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(1.dp, estilo.metal.copy(alpha = .7f))
                )
            }
            Spacer(Modifier.width(10.dp))
            Text("$resueltas de $total palabras", color = ColorTextoClaro, fontFamily = FuenteTexto, fontSize = 15.sp, maxLines = 1)
        }
    }
}

// ---------------------------------------------------------------------------
// Cuadrícula
// ---------------------------------------------------------------------------

private val RUNAS_BORDE = "ᚠᚢᚦᚨᚱᚲᚷᚹᚺᚾᛁᛃᛇᛈᛉᛊᛏᛒᛖᛗᛚᛜᛞᛟ".map { it.toString() }

/**
 * Panel oscuro con marco metálico, rombos en las esquinas y una fila de runas
 * arriba y abajo. La cuadrícula va adentro; si no cabe, se desplaza.
 */
@Composable
private fun PanelCuadricula(
    estilo: EstiloMundo,
    modifier: Modifier = Modifier,
    contenido: @Composable BoxScope.() -> Unit
) {
    Column(
        modifier
            .drawBehind {
                val m = 4.dp.toPx()
                val exterior = Rect(m, m, size.width - m, size.height - m)
                val interior = exterior.deflate(7.dp.toPx())
                drawRect(Color(0xF0110D0A), exterior.topLeft, exterior.size)
                drawRect(estilo.metal.copy(alpha = .85f), exterior.topLeft, exterior.size, style = Stroke(1.2.dp.toPx()))
                drawRect(estilo.metal.copy(alpha = .35f), interior.topLeft, interior.size, style = Stroke(1.dp.toPx()))
                val d = 6.dp.toPx()
                listOf(exterior.topLeft, exterior.topRight, exterior.bottomLeft, exterior.bottomRight).forEach { p ->
                    val rombo = Path().apply {
                        moveTo(p.x, p.y - d); lineTo(p.x + d, p.y); lineTo(p.x, p.y + d); lineTo(p.x - d, p.y); close()
                    }
                    drawPath(rombo, Color(0xFF110D0A))
                    drawPath(rombo, estilo.metal, style = Stroke(1.2.dp.toPx()))
                }
            }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilaRunas(estilo, desfase = 0)
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            content = contenido
        )
        FilaRunas(estilo, desfase = 5)
    }
}

/** Runas repartidas a lo ancho, como el borde tallado del mockup. */
@Composable
private fun FilaRunas(estilo: EstiloMundo, desfase: Int) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clearAndSetSemantics { }
    ) {
        val n = maxOf(1, (maxWidth / 17.dp).toInt())
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(n) { i ->
                Text(RUNAS_BORDE[(i + desfase) % RUNAS_BORDE.size], color = estilo.metal.copy(alpha = .65f), fontSize = 11.sp)
            }
        }
    }
}

private val MARGEN_REJILLA = 10.dp
private val LADO_MINIMO = 36.dp
private val LADO_MAXIMO = 50.dp

@Composable
private fun AreaCuadricula(
    estilo: EstiloMundo,
    crucigrama: Crucigrama,
    seleccionada: PalabraCrucigrama?,
    entradas: Map<Celda, Char>,
    celdaActual: Celda?,
    bloqueadas: Set<Celda>,
    ultimoResultado: ResultadoPalabra?,
    onCelda: (Celda) -> Unit
) {
    val numeros = crucigrama.numeros
    val solucion = crucigrama.solucion
    val inicios = crucigrama.palabras.associate { Celda(it.fila, it.columna) to numeros[it.id] }
    val celdasSeleccion = seleccionada?.celdas.orEmpty().toSet()
    val falloEn = ultimoResultado?.takeIf { it.estado != EstadoRespuesta.CORRECTA }?.palabraId
    val celdasFallo = crucigrama.palabras.firstOrNull { it.id == falloEn }?.celdas.orEmpty().toSet()
    val resueltas = crucigrama.palabras.filter { it.resuelta }.flatMap { it.celdas }.toSet()
    val densidad = LocalDensity.current

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val vistaAncho = maxWidth
        val vistaAlto = maxHeight
        // Celdas lo más grandes posible sin bajar del mínimo; si así no caben, se desplaza
        val porAncho = (vistaAncho - MARGEN_REJILLA * 2) / maxOf(crucigrama.columnas, 1)
        val porAlto = (vistaAlto - MARGEN_REJILLA * 2) / maxOf(crucigrama.filas, 1)
        val lado = minOf(porAncho, porAlto).coerceIn(LADO_MINIMO, LADO_MAXIMO)
        val anchoRejilla = lado * crucigrama.columnas
        val altoRejilla = lado * crucigrama.filas
        val anchoContenido = maxOf(anchoRejilla + MARGEN_REJILLA * 2, vistaAncho)
        val altoContenido = maxOf(altoRejilla + MARGEN_REJILLA * 2, vistaAlto)
        val origenX = (anchoContenido - anchoRejilla) / 2
        val origenY = (altoContenido - altoRejilla) / 2
        val cabe = anchoContenido <= vistaAncho && altoContenido <= vistaAlto

        val scrollH = rememberScrollState()
        val scrollV = rememberScrollState()
        var altoGlobo by remember { mutableStateOf(0.dp) }

        // Sigue a la casilla donde se está escribiendo
        LaunchedEffect(celdaActual, lado, cabe) {
            val celda = celdaActual ?: return@LaunchedEffect
            if (cabe) return@LaunchedEffect
            val x = with(densidad) { (origenX + lado * (celda.columna + .5f) - vistaAncho / 2).toPx() }
            val y = with(densidad) { (origenY + lado * (celda.fila + .5f) - vistaAlto / 2).toPx() }
            launch { scrollH.animateScrollTo(x.roundToInt().coerceAtLeast(0)) }
            launch { scrollV.animateScrollTo(y.roundToInt().coerceAtLeast(0)) }
        }

        Box(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollV, enabled = !cabe)
                .horizontalScroll(scrollH, enabled = !cabe)
        ) {
            Box(Modifier.size(anchoContenido, altoContenido)) {
                solucion.keys.forEach { celda ->
                    key(celda) {
                        Casilla(
                            letra = entradas[celda],
                            numero = inicios[celda],
                            lado = lado,
                            estilo = estilo,
                            semilla = celda.fila * 31 + celda.columna + 1,
                            esActual = celda == celdaActual && seleccionada?.resuelta == false,
                            enSeleccion = celda in celdasSeleccion,
                            resuelta = celda in resueltas || celda in bloqueadas,
                            fallo = celda in celdasFallo,
                            onClick = { onCelda(celda) },
                            modifier = Modifier.offset(x = origenX + lado * celda.columna, y = origenY + lado * celda.fila)
                        )
                    }
                }

                if (seleccionada != null) {
                    val colocacion = colocarGlobo(
                        palabra = seleccionada,
                        celdaActual = celdaActual,
                        lado = lado,
                        origenX = origenX,
                        origenY = origenY,
                        anchoContenido = anchoContenido,
                        altoContenido = altoContenido,
                        altoGlobo = if (altoGlobo > 0.dp) altoGlobo else 104.dp
                    )
                    GloboPista(
                        estilo = estilo,
                        palabra = seleccionada,
                        numero = numeros[seleccionada.id],
                        resultado = ultimoResultado?.takeIf { it.palabraId == seleccionada.id || it.estado == EstadoRespuesta.CORRECTA },
                        lado = colocacion.lado,
                        puntaPx = with(densidad) { colocacion.punta.toPx() },
                        modifier = Modifier
                            .offset(x = colocacion.x, y = colocacion.y)
                            .width(colocacion.ancho)
                            .onSizeChanged { altoGlobo = with(densidad) { it.height.toDp() } }
                    )
                }
            }
        }
    }
}

private enum class LadoGlobo { IZQUIERDA, DERECHA, ARRIBA, ABAJO }

private data class Colocacion(val x: Dp, val y: Dp, val ancho: Dp, val lado: LadoGlobo, val punta: Dp)

/** Vertical: a un lado de la palabra (izquierda si cabe). Horizontal: abajo o arriba. */
private fun colocarGlobo(
    palabra: PalabraCrucigrama,
    celdaActual: Celda?,
    lado: Dp,
    origenX: Dp,
    origenY: Dp,
    anchoContenido: Dp,
    altoContenido: Dp,
    altoGlobo: Dp
): Colocacion {
    val ancho = minOf(220.dp, anchoContenido - 16.dp)
    val separacion = 12.dp
    val borde = 4.dp

    val celdas = palabra.celdas
    val primera = celdas.first()
    val ultima = celdas.last()
    val minX = origenX + lado * primera.columna
    val minY = origenY + lado * primera.fila
    val maxX = origenX + lado * (ultima.columna + 1)
    val maxY = origenY + lado * (ultima.fila + 1)

    val actual = celdaActual?.takeIf { it in celdas } ?: primera
    val centroX = origenX + lado * (actual.columna + .5f)
    val centroY = origenY + lado * (actual.fila + .5f)

    fun acotar(valor: Dp, minimo: Dp, maximo: Dp): Dp = if (maximo < minimo) minimo else valor.coerceIn(minimo, maximo)

    if (palabra.direccion == Direccion.VERTICAL) {
        val y = acotar(centroY - altoGlobo / 2, borde, altoContenido - altoGlobo - borde)
        val izquierda = minX - separacion - ancho
        if (izquierda >= borde) return Colocacion(izquierda, y, ancho, LadoGlobo.DERECHA, centroY - y)
        val derecha = maxX + separacion
        if (derecha + ancho <= anchoContenido - borde) return Colocacion(derecha, y, ancho, LadoGlobo.IZQUIERDA, centroY - y)
    }

    val x = acotar(centroX - ancho / 2, borde, anchoContenido - ancho - borde)
    val abajo = maxY + separacion
    if (abajo + altoGlobo <= altoContenido - borde) return Colocacion(x, abajo, ancho, LadoGlobo.ARRIBA, centroX - x)
    val arriba = maxOf(borde, minY - separacion - altoGlobo)
    return Colocacion(x, arriba, ancho, LadoGlobo.ABAJO, centroX - x)
}

/** Recuadro con una punta en uno de sus lados (el globo de la pista). */
private class FormaGlobo(private val lado: LadoGlobo, private val punta: Float, private val tam: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val t = tam
        val px = punta.coerceIn(t + 4f, maxOf(t + 4f, size.width - t - 4f))
        val py = punta.coerceIn(t + 4f, maxOf(t + 4f, size.height - t - 4f))
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, 0f)
            if (lado == LadoGlobo.ARRIBA) { lineTo(px - t, 0f); lineTo(px, -t); lineTo(px + t, 0f) }
            lineTo(w, 0f)
            if (lado == LadoGlobo.DERECHA) { lineTo(w, py - t); lineTo(w + t, py); lineTo(w, py + t) }
            lineTo(w, h)
            if (lado == LadoGlobo.ABAJO) { lineTo(px + t, h); lineTo(px, h + t); lineTo(px - t, h) }
            lineTo(0f, h)
            if (lado == LadoGlobo.IZQUIERDA) { lineTo(0f, py + t); lineTo(-t, py); lineTo(0f, py - t) }
            close()
        }
        return Outline.Generic(path)
    }
}

/** Globo con la pista de la palabra seleccionada; no bloquea los toques a las casillas. */
@Composable
private fun GloboPista(
    estilo: EstiloMundo,
    palabra: PalabraCrucigrama,
    numero: Int?,
    resultado: ResultadoPalabra?,
    lado: LadoGlobo,
    puntaPx: Float,
    modifier: Modifier = Modifier
) {
    val densidad = LocalDensity.current
    val forma = FormaGlobo(lado, puntaPx, with(densidad) { 9.dp.toPx() })
    val direccion = if (palabra.direccion == Direccion.HORIZONTAL) "HORIZONTAL" else "VERTICAL"

    Column(
        modifier
            .background(Color(0xF5120E0A), forma)
            .border(1.2.dp, estilo.metal, forma)
            .padding(4.dp)
            .border(1.dp, estilo.metal.copy(alpha = .35f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            "${numero ?: ""} $direccion",
            color = estilo.acento,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.8.sp
        )
        Text("${palabra.longitud} letras", color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 15.sp)
        Text(
            palabra.pista,
            color = Color(0xFFD9CDB5),
            fontFamily = FuenteTexto,
            fontSize = 15.5.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        if (resultado != null) {
            Text(
                when (resultado.estado) {
                    EstadoRespuesta.CORRECTA -> "¡Correcto! +${ActualizarPuntajeUseCase.PUNTOS_POR_ACIERTO} puntos."
                    EstadoRespuesta.INCOMPLETA -> "Incompleta. −${ActualizarPuntajeUseCase.PENALIZACION} puntos. Inténtalo de nuevo."
                    EstadoRespuesta.INCORRECTA -> "Incorrecta. −${ActualizarPuntajeUseCase.PENALIZACION} puntos. Inténtalo de nuevo."
                },
                color = if (resultado.estado == EstadoRespuesta.CORRECTA) OdinPaleta.Correcto else OdinPaleta.Incorrecto,
                fontFamily = FuenteTexto,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

/** Casilla de madera oscura con borde metálico. La actual se ilumina con el color del mundo. */
@Composable
private fun Casilla(
    letra: Char?,
    numero: Int?,
    lado: Dp,
    estilo: EstiloMundo,
    semilla: Int,
    esActual: Boolean,
    enSeleccion: Boolean,
    resuelta: Boolean,
    fallo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorLetra = when {
        esActual -> OdinPaleta.Hollin
        fallo -> OdinPaleta.Incorrecto
        resuelta -> ColorPergamino
        else -> OdinPaleta.Hueso
    }
    Box(
        modifier
            .size(lado)
            .drawBehind {
                val ins = 1.5.dp.toPx()
                val tl = Offset(ins, ins)
                val sz = Size(size.width - ins * 2, size.height - ins * 2)
                val radio = CornerRadius(2.dp.toPx())
                if (esActual) {
                    mancha(center, size.minDimension * .95f, estilo.acento, .55f)
                    drawRoundRect(
                        Brush.verticalGradient(listOf(estilo.metalClaro, estilo.acento), startY = tl.y, endY = tl.y + sz.height),
                        tl, sz, radio
                    )
                } else {
                    val arriba = if (resuelta) Color(0xFF3D2E1F) else Color(0xFF2A1F15)
                    val abajo = if (resuelta) Color(0xFF2C2117) else Color(0xFF1A130C)
                    drawRoundRect(Brush.verticalGradient(listOf(arriba, abajo), startY = tl.y, endY = tl.y + sz.height), tl, sz, radio)
                    // Vetas de la madera
                    val rnd = Random(semilla)
                    clipRect(tl.x, tl.y, tl.x + sz.width, tl.y + sz.height) {
                        repeat(4) {
                            val x = tl.x + rnd.nextFloat() * sz.width
                            val veta = Path().apply {
                                moveTo(x, tl.y)
                                quadraticTo(x + (rnd.nextFloat() - .5f) * 6f, center.y, x + (rnd.nextFloat() - .5f) * 4f, tl.y + sz.height)
                            }
                            drawPath(veta, Color.Black.copy(alpha = .35f), style = Stroke(.6.dp.toPx()))
                        }
                        drawRect(Color.White.copy(alpha = .06f), tl, Size(sz.width, 1.dp.toPx()))
                    }
                }
                val (colorBorde, grosor) = when {
                    fallo -> OdinPaleta.Incorrecto to 1.6.dp
                    esActual || enSeleccion -> estilo.acento to 1.6.dp
                    resuelta -> estilo.metal.copy(alpha = .8f) to 1.dp
                    else -> estilo.metal.copy(alpha = .4f) to 1.dp
                }
                drawRoundRect(colorBorde, tl, sz, radio, style = Stroke(grosor.toPx()))
            }
            .semantics { contentDescription = letra?.toString() ?: "vacía" }
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        if (numero != null) {
            Text(
                "$numero",
                color = if (esActual) OdinPaleta.Hollin.copy(alpha = .8f) else estilo.metal.copy(alpha = .9f),
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = (lado.value * .22f).coerceAtLeast(8f).sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 4.dp, top = 1.dp)
            )
        }
        if (letra != null) {
            Text(
                "$letra",
                color = colorLetra,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = (lado.value * .5f).sp,
                style = TextStyle(shadow = if (esActual) null else Shadow(Color.Black.copy(alpha = .8f), Offset(0f, 1f), 4f)),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Ayudas y teclado
// ---------------------------------------------------------------------------

/** Al presionar, la tecla se hunde un poco y se ilumina con el color del mundo. */
@Composable
private fun Modifier.presionable(acento: Color, habilitado: Boolean = true, onClick: () -> Unit): Modifier {
    val interaccion = remember { MutableInteractionSource() }
    val presionada by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionada) .94f else 1f, tween(80), label = "tecla")
    return this
        .graphicsLayer { scaleX = escala; scaleY = escala }
        .drawBehind { if (presionada) mancha(center, size.maxDimension * .7f, acento, .45f) }
        .clickable(interactionSource = interaccion, indication = null, enabled = habilitado, role = Role.Button, onClick = onClick)
}

@Composable
private fun RowScope.BotonAyuda(
    texto: String,
    runa: String,
    costo: Int,
    alcanza: Boolean,
    estilo: EstiloMundo,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .weight(1f)
            .height(46.dp)
            .alpha(if (habilitado) 1f else .4f)
            .presionable(estilo.acento, habilitado, onClick)
            .background(Color(0xF0110D0A), FormaFlecha)
            .border(1.2.dp, estilo.metal, FormaFlecha)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(runa, color = estilo.acento, fontSize = 15.sp)
        Spacer(Modifier.width(6.dp))
        BasicText(
            texto,
            style = TextStyle(color = OdinPaleta.Hueso, fontFamily = FuenteTexto),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 16.sp, stepSize = .5.sp),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(2.dp))
        IconoCuerno(tamano = 16.dp)
        Text(
            "$costo",
            color = if (alcanza) ColorHidromiel else OdinPaleta.Incorrecto,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

private val FILA_1 = "QWERTYUIOP".toList()
private val FILA_2 = "ASDFGHJKLÑ".toList()
private val FILA_3 = "ZXCVBNM".toList()
private val ESPACIO_TECLAS = 6.dp
private val ALTO_TECLA = 48.dp
private val FORMA_TECLA = RoundedCornerShape(10.dp)

/**
 * Teclado propio con Ñ. Las teclas tienen ancho fijo calculado del espacio
 * disponible, así las tres filas quedan parejas y centradas.
 */
@Composable
private fun Teclado(estilo: EstiloMundo, onLetra: (Char) -> Unit, onBorrar: () -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val ancho = (maxWidth - ESPACIO_TECLAS * 9) / 10
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilaTeclas(FILA_1, ancho, estilo, onLetra)
            FilaTeclas(FILA_2, ancho, estilo, onLetra)
            Row(horizontalArrangement = Arrangement.spacedBy(ESPACIO_TECLAS)) {
                Box(
                    Modifier
                        .size(ancho * 1.6f + ESPACIO_TECLAS, ALTO_TECLA)
                        .presionable(estilo.acento, onClick = onBorrar)
                        .fondoTecla(estilo)
                        .semantics { contentDescription = "Borrar" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⌫", color = estilo.acento, fontSize = 20.sp)
                }
                FilaTeclas(FILA_3, ancho, estilo, onLetra)
            }
        }
    }
}

@Composable
private fun FilaTeclas(letras: List<Char>, ancho: Dp, estilo: EstiloMundo, onLetra: (Char) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(ESPACIO_TECLAS)) {
        letras.forEach { letra ->
            Box(
                Modifier
                    .size(ancho, ALTO_TECLA)
                    .presionable(estilo.acento) { onLetra(letra) }
                    .fondoTecla(estilo),
                contentAlignment = Alignment.Center
            ) {
                Text("$letra", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontWeight = FontWeight.SemiBold, fontSize = 21.sp)
            }
        }
    }
}

private fun Modifier.fondoTecla(estilo: EstiloMundo): Modifier = this
    .background(Brush.verticalGradient(listOf(Color(0xFF2C2118), Color(0xFF16100B))), FORMA_TECLA)
    .border(1.dp, estilo.metal.copy(alpha = .55f), FORMA_TECLA)


/** Flujo alternativo A2.1: no se pudo generar el crucigrama. */
@Composable
private fun ErrorGeneracion(
    estilo: EstiloMundo,
    mensaje: String,
    onReintentar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .padding(24.dp)
            .widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("LA RUNA NO RESPONDE", style = estiloTitulo(24.sp), textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(mensaje, color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 16.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        BotonOdin(texto = "Reintentar", onClick = onReintentar, acento = estilo.acento, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        BotonOdin(texto = "Volver al roadmap", onClick = onVolver, acento = estilo.acento, primario = false, metal = estilo.metal, modifier = Modifier.fillMaxWidth())
    }
}
