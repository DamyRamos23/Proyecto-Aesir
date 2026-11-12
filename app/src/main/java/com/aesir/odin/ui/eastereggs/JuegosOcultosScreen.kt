package com.aesir.odin.ui.eastereggs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.eastereggs.Carta
import com.aesir.odin.domain.eastereggs.JuegoOculto
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.DivisorOrnamental
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.IconoCuerno
import com.aesir.odin.ui.components.odin.PanelTallado
import com.aesir.odin.ui.components.odin.SelloRunico
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import com.aesir.odin.ui.theme.Text

/** Cada juego toma el estilo (paisaje, metal y color) de un mundo, como las actividades. */
private fun estiloDe(juego: JuegoOculto): EstiloMundo = when (juego) {
    JuegoOculto.RULETA -> EstilosMundo.Madera
    JuegoOculto.TRAGAMONEDAS -> EstilosMundo.Hierro
    JuegoOculto.BLACKJACK -> EstilosMundo.Piedra
    JuegoOculto.DADOS -> EstilosMundo.Obsidiana
    JuegoOculto.MAYOR_MENOR -> EstilosMundo.Madera
}

private fun runaDe(juego: JuegoOculto) = when (juego) {
    JuegoOculto.RULETA -> "ᛃ"
    JuegoOculto.TRAGAMONEDAS -> "ᚠ"
    JuegoOculto.BLACKJACK -> "ᛒ"
    JuegoOculto.DADOS -> "ᛈ"
    JuegoOculto.MAYOR_MENOR -> "ᛉ"
}

/** Pantalla de un juego oculto: marco común (volver, hidromiel, sello y título) y el juego. */
@Composable
fun JuegoOcultoScreen(juego: JuegoOculto, appContainer: AppContainer, onVolver: () -> Unit) {
    val vm: JuegoOcultoViewModel = viewModel(factory = JuegoOcultoViewModel.factory(appContainer.moduloHidromiel))
    val saldo by vm.saldo.collectAsState(initial = 0)
    val aviso by vm.aviso.collectAsState()
    val estilo = estiloDe(juego)

    FondoMundo(estilo = estilo, alturaPaisaje = 300.dp, paisajeInferior = true) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BotonVolver("Mundos", onVolver)
                Spacer(Modifier.weight(1f))
                ContadorHidromiel(saldo, metal = estilo.metal)
            }
            Spacer(Modifier.height(8.dp))
            SelloRunico(runa = runaDe(juego), estilo = estilo, tamano = 52.dp)
            Spacer(Modifier.height(8.dp))
            Text(juego.nombre, style = estiloTitulo(26.sp, brillo = estilo.acento), maxLines = 1)
            Spacer(Modifier.height(16.dp))
            when (juego) {
                JuegoOculto.RULETA -> RuletaJuego(vm, estilo)
                JuegoOculto.TRAGAMONEDAS -> TragamonedasJuego(vm, estilo)
                JuegoOculto.BLACKJACK -> BlackjackJuego(vm, estilo)
                JuegoOculto.DADOS -> DadosJuego(vm, estilo)
                JuegoOculto.MAYOR_MENOR -> MayorMenorJuego(vm, estilo)
            }
            aviso?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = OdinPaleta.Incorrecto, fontFamily = FuenteTexto, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Pop-up de los 7 toques al contador: un botón por juego y su combinación si ya se descubrió. */
@Composable
fun MenuEastereggs(
    descubiertos: Set<JuegoOculto>,
    todosVisibles: Boolean,
    onElegir: (JuegoOculto) -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = OdinPaleta.HollinAlto,
        title = { Text("Easter eggs", style = estiloTitulo(22.sp, brillo = OdinPaleta.Brasa)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                JuegoOculto.entries.forEach { juego ->
                    val visible = todosVisibles || juego in descubiertos
                    val estilo = estiloDe(juego)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        BotonOdin(
                            if (visible) juego.nombre else "???",
                            { onElegir(juego) },
                            estilo.acento,
                            modifier = Modifier.fillMaxWidth(),
                            habilitado = visible,
                            metal = estilo.metal
                        )
                        Text(
                            when {
                                juego in descubiertos -> juego.textoCombinacion()
                                visible -> "Modo admin: aún no lo descubres"
                                else -> "Descúbrelo tocando los mundos del escudo"
                            },
                            color = OdinPaleta.HuesoTenue,
                            fontFamily = FuenteTexto,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar", color = OdinPaleta.Oro, fontFamily = FuenteTitulo) } }
    )
}

// ---------------------------------------------------------------------------
// Piezas compartidas
// ---------------------------------------------------------------------------

/** Panel tallado del mundo con un rótulo arriba y un divisor ornamental. */
@Composable
fun PanelJuego(estilo: EstiloMundo, titulo: String, semilla: Int = 5, contenido: @Composable ColumnScope.() -> Unit) {
    PanelTallado(estilo = estilo, modifier = Modifier.fillMaxWidth(), semilla = semilla) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titulo.uppercase(), style = estiloTitulo(15.sp, color = estilo.metalClaro), maxLines = 1)
            DivisorOrnamental(estilo.metal, estilo.acento, Modifier.padding(vertical = 10.dp))
            contenido()
        }
    }
}

