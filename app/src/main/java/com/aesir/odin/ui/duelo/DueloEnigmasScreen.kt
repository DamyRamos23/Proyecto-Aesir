package com.aesir.odin.ui.duelo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.model.AccesoDuelo
import com.aesir.odin.domain.model.CierreDuelo
import com.aesir.odin.domain.model.CuernoHidromiel
import com.aesir.odin.domain.model.EstadoFinalDuelo
import com.aesir.odin.domain.model.TipoPregunta
import com.aesir.odin.ui.components.odin.BotonOdin
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.ContadorHidromiel
import com.aesir.odin.ui.components.odin.DivisorOrnamental
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.PanelTallado
import com.aesir.odin.ui.components.odin.SelloRunico
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.EstilosMundo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import com.aesir.odin.ui.theme.Text

/** El duelo usa el estilo dorado (madera y oro) de las demás actividades. */
private val Estilo = EstilosMundo.Madera
private val Pergamino = Color(0xFFF3E2BC)
private val TintaPergamino = Color(0xFF33220F)

/** CU-13 Duelo de enigmas: elige la vista según [EstadoDuelo]. */
@Composable
fun DueloEnigmasScreen(vm: DueloEnigmasViewModel, onSalir: () -> Unit) {
    val estado by vm.estado.collectAsState()
    val saldo by vm.saldo.collectAsState(initial = 0)
    val abandonado by vm.abandonado.collectAsState()

    LaunchedEffect(Unit) { vm.cargarDuelo() }
    LaunchedEffect(abandonado) { if (abandonado) onSalir() }

    val enJuego = estado in setOf(
        EstadoDuelo.EN_CURSO, EstadoDuelo.ULTIMA_ADIVINANZA, EstadoDuelo.PREGUNTAS_AGOTADAS, EstadoDuelo.CONFIRMANDO_ABANDONO
    )
    // E7.1: durante el duelo, «atrás» pide confirmación.
    BackHandler(enabled = enJuego) { vm.solicitarAbandono() }

    FondoMundo(estilo = Estilo, alturaPaisaje = 300.dp, paisajeInferior = true) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BotonVolver(if (enJuego) "Salir" else "Mundos", { if (enJuego) vm.solicitarAbandono() else onSalir() })
                Spacer(Modifier.weight(1f))
                ContadorHidromiel(saldo, metal = Estilo.metal)
            }
            Spacer(Modifier.height(8.dp))
            when (estado) {
                EstadoDuelo.CARGANDO -> Text("El gigante se prepara…", style = estiloTitulo(18.sp), modifier = Modifier.padding(top = 80.dp))
                EstadoDuelo.CONFIRMANDO_APUESTA, EstadoDuelo.SIN_RAGNAROK_SUPERADO, EstadoDuelo.LIMITE_DIARIO_ALCANZADO,
                EstadoDuelo.HIDROMIEL_INSUFICIENTE, EstadoDuelo.CONCEPTOS_INSUFICIENTES -> PantallaReto(vm, estado, saldo, onSalir)
                EstadoDuelo.EN_CURSO, EstadoDuelo.ULTIMA_ADIVINANZA, EstadoDuelo.PREGUNTAS_AGOTADAS,
                EstadoDuelo.CONFIRMANDO_ABANDONO -> PantallaTablero(vm, estado)
                EstadoDuelo.TERMINADO -> vm.cierre.collectAsState().value?.let { ResultadoDueloScreen(it, onSalir) { vm.cargarDuelo() } }
                EstadoDuelo.ERROR -> {
                    Text(vm.mensaje.collectAsState().value ?: "Algo salió mal.", color = OdinPaleta.Incorrecto, fontFamily = FuenteTexto, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 60.dp))
                    Spacer(Modifier.height(16.dp))
                    BotonOdin("Volver a Mundos", onSalir, Estilo.acento, Modifier.fillMaxWidth(), metal = Estilo.metal)
                }
            }
        }
    }

    if (estado == EstadoDuelo.CONFIRMANDO_ABANDONO) {
        AlertDialog(
            onDismissRequest = vm::cancelarAbandono,
            containerColor = OdinPaleta.HollinAlto,
            title = { Text("¿Abandonar el duelo?", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo) },
            text = {
                Text(
                    "Si sales ahora pierdes los ${PoliticaCosto.APUESTA_DUELO} de hidromiel que apostaste y el duelo cuenta para el límite de hoy.",
                    color = OdinPaleta.Hueso, fontFamily = FuenteTexto
                )
            },
            confirmButton = { TextButton(onClick = vm::cancelarAbandono) { Text("Seguir", color = Estilo.acento, fontFamily = FuenteTitulo) } },
            dismissButton = { TextButton(onClick = vm::confirmarAbandono) { Text("Abandonar", color = OdinPaleta.Incorrecto, fontFamily = FuenteTitulo) } }
        )
    }
}

