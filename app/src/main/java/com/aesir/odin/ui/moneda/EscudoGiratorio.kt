package com.aesir.odin.ui.moneda

import com.aesir.odin.domain.sonido.ModuloSonido
import com.aesir.odin.domain.sonido.EventoSonido
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.components.odin.rememberTiempo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot

/** Un movimiento en curso: qué cara está arriba, cuál queda al final y si suelta chispas. */
class LanzamientoEscudo(
    val vuelo: MovimientoMoneda,
    val frente: Int,
    val reverso: Int,
    val semilla: Int,
    val chispas: Boolean = true,
    val caidaDelCielo: Boolean = false,
)

/**
 * Estado del escudo-moneda de Mundos: en qué cara está, si va volando y si ya
 * terminó la caída de entrada (hasta entonces no se muestra lo demás).
 */
@Stable
class GiroEscudo internal constructor(indiceInicial: Int) {
    val caras: List<CaraMoneda> = CaraMoneda.entries
    var indice by mutableIntStateOf(indiceInicial)
        internal set
    var lanzamiento by mutableStateOf<LanzamientoEscudo?>(null)
        internal set
    var pose by mutableStateOf(
        if (EstadoMoneda.entradaVista) PoseMoneda.Reposo else PoseMoneda(0f, -100_000f, 1f, 0f, -1f)
    )
        internal set
    var listo by mutableStateOf(EstadoMoneda.entradaVista)
        internal set
    internal var contador = 0
    /** true mientras el dedo tiene agarrado el escudo. */
    var arrastrando by mutableStateOf(false)
        internal set

    /** Cara en la que quedó el escudo. */
    val cara: CaraMoneda get() = caras[indice]
    val enVuelo: Boolean get() = lanzamiento != null
    /** Se está moviendo (volando o en la mano): el panel de abajo se atenúa. */
    val enMovimiento: Boolean get() = lanzamiento != null || arrastrando
    /** Cara de arriba y cara de abajo del escudo ahora mismo. */
    internal val indiceFrente: Int get() = lanzamiento?.frente ?: indice
    /** En la mano se asoma la siguiente cara si se voltea hacia abajo y la anterior si es hacia arriba. */
    internal val indiceReverso: Int
        get() = lanzamiento?.reverso ?: if (arrastrando) caraAlVoltear(indice, caras.size, pose.angulo) else indice

    companion object {
        val Saver: Saver<GiroEscudo, Int> = Saver(save = { it.indice }, restore = { GiroEscudo(it) })
    }
}

@Composable
fun rememberGiroEscudo(): GiroEscudo = rememberSaveable(saver = GiroEscudo.Saver) { GiroEscudo(0) }

/**
 * Convierte el escudo de Mundos en una moneda:
 * - al abrir la app cae del cielo dando vueltas y queda en la cara de Mundos;
 * - deslizándolo se lanza (más fuerza = más altura y más vueltas): hacia abajo
 *   cae en la siguiente cara y hacia arriba en la anterior;
 * - quieto, cada pocos segundos hace un amago de voltearse (pista de que se puede girar).
 *
 * [caraMundos] es el escudo de siempre (cuadrantes y Valknut);
 * las otras caras se dibujan con el mismo aro rúnico.
 */
