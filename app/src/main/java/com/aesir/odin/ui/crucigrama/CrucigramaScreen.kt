package com.aesir.odin.ui.crucigrama

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.model.Celda
import com.aesir.odin.domain.model.Crucigrama
import com.aesir.odin.domain.model.Direccion
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.PalabraCrucigrama
import com.aesir.odin.domain.model.ResultadoCrucigrama
import com.aesir.odin.domain.model.ResultadoPalabra
import com.aesir.odin.domain.usecase.crucigrama.ActualizarPuntajeUseCase
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.ColorHidromiel
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.IconoCuerno
import com.aesir.odin.ui.components.odin.PanelTallado
import com.aesir.odin.ui.components.odin.dibujarEmblema
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import kotlinx.coroutines.delay

/**
 * CU-07 Resolver crucigrama de conceptos. La cuadrícula va en un panel
 * tallado con el material del mundo; abajo, la pista de la palabra
 * seleccionada, las ayudas que cuestan hidromiel y un teclado propio con Ñ.
 */
@Composable
fun CrucigramaScreen(
    viewModel: CrucigramaViewModel,
    temaId: String,
    temasIncluidos: List<String>,
    onFinalizado: (ResultadoCrucigrama, totalPalabras: Int, ordenMundo: Int) -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val crucigrama by viewModel.crucigrama.collectAsState()
    val seleccionadaId by viewModel.palabraSeleccionada.collectAsState()
    val puntaje by viewModel.puntaje.collectAsState()
    val ultimoResultado by viewModel.ultimoResultado.collectAsState()
    val resultadoFinal by viewModel.resultadoFinal.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val entradas by viewModel.entradas.collectAsState()
    val celdaActual by viewModel.celdaActual.collectAsState()
    val bloqueadas by viewModel.bloqueadas.collectAsState()
    val contexto by viewModel.contexto.collectAsState()
    val avisoAyuda by viewModel.avisoAyuda.collectAsState()
    val saldo by viewModel.saldo.collectAsState()

    var confirmarSalida by remember { mutableStateOf(false) }
    var segundos by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(temaId) {
        if (viewModel.crucigrama.value == null) viewModel.cargarCrucigrama(temaId, temasIncluidos)
    }
    LaunchedEffect(crucigrama != null, resultadoFinal == null) {
        while (crucigrama != null && resultadoFinal == null) {
            delay(1_000)
            segundos++
        }
    }
    LaunchedEffect(resultadoFinal) {
        val resultado = resultadoFinal ?: return@LaunchedEffect
        onFinalizado(resultado, crucigrama?.palabras?.size ?: 0, contexto?.mundo?.orden ?: 1)
    }

    // Salir con un crucigrama en curso es abandonar el nivel (MOD-01)
    val enCurso = crucigrama != null && resultadoFinal == null
    BackHandler(enabled = enCurso) { confirmarSalida = true }
    val salir: () -> Unit = {
        if (enCurso) {
            confirmarSalida = true
        } else {
            onSalir()
        }
    }

    val estilo = EstilosMundo.porOrden(contexto?.mundo?.orden ?: 1)

    FondoMundo(estilo = estilo, modifier = modifier, alturaPaisaje = 260.dp) {
        val actual = crucigrama
        when {
            mensajeError != null -> ErrorGeneracion(
                estilo = estilo,
                mensaje = mensajeError ?: "",
                onReintentar = viewModel::reintentarGeneracion,
                onVolver = onSalir,
                modifier = Modifier.align(Alignment.Center)
            )
            actual == null -> CircularProgressIndicator(color = estilo.acento, modifier = Modifier.align(Alignment.Center))
            else -> ContenidoCrucigrama(
                estilo = estilo,
                crucigrama = actual,
                nombreMundo = contexto?.mundo?.nombre ?: "",
                nivel = contexto?.tema?.orden,
                seleccionadaId = seleccionadaId,
                entradas = entradas,
                celdaActual = celdaActual,
                bloqueadas = bloqueadas,
                ultimoResultado = ultimoResultado,
                puntaje = puntaje,
                saldo = saldo,
                segundos = segundos,
                onVolver = salir,
                onCelda = viewModel::seleccionarCelda,
                onLetra = viewModel::escribir,
                onBorrar = viewModel::borrar,
                onRevelarLetra = viewModel::revelarLetra,
                onRevelarPalabra = viewModel::revelarPalabra
            )
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Hueso,
            textContentColor = OdinPaleta.Hueso,
            title = { Text("¿Abandonar el crucigrama?", fontFamily = FuenteTitulo) },
            text = {
                Text(
                    "Si sales ahora, el nivel cuenta como abandonado: si pagaste entrada de hidromiel, se pierde.",
                    fontFamily = FuenteTexto
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text("Seguir jugando", color = estilo.acento, fontFamily = FuenteTitulo)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    onSalir()
                }) { Text("Abandonar", color = OdinPaleta.Incorrecto, fontFamily = FuenteTitulo) }
            }
        )
    }

    avisoAyuda?.let { motivo ->
        AlertDialog(
            onDismissRequest = viewModel::limpiarAvisoAyuda,
            containerColor = OdinPaleta.HollinAlto,
            titleContentColor = OdinPaleta.Hueso,
            textContentColor = OdinPaleta.Hueso,
            icon = { IconoCuerno(tamano = 32.dp) },
            title = { Text("Hidromiel insuficiente", fontFamily = FuenteTitulo) },
            text = { Text(motivo, fontFamily = FuenteTexto) },
            confirmButton = {
                TextButton(onClick = viewModel::limpiarAvisoAyuda) {
                    Text("Entendido", color = estilo.acento, fontFamily = FuenteTitulo)
                }
            }
        )
    }
}

