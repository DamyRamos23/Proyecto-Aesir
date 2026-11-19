package com.aesir.odin.ui.forja

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.widthIn
import com.aesir.odin.ui.components.odin.ColoresInicio
import com.aesir.odin.ui.components.odin.EscenaAnimada
import com.aesir.odin.domain.model.Artefacto
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import com.aesir.odin.ui.components.odin.ChipInicio
import com.aesir.odin.ui.components.odin.EscenaModo
import com.aesir.odin.ui.components.odin.PantallaInicioModo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.aesir.odin.domain.model.ArtefactoForja
import com.aesir.odin.domain.model.ArtefactoVitrina
import com.aesir.odin.domain.model.AsignacionForja
import com.aesir.odin.domain.model.EtapaCalentar
import com.aesir.odin.domain.model.EtapaMartillar
import com.aesir.odin.domain.model.EtapaTemplar
import com.aesir.odin.domain.model.RangoApuestaForja
import com.aesir.odin.domain.model.ResultadoAccesoForja
import com.aesir.odin.domain.model.ResultadoEtapa
import com.aesir.odin.domain.model.TipoEtapa
import com.aesir.odin.domain.usecase.forja.ValidarAccesoForjaUseCase
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.DivisorOrnamental
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta

/**
 * CU-02 Forjar artefactos en la apuesta de Brokkr (capa View).
 *
 * Una sola ruta con varias vistas según el estado del ViewModel: la forja
 * con sus reglas y la vitrina, preparar la forja (artefacto y apuesta), las
 * tres etapas (CalentarMetalContent, MartillarContent y TemplarContent), la
 * retroalimentación tras cada etapa y el resultado (ResultadoForjaScreen).
 */
