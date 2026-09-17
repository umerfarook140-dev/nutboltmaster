package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.game.ActiveFlyingScrew
import com.example.game.GameUiState
import com.example.model.FloatingScoreText
import com.example.model.Plate
import com.example.model.PlateShape
import com.example.model.PlateType
import com.example.model.Screw
import com.example.model.ScrewColor
import com.example.model.ScrewHole
import com.example.model.ScrewState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Board3DRenderer(
    uiState: GameUiState,
    onScrewTapped: (screwId: Int, trayPositions: List<Offset>) -> Unit,
    onTrayPositionsCalculated: (List<Offset>) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        // Virtual coordinates: 400 x 540
        // Board region occupies 400 x 420, Tray region is at y = 430..530
        val virtualW = 400f
        val virtualH = 540f
        val scaleFactor = (canvasWidth / virtualW).coerceAtMost(canvasHeight / virtualH)

        val offsetX = (canvasWidth - virtualW * scaleFactor) / 2f
        val offsetY = (canvasHeight - virtualH * scaleFactor) / 2f

        // Compute 5 tray slot virtual positions
        val traySlotVirtualPositions = remember {
            List(5) { i ->
                Offset(70f + i * 65f, 475f)
            }
        }

        // Inform ViewModel of tray slot positions in virtual coords
        onTrayPositionsCalculated(traySlotVirtualPositions)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("board_canvas")
                .pointerInput(uiState.screws, scaleFactor, offsetX, offsetY) {
                    detectTapGestures { tapOffset ->
                        // Map physical screen tap to virtual coordinates
                        val vx = (tapOffset.x - offsetX) / scaleFactor
                        val vy = (tapOffset.y - offsetY) / scaleFactor

                        // Find tapped screw (hit test radius 32f virtual pixels)
                        val tappedScrew = uiState.screws
                            .filter { it.state == ScrewState.IN_HOLE }
                            .minByOrNull {
                                val dx = it.currentPosition.x - vx
                                val dy = it.currentPosition.y - vy
                                dx * dx + dy * dy
                            }

                        if (tappedScrew != null) {
                            val distSq = (tappedScrew.currentPosition.x - vx) * (tappedScrew.currentPosition.x - vx) +
                                    (tappedScrew.currentPosition.y - vy) * (tappedScrew.currentPosition.y - vy)
                            if (distSq <= 36f * 36f) {
                                onScrewTapped(tappedScrew.id, traySlotVirtualPositions)
                            }
                        }
                    }
                }
        ) {
            // Apply scale and center translation
            translate(left = offsetX, top = offsetY) {
                scale(scale = scaleFactor, pivot = Offset.Zero) {
                    // 1. Draw Workbench 3D Background Panel
                    drawWorkbenchBackground()

                    // 2. Draw Screw Holes in background
                    for (hole in uiState.holes) {
                        drawScrewHole(hole.x, hole.y)
                    }

                    // 3. Draw Active & Falling Plates (sorted by zIndex)
                    val sortedPlates = uiState.activePlates.sortedBy { it.zIndex }
                    for (plate in sortedPlates) {
                        if (!plate.isCleared) {
                            drawPlate(plate, uiState.holes)
                        }
                    }

                    // 4. Draw Screws that are in holes
                    for (screw in uiState.screws) {
                        if (screw.state == ScrewState.IN_HOLE || screw.state == ScrewState.UNSCREWING) {
                            drawScrew(screw)
                        }
                    }

                    // 5. Draw Screw Tray at bottom
                    drawScrewTray(uiState, traySlotVirtualPositions)

                    // 6. Draw Active Flying Screw (in flight to tray)
                    uiState.flyingScrew?.let { flying ->
                        drawFlyingScrew(flying)
                    }

                    // 7. Draw Floating Score Texts
                    for (text in uiState.floatingTexts) {
                        drawFloatingText(text)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawWorkbenchBackground() {
    // Elegant workshop wooden frame / dark slate board
    val boardRect = Size(380f, 380f)
    val boardTopLeft = Offset(10f, 25f)

    // Outer drop shadow
    drawRoundRect(
        color = Color(0x55000000),
        topLeft = Offset(boardTopLeft.x + 4f, boardTopLeft.y + 8f),
        size = boardRect,
        cornerRadius = CornerRadius(24f, 24f)
    )

    // Background panel with soft radial gradient
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF2C3440), Color(0xFF1E232B), Color(0xFF13171D)),
            center = Offset(200f, 215f),
            radius = 260f
        ),
        topLeft = boardTopLeft,
        size = boardRect,
        cornerRadius = CornerRadius(24f, 24f)
    )

    // Inner subtle chamfer border
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0x44FFFFFF), Color(0x11FFFFFF), Color(0x33000000)),
            start = Offset(10f, 25f),
            end = Offset(390f, 405f)
        ),
        topLeft = boardTopLeft,
        size = boardRect,
        cornerRadius = CornerRadius(24f, 24f),
        style = Stroke(width = 2.5f)
    )

    // Subtle workshop grid dots for 3D depth
    val dotColor = Color(0x18FFFFFF)
    for (gx in 40..360 step 30) {
        for (gy in 55..375 step 30) {
            drawCircle(color = dotColor, radius = 1.5f, center = Offset(gx.toFloat(), gy.toFloat()))
        }
    }
}

