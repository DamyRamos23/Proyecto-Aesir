package com.aesir.odin.ui.alba

import com.aesir.odin.ui.forja.ModoVitrina
import com.aesir.odin.ui.forja.VitrinaScreen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.model.AccesoAlba
import com.aesir.odin.domain.model.Artefacto
import com.aesir.odin.domain.model.Desenlace
import com.aesir.odin.domain.model.Enemigo
import com.aesir.odin.domain.model.NocheAlba
import com.aesir.odin.domain.model.ResultadoNoche
import com.aesir.odin.domain.model.textoMultiplicador
import com.aesir.odin.ui.components.odin.ChipInicio
import com.aesir.odin.ui.components.odin.ColoresInicio
import com.aesir.odin.ui.components.odin.EscenaModo
import com.aesir.odin.ui.components.odin.EscenaWeb
import com.aesir.odin.ui.components.odin.PantallaInicioModo
import com.aesir.odin.ui.components.odin.PulsoEscena
import com.aesir.odin.ui.components.odin.ReglaInicio
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.Text
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

private val Verde = Color(0xFF9FD8C8)
private val Rojo = Color(0xFFFF8A6A)
private val Tarjeta = Color(0xF2141620)
private val Tinta = Color(0xFFF1EAD8)
private val TextoOpcion = Color(0xFFECE4D0)

/**
 * CU-01 Sobrevivir oleadas (Hasta el Alba). Una sola ruta que muestra, según el
 * estado: la pantalla de inicio (diseño aprobado del banquete), el campo de
 * batalla de cada oleada, la decisión de volver al banquete y el resumen.
 */
@Composable
fun HastaElAlbaScreen(vm: HastaElAlbaViewModel, onSalir: () -> Unit) {
    val estado by vm.estado.collectAsState()
    val saldo by vm.saldo.collectAsState()
    val confirmando by vm.confirmandoHuida.collectAsState()

    // E9.1: «atrás» durante la batalla pide confirmación.
    BackHandler(enabled = vm.enBatalla()) { vm.solicitarHuida() }

    when (estado) {
        EstadoAlba.CARGANDO -> Box(Modifier.fillMaxSize().background(ColoresInicio.Fondo), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ColoresInicio.Oro)
        }
        EstadoAlba.APOSTANDO, EstadoAlba.VALHALLA_CERRADO, EstadoAlba.NOCHE_JUGADA,
        EstadoAlba.HIDROMIEL_INSUFICIENTE, EstadoAlba.SIN_PREGUNTAS -> vm.acceso.collectAsState().value?.let {
            val eligiendo by vm.eligiendoArtefactos.collectAsState()
            val llevar by vm.llevar.collectAsState()
            if (eligiendo) {
                // Paso 4: la vitrina del salón en modo Hasta el Alba (elegir qué llevar a la noche)
                BackHandler { vm.cerrarVitrina() }
                VitrinaScreen(
                    modo = ModoVitrina.ALBA,
                    tiene = it.artefactos.map { f -> f.artefacto }.toSet(),
                    onVolver = vm::cerrarVitrina,
                    seleccion = llevar,
                    onAlternar = vm::alternarArtefacto,
                    onListo = vm::cerrarVitrina
                )
            } else InicioAlba(vm, estado, it, saldo, onSalir)
        }
        EstadoAlba.PREGUNTA, EstadoAlba.RESPUESTA, EstadoAlba.DECISION, EstadoAlba.OFRECER_DRAUPNIR -> CampoDeBatalla(vm, estado)
        EstadoAlba.RESUMEN -> vm.resultado.collectAsState().value?.let { ResumenNoche(it, vm.temasConError.collectAsState().value, onSalir) }
        EstadoAlba.ERROR -> MensajeAlba(vm.aviso.collectAsState().value ?: "Algo salió mal.", onSalir)
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = vm::cancelarHuida,
            containerColor = Color(0xFF14161F),
            title = { Text("¿Huir del campo de batalla?", color = Tinta, fontFamily = FuenteTitulo) },
            text = {
                Text(
                    "Salir equivale a caer en batalla: pierdes tu apuesta y no cobras el multiplicador alcanzado. La noche de hoy ya cuenta.",
                    color = Tinta, fontFamily = FuenteTexto
                )
            },
            confirmButton = { TextButton(onClick = vm::cancelarHuida) { Text("Seguir peleando", color = ColoresInicio.Oro, fontFamily = FuenteTitulo) } },
            dismissButton = { TextButton(onClick = vm::confirmarHuida) { Text("Huir", color = Rojo, fontFamily = FuenteTitulo) } }
        )
    }
}

