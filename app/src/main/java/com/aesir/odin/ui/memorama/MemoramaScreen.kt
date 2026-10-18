package com.aesir.odin.ui.memorama

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.model.EstadoTarjeta
import com.aesir.odin.domain.model.ResultadoMemorama
import com.aesir.odin.domain.model.TarjetaMemorama
import com.aesir.odin.domain.model.TipoTarjeta
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.components.odin.mancha
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlinx.coroutines.delay

/**
 * CU-04: tablero del memorama (MemoramaScreen del diagrama de clases).
 * 12 tarjetas en 3 columnas; se voltean al tocarlas. Salir con el memorama
 * en curso pide confirmación (A9.1 / A9.2).
 */
@Composable
fun MemoramaScreen(
    viewModel: MemoramaViewModel,
    mundoId: String,
    nivelId: String,
    onFinalizado: (ResultadoMemorama) -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tarjetas by viewModel.tarjetas.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val pares by viewModel.paresEncontrados.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()

    var confirmarAbandono by remember { mutableStateOf(false) }
    var segundos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(mundoId, nivelId) { viewModel.iniciar(mundoId, nivelId) }

    // Cronómetro visible: se calcula desde el inicio registrado (RF-07)
    val enCurso = tarjetas.isNotEmpty() && resultado == null
    LaunchedEffect(enCurso) {
        while (enCurso) {
            if (viewModel.tiempoInicio > 0) segundos = (System.currentTimeMillis() - viewModel.tiempoInicio) / 1_000
            delay(500)
        }
    }
    LaunchedEffect(resultado) { resultado?.let(onFinalizado) }

    BackHandler(enabled = enCurso) { confirmarAbandono = true }

    val estilo = EstilosMundo.porOrden(contexto?.mundo?.orden ?: 1)

    FondoMundo(estilo = estilo, modifier = modifier, alturaPaisaje = 240.dp) {
        when {
            mensajeError != null -> MensajeError(estilo, mensajeError.orEmpty(), onSalir, Modifier.align(Alignment.Center))
            tarjetas.isEmpty() -> CircularProgressIndicator(color = estilo.acento, modifier = Modifier.align(Alignment.Center))
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Encabezado(
                    estilo = estilo,
                    nombreMundo = contexto?.mundo?.nombre.orEmpty(),
                    nombreNivel = contexto?.tema?.nombre.orEmpty(),
                    segundos = segundos,
                    pares = pares,
                    totalPares = tarjetas.size / 2,
                    onAbandonar = { confirmarAbandono = true }
                )
                Tablero(
                    estilo = estilo,
                    tarjetas = tarjetas,
                    onSeleccionar = viewModel::seleccionarTarjeta,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp)
                )
            }
        }
    }

    if (confirmarAbandono) {
        AlertDialog(
            onDismissRequest = { confirmarAbandono = false },
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Hueso,
            textContentColor = OdinPaleta.Hueso,
            title = { Text("¿Abandonar el memorama?", fontFamily = FuenteTitulo) },
            text = {
                Text(
                    "Se descartará el intento actual y no obtendrás puntos. " +
                        "Si pagaste entrada de hidromiel, se pierde.",
                    fontFamily = FuenteTexto
                )
            },
            // A9.2: continuar desde donde estaba
            confirmButton = {
                TextButton(onClick = { confirmarAbandono = false }) {
                    Text("Continuar", color = estilo.acento, fontFamily = FuenteTitulo)
                }
            },
            // A9.1: confirmar el abandono
            dismissButton = {
                TextButton(onClick = {
                    confirmarAbandono = false
                    viewModel.abandonar(onSalir)
                }) { Text("Abandonar", color = OdinPaleta.Incorrecto, fontFamily = FuenteTitulo) }
            }
        )
    }
}

