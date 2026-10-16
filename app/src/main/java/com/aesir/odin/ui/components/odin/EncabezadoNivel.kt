package com.aesir.odin.ui.components.odin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.theme.EstiloMundo

/**
 * Encabezado de las pantallas de un nivel (memorama, diagnóstico): flecha con
 * el nombre del mundo a la izquierda, emblema del mundo al centro y, si se
 * pasa [saldo], el contador de hidromiel a la derecha.
 */
@Composable
fun EncabezadoNivel(
    estilo: EstiloMundo,
    textoVolver: String,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    saldo: Int? = null
) {
    Box(modifier.fillMaxWidth()) {
        BotonVolver(
            textoVolver,
            onClick = onVolver,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .widthIn(max = 150.dp)
        )
        Canvas(
            Modifier
                .size(44.dp)
                .align(Alignment.Center)
        ) {
            val r = size.minDimension / 2f
            mancha(center, r * 1.5f, estilo.acento, .25f)
            drawCircle(Color(0xFF0D0A07), r)
            drawCircle(estilo.metal, r - 1.dp.toPx(), style = Stroke(2.dp.toPx()))
            drawCircle(estilo.metal.copy(alpha = .4f), r - 5.dp.toPx(), style = Stroke(1.dp.toPx()))
            dibujarEmblema(estilo.emblema, center, r * .62f, estilo.acento)
        }
        if (saldo != null) {
            ContadorHidromiel(saldo = saldo, metal = estilo.metal, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

/**
 * Fila de [total] runas del mundo. Las de [encendidas] brillan con el color
 * del mundo, la de [actual] lleva un aro y las demás quedan apagadas.
 */
@Composable
fun FilaRunas(
    estilo: EstiloMundo,
    total: Int,
    encendidas: Set<Int>,
    actual: Int?,
    modifier: Modifier = Modifier,
    tamano: Int = 18
) {
    Row(
        modifier.clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            val runa = estilo.runas[i % estilo.runas.size]
            Box(Modifier.size((tamano + 8).dp), contentAlignment = Alignment.Center) {
                if (i == actual) {
                    Canvas(Modifier.size((tamano + 8).dp)) {
                        drawCircle(estilo.acento, size.minDimension / 2f - 1.dp.toPx(), style = Stroke(1.5.dp.toPx()))
                    }
                }
                Text(
                    runa,
                    style = estiloRuna(tamano.sp, if (i in encendidas || i == actual) estilo.acento else estilo.metal, i in encendidas),
                    modifier = Modifier.alpha(if (i in encendidas || i == actual) 1f else .35f)
                )
            }
        }
    }
}

/** Franja de runas tenues con el divisor ornamental, como borde del tablero. */
@Composable
fun FranjaRunica(estilo: EstiloMundo, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().clearAndSetSemantics { }, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            (estilo.runas + estilo.runas + estilo.runas).joinToString("  "),
            color = estilo.metal.copy(alpha = .45f),
            fontSize = 10.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        DivisorOrnamental(color = estilo.metal, acento = estilo.acento)
    }
}
