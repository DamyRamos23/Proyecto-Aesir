package com.aesir.odin.ui.ajustes

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.LaunchedEffect
import com.aesir.odin.data.notificaciones.NotificadorCuervos
import com.aesir.odin.domain.notificaciones.PreferenciasCuervos
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import kotlin.math.roundToInt
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesir.odin.ui.components.odin.BotonVolver
import com.aesir.odin.ui.components.odin.FondoMundo
import com.aesir.odin.ui.components.odin.estiloTitulo
import com.aesir.odin.ui.theme.FuenteTexto
import com.aesir.odin.ui.theme.FuenteTitulo
import com.aesir.odin.ui.theme.OdinPaleta
import com.aesir.odin.ui.theme.Text

/** Pantalla de Ajustes: aquí se cambian las preferencias de MOD-02, MOD-03 y MOD-04. */
@Composable
fun AjustesScreen(vm: AjustesViewModel, onVolver: () -> Unit) {
    val presentacion by vm.presentacion.collectAsState()
    val sonido by vm.sonido.collectAsState()
    val cuervos by vm.cuervos.collectAsState()
    var mostrarInfo by remember { mutableStateOf(false) }
    if (mostrarInfo) DialogoInformacion(onCerrar = { mostrarInfo = false })

    FondoMundo(estilo = null) {
      Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            BotonVolver("Mundos", onVolver)
            Spacer(Modifier.height(8.dp))
            Text("AJUSTES", style = estiloTitulo(32.sp))
            Spacer(Modifier.height(24.dp))

            // MOD-02 Efectos de sonido
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = sonido.activado,
                        role = Role.Switch,
                        onValueChange = vm::cambiarSonidoActivado
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Efectos de sonido", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Aciertos, errores, tiempo, hidromiel y los eventos del Valhalla. " +
                            "Todo sonido tiene también su señal en pantalla.",
                        color = OdinPaleta.HuesoTenue,
                        fontFamily = FuenteTexto,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = sonido.activado,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(checkedTrackColor = OdinPaleta.Oro, checkedThumbColor = OdinPaleta.Hollin)
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Volumen",
                    color = if (sonido.activado) OdinPaleta.Hueso else OdinPaleta.HuesoTenue,
                    fontFamily = FuenteTexto,
                    fontSize = 15.sp,
                    modifier = Modifier.width(76.dp)
                )
                Slider(
                    value = sonido.volumen,
                    onValueChange = vm::cambiarVolumen,
                    onValueChangeFinished = vm::probarVolumen,
                    enabled = sonido.activado,
                    colors = SliderDefaults.colors(thumbColor = OdinPaleta.Oro, activeTrackColor = OdinPaleta.Oro),
                    modifier = Modifier
                        .weight(1f)
                        .semantics { stateDescription = "${(sonido.volumen * 100).roundToInt()} %" }
                )
                Text(
                    "${(sonido.volumen * 100).roundToInt()} %",
                    color = OdinPaleta.HuesoTenue,
                    fontFamily = FuenteTexto,
                    fontSize = 14.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(52.dp)
                )
            }

            // Música de fondo (tema principal)
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = sonido.musica,
                        role = Role.Switch,
                        onValueChange = vm::cambiarMusicaActivada
                    )
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Música", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "El tema de ODÍN de fondo: lira, tambores y cuernos del norte.",
                        color = OdinPaleta.HuesoTenue,
                        fontFamily = FuenteTexto,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = sonido.musica,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(checkedTrackColor = OdinPaleta.Oro, checkedThumbColor = OdinPaleta.Hollin)
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Volumen",
                    color = if (sonido.musica) OdinPaleta.Hueso else OdinPaleta.HuesoTenue,
                    fontFamily = FuenteTexto,
                    fontSize = 15.sp,
                    modifier = Modifier.width(76.dp)
                )
                Slider(
                    value = sonido.volumenMusica,
                    onValueChange = vm::cambiarVolumenMusica,
                    enabled = sonido.musica,
                    colors = SliderDefaults.colors(thumbColor = OdinPaleta.Oro, activeTrackColor = OdinPaleta.Oro),
                    modifier = Modifier
                        .weight(1f)
                        .semantics { stateDescription = "${(sonido.volumenMusica * 100).roundToInt()} %" }
                )
                Text(
                    "${(sonido.volumenMusica * 100).roundToInt()} %",
                    color = OdinPaleta.HuesoTenue,
                    fontFamily = FuenteTexto,
                    fontSize = 14.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(52.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            // MOD-03 Visión de Heimdall
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = presentacion.activa,
                        role = Role.Switch,
                        onValueChange = vm::cambiarVisionHeimdall
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Visión de Heimdall", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Modo de lectura: letra Lexend más grande y más espacio entre letras y líneas. " +
                            "No cambia tiempos, puntajes ni hidromiel.",
                        color = OdinPaleta.HuesoTenue,
                        fontFamily = FuenteTexto,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                // El Row ya es el control accesible; el Switch solo lo muestra.
                Switch(
                    checked = presentacion.activa,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(checkedTrackColor = OdinPaleta.Oro, checkedThumbColor = OdinPaleta.Hollin)
                )
            }

            Spacer(Modifier.height(16.dp))
            // MOD-04 Cuervos de Odín
            SeccionCuervos(
                preferencias = cuervos,
                onActivar = vm::cambiarCuervosActivados,
                onCambiarHora = vm::cambiarHoraCuervos
            )

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { mostrarInfo = true }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = OdinPaleta.Oro, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Información de la aplicación", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontSize = 18.sp)
            }
        }
        Text(
            "Versión ${versionApp()}",
            color = OdinPaleta.HuesoTenue,
            fontFamily = FuenteTexto,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
      }
    }
}