// ---------------------------------------------------------------------------
// 1 · Reto del gigante (pasos 1 a 3) y sin acceso (E4.1)
// ---------------------------------------------------------------------------

@Composable
private fun ColumnScope.PantallaReto(vm: DueloEnigmasViewModel, estado: EstadoDuelo, saldo: Int, onSalir: () -> Unit) {
    val acceso by vm.acceso.collectAsState()
    val mensaje by vm.mensaje.collectAsState()

    SelloRunico(runa = "ᛃ", estilo = Estilo, tamano = 60.dp)
    Spacer(Modifier.height(8.dp))
    Text("DUELO DE ENIGMAS", style = estiloTitulo(12.sp, color = Estilo.metal), maxLines = 1)
    Text("El reto del gigante", style = estiloTitulo(28.sp, brillo = Estilo.acento), maxLines = 1)
    Text(
        "«Escondí un saber entre doce tarjetas. Pregúntame, y solo diré sí o no».",
        color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontStyle = FontStyle.Italic, fontSize = 15.sp,
        textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
    )

    PanelTallado(estilo = Estilo, modifier = Modifier.fillMaxWidth(), semilla = 13) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("REGLAS DEL DUELO", style = estiloTitulo(14.sp, color = Estilo.metalClaro), modifier = Modifier.align(Alignment.CenterHorizontally), maxLines = 1)
            DivisorOrnamental(Estilo.metal, Estilo.acento)
            Regla("ᛁ", "Apuestas ${PoliticaCosto.APUESTA_DUELO} de hidromiel y llenas el cuerno con ${CuernoHidromiel.SORBOS_INICIALES} sorbos.")
            Regla("ᛟ", "Cada pregunta cuesta 1 sorbo. Una adivinanza fallida, 2.")
            Regla("ᚷ", "Si aciertas recuperas los ${PoliticaCosto.APUESTA_DUELO} y ganas ${PoliticaCosto.HIDROMIEL_POR_SORBO} por cada sorbo que te sobre. Sin diezmo.")
            Regla("ᚱ", "Algunas preguntas son enigmas: interprétalas antes de gastar un sorbo.")
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Dato("${PoliticaCosto.APUESTA_DUELO}", "apuesta", Modifier.weight(1f))
        Dato("${acceso?.duelosRestantes ?: "–"} / ${PoliticaCosto.MAX_DUELOS_DIA}", "duelos hoy", Modifier.weight(1f))
        Dato("$saldo", "tu hidromiel", Modifier.weight(1f))
    }
    Spacer(Modifier.height(14.dp))

    val aviso = when (estado) {
        EstadoDuelo.SIN_RAGNAROK_SUPERADO -> "El gigante solo reta a quien ya superó un Ragnarök." to "Supera el reto de fin de mundo de UML para despertarlo."
        EstadoDuelo.LIMITE_DIARIO_ALCANZADO -> "Ya jugaste los ${PoliticaCosto.MAX_DUELOS_DIA} duelos de hoy" to "El gigante vuelve a aceptar retos mañana."
        EstadoDuelo.HIDROMIEL_INSUFICIENTE -> "Te faltan ${acceso?.faltante ?: 0} de hidromiel" to (mensaje ?: "Supera un nivel o vuelve por tu recompensa diaria.")
        EstadoDuelo.CONCEPTOS_INSUFICIENTES -> "Aún no hay suficientes saberes" to (mensaje ?: "Supera más temas para que el gigante pueda esconder uno.")
        else -> null
    }
    if (aviso != null) {
        AvisoSinAcceso(aviso.first, aviso.second)
        Spacer(Modifier.height(12.dp))
    }
    BotonOdin(
        "Apostar ${PoliticaCosto.APUESTA_DUELO} y retar", vm::confirmarApuesta, Estilo.acento, Modifier.fillMaxWidth(),
        habilitado = estado == EstadoDuelo.CONFIRMANDO_APUESTA, metal = Estilo.metal
    )
    Spacer(Modifier.height(10.dp))
    BotonOdin("Ahora no", onSalir, Estilo.acento, Modifier.fillMaxWidth(), primario = false, metal = Estilo.metal)
}