// ---------------------------------------------------------------------------
// Pantalla de inicio (paso 4)
// ---------------------------------------------------------------------------

@Composable
private fun InicioAlba(vm: HastaElAlbaViewModel, estado: EstadoAlba, acceso: AccesoAlba, saldo: Int, onSalir: () -> Unit) {
    val apuesta by vm.apuesta.collectAsState()
    val aviso by vm.aviso.collectAsState()
    val abierto = estado == EstadoAlba.APOSTANDO
    val motivo = when (estado) {
        EstadoAlba.VALHALLA_CERRADO -> "Hasta el Alba se abre al superar tu primer Ragnarök." +
            (acceso.mundoEnCurso?.let { " Tu mundo en curso: $it." } ?: "")
        EstadoAlba.NOCHE_JUGADA -> acceso.nocheDeHoy?.let { "Ya peleaste esta noche: ${it.desenlace.nombre.lowercase()} en la oleada ${it.oleadaAlcanzada}. La siguiente noche empieza en ${tiempoHastaManana()}." }
            ?: "Ya peleaste esta noche. La siguiente empieza en ${tiempoHastaManana()}."
        EstadoAlba.HIDROMIEL_INSUFICIENTE -> "Te faltan ${acceso.faltante} de hidromiel: no puedes apostar lo que necesitas para entrar a tu siguiente nivel (piso de ${acceso.rango.piso})."
        EstadoAlba.SIN_PREGUNTAS -> "Supera más temas del roadmap: aún no hay preguntas para la batalla."
        else -> aviso
    }
    PantallaInicioModo(
        escena = EscenaModo.ALBA,
        titulo = "Hasta el Alba",
        lema = "Alza tu cuerno en el banquete eterno y resiste siete oleadas hasta que salga el sol.",
        saldo = saldo,
        chips = listOf(
            ChipInicio(if (estado == EstadoAlba.NOCHE_JUGADA) "Sin noches hoy" else "1 noche hoy", borde = Color(0x80F0A35A)),
            ChipInicio("Artefactos:", "${acceso.artefactos.size} / ${Artefacto.entries.size}")
        ),
        textoBoton = when (estado) {
            EstadoAlba.VALHALLA_CERRADO -> "Supera tu primer Ragnarök"
            EstadoAlba.NOCHE_JUGADA -> "Vuelve mañana"
            EstadoAlba.HIDROMIEL_INSUFICIENTE -> "Hidromiel insuficiente"
            EstadoAlba.SIN_PREGUNTAS -> "Faltan preguntas"
            else -> "Bajar al campo"
        },
        botonHabilitado = abierto && vm.apuestaValida(),
        onComenzar = vm::bajarAlCampo,
        onVolver = onSalir,
        aviso = motivo,
        extra = if (abierto) { {
            SelectorApuestaAlba(apuesta, acceso, vm.apuestaValida(), vm::cambiarApuesta)
            val llevar by vm.llevar.collectAsState()
            Text(
                if (acceso.artefactos.isEmpty()) "Ver la vitrina del salón" else "Artefactos para la noche: ${llevar.size} / ${acceso.artefactos.size} · elegir",
                color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 16.sp,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button, onClick = vm::abrirVitrina)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        } } else null,
        comoSeJuega = { ReglasAlba(acceso) }
    )
}

