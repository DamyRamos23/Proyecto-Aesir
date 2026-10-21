package com.aesir.odin.ui.eastereggs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.eastereggs.ApuestaDados
import com.aesir.odin.domain.eastereggs.ColorRuleta
import com.aesir.odin.domain.eastereggs.Dados
import com.aesir.odin.domain.eastereggs.Ruleta
import com.aesir.odin.domain.eastereggs.Simbolo
import com.aesir.odin.domain.eastereggs.Tragamonedas
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.OdinPaleta
import com.aesir.odin.ui.theme.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val VerdePino = Color(0xFF1F4D3A)
private val BeigeClaro = Color(0xFFEFE4C8)
private val Marfil = Color(0xFFF7F1E3)

// ---------------------------------------------------------------------------
// Ruleta
// ---------------------------------------------------------------------------

private const val VUELTAS_RULETA = 12
private const val DURACION_RULETA_MS = 6_000

/** Arranca rápido y frena poco a poco, como una ruleta real. */
private val FrenadoRuleta = CubicBezierEasing(0.15f, 0.85f, 0.25f, 1f)

@Composable
fun RuletaJuego(vm: JuegoOcultoViewModel, estilo: EstiloMundo) {
    val scope = rememberCoroutineScope()
    var apuesta by remember { mutableIntStateOf(10) }
    var eleccion by remember { mutableStateOf(ColorRuleta.VERDE) }
    var girando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    val giro = remember { Animatable(0f) }

    PanelJuego(estilo, "Rueda de Freyja", semilla = 3) {
        Box(contentAlignment = Alignment.TopCenter) {
            Canvas(
                Modifier
                    .padding(top = 12.dp)
                    .size(250.dp)
                    .semantics { contentDescription = "Ruleta verde pino y beige" }
            ) {
                rotate(giro.value) {
                    drawArc(VerdePino, 0f, 180f, useCenter = true)
                    drawArc(BeigeClaro, 180f, 180f, useCenter = true)
                    // Marcas en el borde para que se note el giro
                    for (k in 0 until 24) {
                        rotate(k * 15f) {
                            drawLine(estilo.metal.copy(alpha = .7f), Offset(center.x, 4.dp.toPx()), Offset(center.x, 14.dp.toPx()), 2.dp.toPx())
                        }
                    }
                }
                drawCircle(estilo.metal, style = Stroke(6.dp.toPx()))
                drawCircle(estilo.metalOscuro, radius = 16.dp.toPx())
                drawCircle(estilo.acento, radius = 8.dp.toPx())
            }
            // Puntero fijo arriba
            Canvas(Modifier.size(26.dp)) {
                drawPath(Path().apply { moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2, size.height); close() }, estilo.acento)
            }
        }
    }

    MensajeResultado(resultado?.first, resultado?.second == true, estilo)
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ColorRuleta.entries.forEach { c ->
            BotonOdin(
                if (c == ColorRuleta.VERDE) "Verde" else "Beige",
                { eleccion = c },
                if (c == ColorRuleta.VERDE) VerdePino else BeigeClaro,
                Modifier.weight(1f),
                habilitado = !girando,
                primario = eleccion == c,
                metal = estilo.metal
            )
        }
    }
    Spacer(Modifier.height(16.dp))
    SelectorApuesta(apuesta, Ruleta.MINIMA, Ruleta.MAXIMA, Ruleta.PASO, !girando, estilo) { apuesta = it }
    Spacer(Modifier.height(14.dp))
    BotonOdin("Girar", {
        vm.apostar(apuesta) {
            val salio = Ruleta.girar(Random)
            val elegido = eleccion
            val cantidad = apuesta
            scope.launch {
                girando = true
                resultado = null
                // Ángulo (dentro del color que salió) que debe quedar bajo el puntero (arriba, -90°)
                val angulo = if (salio == ColorRuleta.VERDE) Random.nextInt(15, 165) else Random.nextInt(195, 345)
                val base = giro.value
                val ajuste = ((-90f - angulo - base) % 360f + 360f) % 360f
                giro.animateTo(base + 360f * VUELTAS_RULETA + ajuste, tween(DURACION_RULETA_MS, easing = FrenadoRuleta))
                val premio = Ruleta.premio(cantidad, elegido, salio)
                vm.pagar(premio)
                val color = if (salio == ColorRuleta.VERDE) "verde" else "beige"
                resultado = (if (premio > 0) "¡Salió $color! Recibes $premio." else "Salió $color. Perdiste $cantidad.") to (premio > 0)
                girando = false
            }
        }
    }, estilo.acento, Modifier.fillMaxWidth(), habilitado = !girando, metal = estilo.metal)
    NotaReglas("Elige un color. Si aciertas recibes el doble menos el diezmo de Odín (10 %).")
}

// ---------------------------------------------------------------------------
// Tragamonedas
// ---------------------------------------------------------------------------