/** Botón redondo de metal para − y +. */
@Composable
private fun BotonRedondo(texto: String, estilo: EstiloMundo, habilitado: Boolean, descripcion: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .alpha(if (habilitado) 1f else .35f)
            .clip(CircleShape)
            .background(Color(0xF20C0A08))
            .border(1.5.dp, estilo.metal, CircleShape)
            .clickable(enabled = habilitado, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        Text(texto, style = estiloTitulo(22.sp, color = estilo.acento), maxLines = 1)
    }
}

/** − [cuerno] cantidad + entre [minimo] y [maximo]. */
@Composable
fun SelectorApuesta(valor: Int, minimo: Int, maximo: Int, paso: Int, habilitado: Boolean, estilo: EstiloMundo, onCambio: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("APUESTA", style = estiloTitulo(12.sp, color = OdinPaleta.HuesoTenue), maxLines = 1)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonRedondo("−", estilo, habilitado && valor > minimo, "Apostar menos") { onCambio((valor - paso).coerceAtLeast(minimo)) }
            Row(Modifier.width(110.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconoCuerno(tamano = 22.dp, metal = estilo.metal)
                Spacer(Modifier.width(6.dp))
                Text("$valor", style = estiloTitulo(26.sp, color = estilo.acento), maxLines = 1)
            }
            BotonRedondo("+", estilo, habilitado && valor < maximo, "Apostar más") { onCambio((valor + paso).coerceAtMost(maximo)) }
        }
    }
}

@Composable
fun MensajeResultado(texto: String?, gano: Boolean, estilo: EstiloMundo) {
    if (texto == null) return
    Spacer(Modifier.height(14.dp))
    Text(
        texto,
        style = estiloTitulo(18.sp, color = if (gano) estilo.acento else OdinPaleta.Hueso, brillo = if (gano) estilo.acento else null),
        textAlign = TextAlign.Center
    )
}

@Composable
fun NotaReglas(texto: String) {
    Text(
        texto,
        color = OdinPaleta.HuesoTenue,
        fontFamily = FuenteTexto,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 12.dp)
    )
}

/** Carta de la baraja; [oculta] muestra el reverso con la runa del mundo. */
@Composable
fun CartaVista(carta: Carta?, estilo: EstiloMundo, oculta: Boolean = false, grande: Boolean = false) {
    val forma = RoundedCornerShape(if (grande) 10.dp else 7.dp)
    val ancho = if (grande) 100.dp else 52.dp
    val alto = ancho * 1.4f
    if (oculta || carta == null) {
        Box(
            Modifier
                .size(ancho, alto)
                .background(estilo.caraOscura, forma)
                .border(1.5.dp, estilo.metal, forma)
                .semantics { contentDescription = "Carta boca abajo" },
            contentAlignment = Alignment.Center
        ) {
            Text(estilo.runas[0], style = estiloTitulo(if (grande) 34.sp else 20.sp, color = estilo.metal), maxLines = 1)
        }
        return
    }
    val color = if (carta.palo.rojo) Color(0xFFB3261E) else Color(0xFF1B1B1B)
    Box(
        Modifier
            .size(ancho, alto)
            .background(Color(0xFFF7F1E3), forma)
            .border(1.5.dp, estilo.metal, forma)
            .padding(4.dp)
            .semantics(mergeDescendants = true) { contentDescription = "${carta.etiqueta} de ${carta.palo.name.lowercase()}" }
    ) {
        Column(Modifier.align(Alignment.TopStart), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(carta.etiqueta, color = color, fontWeight = FontWeight.Bold, fontSize = if (grande) 22.sp else 14.sp, maxLines = 1)
            Text(carta.palo.glifo, color = color, fontSize = if (grande) 16.sp else 11.sp, maxLines = 1)
        }
        Text(carta.palo.glifo, color = color, fontSize = if (grande) 44.sp else 22.sp, modifier = Modifier.align(Alignment.Center), maxLines = 1)
    }
}

/** Fila de cartas de altura fija (no salta cuando está vacía); se encima si son muchas. */
@Composable
fun FilaCartas(cartas: List<Carta>, estilo: EstiloMundo, ocultarDesde: Int = Int.MAX_VALUE) {
    val traslape = if (cartas.size > 4) (-18).dp else 6.dp
    Box(Modifier.height(52.dp * 1.4f), contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(traslape)) {
            cartas.forEachIndexed { i, carta -> CartaVista(carta, estilo, oculta = i >= ocultarDesde) }
        }
    }
}
