package com.example.model

import androidx.compose.ui.geometry.Offset

enum class PlateType {
    WOOD,
    METAL,
    GOLD_STEEL
}

enum class PlateShape {
    RECTANGLE,
    HORIZONTAL_BAR,
    VERTICAL_BAR,
    DIAGONAL_BAR_RIGHT,
    DIAGONAL_BAR_LEFT,
    SQUARE,
    ROUNDED_BOX,
    CIRCLE
}

data class ScrewHole(
    val id: Int,
    val x: Float,
    val y: Float,
    val currentScrewId: Int? = null
)

data class Plate(
    val id: Int,
    val type: PlateType,
    val shape: PlateShape,
    val x: Float, // Center X in virtual coords (0..400)
    val y: Float, // Center Y in virtual coords (0..500)
    val width: Float,
    val height: Float,
    val rotationDeg: Float = 0f,
    val zIndex: Int = 0,
    val holeIds: List<Int> = emptyList(),
    // Controlled physics falling state
    val isFalling: Boolean = false,
    val fallOffsetY: Float = 0f,
    val fallVelocityY: Float = 0f,
    val fallRotationDeg: Float = 0f,
    val fallAngularVelocity: Float = 0f,
    val fallAlpha: Float = 1f,
    val isCleared: Boolean = false
)

enum class ScrewState {
    IN_HOLE,
    SELECTED,
    UNSCREWING,
    FLYING_TO_TRAY,
    IN_TRAY,
    MATCHING,
    MATCHED,
    REMOVED
}

data class Screw(
    val id: Int,
    val color: ScrewColor,
    val holeId: Int?,
    val state: ScrewState = ScrewState.IN_HOLE,
    val rotationAngle: Float = 0f,
    val liftHeight: Float = 0f,
    val currentPosition: Offset = Offset.Zero
)

data class TraySlot(
    val index: Int,
    val screwId: Int? = null,
    val color: ScrewColor? = null,
    val isMatching: Boolean = false,
    val matchProgress: Float = 0f // 0f to 1f
)

data class FloatingScoreText(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    val color: androidx.compose.ui.graphics.Color,
    val progress: Float = 0f // 0f to 1f
)

data class LevelData(
    val levelNumber: Int,
    val title: String,
    val plates: List<Plate>,
    val holes: List<ScrewHole>,
    val screws: List<Screw>,
    val maxTraySlots: Int = 5,
    val description: String = ""
)