@Composable
fun TragamonedasJuego(vm: JuegoOcultoViewModel, estilo: EstiloMundo) {
    val scope = rememberCoroutineScope()
    var rodillos by remember { mutableStateOf(listOf(Simbolo.OJO, Simbolo.OJO, Simbolo.OJO)) }
    var girando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    PanelJuego(estilo, "Rodillos de las Nornas", semilla = 6) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            rodillos.forEach { s ->
                Box(
                    Modifier
                        .size(80.dp, 96.dp)
                        .background(Marfil, RoundedCornerShape(10.dp))
                        .border(2.dp, estilo.metal, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.glifo, color = Color(0xFF3A2A12), fontSize = 42.sp, maxLines = 1)
                }
            }
        }
    }

    MensajeResultado(resultado?.first, resultado?.second == true, estilo)
    Spacer(Modifier.height(16.dp))
    BotonOdin("Tirar · ${Tragamonedas.COSTO}", {
        vm.apostar(Tragamonedas.COSTO) {
            val tiro = Tragamonedas.tirar(Random)
            scope.launch {
                girando = true
                resultado = null
                // Los rodillos se detienen uno por uno
                val paradas = listOf(900L, 1400L, 1900L)
                val inicio = System.currentTimeMillis()
                while (true) {
                    val t = System.currentTimeMillis() - inicio
                    rodillos = List(3) { i -> if (t >= paradas[i]) tiro[i] else Tragamonedas.rodillo.random() }
                    if (t >= paradas.last()) break
                    delay(70)
                }
                val premio = Tragamonedas.premio(tiro)
                vm.pagar(premio)
                resultado = (if (premio > 0) "¡Ganaste $premio de hidromiel!" else "Sin premio esta vez.") to (premio > 0)
                girando = false
            }
        }
    }, estilo.acento, Modifier.fillMaxWidth(), habilitado = !girando, metal = estilo.metal)
    Spacer(Modifier.height(16.dp))
    PanelJuego(estilo, "Premios", semilla = 2) {
        Simbolo.entries.reversed().forEach { s ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${s.glifo} ${s.glifo} ${s.glifo}", fontSize = 18.sp, maxLines = 1)
                Text("${s.premioTriple}", style = estiloTitulo(16.sp, color = estilo.acento), maxLines = 1)
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Dos ${Simbolo.CUERNO.glifo}", color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 15.sp, maxLines = 1)
            Text("${Tragamonedas.PREMIO_DOS_CUERNOS}", style = estiloTitulo(16.sp, color = estilo.acento), maxLines = 1)
        }
    }
}

// ---------------------------------------------------------------------------
// Dados de la suerte
// ---------------------------------------------------------------------------

@Composable
fun DadosJuego(vm: JuegoOcultoViewModel, estilo: EstiloMundo) {
    val scope = rememberCoroutineScope()
    var eleccion by remember { mutableStateOf(ApuestaDados.MENOR) }
    var dados by remember { mutableStateOf(3 to 4) }
    var inclinacion by remember { mutableFloatStateOf(0f) }
    var tirando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    PanelJuego(estilo, "Dados de la suerte", semilla = 8) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Dado(dados.first, inclinacion, estilo)
            Dado(dados.second, -inclinacion, estilo)
        }
        Spacer(Modifier.height(10.dp))
        Text("Suma ${dados.first + dados.second}", style = estiloTitulo(20.sp, color = estilo.acento), maxLines = 1)
    }

    MensajeResultado(resultado?.first, resultado?.second == true, estilo)
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ApuestaDados.entries.forEach { a ->
            val paga = if (a == ApuestaDados.SIETE) 50 else 20
            BotonOdin(
                "${a.texto} · $paga", { eleccion = a }, estilo.acento, Modifier.fillMaxWidth(),
                habilitado = !tirando, primario = eleccion == a, metal = estilo.metal
            )
        }
    }
    Spacer(Modifier.height(16.dp))
    BotonOdin("Tirar · ${Dados.APUESTA}", {
        vm.apostar(Dados.APUESTA) {
            val final = Dados.tirar(Random)
            val elegido = eleccion
            scope.launch {
                tirando = true
                resultado = null
                repeat(14) { i ->
                    dados = Random.nextInt(1, 7) to Random.nextInt(1, 7)
                    inclinacion = if (i % 2 == 0) 14f else -14f
                    delay(60L + i * 6)
                }
                dados = final
                inclinacion = 0f
                val suma = final.first + final.second
                val premio = Dados.premio(elegido, suma)
                vm.pagar(premio)
                resultado = (if (premio > 0) "¡Suma $suma! Recibes $premio." else "Suma $suma. Perdiste ${Dados.APUESTA}.") to (premio > 0)
                tirando = false
            }
        }
    }, estilo.acento, Modifier.fillMaxWidth(), habilitado = !tirando, metal = estilo.metal)
    NotaReglas("Apuesta fija de ${Dados.APUESTA}. Menor o mayor que 7 paga 20; exactamente 7 paga 50.")
}

/** Cara de un dado con sus puntos. */
@Composable
private fun Dado(valor: Int, inclinacion: Float, estilo: EstiloMundo) {
    Canvas(
        Modifier
            .size(84.dp)
            .rotate(inclinacion)
            .background(Marfil, RoundedCornerShape(14.dp))
            .border(2.dp, estilo.metal, RoundedCornerShape(14.dp))
            .semantics { contentDescription = "Dado: $valor" }
    ) {
        val c = size.width / 2
        val d = size.width / 4
        val puntos = when (valor) {
            1 -> listOf(Offset(c, c))
            2 -> listOf(Offset(c - d, c - d), Offset(c + d, c + d))
            3 -> listOf(Offset(c - d, c - d), Offset(c, c), Offset(c + d, c + d))
            4 -> listOf(Offset(c - d, c - d), Offset(c + d, c - d), Offset(c - d, c + d), Offset(c + d, c + d))
            5 -> listOf(Offset(c - d, c - d), Offset(c + d, c - d), Offset(c, c), Offset(c - d, c + d), Offset(c + d, c + d))
            else -> listOf(
                Offset(c - d, c - d), Offset(c + d, c - d), Offset(c - d, c), Offset(c + d, c), Offset(c - d, c + d), Offset(c + d, c + d)
            )
        }
        puntos.forEach { drawCircle(estilo.caraOscura, radius = size.width / 12, center = it) }
    }
}
