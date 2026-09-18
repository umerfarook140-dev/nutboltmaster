package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobBanner
import com.example.data.GamePreferences

@Composable
fun LevelSelectScreen(
    prefs: GamePreferences,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val highestUnlocked = prefs.highestUnlockedLevel
    val gridState = rememberLazyGridState()

    // Scroll automatically to current unlocked level
    LaunchedEffect(highestUnlocked) {
        val targetIndex = (highestUnlocked - 1).coerceIn(0, 99)
        gridState.scrollToItem(targetIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF090D16))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x33FFFFFF), CircleShape)
                    .testTag("level_select_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "SELECT LEVEL",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "100 Puzzles • Unlocked: $highestUnlocked/100",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // 100 Levels Grid (4 columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = gridState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("level_select_grid"),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(100) { index ->
                val levelNum = index + 1
                val isUnlocked = levelNum <= highestUnlocked
                val stars = prefs.getStarsForLevel(levelNum)
                val isCurrent = levelNum == highestUnlocked

                LevelItemCard(
                    levelNum = levelNum,
                    isUnlocked = isUnlocked,
                    isCurrent = isCurrent,
                    stars = stars,
                    onClick = {
                        if (isUnlocked) {
                            onLevelSelected(levelNum)
                        }
                    }
                )
            }
        }

        // Bottom Banner Ad (does not cover grid or interactive controls)
        AdMobBanner()
    }
}

@Composable
private fun LevelItemCard(
    levelNum: Int,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    val bgColors = when {
        isCurrent -> listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
        isUnlocked -> listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        else -> listOf(Color(0xFF18181B), Color(0xFF09090B))
    }

    val borderColor = when {
        isCurrent -> Color(0xFF60A5FA)
        isUnlocked && stars > 0 -> Color(0xFFF59E0B)
        isUnlocked -> Color(0xFF334155)
        else -> Color(0xFF27272A)
    }

    Box(
        modifier = Modifier
            .height(86.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(bgColors))
            .border(
                width = if (isCurrent) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_button_$levelNum"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isUnlocked) {
                Text(
                    text = "$levelNum",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isCurrent) Color.White else Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Stars row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (s in 1..3) {
                        val isStarLit = s <= stars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isStarLit) Color(0xFFFBBF24) else Color(0xFF334155),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF52525B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$levelNum",
                    fontSize = 12.sp,
                    color = Color(0xFF52525B)
                )
            }
        }
    }
}
