package com.aesir.odin.ui.components.odin

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.provider.Settings
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.Text
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Pantallas de inicio de los modos del Valhalla, tal como se aprobaron en el
 * diseño «ODÍN · Pantallas de inicio de los modos» (La Völva, El gigante,
 * Hasta el alba y La forja).
 *
 * El arte animado de cada modo vive en `assets/escenas/escena_<id>.html`
 * (SVG + animaciones CSS, generado con `tools/escenas.py` a partir del diseño)
 * y se pinta con un WebView decorativo; la interfaz va encima en Compose para
 * que respete la Visión de Heimdall, TalkBack y los tamaños de letra.
 */
enum class EscenaModo(val id: String, val descripcion: String) {
    VOLVA("volva", "La vidente con cuernos de ciervo y una venda negra sobre los ojos, ante la luna, junto a un caldero violeta y dos cuervos."),
    DUELO("duelo", "El gigante Vafþrúðnir en su trono de hielo, runas flotando frente a él y Odín encapuchado junto al fuego."),
    ALBA("alba", "Un banquete en el Valhalla: un vikingo alza su cuerno y por la puerta abierta se ve el amanecer sobre la batalla."),
    BROKKR("brokkr", "La forja de los enanos: Eitri martilla, Brokkr aviva el fuego y la mosca de Loki ronda en la sombra.")
}

/** Colores del diseño de las pantallas de inicio. */
object ColoresInicio {
    val Oro = Color(0xFFE3C27A)
    val OroTitulo = Color(0xFFECD08F)
    val OroBoton = Color(0xFFD4AD5C)
    val BordeBoton = Color(0xFFF3DCA3)
    val TintaBoton = Color(0xFF17110A)
    val Antetitulo = Color(0xFFC7B98F)
    val Lema = Color(0xFFD4CDE4)
    val Chip = Color(0xB806070D)
    val Fondo = Color(0xFF06070D)
    val Tenue = Color(0xFFC9BFA0)
}

/** Una píldora informativa bajo el lema («6 visiones · 15 s», «Duelos hoy 2 / 3»…). */
data class ChipInicio(val texto: String, val destacado: String? = null, val borde: Color = ColoresInicio.Oro.copy(alpha = .4f))

/**
 * Pantalla de inicio de un modo. Al pulsar el botón principal la escena se
 * «activa» (el caldero hierve, los cuervos huyen, el gigante acepta…) y,
 * cuando termina la animación, se llama a [onComenzar].
 */