@Composable
fun EscudoGiratorio(
    giro: GiroEscudo,
    modifier: Modifier = Modifier,
    /** Tocar (sin arrastrar) una cara de modo entra a ese modo; la moneda solo gira al arrastrarla. */
    onTocarCara: (CaraMoneda) -> Unit = {},
    caraMundos: @Composable () -> Unit
) {
    val densidad = LocalDensity.current
    val vibrar = LocalHapticFeedback.current
    val altoPantallaPx = with(densidad) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    var yEnRaiz by remember { mutableFloatStateOf(0f) }
    val medidor = rememberTextMeasurer()
    val runas = remember(medidor) {
        CaraMoneda.entries.associateWith { c -> c.runas.map { medidor.measure(it.toString(), TextStyle(fontSize = 13.sp)) } }
    }
    val escenas = rememberEscenasMoneda()
    /** Cuenta los toques sobre el escudo (para pausar el amago de pista). */
    var toques by remember { mutableIntStateOf(0) }
    val tiempo = rememberTiempo()

    BoxWithConstraints(
        modifier
            .aspectRatio(1f)
            .onGloballyPositioned { yEnRaiz = it.positionInRoot().y }
    ) {
        val ladoPx = constraints.maxWidth.toFloat()
        val radioPx = ladoPx / 2f

        // Caída del cielo al abrir la app (una vez por sesión)
        LaunchedEffect(Unit) {
            if (!EstadoMoneda.entradaVista && giro.lanzamiento == null) {
                giro.lanzamiento = LanzamientoEscudo(
                    VueloMoneda(
                        medioGiros = VueloMoneda.MEDIO_GIROS_ENTRADA,
                        alturaPx = altoPantallaPx,
                        rebotePx = ladoPx * 0.12f,
                        caidaDelCielo = true
                    ),
                    frente = 0, reverso = 0, semilla = 7, caidaDelCielo = true
                )
            }
        }

        // Si el escudo sale de pantalla con el dedo encima (cambio de pantalla, recarga de
        // mundos), el gesto se corta sin avisar: se suelta para que no quede trabado.
        DisposableEffect(Unit) {
            onDispose {
                if (giro.arrastrando) {
                    giro.arrastrando = false
                    giro.pose = PoseMoneda.Reposo
                }
            }
        }

        // Bucle de animación: la pose avanza cuadro a cuadro (FisicaMoneda)
        LaunchedEffect(giro.lanzamiento) {
            val activo = giro.lanzamiento ?: return@LaunchedEffect
            if (activo.vuelo is VueloMoneda) ModuloSonido.avisar(EventoSonido.GIRO_MONEDA) // MOD-02, Interfaz
            val inicio = withFrameNanos { it }
            var aterrizo = false
            while (true) {
                val t = withFrameNanos { (it - inicio) / 1_000_000_000f }
                giro.pose = activo.vuelo.pose(t)
                if (!aterrizo && t >= activo.vuelo.enElAire) {
                    aterrizo = true
                    if (activo.chispas) ModuloSonido.avisar(EventoSonido.CAIDA_MONEDA) // MOD-02, Interfaz
                    if (activo.chispas) vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                if (t >= activo.vuelo.duracion) break
            }
            if (activo.vuelo.cambiaCara) giro.indice = activo.reverso
            giro.pose = PoseMoneda.Reposo
            giro.lanzamiento = null
            EstadoMoneda.entradaVista = true
            giro.listo = true
        }

        // Amago de pista: siempre que la moneda está quieta, cada pocos segundos se
        // inclina un poco (abajo y arriba, alternando) para recordar que se puede voltear.
        // Cada toque reinicia la espera: el amago nunca mueve la moneda bajo el dedo.
        LaunchedEffect(giro.listo, giro.lanzamiento, giro.arrastrando, toques) {
            if (!giro.listo || giro.lanzamiento != null || giro.arrastrando) return@LaunchedEffect
            try {
                delay((PistaMoneda.PAUSA_INICIAL * 1000).toLong())
                var ciclo = 0
                while (true) {
                    val sentido = if (ciclo % 2 == 0) SENTIDO_SIGUIENTE else -SENTIDO_SIGUIENTE
                    val inicio = withFrameNanos { it }
                    while (true) {
                        val t = withFrameNanos { (it - inicio) / 1_000_000_000f }
                        if (t >= PistaMoneda.DURACION) break
                        giro.pose = PistaMoneda.pose(t, sentido)
                    }
                    giro.pose = PoseMoneda.Reposo
                    ciclo++
                    delay(((PistaMoneda.CADA - PistaMoneda.DURACION) * 1000).toLong())
                }
            } finally {
                // Si lo interrumpe el dedo, la pose ya es la del arrastre: no se toca.
                if (!giro.arrastrando && giro.lanzamiento == null) giro.pose = PoseMoneda.Reposo
            }
        }

        /** Lanza el escudo desde la pose actual (en reposo o desde la mano). */
        val lanzar: (Float, Float) -> Unit = lanzar@{ dpPorSegundo, direccion ->
            // Si no se puede lanzar, igual se suelta: nunca se queda "en la mano" para siempre.
            if (!giro.listo || giro.lanzamiento != null) { giro.arrastrando = false; return@lanzar }
            val desde = giro.pose
            val medios = VueloMoneda.medioGirosPorVelocidad(dpPorSegundo)
            // Sube lo que deje el espacio sobre el escudo (puede pasar un poco por encima del título)
            val techo = (yEnRaiz + ladoPx * 0.15f).coerceAtLeast(ladoPx * 0.4f)
            val altura = with(densidad) { (110 + 9 * medios).dp.toPx() }.coerceAtMost(techo)
            giro.contador++
            giro.arrastrando = false
            giro.lanzamiento = LanzamientoEscudo(
                VueloMoneda(
                    medios, altura, ladoPx * 0.08f,
                    anguloInicial = desde.angulo, alturaInicialPx = desde.alturaPx,
                    escalaInicial = desde.escala, direccion = direccion
                ),
                frente = giro.indice,
                reverso = caraAlVoltear(giro.indice, giro.caras.size, direccion),
                semilla = giro.contador
            )
        }
        val lanzarActual by rememberUpdatedState(lanzar)

        // Agarrar el escudo: se levanta poco a poco y se voltea con el dedo
        val arrastre = remember { floatArrayOf(0f, 0f) } // dx, dy acumulados en px
        val empezarArrastre: () -> Unit = {
            if (giro.listo && giro.lanzamiento == null) {
                arrastre[0] = 0f; arrastre[1] = 0f
                giro.arrastrando = true
            }
        }
        val moverArrastre: (Offset) -> Unit = mover@{ delta ->
            if (!giro.arrastrando) return@mover
            val antes = giro.pose.angulo
            arrastre[0] += delta.x; arrastre[1] += delta.y
            val dyDp = with(densidad) { arrastre[1].toDp().value }
            val distDp = with(densidad) { hypot(arrastre[0], arrastre[1]).toDp().value }
            val angulo = ArrastreMoneda.angulo(dyDp)
            giro.pose = PoseMoneda(
                angulo,
                -with(densidad) { ArrastreMoneda.alturaDp(distDp).dp.toPx() },
                ArrastreMoneda.escala(distDp),
                0f, 10f
            )
            // Un toquecito al pasar de canto (se asoma la otra cara)
            if ((abs(antes) <= 90f) != (abs(angulo) <= 90f)) vibrar.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        val soltar: (Float, Float) -> Unit = soltar@{ velocidadYDp, rapidezDp ->
            if (!giro.arrastrando) return@soltar
            val desde = giro.pose
            if (rapidezDp >= ArrastreMoneda.VELOCIDAD_LANZAR) {
                // Soltada con fuerza: se lanza en la dirección del gesto
                val direccion = when {
                    velocidadYDp < -1f -> 1f
                    velocidadYDp > 1f -> -1f
                    desde.angulo < 0f -> -1f
                    else -> 1f
                }
                lanzarActual(rapidezDp, direccion)
            } else {
                // Soltada despacio: baja a la mesa; termina de voltearse si ya pasó de canto
                giro.arrastrando = false
                val asentar = AsentarMoneda(desde.angulo, desde.alturaPx, desde.escala)
                giro.contador++
                giro.lanzamiento = LanzamientoEscudo(
                    asentar,
                    frente = giro.indice,
                    reverso = if (asentar.cambiaCara) caraAlVoltear(giro.indice, giro.caras.size, desde.angulo) else giro.indice,
                    semilla = giro.contador,
                    chispas = asentar.cambiaCara
                )
            }
        }
        /** Easter egg: mientras gira en el aire se le puede dar más impulso para que siga girando. */
        val impulsar: (Float, Float) -> Unit = impulsar@{ rapidezDp, velocidadYDp ->
            val activo = giro.lanzamiento ?: return@impulsar
            if (activo.caidaDelCielo) return@impulsar
            val desde = giro.pose
            val direccion = if (velocidadYDp > 1f) -1f else 1f
            val techo = (yEnRaiz + ladoPx * 0.15f).coerceAtLeast(ladoPx * 0.4f)
            val medios = VueloMoneda.medioGirosPorVelocidad(rapidezDp.coerceAtLeast(900f))
            val altura = with(densidad) { (110 + 9 * medios).dp.toPx() }.coerceAtMost(techo)
            giro.contador++
            vibrar.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            giro.lanzamiento = LanzamientoEscudo(
                VueloMoneda(medios, altura, ladoPx * 0.08f, anguloInicial = desde.angulo,
                    alturaInicialPx = desde.alturaPx, escalaInicial = desde.escala, direccion = direccion),
                // Conserva la cara de abajo: el impulso no cambia hacia dónde se volteó.
                frente = activo.frente,
                reverso = activo.reverso,
                semilla = giro.contador
            )
        }
        val impulsarActual by rememberUpdatedState(impulsar)
        val empezarActual by rememberUpdatedState(empezarArrastre)
        val moverActual by rememberUpdatedState(moverArrastre)
        val soltarActual by rememberUpdatedState(soltar)

        // La pose cambia cada cuadro: se lee solo al dibujar (graphicsLayer y Canvas), no al
        // componer. Si se leyera aquí, el escudo de Mundos se recompondría cada cuadro mientras
        // la moneda hace el amago y los toques en los cuadrantes se perdían.
        val activo = giro.lanzamiento
        val frente = giro.caras[giro.indiceFrente]
        val indiceReverso by remember(giro) { derivedStateOf { giro.indiceReverso } }
        val reverso = giro.caras[indiceReverso]
        val muestraFrente by remember(giro) { derivedStateOf { cos(giro.pose.anguloVisible / 180f * PI.toFloat()) >= 0f } }
        val visible = if (muestraFrente) frente else reverso
        // Mientras vuela o cae no se toca nada; al arrastrarlo el gesto ya consume los toques.
        val bloqueado = activo != null || !giro.listo

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Solo observa (no consume): los cuadrantes y el arrastre siguen recibiendo el toque.
                    awaitPointerEventScope {
                        while (true) {
                            val evento = awaitPointerEvent(PointerEventPass.Initial)
                            if (evento.changes.any { it.changedToDown() }) toques++
                        }
                    }
                }
                .semantics {
                    customActions = listOf(
                        CustomAccessibilityAction("Girar el escudo a la siguiente cara") { lanzarActual(0f, SENTIDO_SIGUIENTE); true },
                        CustomAccessibilityAction("Girar el escudo a la cara anterior") { lanzarActual(0f, -SENTIDO_SIGUIENTE); true }
                    )
                }
                .pointerInput(Unit) {
                    val rastreo = VelocityTracker()
                    detectDragGestures(
                        onDragStart = {
                            rastreo.resetTracking()
                            empezarActual()
                        },
                        onDragEnd = {
                            val v = rastreo.calculateVelocity()
                            soltarActual(v.y / density, hypot(v.x, v.y) / density)
                        },
                        onDragCancel = { soltarActual(0f, 0f) },
                        onDrag = { cambio, arrastrado ->
                            rastreo.addPosition(cambio.uptimeMillis, cambio.position)
                            cambio.consume()
                            moverActual(arrastrado)
                        }
                    )
                }
        ) {
            // Sombra y chispas (mientras vuela o está en la mano)
            if (activo != null || giro.arrastrando) {
                Canvas(Modifier.fillMaxSize()) {
                    val pose = giro.pose
                    val suelo = Offset(center.x, size.height + radioPx * 0.04f)
                    val cercania = 1f - (-pose.alturaPx / (altoPantallaPx * 0.6f)).coerceIn(0f, 0.75f)
                    if (giro.arrastrando || pose.desdeAterrizaje < 0.5f) dibujarSombraMoneda(suelo, radioPx * 0.85f, cercania)
                    if (activo != null && activo.chispas) dibujarChispas(suelo, radioPx, pose.desdeAterrizaje, visible.acento, activo.semilla)
                }
            }
            // Canto del escudo al ponerse de lado
            Canvas(Modifier.fillMaxSize()) {
                val pose = giro.pose
                dibujarCantoEscudo(pose.anguloVisible, pose.escala, pose.alturaPx)
            }

            // Cara visible (girada y desplazada)
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val pose = giro.pose
                        val angulo = pose.anguloVisible
                        translationY = pose.alturaPx + desplazamientoCanto(angulo, radioPx * pose.escala)
                        rotationX = rotacionCaraVisible(angulo)
                        scaleX = pose.escala
                        scaleY = pose.escala
                        cameraDistance = 48f * density
                    }
            ) {
                val mostrarMundos = visible == CaraMoneda.MUNDOS
                // El escudo de Mundos se mantiene compuesto mientras participa en el giro
                // (así no se reinicia en cada media vuelta).
                if (frente == CaraMoneda.MUNDOS || reverso == CaraMoneda.MUNDOS) {
                    Box(Modifier.fillMaxSize().graphicsLayer { alpha = if (mostrarMundos) 1f else 0f }) {
                        caraMundos()
                    }
                }
                if (!mostrarMundos) {
                    Canvas(
                        Modifier
                            .fillMaxSize()
                            .pointerInput(visible) { detectTapGestures { if (giro.listo) onTocarCara(visible) } }
                    ) {
                        dibujarCaraEscudo(visible, runas.getValue(visible), escenas[visible], tiempo.value)
                    }
                }
                if (bloqueado) {
                    if (activo?.caidaDelCielo != false) {
                        // Mientras cae del cielo no se puede tocar nada del escudo
                        Box(
                            Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                                        }
                                    }
                                }
                        )
                    } else {
                        // En el aire: deslizarlo le da más impulso (un toque no hace nada)
                        Box(
                            Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    val rastreo = VelocityTracker()
                                    detectDragGestures(
                                        onDragStart = { rastreo.resetTracking() },
                                        onDragEnd = {
                                            val v = rastreo.calculateVelocity()
                                            impulsarActual(hypot(v.x, v.y) / density, v.y / density)
                                        },
                                        onDrag = { cambio, _ ->
                                            rastreo.addPosition(cambio.uptimeMillis, cambio.position)
                                            cambio.consume()
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}