/**
 * MOD-04: activar los avisos, elegir la hora del recordatorio y, si el
 * teléfono no da permiso (E1), ir a su configuración para activarlos.
 */
@Composable
private fun SeccionCuervos(
    preferencias: PreferenciasCuervos,
    onActivar: (Boolean) -> Unit,
    onCambiarHora: (Int, Int) -> Unit
) {
    val contexto = LocalContext.current
    var permiso by remember { mutableStateOf(NotificadorCuervos.tienePermiso(contexto)) }
    // El alumno puede volver de la configuración del sistema con el permiso ya concedido
    LaunchedEffect(Unit) {
        while (true) {
            permiso = NotificadorCuervos.tienePermiso(contexto)
            delay(1000)
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = preferencias.activadas, role = Role.Switch, onValueChange = onActivar)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Cuervos de Odín", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Huginn y Muninn te avisan si tu racha está en riesgo, si tu hidromiel del día te espera " +
                    "o si llevas días sin entrar. Como máximo un aviso al día.",
                color = OdinPaleta.HuesoTenue,
                fontFamily = FuenteTexto,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = preferencias.activadas,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedTrackColor = OdinPaleta.Oro, checkedThumbColor = OdinPaleta.Hollin)
        )
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = preferencias.activadas, role = Role.Button) {
                TimePickerDialog(contexto, { _, hora, minuto -> onCambiarHora(hora, minuto) }, preferencias.hora, preferencias.minuto, true).show()
            }
            .padding(top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Hora del recordatorio",
            color = if (preferencias.activadas) OdinPaleta.Hueso else OdinPaleta.HuesoTenue,
            fontFamily = FuenteTexto,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            preferencias.textoHora,
            color = if (preferencias.activadas) OdinPaleta.Oro else OdinPaleta.HuesoTenue,
            fontFamily = FuenteTitulo,
            fontSize = 16.sp
        )
    }
    if (preferencias.activadas && !permiso) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { abrirConfiguracionNotificaciones(contexto) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "El teléfono tiene bloqueadas las notificaciones de ODÍN. Toca aquí para activarlas en la configuración del sistema.",
                color = OdinPaleta.Escarcha,
                fontFamily = FuenteTexto,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

/** MOD-04, E1: lleva a la configuración de notificaciones de la app en el sistema operativo. */
private fun abrirConfiguracionNotificaciones(contexto: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", contexto.packageName, null))
    }
    runCatching { contexto.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

private val INTEGRANTES = listOf(
    "Daniel Yaaziel Ramos Aguilera",
    "André Karim Quiñonez Juárez",
    "Flavio Uriel Huizar González",
    "Ximena Rodríguez Espitia"
)

@Composable
private fun DialogoInformacion(onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = OdinPaleta.HollinAlto,
        title = { Text("Información de la aplicación", color = OdinPaleta.Hueso, fontFamily = FuenteTitulo) },
        text = {
            Column {
                Text(
                    "ODÍN fue desarrollado por el equipo Æsir, compuesto por:",
                    color = OdinPaleta.Hueso,
                    fontFamily = FuenteTexto,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(8.dp))
                INTEGRANTES.forEach {
                    Text("•  $it", color = OdinPaleta.Hueso, fontFamily = FuenteTexto, fontSize = 15.sp, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar", color = OdinPaleta.Oro, fontFamily = FuenteTitulo) }
        }
    )
}

/** versionName del APK (build.gradle.kts), sin depender de BuildConfig. */
@Composable
private fun versionApp(): String {
    val contexto = LocalContext.current
    return remember {
        runCatching { contexto.packageManager.getPackageInfo(contexto.packageName, 0).versionName }.getOrNull().orEmpty()
    }
}
