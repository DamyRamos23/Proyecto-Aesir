package com.aesir.odin.ui.mundos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import com.aesir.odin.ui.theme.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.res.painterResource
import com.aesir.odin.R
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.racha.ContadorRacha
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.TextoAjustable
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
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import com.aesir.odin.domain.eastereggs.JuegoOculto
import com.aesir.odin.ui.eastereggs.MenuEastereggs
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.aesir.odin.domain.hidromiel.CombinacionSecreta
import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.ui.components.odin.rememberTiempo
import com.aesir.odin.ui.components.odin.PuntosProgreso
import com.aesir.odin.ui.moneda.CaraMoneda
import com.aesir.odin.ui.moneda.EscudoGiratorio
import com.aesir.odin.ui.moneda.rememberGiroEscudo
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle

/**
 * Pantalla que lista los mundos del roadmap como un escudo dividido en
 * fragmentos. Al entrar a un mundo navega a RoadmapScreen pasando su id;
 * es RoadmapScreen quien valida si ese nivel está desbloqueado, tal como lo
 * describe el diagrama de secuencia.
 */
@Composable
fun MundosScreen(
    navController: NavHostController,
    appContainer: AppContainer,
    onInvocarPreguntaFlash: () -> Unit = {}
) {
    val viewModel: MundosViewModel = viewModel(
        factory = MundosViewModel.factory(
            appContainer.obtenerMundosUseCase,
            appContainer.validarAccesoDiagnosticoUseCase
        )
    )
    val mundos by viewModel.mundos.collectAsState()
    val saldo by remember { appContainer.moduloHidromiel.observarSaldo(USUARIO_LOCAL) }.collectAsState(initial = 0)
    // MOD-05: contador de la racha; abre la pantalla de la carrera de Sól
    val racha by appContainer.moduloCarreraSol.estado.collectAsState()
    LaunchedEffect(Unit) { appContainer.moduloCarreraSol.resumen(USUARIO_LOCAL, java.time.LocalDate.now()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.cargarMundos()
    }

    var seleccionadoId by rememberSaveable { mutableStateOf<String?>(null) }
    val adminActivo by ModoAdmin.activo.collectAsState()
    /**
     * Combinación secreta del modo admin: tocar los cuatro mundos en el
     * sentido del reloj (arriba-izq → arriba-der → abajo-der → abajo-izq), dos
     * vueltas seguidas, sin pausas de más de 1.5 s. Lo mismo lo apaga.
     */
    val combinacion = remember { CombinacionSecreta(listOf(0, 1, 3, 2, 0, 1, 3, 2)) }
    var avisoAdmin by remember { mutableStateOf<String?>(null) }
    var editandoSaldo by remember { mutableStateOf(false) }
    // Easter eggs: una combinación por juego oculto y 7 toques al contador para el menú.
    val combinacionesJuegos = remember { JuegoOculto.entries.associateWith { CombinacionSecreta(it.combinacion) } }
    val sieteToques = remember { CombinacionSecreta(List(7) { 0 }) }
    var mostrarMenuJuegos by remember { mutableStateOf(false) }
    val abrirJuego: (JuegoOculto) -> Unit = { juego ->
        appContainer.almacenEastereggs.descubrir(juego)
        mostrarMenuJuegos = false
        navController.navigate(OdinNavGraph.rutaJuegoOcultoCon(juego))
    }
    if (mostrarMenuJuegos) {
        MenuEastereggs(
            descubiertos = remember { appContainer.almacenEastereggs.descubiertos() },
            todosVisibles = adminActivo,
            onElegir = { juego ->
                mostrarMenuJuegos = false
                navController.navigate(OdinNavGraph.rutaJuegoOcultoCon(juego))
            },
            onCerrar = { mostrarMenuJuegos = false }
        )
    }
    if (editandoSaldo) {
        var texto by remember { mutableStateOf(saldo.toString()) }
        AlertDialog(
            onDismissRequest = { editandoSaldo = false },
            title = { Text("Hidromiel (admin)") },
            text = {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { nuevo -> texto = nuevo.filter(Char::isDigit).take(6) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val cantidad = texto.toIntOrNull()
                    if (cantidad != null) scope.launch { appContainer.moduloHidromiel.establecerSaldoAdmin(USUARIO_LOCAL, cantidad) }
                    editandoSaldo = false
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editandoSaldo = false }) { Text("Cancelar") } }
        )
    }
    val vibrar = LocalHapticFeedback.current
    LaunchedEffect(avisoAdmin) {
        if (avisoAdmin != null) {
            delay(2500)
            avisoAdmin = null
        }
    }
    // El escudo es una moneda: cae del cielo al abrir la app y al girarlo muestra los casos de uso.
    val giro = rememberGiroEscudo()
    val caraActual = giro.cara
    val enMundos = caraActual == CaraMoneda.MUNDOS
    // Todo lo demás aparece cuando el escudo termina de caer.
    val alfaUi by animateFloatAsState(if (giro.listo) 1f else 0f, tween(450), label = "alfaUi")
    val visibles = mundos.take(4)
    val seleccionado = visibles.firstOrNull { it.id == seleccionadoId }
        ?: visibles.firstOrNull { it.desbloqueado }
        ?: visibles.firstOrNull()
    /** CU-10: la primera vez se presenta la evaluación diagnóstica del mundo. */
    val entrarACara: (CaraMoneda) -> Unit = { cara ->
        if (cara != CaraMoneda.MUNDOS) navController.navigate(
            when (cara) {
                CaraMoneda.VOLVA -> OdinNavGraph.rutaVolva
                CaraMoneda.HASTA_EL_ALBA -> OdinNavGraph.rutaAlba
                CaraMoneda.POR_REVELAR -> OdinNavGraph.rutaDuelo
                CaraMoneda.APUESTA_BROKKR -> OdinNavGraph.rutaForjaBrokkr
                else -> OdinNavGraph.rutaCaraMonedaCon(cara.name)
            }
        )
    }
    val entrar: (Mundo) -> Unit = { mundo ->
        scope.launch {
            val ruta = if (viewModel.intentarAbrirDiagnostico(mundo.id)) {
                OdinNavGraph.rutaEvaluacionDiagnosticaCon(mundo.id)
            } else {
                OdinNavGraph.rutaRoadmapCon(mundo.id)
            }
            navController.navigate(ruta)
        }
    }

    Box(Modifier.fillMaxSize()) {
    FondoMundo(estilo = null) {
        // Pantalla fija (sin desplazamiento): el escudo ocupa el espacio que sobra y se adapta a cada pantalla.
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(Modifier.fillMaxWidth().alpha(alfaUi), verticalAlignment = Alignment.CenterVertically) {
                TextoAjustable(
                    caraActual.titulo.uppercase(),
                    estiloTitulo(36.sp, brillo = if (enMundos) null else caraActual.acento),
                    // Ocupa todo el espacio libre de la fila (antes compartía la mitad con un Spacer y se cortaba).
                    Modifier.weight(1f),
                    minEscala = 0.5f,
                    maxLineas = 2
                )
                if (adminActivo) {
                    Text(
                        "ADMIN",
                        color = OdinPaleta.Hollin,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(OdinPaleta.Brasa)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
                if (adminActivo) {
                    // Modo admin: editar el hidromiel con un toque (además de mantener presionado el contador).
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Editar hidromiel",
                        tint = OdinPaleta.Hollin,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(OdinPaleta.Oro)
                            .clickable(role = Role.Button) { editandoSaldo = true }
                            .padding(7.dp)
                    )
                }
                if (adminActivo) {
                    // Modo admin: saca una pregunta flash a voluntad.
                    Icon(
                        painterResource(R.drawable.ic_rayo),
                        contentDescription = "Invocar pregunta flash",
                        tint = OdinPaleta.Hollin,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(OdinPaleta.Brasa)
                            .clickable(role = Role.Button, onClick = onInvocarPreguntaFlash)
                            .padding(6.dp)
                    )
                }
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Ajustes",
                    tint = OdinPaleta.Hueso,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button) { navController.navigate(OdinNavGraph.rutaAjustes) }
                        .padding(5.dp)
                )
                ContadorRacha(
                    estado = racha,
                    onClick = { navController.navigate(OdinNavGraph.rutaRacha) },
                    modifier = Modifier.padding(end = 8.dp)
                )
                // 7 toques: menú de easter eggs. Modo admin: mantener 3 s permite fijar el hidromiel.
                ContadorHidromiel(
                    saldo = saldo,
                    modifier = Modifier.pointerInput(adminActivo) {
                        awaitEachGesture {
                            awaitFirstDown()
                            val soltado = withTimeoutOrNull(PRESION_ADMIN_MS) { waitForUpOrCancellation() }
                            if (soltado == null) {
                                if (adminActivo) {
                                    vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
                                    editandoSaldo = true
                                }
                            } else if (sieteToques.registrar(0)) {
                                mostrarMenuJuegos = true
                            }
                        }
                    }
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (enMundos) "Cada mundo es un tema de Ingeniería de Software. Supera su Ragnarök para abrir el siguiente."
                else caraActual.subtitulo,
                color = if (enMundos) OdinPaleta.HuesoTenue else caraActual.acento,
                fontFamily = FuenteTexto,
                fontStyle = if (enMundos) FontStyle.Normal else FontStyle.Italic,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(alfaUi)
            )

            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
            val ladoEscudo = minOf(maxWidth, maxHeight, 440.dp)
            if (visibles.isNotEmpty()) {
                EscudoGiratorio(
                    giro = giro,
                    modifier = Modifier.size(ladoEscudo),
                    onTocarCara = { cara -> entrarACara(cara) }
                ) {
                EscudoMundos(
                    mundos = visibles,
                    seleccionadoId = seleccionado?.id,
                    onSeleccionar = { id ->
                        // Tocar otra vez el mundo ya elegido también entra (además del botón).
                        // Se compara con la selección explícita para no estorbar la combinación admin.
                        val tocado = visibles.firstOrNull { it.id == id }
                        if (id == seleccionadoId && tocado != null && tocado.desbloqueado) {
                            entrar(tocado)
                            return@EscudoMundos
                        }
                        seleccionadoId = id
                        val i = visibles.indexOfFirst { it.id == id }
                        // Todas las combinaciones escuchan cada toque (no se interrumpen entre sí).
                        val juego = combinacionesJuegos.entries.filter { (_, c) -> i >= 0 && c.registrar(i) }.firstOrNull()?.key
                        if (juego != null) {
                            vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
                            abrirJuego(juego)
                            return@EscudoMundos
                        }
                        if (i >= 0 && combinacion.registrar(i)) {
                            ModoAdmin.alternar()
                            viewModel.cargarMundos()
                            vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
                            avisoAdmin = if (ModoAdmin.estaActivo) "ᛟ Modo admin activado: todo abierto y gratis" else "Modo admin desactivado"
                        }
                    },
                    // CU-11: el reto mezcla preguntas de todos los temas ya
                    // estudiados, por eso se entra desde el centro del escudo.
                    onAbrirReto = { navController.navigate(OdinNavGraph.rutaRetoContrareloj) },
                    modifier = Modifier.fillMaxSize()
                )
                }
            }
            }
            Text(
                if (enMundos) "Valknut: reto contrarreloj · Arrastra el escudo para girarlo"
                else "Arrastra o toca el escudo para girarlo otra vez",
                color = OdinPaleta.HuesoTenue.copy(alpha = .8f),
                fontFamily = FuenteTexto,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().alpha(alfaUi)
            )
            Spacer(Modifier.height(10.dp))

            // El panel no desaparece al girar: se atenúa mientras el escudo se mueve
            // y su contenido se funde con el de la cara nueva al caer.
            // Ocupa siempre su lugar (alto fijo) para que el escudo no cambie de tamaño.
            val alfaPanel by animateFloatAsState(
                when {
                    !giro.listo -> 0f
                    giro.enMovimiento -> 0.35f
                    else -> 1f
                },
                tween(if (giro.enMovimiento) 250 else 500),
                label = "alfaPanel"
            )
            Box(Modifier.fillMaxWidth().height(84.dp).alpha(alfaPanel)) {
                AnimatedContent(
                    targetState = caraActual,
                    transitionSpec = {
                        (fadeIn(tween(400, delayMillis = 120)) togetherWith fadeOut(tween(220)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "panelCara"
                ) { cara ->
                    if (cara == CaraMoneda.MUNDOS) {
                        if (seleccionado != null) {
                            AnimatedContent(
                                targetState = seleccionado,
                                transitionSpec = { (fadeIn(tween(220)) togetherWith fadeOut(tween(120))).using(SizeTransform(clip = false)) },
                                contentKey = { it.id },
                                label = "detalleMundo"
                            ) { mundo ->
                                PanelMundo(
                                    mundo = mundo,
                                    anterior = visibles.getOrNull(visibles.indexOfFirst { it.id == mundo.id } - 1),
                                    enMovimiento = giro.enMovimiento,
                                    onEntrar = { if (giro.listo) entrar(mundo) }
                                )
                            }
                        }
                    } else {
                        PanelCasoUso(
                            cara = cara,
                            enMovimiento = giro.enMovimiento,
                            onEntrar = { if (giro.listo) entrarACara(cara) }
                        )
                    }
                }
            }

        }
    }
    AnimatedVisibility(
        visible = avisoAdmin != null,
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut()
    ) {
        Text(
            avisoAdmin.orEmpty(),
            color = OdinPaleta.Hollin,
            fontFamily = FuenteTexto,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            modifier = Modifier
                .clip(CircleShape)
                .background(OdinPaleta.Brasa)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
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
    onAbrirReto: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Latido del nivel oculto: halo, aro que brilla y una onda que se abre
    val tiempo = rememberTiempo()
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

        Canvas(
            Modifier
                .align(Alignment.Center)
                .size(lado * 0.22f * 1.6f)
        ) {
            val fase = (tiempo.value % 2.6f) / 2.6f
            val tri = if (fase <= 0.5f) fase * 2f else 2f - fase * 2f
            val r = size.minDimension / 2f / 1.6f
            drawCircle(
                Brush.radialGradient(
                    0.55f to OdinPaleta.Oro.copy(alpha = 0f),
                    0.7f to OdinPaleta.Oro.copy(alpha = 0.45f * (0.4f + 0.6f * tri)),
                    1f to OdinPaleta.Oro.copy(alpha = 0f),
                    center = center, radius = r * 1.45f
                ),
                r * 1.45f, blendMode = BlendMode.Plus
            )
            drawCircle(OdinPaleta.Brasa.copy(alpha = 0.8f * (1f - fase)), r * (1f + 0.45f * fase), style = Stroke(1.6.dp.toPx()))
        }
        Canvas(
            Modifier
                .align(Alignment.Center)
                .size(lado * 0.22f)
                .clip(CircleShape)
                .semantics { contentDescription = "Reto contrarreloj" }
                .clickable(role = Role.Button, onClick = onAbrirReto)
        ) {
            val r = size.minDimension / 2f
            drawCircle(Color.Black.copy(alpha = .6f), r * 1.08f, center + Offset(0f, 3.dp.toPx()))
            drawCircle(Brush.radialGradient(listOf(Color(0xFF2E271F), Color(0xFF12100D)), center = center, radius = r), r)
            drawCircle(OdinPaleta.Hierro, r - 1.5.dp.toPx(), style = Stroke(3.dp.toPx()))
            val fase = (tiempo.value % 2.6f) / 2.6f
            val tri = if (fase <= 0.5f) fase * 2f else 2f - fase * 2f
            drawCircle(OdinPaleta.Brasa.copy(alpha = 0.45f + 0.55f * tri), r - 7.dp.toPx(), style = Stroke(1.8.dp.toPx()))
            dibujarValknut(center, r * .5f, OdinPaleta.Oro, 1.6.dp.toPx())
            dibujarValknut(center, r * .5f, OdinPaleta.Brasa.copy(alpha = 0.35f * tri), 1.6.dp.toPx())
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
        // Cargado hacia el centro del escudo, donde el cuarto de círculo es más ancho y no recorta el nombre.
        Cuadrante.SUPERIOR_IZQ -> BiasAlignment(0.45f, 0.45f)
        Cuadrante.SUPERIOR_DER -> BiasAlignment(-0.45f, 0.45f)
        Cuadrante.INFERIOR_IZQ -> BiasAlignment(0.45f, -0.75f)
        Cuadrante.INFERIOR_DER -> BiasAlignment(-0.45f, -0.75f)
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
            Modifier.align(alineacion).fillMaxWidth(0.55f),
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
            TextoAjustable(
                mundo.nombre,
                estiloTitulo(13.sp, if (bloqueado) Color(0xFFD4E4EE) else OdinPaleta.Hueso).copy(textAlign = TextAlign.Center),
                Modifier.fillMaxWidth(),
                minEscala = 0.7f,
                maxLineas = 2
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

/**
 * Panel compacto (alto fijo) para que la pantalla no brinque al cambiar de mundo
 * o de cara. Si la descripción no cabe, al tocarlo se despliega hacia arriba
 * (encima del escudo, sin mover nada) con el texto completo; otro toque lo cierra.
 */
@Composable
private fun PanelCompacto(
    titulo: String,
    texto: String,
    metal: Color,
    acento: Color,
    brillo: Color?,
    bloqueado: Boolean,
    enMovimiento: Boolean,
    onEntrar: (() -> Unit)?
) {
    val forma = CutCornerShape(8.dp)
    var recortado by remember(texto) { mutableStateOf(false) }
    var expandido by remember(titulo, texto) { mutableStateOf(false) }
    // Al girar el escudo se cierra la descripción completa
    LaunchedEffect(enMovimiento) { if (enMovimiento) expandido = false }
    Box(Modifier.fillMaxWidth().height(84.dp)) {
        Row(
            Modifier
                .fillMaxSize()
                .clip(forma)
                .background(Color(0xE61E1A15))
                .border(1.dp, metal.copy(alpha = .7f), forma)
                .padding(start = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .clickable(enabled = recortado, onClickLabel = "Ver descripción completa") { expandido = true }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextoAjustable(
                        titulo.uppercase(),
                        estiloTitulo(17.sp, if (bloqueado) OdinPaleta.Escarcha else OdinPaleta.Hueso, brillo = brillo),
                        Modifier.weight(1f, fill = false),
                        minEscala = 0.7f,
                        maxLineas = 1
                    )
                    if (recortado) {
                        Spacer(Modifier.width(6.dp))
                        Text("Leer más ▴", color = acento.copy(alpha = .9f), fontFamily = FuenteTexto, fontSize = 11.sp, maxLines = 1)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (bloqueado) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = OdinPaleta.Escarcha, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(
                        texto,
                        color = if (bloqueado) OdinPaleta.Escarcha else OdinPaleta.HuesoTenue,
                        fontFamily = FuenteTexto,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { if (it.hasVisualOverflow) recortado = true }
                    )
                }
            }
            if (onEntrar != null) {
                Spacer(Modifier.width(10.dp))
                BotonOdin(
                    texto = "Entrar",
                    onClick = onEntrar,
                    acento = acento,
                    metal = metal,
                    iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.width(140.dp)
                )
            }
        }
        // Descripción completa: crece hacia arriba desde el panel, encima del escudo
        AnimatedVisibility(
            visible = expandido,
            enter = fadeIn(tween(220)) + expandVertically(tween(260), expandFrom = Alignment.Bottom),
            exit = fadeOut(tween(160)) + shrinkVertically(tween(200), shrinkTowards = Alignment.Bottom),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Bottom, unbounded = true)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(forma)
                    .background(Color(0xF51E1A15))
                    .border(1.dp, metal.copy(alpha = .8f), forma)
                    .clickable(onClickLabel = "Cerrar descripción") { expandido = false }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        titulo.uppercase(),
                        style = estiloTitulo(17.sp, if (bloqueado) OdinPaleta.Escarcha else OdinPaleta.Hueso, brillo = brillo),
                        modifier = Modifier.weight(1f)
                    )
                    Text("Cerrar ▾", color = acento.copy(alpha = .9f), fontFamily = FuenteTexto, fontSize = 11.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    texto,
                    color = if (bloqueado) OdinPaleta.Escarcha else OdinPaleta.HuesoTenue,
                    fontFamily = FuenteTexto,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                if (onEntrar != null) {
                    Spacer(Modifier.height(12.dp))
                    BotonOdin(
                        texto = "Entrar",
                        onClick = { expandido = false; onEntrar() },
                        acento = acento,
                        metal = metal,
                        iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PanelMundo(mundo: Mundo, anterior: Mundo?, enMovimiento: Boolean, onEntrar: () -> Unit) {
    val estilo = EstilosMundo.porOrden(mundo.orden)
    PanelCompacto(
        titulo = mundo.nombre,
        texto = if (mundo.desbloqueado) mundo.descripcion
        else if (anterior != null) "Se abre al superar el Ragnarök de ${anterior.nombre}." else "Este mundo todavía está bloqueado.",
        metal = if (mundo.desbloqueado) estilo.metal else OdinPaleta.Hierro,
        acento = estilo.acento,
        brillo = null,
        bloqueado = !mundo.desbloqueado,
        enMovimiento = enMovimiento,
        onEntrar = if (mundo.desbloqueado) onEntrar else null
    )
}

/** Panel de una cara de caso de uso del escudo (en lugar del panel del mundo). */
@Composable
private fun PanelCasoUso(cara: CaraMoneda, enMovimiento: Boolean, onEntrar: () -> Unit) {
    PanelCompacto(
        titulo = cara.titulo,
        texto = cara.descripcion,
        metal = cara.metal,
        acento = cara.acento,
        brillo = cara.acento,
        bloqueado = false,
        enMovimiento = enMovimiento,
        onEntrar = onEntrar
    )
}

/** Mantener presionado el contador este tiempo abre el editor de hidromiel (modo admin). */
private const val PRESION_ADMIN_MS = 3_000L
