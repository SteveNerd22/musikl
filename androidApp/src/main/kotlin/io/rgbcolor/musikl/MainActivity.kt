package io.rgbcolor.musikl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MobileAppShell()
        }
    }
}

enum class Screen { OPZIONI, RICERCA, PLAYLIST }

@Composable
fun MobileAppShell() {
    val viewModel = remember { createSearchViewModel() }
    var currentScreen by remember { mutableStateOf(Screen.RICERCA) }

    Scaffold(
        bottomBar = {
            MyBottomNavigation(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.RICERCA -> {
                    SearchMainScreen(viewModel)
                }
                Screen.OPZIONI -> { /* Qui metterai la UI delle opzioni */ }
                Screen.PLAYLIST -> { /* Qui metterai la UI delle playlist */ }
            }
        }
    }
}