@Composable
private fun SelectorApuestaAlba(apuesta: Int, acceso: AccesoAlba, valida: Boolean, onCambiar: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonRedondo("−", "Apostar menos") { onCambiar(apuesta - PASO_APUESTA) }
            Text(
                "$apuesta", color = if (valida) Color(0xFFFFF6E0) else Rojo, fontFamily = FuenteTitulo,
                fontWeight = FontWeight.Bold, fontSize = 34.sp, textAlign = TextAlign.Center,
                modifier = Modifier.width(100.dp).semantics { contentDescription = "Apuesta de $apuesta de hidromiel" }
            )
            BotonRedondo("+", "Apostar más") { onCambiar(apuesta + PASO_APUESTA) }
        }
        Text("Apuesta de ${acceso.rango.minimo} a ${acceso.rango.maximo}", color = if (valida) ColoresInicio.Tenue else Rojo,
            fontFamily = FuenteTexto, fontSize = 14.sp)
    }
}

@Composable
private fun BotonRedondo(texto: String, descripcion: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(Color(0xCC06070D)).border(1.dp, ColoresInicio.Oro.copy(alpha = .5f), CircleShape)
            .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) { Text(texto, color = ColoresInicio.Oro, fontFamily = FuenteTitulo, fontSize = 22.sp) }
}

@Composable
private fun ColumnScope.ReglasAlba(acceso: AccesoAlba) {
    ReglaInicio("ᛏ", "Apuesta de ${PoliticaCosto.APUESTA_MIN_ALBA} a ${PoliticaCosto.APUESTA_MAX_ALBA} de hidromiel, sin bajar del piso de apuesta. Una noche por día.")
    ReglaInicio("ᚦ", "Cada golpe es una pregunta de tus temas superados. Si aciertas, el enemigo pierde un golpe; si fallas o se acaba el tiempo, se rompe uno de tus 3 escudos.")
    ReglaInicio("ᛉ", "Al vencer a cada enemigo decides: vuelves al banquete y cobras tu apuesta por el multiplicador alcanzado, o sigues peleando.")
    ReglaInicio("ᚺ", "Si se rompe tu tercer escudo caes en batalla y pierdes la apuesta. El diezmo de Odín es del 10 % del premio.")
    Spacer(Modifier.height(8.dp))
    Text("Las siete horas de la noche", color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontSize = 15.sp,
        modifier = Modifier.semantics { heading() })
    Enemigo.entries.forEach { e ->
        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp).semantics(mergeDescendants = true) {}) {
            Text("${e.numero}. ${e.nombre}", color = Tinta, fontFamily = FuenteTexto, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text("${e.golpes} golpes · ${e.segundosPorPregunta} s · ${textoMultiplicador(e.multiplicador)}",
                color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 14.sp)
        }
    }
    Spacer(Modifier.height(8.dp))
    Text("Artefactos de la noche", color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontSize = 15.sp,
        modifier = Modifier.semantics { heading() })
    if (acceso.artefactos.isEmpty()) {
        Text("Aún no tienes artefactos. Fórjalos en la Apuesta de Brokkr: cada uno se usa una vez por noche.",
            color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 15.sp)
    } else acceso.artefactos.forEach { f ->
        ReglaInicio(RUNA_ARTEFACTO.getValue(f.artefacto), "${f.artefacto.nombre}: ${f.artefacto.poder}")
    }
}

// ---------------------------------------------------------------------------
// Campo de batalla (pasos 8 a 16)
// ---------------------------------------------------------------------------

@Composable
private fun CampoDeBatalla(vm: HastaElAlbaViewModel, estado: EstadoAlba) {
    val noche by vm.noche.collectAsState()
    val pulso by vm.pulso.collectAsState()
    val confirmando by vm.confirmandoHuida.collectAsState()
    val n = noche ?: return

    BoxWithConstraints(Modifier.fillMaxSize().background(ColoresInicio.Fondo)) {
        val altoEscena = maxHeight * 0.46f
        EscenaWeb(
            archivo = "escena_batalla.html",
            descripcion = "${n.enemigo.nombre}, oleada ${n.enemigo.numero} de ${NocheAlba.OLEADAS}",
            estado = "odinEnemigo(${n.enemigo.numero})",
            pulso = pulso,
            modifier = Modifier.fillMaxWidth().height(altoEscena + 40.dp)
        )
        Column(Modifier.fillMaxSize()) {
            BarraSuperior(n, vm::solicitarHuida)
            Spacer(Modifier.height((altoEscena - 120.dp).coerceAtLeast(0.dp)))
            NombreEnemigo(n)
            FilaEstado(vm, n, estado)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (estado) {
                    EstadoAlba.DECISION -> PanelDecision(vm, n)
                    EstadoAlba.OFRECER_DRAUPNIR -> PanelDraupnir(vm, n)
                    else -> if (!confirmando) PanelPregunta(vm, estado)
                }
            }
        }
        VeredictoFlotante(vm, Modifier.align(Alignment.TopCenter).padding(top = altoEscena * 0.45f))
    }
}

