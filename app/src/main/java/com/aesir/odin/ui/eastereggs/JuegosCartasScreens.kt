package com.aesir.odin.ui.eastereggs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.eastereggs.Blackjack
import com.aesir.odin.domain.eastereggs.ManoJugador
import com.aesir.odin.domain.eastereggs.MayorMenor
import com.aesir.odin.domain.eastereggs.Prediccion
import com.aesir.odin.domain.eastereggs.valorMano
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.OdinPaleta
import com.aesir.odin.ui.theme.Text
import kotlin.random.Random

/** Placa pequeña con un número (puntos de una mano, bote…). */
@Composable
private fun Placa(texto: String, estilo: EstiloMundo, resaltada: Boolean = false) {
    val forma = CutCornerShape(6.dp)
    Text(
        texto,
        style = estiloTitulo(15.sp, color = if (resaltada) estilo.acento else OdinPaleta.Hueso),
        maxLines = 1,
        modifier = Modifier
            .border(1.dp, if (resaltada) estilo.acento else estilo.metal.copy(alpha = .6f), forma)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

// ---------------------------------------------------------------------------
// Blackjack
// ---------------------------------------------------------------------------

@Composable
fun BlackjackJuego(vm: JuegoOcultoViewModel, estilo: EstiloMundo) {
    var apuesta by remember { mutableIntStateOf(10) }
    var juego by remember { mutableStateOf<Blackjack?>(null) }
    var pagado by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    // El juego es un objeto mutable: este contador hace que la pantalla se redibuje tras cada jugada.
    var jugadas by remember { mutableIntStateOf(0) }
    jugadas.let { } // se lee para redibujar tras cada jugada

    fun revisar() {
        jugadas++
        val j = juego ?: return
        if (j.terminado && !pagado) {
            pagado = true
            val premio = j.premio()
            vm.pagar(premio)
            val apostado = j.manos.sumOf { it.apuesta }
            resultado = when {
                premio > apostado -> "¡Ganaste! Recibes $premio de hidromiel." to true
                premio == apostado -> "Empate: recuperas $premio." to false
                premio > 0 -> "Recuperas $premio de $apostado." to false
                else -> "La casa gana. Perdiste $apostado." to false
            }
        }
    }

    val j = juego
    PanelJuego(estilo, "Mesa", semilla = 7) {
        // La casa
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("LA CASA", style = estiloTitulo(13.sp, color = OdinPaleta.HuesoTenue), maxLines = 1)
            if (j != null) {
                Spacer(Modifier.padding(start = 8.dp))
                Placa(if (j.terminado) "${valorMano(j.crupier)}" else "${j.crupier[0].puntos} + ?", estilo)
            }
        }
        Spacer(Modifier.height(8.dp))
        FilaCartas(j?.crupier.orEmpty(), estilo, ocultarDesde = if (j == null || j.terminado) Int.MAX_VALUE else 1)

        Spacer(Modifier.height(18.dp))

        // El alumno: una o dos manos (si dividió)
        Text("TUS CARTAS", style = estiloTitulo(13.sp, color = OdinPaleta.HuesoTenue), maxLines = 1)
        Spacer(Modifier.height(8.dp))
        if (j == null) {
            FilaCartas(emptyList(), estilo)
        } else {
            j.manos.forEachIndexed { i, mano -> ManoVista(mano, estilo, activa = !j.terminado && i == j.indiceActual, varias = j.manos.size > 1) }
        }
    }

    MensajeResultado(resultado?.first, resultado?.second == true, estilo)
    Spacer(Modifier.height(16.dp))

    if (j == null || j.terminado) {
        SelectorApuesta(apuesta, 10, 50, 5, true, estilo) { apuesta = it }
        Spacer(Modifier.height(14.dp))
        BotonOdin("Repartir", {
            vm.apostar(apuesta) {
                juego = Blackjack(apuesta, Random)
                pagado = false
                resultado = null
                revisar()
            }
        }, estilo.acento, Modifier.fillMaxWidth(), metal = estilo.metal)
    } else {
        val mano = j.manoActual
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonOdin("Pedir", { j.pedir(); revisar() }, estilo.acento, Modifier.weight(1f), metal = estilo.metal)
                BotonOdin("Plantarse", { j.plantarse(); revisar() }, estilo.acento, Modifier.weight(1f), metal = estilo.metal)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonOdin("Doblar", {
                    if (mano != null) vm.apostar(mano.apuesta) { j.doblar(); revisar() }
                }, estilo.acento, Modifier.weight(1f), habilitado = j.puedeDoblar, primario = false, metal = estilo.metal)
                BotonOdin("Dividir", {
                    if (mano != null) vm.apostar(mano.apuesta) { j.dividir(); revisar() }
                }, estilo.acento, Modifier.weight(1f), habilitado = j.puedeDividir, primario = false, metal = estilo.metal)
            }
        }
    }
    NotaReglas("La casa pide hasta 16 y se planta en 17. Blackjack paga 3:2 y el empate devuelve la apuesta. Doblar y dividir cobran otra apuesta igual.")
}