@Composable
fun ForjaBrokkrScreen(viewModel: ForjaBrokkrViewModel, onSalir: () -> Unit) {
    val estado by viewModel.estado.collectAsState()
    val forja by viewModel.forja.collectAsState()
    val vitrina by viewModel.vitrina.collectAsState()
    val asignacion by viewModel.asignacion.collectAsState()
    val rango by viewModel.rango.collectAsState()
    val ultimaRespuesta by viewModel.ultimaRespuesta.collectAsState()
    val segundos by viewModel.segundosTemple.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val aviso by viewModel.aviso.collectAsState()
    val acceso by viewModel.acceso.collectAsState()
    val procesando by viewModel.procesando.collectAsState()
    val saldo by viewModel.saldo.collectAsState()

    // Paso 2 → 3: la vista de reglas y vitrina va antes de preparar la apuesta
    var preparando by rememberSaveable { mutableStateOf(false) }
    // La vitrina del salón en modo forja (ver la colección)
    var viendoVitrina by rememberSaveable { mutableStateOf(false) }

    val enEtapas = forja != null && estado in setOf(
        EstadoForja.CALENTANDO, EstadoForja.MARTILLANDO, EstadoForja.TEMPLANDO, EstadoForja.CONFIRMANDO_ABANDONO
    )

    // Atrás del sistema: en las etapas pide confirmar el abandono (A7.1)
    BackHandler(enabled = true) {
        when {
            viendoVitrina -> viendoVitrina = false
            enEtapas -> viewModel.solicitarAbandono()
            estado == EstadoForja.ELIGIENDO_APUESTA && preparando -> {
                preparando = false
                viewModel.limpiarAviso()
            }
            estado == EstadoForja.CARGANDO -> Unit
            else -> onSalir()
        }
    }

    when {
        viendoVitrina && !enEtapas -> VitrinaScreen(
            modo = ModoVitrina.FORJA,
            tiene = vitrina.mapNotNull { Artefacto.deIdForja(it.artefacto.id) }.toSet(),
            onVolver = { viendoVitrina = false }
        )

        estado == EstadoForja.CARGANDO -> Cargando()

        estado == EstadoForja.ERROR -> MarcoForja {
            BarraForja("Menú", onSalir, saldo)
            Spacer(Modifier.height(40.dp))
            Text("LA FORJA SE APAGÓ", style = estiloTitulo(24.sp, OdinPaleta.Brasa))
            Spacer(Modifier.height(16.dp))
            TextoClaro(aviso ?: "Algo salió mal en la forja.")
            Spacer(Modifier.height(24.dp))
            BotonOdin("Volver al Valhalla", onSalir, OdinPaleta.Brasa, Modifier.fillMaxWidth())
        }

        estado == EstadoForja.TERMINADO -> {
            val final = resultado
            if (final == null || final.abandonada) {
                // A7.1: la forja abandonada se registra como rota y se regresa al Valhalla
                LaunchedEffect(Unit) { onSalir() }
                Cargando()
            } else {
                ResultadoForjaScreen(resultado = final, onVolver = onSalir)
            }
        }

        enEtapas -> {
            val actual = forja!!
            val etapa = actual.etapas[(actual.etapaActual).coerceAtMost(actual.etapas.lastIndex)]
            val respuesta = ultimaRespuesta
            if (respuesta != null) {
                RetroalimentacionEtapa(respuesta, onContinuar = viewModel::continuar)
            } else {
                when (etapa) {
                    is EtapaCalentar -> CalentarMetalContent(
                        etapa = etapa, saldo = saldo, procesando = procesando,
                        onAtras = viewModel::solicitarAbandono, onRevelar = viewModel::revelarCalentar
                    )
                    is EtapaMartillar -> MartillarContent(
                        etapa = etapa, saldo = saldo, procesando = procesando,
                        onAtras = viewModel::solicitarAbandono, onRevelar = viewModel::revelarMartillar
                    )
                    is EtapaTemplar -> TemplarContent(
                        etapa = etapa, segundos = segundos, saldo = saldo, procesando = procesando,
                        onAtras = viewModel::solicitarAbandono, onRevelar = viewModel::revelarTemplar
                    )
                }
            }
            if (estado == EstadoForja.CONFIRMANDO_ABANDONO) {
                DialogoAbandono(onSeguir = viewModel::cancelarAbandono, onAbandonar = viewModel::confirmarAbandono)
            }
        }

        estado == EstadoForja.ELIGIENDO_APUESTA && preparando && asignacion != null && rango != null ->
            PrepararForjaContent(
                asignacion = asignacion!!,
                rango = rango!!,
                saldo = saldo,
                aviso = aviso,
                procesando = procesando,
                onAtras = {
                    preparando = false
                    viewModel.limpiarAviso()
                },
                onCambioApuesta = viewModel::limpiarAviso,
                onApostar = viewModel::apostarALoki,
                onVerVitrina = { viendoVitrina = true }
            )

        else -> InicioForjaContent(
            estado = estado,
            acceso = acceso,
            vitrina = vitrina,
            saldo = saldo,
            onMenu = onSalir,
            onComenzar = { preparando = true },
            onVerVitrina = { viendoVitrina = true }
        )
    }
}

@Composable
private fun Cargando() {
    MarcoForja {
        Spacer(Modifier.height(160.dp))
        CircularProgressIndicator(color = OdinPaleta.Brasa)
        Spacer(Modifier.height(16.dp))
        TextoClaro("Avivando el fuego de la forja…", color = OdinPaleta.HuesoTenue)
    }
}

// ---------------------------------------------------------------------------
// Pasos 2 y A2.x: la forja de Brokkr, reglas y vitrina
// ---------------------------------------------------------------------------

