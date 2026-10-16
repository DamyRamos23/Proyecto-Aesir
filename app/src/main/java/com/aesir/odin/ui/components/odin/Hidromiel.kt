package com.aesir.odin.ui.components.odin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.hidromiel.AvisoHidromiel
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import com.aesir.odin.domain.hidromiel.TipoMovimiento
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onSubscription

/** Color del hidromiel (miel oscura). */
val ColorHidromiel = Color(0xFFE2A746)

/** Cuerno nórdico para beber, el ícono del hidromiel. */
@Composable
fun IconoCuerno(modifier: Modifier = Modifier, tamano: Dp = 18.dp, metal: Color = OdinPaleta.Oro) {
    Canvas(modifier.size(tamano)) {
        val w = size.width
        val h = size.height
        val cuerpo = Path().apply {
            moveTo(w * .58f, h * .20f)
            quadraticTo(w * .52f, h * .72f, w * .08f, h * .90f)
            quadraticTo(w * .70f, h * .92f, w * .96f, h * .22f)
            close()
        }
        drawPath(
            cuerpo,
            Brush.linearGradient(
                listOf(Color(0xFFF3D9A4), ColorHidromiel, Color(0xFF7A4A16)),
                start = Offset(w, 0f), end = Offset(0f, h)
            )
        )
        drawPath(cuerpo, Color(0xFF2A1A0A), style = Stroke(w * .05f))
        // Boca del cuerno con el hidromiel
        val boca = Size(w * .42f, h * .16f)
        val esquina = Offset(w * .56f, h * .12f)
        drawOval(Color(0xFF8C5518), esquina, boca)
        drawOval(metal, esquina, boca, style = Stroke(w * .06f))
        // Anillos de metal
        drawLine(metal, Offset(w * .50f, h * .52f), Offset(w * .80f, h * .56f), w * .06f)
        drawLine(metal, Offset(w * .26f, h * .78f), Offset(w * .44f, h * .82f), w * .05f)
    }
}

/** Saldo de hidromiel en una placa pequeña. */
@Composable
fun ContadorHidromiel(saldo: Int, modifier: Modifier = Modifier, metal: Color = OdinPaleta.Oro) {
    val forma = CutCornerShape(6.dp)
    Row(
        modifier
            .clip(forma)
            .background(Color(0xE60B0907))
            .border(1.dp, metal.copy(alpha = .8f), forma)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Hidromiel: $saldo" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconoCuerno(tamano = 18.dp, metal = metal)
        Spacer(Modifier.width(6.dp))
        Text(
            "$saldo",
            color = ColorHidromiel,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

/**
 * Muestra arriba de la pantalla, uno por uno, los avisos del MOD-01
 * (lo ganado o perdido). [alSuscribir] corre en cuanto ya se están
 * escuchando los avisos; ahí se reclama la recompensa diaria para que su
 * aviso no se pierda.
 */
@Composable
fun AvisosHidromiel(
    avisos: SharedFlow<AvisoHidromiel>,
    modifier: Modifier = Modifier,
    alSuscribir: suspend () -> Unit = {}
) {
    var actual by remember { mutableStateOf<AvisoHidromiel?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(avisos) {
        avisos
            .onSubscription { runCatching { alSuscribir() } }
            // Solo se anuncian las recompensas (diaria y por nivel con premio):
            // los cobros y demás movimientos ya se ven en el contador.
            .filter { it.tipo == TipoMovimiento.RECOMPENSA_DIARIA || (it.tipo == TipoMovimiento.RECOMPENSA_NIVEL && it.cantidad > 0) }
            .collect { aviso ->
                actual = aviso
                visible = true
                delay(2800)
                visible = false
                delay(350)
            }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(260)) { -it } + fadeIn(tween(260)),
        exit = slideOutVertically(tween(260)) { -it } + fadeOut(tween(260)),
        modifier = modifier
    ) {
        actual?.let { PlacaAviso(it) }
    }
}

@Composable
private fun PlacaAviso(aviso: AvisoHidromiel) {
    val forma = CutCornerShape(10.dp)
    val color = when {
        aviso.cantidad > 0 -> ColorHidromiel
        aviso.cantidad < 0 -> OdinPaleta.Incorrecto
        else -> OdinPaleta.HuesoTenue
    }
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .widthIn(max = 460.dp)
            .fillMaxWidth()
            .clip(forma)
            .background(Color(0xF2120F0C))
            .border(1.dp, color.copy(alpha = .9f), forma)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconoCuerno(tamano = 30.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            if (aviso.cantidad != 0) {
                Text(
                    (if (aviso.cantidad > 0) "+" else "−") + "${kotlin.math.abs(aviso.cantidad)} de hidromiel",
                    color = color,
                    fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
            Text(aviso.mensaje, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 14.sp, lineHeight = 18.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text("${aviso.saldoActual}", color = ColorHidromiel, fontFamily = FuenteTitulo, fontSize = 15.sp)
    }
}
