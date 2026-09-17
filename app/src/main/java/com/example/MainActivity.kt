package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameViewModel
import com.example.ui.DebugDialog
import com.example.ui.GameScreen
import com.example.ui.LevelSelectScreen
import com.example.ui.MainMenuScreen
import com.example.ui.SettingsDialog
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    MAIN_MENU,
    LEVEL_SELECT,
    GAME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F172A)
                ) {
                    NutBoltMasterApp()
                }
            }
        }
    }
}

@Composable
fun NutBoltMasterApp(
    viewModel: GameViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(ScreenState.MAIN_MENU) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDebugDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Handle system back navigation
    BackHandler(enabled = currentScreen != ScreenState.MAIN_MENU) {
        when (currentScreen) {
            ScreenState.GAME -> {
                if (uiState.isPaused) {
                    viewModel.resumeGame()
                } else {
                    currentScreen = ScreenState.MAIN_MENU
                }
            }
            ScreenState.LEVEL_SELECT -> {
                currentScreen = ScreenState.MAIN_MENU
            }
            ScreenState.MAIN_MENU -> {}
        }
    }

    when (currentScreen) {
        ScreenState.MAIN_MENU -> {
            MainMenuScreen(
                prefs = viewModel.prefs,
                onPlayClicked = {
                    viewModel.loadLevel(viewModel.prefs.currentLevel)
                    currentScreen = ScreenState.GAME
                },
                onLevelsClicked = {
                    currentScreen = ScreenState.LEVEL_SELECT
                },
                onSettingsClicked = {
                    showSettingsDialog = true
                },
                onDebugClicked = {
                    showDebugDialog = true
                }
            )
        }

        ScreenState.LEVEL_SELECT -> {
            LevelSelectScreen(
                prefs = viewModel.prefs,
                onLevelSelected = { levelNum ->
                    viewModel.loadLevel(levelNum)
                    currentScreen = ScreenState.GAME
                },
                onBack = {
                    currentScreen = ScreenState.MAIN_MENU
                }
            )
        }

        ScreenState.GAME -> {
            GameScreen(
                viewModel = viewModel,
                onBackToMenu = {
                    currentScreen = ScreenState.MAIN_MENU
                }
            )
        }
    }

    // Global Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            soundEnabled = viewModel.prefs.soundEnabled,
            hapticsEnabled = viewModel.prefs.hapticsEnabled,
            onSoundToggled = { viewModel.setSoundEnabled(it) },
            onHapticsToggled = { viewModel.setHapticsEnabled(it) },
            onResetProgress = {
                viewModel.debugResetAllProgress()
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Global Debug Dialog
    if (showDebugDialog) {
        DebugDialog(
            currentLevel = viewModel.prefs.currentLevel,
            onSelectLevel = { lvl ->
                viewModel.debugSetLevel(lvl)
                currentScreen = ScreenState.GAME
            },
            onAddCoins = {
                viewModel.debugAddCoins(500)
            },
            onClearLevel = {
                viewModel.debugClearCurrentLevel()
            },
            onResetAll = {
                viewModel.debugResetAllProgress()
            },
            onDismiss = { showDebugDialog = false }
        )
    }
}