@Composable
fun PantallaInicioModo(
    escena: EscenaModo,
    titulo: String,
    lema: String,
    saldo: Int?,
    chips: List<ChipInicio>,
    textoBoton: String,
    botonHabilitado: Boolean,
    onComenzar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    antetitulo: String = "MODO DEL VALHALLA",
    aviso: String? = null,
    comoSeJuega: (@Composable ColumnScope.() -> Unit)? = null,
    extra: (@Composable ColumnScope.() -> Unit)? = null
) {
    var activa by remember { mutableStateOf(false) }
    var mostrarReglas by remember { mutableStateOf(false) }
    val comenzar by rememberUpdatedState(onComenzar)

    LaunchedEffect(activa) {
        if (activa) {
            delay(DURACION_ACTIVACION_MS)
            comenzar()
            activa = false
        }
    }

    Box(modifier.fillMaxSize().background(ColoresInicio.Fondo)) {
        EscenaAnimada(escena, activa, Modifier.fillMaxSize())

        // Barra superior: volver y cuerno de hidromiel
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 22.dp, vertical = 12.dp).alpha(if (activa) 0f else 1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(Color(0x9906070D))
                    .border(1.dp, ColoresInicio.Oro.copy(alpha = .45f), CircleShape)
                    .clickable(role = Role.Button, onClick = onVolver)
                    .semantics { contentDescription = "Volver" },
                contentAlignment = Alignment.Center
            ) { Text("‹", color = ColoresInicio.Oro, fontSize = 28.sp, fontFamily = FuenteTitulo) }
            Spacer(Modifier.weight(1f))
            if (saldo != null) Row(
                Modifier.height(40.dp).clip(RoundedCornerShape(20.dp)).background(Color(0x9906070D))
                    .border(1.dp, ColoresInicio.Oro.copy(alpha = .45f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp)
                    .semantics(mergeDescendants = true) { contentDescription = "$saldo de hidromiel" },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$saldo", color = Color(0xFFF1DFB2), fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text("hidromiel", color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 14.sp)
            }
        }

        // Bloque inferior: título, lema, datos y botón
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp).widthIn(max = 520.dp)
                .alpha(if (activa) 0f else 1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(36.dp).height(1.dp).background(ColoresInicio.Oro.copy(alpha = .5f)))
                Text(antetitulo, color = ColoresInicio.Antetitulo, fontFamily = FuenteTitulo, fontSize = 11.sp, letterSpacing = 3.sp,
                    modifier = Modifier.padding(horizontal = 10.dp))
                Box(Modifier.width(36.dp).height(1.dp).background(ColoresInicio.Oro.copy(alpha = .5f)))
            }
            Text(titulo.uppercase(), color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold,
                fontSize = if (titulo.length > 10) 38.sp else 44.sp, letterSpacing = 4.sp, textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() })
            Text(lema, color = ColoresInicio.Lema, fontFamily = FuenteTexto, fontStyle = FontStyle.Italic, fontSize = 17.sp,
                lineHeight = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 340.dp))
            if (chips.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { chips.forEach { ChipDato(it) } }
            }
            extra?.invoke(this)
            if (aviso != null) {
                Text(aviso, color = Color(0xFFF2B8A0), fontFamily = FuenteTexto, fontSize = 15.sp, textAlign = TextAlign.Center)
            }
            BotonInicio(textoBoton, botonHabilitado && !activa) { activa = true }
            if (comoSeJuega != null) {
                Text(
                    "Cómo se juega", color = ColoresInicio.Tenue, fontFamily = FuenteTexto, fontSize = 17.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button) { mostrarReglas = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }

        if (mostrarReglas && comoSeJuega != null) {
            HojaComoSeJuega(titulo, onCerrar = { mostrarReglas = false }, contenido = comoSeJuega)
        }
    }
}

const val DURACION_ACTIVACION_MS = 1700L

@Composable
private fun ChipDato(chip: ChipInicio) {
    Row(
        Modifier.height(30.dp).clip(RoundedCornerShape(15.dp)).background(ColoresInicio.Chip)
            .border(1.dp, chip.borde, RoundedCornerShape(15.dp)).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(chip.texto, color = Color(0xFFDDD3F5), fontFamily = FuenteTexto, fontSize = 14.sp, maxLines = 1)
        if (chip.destacado != null) {
            Text(" ${chip.destacado}", color = ColoresInicio.OroTitulo, fontFamily = FuenteTexto, fontSize = 14.sp, maxLines = 1)
        }
    }
}

@Composable
private fun BotonInicio(texto: String, habilitado: Boolean, onClick: () -> Unit) {
    val forma = RoundedCornerShape(14.dp)
    Box(
        Modifier.fillMaxWidth().height(56.dp).alpha(if (habilitado) 1f else .5f).clip(forma)
            .background(ColoresInicio.OroBoton).border(BorderStroke(1.dp, ColoresInicio.BordeBoton), forma)
            .clickable(enabled = habilitado, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(texto.uppercase(), color = ColoresInicio.TintaBoton, fontFamily = FuenteTitulo, fontWeight = FontWeight.Bold,
            fontSize = 16.sp, letterSpacing = 1.5.sp, textAlign = TextAlign.Center)
    }
}

/** Hoja con las reglas del modo, sobre la escena oscurecida. */
@Composable
private fun HojaComoSeJuega(titulo: String, onCerrar: () -> Unit, contenido: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color(0xCC000000))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCerrar),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 560.dp).navigationBarsPadding()
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).background(Color(0xFF0E1018))
                .border(1.dp, ColoresInicio.Oro.copy(alpha = .35f), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                .verticalScroll(rememberScrollState()).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cómo se juega · $titulo", color = ColoresInicio.OroTitulo, fontFamily = FuenteTitulo, fontSize = 18.sp,
                    modifier = Modifier.weight(1f).semantics { heading() })
                Text("Cerrar", color = ColoresInicio.Oro, fontFamily = FuenteTexto, fontSize = 16.sp,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button, onClick = onCerrar).padding(8.dp))
            }
            contenido()
        }
    }
}