@Composable
private fun ManoVista(mano: ManoJugador, estilo: EstiloMundo, activa: Boolean, varias: Boolean) {
    val forma = CutCornerShape(8.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .border(1.5.dp, if (activa && varias) estilo.acento else Color.Transparent, forma)
            .padding(8.dp)
    ) {
        FilaCartas(mano.cartas, estilo)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Placa(if (mano.sePaso) "${mano.valor} · te pasaste" else "${mano.valor}", estilo, resaltada = activa)
            Placa("Apuesta ${mano.apuesta}", estilo)
        }
    }
}

// ---------------------------------------------------------------------------
// Mayor o menor
// ---------------------------------------------------------------------------

@Composable
fun MayorMenorJuego(vm: JuegoOcultoViewModel, estilo: EstiloMundo) {
    var apuesta by remember { mutableIntStateOf(10) }
    var juego by remember { mutableStateOf<MayorMenor?>(null) }
    var resultado by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var jugadas by remember { mutableIntStateOf(0) }
    jugadas.let { } // se lee para redibujar tras cada jugada

    fun jugar(p: Prediccion) {
        val j = juego ?: return
        val acerto = j.adivinar(p)
        jugadas++
        resultado = when {
            !acerto -> "Salió ${j.ultima?.etiqueta}${j.ultima?.palo?.glifo}. Perdiste el bote." to false
            j.terminado -> { vm.pagar(j.bote); "¡Llegaste al tope! Recibes ${j.bote} de hidromiel." to true }
            else -> "¡Acertaste! El bote sube a ${j.bote}." to true
        }
    }

    val j = juego
    PanelJuego(estilo, "Baraja", semilla = 4) {
        CartaVista(j?.actual, estilo, oculta = j == null, grande = true)
        Spacer(Modifier.height(12.dp))
        if (j != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Placa("Bote ${j.bote}", estilo, resaltada = true)
                Placa("Tope ${j.tope}", estilo)
                Placa("Aciertos ${j.aciertos}", estilo)
            }
        }
    }

    MensajeResultado(resultado?.first, resultado?.second == true, estilo)
    Spacer(Modifier.height(16.dp))

    if (j == null || j.terminado) {
        SelectorApuesta(apuesta, 10, 50, 5, true, estilo) { apuesta = it }
        Spacer(Modifier.height(14.dp))
        BotonOdin("Sacar carta", {
            vm.apostar(apuesta) {
                juego = MayorMenor(apuesta, Random)
                resultado = null
                jugadas++
            }
        }, estilo.acento, Modifier.fillMaxWidth(), metal = estilo.metal)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Prediccion.entries.forEach { p ->
                    val mult = j.multiplicadorPorCiento(p)
                    BotonOdin(
                        "${if (p == Prediccion.MAYOR) "Mayor" else "Menor"} ×${mult / 100}.${"%02d".format(mult % 100)}",
                        { jugar(p) },
                        estilo.acento,
                        Modifier.weight(1f),
                        habilitado = mult > 0,
                        metal = estilo.metal
                    )
                }
            }
            BotonOdin("Cobrar ${j.bote}", {
                val premio = j.cobrar()
                vm.pagar(premio)
                jugadas++
                resultado = "Cobraste $premio de hidromiel." to true
            }, estilo.acento, Modifier.fillMaxWidth(), habilitado = j.puedeCobrar, primario = false, metal = estilo.metal)
        }
    }
    NotaReglas("El As es la carta más alta y si sale una carta igual pierdes. Mientras menos probable sea tu elección, más sube el bote.")
}
