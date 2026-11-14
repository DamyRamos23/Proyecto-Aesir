package com.aesir.odin.ui.forja

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.CalidadArtefacto
import com.aesir.odin.domain.model.EstadoFinalForja
import com.aesir.odin.domain.model.ResultadoForja
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.OdinPaleta

/**
 * Paso 19 y A16.2 (RF-46): resultado de la forja. Muestra el artefacto
 * perfecto o roto, los errores, el hidromiel ganado o perdido y, si se obtuvo
 * el artefacto, el poder que otorga.
 */
@Composable
fun ResultadoForjaScreen(resultado: ResultadoForja, onVolver: () -> Unit) {
    val perfecto = resultado.resultado == EstadoFinalForja.PERFECTO
    val artefacto = resultado.artefacto

    MarcoForja {
        Spacer(Modifier.height(16.dp))
        Text(
            when {
                perfecto && artefacto != null -> "¡ARTEFACTO PERFECTO!"
                perfecto -> "¡FORJA PERFECTA!"
                else -> "ARTEFACTO ROTO"
            },
            style = estiloTitulo(28.sp, OdinPaleta.Brasa, brillo = if (perfecto) OdinPaleta.Brasa.copy(alpha = .6f) else null),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        IconoArtefacto(artefacto?.id, tamano = 190.dp, roto = !perfecto)
        Spacer(Modifier.height(12.dp))
        Pergamino(Modifier.fillMaxWidth()) {
            TextoPergamino(
                when {
                    perfecto -> "Sin errores. La forja fue un éxito."
                    resultado.errores == 1 -> "La forja tuvo 1 error. Perdiste tu apuesta."
                    else -> "La forja tuvo ${resultado.errores} errores. Perdiste tu apuesta."
                },
                17
            )
        }
        Spacer(Modifier.height(16.dp))
        if (perfecto) {
            LineaHidromiel("+${resultado.hidromielGanado} hidromiel", OdinPaleta.Brasa)
            TextoClaro("Apuesta de ${resultado.apuesta} ×3 menos el diezmo de Odín", 13, OdinPaleta.HuesoTenue)
        } else {
            LineaHidromiel("−${resultado.hidromielPerdido} hidromiel", OdinPaleta.Incorrecto)
        }
        Spacer(Modifier.height(10.dp))

        if (perfecto && artefacto != null) {
            TextoClaro("${artefacto.nombre} se añadió a tu vitrina", 15)
            Spacer(Modifier.height(10.dp))
            PlacaOscura(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = OdinPaleta.Brasa, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.size(8.dp))
                    TextoClaro("Poder: ${artefacto.poder(CalidadArtefacto.PERFECTO)}", 14)
                }
            }
        } else if (perfecto) {
            TextoClaro("Ya tenías perfectos todos tus artefactos: esta forja fue solo por hidromiel.", 14, OdinPaleta.HuesoTenue)
        } else {
            PlacaOscura(Modifier.fillMaxWidth()) {
                TextoClaro(
                    "Errores: ${resultado.errores}" + if (artefacto != null) "\nEl artefacto no se entregó." else "",
                    15
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        BotonOdin("Volver al Valhalla", onVolver, OdinPaleta.Brasa, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
    }
}
