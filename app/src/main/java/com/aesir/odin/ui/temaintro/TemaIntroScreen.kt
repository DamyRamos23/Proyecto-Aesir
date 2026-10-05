package com.aesir.odin.ui.temaintro

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.aesir.odin.di.AppContainer
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.DivisorOrnamental
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.SelloRunico
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.components.odin.manchaOval
import com.aesir.odin.ui.navigation.OdinNavGraph
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.random.Random

/**
 * Introducción de un tema (CU-02), presentada como un pergamino desenrollado.
 * El texto sale de la primera lección del tema; el sello, las runas y el
 * paisaje de fondo dependen del mundo.
 */
@Composable
fun TemaIntroScreen(
    navController: NavHostController,
    temaId: String,
    appContainer: AppContainer
) {
    val viewModel: TemaIntroViewModel = viewModel(
        factory = TemaIntroViewModel.factory(
            appContainer.obtenerIntroduccionTemaUseCase,
            appContainer.registrarIntroduccionVistaUseCase,
            appContainer.leccionRepository,
            appContainer.obtenerMundoUseCase,
            appContainer.obtenerTemasPorMundoUseCase
        )
    )

    LaunchedEffect(temaId) {
        viewModel.cargarIntroduccion(temaId)
    }

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is TemaIntroUiState.Cargando -> FondoMundo(estilo = null) {
            CircularProgressIndicator(color = OdinPaleta.Brasa, modifier = Modifier.align(Alignment.Center))
        }
        is TemaIntroUiState.Error -> FondoMundo(estilo = null) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No se pudo cargar la introducción del tema", color = OdinPaleta.Hueso, fontFamily = FuenteTexto)
                TextButton(onClick = { navController.popBackStack() }) { Text("Volver", color = OdinPaleta.Brasa) }
            }
        }
        is TemaIntroUiState.Contenido -> {
            val estilo = EstilosMundo.porOrden(state.mundo.orden)
            FondoMundo(estilo = estilo, alturaPaisaje = 260.dp) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 10.dp, end = 18.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BotonVolver("Roadmap", onClick = { viewModel.onCerrarPulsado { navController.popBackStack() } })
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Tema ${state.tema.orden} de ${state.totalTemas}",
                            color = Color(0xFFD8CFBE),
                            fontFamily = FuenteTexto,
                            fontSize = 15.sp
                        )
                    }

                    Pergamino(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        ContenidoPergamino(state, estilo)
                    }

                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BotonOdin(
                            texto = "Comenzar lección",
                            onClick = {
                                viewModel.onComenzarLeccionPulsado { leccionId ->
                                    navController.navigate(OdinNavGraph.leccion(leccionId))
                                }
                            },
                            acento = estilo.acento,
                            iconoFinal = Icons.AutoMirrored.Filled.ArrowForward,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextButton(onClick = { viewModel.onCerrarPulsado { navController.popBackStack() } }) {
                            Text("Volver al roadmap", color = Color(0xFFC9BFAE), fontFamily = FuenteTexto, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContenidoPergamino(state: TemaIntroUiState.Contenido, estilo: EstiloMundo) {
    val runa = estilo.runaDeTema(state.tema.orden)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp, vertical = 40.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SelloRunico(runa = runa, estilo = estilo, tamano = 56.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "TEMA ${state.tema.orden} · INTRODUCCIÓN",
                    color = OdinPaleta.TintaRoja,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.4.sp
                )
                Text(
                    state.tema.nombre,
                    color = OdinPaleta.TintaOscura,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 23.sp,
                    lineHeight = 26.sp
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        DivisorOrnamental(OdinPaleta.Tinta, OdinPaleta.TintaRoja)
        Spacer(Modifier.height(8.dp))

        Text(
            state.tema.descripcion,
            color = Color(0xFF4A3420),
            fontFamily = FuenteTexto,
            fontStyle = FontStyle.Italic,
            fontSize = 15.sp,
            lineHeight = 20.sp
        )

        if (state.tituloLeccion != null) {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(runa, color = OdinPaleta.TintaRoja, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(
                    state.tituloLeccion,
                    color = OdinPaleta.TintaOscura,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }

        state.parrafos.forEachIndexed { i, parrafo ->
            Spacer(Modifier.height(if (i == 0) 6.dp else 12.dp))
            Text(
                text = if (i == 0 && parrafo.isNotEmpty()) {
                    // Capitular roja, como en los manuscritos
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF8A2A16), fontSize = 34.sp, fontWeight = FontWeight.SemiBold)) {
                            append(parrafo.first())
                        }
                        append(parrafo.drop(1))
                    }
                } else {
                    buildAnnotatedString { append(parrafo) }
                },
                color = OdinPaleta.Tinta,
                fontFamily = FuenteTexto,
                fontSize = 15.5.sp,
                lineHeight = 22.sp
            )
        }

        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Lee con calma: la lección te preguntará sobre esto.",
                color = Color(0xFF5A4430),
                fontFamily = FuenteTexto,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            SelloDeCera(estilo)
        }
    }
}