private fun DrawScope.drawScrewHole(x: Float, y: Float) {
    // Deep cylindrical recess
    // Outer washer shadow
    drawCircle(
        color = Color(0x33000000),
        radius = 16f,
        center = Offset(x, y + 1.5f)
    )
    // Metallic washer ring
    drawCircle(
        brush = Brush.sweepGradient(
            listOf(Color(0xFF64748B), Color(0xFF94A3B8), Color(0xFF475569), Color(0xFF94A3B8), Color(0xFF64748B)),
            center = Offset(x, y)
        ),
        radius = 15f,
        center = Offset(x, y)
    )
    // Dark recess hole
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0A0C0E), Color(0xFF1E242B)),
            center = Offset(x - 2f, y - 2f),
            radius = 12f
        ),
        radius = 11f,
        center = Offset(x, y)
    )
    // Hole bottom shadow
    drawCircle(
        color = Color(0xFF08090A),
        radius = 9f,
        center = Offset(x, y)
    )
}

private fun DrawScope.drawPlate(plate: Plate, holes: List<ScrewHole>) {
    val plateX = plate.x
    val plateY = plate.y + plate.fallOffsetY
    val totalRotation = plate.rotationDeg + plate.fallRotationDeg
    val alpha = plate.fallAlpha

    rotate(degrees = totalRotation, pivot = Offset(plateX, plateY)) {
        val halfW = plate.width / 2f
        val halfH = plate.height / 2f
        val topLeft = Offset(plateX - halfW, plateY - halfH)
        val plateSize = Size(plate.width, plate.height)
        val cornerRadius = CornerRadius(18f, 18f)

        // 3D Drop Shadow underneath plate
        val shadowOffsetY = 6f + plate.zIndex * 3f + (plate.fallOffsetY * 0.08f)
        drawRoundRect(
            color = Color(0f, 0f, 0f, 0.45f * alpha),
            topLeft = Offset(topLeft.x, topLeft.y + shadowOffsetY),
            size = plateSize,
            cornerRadius = cornerRadius
        )

        when (plate.type) {
            PlateType.WOOD -> {
                // Rich Oak wood grain gradient
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFD49755).copy(alpha = alpha),
                            Color(0xFFA6692C).copy(alpha = alpha),
                            Color(0xFFBD8041).copy(alpha = alpha),
                            Color(0xFF8F541D).copy(alpha = alpha)
                        ),
                        start = Offset(topLeft.x, topLeft.y),
                        end = Offset(topLeft.x + plate.width, topLeft.y + plate.height)
                    ),
                    topLeft = topLeft,
                    size = plateSize,
                    cornerRadius = cornerRadius
                )

                // Wood grain lines
                val grainColor = Color(0.3f, 0.18f, 0.08f, 0.22f * alpha)
                val numGrains = 4
                for (g in 1..numGrains) {
                    val gy = topLeft.y + (plate.height * (g.toFloat() / (numGrains + 1)))
                    drawLine(
                        color = grainColor,
                        start = Offset(topLeft.x + 8f, gy),
                        end = Offset(topLeft.x + plate.width - 8f, gy),
                        strokeWidth = 2f
                    )
                }

                // 3D Bevel highlight & rim
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(1f, 0.9f, 0.7f, 0.45f * alpha),
                            Color(0.2f, 0.1f, 0.05f, 0.4f * alpha)
                        ),
                        start = Offset(topLeft.x, topLeft.y),
                        end = Offset(topLeft.x, topLeft.y + plate.height)
                    ),
                    topLeft = topLeft,
                    size = plateSize,
                    cornerRadius = cornerRadius,
                    style = Stroke(width = 2.5f)
                )
            }

            PlateType.METAL -> {
                // Brushed steel metallic gradient
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE2E8F0).copy(alpha = alpha),
                            Color(0xFF94A3B8).copy(alpha = alpha),
                            Color(0xFFCBD5E1).copy(alpha = alpha),
                            Color(0xFF64748B).copy(alpha = alpha)
                        ),
                        start = Offset(topLeft.x, topLeft.y),
                        end = Offset(topLeft.x + plate.width, topLeft.y + plate.height)
                    ),
                    topLeft = topLeft,
                    size = plateSize,
                    cornerRadius = cornerRadius
                )

                // Brushed metal diagonal reflection
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(1f, 1f, 1f, 0.25f * alpha),
                            Color(1f, 1f, 1f, 0.02f * alpha),
                            Color(0f, 0f, 0f, 0.2f * alpha)
                        ),
                        start = Offset(topLeft.x, topLeft.y),
                        end = Offset(topLeft.x + plate.width * 0.8f, topLeft.y + plate.height * 0.8f)
                    ),
                    topLeft = topLeft,
                    size = plateSize,
                    cornerRadius = cornerRadius
                )

                // Sleek steel bevel border
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(1f, 1f, 1f, 0.6f * alpha),
                            Color(0.2f, 0.25f, 0.3f, 0.5f * alpha)
                        ),
                        start = Offset(topLeft.x, topLeft.y),
                        end = Offset(topLeft.x, topLeft.y + plate.height)
                    ),
                    topLeft = topLeft,
                    size = plateSize,
                    cornerRadius = cornerRadius,
                    style = Stroke(width = 2f)
                )
            }

            PlateType.GOLD_STEEL -> {
                // Luxurious gold brass plate
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFDE047).copy(alpha = alpha),
                            Color(0xFFEAB308).copy(alpha = alpha),
                            Color(0xFFCA8A04).copy(alpha = alpha),
                            Color(0xFFFEF08A).copy(alpha = alpha)
                        ),
                        start = Offset(topLeft.x, topLeft.y),
                        end = Offset(topLeft.x + plate.width, topLeft.y + plate.height)
                    ),
                    topLeft = topLeft,
                    size = plateSize,
                    cornerRadius = cornerRadius
                )

                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(1f, 1f, 1f, 0.65f * alpha), Color(0.4f, 0.3f, 0.05f, 0.5f * alpha)),
                        start = Offset(topLeft.x, topLeft.y),
                        end = Offset(topLeft.x, topLeft.y + plate.height)
                    ),
                    topLeft = topLeft,
                    size = plateSize,
                    cornerRadius = cornerRadius,
                    style = Stroke(width = 2.5f)
                )
            }
        }

        // Draw screw hole punch-outs in plate
        val plateHoles = holes.filter { it.id in plate.holeIds }
        for (h in plateHoles) {
            // Transform hole from board space to local plate space
            // In unrotated relative space:
            val localX = h.x - plate.x
            val localY = h.y - plate.y
            val rad = Math.toRadians(-plate.rotationDeg.toDouble())
            val unrotX = (localX * cos(rad) - localY * sin(rad)).toFloat()
            val unrotY = (localX * sin(rad) + localY * cos(rad)).toFloat()

            val holeCenter = Offset(plateX + unrotX, plateY + unrotY)

            // Punch out rim in plate
            drawCircle(
                color = Color(0x33000000 * alpha.toLong()),
                radius = 14f,
                center = Offset(holeCenter.x, holeCenter.y + 1f)
            )
            drawCircle(
                color = Color(0xFF1E232B).copy(alpha = alpha),
                radius = 12f,
                center = holeCenter
            )
            drawCircle(
                color = if (plate.type == PlateType.WOOD) Color(0xFF452A12).copy(alpha = alpha) else Color(0xFF334155).copy(alpha = alpha),
                radius = 12f,
                center = holeCenter,
                style = Stroke(width = 1.5f)
            )
        }
    }
}

