package com.aesir.odin.ui.forja

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.Artefacto
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.ColoresInicio
import com.aesir.odin.ui.components.odin.EscenaAnimada
import com.aesir.odin.ui.components.odin.EscenaModo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlin.math.sin

/**
 * La vitrina del salón (CU-01 y CU-02). Es una sola pantalla que cambia según
 * el modo desde el que se abre:
 * - [ModoVitrina.FORJA]: desde la Apuesta de Brokkr, para ver la colección
 *   (qué artefactos tiene el alumno, de qué mundo salen y su poder). Lleva el
 *   tema de la forja: brasas sobre la escena de los enanos.
 * - [ModoVitrina.ALBA]: desde Hasta el Alba, antes de bajar al campo, para
 *   elegir qué artefactos lleva a la noche. Lleva el tema del banquete al
 *   amanecer.
 * Un artefacto se tiene completo o no se tiene; los que faltan se ven como
 * siluetas «por forjar».
 */
enum class ModoVitrina(
    val escena: EscenaModo,
    val acento: Color,
    val brillo: Color,
    val titulo: String,
    val subtitulo: String
) {
    FORJA(
        EscenaModo.BROKKR, Color(0xFFFF9A3C), Color(0xFFFFC266),
        "La vitrina del salón",
        "Los tesoros que has forjado con Eitri y Brokkr. Cada uno sale de un mundo superado."
    ),
    ALBA(
        EscenaModo.ALBA, Color(0xFFF0A35A), Color(0xFFFFE2A8),
        "Armas para la noche",
        "Elige qué artefactos bajas al campo. Cada uno se usa una vez por noche."
    )
}