/** Una regla de la hoja «Cómo se juega». */
@Composable
fun ReglaInicio(runa: String, texto: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("$runa ", color = ColoresInicio.Oro, fontSize = 14.sp)
        Text(texto, color = Color(0xFFE9E2D0), fontFamily = FuenteTexto, fontSize = 15.sp, lineHeight = 20.sp)
    }
}

// ---------------------------------------------------------------------------
// Escena animada (WebView decorativo) con parallax por inclinación
// ---------------------------------------------------------------------------

@Composable
fun EscenaAnimada(escena: EscenaModo, activa: Boolean, modifier: Modifier = Modifier) {
    EscenaWeb(
        archivo = "escena_${escena.id}.html",
        descripcion = escena.descripcion,
        estado = if (activa) "odinActivar()" else "odinReposar()",
        modifier = modifier
    )
}

/** Un evento de una sola vez para la escena (un golpe, un escudo roto…); [id] distinto = se vuelve a disparar. */
data class PulsoEscena(val id: Int, val script: String)

/**
 * Escena animada de `assets/escenas/[archivo]` en un WebView decorativo.
 * [estado] es un script idempotente (p. ej. `odinEnemigo(3)`) que se vuelve a
 * aplicar al terminar de cargar la página; [pulso] se dispara una vez por id.
 */
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun EscenaWeb(
    archivo: String,
    descripcion: String,
    estado: String,
    modifier: Modifier = Modifier,
    pulso: PulsoEscena? = null
) {
    val contexto = LocalContext.current
    val control = remember(archivo) { ControlEscena() }
    val vista = remember(archivo) {
        WebView(contexto).apply {
            setBackgroundColor(android.graphics.Color.rgb(6, 7, 13))
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.setSupportZoom(false)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            isFocusable = false
            setOnTouchListener { _, _ -> true } // decorativa: no se desplaza ni se selecciona
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    control.cargada = true
                    control.estado?.let { view.evaluateJavascript(it, null) }
                }
            }
            loadUrl("file:///android_asset/escenas/$archivo")
        }
    }

    LaunchedEffect(estado) {
        control.estado = "try{$estado}catch(e){}"
        if (control.cargada) vista.evaluateJavascript(control.estado!!, null)
    }
    LaunchedEffect(pulso?.id) {
        val p = pulso ?: return@LaunchedEffect
        if (control.cargada) vista.evaluateJavascript("try{${p.script}}catch(e){}", null)
    }

    InclinacionDispositivo(contexto) { x, y ->
        vista.evaluateJavascript("window.odinInclinar&&odinInclinar(${"%.3f".format(java.util.Locale.US, x)},${"%.3f".format(java.util.Locale.US, y)})", null)
    }

    DisposableEffect(vista) {
        vista.onResume()
        onDispose { vista.stopLoading(); vista.destroy() }
    }

    AndroidView(
        factory = { vista },
        modifier = modifier.semantics { contentDescription = descripcion }
    )
}

private class ControlEscena {
    var cargada = false
    var estado: String? = null
}

/**
 * Inclinación del teléfono normalizada a −1…1 (como el `deviceorientation` del
 * diseño: ±25° de giro, 45° de inclinación como reposo). Se apaga si el
 * sistema tiene las animaciones desactivadas.
 */
@Composable
private fun InclinacionDispositivo(contexto: Context, onCambio: (Float, Float) -> Unit) {
    val callback by rememberUpdatedState(onCambio)
    DisposableEffect(contexto) {
        val animaciones = Settings.Global.getFloat(contexto.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        val sensores = contexto.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sensores?.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: sensores?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (animaciones == 0f || sensores == null || sensor == null) return@DisposableEffect onDispose { }
        val escala = (SensorManager.GRAVITY_EARTH * 0.42f)
        val reposoY = SensorManager.GRAVITY_EARTH * 0.707f
        var sx = 0f
        var sy = 0f
        val oyente = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val x = (-e.values[0] / escala).coerceIn(-1f, 1f)
                val y = ((e.values[1] - reposoY) / escala).coerceIn(-1f, 1f)
                val nx = sx + (x - sx) * 0.15f
                val ny = sy + (y - sy) * 0.15f
                if (abs(nx - sx) + abs(ny - sy) > 0.004f) { sx = nx; sy = ny; callback(sx, sy) }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) = Unit
        }
        sensores.registerListener(oyente, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensores.unregisterListener(oyente) }
    }
}
