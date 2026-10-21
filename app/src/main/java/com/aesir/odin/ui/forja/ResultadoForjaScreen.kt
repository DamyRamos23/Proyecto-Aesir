package com.aesir.odin.ui.forja

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
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
    if (perfecto) RevelacionResultado(resultado, onVolver) else ArtefactoRoto(resultado, onVolver)
}

@Composable
private fun RevelacionResultado(resultado: ResultadoForja, onVolver: () -> Unit) {
    val artefacto = resultado.artefacto
    MarcoForja(tinte = Color(0xFF4A2A0E)) {
        Spacer(Modifier.height(8.dp))
        RielEtapas(actual = 4)
        Spacer(Modifier.height(4.dp))
        Text("LA FORJA HA TERMINADO", color = Color(0xFFFFC266), fontFamily = FuenteTitulo, fontSize = 12.sp, letterSpacing = 3.sp)
        RevelacionArtefacto(artefacto?.id)
        Box(
            Modifier
                .apareceTras(1100)
                .background(Color(0x24FF9A3C), RoundedCornerShape(13.dp))
                .border(1.dp, Color(0x8CFFC266), RoundedCornerShape(13.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                if (artefacto != null) "ARTEFACTO FORJADO" else "FORJA PERFECTA",
                color = Color(0xFFFFC266), fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold,
                fontSize = 11.sp, letterSpacing = 2.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        TituloDorado(artefacto?.nombre ?: "Hidromiel")
        Text(
            if (artefacto != null) "Del mundo ${artefacto.mundoNombre} · forjado por Brokkr"
            else "Ya tenías todos tus artefactos: esta forja fue solo por hidromiel.",
            color = Color(0xFFC9B48A), fontFamily = FuenteTexto, fontStyle = FontStyle.Italic, fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.apareceTras(1350).padding(top = 6.dp)
        )
        if (artefacto != null) {
            Spacer(Modifier.height(14.dp))
            Pergamino(Modifier.fillMaxWidth().apareceTras(1600)) {
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = OdinPaleta.TintaRoja, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("PODER EN HASTA EL ALBA", color = OdinPaleta.TintaRoja, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.5.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(artefacto.poder, color = OdinPaleta.Tinta, fontFamily = FuenteTexto, fontSize = 16.sp, lineHeight = 21.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().apareceTras(1850), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DatoResultado("+${resultado.hidromielGanado}", "HIDROMIEL", Color(0xFFFFC266), Modifier.weight(1f))
            DatoResultado("${resultado.apuesta} ×3", "APUESTA", OdinPaleta.Hueso, Modifier.weight(1f))
            DatoResultado("0", "ERRORES", Color(0xFFDDEAF2), Modifier.weight(1f))
        }
        Text(
            "Apuesta ×3 menos el diezmo de Odín" + if (artefacto != null) " · ${artefacto.nombre} ya está en tu vitrina" else "",
            color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 12.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(16.dp))
        Box(Modifier.apareceTras(2100).fillMaxWidth()) {
            BotonOdin("Volver al Valhalla", onVolver, OdinPaleta.Brasa, Modifier.fillMaxWidth())
        }
        if (artefacto?.id == "art_mjolnir") {
            Text(
                "El mango quedó corto: la mosca de Loki picó a Brokkr en el párpado.",
                color = Color(0xFF8C7A5C), fontFamily = FuenteTexto, fontStyle = FontStyle.Italic, fontSize = 12.sp,
                textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ArtefactoRoto(resultado: ResultadoForja, onVolver: () -> Unit) {
    val artefacto = resultado.artefacto
    MarcoForja {
        Spacer(Modifier.height(16.dp))
        Text("ARTEFACTO ROTO", style = estiloTitulo(28.sp, OdinPaleta.Brasa), textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        IconoArtefacto(artefacto?.id, tamano = 190.dp, roto = true)
        Spacer(Modifier.height(12.dp))
        Pergamino(Modifier.fillMaxWidth()) {
            TextoPergamino(
                if (resultado.errores == 1) "La forja tuvo 1 error. Perdiste tu apuesta."
                else "La forja tuvo ${resultado.errores} errores. Perdiste tu apuesta.",
                17
            )
        }
        Spacer(Modifier.height(16.dp))
        LineaHidromiel("−${resultado.hidromielPerdido} hidromiel", OdinPaleta.Incorrecto)
        Spacer(Modifier.height(10.dp))
        PlacaOscura(Modifier.fillMaxWidth()) {
            TextoClaro(
                "Errores: ${resultado.errores}" + if (artefacto != null) "\nEl artefacto no se entregó." else "",
                15
            )
        }
        Spacer(Modifier.height(24.dp))
        BotonOdin("Volver al Valhalla", onVolver, OdinPaleta.Brasa, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
    }
}
