package com.aesir.odin.ui.retoarrastrar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.aesir.odin.domain.model.ElementoArrastrable
import com.aesir.odin.domain.model.ModalidadReto
import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.model.RetoArrastrar
import com.aesir.odin.domain.usecase.retoarrastrar.RegistrarErrorRetoArrastrarUseCase
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.MarcadorErrores
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * CU-09 Resolver reto de arrastrar y soltar.
 *
 * El alumno arrastra cada "piedra rúnica" (una etapa del proceso o una pieza
 * del diagrama) a su lugar; también puede tocar una piedra y luego el lugar.
 * Al comprobar, las mal colocadas se marcan y tiemblan (A7.1); con tres
 * intentos fallidos el reto termina y se muestra la solución (A7.2). Si se
 * aprueba, se pasa al resumen (paso 9).
 *
 * @param temasIncluidos lecciones cercanas en el árbol; la primera es la que se sugiere repasar.
 * @param onAprobado resultado, nombres de los niveles que se abrieron y orden del mundo.
 * @param onSalir el alumno confirmó que quiere abandonar (A4.1).
 */
@Composable
fun RetoArrastrarScreen(
    viewModel: RetoArrastrarViewModel,
    temaId: String,
    temasIncluidos: List<String>,
    onAprobado: (ResultadoReto, List<String>, Int) -> Unit,
    onVolverRoadmap: () -> Unit,
    onRepasarLeccion: (String) -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reto by viewModel.reto.collectAsState()
    val colocaciones by viewModel.colocaciones.collectAsState()
    val puntaje by viewModel.puntaje.collectAsState()
    val intentosFallidos by viewModel.intentosFallidos.collectAsState()
    val incorrectos by viewModel.elementosIncorrectos.collectAsState()
    val resultado by viewModel.resultadoFinal.collectAsState()
    val confirmarSalida by viewModel.mostrarConfirmacionSalida.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val desbloqueados by viewModel.desbloqueados.collectAsState()
    val comprobando by viewModel.comprobando.collectAsState()
    val aviso by viewModel.aviso.collectAsState()
    val saldo by viewModel.saldo.collectAsState()

    LaunchedEffect(temaId) { viewModel.cargarReto(temaId) }

    val enCurso = reto != null && resultado == null
    BackHandler(enabled = enCurso) { viewModel.solicitarSalida() }
    val salir: () -> Unit = {
        if (enCurso) viewModel.solicitarSalida() else onVolverRoadmap()
    }

    val ordenMundo = contexto?.mundo?.orden ?: 1
    val estilo = EstilosMundo.porOrden(ordenMundo)

    // Paso 9: tras un momento de festejo se pasa al resumen.
    LaunchedEffect(resultado) {
        val r = resultado ?: return@LaunchedEffect
        if (r.aprobado) {
            delay(1200)
            onAprobado(r, desbloqueados, ordenMundo)
        }
    }

    // A7.1: las piedras mal colocadas tiemblan.
    val temblor = remember { Animatable(0f) }
    LaunchedEffect(intentosFallidos) {
        if (intentosFallidos > 0) {
            temblor.snapTo(0f)
            temblor.animateTo(0f, keyframes {
                durationMillis = 420
                -9f at 50
                8f at 120
                -6f at 190
                4f at 270
                -2f at 340
            })
        }
    }

    val arrastre = remember { EstadoArrastre() }
    var seleccionado by remember { mutableStateOf<String?>(null) }

    FondoMundo(estilo = if (contexto != null) estilo else null, alturaPaisaje = 300.dp, modifier = modifier) {
        Box(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { arrastre.raiz = it.positionInRoot() }
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState(), enabled = arrastre.elementoId == null)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BotonVolver(contexto?.mundo?.nombre ?: "Roadmap", onClick = salir)
                    Spacer(Modifier.weight(1f))
                    ContadorHidromiel(saldo = saldo, metal = estilo.metal)
                }

                val retoActual = reto
                when {
                    mensajeError != null -> PanelMensaje(estilo, mensajeError.orEmpty(), onVolverRoadmap)
                    retoActual == null -> Box(Modifier.fillMaxWidth().height(320.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = estilo.acento)
                    }
                    else -> {
                        val terminado = resultado != null
                        val aprobado = resultado?.aprobado == true
                        val habilitado = !terminado && !comprobando
                        val estadoDe: (String) -> EstadoPiedra = { id ->
                            when {
                                aprobado -> EstadoPiedra.CORRECTA
                                terminado -> EstadoPiedra.SOLUCION
                                id in incorrectos -> EstadoPiedra.INCORRECTA
                                id == seleccionado -> EstadoPiedra.SELECCIONADA
                                else -> EstadoPiedra.NORMAL
                            }
                        }
                        val runaDe: (ElementoArrastrable) -> String = { e ->
                            estilo.runas[retoActual.elementos.indexOf(e).coerceAtLeast(0) % estilo.runas.size]
                        }
                        val tocarPiedra: (String) -> Unit = { id ->
                            if (habilitado) seleccionado = if (seleccionado == id) null else id
                        }
                        val tocarHueco: (String) -> Unit = { posicionId ->
                            if (habilitado) {
                                val elegido = seleccionado
                                val ocupante = colocaciones.entries.firstOrNull { it.value == posicionId }?.key
                                when {
                                    elegido != null -> {
                                        viewModel.colocarElemento(elegido, posicionId)
                                        seleccionado = null
                                    }
                                    ocupante != null -> seleccionado = ocupante
                                }
                            }
                        }
                        val soltar: (String, Offset) -> Unit = { id, puntero ->
                            val hueco = arrastre.huecoBajo(puntero)
                            when {
                                hueco != null -> viewModel.colocarElemento(id, hueco)
                                arrastre.banco.contains(puntero) -> viewModel.colocarElemento(id, null)
                            }
                            seleccionado = null
                        }

                        Cabecera(retoActual, contexto?.tema?.nombre, estilo)
                        Spacer(Modifier.height(10.dp))
                        Marcador(puntaje, intentosFallidos, estilo)
                        Spacer(Modifier.height(14.dp))

                        when (retoActual.modalidad) {
                            ModalidadReto.ORDENAR_PROCESO -> OrdenarProcesoContent(
                                reto = retoActual, colocaciones = colocaciones, estilo = estilo, arrastre = arrastre,
                                habilitado = habilitado, temblor = temblor.value, estadoDe = estadoDe, runaDe = runaDe,
                                onTocarHueco = tocarHueco, onTocarPiedra = tocarPiedra, onSoltar = soltar
                            )
                            ModalidadReto.COMPLETAR_DIAGRAMA -> CompletarDiagramaContent(
                                reto = retoActual, colocaciones = colocaciones, estilo = estilo, arrastre = arrastre,
                                habilitado = habilitado, temblor = temblor.value, estadoDe = estadoDe, runaDe = runaDe,
                                onTocarHueco = tocarHueco, onTocarPiedra = tocarPiedra, onSoltar = soltar
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        Retroalimentacion(
                            incorrectos = incorrectos.size,
                            intentosFallidos = intentosFallidos,
                            aprobado = aprobado,
                            terminado = terminado
                        )

                        if (!terminado) {
                            val pendientes = retoActual.elementos.filter { it.id !in colocaciones }
                            Spacer(Modifier.height(10.dp))
                            Banco(
                                pendientes = pendientes, estilo = estilo, arrastre = arrastre,
                                habilitado = habilitado, temblor = temblor.value, estadoDe = estadoDe, runaDe = runaDe,
                                ordenar = retoActual.modalidad == ModalidadReto.ORDENAR_PROCESO,
                                seleccionado = seleccionado,
                                onTocarBanco = {
                                    seleccionado?.let { id ->
                                        if (id in colocaciones) viewModel.colocarElemento(id, null)
                                        seleccionado = null
                                    }
                                },
                                onTocarPiedra = tocarPiedra, onSoltar = soltar
                            )
                            Spacer(Modifier.height(16.dp))
                            BotonOdin(
                                texto = if (comprobando) "Comprobando…" else "Comprobar",
                                onClick = { seleccionado = null; viewModel.confirmarRespuesta() },
                                acento = estilo.acento,
                                habilitado = pendientes.isEmpty() && habilitado,
                                iconoFinal = Icons.Filled.Check,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (pendientes.isNotEmpty()) {
                                Text(
                                    "Coloca todas las piedras para poder comprobar.",
                                    color = OdinPaleta.HuesoTenue,
                                    fontFamily = FuenteTexto,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                )
                            }
                        } else if (!aprobado) {
                            Spacer(Modifier.height(14.dp))
                            PanelLimite(
                                estilo = estilo,
                                hayLeccion = temasIncluidos.isNotEmpty(),
                                onRepasar = {
                                    temasIncluidos.firstOrNull()?.let { viewModel.repasarLeccion(it, onRepasarLeccion) }
                                },
                                onVolver = onVolverRoadmap
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            // Piedra que va siguiendo al dedo
            val idArrastrado = arrastre.elementoId
            val elementoArrastrado = reto?.elementos?.firstOrNull { it.id == idArrastrado }
            if (elementoArrastrado != null) {
                val densidad = LocalDensity.current
                val ancho = with(densidad) { arrastre.tamano.width.toDp() }
                val alto = with(densidad) { arrastre.tamano.height.toDp() }
                val indice = reto?.elementos?.indexOf(elementoArrastrado) ?: 0
                Box(
                    Modifier
                        .offset {
                            val p = arrastre.puntero - arrastre.agarre - arrastre.raiz
                            IntOffset(p.x.roundToInt(), p.y.roundToInt())
                        }
                        .size(ancho, alto)
                        .graphicsLayer {
                            scaleX = 1.07f
                            scaleY = 1.07f
                            rotationZ = -2.5f
                            shadowElevation = 14.dp.toPx()
                            shape = CutCornerShape(7.dp)
                        }
                ) {
                    PiedraRunica(
                        texto = elementoArrastrado.texto,
                        runa = estilo.runas[indice.coerceAtLeast(0) % estilo.runas.size],
                        estado = EstadoPiedra.SELECCIONADA,
                        estilo = estilo,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarSalida,
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Hueso,
            textContentColor = OdinPaleta.Hueso,
            title = { Text("¿Abandonar el reto?", fontFamily = FuenteTitulo) },
            text = {
                Text(
                    "Si sales ahora perderás el progreso del reto: no obtendrás puntaje y no quedará completado. " +
                        "Si pagaste entrada de hidromiel, se pierde.",
                    fontFamily = FuenteTexto
                )
            },
            confirmButton = {
                TextButton(onClick = onSalir) { Text("Salir", color = OdinPaleta.Incorrecto, fontFamily = FuenteTitulo) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarSalida) { Text("Continuar", color = estilo.acento, fontFamily = FuenteTitulo) }
            }
        )
    }

    aviso?.let { texto ->
        AlertDialog(
            onDismissRequest = viewModel::limpiarAviso,
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Hueso,
            textContentColor = OdinPaleta.Hueso,
            title = { Text("No se pudo abrir la lección", fontFamily = FuenteTitulo) },
            text = { Text(texto, fontFamily = FuenteTexto) },
            confirmButton = {
                TextButton(onClick = viewModel::limpiarAviso) { Text("Entendido", color = estilo.acento, fontFamily = FuenteTitulo) }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Arrastre
// ---------------------------------------------------------------------------

/** Lo que se está arrastrando y dónde están los lugares para soltarlo, en coordenadas de la raíz. */
@Stable
private class EstadoArrastre {
    var elementoId by mutableStateOf<String?>(null)
        private set
    var puntero by mutableStateOf(Offset.Zero)
    var agarre by mutableStateOf(Offset.Zero)
        private set
    var tamano by mutableStateOf(IntSize.Zero)
        private set
    var raiz by mutableStateOf(Offset.Zero)
    val huecos = mutableStateMapOf<String, Rect>()
    var banco by mutableStateOf(Rect.Zero)

    fun iniciar(id: String, inicio: Offset, agarre: Offset, tamano: IntSize) {
        elementoId = id
        puntero = inicio
        this.agarre = agarre
        this.tamano = tamano
    }

    fun terminar() {
        elementoId = null
    }

    fun huecoBajo(p: Offset): String? = huecos.entries.firstOrNull { it.value.inflate(8f).contains(p) }?.key

    /** Hueco sobre el que está la piedra ahora (para iluminarlo). */
    val huecoResaltado: String? get() = if (elementoId == null) null else huecoBajo(puntero)

    val sobreBanco: Boolean get() = elementoId != null && banco.contains(puntero)
}

private enum class EstadoPiedra { NORMAL, SELECCIONADA, INCORRECTA, CORRECTA, SOLUCION }

/** Piedra que se puede arrastrar o tocar para elegirla. */
@Composable
private fun PiedraArrastrable(
    elemento: ElementoArrastrable,
    runa: String,
    estado: EstadoPiedra,
    estilo: EstiloMundo,
    arrastre: EstadoArrastre,
    habilitada: Boolean,
    temblor: Float,
    onTocar: () -> Unit,
    onSoltar: (String, Offset) -> Unit,
    modifier: Modifier = Modifier,
    /** true si la piedra llena el lugar que la contiene; false para que mida lo que su texto. */
    llenar: Boolean = true
) {
    var origen by remember { mutableStateOf(Offset.Zero) }
    var tamano by remember { mutableStateOf(IntSize.Zero) }
    val soltar by rememberUpdatedState(onSoltar)
    val arrastrandoEsta = arrastre.elementoId == elemento.id
    Box(
        modifier
            .onGloballyPositioned {
                origen = it.positionInRoot()
                tamano = it.size
            }
            .graphicsLayer {
                alpha = if (arrastrandoEsta) 0.22f else 1f
                translationX = if (estado == EstadoPiedra.INCORRECTA) temblor * density else 0f
            }
            .pointerInput(elemento.id, habilitada) {
                if (!habilitada) return@pointerInput
                detectDragGestures(
                    onDragStart = { o -> arrastre.iniciar(elemento.id, origen + o, o, tamano) },
                    onDragEnd = {
                        soltar(elemento.id, arrastre.puntero)
                        arrastre.terminar()
                    },
                    onDragCancel = { arrastre.terminar() },
                    onDrag = { cambio, d ->
                        cambio.consume()
                        arrastre.puntero += d
                    }
                )
            }
            .clickable(enabled = habilitada, role = Role.Button, onClick = onTocar)
            .semantics { contentDescription = "Piedra: ${elemento.texto}" }
    ) {
        PiedraRunica(
            elemento.texto, runa, estado, estilo,
            if (llenar) Modifier.fillMaxSize() else Modifier.height(40.dp),
            compacta = !llenar
        )
    }
}

/** Tablilla de piedra con su runa y el texto del elemento. */
@Composable
private fun PiedraRunica(
    texto: String,
    runa: String,
    estado: EstadoPiedra,
    estilo: EstiloMundo,
    modifier: Modifier = Modifier,
    compacta: Boolean = false
) {
    val borde by animateColorAsState(
        when (estado) {
            EstadoPiedra.NORMAL -> estilo.metal.copy(alpha = .75f)
            EstadoPiedra.SELECCIONADA -> estilo.acento
            EstadoPiedra.INCORRECTA -> OdinPaleta.Incorrecto
            EstadoPiedra.CORRECTA, EstadoPiedra.SOLUCION -> OdinPaleta.Correcto
        },
        label = "bordePiedra"
    )
    val brillo by animateFloatAsState(
        when (estado) {
            EstadoPiedra.SELECCIONADA, EstadoPiedra.CORRECTA -> 1f
            EstadoPiedra.INCORRECTA -> .8f
            else -> 0f
        },
        label = "brilloPiedra"
    )
    val forma = CutCornerShape(7.dp)
    Row(
        modifier
            .drawBehind {
                if (brillo > 0f) mancha(center, size.maxDimension * .62f, borde, .22f * brillo)
            }
            .clip(forma)
            .background(Brush.verticalGradient(listOf(Color(0xFF3A332B), Color(0xFF1C1814))))
            .background(
                when (estado) {
                    EstadoPiedra.INCORRECTA -> OdinPaleta.Incorrecto.copy(alpha = .16f)
                    EstadoPiedra.CORRECTA, EstadoPiedra.SOLUCION -> OdinPaleta.Correcto.copy(alpha = .12f)
                    else -> Color.Transparent
                }
            )
            .border(if (estado == EstadoPiedra.NORMAL) 1.dp else 2.dp, borde, forma)
            .padding(horizontal = if (compacta) 8.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            runa,
            color = if (estado == EstadoPiedra.NORMAL) estilo.metal else borde,
            fontSize = if (compacta) 13.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = if (compacta) 6.dp else 8.dp)
        )
        Text(
            texto,
            color = OdinPaleta.Hueso,
            fontFamily = FuenteTexto,
            fontSize = if (compacta) 12.sp else 14.sp,
            lineHeight = if (compacta) 14.sp else 17.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

/** Lugar vacío donde se suelta una piedra. */
@Composable
private fun HuecoVacio(
    texto: String,
    estilo: EstiloMundo,
    resaltado: Boolean,
    modifier: Modifier = Modifier
) {
    val escala by animateFloatAsState(if (resaltado) 1.04f else 1f, spring(dampingRatio = .5f), label = "hueco")
    val color = if (resaltado) estilo.acento else estilo.metal.copy(alpha = .55f)
    Box(
        modifier
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .drawBehind {
                val r = 6.dp.toPx()
                drawRect(Color.Black.copy(alpha = if (resaltado) .45f else .32f))
                if (resaltado) mancha(center, size.maxDimension * .6f, estilo.acento, .25f)
                drawRect(
                    color,
                    style = Stroke(
                        (if (resaltado) 2f else 1.2f) * density,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(r, r * .7f))
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = color,
            fontFamily = FuenteTexto,
            fontStyle = FontStyle.Italic,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

/** Registra los límites de un hueco en coordenadas de la raíz. */
private fun Modifier.hueco(id: String, arrastre: EstadoArrastre): Modifier =
    onGloballyPositioned { arrastre.huecos[id] = Rect(it.positionInRoot(), it.size.toSize()) }

// ---------------------------------------------------------------------------
// Modalidades
// ---------------------------------------------------------------------------

private val NUMEROS_ROMANOS = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X")

/** ORDENAR_PROCESO: posiciones numeradas, de la primera a la última etapa. */
@Composable
private fun OrdenarProcesoContent(
    reto: RetoArrastrar,
    colocaciones: Map<String, String>,
    estilo: EstiloMundo,
    arrastre: EstadoArrastre,
    habilitado: Boolean,
    temblor: Float,
    estadoDe: (String) -> EstadoPiedra,
    runaDe: (ElementoArrastrable) -> String,
    onTocarHueco: (String) -> Unit,
    onTocarPiedra: (String) -> Unit,
    onSoltar: (String, Offset) -> Unit
) {
    val porPosicion = colocaciones.entries.associate { (elemento, posicion) -> posicion to elemento }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        reto.posiciones.sortedBy { it.orden }.forEachIndexed { i, posicion ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Número de la etapa, grabado en un círculo; la línea une las etapas
                Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val r = size.minDimension / 2f
                        drawCircle(Color.Black.copy(alpha = .55f), r)
                        drawCircle(estilo.metal, r, style = Stroke(1.5.dp.toPx()))
                    }
                    Text(
                        NUMEROS_ROMANOS.getOrElse(i) { "${i + 1}" },
                        color = estilo.acento,
                        fontFamily = FuenteTitulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                val elementoId = porPosicion[posicion.id]
                val elemento = reto.elementos.firstOrNull { it.id == elementoId }
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .hueco(posicion.id, arrastre)
                        .clickable(enabled = habilitado, role = Role.Button) { onTocarHueco(posicion.id) }
                ) {
                    if (elemento != null) {
                        PiedraArrastrable(
                            elemento = elemento, runa = runaDe(elemento), estado = estadoDe(elemento.id), estilo = estilo,
                            arrastre = arrastre, habilitada = habilitado, temblor = temblor,
                            onTocar = { onTocarPiedra(elemento.id) }, onSoltar = onSoltar,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        HuecoVacio(
                            "Etapa ${i + 1}",
                            estilo,
                            resaltado = arrastre.huecoResaltado == posicion.id,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/** COMPLETAR_DIAGRAMA: el diagrama incompleto con sus huecos. */
@Composable
private fun CompletarDiagramaContent(
    reto: RetoArrastrar,
    colocaciones: Map<String, String>,
    estilo: EstiloMundo,
    arrastre: EstadoArrastre,
    habilitado: Boolean,
    temblor: Float,
    estadoDe: (String) -> EstadoPiedra,
    runaDe: (ElementoArrastrable) -> String,
    onTocarHueco: (String) -> Unit,
    onTocarPiedra: (String) -> Unit,
    onSoltar: (String, Offset) -> Unit
) {
    val porPosicion = colocaciones.entries.associate { (elemento, posicion) -> posicion to elemento }
    val forma = CutCornerShape(10.dp)
    BoxWithConstraints(
        Modifier
            .widthIn(max = 460.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(forma)
            .background(Color(0xE6120F0C))
            .border(1.dp, estilo.metal, forma)
            .padding(6.dp)
    ) {
        val lado = maxWidth
        val altoHueco = 40.dp
        Canvas(Modifier.fillMaxSize()) {
            with(DiagramasReto) { dibujarDiagrama(reto.imagenDiagrama, estilo.metalClaro, estilo.acento, altoHueco.toPx()) }
        }
        // Lo que ya viene dado
        DiagramasReto.etiquetas(reto.imagenDiagrama).forEach { etiqueta ->
            val ancho = lado * etiqueta.ancho
            Box(
                Modifier
                    .offset(x = lado * etiqueta.x - ancho / 2, y = lado * etiqueta.y - altoHueco / 2)
                    .size(ancho, altoHueco),
                contentAlignment = Alignment.Center
            ) {
                if (etiqueta.piedra) {
                    PiedraFija(etiqueta.texto, estilo, Modifier.fillMaxSize())
                } else {
                    Text(
                        etiqueta.texto,
                        color = estilo.metalClaro,
                        fontFamily = if (etiqueta.negrita) FuenteTitulo else FuenteTexto,
                        fontWeight = if (etiqueta.negrita) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
        // Huecos
        reto.posiciones.forEach { posicion ->
            val ancho = lado * DiagramasReto.ANCHO_HUECO
            val x = lado * (posicion.x ?: 0.5f) - ancho / 2
            val y = lado * (posicion.y ?: 0.5f) - altoHueco / 2
            val elemento = reto.elementos.firstOrNull { it.id == porPosicion[posicion.id] }
            Box(
                Modifier
                    .offset(x = x, y = y)
                    .size(ancho, altoHueco)
                    .hueco(posicion.id, arrastre)
                    .clickable(enabled = habilitado, role = Role.Button) { onTocarHueco(posicion.id) }
            ) {
                if (elemento != null) {
                    PiedraArrastrable(
                        elemento = elemento, runa = runaDe(elemento), estado = estadoDe(elemento.id), estilo = estilo,
                        arrastre = arrastre, habilitada = habilitado, temblor = temblor,
                        onTocar = { onTocarPiedra(elemento.id) }, onSoltar = onSoltar,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    HuecoVacio(
                        posicion.etiqueta ?: "?",
                        estilo,
                        resaltado = arrastre.huecoResaltado == posicion.id,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/** Texto fijo del diagrama que se dibuja como piedra (no se puede mover). */
@Composable
private fun PiedraFija(texto: String, estilo: EstiloMundo, modifier: Modifier = Modifier) {
    val forma = CutCornerShape(7.dp)
    Box(
        modifier
            .clip(forma)
            .background(estilo.metalOscuro.copy(alpha = .55f))
            .border(1.dp, estilo.metal.copy(alpha = .6f), forma)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = estilo.metalClaro,
            fontFamily = FuenteTexto,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

// ---------------------------------------------------------------------------
// Banco, cabecera y mensajes
// ---------------------------------------------------------------------------

/** Piedras que faltan por colocar. También sirve para devolver una piedra. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Banco(
    pendientes: List<ElementoArrastrable>,
    estilo: EstiloMundo,
    arrastre: EstadoArrastre,
    habilitado: Boolean,
    temblor: Float,
    estadoDe: (String) -> EstadoPiedra,
    runaDe: (ElementoArrastrable) -> String,
    ordenar: Boolean,
    seleccionado: String?,
    onTocarBanco: () -> Unit,
    onTocarPiedra: (String) -> Unit,
    onSoltar: (String, Offset) -> Unit
) {
    val forma = CutCornerShape(10.dp)
    val resaltado = arrastre.sobreBanco
    Column(
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned { arrastre.banco = Rect(it.positionInRoot(), it.size.toSize()) }
            .clip(forma)
            .background(Color(0xD90C0A08))
            .border(1.dp, if (resaltado) estilo.acento else estilo.metal.copy(alpha = .6f), forma)
            .clickable(enabled = habilitado && seleccionado != null, onClick = onTocarBanco)
            .padding(12.dp)
    ) {
        Text(
            if (ordenar) "ETAPAS POR ORDENAR" else "PIEZAS POR COLOCAR",
            color = estilo.acento,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(8.dp))
        if (pendientes.isEmpty()) {
            Text(
                "Todas las piedras están en su lugar. Arrastra una aquí para quitarla.",
                color = OdinPaleta.HuesoTenue,
                fontFamily = FuenteTexto,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pendientes.forEach { elemento ->
                    androidx.compose.runtime.key(elemento.id) {
                        PiedraArrastrable(
                            elemento = elemento, runa = runaDe(elemento), estado = estadoDe(elemento.id), estilo = estilo,
                            arrastre = arrastre, habilitada = habilitado, temblor = temblor,
                            onTocar = { onTocarPiedra(elemento.id) }, onSoltar = onSoltar,
                            llenar = ordenar,
                            modifier = if (ordenar) Modifier.fillMaxWidth().height(46.dp)
                            else Modifier.widthIn(min = 96.dp, max = 240.dp).height(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cabecera(reto: RetoArrastrar, nombreTema: String?, estilo: EstiloMundo) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(38.dp).padding(top = 4.dp)) {
            val r = size.minDimension / 2f
            drawCircle(Color.Black.copy(alpha = .55f), r)
            drawCircle(estilo.metal, r, style = Stroke(1.5.dp.toPx()))
            // Dos piedras cruzadas: el ícono del arrastrar y soltar
            val d = r * .45f
            drawRect(estilo.acento, Offset(center.x - d * 1.1f, center.y - d * .9f), androidx.compose.ui.geometry.Size(d * 1.3f, d * .75f))
            drawRect(estilo.acento.copy(alpha = .6f), Offset(center.x - d * .2f, center.y + d * .15f), androidx.compose.ui.geometry.Size(d * 1.3f, d * .75f))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            when (reto.modalidad) {
                ModalidadReto.ORDENAR_PROCESO -> "RETO · ORDENAR EL PROCESO"
                ModalidadReto.COMPLETAR_DIAGRAMA -> "RETO · COMPLETAR EL DIAGRAMA"
            },
            color = estilo.acento,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )
        if (nombreTema != null) {
            Text(nombreTema, style = estiloTitulo(24.sp), textAlign = TextAlign.Center)
        }
        Text(
            reto.instrucciones,
            color = Color(0xFFD8CFBE),
            fontFamily = FuenteTexto,
            fontSize = 14.5.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** Puntaje del reto (empieza en 100) y los intentos fallidos. */
@Composable
private fun Marcador(puntaje: Int, intentosFallidos: Int, estilo: EstiloMundo) {
    val forma = CutCornerShape(8.dp)
    val puntos by animateFloatAsState(puntaje.toFloat(), tween(500), label = "puntaje")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Color(0xCC0C0A08))
            .border(1.dp, estilo.metal.copy(alpha = .6f), forma)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("PUNTAJE", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTitulo, fontSize = 11.sp, letterSpacing = 1.5.sp)
            Text(
                "${puntos.roundToInt()}",
                color = estilo.acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("INTENTOS FALLIDOS", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTitulo, fontSize = 11.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(4.dp))
            MarcadorErrores(intentosFallidos, RegistrarErrorRetoArrastrarUseCase.LIMITE_INTENTOS, estilo.metal)
        }
    }
}

@Composable
private fun Retroalimentacion(incorrectos: Int, intentosFallidos: Int, aprobado: Boolean, terminado: Boolean) {
    val texto = when {
        aprobado -> "¡Todo en su lugar! Las runas encajan."
        terminado -> "Se agotaron los intentos. Así queda la solución correcta."
        incorrectos > 0 -> {
            val quedan = RegistrarErrorRetoArrastrarUseCase.LIMITE_INTENTOS - intentosFallidos
            val piezas = if (incorrectos == 1) "1 piedra está mal colocada" else "$incorrectos piedras están mal colocadas"
            "$piezas (−20 puntos). Reacomódalas: " + if (quedan == 1) "te queda 1 intento." else "te quedan $quedan intentos."
        }
        else -> null
    }
    AnimatedVisibility(
        visible = texto != null,
        enter = fadeIn() + expandVertically() + scaleIn(initialScale = .95f),
        exit = fadeOut() + shrinkVertically()
    ) {
        val color = if (aprobado) OdinPaleta.Correcto else OdinPaleta.Incorrecto
        val forma = CutCornerShape(8.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(forma)
                .background(color.copy(alpha = .14f))
                .border(1.dp, color.copy(alpha = .8f), forma)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(10.dp))
            Text(texto.orEmpty(), color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 14.sp, lineHeight = 18.sp)
        }
    }
}

/** A7.2: el reto terminó sin puntaje; se sugiere repasar la lección del tema. */
@Composable
private fun PanelLimite(estilo: EstiloMundo, hayLeccion: Boolean, onRepasar: () -> Unit, onVolver: () -> Unit) {
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
            "RETO NO SUPERADO",
            color = OdinPaleta.Incorrecto,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp
        )
        Text("Las runas no encajaron", style = estiloTitulo(20.sp), modifier = Modifier.padding(top = 2.dp))
        Text(
            "Fallaste ${RegistrarErrorRetoArrastrarUseCase.LIMITE_INTENTOS} veces, así que el reto termina sin puntaje. " +
                "Revisa la solución de arriba" + if (hayLeccion) " y repasa la lección del tema antes de volver a intentarlo." else ".",
            color = Color(0xFFB8AE9C),
            fontFamily = FuenteTexto,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(14.dp))
        if (hayLeccion) {
            BotonOdin(
                texto = "Repasar lección",
                onClick = onRepasar,
                acento = estilo.acento,
                iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
        BotonOdin(
            texto = "Volver al roadmap",
            onClick = onVolver,
            acento = estilo.acento,
            primario = !hayLeccion,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PanelMensaje(estilo: EstiloMundo, mensaje: String, onVolver: () -> Unit) {
    val forma = CutCornerShape(10.dp)
    Column(
        Modifier
            .padding(top = 40.dp)
            .fillMaxWidth()
            .clip(forma)
            .background(Color(0xE6120F0C))
            .border(1.dp, estilo.metal, forma)
            .padding(16.dp)
    ) {
        Text("Reto no disponible", style = estiloTitulo(20.sp))
        Text(
            mensaje,
            color = Color(0xFFB8AE9C),
            fontFamily = FuenteTexto,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )
        BotonOdin(texto = "Volver al roadmap", onClick = onVolver, acento = estilo.acento, modifier = Modifier.fillMaxWidth())
    }
}