/** Sello de cera roja con el emblema del mundo en relieve. */
@Composable
private fun SelloDeCera(estilo: EstiloMundo) {
    Canvas(Modifier.size(64.dp)) {
        val r = size.minDimension / 2f * .9f
        mancha(center + Offset(0f, 2.dp.toPx()), r * 1.15f, Color.Black, .35f)
        // Borde irregular de la cera
        val rnd = Random(3)
        val borde = Path()
        val n = 28
        for (i in 0..n) {
            val a = i * 2 * Math.PI / n
            val rr = r * (0.93f + rnd.nextFloat() * .1f)
            val p = center + Offset(kotlin.math.cos(a).toFloat() * rr, kotlin.math.sin(a).toFloat() * rr)
            if (i == 0) borde.moveTo(p.x, p.y) else borde.lineTo(p.x, p.y)
        }
        borde.close()
        drawPath(
            borde,
            Brush.radialGradient(listOf(Color(0xFFC0402A), Color(0xFF8A1E12), Color(0xFF4A0C06)), center = center - Offset(r * .25f, r * .3f), radius = r * 1.4f)
        )
        drawCircle(Color(0xFF5A0E08), r * .7f, style = Stroke(2.dp.toPx()))
        drawCircle(Color(0x59E0705A), r * .7f, center - Offset(.8f, .8f), style = Stroke(.8.dp.toPx()))
        dibujarEmblema(estilo.emblema, center, r * .45f, Color(0xFF4A0A05))
        dibujarEmblema(estilo.emblema, center - Offset(.7f, .7f), r * .45f, Color(0x73E88070))
        manchaOval(center - Offset(r * .35f, r * .45f), r * .25f, r * .1f, Color.White, .2f)
    }
}

/**
 * Pergamino con rodillos de madera arriba y abajo, papel envejecido y bordes
 * quemados. El contenido se desplaza dentro del papel, entre los rodillos.
 */
@Composable
private fun Pergamino(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Box(modifier.drawBehind { dibujarPergamino() }) {
        contenido()
    }
}

