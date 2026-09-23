package com.aesir.odin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aesir.odin.di.AppContainer
import com.aesir.odin.ui.navigation.OdinNavGraph
import com.aesir.odin.ui.theme.ColorNocheOscura
import com.aesir.odin.ui.theme.ODINTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ODINTheme {
                val appContainer = remember { AppContainer(applicationContext) }
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize(), containerColor = ColorNocheOscura) { innerPadding ->
                    OdinNavGraph.construirGrafo(
                        nav = navController,
                        appContainer = appContainer,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