@Composable
private fun InicioForjaContent(
    estado: EstadoForja,
    acceso: ResultadoAccesoForja?,
    vitrina: List<ArtefactoVitrina>,
    saldo: Int,
    onMenu: () -> Unit,
    onComenzar: () -> Unit,
    onVerVitrina: () -> Unit
) {
    val porDia = ValidarAccesoForjaUseCase.FORJAS_POR_DIA
    val usadas = porDia - (acceso?.forjasRestantes ?: porDia)
    val apagada = estado != EstadoForja.ELIGIENDO_APUESTA
    val aviso = if (!apagada) null else when (estado) {
        EstadoForja.FORJA_APAGADA ->
            "La forja está apagada: ya usaste tus dos forjas de hoy. Podrás volver a forjar mañana."
        EstadoForja.HIDROMIEL_INSUFICIENTE ->
            "Te faltan ${acceso?.hidromielFaltante ?: 0} de hidromiel para apostar. Debes conservar lo necesario para entrar a tu siguiente nivel."
        else -> "Supera el reto de fin de mundo (Ragnarök) de un mundo para encender la forja."
    }
    // Pantalla de inicio aprobada (escena de la forja de los enanos), con las reglas y la vitrina
    PantallaInicioModo(
        escena = EscenaModo.BROKKR,
        titulo = "La forja",
        lema = "Eitri martilla y Brokkr aviva el fuego. Forja un tesoro de los dioses… si la mosca de Loki te deja.",
        saldo = saldo,
        chips = listOf(
            ChipInicio("La apuesta de Brokkr", borde = Color(0x80FFB24A)),
            ChipInicio("Forjas de hoy:", "$usadas / $porDia"),
            ChipInicio("Vitrina:", "${vitrina.size} / ${Artefacto.entries.size}")
        ),
        textoBoton = when (estado) {
            EstadoForja.FORJA_APAGADA -> "Vuelve mañana"
            EstadoForja.HIDROMIEL_INSUFICIENTE -> "Hidromiel insuficiente"
            EstadoForja.ELIGIENDO_APUESTA -> "Comenzar forja"
            else -> "Supera tu primer Ragnarök"
        },
        botonHabilitado = !apagada,
        onComenzar = onComenzar,
        onVolver = onMenu,
        aviso = aviso,
        extra = {
            Text(
                "Ver la vitrina del salón", color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 16.sp,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                modifier = Modifier.clip(CutCornerShape(6.dp)).clickable(role = Role.Button, onClick = onVerVitrina)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        },
        comoSeJuega = {
            Pergamino(Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextoPergamino("Apuesta de ${RangoApuestaForja.MINIMO} a ${RangoApuestaForja.MAXIMO}", 17, negrita = true)
                    TextoPergamino("Tres etapas: calentar, martillar y templar", 14)
                    TextoPergamino("0 errores: el artefacto sale completo · ganas tu apuesta ×3", 14)
                    TextoPergamino("1 error: el artefacto se rompe · pierdes tu apuesta", 14)
                    TextoPergamino("Te toca al azar un artefacto de tus mundos superados que aún no tienes", 14)
                    TextoPergamino("Dos forjas por día", 14)
                }
            }
        }
    )
}


// ---------------------------------------------------------------------------
// Pasos 3 a 5: preparar la forja
// ---------------------------------------------------------------------------

/**
 * Pasos 3 a 5: la selección de la forja. El fuego elige al azar el artefacto
 * (RF-30) y aparece sobre el yunque con su mundo y su poder; abajo se elige la
 * apuesta. Con el tema de la forja: la escena de los enanos oscurecida y brasas.
 */