private fun DrawScope.dibujarPergamino() {
    val w = size.width
    val h = size.height
    val rnd = Random(17)
    val x0 = 8.dp.toPx()
    val x1 = w - 8.dp.toPx()
    val y0 = 12.dp.toPx()
    val y1 = h - 12.dp.toPx()

    // Sombra
    drawRoundRect(Color.Black.copy(alpha = .55f), Offset(x0 + 4f, y0 + 10f), Size(x1 - x0, y1 - y0), CornerRadius(6f))

    // Contorno con bordes irregulares
    val papel = Path()
    val paso = 9.dp.toPx()
    fun jitter() = (rnd.nextFloat() - .5f) * 3.dp.toPx()
    papel.moveTo(x0, y0)
    var x = x0
    while (x < x1) { x += paso; papel.lineTo(x.coerceAtMost(x1), y0 + jitter()) }
    var y = y0
    while (y < y1) { y += paso; papel.lineTo(x1 + jitter(), y.coerceAtMost(y1)) }
    x = x1
    while (x > x0) { x -= paso; papel.lineTo(x.coerceAtLeast(x0), y1 + jitter()) }
    y = y1
    while (y > y0) { y -= paso; papel.lineTo(x0 + jitter(), y.coerceAtLeast(y0)) }
    papel.close()

    clipPath(papel) {
        drawRect(
            Brush.radialGradient(
                0f to Color(0xFFEFE0B8), .7f to Color(0xFFE2CC98), 1f to Color(0xFFBF9A62),
                center = Offset(w / 2, h * .45f), radius = maxOf(w, h) * .7f
            )
        )
        repeat(14) {
            manchaOval(
                Offset(rnd.nextFloat() * w, rnd.nextFloat() * h),
                (20 + rnd.nextInt(40)).dp.toPx(), (12 + rnd.nextInt(30)).dp.toPx(),
                Color(0xFF8A6030), .06f + rnd.nextFloat() * .06f
            )
        }
        repeat(220) {
            val fx = rnd.nextFloat() * w
            val fy = rnd.nextFloat() * h
            drawLine(Color(0xFF5A3A18).copy(alpha = .05f + rnd.nextFloat() * .06f), Offset(fx, fy), Offset(fx + (rnd.nextFloat() - .5f) * 6.dp.toPx(), fy + (4 + rnd.nextInt(10)).dp.toPx()), .6.dp.toPx())
        }
        // Bordes quemados
        val q = 22.dp.toPx()
        val quemado = Color(0xFF4A2A10)
        drawRect(Brush.horizontalGradient(listOf(quemado.copy(alpha = .55f), Color.Transparent), startX = x0, endX = x0 + q))
        drawRect(Brush.horizontalGradient(listOf(Color.Transparent, quemado.copy(alpha = .55f)), startX = x1 - q, endX = x1))
        drawRect(Brush.verticalGradient(listOf(Color(0x997A5A30), Color.Transparent), startY = y0, endY = y0 + 30.dp.toPx()))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0x997A5A30)), startY = y1 - 30.dp.toPx(), endY = y1))
    }
    drawPath(papel, Color(0x993A2008), style = Stroke(1.2.dp.toPx()))

    // Marco interior a tinta con rombos rojos
    val m1 = 18.dp.toPx()
    val m2 = 22.dp.toPx()
    drawRect(Color(0x593A2715), Offset(x0 + m1 - 8f, y0 + m1), Size(x1 - x0 - 2 * m1 + 16f, y1 - y0 - 2 * m1), style = Stroke(.8.dp.toPx()))
    drawRect(Color(0x403A2715), Offset(x0 + m2 - 8f, y0 + m2), Size(x1 - x0 - 2 * m2 + 16f, y1 - y0 - 2 * m2), style = Stroke(.6.dp.toPx()))
    listOf(
        Offset(x0 + m1 - 8f, y0 + m1), Offset(x1 - m1 + 8f, y0 + m1),
        Offset(x0 + m1 - 8f, y1 - m1), Offset(x1 - m1 + 8f, y1 - m1)
    ).forEach { c ->
        val d = 6.dp.toPx()
        val rombo = Path().apply { moveTo(c.x, c.y - d); lineTo(c.x + d, c.y); lineTo(c.x, c.y + d); lineTo(c.x - d, c.y); close() }
        drawPath(rombo, Color(0xFFE8D5A6))
        drawPath(rombo, OdinPaleta.TintaRoja, style = Stroke(1.dp.toPx()))
    }

    // Rodillos de madera con remates dorados
    rodillo(y0)
    rodillo(y1)
}

private fun DrawScope.rodillo(y: Float) {
    val w = size.width
    val alto = 18.dp.toPx()
    drawRoundRect(
        Brush.verticalGradient(
            0f to Color(0xFF7A4E2A), .35f to Color(0xFFC08850), .55f to Color(0xFF6A4020), 1f to Color(0xFF2A170A),
            startY = y - alto / 2, endY = y + alto / 2
        ),
        topLeft = Offset(0f, y - alto / 2),
        size = Size(w, alto),
        cornerRadius = CornerRadius(alto / 2)
    )
    var x = 10.dp.toPx()
    while (x < w - 10.dp.toPx()) {
        drawLine(Color(0x402A170A), Offset(x, y - alto * .4f), Offset(x + 3.dp.toPx(), y + alto * .4f), .7.dp.toPx())
        x += 7.dp.toPx()
    }
    drawLine(Color(0x59F0C890), Offset(4.dp.toPx(), y - alto * .22f), Offset(w - 4.dp.toPx(), y - alto * .22f), 1.2.dp.toPx())
    val oro = Brush.verticalGradient(listOf(Color(0xFFF5D890), Color(0xFFC9A35B), Color(0xFF4A3410)), startY = y - alto * .7f, endY = y + alto * .7f)
    listOf(0f, w).forEach { cx ->
        drawRoundRect(oro, Offset(cx - 5.dp.toPx(), y - alto * .62f), Size(10.dp.toPx(), alto * 1.24f), CornerRadius(2.dp.toPx()))
        drawCircle(Color(0xFF5A3E12), 2.5.dp.toPx(), Offset(cx, y))
    }
}