@Composable
private fun ContenidoCrucigrama(
    estilo: EstiloMundo,
    crucigrama: Crucigrama,
    nombreMundo: String,
    nivel: Int?,
    seleccionadaId: String?,
    entradas: Map<Celda, Char>,
    celdaActual: Celda?,
    bloqueadas: Set<Celda>,
    ultimoResultado: ResultadoPalabra?,
    puntaje: Int,
    saldo: Int,
    segundos: Int,
    onVolver: () -> Unit,
    onCelda: (Celda) -> Unit,
    onLetra: (Char) -> Unit,
    onBorrar: () -> Unit,
    onRevelarLetra: () -> Unit,
    onRevelarPalabra: () -> Unit
) {
    val seleccionada = crucigrama.palabras.firstOrNull { it.id == seleccionadaId }
    val numeros = crucigrama.numeros

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezado
        Box(Modifier.fillMaxWidth()) {
            BotonVolver(nombreMundo.ifEmpty { "Roadmap" }, onClick = onVolver, modifier = Modifier.align(Alignment.CenterStart))
            Canvas(Modifier.size(40.dp).align(Alignment.Center)) {
                val r = size.minDimension / 2f
                drawCircle(Color.Black.copy(alpha = .55f), r)
                drawCircle(estilo.metal, r, style = Stroke(1.5.dp.toPx()))
                dibujarEmblema(estilo.emblema, center, r * .72f, estilo.acento)
            }
            ContadorHidromiel(saldo = saldo, metal = estilo.metal, modifier = Modifier.align(Alignment.CenterEnd))
        }
        Spacer(Modifier.height(6.dp))
        Text("CRUCIGRAMA", style = estiloTitulo(30.sp), textAlign = TextAlign.Center)
        Text(
            listOfNotNull(nivel?.let { "Nivel $it" }, nombreMundo.ifEmpty { null }).joinToString(" · "),
            color = Color(0xFFD8CFBE),
            fontFamily = FuenteTexto,
            fontSize = 15.sp
        )
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("%02d:%02d".format(segundos / 60, segundos % 60), color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 13.sp)
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(Color.Black.copy(alpha = .55f))
                    .border(1.dp, estilo.metal.copy(alpha = .7f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(crucigrama.resueltas.toFloat() / crucigrama.palabras.size)
                        .height(6.dp)
                        .background(estilo.acento)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "${crucigrama.resueltas} de ${crucigrama.palabras.size} palabras · $puntaje pts",
                color = Color(0xFFD8CFBE),
                fontFamily = FuenteTexto,
                fontSize = 13.sp
            )
        }

        // Cuadrícula
        PanelTallado(
            estilo = estilo,
            semilla = 13,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Cuadricula(
                estilo = estilo,
                crucigrama = crucigrama,
                numeros = numeros,
                seleccionada = seleccionada,
                entradas = entradas,
                celdaActual = celdaActual,
                bloqueadas = bloqueadas,
                falloEn = ultimoResultado?.takeIf { it.estado != EstadoRespuesta.CORRECTA }?.palabraId,
                onCelda = onCelda,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(8.dp))
        PanelPista(estilo, seleccionada, seleccionada?.let { numeros[it.id] }, ultimoResultado)
        Spacer(Modifier.height(8.dp))

        // Ayudas (MOD-01, E5)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonAyuda(
                texto = "Revelar letra",
                runa = estilo.runas[4],
                costo = PoliticaCosto.AYUDA_LETRA,
                alcanza = saldo >= PoliticaCosto.AYUDA_LETRA,
                estilo = estilo,
                habilitado = seleccionada != null && !seleccionada.resuelta,
                onClick = onRevelarLetra
            )
            BotonAyuda(
                texto = "Revelar palabra",
                runa = estilo.runas[3],
                costo = PoliticaCosto.AYUDA_PALABRA,
                alcanza = saldo >= PoliticaCosto.AYUDA_PALABRA,
                estilo = estilo,
                habilitado = seleccionada != null && !seleccionada.resuelta,
                onClick = onRevelarPalabra
            )
        }
        Spacer(Modifier.height(10.dp))
        Teclado(estilo = estilo, onLetra = onLetra, onBorrar = onBorrar)
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun Cuadricula(
    estilo: EstiloMundo,
    crucigrama: Crucigrama,
    numeros: Map<String, Int>,
    seleccionada: PalabraCrucigrama?,
    entradas: Map<Celda, Char>,
    celdaActual: Celda?,
    bloqueadas: Set<Celda>,
    falloEn: String?,
    onCelda: (Celda) -> Unit,
    modifier: Modifier = Modifier
) {
    val solucion = crucigrama.solucion
    val inicios = crucigrama.palabras.associate { Celda(it.fila, it.columna) to numeros[it.id] }
    val celdasSeleccion = seleccionada?.celdas.orEmpty().toSet()
    val celdasFallo = crucigrama.palabras.firstOrNull { it.id == falloEn }?.celdas.orEmpty().toSet()

    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val lado = minOf(maxWidth / crucigrama.columnas, maxHeight / crucigrama.filas, 36.dp)
        Column {
            for (f in 0 until crucigrama.filas) {
                Row {
                    for (c in 0 until crucigrama.columnas) {
                        val celda = Celda(f, c)
                        if (celda !in solucion) {
                            Spacer(Modifier.size(lado))
                            continue
                        }
                        val esActual = celda == celdaActual && seleccionada?.resuelta == false
                        val enSeleccion = celda in celdasSeleccion
                        val fija = celda in bloqueadas
                        val fondo = when {
                            esActual -> estilo.acento
                            enSeleccion -> estilo.acento.copy(alpha = .16f)
                            fija -> estilo.acento.copy(alpha = .08f)
                            else -> Color(0xD90B0907)
                        }
                        val borde = when {
                            celda in celdasFallo -> OdinPaleta.Incorrecto
                            esActual || enSeleccion -> estilo.acento
                            else -> estilo.metal.copy(alpha = .45f)
                        }
                        val letra = entradas[celda]
                        Box(
                            Modifier
                                .size(lado)
                                .padding(1.5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(fondo)
                                .border(1.dp, borde, RoundedCornerShape(3.dp))
                                .semantics {
                                    contentDescription = "Fila ${f + 1}, columna ${c + 1}" + (letra?.let { ", $it" } ?: ", vacía")
                                }
                                .clickable(role = Role.Button) { onCelda(celda) },
                            contentAlignment = Alignment.Center
                        ) {
                            inicios[celda]?.let { n ->
                                Text(
                                    "$n",
                                    color = if (esActual) OdinPaleta.Hollin else estilo.metal,
                                    fontSize = (lado.value * .24f).coerceAtLeast(7f).sp,
                                    modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp)
                                )
                            }
                            if (letra != null) {
                                Text(
                                    "$letra",
                                    color = when {
                                        esActual -> OdinPaleta.Hollin
                                        fija -> estilo.acento
                                        else -> OdinPaleta.Hueso
                                    },
                                    fontFamily = FuenteTitulo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (lado.value * .5f).sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelPista(
    estilo: EstiloMundo,
    palabra: PalabraCrucigrama?,
    numero: Int?,
    ultimoResultado: ResultadoPalabra?
) {
    val forma = CutCornerShape(8.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Color(0xE60B0907))
            .border(1.dp, estilo.metal.copy(alpha = .8f), forma)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (palabra == null) {
            Text("Toca una casilla para ver su pista.", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 15.sp)
            return@Column
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${numero ?: ""} ${if (palabra.direccion == Direccion.HORIZONTAL) "HORIZONTAL" else "VERTICAL"}",
                color = estilo.acento,
                fontFamily = FuenteTitulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp
            )
            Text(
                " · ${palabra.longitud} letras",
                color = Color(0xFFD8CFBE),
                fontFamily = FuenteTexto,
                fontSize = 13.sp
            )
        }
        Text(
            palabra.pista,
            color = OdinPaleta.Hueso,
            fontFamily = FuenteTexto,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        val retro = ultimoResultado?.takeIf { it.palabraId == palabra.id || it.estado == EstadoRespuesta.CORRECTA }
        if (retro != null) {
            val correcta = retro.estado == EstadoRespuesta.CORRECTA
            Text(
                when (retro.estado) {
                    EstadoRespuesta.CORRECTA -> "¡Correcto! +${ActualizarPuntajeUseCase.PUNTOS_POR_ACIERTO} puntos."
                    EstadoRespuesta.INCOMPLETA -> "Respuesta incompleta. −${ActualizarPuntajeUseCase.PENALIZACION} puntos. Inténtalo de nuevo."
                    EstadoRespuesta.INCORRECTA -> "Respuesta incorrecta. −${ActualizarPuntajeUseCase.PENALIZACION} puntos. Inténtalo de nuevo."
                },
                color = if (correcta) OdinPaleta.Correcto else OdinPaleta.Incorrecto,
                fontFamily = FuenteTexto,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun RowScope.BotonAyuda(
    texto: String,
    runa: String,
    costo: Int,
    alcanza: Boolean,
    estilo: EstiloMundo,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    val forma = CutCornerShape(12.dp)
    Row(
        Modifier
            .weight(1f)
            .height(44.dp)
            .alpha(if (habilitado) 1f else .4f)
            .clip(forma)
            .background(Color(0xE60B0907))
            .border(1.dp, estilo.metal, forma)
            .clickable(enabled = habilitado, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(runa, color = estilo.acento, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Text(texto, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 15.sp, maxLines = 1, modifier = Modifier.weight(1f))
        IconoCuerno(tamano = 16.dp)
        Text(
            "$costo",
            color = if (alcanza) ColorHidromiel else OdinPaleta.Incorrecto,
            fontFamily = FuenteTitulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

private val FILAS_TECLADO = listOf("QWERTYUIOP", "ASDFGHJKLÑ", "ZXCVBNM")

@Composable
private fun Teclado(estilo: EstiloMundo, onLetra: (Char) -> Unit, onBorrar: () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FILAS_TECLADO.forEachIndexed { i, fila ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                if (i == 2) Tecla("⌫", estilo, peso = 1.6f, descripcion = "Borrar", onClick = onBorrar)
                fila.forEach { letra -> Tecla("$letra", estilo, onClick = { onLetra(letra) }) }
                if (i == 2) Spacer(Modifier.weight(1.6f))
            }
        }
    }
}

@Composable
private fun RowScope.Tecla(
    texto: String,
    estilo: EstiloMundo,
    peso: Float = 1f,
    descripcion: String? = null,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(5.dp)
    Box(
        Modifier
            .weight(peso)
            .height(44.dp)
            .clip(forma)
            .background(Color(0xE60B0907))
            .border(1.dp, estilo.metal.copy(alpha = .55f), forma)
            .then(if (descripcion != null) Modifier.semantics { contentDescription = descripcion } else Modifier)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
    }
}

/** Flujo alternativo A2.1: no se pudo generar el crucigrama. */
@Composable
private fun ErrorGeneracion(
    estilo: EstiloMundo,
    mensaje: String,
    onReintentar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .padding(24.dp)
            .widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("LA RUNA NO RESPONDE", style = estiloTitulo(24.sp), textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(mensaje, color = Color(0xFFD8CFBE), fontFamily = FuenteTexto, fontSize = 16.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        BotonOdin(texto = "Reintentar", onClick = onReintentar, acento = estilo.acento, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        BotonOdin(texto = "Volver al roadmap", onClick = onVolver, acento = estilo.acento, primario = false, metal = estilo.metal, modifier = Modifier.fillMaxWidth())
    }
}