@Composable
fun VitrinaScreen(
    modo: ModoVitrina,
    tiene: Set<Artefacto>,
    onVolver: () -> Unit,
    seleccion: Set<Artefacto> = emptySet(),
    onAlternar: (Artefacto) -> Unit = {},
    onListo: () -> Unit = onVolver
) {
    val eligiendo = modo == ModoVitrina.ALBA
    Box(Modifier.fillMaxSize().background(ColoresInicio.Fondo)) {
        // Fondo: la escena del modo, en reposo y oscurecida para que se lea la vitrina
        EscenaAnimada(modo.escena, activa = false, modifier = Modifier.fillMaxSize().alpha(.55f))
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x8806070D), Color(0xEE06070D), Color(0xFF06070D)))
            )
        )
        BrasasFlotantes(modo.acento, Modifier.fillMaxSize())

        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color(0x9906070D))
                        .border(1.dp, modo.acento.copy(alpha = .5f), CircleShape)
                        .clickable(role = Role.Button, onClick = onVolver)
                        .semantics { contentDescription = "Volver" },
                    contentAlignment = Alignment.Center
                ) { Text("‹", color = modo.brillo, fontSize = 28.sp, fontFamily = FuenteTitulo) }
                Spacer(Modifier.weight(1f))
                Text(
                    "${tiene.size} / ${Artefacto.entries.size}", color = modo.brillo, fontFamily = FuenteTitulo,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    modifier = Modifier.semantics { contentDescription = "Tienes ${tiene.size} de ${Artefacto.entries.size} artefactos" }
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                modo.titulo.uppercase(), color = modo.brillo, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold,
                fontSize = 30.sp, letterSpacing = 3.sp, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() }
            )
            Text(
                modo.subtitulo, color = ColoresInicio.Lema, fontFamily = FuenteTexto, fontSize = 15.sp,
                textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 360.dp).padding(top = 6.dp)
            )
            Spacer(Modifier.height(18.dp))

            Column(Modifier.widthIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Artefacto.entries.chunked(2).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        fila.forEach { a ->
                            NichoVitrina(
                                artefacto = a,
                                tiene = a in tiene,
                                modo = modo,
                                elegido = eligiendo && a in seleccion,
                                onClick = if (eligiendo && a in tiene) ({ onAlternar(a) }) else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            if (eligiendo) {
                Text(
                    when {
                        tiene.isEmpty() -> "Aún no tienes artefactos: fórjalos en la Apuesta de Brokkr."
                        seleccion.isEmpty() -> "Bajarás al campo sin artefactos."
                        else -> "Llevas ${seleccion.size} de ${tiene.size}: " + seleccion.sortedBy { it.ordinal }.joinToString { it.nombre }
                    },
                    color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 15.sp, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                BotonOdin("Llevar a la noche", onListo, modo.acento, Modifier.widthIn(max = 480.dp).fillMaxWidth())
            } else {
                Text(
                    if (tiene.size == Artefacto.entries.size) "Tu vitrina está completa: las forjas siguientes serán solo por hidromiel."
                    else "Los que faltan se forjan al superar el Ragnarök de su mundo.",
                    color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 15.sp, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                BotonOdin("Volver a la forja", onVolver, modo.acento, Modifier.widthIn(max = 480.dp).fillMaxWidth(), primario = false)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Un nicho de la vitrina: arco de piedra, cristal, pedestal y el artefacto (o su silueta). */
@Composable
private fun NichoVitrina(
    artefacto: Artefacto,
    tiene: Boolean,
    modo: ModoVitrina,
    elegido: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val forma = CutCornerShape(10.dp)
    val borde = when {
        elegido -> modo.brillo
        tiene -> modo.acento.copy(alpha = .7f)
        else -> OdinPaleta.Hierro
    }
    val pulso by rememberInfiniteTransition(label = "nicho").animateFloat(
        .6f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse), label = "brillo"
    )
    Column(
        modifier
            .clip(forma)
            .background(Color(0xE60B0907))
            .border(if (elegido) 2.5.dp else 1.dp, borde, forma)
            .then(if (onClick != null) Modifier.clickable(role = Role.Checkbox, onClick = onClick) else Modifier)
            .padding(10.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = if (tiene) "${artefacto.nombre}, del mundo ${artefacto.nombreMundo}: ${artefacto.poder}" +
                    (if (modo == ModoVitrina.ALBA) if (elegido) ". Lo llevas a la noche" else ". No lo llevas" else "")
                else "${artefacto.nombre}: por forjar, sale del mundo ${artefacto.nombreMundo}"
                if (modo == ModoVitrina.ALBA && tiene) selected = elegido
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(.9f), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Arco de piedra del nicho
                val arco = Path().apply {
                    moveTo(w * .1f, h)
                    lineTo(w * .1f, h * .35f)
                    quadraticTo(w * .5f, -h * .12f, w * .9f, h * .35f)
                    lineTo(w * .9f, h)
                    close()
                }
                drawPath(arco, Brush.verticalGradient(listOf(Color(0xFF1C1712), Color(0xFF0E0B08))))
                drawPath(arco, OdinPaleta.Hierro.copy(alpha = .9f), style = Stroke(w * .02f))
                if (tiene) {
                    drawCircle(
                        Brush.radialGradient(listOf(modo.acento.copy(alpha = .35f * pulso), Color.Transparent), center = Offset(w / 2, h * .5f), radius = w * .45f),
                        w * .45f, Offset(w / 2, h * .5f)
                    )
                }
                // Pedestal
                drawRect(Color(0xFF3A3229), Offset(w * .28f, h * .82f), Size(w * .44f, h * .07f))
                drawRect(Color(0xFF2A241D), Offset(w * .22f, h * .89f), Size(w * .56f, h * .11f))
                // Cristal: reflejo diagonal
                drawLine(Color.White.copy(alpha = .08f), Offset(w * .22f, h * .3f), Offset(w * .42f, h * .1f), w * .03f)
                drawLine(Color.White.copy(alpha = .05f), Offset(w * .3f, h * .45f), Offset(w * .58f, h * .17f), w * .015f)
            }
            if (tiene) {
                IconoArtefacto(artefacto.idForja, tamano = 86.dp, modifier = Modifier.padding(bottom = 18.dp))
            } else {
                Box(Modifier.padding(bottom = 18.dp).alpha(.35f)) {
                    IconoArtefacto(artefacto.idForja, tamano = 86.dp, apagado = true)
                }
                Text("ᛜ", color = OdinPaleta.HuesoTenue, fontSize = 26.sp, modifier = Modifier.padding(bottom = 18.dp))
            }
            if (modo == ModoVitrina.ALBA && tiene) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(26.dp).clip(CircleShape)
                        .background(if (elegido) modo.acento else Color(0xCC06070D))
                        .border(1.5.dp, modo.brillo, CircleShape),
                    contentAlignment = Alignment.Center
                ) { if (elegido) Text("✓", color = ColoresInicio.TintaBoton, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Text(artefacto.nombre, color = if (tiene) modo.brillo else OdinPaleta.HuesoTenue, fontFamily = FuenteTitulo, fontSize = 17.sp)
        Text("MUNDO ${artefacto.mundo} · ${artefacto.nombreMundo.uppercase()}", color = OdinPaleta.Oro.copy(alpha = .8f),
            fontFamily = FuenteTitulo, fontSize = 10.sp, letterSpacing = 1.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            if (tiene) artefacto.poder else "Por forjar",
            color = if (tiene) ColoresInicio.Tenue else OdinPaleta.HuesoTenue.copy(alpha = .7f),
            fontFamily = FuenteTexto, fontSize = 13.sp, lineHeight = 16.sp, textAlign = TextAlign.Center
        )
    }
}

/** Chispas que suben despacio, del color del modo (brasas de la forja o luz del alba). */
@Composable
internal fun BrasasFlotantes(color: Color, modifier: Modifier = Modifier) {
    val t by rememberInfiniteTransition(label = "brasas").animateFloat(
        0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "t"
    )
    Canvas(modifier) {
        for (i in 0 until 26) {
            val semilla = i * 0.6180339f
            val x = ((semilla * 977f) % 1f) * size.width + sin((t * 6.283f) + i) * 12f
            val fase = (t + (semilla * 3.7f) % 1f) % 1f
            val y = size.height * (1.05f - fase * 1.1f)
            val a = (sin(fase * 3.1416f) * .7f).coerceAtLeast(0f)
            drawCircle(color.copy(alpha = a), 1.5f + (i % 3), Offset(x, y))
        }
    }
}