@Composable
private fun Regla(runa: String, texto: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(runa, style = estiloTitulo(16.sp, color = Estilo.acento), modifier = Modifier.width(24.dp), maxLines = 1)
        Text(texto, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 15.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun Dato(valor: String, etiqueta: String, modifier: Modifier) {
    Column(
        modifier.border(1.dp, Estilo.metal.copy(alpha = .5f)).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, style = estiloTitulo(20.sp, color = Estilo.acento), maxLines = 1)
        Text(etiqueta, color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun AvisoSinAcceso(titulo: String, detalle: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(OdinPaleta.HollinAlto)
            .border(1.5.dp, OdinPaleta.Hierro)
            .padding(14.dp)
    ) {
        Text(titulo, style = estiloTitulo(15.sp))
        Text(detalle, color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

// ---------------------------------------------------------------------------
// 2 y 3 · Tablero, preguntas y revelar (pasos 5 a 12)
// ---------------------------------------------------------------------------

@Composable
private fun ColumnScope.PantallaTablero(vm: DueloEnigmasViewModel, estado: EstadoDuelo) {
    val tablero by vm.tablero.collectAsState()
    val respuesta by vm.ultimaRespuesta.collectAsState()
    val t = tablero ?: return
    // Paso 11: modo revelar (elegir la tarjeta). Con el cuerno vacío o sin preguntas es el único modo.
    var revelando by rememberSaveable { mutableStateOf(false) }
    var elegida by rememberSaveable { mutableStateOf<String?>(null) }
    val soloRevelar = estado == EstadoDuelo.ULTIMA_ADIVINANZA || estado == EstadoDuelo.PREGUNTAS_AGOTADAS
    val modoRevelar = revelando || soloRevelar

    Cuerno(t.sorbos)
    Spacer(Modifier.height(10.dp))

    t.ultimoFallo?.let {
        Text(
            "No era «$it». El cuerno pierde 2 sorbos y la tarjeta se voltea.",
            color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().background(OdinPaleta.Incorrecto.copy(alpha = .15f)).border(1.dp, OdinPaleta.Incorrecto).padding(10.dp)
        )
        Spacer(Modifier.height(10.dp))
    }
    respuesta?.let { (pregunta, r) ->
        Column(
            Modifier.fillMaxWidth().background(Estilo.acento.copy(alpha = .10f)).border(1.dp, Estilo.acento.copy(alpha = .6f)).padding(10.dp)
        ) {
            Text(pregunta, color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontStyle = FontStyle.Italic, fontSize = 13.sp)
            Text("El gigante responde: ${if (r.esSi) "SÍ" else "NO"}", style = estiloTitulo(17.sp, color = Estilo.acento))
        }
        Spacer(Modifier.height(10.dp))
    }

    val posibles = t.tarjetas.count { !it.descartada }
    Text(
        if (modoRevelar) "Elige la tarjeta que crees verdadera" else "TABLERO · $posibles POSIBLES",
        style = estiloTitulo(if (modoRevelar) 17.sp else 13.sp, color = if (modoRevelar) OdinPaleta.Hueso else Estilo.metal),
        textAlign = TextAlign.Center
    )
    if (!modoRevelar) Text("Toca una tarjeta para descartarla o levantarla.", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))
    t.tarjetas.chunked(3).forEach { fila ->
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            fila.forEach { tarjeta ->
                val id = tarjeta.concepto.id
                Tarjeta(
                    nombre = tarjeta.concepto.nombre,
                    descartada = tarjeta.descartada,
                    elegida = modoRevelar && elegida == id,
                    modifier = Modifier.weight(1f)
                ) {
                    if (modoRevelar) { if (!tarjeta.descartada) elegida = id } else vm.voltearTarjeta(id)
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))

    if (modoRevelar) {
        val nombre = t.tarjetas.firstOrNull { it.concepto.id == elegida }?.concepto?.nombre
        PanelTallado(estilo = Estilo, modifier = Modifier.fillMaxWidth(), semilla = 11) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TU RESPUESTA", style = estiloTitulo(12.sp, color = Estilo.metal), maxLines = 1)
                Text(nombre ?: "Toca una tarjeta", style = estiloTitulo(24.sp, color = Estilo.acento), maxLines = 1)
                Text(
                    "Si aciertas recuperas ${PoliticaCosto.APUESTA_DUELO} y ganas ${PoliticaCosto.HIDROMIEL_POR_SORBO * t.sorbos}. " +
                        if (t.sorbos <= 2) "Si fallas, el cuerno queda vacío y pierdes el duelo." else "Si fallas, pierdes 2 sorbos.",
                    color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 14.sp, textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        BotonOdin(
            if (nombre != null) "Revelar «$nombre»" else "Revelar",
            { elegida?.let { vm.revelar(it); elegida = null; revelando = false } },
            Estilo.acento, Modifier.fillMaxWidth(), habilitado = nombre != null, metal = Estilo.metal
        )
        if (!soloRevelar) {
            Spacer(Modifier.height(10.dp))
            BotonOdin("Seguir preguntando", { revelando = false; elegida = null }, Estilo.acento, Modifier.fillMaxWidth(), primario = false, metal = Estilo.metal)
        } else {
            Text(
                if (estado == EstadoDuelo.ULTIMA_ADIVINANZA) "El cuerno está vacío: solo te queda una adivinanza." else "Ya no quedan preguntas: debes revelar.",
                color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp)
            )
        }
    } else {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("PREGÚNTALE AL GIGANTE", style = estiloTitulo(13.sp, color = Estilo.metal), maxLines = 1)
            Spacer(Modifier.weight(1f))
            Text("1 sorbo cada una", color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 12.sp, maxLines = 1)
        }
        Spacer(Modifier.height(8.dp))
        t.preguntas.forEach { p ->
            FilaPregunta(p.texto, p.tipo) { vm.preguntar(p.id) }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(6.dp))
        BotonOdin("Revelar", { revelando = true }, Estilo.acento, Modifier.fillMaxWidth(), metal = Estilo.metal)
    }
}

/** Cuerno con un recuadro por sorbo (llenos los que quedan). */
@Composable
private fun Cuerno(sorbos: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .border(1.dp, Estilo.metal.copy(alpha = .6f))
            .background(Color(0x990B0907))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Cuerno: quedan $sorbos de ${CuernoHidromiel.SORBOS_INICIALES} sorbos" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("CUERNO", style = estiloTitulo(11.sp, color = Estilo.metal), maxLines = 1)
            Text("$sorbos / ${CuernoHidromiel.SORBOS_INICIALES} sorbos", style = estiloTitulo(19.sp, color = Estilo.acento), maxLines = 1)
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(CuernoHidromiel.SORBOS_INICIALES) { i ->
                Box(
                    Modifier
                        .size(18.dp, 26.dp)
                        .background(if (i < sorbos) Estilo.acento else Color.Transparent, CutCornerShape(3.dp))
                        .border(1.5.dp, if (i < sorbos) Estilo.metal else OdinPaleta.Hierro, CutCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
private fun Tarjeta(nombre: String, descartada: Boolean, elegida: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val forma = CutCornerShape(7.dp)
    Box(
        modifier
            .heightIn(min = 52.dp)
            .background(
                when {
                    elegida -> Estilo.acento
                    descartada -> OdinPaleta.HollinAlto
                    else -> Pergamino
                },
                forma
            )
            .border(if (elegida) 2.5.dp else 1.5.dp, if (descartada) OdinPaleta.Hierro else Estilo.metal, forma)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { stateDescription = if (descartada) "descartada" else "posible" }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            nombre,
            color = when {
                elegida -> OdinPaleta.Hollin
                descartada -> OdinPaleta.HuesoTenue
                else -> TintaPergamino
            },
            fontFamily = FuenteTexto,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center,
            textDecoration = if (descartada) TextDecoration.LineThrough else null
        )
    }
}

@Composable
private fun FilaPregunta(texto: String, tipo: TipoPregunta, onClick: () -> Unit) {
    val colorTipo = if (tipo == TipoPregunta.ENIGMA) Estilo.acento else OdinPaleta.Escarcha
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .background(Color(0xF21E1A15))
            .border(1.dp, Estilo.metal.copy(alpha = .55f))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (tipo == TipoPregunta.ENIGMA) "ENIGMA" else "DIRECTA",
            style = estiloTitulo(10.sp, color = colorTipo),
            maxLines = 1,
            modifier = Modifier.border(1.dp, colorTipo).padding(horizontal = 6.dp, vertical = 3.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            texto, color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 14.sp, lineHeight = 18.sp,
            fontStyle = if (tipo == TipoPregunta.ENIGMA) FontStyle.Italic else FontStyle.Normal
        )
    }
}

// ---------------------------------------------------------------------------
// 4 y 5 · Resultado (paso 13 y E12.1)
// ---------------------------------------------------------------------------

/** Resultado del duelo: ganado (premio) o perdido (revela el concepto y avisa de la bitácora). */
@Composable
fun ColumnScope.ResultadoDueloScreen(cierre: CierreDuelo, onSalir: () -> Unit, onOtroDuelo: () -> Unit) {
    val r = cierre.resultado
    val gano = r.resultado == EstadoFinalDuelo.GANADO
    Spacer(Modifier.height(10.dp))
    SelloRunico(runa = if (gano) "ᛏ" else "ᚦ", estilo = Estilo, tamano = 72.dp, encendido = gano)
    Spacer(Modifier.height(8.dp))
    Text(if (gano) "EL GIGANTE SE RINDE" else "EL CUERNO QUEDÓ VACÍO", style = estiloTitulo(12.sp, color = Estilo.metal), maxLines = 1)
    Text(
        if (gano) "¡Venciste al gigante!" else "El gigante ganó",
        style = estiloTitulo(28.sp, brillo = if (gano) Estilo.acento else null), maxLines = 1
    )
    Spacer(Modifier.height(14.dp))
    Column(
        Modifier.fillMaxWidth().background(Pergamino, CutCornerShape(10.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("EL SABER ESCONDIDO", style = estiloTitulo(11.sp, color = OdinPaleta.TintaRoja).copy(shadow = null), maxLines = 1)
        Text(cierre.conceptoVerdadero.nombre, style = estiloTitulo(24.sp, color = TintaPergamino).copy(shadow = null), maxLines = 1)
        Text(cierre.conceptoVerdadero.explicacion, color = TintaPergamino, fontFamily = FuenteTexto, fontSize = 15.sp, lineHeight = 20.sp)
    }
    Spacer(Modifier.height(12.dp))
    if (!gano) {
        Text(
            "Este concepto se guardó en tu bitácora de errores para que lo repases.",
            color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().border(1.dp, OdinPaleta.Escarcha.copy(alpha = .6f)).padding(12.dp)
        )
        Spacer(Modifier.height(12.dp))
    }
    PanelTallado(estilo = Estilo, modifier = Modifier.fillMaxWidth(), semilla = 17) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (gano) {
                FilaResumen("Apuesta recuperada", "${PoliticaCosto.APUESTA_DUELO}")
                FilaResumen("Sorbos que te sobraron", "${r.sorbosSobrantes} × ${PoliticaCosto.HIDROMIEL_POR_SORBO} = ${r.sorbosSobrantes * PoliticaCosto.HIDROMIEL_POR_SORBO}")
            }
            FilaResumen("Preguntas hechas", "${r.preguntasRealizadas}")
            FilaResumen("Adivinanzas fallidas", "${r.adivinanzasFallidas}")
            DivisorOrnamental(Estilo.metal, Estilo.acento)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (gano) "PREMIO DEL DUELO" else "APUESTA PERDIDA", style = estiloTitulo(13.sp, color = Estilo.metalClaro), maxLines = 1)
                Spacer(Modifier.weight(1f))
                Text(
                    if (gano) "+${r.hidromielObtenido}" else "−${PoliticaCosto.APUESTA_DUELO}",
                    style = estiloTitulo(24.sp, color = if (gano) Estilo.acento else OdinPaleta.Incorrecto), maxLines = 1
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    BotonOdin("Volver a Mundos", onSalir, Estilo.acento, Modifier.fillMaxWidth(), metal = Estilo.metal)
    Spacer(Modifier.height(10.dp))
    BotonOdin("Otro duelo", onOtroDuelo, Estilo.acento, Modifier.fillMaxWidth(), primario = false, metal = Estilo.metal)
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(etiqueta, color = OdinPaleta.HuesoTenue, fontFamily = FuenteTexto, fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        Text(valor, style = estiloTitulo(15.sp), maxLines = 1)
    }
}