@Composable
private fun Encabezado(
    estilo: EstiloMundo,
    nombreMundo: String,
    nombreNivel: String,
    segundos: Long,
    pares: Int,
    totalPares: Int,
    onAbandonar: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth()) {
            BotonVolver("Abandonar", onClick = onAbandonar, modifier = Modifier.align(Alignment.CenterStart))
            Canvas(
                Modifier
                    .size(42.dp)
                    .align(Alignment.CenterEnd)
            ) {
                val r = size.minDimension / 2f
                mancha(center, r * 1.5f, estilo.acento, .25f)
                drawCircle(Color(0xFF0D0A07), r)
                drawCircle(estilo.metal, r - 1.dp.toPx(), style = Stroke(2.dp.toPx()))
                dibujarEmblema(estilo.emblema, center, r * .6f, estilo.acento)
            }
        }
        Text("MEMORAMA", style = estiloTitulo(32.sp, OdinPaleta.Hueso, brillo = estilo.acento), modifier = Modifier.padding(top = 2.dp))
        Text(
            listOf(nombreNivel, nombreMundo).filter { it.isNotEmpty() }.joinToString(" · "),
            color = OdinPaleta.HuesoTenue,
            fontFamily = FuenteTexto,
            fontSize = 15.sp,
            maxLines = 1
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 6.dp, end = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "%02d:%02d".format(segundos / 60, segundos % 60),
                color = OdinPaleta.Hueso,
                fontFamily = FuenteTitulo,
                fontSize = 18.sp,
                modifier = Modifier.semantics { contentDescription = "Tiempo: $segundos segundos" }
            )
            Text(
                "$pares de $totalPares pares",
                color = estilo.acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

/** Rejilla de 3 columnas que reparte el alto disponible entre las filas. */
@Composable
private fun Tablero(
    estilo: EstiloMundo,
    tarjetas: List<TarjetaMemorama>,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val columnas = 3
    val filas = tarjetas.indices.chunked(columnas)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        filas.forEach { fila ->
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fila.forEach { indice ->
                    Tarjeta(
                        estilo = estilo,
                        tarjeta = tarjetas[indice],
                        runa = estilo.runas[5],
                        onClick = { onSeleccionar(indice) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )
                }
                repeat(columnas - fila.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Tarjeta(
    estilo: EstiloMundo,
    tarjeta: TarjetaMemorama,
    runa: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val descubierta = tarjeta.estado != EstadoTarjeta.OCULTA
    val giro by animateFloatAsState(if (descubierta) 180f else 0f, tween(320), label = "giroTarjeta")
    val forma = RoundedCornerShape(8.dp)
    val encontrada = tarjeta.estado == EstadoTarjeta.ENCONTRADA

    Box(
        modifier
            .graphicsLayer {
                rotationY = giro
                cameraDistance = 14f * density
            }
            .clip(forma)
            .clickable(enabled = tarjeta.estado == EstadoTarjeta.OCULTA, role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = if (descubierta) "${etiqueta(tarjeta.tipo)}: ${tarjeta.contenido}" else "Tarjeta oculta"
            }
    ) {
        if (giro <= 90f) {
            // Reverso: runa del minijuego sobre la cara del mundo
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1B1712))
                    .border(1.5.dp, estilo.metal, forma),
                contentAlignment = Alignment.Center
            ) {
                Text(runa, color = estilo.acento, fontSize = 30.sp, fontFamily = FuenteTitulo)
            }
        } else {
            // Anverso (se gira 180° para que el texto no salga en espejo)
            Column(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(if (encontrada) Color(0xFFDCE8D3) else Color(0xFFEDE2C8))
                    .border(if (encontrada) 2.5.dp else 1.5.dp, if (encontrada) Color(0xFF3F8A4E) else estilo.metal, forma)
                    .alpha(if (encontrada) .85f else 1f)
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    etiqueta(tarjeta.tipo).uppercase(),
                    color = if (tarjeta.tipo == TipoTarjeta.CONCEPTO) OdinPaleta.TintaRoja else OdinPaleta.Tinta,
                    fontFamily = FuenteTitulo,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val esConcepto = tarjeta.tipo == TipoTarjeta.CONCEPTO
                    BasicText(
                        tarjeta.contenido,
                        style = TextStyle(
                            color = OdinPaleta.TintaOscura,
                            fontFamily = if (esConcepto) FuenteTitulo else FuenteTexto,
                            fontWeight = if (esConcepto) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        ),
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 8.sp,
                            maxFontSize = if (esConcepto) 17.sp else 13.sp,
                            stepSize = .5.sp
                        )
                    )
                }
            }
        }
    }
}

private fun etiqueta(tipo: TipoTarjeta) = when (tipo) {
    TipoTarjeta.CONCEPTO -> "Concepto"
    TipoTarjeta.DEFINICION -> "Definición"
}

@Composable
private fun MensajeError(estilo: EstiloMundo, mensaje: String, onVolver: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(mensaje, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 16.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        BotonOdin(texto = "Volver al roadmap", onClick = onVolver, acento = estilo.acento)
    }
}