@Composable
private fun PrepararForjaContent(
    asignacion: AsignacionForja,
    rango: RangoApuestaForja,
    saldo: Int,
    aviso: String?,
    procesando: Boolean,
    onAtras: () -> Unit,
    onCambioApuesta: () -> Unit,
    onApostar: (Int) -> Unit,
    onVerVitrina: () -> Unit
) {
    var cantidad by rememberSaveable { mutableIntStateOf(RangoApuestaForja.MINIMO) }
    val valida = rango.contiene(cantidad)
    val artefacto = asignacion.artefacto
    val enum = artefacto?.let { Artefacto.deIdForja(it.id) }
    val aparicion = remember { Animatable(0f) }
    LaunchedEffect(asignacion) { aparicion.animateTo(1f, tween(1100)) }
    val brasa = Color(0xFFFF9A3C)

    Box(Modifier.fillMaxSize().background(ColoresInicio.Fondo)) {
        EscenaAnimada(EscenaModo.BROKKR, activa = false, modifier = Modifier.fillMaxSize().alpha(.45f))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x6606070D), Color(0xDD06070D), Color(0xFF06070D)))))
        BrasasFlotantes(brasa, Modifier.fillMaxSize())
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BarraForja("Atrás", onAtras, saldo)
            Text("EL FUEGO ELIGIÓ", color = ColoresInicio.Antetitulo, fontFamily = FuenteTitulo, fontSize = 12.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(6.dp))
            // El artefacto sube del fuego y queda sobre el yunque
            Box(Modifier.size(230.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawCircle(
                        Brush.radialGradient(listOf(brasa.copy(alpha = .55f * aparicion.value), Color.Transparent), center = Offset(w / 2, h * .45f), radius = w * .5f),
                        w * .5f, Offset(w / 2, h * .45f)
                    )
                    // Yunque
                    val yunque = Path().apply {
                        moveTo(w * .18f, h * .74f); lineTo(w * .82f, h * .74f); lineTo(w * .74f, h * .8f)
                        lineTo(w * .62f, h * .8f); lineTo(w * .66f, h * .92f); lineTo(w * .34f, h * .92f)
                        lineTo(w * .38f, h * .8f); lineTo(w * .26f, h * .8f); close()
                    }
                    drawPath(yunque, Brush.verticalGradient(listOf(Color(0xFF55504A), Color(0xFF1E1B18)), startY = h * .74f, endY = h * .92f))
                    drawLine(brasa.copy(alpha = .6f), Offset(w * .2f, h * .742f), Offset(w * .8f, h * .742f), 2f)
                }
                Box(
                    Modifier.padding(bottom = 46.dp).alpha(aparicion.value)
                        .graphicsLayer {
                            translationY = (1f - aparicion.value) * 60f
                            scaleX = .7f + .3f * aparicion.value
                            scaleY = .7f + .3f * aparicion.value
                        }
                ) { IconoArtefacto(artefacto?.id, tamano = 150.dp) }
            }
            if (artefacto != null) {
                Text(artefacto.nombre, style = estiloTitulo(30.sp, Color(0xFFFFC266), brillo = brasa.copy(alpha = .5f)))
                Text(
                    "MUNDO ${enum?.mundo ?: ""} · ${artefacto.mundoNombre.uppercase()}", color = OdinPaleta.Oro,
                    fontFamily = FuenteTitulo, fontSize = 13.sp, letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(10.dp))
                PlacaOscura(Modifier.fillMaxWidth().widthIn(max = 480.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PODER EN HASTA EL ALBA", color = OdinPaleta.Oro, fontFamily = FuenteTitulo, fontSize = 12.sp, letterSpacing = 2.sp)
                        Spacer(Modifier.height(4.dp))
                        TextoClaro(artefacto.poder, 16)
                        Spacer(Modifier.height(4.dp))
                        TextoClaro("Sin errores se queda en tu vitrina. Con un error se rompe.", 13, OdinPaleta.HuesoTenue)
                    }
                }
            } else {
                // A3.1: ya tiene todos los artefactos de sus mundos superados
                Text("FORJA POR HIDROMIEL", style = estiloTitulo(24.sp, Color(0xFFFFC266)))
                Spacer(Modifier.height(8.dp))
                PlacaOscura(Modifier.fillMaxWidth().widthIn(max = 480.dp)) {
                    TextoClaro("Ya tienes todos los artefactos de tus mundos. Esta forja será solo por hidromiel: cobrarás el premio que corresponda, pero no recibirás un artefacto.", 14)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("TU APUESTA A LOKI", color = ColoresInicio.Antetitulo, fontFamily = FuenteTitulo, fontSize = 12.sp, letterSpacing = 3.sp)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                BotonPasoApuesta("−", "Apostar menos") { cantidad = (cantidad - PASO_APUESTA_FORJA).coerceAtLeast(0); onCambioApuesta() }
                Text(
                    "$cantidad", color = if (valida) Color(0xFFFFF6E0) else OdinPaleta.Incorrecto, fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.Bold, fontSize = 36.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.width(110.dp).semantics { contentDescription = "Apuesta de $cantidad de hidromiel" }
                )
                BotonPasoApuesta("+", "Apostar más") { cantidad = (cantidad + PASO_APUESTA_FORJA).coerceAtMost(999); onCambioApuesta() }
            }
            TextoClaro(
                "Apuesta permitida: ${rango.minimo}–${rango.maximoPermitido.coerceAtLeast(rango.minimo)} · perfecta paga ×3 menos el diezmo",
                13, if (valida) OdinPaleta.HuesoTenue else OdinPaleta.Incorrecto
            )
            if (rango.pisoApuesta > 0) {
                TextoClaro("Se reservan ${rango.pisoApuesta} de hidromiel para tu siguiente nivel.", 12, OdinPaleta.HuesoTenue)
            }
            if (aviso != null) {
                Spacer(Modifier.height(6.dp))
                TextoClaro(aviso, 14, OdinPaleta.Incorrecto)
            }
            Spacer(Modifier.height(14.dp))
            BotonOdin(
                "Apostar a Loki", onClick = { onApostar(cantidad) }, acento = brasa,
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(), habilitado = valida && !procesando
            )
            Text(
                "Ver la vitrina del salón", color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 15.sp,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                modifier = Modifier.padding(top = 6.dp).clip(CutCornerShape(6.dp)).clickable(role = Role.Button, onClick = onVerVitrina)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

private const val PASO_APUESTA_FORJA = 5

@Composable
private fun BotonPasoApuesta(texto: String, etiqueta: String, onClick: () -> Unit) {
    Box(
        Modifier.size(50.dp).clip(CircleShape).background(Color(0xE60B0907))
            .border(1.dp, Color(0xFFFF9A3C).copy(alpha = .7f), CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = etiqueta },
        contentAlignment = Alignment.Center
    ) { Text(texto, color = Color(0xFFFF9A3C), fontFamily = FuenteTitulo, fontSize = 26.sp) }
}

// ---------------------------------------------------------------------------
// Pasos 7 y 8: Calentar el metal (ordenar con arrastrar y soltar, CU-09)
// ---------------------------------------------------------------------------

@Composable
internal fun CalentarMetalContent(
    etapa: EtapaCalentar,
    saldo: Int,
    procesando: Boolean,
    onAtras: () -> Unit,
    onRevelar: (List<String>) -> Unit
) {
    val orden = remember(etapa) { mutableStateListOf(*etapa.pasosIniciales.toTypedArray()) }
    var arrastrando by remember { mutableStateOf<String?>(null) }
    var desplazamiento by remember { mutableFloatStateOf(0f) }
    val alto = 60.dp
    val espacio = 10.dp
    val pasoPx = with(LocalDensity.current) { (alto + espacio).toPx() }

    MarcoForja {
        BarraForja("Atrás", onAtras, saldo)
        Spacer(Modifier.height(8.dp))
        RielEtapas(actual = 1)
        EscenaCalentar(Modifier.padding(top = 4.dp))
        EncabezadoEtapa(1, TipoEtapa.CALENTAR.nombre)
        TextoClaro("Ordena los pasos del proceso", 17)
        TextoClaro(etapa.enunciado, 13, OdinPaleta.HuesoTenue)
        TextoClaro("Mantén y arrastra cada piedra a su lugar. Solo tienes un intento.", 12, OdinPaleta.HuesoTenue)
        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(espacio), modifier = Modifier.fillMaxWidth()) {
            orden.forEachIndexed { indice, elemento ->
                androidx.compose.runtime.key(elemento.id) {
                    val activo = elemento.id == arrastrando
                    Pergamino(
                        resaltado = activo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(alto)
                            .zIndex(if (activo) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (activo) desplazamiento else 0f
                                scaleX = if (activo) 1.03f else 1f
                                scaleY = if (activo) 1.03f else 1f
                            }
                            .semantics { contentDescription = "Paso en la posición ${indice + 1}: ${elemento.texto}" }
                            .pointerInput(elemento.id) {
                                detectDragGestures(
                                    onDragStart = {
                                        arrastrando = elemento.id
                                        desplazamiento = 0f
                                    },
                                    onDragEnd = {
                                        arrastrando = null
                                        desplazamiento = 0f
                                    },
                                    onDragCancel = {
                                        arrastrando = null
                                        desplazamiento = 0f
                                    },
                                    onDrag = { cambio, arrastre ->
                                        cambio.consume()
                                        desplazamiento += arrastre.y
                                        val i = orden.indexOfFirst { it.id == elemento.id }
                                        if (desplazamiento > pasoPx / 2 && i < orden.lastIndex) {
                                            orden.add(i + 1, orden.removeAt(i))
                                            desplazamiento -= pasoPx
                                        } else if (desplazamiento < -pasoPx / 2 && i > 0) {
                                            orden.add(i - 1, orden.removeAt(i))
                                            desplazamiento += pasoPx
                                        }
                                    }
                                )
                            }
                    ) {
                        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Menu, contentDescription = null, tint = OdinPaleta.Tinta.copy(alpha = .6f))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                elemento.texto,
                                color = OdinPaleta.Tinta,
                                fontFamily = FuenteTexto,
                                fontSize = 15.sp,
                                lineHeight = 18.sp,
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        BotonOdin(
            "Revelar respuesta",
            onClick = { onRevelar(orden.map { it.id }) },
            acento = OdinPaleta.Brasa,
            modifier = Modifier.fillMaxWidth(),
            habilitado = !procesando && arrastrando == null
        )
        Spacer(Modifier.height(8.dp))
    }
}

// ---------------------------------------------------------------------------
// Pasos 10 y 11: Martillar (elegir una pieza de diseño)
// ---------------------------------------------------------------------------

@Composable
internal fun MartillarContent(
    etapa: EtapaMartillar,
    saldo: Int,
    procesando: Boolean,
    onAtras: () -> Unit,
    onRevelar: (String) -> Unit
) {
    var elegida by remember(etapa) { mutableStateOf<String?>(null) }

    MarcoForja {
        BarraForja("Atrás", onAtras, saldo)
        Spacer(Modifier.height(8.dp))
        RielEtapas(actual = 2)
        EscenaMartillar(Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(8.dp))
        EncabezadoEtapa(2, TipoEtapa.MARTILLAR.nombre)
        PlacaOscura(Modifier.fillMaxWidth()) { TextoClaro(etapa.enunciado, 16) }
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            etapa.piezas.forEach { pieza ->
                val seleccionada = pieza.id == elegida
                Pergamino(
                    resaltado = seleccionada,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.RadioButton) { elegida = pieza.id }
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                            if (seleccionada) {
                                Box(
                                    Modifier.size(24.dp).clip(androidx.compose.foundation.shape.CircleShape).background(OdinPaleta.Tinta),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Filled.Check, null, tint = OdinPaleta.Brasa, modifier = Modifier.size(16.dp)) }
                            }
                        }
                        Text(
                            pieza.texto,
                            color = OdinPaleta.Tinta,
                            fontFamily = FuenteTexto,
                            fontSize = 16.sp,
                            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.size(26.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        BotonOdin(
            "Revelar respuesta",
            onClick = { elegida?.let(onRevelar) },
            acento = OdinPaleta.Brasa,
            modifier = Modifier.fillMaxWidth(),
            habilitado = elegida != null && !procesando
        )
        Spacer(Modifier.height(8.dp))
    }
}

// ---------------------------------------------------------------------------
// Pasos 13 y 14: Templar (completar antes de que se enfríe el metal)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TemplarContent(
    etapa: EtapaTemplar,
    segundos: Int,
    saldo: Int,
    procesando: Boolean,
    onAtras: () -> Unit,
    onRevelar: (Map<String, String>) -> Unit
) {
    val valores = remember(etapa) { mutableStateMapOf<String, String>() }
    var activo by remember(etapa) { mutableStateOf(etapa.huecos.firstOrNull()?.id) }
    val completo = etapa.huecos.all { !valores[it.id].isNullOrBlank() }

    MarcoForja(tinte = colorFondoTemple(segundos)) {
        BarraForja("Atrás", onAtras, saldo)
        Spacer(Modifier.height(8.dp))
        RielEtapas(actual = 3)
        EscenaTemplar(segundos, Modifier.padding(top = 4.dp))
        BarraFasesTemple(segundos)
        Spacer(Modifier.height(6.dp))
        EncabezadoEtapa(3, TipoEtapa.TEMPLAR.nombre, color = Color(0xFFDDEAF2))
        TextoClaro(etapa.enunciado.ifBlank { "Completa la definición antes de que se enfríe el metal" }, 16)
        Spacer(Modifier.height(12.dp))
        Pergamino(Modifier.fillMaxWidth()) {
            Text(
                plantillaConHuecos(etapa, valores, activo),
                color = OdinPaleta.Tinta,
                fontFamily = FuenteTexto,
                fontSize = 16.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )
        }
        if (etapa.huecos.size > 1) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                etapa.huecos.forEachIndexed { i, hueco ->
                    Chip("Hueco ${i + 1}", seleccionado = hueco.id == activo, oscuro = true) { activo = hueco.id }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            etapa.opciones.forEach { opcion ->
                val usada = valores.values.contains(opcion)
                Chip(opcion, seleccionado = usada, oscuro = false) {
                    val destino = activo ?: return@Chip
                    // Si la opción ya estaba en otro hueco, se mueve a este
                    valores.entries.firstOrNull { it.value == opcion }?.let { valores.remove(it.key) }
                    valores[destino] = opcion
                    activo = etapa.huecos.firstOrNull { valores[it.id].isNullOrBlank() }?.id ?: destino
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        BotonOdin(
            "Revelar respuesta",
            onClick = { onRevelar(valores.toMap()) },
            acento = OdinPaleta.Brasa,
            modifier = Modifier.fillMaxWidth(),
            habilitado = completo && !procesando && segundos > 0
        )
        Spacer(Modifier.height(8.dp))
    }
}

private fun plantillaConHuecos(etapa: EtapaTemplar, valores: Map<String, String>, activo: String?) = buildAnnotatedString {
    val patron = Regex("\\{(h\\d+)\\}")
    var desde = 0
    patron.findAll(etapa.plantilla).forEach { m ->
        append(etapa.plantilla.substring(desde, m.range.first))
        val id = m.groupValues[1]
        val valor = valores[id]
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Bold,
                color = if (id == activo) OdinPaleta.TintaRoja else OdinPaleta.TintaOscura,
                background = Color(0x33C9A35B)
            )
        ) { append(if (valor.isNullOrBlank()) " ________ " else " $valor ") }
        desde = m.range.last + 1
    }
    append(etapa.plantilla.substring(desde))
}

@Composable
private fun Chip(texto: String, seleccionado: Boolean, oscuro: Boolean, onClick: () -> Unit) {
    val forma = CutCornerShape(6.dp)
    val fondo = when {
        oscuro && seleccionado -> OdinPaleta.Brasa.copy(alpha = .25f)
        oscuro -> Color(0xE60B0907)
        seleccionado -> ColorPergaminoSombra
        else -> ColorPergamino
    }
    Box(
        Modifier
            .clip(forma)
            .background(fondo)
            .border(if (seleccionado) 2.dp else 1.dp, if (seleccionado) OdinPaleta.Brasa else ColorMarcoForja, forma)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            texto,
            color = if (oscuro) OdinPaleta.Hueso else OdinPaleta.Tinta,
            fontFamily = FuenteTexto,
            fontSize = 15.sp
        )
    }
}

// ---------------------------------------------------------------------------
// Pasos 9, 12 y 15: retroalimentación (nunca muestra la respuesta correcta)
// ---------------------------------------------------------------------------

@Composable
private fun RetroalimentacionEtapa(respuesta: ResultadoEtapa, onContinuar: () -> Unit) {
    val numero = respuesta.tipo.ordinal + 1
    val titulo = when {
        respuesta.metalFrio -> "EL METAL SE ENFRIÓ"
        !respuesta.correcta -> "EL METAL SE AGRIETÓ"
        respuesta.tipo == TipoEtapa.CALENTAR -> "EL METAL ESTÁ LISTO"
        respuesta.tipo == TipoEtapa.MARTILLAR -> "EL METAL TOMÓ FORMA"
        else -> "EL METAL ESTÁ TEMPLADO"
    }
    val boton = when {
        respuesta.forjaTerminada -> "Ver el resultado"
        respuesta.tipo == TipoEtapa.CALENTAR -> "Continuar a martillar"
        else -> "Continuar a templar"
    }
    MarcoForja {
        Spacer(Modifier.height(24.dp))
        Text(titulo, style = estiloTitulo(26.sp, OdinPaleta.Brasa, brillo = OdinPaleta.Brasa.copy(alpha = .5f)), textAlign = TextAlign.Center)
        Spacer(Modifier.height(30.dp))
        Box(contentAlignment = Alignment.TopCenter) {
            Pergamino(Modifier.fillMaxWidth().padding(top = 26.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 26.dp, bottom = 8.dp)) {
                    Text(
                        if (respuesta.correcta) "¡Respuesta correcta!" else "Respuesta incorrecta",
                        color = OdinPaleta.Tinta, fontFamily = FuenteTitulo, fontSize = 26.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    )
                    if (!respuesta.correcta) {
                        Spacer(Modifier.height(6.dp))
                        TextoPergamino(
                            if (respuesta.metalFrio) "Se acabó el tiempo antes de revelar tu respuesta. Se sumó un error a la forja."
                            else "Se sumó un error a la forja. Quedó anotado en tu bitácora para repasarlo.",
                            14
                        )
                    }
                }
            }
            Box(
                Modifier
                    .size(54.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(OdinPaleta.Tinta)
                    .border(2.dp, if (respuesta.correcta) OdinPaleta.Brasa else OdinPaleta.Incorrecto, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (respuesta.correcta) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    tint = if (respuesta.correcta) OdinPaleta.Brasa else OdinPaleta.Incorrecto,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(Modifier.height(26.dp))
        val completadas = if (respuesta.forjaTerminada) 3 else numero
        com.aesir.odin.ui.components.odin.PuntosProgreso(
            total = 3, actual = completadas, acento = OdinPaleta.Brasa, metal = OdinPaleta.Oro
        )
        Spacer(Modifier.height(8.dp))
        Text("$numero / 3", color = OdinPaleta.Brasa, fontFamily = FuenteTitulo, fontSize = 20.sp)
        if (respuesta.erroresAcumulados > 0) {
            TextoClaro(
                "Errores en la forja: ${respuesta.erroresAcumulados}",
                14,
                OdinPaleta.Incorrecto
            )
        }
        Spacer(Modifier.height(30.dp))
        BotonOdin(boton, onContinuar, OdinPaleta.Brasa, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
    }
}

// ---------------------------------------------------------------------------
// A7.1: confirmar el abandono
// ---------------------------------------------------------------------------

@Composable
private fun DialogoAbandono(onSeguir: () -> Unit, onAbandonar: () -> Unit) {
    Dialog(onDismissRequest = onSeguir) {
        Column(
            Modifier
                .clip(CutCornerShape(10.dp))
                .background(Color(0xFF15110C))
                .border(1.5.dp, ColorMarcoForja, CutCornerShape(10.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("¿ABANDONAR LA FORJA?", style = estiloTitulo(22.sp, OdinPaleta.Brasa), textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            IconoArtefacto("art_mjolnir", tamano = 90.dp, roto = true)
            Spacer(Modifier.height(10.dp))
            TextoClaro(
                "Si abandonas, el artefacto se romperá, perderás tu apuesta y contará como una forja del día.",
                15
            )
            Spacer(Modifier.height(18.dp))
            BotonOdin("Seguir forjando", onSeguir, OdinPaleta.Brasa, Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            BotonOdin("Abandonar", onAbandonar, OdinPaleta.Brasa, Modifier.fillMaxWidth(), primario = false)
        }
    }
}
