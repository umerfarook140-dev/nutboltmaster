package com.example.game

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.data.LevelGenerator
import com.example.model.FloatingScoreText
import com.example.model.LevelData
import com.example.model.Plate
import com.example.model.Screw
import com.example.model.ScrewColor
import com.example.model.ScrewHole
import com.example.model.ScrewState
import com.example.model.TraySlot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveFlyingScrew(
    val screwId: Int,
    val color: ScrewColor,
    val startPos: Offset,
    val targetPos: Offset,
    val targetSlotIndex: Int,
    val progress: Float = 0f, // 0f to 1f
    val rotationAngle: Float = 0f,
    val liftHeight: Float = 0f
)

data class GameUiState(
    val levelData: LevelData? = null,
    val currentLevelNumber: Int = 1,
    val score: Int = 0,
    val coins: Int = 100,
    val movesCount: Int = 0,
    val traySlots: List<TraySlot> = List(5) { TraySlot(it) },
    val activePlates: List<Plate> = emptyList(),
    val holes: List<ScrewHole> = emptyList(),
    val screws: List<Screw> = emptyList(),
    val flyingScrew: ActiveFlyingScrew? = null,
    val floatingTexts: List<FloatingScoreText> = emptyList(),
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val isLevelComplete: Boolean = false,
    val starsEarned: Int = 0,
    val isAnimating: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = GamePreferences(application)
    val soundManager = SoundManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var physicsJob: Job? = null
    private var nextTextId = 1L

    init {
        soundManager.soundEnabled = prefs.soundEnabled
        soundManager.hapticsEnabled = prefs.hapticsEnabled
        loadLevel(prefs.currentLevel)
        startPhysicsLoop()
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.soundEnabled = enabled
        soundManager.soundEnabled = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.hapticsEnabled = enabled
        soundManager.hapticsEnabled = enabled
    }

    fun loadLevel(levelNum: Int) {
        val level = levelNum.coerceIn(1, 100)
        prefs.currentLevel = level
        val data = LevelGenerator.generateLevel(level)

        val slots = List(data.maxTraySlots) { TraySlot(it) }

        _uiState.update {
            it.copy(
                levelData = data,
                currentLevelNumber = level,
                coins = prefs.totalCoins,
                score = 0,
                movesCount = 0,
                traySlots = slots,
                activePlates = data.plates,
                holes = data.holes,
                screws = data.screws,
                flyingScrew = null,
                floatingTexts = emptyList(),
                isPaused = false,
                isGameOver = false,
                isLevelComplete = false,
                starsEarned = 0,
                isAnimating = false
            )
        }
    }

    fun restartLevel() {
        soundManager.playClick()
        loadLevel(_uiState.value.currentLevelNumber)
    }

    fun nextLevel() {
        soundManager.playClick()
        val next = (_uiState.value.currentLevelNumber + 1).coerceAtMost(100)
        loadLevel(next)
    }

    fun togglePause() {
        soundManager.playClick()
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun resumeGame() {
        soundManager.playClick()
        _uiState.update { it.copy(isPaused = false) }
    }

    /**
     * Core Player Tap Handling
     * When player taps a screw:
     * 1. Highlight selected screw
     * 2. Prevent double tapping
     * 3. Find first empty slot in tray
     * 4. Animate unscrew and flying to tray
     * 5. Check plates for falling
     * 6. Check tray for match-3
     */
    fun onScrewTapped(screwId: Int, traySlotPositions: List<Offset>) {
        val state = _uiState.value
        if (state.isPaused || state.isGameOver || state.isLevelComplete || state.isAnimating) {
            return
        }

        val screw = state.screws.find { it.id == screwId && it.state == ScrewState.IN_HOLE } ?: return

        // Find first empty tray slot
        val emptySlotIndex = state.traySlots.indexOfFirst { it.screwId == null }
        if (emptySlotIndex == -1) {
            // Tray is full, cannot remove screw
            soundManager.playClick()
            return
        }

        // Target position in tray
        val targetPos = if (emptySlotIndex < traySlotPositions.size) {
            traySlotPositions[emptySlotIndex]
        } else {
            Offset(200f, 480f)
        }

        // Concurrency lock
        _uiState.update { it.copy(isAnimating = true, movesCount = it.movesCount + 1) }
        soundManager.playUnscrew()

        // Highlight and launch animation coroutine
        viewModelScope.launch {
            val startPos = screw.currentPosition

            // Step 1: Unscrew rotation & lift out of hole
            val unscrewDuration = 220L
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                val p = (elapsed.toFloat() / unscrewDuration).coerceIn(0f, 1f)
                val rot = p * 720f
                val lift = p * 25f

                _uiState.update { cur ->
                    cur.copy(
                        screws = cur.screws.map {
                            if (it.id == screwId) it.copy(
                                state = ScrewState.UNSCREWING,
                                rotationAngle = rot,
                                liftHeight = lift
                            ) else it
                        }
                    )
                }
                if (p >= 1f) break
                delay(16)
            }

            // Step 2: Smooth flight from hole to tray slot
            val flightDuration = 280L
            val flightStart = System.currentTimeMillis()
            _uiState.update { cur ->
                cur.copy(
                    flyingScrew = ActiveFlyingScrew(
                        screwId = screw.id,
                        color = screw.color,
                        startPos = startPos,
                        targetPos = targetPos,
                        targetSlotIndex = emptySlotIndex,
                        progress = 0f,
                        rotationAngle = 720f,
                        liftHeight = 25f
                    ),
                    screws = cur.screws.map {
                        if (it.id == screwId) it.copy(state = ScrewState.FLYING_TO_TRAY) else it
                    }
                )
            }

            while (true) {
                val elapsed = System.currentTimeMillis() - flightStart
                val p = (elapsed.toFloat() / flightDuration).coerceIn(0f, 1f)
                val currentRot = 720f + p * 360f

                _uiState.update { cur ->
                    val fly = cur.flyingScrew?.copy(progress = p, rotationAngle = currentRot)
                    cur.copy(flyingScrew = fly)
                }
                if (p >= 1f) break
                delay(16)
            }

            // Step 3: Screw settles into tray slot
            soundManager.playTrayDrop()
            addFloatingText("+10", targetPos.x, targetPos.y - 30f, Color(0xFFFBBF24))

            _uiState.update { cur ->
                val updatedSlots = cur.traySlots.map { slot ->
                    if (slot.index == emptySlotIndex) {
                        slot.copy(screwId = screw.id, color = screw.color)
                    } else slot
                }
                val updatedScrews = cur.screws.map {
                    if (it.id == screwId) it.copy(state = ScrewState.IN_TRAY, holeId = null) else it
                }
                cur.copy(
                    flyingScrew = null,
                    traySlots = updatedSlots,
                    screws = updatedScrews,
                    score = cur.score + 10,
                    isAnimating = false
                )
            }

            // Check plates: Has any plate lost all its screws?
            checkPlatesFalling()

            // Check match 3 in tray
            checkMatch3()
        }
    }

    /**
     * Checks if any wooden/metal plates have zero remaining screws attached.
     * Initiates stable downward falling physics.
     */
    private fun checkPlatesFalling() {
        val state = _uiState.value
        // Active screws currently in a hole
        val fastenedHoleIds = state.screws
            .filter { it.state == ScrewState.IN_HOLE && it.holeId != null }
            .mapNotNull { it.holeId }
            .toSet()

        var droppedAny = false

        val updatedPlates = state.activePlates.map { plate ->
            if (!plate.isFalling && !plate.isCleared) {
                val remainingScrewsCount = plate.holeIds.count { it in fastenedHoleIds }
                if (remainingScrewsCount == 0) {
                    droppedAny = true
                    // Plate has no screws holding it: Initiate downward fall
                    plate.copy(
                        isFalling = true,
                        fallVelocityY = 120f,
                        fallAngularVelocity = if (plate.id % 2 == 0) 35f else -35f
                    )
                } else {
                    plate
                }
            } else {
                plate
            }
        }

        if (droppedAny) {
            soundManager.playPlateFall()
            _uiState.update { it.copy(activePlates = updatedPlates) }
        }
    }

    /**
     * Checks if 3 screws of the same color are in the tray.
     * Automatically matches and clears them with animation!
     */
    private fun checkMatch3() {
        val state = _uiState.value
        val filledSlots = state.traySlots.filter { it.color != null }

        // Group by color
        val colorGroups = filledSlots.groupBy { it.color!! }
        val matchingColor = colorGroups.entries.find { it.value.size >= 3 }?.key

        if (matchingColor != null) {
            val matchingIndices = filledSlots
                .filter { it.color == matchingColor }
                .take(3)
                .map { it.index }

            viewModelScope.launch {
                _uiState.update { it.copy(isAnimating = true) }

                // Mark slots as matching
                _uiState.update { cur ->
                    val updatedSlots = cur.traySlots.map { slot ->
                        if (slot.index in matchingIndices) slot.copy(isMatching = true) else slot
                    }
                    cur.copy(traySlots = updatedSlots)
                }

                soundManager.playMatch()

                // Small satisfying converge & burst animation
                val matchAnimDuration = 320L
                val startTime = System.currentTimeMillis()
                while (true) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val p = (elapsed.toFloat() / matchAnimDuration).coerceIn(0f, 1f)
                    _uiState.update { cur ->
                        val updatedSlots = cur.traySlots.map { slot ->
                            if (slot.index in matchingIndices) slot.copy(matchProgress = p) else slot
                        }
                        cur.copy(traySlots = updatedSlots)
                    }
                    if (p >= 1f) break
                    delay(16)
                }

                // Clear slots and update score
                addFloatingText("+100 MATCH!", 200f, 440f, Color(0xFF10B981))

                _uiState.update { cur ->
                    val updatedSlots = cur.traySlots.map { slot ->
                        if (slot.index in matchingIndices) {
                            TraySlot(index = slot.index)
                        } else slot
                    }
                    val matchedScrewIds = cur.traySlots.filter { it.index in matchingIndices }.mapNotNull { it.screwId }
                    val updatedScrews = cur.screws.map {
                        if (it.id in matchedScrewIds) it.copy(state = ScrewState.MATCHED) else it
                    }
                    cur.copy(
                        traySlots = updatedSlots,
                        screws = updatedScrews,
                        score = cur.score + 100,
                        isAnimating = false
                    )
                }

                // Check win condition
                checkWinOrLoss()
            }
        } else {
            // Check if tray is full with no possible moves
            val emptySlots = state.traySlots.count { it.screwId == null }
            if (emptySlots == 0) {
                // All 5 slots full and no 3 of the same color
                _uiState.update { it.copy(isGameOver = true) }
                soundManager.playGameOver()
            } else {
                checkWinOrLoss()
            }
        }
    }

    /**
     * Check if player cleared all plates or has remaining moves.
     */
    private fun checkWinOrLoss() {
        val state = _uiState.value
        if (state.isLevelComplete || state.isGameOver) return

        // Level is won when all plates are cleared (or all screws cleared/matched)
        val allPlatesCleared = state.activePlates.all { it.isCleared || (it.isFalling && it.fallAlpha <= 0.05f) }
        val remainingScrewsInBoard = state.screws.count { it.state == ScrewState.IN_HOLE }

        if (allPlatesCleared || remainingScrewsInBoard == 0) {
            triggerWin()
        }
    }

    private fun triggerWin() {
        val state = _uiState.value
        if (state.isLevelComplete) return

        soundManager.playWin()

        // Stars calculation (3 stars for high efficiency)
        val stars = when {
            state.movesCount <= (state.screws.size + 1) -> 3
            state.movesCount <= (state.screws.size + 4) -> 2
            else -> 1
        }

        val levelBonus = 500 + (stars * 100)
        val coinsEarned = 50

        prefs.addCoins(coinsEarned)
        prefs.saveStarsForLevel(state.currentLevelNumber, stars)
        prefs.saveHighScoreForLevel(state.currentLevelNumber, state.score + levelBonus)
        prefs.unlockNextLevel(state.currentLevelNumber)

        _uiState.update {
            it.copy(
                isLevelComplete = true,
                starsEarned = stars,
                score = it.score + levelBonus,
                coins = prefs.totalCoins
            )
        }
    }

    private fun addFloatingText(text: String, x: Float, y: Float, color: Color) {
        val item = FloatingScoreText(
            id = nextTextId++,
            text = text,
            x = x,
            y = y,
            color = color,
            progress = 0f
        )
        _uiState.update { it.copy(floatingTexts = it.floatingTexts + item) }
    }

    /**
     * Physics simulation loop:
     * - Downward gravity acceleration for falling plates
     * - Slight rotation
     * - Fade out when falling past board
     * - Floating score text updates
     */
    private fun startPhysicsLoop() {
        physicsJob?.cancel()
        physicsJob = viewModelScope.launch {
            val dt = 0.016f // 60 FPS delta
            val gravity = 950f // pixels / sec^2

            while (true) {
                val state = _uiState.value

                if (!state.isPaused) {
                    var platesChanged = false
                    val updatedPlates = state.activePlates.map { plate ->
                        if (plate.isFalling && !plate.isCleared) {
                            platesChanged = true
                            val newVel = plate.fallVelocityY + gravity * dt
                            val newOffsetY = plate.fallOffsetY + newVel * dt
                            val newRot = plate.fallRotationDeg + plate.fallAngularVelocity * dt
                            val newAlpha = (1f - (newOffsetY / 400f)).coerceIn(0f, 1f)
                            val isCleared = newOffsetY > 450f || newAlpha <= 0f

                            if (isCleared && !plate.isCleared) {
                                addFloatingText("+150 PANEL!", plate.x, plate.y, Color(0xFF38BDF8))
                            }

                            plate.copy(
                                fallVelocityY = newVel,
                                fallOffsetY = newOffsetY,
                                fallRotationDeg = newRot,
                                fallAlpha = newAlpha,
                                isCleared = isCleared
                            )
                        } else {
                            plate
                        }
                    }

                    // Update floating texts
                    val updatedTexts = state.floatingTexts.mapNotNull { txt ->
                        val nextP = txt.progress + (dt * 1.5f)
                        if (nextP < 1f) {
                            txt.copy(progress = nextP, y = txt.y - (30f * dt))
                        } else null
                    }

                    if (platesChanged || state.floatingTexts.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                activePlates = updatedPlates,
                                floatingTexts = updatedTexts
                            )
                        }
                        if (platesChanged) {
                            checkWinOrLoss()
                        }
                    }
                }

                delay(16)
            }
        }
    }

    // --- Developer / Debug Methods ---

    fun debugSetLevel(lvl: Int) {
        prefs.unlockNextLevel(lvl - 1)
        loadLevel(lvl)
    }

    fun debugAddCoins(amount: Int = 500) {
        prefs.addCoins(amount)
        _uiState.update { it.copy(coins = prefs.totalCoins) }
    }

    fun debugClearCurrentLevel() {
        triggerWin()
    }

    fun debugResetAllProgress() {
        prefs.resetAllProgress()
        loadLevel(1)
    }

    override fun onCleared() {
        super.onCleared()
        physicsJob?.cancel()
    }
}
