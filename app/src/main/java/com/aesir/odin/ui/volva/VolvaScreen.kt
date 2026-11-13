package com.aesir.odin.ui.volva

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.model.AccesoVolva
import com.aesir.odin.domain.model.ConsultaVolva
import com.aesir.odin.domain.model.Fe
import com.aesir.odin.domain.model.Lectura
import com.aesir.odin.domain.model.ResultadoConsulta
import com.aesir.odin.domain.model.RunaFuthark
import com.aesir.odin.domain.model.Vision
import com.aesir.odin.domain.model.VisionRespondida
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.DivisorOrnamental
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.PanelTallado
import com.aesir.odin.ui.components.odin.PuntosProgreso
import com.aesir.odin.ui.components.odin.estiloRuna
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstiloMundo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta

/** Paso de la apuesta en el selector (botones − y +). */
private const val PASO_APUESTA = 5
/** Últimos segundos en los que el contador se pone en rojo. */
private const val SEGUNDOS_URGENCIA = 5

private val ColorProfecia = Color(0xFFF0C75A)
private val ColorClara = Color(0xFF7FB8E8)
private val ColorHumo = Color(0xFF9A9A9A)
private val ColorMaldicion = Color(0xFFE2614B)

/**
 * CU-12 Retar a la bruja. Una sola ruta que muestra, según el estado, las tres
 * pantallas del diagrama de clases: [CalderoVolvaScreen], [VisionVolvaScreen]
 * y [LecturaCalderoScreen]. Usa el estilo de aurora (Hierro) por ser un modo
 * del Valhalla que mezcla temas de varios mundos.
 */