@Composable
private fun BarraSuperior(n: NocheAlba, onHuir: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(Color(0x9906070D)).border(1.dp, ColoresInicio.Oro.copy(alpha = .45f), CircleShape)
                .clickable(role = Role.Button, onClick = onHuir).semantics { contentDescription = "Huir del campo de batalla" },
            contentAlignment = Alignment.Center
        ) { Text("✕", color = ColoresInicio.Oro, fontSize = 18.sp) }
        Spacer(Modifier.weight(1f))
        Pildora {
            Text("OLEADA ${n.enemigo.numero} / ${NocheAlba.OLEADAS}", color = Color(0xFFF1DFB2), fontFamily = FuenteTitulo, fontSize = 13.sp, letterSpacing = 1.5.sp)
            Box(Modifier.padding(horizontal = 8.dp).width(1.dp).height(16.dp).background(ColoresInicio.Oro.copy(alpha = .4f)))
            Text(n.enemigo.hora, color = Verde, fontFamily = FuenteTexto, fontSize = 14.sp)
        }
        Spacer(Modifier.weight(1f))
        Pildora(Modifier.semantics(mergeDescendants = true) {
            contentDescription = if (n.multiplicadorAlcanzado == 0) "Aún sin multiplicador alcanzado" else "Multiplicador alcanzado ${textoMultiplicador(n.multiplicadorDeCobro)}"
        }) {
            Text(if (n.multiplicadorAlcanzado == 0) "×—" else textoMultiplicador(n.multiplicadorDeCobro),
                color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun Pildora(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Row(
        modifier.height(34.dp).clip(RoundedCornerShape(17.dp)).background(Color(0xB306070D))
            .border(1.dp, ColoresInicio.Oro.copy(alpha = .45f), RoundedCornerShape(17.dp)).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { contenido() }
}

@Composable
private fun NombreEnemigo(n: NocheAlba) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(n.enemigo.nombre.uppercase(), color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold,
            fontSize = 24.sp, letterSpacing = 3.sp, modifier = Modifier.semantics { heading() })
        Row(
            Modifier.padding(top = 4.dp).semantics(mergeDescendants = true) {
                contentDescription = "Le quedan ${n.golpesRestantes} de ${n.enemigo.golpes} golpes. Esta oleada vale ${textoMultiplicador(n.enemigo.multiplicador)}."
            },
            horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(n.enemigo.golpes) { i ->
                Box(
                    Modifier.width(30.dp).height(8.dp).clip(RoundedCornerShape(4.dp))
                        .background(if (i < n.golpesRestantes) Verde else Color.Transparent)
                        .border(1.dp, Verde, RoundedCornerShape(4.dp))
                )
            }
            Text("  vale ${textoMultiplicador(n.enemigo.multiplicador)}", color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FilaEstado(vm: HastaElAlbaViewModel, n: NocheAlba, estado: EstadoAlba) {
    val segundos by vm.segundos.collectAsState()
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.semantics(mergeDescendants = true) { contentDescription = "${n.escudos} de ${NocheAlba.ESCUDOS_POR_NOCHE} escudos" },
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(NocheAlba.ESCUDOS_POR_NOCHE) { i -> Escudo(vivo = i < n.escudos) }
        }
        Spacer(Modifier.weight(1f))
        if (estado == EstadoAlba.PREGUNTA || estado == EstadoAlba.RESPUESTA) {
            Contador(segundos, n.enemigo.segundosPorPregunta)
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Artefacto.entries.forEach { a -> BotonArtefacto(vm, n, a, estado) }
        }
    }
}

@Composable
private fun Escudo(vivo: Boolean) {
    Canvas(Modifier.size(28.dp).alpha(if (vivo) 1f else .3f)) {
        val r = size.minDimension / 2
        drawCircle(if (vivo) Color(0xFF6A4A2A) else Color(0xFF1A1A20), r - 1.5f)
        drawCircle(ColoresInicio.Oro, r - 1.5f, style = Stroke(3f))
        drawLine(Color(0xFF3A2A1C), Offset(2f, r), Offset(size.width - 2f, r), 4f)
        drawLine(Color(0xFF3A2A1C), Offset(r, 2f), Offset(r, size.height - 2f), 4f)
        drawCircle(Color(0xFFC9A86A), r * .3f)
    }
}

@Composable
private fun Contador(segundos: Int, total: Int) {
    val color = if (segundos <= 5) Rojo else ColoresInicio.OroTitulo
    Box(
        Modifier.size(56.dp).semantics {
            contentDescription = "$segundos segundos para responder"
            liveRegion = LiveRegionMode.Polite
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val grosor = 4.dp.toPx()
            drawCircle(Color(0xCC06070D))
            drawArc(ColoresInicio.Oro.copy(alpha = .25f), 0f, 360f, false, Offset(grosor / 2, grosor / 2), Size(size.width - grosor, size.height - grosor), style = Stroke(grosor))
            drawArc(color, -90f, 360f * segundos / total.coerceAtLeast(1), false, Offset(grosor / 2, grosor / 2),
                Size(size.width - grosor, size.height - grosor), style = Stroke(grosor, cap = StrokeCap.Round))
        }
        Text("$segundos", color = color, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun BotonArtefacto(vm: HastaElAlbaViewModel, n: NocheAlba, artefacto: Artefacto, estado: EstadoAlba) {
    val forjado = n.artefactos.firstOrNull { it.artefacto == artefacto }
    val usado = artefacto in n.artefactosUsados
    val momento = when (artefacto) {
        Artefacto.DRAUPNIR -> estado == EstadoAlba.DECISION
        else -> estado == EstadoAlba.PREGUNTA
    }
    val activo = forjado != null && !usado && momento
    val descripcion = when {
        forjado == null -> "${artefacto.nombre}: no lo llevaste esta noche"
        usado -> "${artefacto.nombre}: ya usado esta noche"
        else -> "${artefacto.nombre}: ${artefacto.poder}"
    }
    Box(
        Modifier.size(40.dp).alpha(if (activo) 1f else .4f).clip(RoundedCornerShape(10.dp)).background(Color(0xB306070D))
            .border(1.dp, ColoresInicio.Oro, RoundedCornerShape(10.dp))
            .clickable(enabled = activo, role = Role.Button) { vm.usar(artefacto) }
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        Text(RUNA_ARTEFACTO.getValue(artefacto), color = ColoresInicio.OroTitulo, fontSize = 20.sp)
    }
}

@Composable
private fun PanelPregunta(vm: HastaElAlbaViewModel, estado: EstadoAlba) {
    val pregunta by vm.pregunta.collectAsState()
    val seleccion by vm.seleccion.collectAsState()
    val senaladas by vm.senaladas.collectAsState()
    val eliminadas by vm.eliminadas.collectAsState()
    val aviso by vm.aviso.collectAsState()
    val p = pregunta ?: return
    val respondiendo = estado == EstadoAlba.RESPUESTA

    LaunchedEffect(aviso) {
        if (aviso != null) { delay(2600); vm.limpiarAviso() }
    }

    Column(Modifier.fillMaxSize().navigationBarsPadding().padding(start = 14.dp, end = 14.dp, bottom = 12.dp)) {
        Column(
            Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(Tarjeta, Color(0xFA0A0B12))))
                .border(1.dp, ColoresInicio.Oro.copy(alpha = .3f), RoundedCornerShape(16.dp))
                .verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(if (p.esSeleccionMultiple) "Elige todas las correctas" else "Elige una respuesta",
                color = Verde, fontFamily = FuenteTitulo, fontSize = 11.sp, letterSpacing = 2.sp)
            Text(p.enunciado, color = Tinta, fontFamily = FuenteTexto, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 24.sp)
            p.opciones.forEachIndexed { i, texto ->
                OpcionBatalla(
                    texto = texto,
                    elegida = i in seleccion,
                    senalada = i in senaladas,
                    eliminada = i in eliminadas,
                    habilitada = !respondiendo,
                    multiple = p.esSeleccionMultiple
                ) { vm.alternarOpcion(i) }
            }
            if (aviso != null) Text(aviso!!, color = Verde, fontFamily = FuenteTexto, fontSize = 15.sp)
        }
        Spacer(Modifier.height(8.dp))
        BotonPrincipal("Atacar", habilitado = !respondiendo && seleccion.isNotEmpty(), onClick = vm::confirmar)
    }
}

@Composable
private fun OpcionBatalla(
    texto: String, elegida: Boolean, senalada: Boolean, eliminada: Boolean, habilitada: Boolean, multiple: Boolean, onClick: () -> Unit
) {
    val borde = when {
        senalada -> Verde
        elegida -> ColoresInicio.Oro
        else -> ColoresInicio.Oro.copy(alpha = .28f)
    }
    Row(
        Modifier.fillMaxWidth().heightIn(min = 46.dp).alpha(if (eliminada) .3f else 1f).clip(RoundedCornerShape(12.dp))
            .background(if (elegida) Color(0x29D4AD5C) else Color(0x08FFFFFF)).border(1.dp, borde, RoundedCornerShape(12.dp))
            .clickable(enabled = habilitada && !eliminada, role = if (multiple) Role.Checkbox else Role.RadioButton, onClick = onClick)
            .semantics { stateDescription = buildString {
                append(if (elegida) "elegida" else "sin elegir")
                if (senalada) append(", señalada por Gungnir")
                if (eliminada) append(", eliminada por Gungnir")
            } }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(22.dp).clip(RoundedCornerShape(if (multiple) 6.dp else 11.dp))
                .background(if (elegida) ColoresInicio.Oro else Color.Transparent).border(1.5.dp, borde, RoundedCornerShape(if (multiple) 6.dp else 11.dp)),
            contentAlignment = Alignment.Center
        ) { if (elegida) Text("✓", color = ColoresInicio.TintaBoton, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(10.dp))
        Text(texto, color = TextoOpcion, fontFamily = FuenteTexto, fontSize = 17.sp, modifier = Modifier.weight(1f))
        if (senalada) Text("ᚷ", color = Verde, fontSize = 18.sp)
    }
}

@Composable
private fun VeredictoFlotante(vm: HastaElAlbaViewModel, modifier: Modifier) {
    val veredicto by vm.veredicto.collectAsState()
    val estado by vm.estado.collectAsState()
    val v = veredicto ?: return
    if (estado != EstadoAlba.RESPUESTA) return
    val golpe = v == Veredicto.GOLPE
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color(0xE606070D))
            .border(1.dp, if (golpe) ColoresInicio.Oro else Rojo, RoundedCornerShape(14.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(v.titulo, color = if (golpe) ColoresInicio.OroTitulo else Rojo, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(v.detalle, color = Tinta, fontFamily = FuenteTexto, fontSize = 15.sp)
    }
}

@Composable
private fun PanelDecision(vm: HastaElAlbaViewModel, n: NocheAlba) {
    val aviso by vm.aviso.collectAsState()
    val siguiente = n.enemigo.siguiente()
    Column(
        Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("¡${n.enemigo.nombre} vencido!", color = Verde, fontFamily = FuenteTitulo, fontSize = 15.sp, letterSpacing = 1.sp)
        Text("¿Vuelves al banquete o sigues peleando?", color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold,
            fontSize = 21.sp, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Dato("Cobrarías", "${vm.premioSiCobra()}", "${textoMultiplicador(n.multiplicadorDeCobro)} − diezmo", Modifier.weight(1f))
            siguiente?.let { Dato("Siguiente", it.nombre, "${it.golpes} golpes · ${it.segundosPorPregunta} s · ${textoMultiplicador(it.multiplicador)}", Modifier.weight(1f)) }
        }
        if (aviso != null) Text(aviso!!, color = Verde, fontFamily = FuenteTexto, fontSize = 15.sp, textAlign = TextAlign.Center)
        BotonPrincipal("Volver al banquete", habilitado = true, onClick = vm::volverAlBanquete)
        BotonSecundario("Seguir peleando", vm::seguir)
        Text("Si caes después, pierdes la apuesta y el multiplicador alcanzado.", color = ColoresInicio.Tenue,
            fontFamily = FuenteTexto, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PanelDraupnir(vm: HastaElAlbaViewModel, n: NocheAlba) {
    val draupnir = n.disponible(Artefacto.DRAUPNIR)
    Column(
        Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("¡Sobreviviste hasta el alba!", color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold,
            fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
        Text(
            "Aún tienes Draupnir sin usar. ¿Lo activas antes de cobrar? Tu multiplicador final sería ×4.5.",
            color = Tinta, fontFamily = FuenteTexto, fontSize = 17.sp, textAlign = TextAlign.Center
        )
        BotonPrincipal("Activar Draupnir y cobrar", habilitado = true) { vm.cobrarAlba(true) }
        BotonSecundario("Cobrar sin Draupnir") { vm.cobrarAlba(false) }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String, detalle: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).background(Tarjeta).border(1.dp, ColoresInicio.Oro.copy(alpha = .3f), RoundedCornerShape(12.dp))
            .padding(10.dp).semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(etiqueta.uppercase(), color = ColoresInicio.Antetitulo, fontFamily = FuenteTitulo, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Text(valor, color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
        Text(detalle, color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun BotonPrincipal(texto: String, habilitado: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(54.dp).alpha(if (habilitado) 1f else .5f).clip(RoundedCornerShape(14.dp))
            .background(ColoresInicio.OroBoton).border(1.dp, ColoresInicio.BordeBoton, RoundedCornerShape(14.dp))
            .clickable(enabled = habilitado, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(texto.uppercase(), color = ColoresInicio.TintaBoton, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 2.sp)
    }
}

@Composable
private fun BotonSecundario(texto: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xB306070D))
            .border(1.dp, ColoresInicio.Oro.copy(alpha = .6f), RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(texto.uppercase(), color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 2.sp)
    }
}

// ---------------------------------------------------------------------------
// Resumen de la noche (pasos 21 a 23, A11.2, A16.1)
// ---------------------------------------------------------------------------

@Composable
private fun ResumenNoche(r: ResultadoNoche, temasConError: List<String>, onSalir: () -> Unit) {
    Box(Modifier.fillMaxSize().background(ColoresInicio.Fondo)) {
        EscenaWeb(
            archivo = "escena_batalla.html",
            descripcion = r.desenlace.nombre,
            estado = "odinEnemigo(${if (r.desenlace == Desenlace.ALBA) 7 else r.oleadaAlcanzada})",
            modifier = Modifier.fillMaxWidth().height(260.dp).alpha(.55f)
        )
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.height(120.dp))
            val color = when (r.desenlace) {
                Desenlace.ALBA -> ColoresInicio.OroTitulo
                Desenlace.RETIRADA -> Verde
                Desenlace.CAIDA -> Rojo
            }
            Text(r.desenlace.nombre.uppercase(), color = color, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold,
                fontSize = 26.sp, letterSpacing = 2.sp, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
            Text(
                when (r.desenlace) {
                    Desenlace.ALBA -> "Suena el cuerno de Odín: el sol sale sobre el campo y tú sigues en pie."
                    Desenlace.RETIRADA -> "Vuelves al banquete con tu botín de la oleada ${r.oleadasSuperadas}."
                    Desenlace.CAIDA -> "Caíste en la oleada ${r.oleadaAlcanzada}. Resucitarás la noche siguiente."
                },
                color = Tinta, fontFamily = FuenteTexto, fontStyle = FontStyle.Italic, fontSize = 17.sp, textAlign = TextAlign.Center
            )
            if (r.einherjarNuevo) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0x33D4AD5C))
                        .border(1.dp, ColoresInicio.Oro, RoundedCornerShape(14.dp)).padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Logro «Hasta el Alba»", color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Ahora llevas el título de Einherjar en tu salón.", color = Tinta, fontFamily = FuenteTexto, fontSize = 15.sp)
                }
            }
            Column(
                Modifier.fillMaxWidth().widthIn(max = 520.dp).clip(RoundedCornerShape(16.dp)).background(Tarjeta)
                    .border(1.dp, ColoresInicio.Oro.copy(alpha = .3f), RoundedCornerShape(16.dp)).padding(14.dp)
            ) {
                Fila("Oleadas superadas", "${r.oleadasSuperadas} de ${NocheAlba.OLEADAS}")
                Fila("Aciertos", "${r.aciertos}")
                Fila("Errores", "${r.errores}")
                Fila("Escudos restantes", "${r.escudosRestantes}")
                Fila("Artefactos usados", if (r.artefactosUsados.isEmpty()) "Ninguno" else r.artefactosUsados.joinToString { it.nombre })
                Fila("Apuesta", "${r.apuesta}")
                Fila("Multiplicador", if (r.desenlace == Desenlace.CAIDA) "Perdido" else textoMultiplicador(r.multiplicador))
                Fila("Premio bruto", "${r.premioBruto}")
                Fila("Diezmo de Odín", "${r.diezmo}")
                Fila("Premio neto", "${r.premioNeto}", destacado = true)
            }
            if (r.premioPendiente) {
                Text("No se pudo acreditar el premio ahora: se acreditará la próxima vez que abras ODÍN.",
                    color = Rojo, fontFamily = FuenteTexto, fontSize = 15.sp, textAlign = TextAlign.Center)
            }
            if (r.desenlace == Desenlace.CAIDA && temasConError.isNotEmpty()) {
                Text("Repasa las lecciones interactivas de: ${temasConError.joinToString()}.", color = ColoresInicio.Tenue,
                    fontFamily = FuenteTexto, fontSize = 15.sp, textAlign = TextAlign.Center)
            }
            if (r.desenlace == Desenlace.CAIDA) {
                // A11.2 paso 4
                Text("Caíste en batalla, pero resucitarás la noche siguiente en el Valhalla.", color = ColoresInicio.Tenue,
                    fontFamily = FuenteTexto, fontSize = 15.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(4.dp))
            BotonPrincipal("Volver al Valhalla", habilitado = true, onClick = onSalir)
        }
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).semantics(mergeDescendants = true) {}) {
        Text(etiqueta, color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(valor, color = if (destacado) ColoresInicio.OroTitulo else Tinta, fontFamily = if (destacado) FuenteTitulo else FuenteTexto,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal, fontSize = if (destacado) 18.sp else 16.sp)
    }
}

@Composable
private fun MensajeAlba(texto: String, onSalir: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(ColoresInicio.Fondo).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text("Algo salió mal", color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        Text(texto, color = Tinta, fontFamily = FuenteTexto, fontSize = 16.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        BotonPrincipal("Volver al Valhalla", habilitado = true, onClick = onSalir)
    }
}

/** A2.2: tiempo que falta para la siguiente noche (inicio del siguiente día real). */
@Composable
private fun tiempoHastaManana(): String {
    val ahora = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(30_000); ahora.longValue = System.currentTimeMillis() } }
    val zona = ZoneId.systemDefault()
    val inicio = java.time.Instant.ofEpochMilli(ahora.longValue).atZone(zona).toLocalDateTime()
    val manana = LocalDate.now(zona).plusDays(1).atStartOfDay()
    val falta = Duration.between(inicio, manana).coerceAtLeast(Duration.ZERO)
    return "${falta.toHours()} h ${falta.toMinutes() % 60} min"
}

private const val PASO_APUESTA = 5

/** Runa que identifica cada artefacto en los botones. */
private val RUNA_ARTEFACTO = mapOf(
    Artefacto.MJOLNIR to "ᚦ",
    Artefacto.GUNGNIR to "ᚷ",
    Artefacto.DRAUPNIR to "ᛟ",
    Artefacto.SKIDBLADNIR to "ᛗ"
)