private fun DrawScope.drawScrew(screw: Screw) {
    val x = screw.currentPosition.x
    val y = screw.currentPosition.y
    val rot = screw.rotationAngle
    val lift = screw.liftHeight
    val scaleMultiplier = 1f + (lift * 0.012f)

    // Shadow cast when lifted
    val shadowOffset = 3f + lift * 0.6f
    val shadowAlpha = (0.4f - lift * 0.008f).coerceIn(0.15f, 0.45f)
    drawCircle(
        color = Color(0f, 0f, 0f, shadowAlpha),
        radius = 15f * scaleMultiplier,
        center = Offset(x, y + shadowOffset)
    )

    scale(scale = scaleMultiplier, pivot = Offset(x, y)) {
        rotate(degrees = rot, pivot = Offset(x, y)) {
            drawScrewHead(x, y, screw.color)
        }
    }
}

private fun DrawScope.drawScrewHead(x: Float, y: Float, color: ScrewColor) {
    val radius = 15f

    // 1. Outer metallic beveled rim
    drawCircle(
        brush = Brush.sweepGradient(
            listOf(
                Color(0xFFCBD5E1),
                Color(0xFF64748B),
                Color(0xFFF1F5F9),
                Color(0xFF475569),
                Color(0xFFE2E8F0),
                Color(0xFFCBD5E1)
            ),
            center = Offset(x, y)
        ),
        radius = radius,
        center = Offset(x, y)
    )

    // 2. Anodized colored screw dome
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.lightColor,
                color.baseColor,
                color.darkColor
            ),
            center = Offset(x - radius * 0.3f, y - radius * 0.35f),
            radius = radius * 1.1f
        ),
        radius = radius - 2.5f,
        center = Offset(x, y)
    )

    // 3. Specular highlight crescent
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(1f, 1f, 1f, 0.75f),
                Color(1f, 1f, 1f, 0.05f)
            ),
            center = Offset(x - radius * 0.35f, y - radius * 0.35f),
            radius = radius * 0.5f
        ),
        radius = radius * 0.5f,
        center = Offset(x - radius * 0.25f, y - radius * 0.25f)
    )

    // 4. Philips cross-slot (+) with 3D depth
    val slotArmLength = 6.5f
    val slotWidth = 2.4f

    // Dark indented slot base
    drawRect(
        color = Color(0xFF171717),
        topLeft = Offset(x - slotWidth / 2f, y - slotArmLength),
        size = Size(slotWidth, slotArmLength * 2f)
    )
    drawRect(
        color = Color(0xFF171717),
        topLeft = Offset(x - slotArmLength, y - slotWidth / 2f),
        size = Size(slotArmLength * 2f, slotWidth)
    )

    // Highlight on top-left edge of the slot
    drawLine(
        color = Color(1f, 1f, 1f, 0.5f),
        start = Offset(x - slotArmLength, y - slotWidth / 2f),
        end = Offset(x + slotArmLength, y - slotWidth / 2f),
        strokeWidth = 0.8f
    )
    drawLine(
        color = Color(1f, 1f, 1f, 0.5f),
        start = Offset(x - slotWidth / 2f, y - slotArmLength),
        end = Offset(x - slotWidth / 2f, y + slotArmLength),
        strokeWidth = 0.8f
    )
}