@Composable
fun VolvaScreen(viewModel: ConsultaVolvaViewModel, onSalir: () -> Unit, modifier: Modifier = Modifier) {
    val estado by viewModel.estado.collectAsState()
    val acceso by viewModel.acceso.collectAsState()
    val apuesta by viewModel.apuesta.collectAsState()
    val consulta by viewModel.consulta.collectAsState()
    val fe by viewModel.fe.collectAsState()
    val segundos by viewModel.segundosRestantes.collectAsState()
    val ultima by viewModel.ultimaRespuesta.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val aviso by viewModel.aviso.collectAsState()
    val confirmando by viewModel.confirmandoAbandono.collectAsState()
    val saldo by viewModel.saldo.collectAsState()
    val estilo = EstilosMundo.Hierro

    // E6.1: el botón de regreso del sistema durante la consulta pide confirmación.
    BackHandler(enabled = viewModel.enConsulta()) { viewModel.solicitarAbandono() }

    FondoMundo(estilo = estilo, modifier = modifier, alturaPaisaje = 280.dp, paisajeInferior = true) {
        when (estado) {
            EstadoConsulta.CARGANDO ->
                CircularProgressIndicator(color = estilo.acento, modifier = Modifier.align(Alignment.Center))

            EstadoConsulta.APOSTANDO, EstadoConsulta.CALDERO_APAGADO, EstadoConsulta.VALHALLA_CERRADO,
            EstadoConsulta.HIDROMIEL_INSUFICIENTE, EstadoConsulta.VISIONES_INSUFICIENTES -> acceso?.let {
                CalderoVolvaScreen(
                    estilo = estilo, estado = estado, acceso = it, saldo = saldo, apuesta = apuesta,
                    apuestaValida = viewModel.apuestaValida(), aviso = aviso,
                    onCambiarApuesta = viewModel::cambiarApuesta,
                    onAcercarse = viewModel::acercarseAlCaldero,
                    onSalir = onSalir
                )
            }

            EstadoConsulta.DECLARANDO_FE, EstadoConsulta.ELIGIENDO_OPCION, EstadoConsulta.VISION_RESUELTA -> consulta?.let { c ->
                val vision = if (estado == EstadoConsulta.VISION_RESUELTA) c.visiones.getOrNull(c.indiceActual - 1) else c.visionActual()
                if (vision != null) {
                    VisionVolvaScreen(
                        estilo = estilo, estado = estado, consulta = c, vision = vision, fe = fe,
                        segundos = segundos, ultima = ultima,
                        onDeclararFe = viewModel::declararFe,
                        onElegir = viewModel::elegirOpcion,
                        onSiguiente = viewModel::siguienteVision,
                        onSalir = viewModel::solicitarAbandono
                    )
                }
            }

            EstadoConsulta.LECTURA -> resultado?.let { LecturaCalderoScreen(estilo, it, onSalir) }

            EstadoConsulta.ERROR -> Mensaje(estilo, "Algo salió mal", aviso ?: "Inténtalo de nuevo.", onSalir)
        }

        if (confirmando) {
            AlertDialog(
                onDismissRequest = viewModel::cancelarAbandono,
                title = { Text("¿Retirarte del caldero?", fontFamily = FuenteTitulo) },
                text = { Text("Si te retiras, perderás tu apuesta y la consulta contará como la de hoy.", fontFamily = FuenteTexto) },
                confirmButton = { TextButton(onClick = { viewModel.confirmarAbandono(onSalir) }) { Text("Retirarme") } },
                dismissButton = { TextButton(onClick = viewModel::cancelarAbandono) { Text("Seguir") } }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Pantalla 1 · El caldero del destino
// ---------------------------------------------------------------------------

@Composable
internal fun CalderoVolvaScreen(
    estilo: EstiloMundo,
    estado: EstadoConsulta,
    acceso: AccesoVolva,
    saldo: Int,
    apuesta: Int,
    apuestaValida: Boolean,
    aviso: String?,
    onCambiarApuesta: (Int) -> Unit,
    onAcercarse: () -> Unit,
    onSalir: () -> Unit
) {
    val encendido = estado == EstadoConsulta.APOSTANDO
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BotonVolver("Valhalla", onClick = onSalir)
            Spacer(Modifier.weight(1f))
            ContadorHidromiel(saldo = saldo)
        }
        Text("EL CALDERO DEL DESTINO", style = estiloTitulo(24.sp, brillo = estilo.acento), textAlign = TextAlign.Center)
        Text("Reta a la Völva", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Caldero(encendido = encendido, acento = estilo.acento, modifier = Modifier.size(150.dp))

        val motivo = when (estado) {
            EstadoConsulta.CALDERO_APAGADO -> "La Völva descansa. Ya consultaste el caldero hoy: vuelve mañana."
            EstadoConsulta.VALHALLA_CERRADO -> "Supera tu primer Ragnarök para que la Völva te reciba."
            EstadoConsulta.HIDROMIEL_INSUFICIENTE -> "Te faltan ${acceso.faltante} de hidromiel para la apuesta mínima sin bajar del piso de apuesta (${acceso.rango.piso})."
            EstadoConsulta.VISIONES_INSUFICIENTES -> "Supera más temas del roadmap: la Völva necesita dos visiones de cada Norna."
            else -> null
        }
        if (motivo != null) {
            Text(motivo, color = OdinPaleta.Brasa, fontFamily = FuenteTexto, fontSize = 15.sp, textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp))
        }

        PanelTallado(estilo = estilo, modifier = Modifier.fillMaxWidth(), semilla = 12) {
            Column {
                Text("Reglas del caldero", style = estiloTitulo(17.sp, color = estilo.acento))
                Spacer(Modifier.height(6.dp))
                listOf(
                    "Apuesta de ${PoliticaCosto.APUESTA_MIN_VOLVA} a ${PoliticaCosto.APUESTA_MAX_VOLVA} de hidromiel, sin bajar del piso de apuesta.",
                    "Seis visiones, dos por Norna: Urd (encuentra el error), Verdandi (identifica el caso) y Skuld (predice la consecuencia).",
                    "Antes de ver las opciones declara tu fe: Cauta +1 / −1, Firme +2 / −2, Total +4 / −4.",
                    "Tienes ${ConsultaVolva.SEGUNDOS_POR_VISION} segundos para elegir.",
                    "Profecía cumplida (14 o más): ×2.5 y una runa · Lectura clara (8 a 13): ×1.5 · Humo del caldero (1 a 7): recuperas la apuesta · Maldición (0 o menos): la pierdes. El diezmo de Odín es del 10 %.",
                    "Una consulta por día."
                ).forEach { TextoRegla(it) }
            }
        }

        Spacer(Modifier.height(12.dp))
        SalonDeRunas(acceso.salon.obtenidas, estilo)

        if (encendido) {
            Spacer(Modifier.height(16.dp))
            SelectorApuesta(apuesta, acceso, apuestaValida, estilo, onCambiarApuesta)
            if (aviso != null) {
                Text(aviso, color = OdinPaleta.Incorrecto, fontFamily = FuenteTexto, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(12.dp))
            BotonOdin("Acercarse al caldero", onClick = onAcercarse, acento = estilo.acento, habilitado = apuestaValida,
                modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TextoRegla(texto: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("ᛟ ", color = OdinPaleta.Oro, fontSize = 13.sp)
        Text(texto, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 14.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun SelectorApuesta(apuesta: Int, acceso: AccesoVolva, valida: Boolean, estilo: EstiloMundo, onCambiar: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("TU APUESTA", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTitulo, fontSize = 12.sp, letterSpacing = 1.5.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonCircular("−", estilo) { onCambiar(apuesta - PASO_APUESTA) }
            Text(
                "$apuesta",
                style = estiloTitulo(40.sp, color = if (valida) Color(0xFFFFF6E0) else OdinPaleta.Incorrecto),
                modifier = Modifier.width(110.dp).semantics { contentDescription = "Apuesta de $apuesta de hidromiel" },
                textAlign = TextAlign.Center
            )
            BotonCircular("+", estilo) { onCambiar(apuesta + PASO_APUESTA) }
        }
        Text(
            "Rango permitido: ${acceso.rango.minimo} a ${acceso.rango.maximo}",
            color = if (valida) OdinPaleta.HuesoTenue else OdinPaleta.Incorrecto,
            fontFamily = FuenteTexto, fontSize = 14.sp
        )
    }
}

@Composable
private fun BotonCircular(texto: String, estilo: EstiloMundo, onClick: () -> Unit) {
    Box(
        Modifier.size(46.dp).clip(CircleShape).background(Color(0xCC0B0907)).border(1.5.dp, estilo.metal, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(texto, style = estiloTitulo(24.sp, color = estilo.acento)) }
}

/** Salón de runas: las 24 del Futhark en 6 × 4; las obtenidas se iluminan. */
@Composable
private fun SalonDeRunas(obtenidas: List<RunaFuthark>, estilo: EstiloMundo) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("SALÓN DE RUNAS · ${obtenidas.size} de ${RunaFuthark.TODAS.size}", color = OdinPaleta.HuesoTenue,
            fontFamily = FuenteTitulo, fontSize = 12.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(6.dp))
        RunaFuthark.TODAS.chunked(6).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                fila.forEach { runa ->
                    val tiene = obtenidas.any { it.orden == runa.orden }
                    Text(
                        runa.simbolo,
                        style = estiloRuna(24.sp, if (tiene) estilo.acento else Color(0xFF55606E), tiene),
                        modifier = Modifier.width(30.dp).semantics { contentDescription = "${runa.nombre}${if (tiene) ", obtenida" else ""}" }
                    )
                }
            }
        }
    }
}

/** Caldero dibujado: encendido con vapor del color del mundo, o apagado en gris. */
@Composable
private fun Caldero(encendido: Boolean, acento: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.semantics { contentDescription = if (encendido) "Caldero encendido" else "Caldero apagado" }) {
        val w = size.width
        val h = size.height
        if (encendido) {
            drawCircle(Brush.radialGradient(listOf(acento.copy(alpha = .45f), Color.Transparent), center = Offset(w / 2, h * .45f), radius = w * .5f), w * .5f, Offset(w / 2, h * .45f))
            repeat(3) { i ->
                val x = w * (.35f + i * .15f)
                val vapor = Path().apply {
                    moveTo(x, h * .42f)
                    cubicTo(x - w * .08f, h * .3f, x + w * .08f, h * .2f, x, h * .08f)
                }
                drawPath(vapor, acento.copy(alpha = .6f), style = Stroke(3.dp.toPx()))
            }
        }
        val cuerpo = if (encendido) Color(0xFF2B2F3A) else Color(0xFF3A3A3A)
        drawOval(cuerpo, Offset(w * .15f, h * .45f), Size(w * .7f, h * .45f))
        drawOval(if (encendido) acento else Color(0xFF555555), Offset(w * .12f, h * .4f), Size(w * .76f, h * .14f))
        drawOval(Color(0xFF111111), Offset(w * .17f, h * .42f), Size(w * .66f, h * .09f))
        drawLine(cuerpo, Offset(w * .28f, h * .86f), Offset(w * .22f, h * .97f), 6.dp.toPx())
        drawLine(cuerpo, Offset(w * .72f, h * .86f), Offset(w * .78f, h * .97f), 6.dp.toPx())
    }
}

// ---------------------------------------------------------------------------
// Pantalla 2 · Visión
// ---------------------------------------------------------------------------

@Composable
internal fun VisionVolvaScreen(
    estilo: EstiloMundo,
    estado: EstadoConsulta,
    consulta: ConsultaVolva,
    vision: Vision,
    fe: Fe?,
    segundos: Int,
    ultima: VisionRespondida?,
    onDeclararFe: (Fe) -> Unit,
    onElegir: (Int) -> Unit,
    onSiguiente: () -> Unit,
    onSalir: () -> Unit
) {
    val numero = if (estado == EstadoConsulta.VISION_RESUELTA) consulta.indiceActual else consulta.indiceActual + 1
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BotonVolver("Retirarse", onClick = onSalir)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("PUNTOS DE DESTINO", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTitulo, fontSize = 11.sp, letterSpacing = 1.2.sp)
                val puntos = consulta.puntosDestino()
                Text(if (puntos >= 0) "+$puntos" else "−${-puntos}", style = estiloTitulo(24.sp, color = estilo.acento))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Visión $numero de ${consulta.visiones.size}", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontSize = 15.sp)
            Spacer(Modifier.width(12.dp))
            PuntosProgreso(total = consulta.visiones.size, actual = numero - 1, acento = estilo.acento, metal = estilo.metal)
        }
        Spacer(Modifier.height(10.dp))

        PanelTallado(estilo = estilo, modifier = Modifier.fillMaxWidth(), semilla = numero) {
            Column {
                Text("${vision.norna.nombre.uppercase()} · ${vision.norna.tiempo}", color = estilo.acento,
                    fontFamily = FuenteTitulo, fontSize = 13.sp, letterSpacing = 1.5.sp)
                Text(vision.norna.tipoVision, style = estiloTitulo(20.sp))
                DivisorOrnamental(estilo.metal, estilo.acento, Modifier.padding(vertical = 6.dp))
                Text(vision.caso, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 17.sp, lineHeight = 24.sp)
            }
        }
        Spacer(Modifier.height(14.dp))

        when (estado) {
            EstadoConsulta.DECLARANDO_FE -> {
                Text("Declara tu fe antes de ver las opciones", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto,
                    fontSize = 15.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Fe.entries.forEach { opcion ->
                        TarjetaFe(opcion, estilo, Modifier.weight(1f)) { onDeclararFe(opcion) }
                    }
                }
            }

            EstadoConsulta.ELIGIENDO_OPCION -> {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Fe ${fe?.nombre.orEmpty()}", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    ContadorCircular(segundos, ConsultaVolva.SEGUNDOS_POR_VISION, estilo.acento)
                }
                Spacer(Modifier.height(8.dp))
                vision.opciones.forEachIndexed { i, texto ->
                    TarjetaOpcion(texto, estilo, colorBorde = estilo.metal) { onElegir(i) }
                }
            }

            else -> ultima?.let { r ->
                vision.opciones.forEachIndexed { i, texto ->
                    // Solo se marca la que eligió el alumno; nunca se revela la correcta (RF-170).
                    val color = when {
                        i != r.opcionElegida -> estilo.metal.copy(alpha = .4f)
                        r.cumplida -> OdinPaleta.Correcto
                        else -> OdinPaleta.Incorrecto
                    }
                    TarjetaOpcion(texto, estilo, colorBorde = color, onClick = null)
                }
                Spacer(Modifier.height(8.dp))
                val titulo = when {
                    r.cumplida -> "Visión cumplida"
                    r.opcionElegida == null -> "Se acabó el tiempo: visión errada"
                    else -> "Visión errada"
                }
                Text(
                    "$titulo · ${if (r.puntos >= 0) "+" else "−"}${kotlin.math.abs(r.puntos)} puntos de destino",
                    color = if (r.cumplida) OdinPaleta.Correcto else OdinPaleta.Incorrecto,
                    fontFamily = FuenteTitulo, fontSize = 17.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                BotonOdin(
                    if (consulta.terminada()) "Leer el caldero" else "Siguiente visión",
                    onClick = onSiguiente, acento = estilo.acento, modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TarjetaFe(fe: Fe, estilo: EstiloMundo, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(CutCornerShape(10.dp)).background(Color(0xE60B0907)).border(1.5.dp, estilo.metal, CutCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(fe.nombre, style = estiloTitulo(18.sp, color = estilo.acento))
        Text("+${fe.puntosAcierto} / −${-fe.puntosFallo}", color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 14.sp)
    }
}

@Composable
private fun TarjetaOpcion(texto: String, estilo: EstiloMundo, colorBorde: Color, onClick: (() -> Unit)?) {
    Text(
        texto,
        color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 16.sp, lineHeight = 22.sp,
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(CutCornerShape(8.dp))
            .background(Color(0xE60B0907)).border(1.5.dp, colorBorde, CutCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}

@Composable
private fun ContadorCircular(segundos: Int, total: Int, acento: Color) {
    val urgente = segundos <= SEGUNDOS_URGENCIA
    val color = if (urgente) OdinPaleta.Incorrecto else acento
    Box(Modifier.size(58.dp).semantics { contentDescription = "Quedan $segundos segundos" }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color(0x66000000), size.minDimension / 2)
            drawArc(color, -90f, 360f * segundos / total.coerceAtLeast(1), false, style = Stroke(4.dp.toPx()))
        }
        Text("$segundos", style = estiloTitulo(22.sp, color = color))
    }
}

// ---------------------------------------------------------------------------
// Pantalla 3 · Lectura del caldero
// ---------------------------------------------------------------------------

@Composable
internal fun LecturaCalderoScreen(estilo: EstiloMundo, resultado: ResultadoConsulta, onSalir: () -> Unit) {
    val lectura = resultado.lectura ?: Lectura.MALDICION
    val (color, frase) = when (lectura) {
        Lectura.PROFECIA_CUMPLIDA -> ColorProfecia to "Las Nornas hablaron por tu boca: tu destino es claro."
        Lectura.LECTURA_CLARA -> ColorClara to "Ves con claridad, aunque aún hay niebla en algunos hilos."
        Lectura.HUMO_DEL_CALDERO -> ColorHumo to "El humo cubre el caldero: conservas tu apuesta."
        Lectura.MALDICION -> ColorMaldicion to "Como Gullveig, ardes hoy y renaces mañana."
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("LECTURA DEL CALDERO", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTitulo, fontSize = 13.sp, letterSpacing = 1.5.sp)
        Text(lectura.nombre, style = estiloTitulo(30.sp, color = color, brillo = color), textAlign = TextAlign.Center)
        Text(frase, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))

        PanelTallado(estilo = estilo, modifier = Modifier.fillMaxWidth(), semilla = 21) {
            Column {
                resultado.respondidas.forEachIndexed { i, r ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (r.cumplida) "✓" else "✗", color = if (r.cumplida) OdinPaleta.Correcto else OdinPaleta.Incorrecto, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("${i + 1}. ${r.norna.nombre} · fe ${r.fe.nombre}", color = OdinPaleta.Hueso, fontFamily = FuenteTexto,
                            fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Text(if (r.puntos >= 0) "+${r.puntos}" else "−${-r.puntos}", color = estilo.acento, fontFamily = FuenteTitulo, fontSize = 15.sp)
                    }
                }
                DivisorOrnamental(estilo.metal, estilo.acento, Modifier.padding(vertical = 6.dp))
                FilaResumen("Puntos de destino", "${resultado.puntosDestino}")
                FilaResumen("Apuesta", "${resultado.apuesta}")
                if (resultado.premioBruto > 0) FilaResumen("Premio bruto", "${resultado.premioBruto}")
                if (resultado.diezmo > 0) FilaResumen("Diezmo de Odín", "−${resultado.diezmo}")
                val neto = resultado.pago - resultado.apuesta
                FilaResumen(if (neto >= 0) "Hidromiel ganado" else "Hidromiel perdido", "${kotlin.math.abs(neto)}")
            }
        }

        Spacer(Modifier.height(14.dp))
        val runa = resultado.runaObtenida
        if (runa != null) {
            Text("Runa obtenida", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTitulo, fontSize = 13.sp)
            Text(runa.simbolo, style = estiloRuna(64.sp, ColorProfecia, true))
            Text(runa.nombre, style = estiloTitulo(18.sp, color = ColorProfecia))
        } else if (lectura == Lectura.PROFECIA_CUMPLIDA) {
            // A14.1
            Text("Ya tienes las 24 runas del Futhark: la Völva solo te paga el premio.",
                color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(18.dp))
        BotonOdin("Volver al Valhalla", onClick = onSalir, acento = estilo.acento, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(etiqueta, color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(valor, color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Mensaje(estilo: EstiloMundo, titulo: String, texto: String, onSalir: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, style = estiloTitulo(24.sp))
        Spacer(Modifier.height(8.dp))
        Text(texto, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        BotonOdin("Volver", onClick = onSalir, acento = estilo.acento)
    }
}
