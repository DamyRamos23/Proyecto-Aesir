package com.aesir.odin.ui.ajustes

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
