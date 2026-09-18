package com.example.ui

import android.app.Activity
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import com.example.game.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()
    var showDebugDialog by remember { mutableStateOf(false) }
    var trayPositions by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF090D16), Color(0xFF131B2A), Color(0xFF0F172A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            GameTopBar(
                levelNum = uiState.currentLevelNumber,
                score = uiState.score,
                coins = uiState.coins,
                onBack = onBackToMenu,
                onRestart = { viewModel.restartLevel() },
                onPause = { viewModel.togglePause() },
                onDebug = { showDebugDialog = true }
            )

            // Hint / description chip
            uiState.levelData?.let { levelData ->
                if (levelData.description.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = levelData.description,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 3D Puzzle Board Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Board3DRenderer(
                    uiState = uiState,
                    onScrewTapped = { screwId, positions ->
                        viewModel.onScrewTapped(screwId, positions)
                    },
                    onTrayPositionsCalculated = { positions ->
                        trayPositions = positions
                    }
                )
            }

            // Rewarded Ad Boosters Bar (Watch Ad for Hint / Watch Ad for Extra Move)
            // Clean, non-intrusive action bar positioned below the board
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Booster 1: Watch Ad for Hint
                BoosterButton(
                    icon = Icons.Default.Lightbulb,
                    title = "Hint",
                    badge = "Ad",
                    tag = "booster_hint_button",
                    onClick = {
                        activity?.let { act ->
                            AdManager.showRewardedAd(
                                activity = act,
                                onUserEarnedReward = {
                                    viewModel.applyHintReward()
                                }
                            )
                        } ?: run {
                            viewModel.applyHintReward()
                        }
                    }
                )

                // Booster 2: Watch Ad for Extra Move / Free 2 Slots
                BoosterButton(
                    icon = Icons.Default.AutoAwesome,
                    title = "Extra Move",
                    badge = "Ad",
                    tag = "booster_extra_move_button",
                    onClick = {
                        activity?.let { act ->
                            AdManager.showRewardedAd(
                                activity = act,
                                onUserEarnedReward = {
                                    viewModel.applyExtraMoveReward()
                                }
                            )
                        } ?: run {
                            viewModel.applyExtraMoveReward()
                        }
                    }
                )
            }
        }

        // --- Dialogs ---

        // Pause Dialog
        if (uiState.isPaused) {
            PauseDialog(
                onResume = { viewModel.resumeGame() },
                onRestart = {
                    viewModel.resumeGame()
                    viewModel.restartLevel()
                },
                onMainMenu = onBackToMenu
            )
        }

        // Game Over (No More Moves) Dialog
        if (uiState.isGameOver) {
            GameOverDialog(
                score = uiState.score,
                onWatchAdToContinue = {
                    activity?.let { act ->
                        AdManager.showRewardedAd(
                            activity = act,
                            onUserEarnedReward = {
                                viewModel.applyContinueReward()
                            }
                        )
                    } ?: run {
                        viewModel.applyContinueReward()
                    }
                },
                onRestart = { viewModel.restartLevel() },
                onMainMenu = onBackToMenu
            )
        }

        // Level Complete Dialog
        if (uiState.isLevelComplete) {
            LevelCompleteDialog(
                level = uiState.currentLevelNumber,
                stars = uiState.starsEarned,
                score = uiState.score,
                coinsEarned = 50,
                onNextLevel = {
                    activity?.let { act ->
                        AdManager.onLevelCompleted(act) {
                            viewModel.nextLevel()
                        }
                    } ?: viewModel.nextLevel()
                },
                onReplay = { viewModel.restartLevel() },
                onMainMenu = onBackToMenu
            )
        }

        // Debug Dialog
        if (showDebugDialog) {
            DebugDialog(
                currentLevel = uiState.currentLevelNumber,
                onSelectLevel = { lvl -> viewModel.debugSetLevel(lvl) },
                onAddCoins = { viewModel.debugAddCoins(500) },
                onClearLevel = { viewModel.debugClearCurrentLevel() },
                onResetAll = { viewModel.debugResetAllProgress() },
                onDismiss = { showDebugDialog = false }
            )
        }
    }
}

@Composable
private fun BoosterButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badge: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .background(Color(0xFF10B981), RoundedCornerShape(6.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(
                text = badge,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GameTopBar(
    levelNum: Int,
    score: Int,
    coins: Int,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onPause: () -> Unit,
    onDebug: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFF1E293B), CircleShape)
                .border(1.dp, Color(0xFF334155), CircleShape)
                .testTag("game_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Level Title & Score in center
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "LEVEL $levelNum",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "SCORE: $score",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24)
            )
        }

        // Actions: Coins + Restart + Pause + Debug
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Coins badge
            Row(
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$coins",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Restart Button
            IconButton(
                onClick = onRestart,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .border(1.dp, Color(0xFF334155), CircleShape)
                    .testTag("game_restart_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Pause Button
            IconButton(
                onClick = onPause,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .border(1.dp, Color(0xFF334155), CircleShape)
                    .testTag("game_pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Debug Button
            IconButton(
                onClick = onDebug,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x33A855F7), CircleShape)
                    .border(1.dp, Color(0xFFA855F7), CircleShape)
                    .testTag("game_debug_button")
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "Debug",
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
