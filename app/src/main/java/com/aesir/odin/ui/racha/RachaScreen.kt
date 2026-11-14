package com.aesir.odin.ui.racha

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aesir.odin.domain.racha.EstadoRachaDia
import com.aesir.odin.domain.racha.ReglasRacha
import com.aesir.odin.domain.racha.ResumenRacha
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.forja.BarraForja
import com.aesir.odin.ui.forja.LineaHidromiel
import com.aesir.odin.ui.forja.MarcoForja
import com.aesir.odin.ui.forja.Pergamino
import com.aesir.odin.ui.forja.PlacaOscura
import com.aesir.odin.ui.forja.TextoClaro
import com.aesir.odin.ui.forja.TextoPergamino
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlinx.coroutines.delay

/**
 * Pantalla de la racha (MOD-05 · La carrera de Sól): Sól y Sköll según el
 * estado del día, la racha actual y la máxima, las ofrendas, los títulos y
 * los hitos. Desde aquí se compra la ofrenda a Sól.
 */
@Composable
fun RachaScreen(viewModel: RachaViewModel, onVolver: () -> Unit) {
    val resumen by viewModel.resumen.collectAsState()
    val confirmacion by viewModel.confirmacion.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val procesando by viewModel.procesando.collectAsState()
    val saldo by viewModel.saldo.collectAsState()

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            delay(4000)
            viewModel.limpiarMensaje()
        }
    }

    MarcoForja {
        BarraForja("Mundos", onVolver, saldo)
        Spacer(Modifier.height(4.dp))
        Text("LA CARRERA DE SÓL", style = estiloTitulo(26.sp, ColorSol), textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        val r = resumen
        if (r == null) {
            Spacer(Modifier.height(80.dp))
            CircularProgressIndicator(color = ColorSol)
        } else {
            ContenidoRacha(r, saldo, procesando, mensaje, viewModel::solicitarOfrenda)
        }
    }

    confirmacion?.let { ofrenda ->
        Dialog(onDismissRequest = viewModel::cancelarOfrenda) {
            Column(
                Modifier
                    .clip(CutCornerShape(10.dp))
                    .background(Color(0xFF15110C))
                    .border(1.5.dp, ColorSol.copy(alpha = .7f), CutCornerShape(10.dp))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¿OFRENDA A SÓL?", style = estiloTitulo(22.sp, ColorSol))
                Spacer(Modifier.height(10.dp))
                IconoSol(EstadoRachaDia.ENCENDIDA, tamano = 70.dp)
                Spacer(Modifier.height(10.dp))
                TextoClaro("La ofrenda protege tu racha un día sin actividad y se usa sola cuando la necesitas.", 15)
                Spacer(Modifier.height(10.dp))
                LineaHidromiel("Cuesta ${ofrenda.costo} hidromiel")
                TextoClaro("Tienes $saldo · ofrendas guardadas: ${ofrenda.ofrendas} de ${ReglasRacha.MAX_OFRENDAS}", 13, OdinPaleta.HuesoTenue)
                Spacer(Modifier.height(18.dp))
                BotonOdin("Ofrecer", viewModel::confirmarOfrenda, ColorSol, Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                BotonOdin("Cancelar", viewModel::cancelarOfrenda, ColorSol, Modifier.fillMaxWidth(), primario = false)
            }
        }
    }
}

@Composable
private fun ContenidoRacha(
    r: ResumenRacha,
    saldo: Int,
    procesando: Boolean,
    mensaje: String?,
    onOfrenda: () -> Unit
) {
    val e = r.estado
    EscenaCarrera(e.estado)
    Spacer(Modifier.height(10.dp))
    val (titulo, texto) = when (e.estado) {
        EstadoRachaDia.ENCENDIDA -> "SÓL BRILLA" to "Ya estudiaste hoy: tu racha está encendida."
        EstadoRachaDia.EN_RIESGO -> "SKÖLL SE ACERCA" to "Aún no estudias hoy. Termina una actividad para que Sól siga corriendo."
        EstadoRachaDia.PROTEGIDA -> "SÓL ESTÁ A SALVO" to "Una ofrenda protegió tu racha del día que faltaste."
        EstadoRachaDia.APAGADA -> "SÓL ESTÁ OCULTA" to "Termina una actividad de aprendizaje para encender tu racha."
    }
    Text(titulo, color = colorDe(e.estado), fontFamily = FuenteTitulo, fontSize = 20.sp, letterSpacing = 2.sp)
    TextoClaro(texto, 15)
    Spacer(Modifier.height(14.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Dato("Racha actual", if (e.actual == 1) "1 día" else "${e.actual} días", Modifier.weight(1f))
        Dato("Mejor racha", if (e.maxima == 1) "1 día" else "${e.maxima} días", Modifier.weight(1f))
    }
    Spacer(Modifier.height(10.dp))

    // Ofrendas guardadas
    PlacaOscura(Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("OFRENDAS A SÓL", color = OdinPaleta.Oro, fontFamily = FuenteTitulo, fontSize = 13.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "${e.ofrendas} de ${ReglasRacha.MAX_OFRENDAS} ofrendas guardadas"
                }
            ) {
                repeat(ReglasRacha.MAX_OFRENDAS) { i ->
                    IconoSol(if (i < e.ofrendas) EstadoRachaDia.ENCENDIDA else EstadoRachaDia.APAGADA, tamano = 30.dp)
                }
            }
            Spacer(Modifier.height(6.dp))
            TextoClaro("Cada ofrenda protege un día sin actividad. Puedes guardar hasta ${ReglasRacha.MAX_OFRENDAS}.", 13, OdinPaleta.HuesoTenue)
            Spacer(Modifier.height(10.dp))
            BotonOdin(
                "Ofrenda a Sól · ${ReglasRacha.COSTO_OFRENDA}",
                onOfrenda,
                ColorSol,
                Modifier.fillMaxWidth(),
                habilitado = !procesando && e.ofrendas < ReglasRacha.MAX_OFRENDAS
            )
            if (e.ofrendas >= ReglasRacha.MAX_OFRENDAS) {
                TextoClaro("Ya tienes el máximo de ofrendas.", 13, OdinPaleta.HuesoTenue)
            }
        }
    }
    if (mensaje != null) {
        Spacer(Modifier.height(8.dp))
        TextoClaro(mensaje, 14, ColorSol)
    }

    if (r.titulos.isNotEmpty()) {
        Spacer(Modifier.height(14.dp))
        Text("TÍTULOS", color = OdinPaleta.Oro, fontFamily = FuenteTitulo, fontSize = 13.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        r.titulos.forEach { titulo ->
            Pergamino(Modifier.fillMaxWidth().padding(vertical = 3.dp)) { TextoPergamino("«$titulo»", 16, negrita = true) }
        }
    }

    Spacer(Modifier.height(14.dp))
    Text("HITOS", color = OdinPaleta.Oro, fontFamily = FuenteTitulo, fontSize = 13.sp, letterSpacing = 2.sp)
    Spacer(Modifier.height(6.dp))
    val siguiente = r.hitos.firstOrNull { it.dias !in r.hitosCobrados && it.dias > e.actual }
    r.hitos.forEach { hito ->
        val cobrado = hito.dias in r.hitosCobrados
        val forma = CutCornerShape(6.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .clip(forma)
                .background(Color(0xE60B0907))
                .border(if (hito == siguiente) 2.dp else 1.dp, if (cobrado || hito == siguiente) ColorSol else OdinPaleta.Hierro, forma)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${hito.dias} días", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontSize = 16.sp, modifier = Modifier.width(90.dp))
            Column(Modifier.weight(1f)) {
                Text("+${hito.bono} de hidromiel", color = ColorSol, fontFamily = FuenteTexto, fontSize = 14.sp)
                hito.titulo?.let { Text("Título «$it»", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 13.sp) }
            }
            if (cobrado) Icon(Icons.Filled.Check, contentDescription = "Cobrado", tint = ColorSol)
            else if (hito == siguiente) Text("faltan ${hito.dias - e.actual}", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 12.sp)
        }
    }
    Spacer(Modifier.height(10.dp))
    TextoClaro("Solo la primera actividad terminada de cada día suma a la racha. Abrir la app o la pregunta flash no cuentan.", 12, OdinPaleta.HuesoTenue)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Dato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Pergamino(modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextoPergamino(etiqueta, 13)
            Text(valor, color = OdinPaleta.Tinta, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
}

/** Sól corre por el cielo y Sköll la persigue; la distancia depende del estado del día. */
@Composable
private fun EscenaCarrera(estado: EstadoRachaDia) {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(CutCornerShape(8.dp))
            .border(1.dp, OdinPaleta.Hierro, CutCornerShape(8.dp))
            .semantics { contentDescription = "Sól y el lobo Sköll" }
    ) {
        val w = size.width
        val h = size.height
        val dia = estado == EstadoRachaDia.ENCENDIDA || estado == EstadoRachaDia.PROTEGIDA
        drawRect(
            Brush.verticalGradient(
                if (dia) listOf(Color(0xFF3A2A12), Color(0xFF130F0A)) else listOf(Color(0xFF0B1220), Color(0xFF0D0B0A))
            )
        )
        // Arco del cielo por donde corre Sól
        drawArc(
            OdinPaleta.Hierro.copy(alpha = .5f), 180f, 180f, false,
            topLeft = Offset(w * .08f, h * .2f), size = androidx.compose.ui.geometry.Size(w * .84f, h * 1.3f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f)
        )
        val sol = when (estado) {
            EstadoRachaDia.ENCENDIDA -> Offset(w * .7f, h * .32f)
            EstadoRachaDia.PROTEGIDA -> Offset(w * .62f, h * .34f)
            EstadoRachaDia.EN_RIESGO -> Offset(w * .5f, h * .36f)
            EstadoRachaDia.APAGADA -> Offset(w * .4f, h * .42f)
        }
        dibujarSol(sol, h * .26f, estado)
        // Sköll: lejos si Sól brilla, cerca si está en riesgo, encima si se apagó
        val lobo = when (estado) {
            EstadoRachaDia.ENCENDIDA -> Offset(w * .06f, h * .9f)
            EstadoRachaDia.PROTEGIDA -> Offset(w * .1f, h * .9f)
            EstadoRachaDia.EN_RIESGO -> Offset(w * .22f, h * .7f)
            EstadoRachaDia.APAGADA -> Offset(w * .26f, h * .62f)
        }
        dibujarLobo(lobo, h * .3f, Color(0xFF4A4250))
        drawLine(OdinPaleta.Hierro, Offset(0f, h * .92f), Offset(w, h * .92f), 2f)
    }
}