private fun DrawScope.drawFlyingScrew(flying: ActiveFlyingScrew) {
    val p = flying.progress
    // Smooth bezier flight curve with gentle arc height
    val arcPeakY = -50f * sin(p * PI.toFloat())
    val currentX = flying.startPos.x + (flying.targetPos.x - flying.startPos.x) * p
    val currentY = flying.startPos.y + (flying.targetPos.y - flying.startPos.y) * p + arcPeakY

    val rot = flying.rotationAngle
    val scale = 1.25f - (p * 0.25f) // starts larger and settles into tray size

    // Flight drop shadow
    drawCircle(
        color = Color(0f, 0f, 0f, 0.35f),
        radius = 16f * scale,
        center = Offset(currentX, currentY + 16f + (arcPeakY * -0.2f))
    )

    scale(scale = scale, pivot = Offset(currentX, currentY)) {
        rotate(degrees = rot, pivot = Offset(currentX, currentY)) {
            drawScrewHead(currentX, currentY, flying.color)
        }
    }
}

private fun DrawScope.drawScrewTray(uiState: GameUiState, slotPositions: List<Offset>) {
    // 3D Machined Metal Tray Dock at bottom
    val trayWidth = 360f
    val trayHeight = 85f
    val trayTopLeft = Offset(20f, 435f)
    val cornerRadius = CornerRadius(22f, 22f)

    // Tray base shadow
    drawRoundRect(
        color = Color(0x66000000),
        topLeft = Offset(trayTopLeft.x, trayTopLeft.y + 6f),
        size = Size(trayWidth, trayHeight),
        cornerRadius = cornerRadius
    )

    // Tray body metallic gradient
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF111827)),
            startY = trayTopLeft.y,
            endY = trayTopLeft.y + trayHeight
        ),
        topLeft = trayTopLeft,
        size = Size(trayWidth, trayHeight),
        cornerRadius = cornerRadius
    )

    // Tray brushed chrome border
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF475569), Color(0xFF94A3B8), Color(0xFF334155)),
            start = Offset(trayTopLeft.x, trayTopLeft.y),
            end = Offset(trayTopLeft.x + trayWidth, trayTopLeft.y + trayHeight)
        ),
        topLeft = trayTopLeft,
        size = Size(trayWidth, trayHeight),
        cornerRadius = cornerRadius,
        style = Stroke(width = 2.5f)
    )

    // Draw the 5 slots
    for (i in 0 until 5) {
        val slotPos = slotPositions[i]
        val slot = uiState.traySlots.getOrNull(i)

        // Concave slot socket
        drawCircle(
            color = Color(0x44000000),
            radius = 24f,
            center = Offset(slotPos.x, slotPos.y + 2f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0B0F17), Color(0xFF1E293B)),
                center = Offset(slotPos.x, slotPos.y),
                radius = 22f
            ),
            radius = 22f,
            center = slotPos
        )
        // Outer socket chrome ring
        drawCircle(
            brush = Brush.sweepGradient(
                listOf(Color(0xFF64748B), Color(0xFF94A3B8), Color(0xFF334155), Color(0xFF94A3B8), Color(0xFF64748B)),
                center = slotPos
            ),
            radius = 22f,
            center = slotPos,
            style = Stroke(width = 1.8f)
        )

        // Draw screw settled in slot
        if (slot?.color != null && slot.screwId != null) {
            if (slot.isMatching) {
                // Matching animation: Pulsing scale and golden glow
                val p = slot.matchProgress
                val matchScale = 1f + sin(p * PI.toFloat()) * 0.35f
                val glowRadius = 25f + sin(p * PI.toFloat()) * 18f

                // Golden burst aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.8f * (1f - p)), Color(0x00FBBF24)),
                        center = slotPos,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = slotPos
                )

                scale(scale = matchScale * (1f - p * 0.4f), pivot = slotPos) {
                    drawScrewHead(slotPos.x, slotPos.y, slot.color)
                }
            } else {
                drawScrewHead(slotPos.x, slotPos.y, slot.color)
            }
        }
    }
}

private fun DrawScope.drawFloatingText(text: FloatingScoreText) {
    val alpha = (1f - text.progress).coerceIn(0f, 1f)
    // Draw stylized pulsing star or particle marker above text position
    drawCircle(
        color = text.color.copy(alpha = alpha * 0.7f),
        radius = 8f * (1f - text.progress * 0.3f),
        center = Offset(text.x, text.y)
    )
    drawCircle(
        color = Color.White.copy(alpha = alpha),
        radius = 4f,
        center = Offset(text.x, text.y)
    )
}
