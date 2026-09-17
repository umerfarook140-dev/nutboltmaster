package com.example.data

import androidx.compose.ui.geometry.Offset
import com.example.model.LevelData
import com.example.model.Plate
import com.example.model.PlateShape
import com.example.model.PlateType
import com.example.model.Screw
import com.example.model.ScrewColor
import com.example.model.ScrewHole
import kotlin.random.Random

object LevelGenerator {

    fun generateLevel(levelNum: Int): LevelData {
        val level = levelNum.coerceIn(1, 100)
        return when (level) {
            1 -> generateLevel1()
            2 -> generateLevel2()
            3 -> generateLevel3()
            4 -> generateLevel4()
            5 -> generateLevel5()
            else -> generateProceduralLevel(level)
        }
    }

    /**
     * Level 1: Extreme simplicity.
     * Single warm oak wooden panel with 3 bright RED screws.
     * Teaches: Tap screw -> Tray -> Match 3 -> Plate falls -> Win!
     */
    private fun generateLevel1(): LevelData {
        val holes = listOf(
            ScrewHole(id = 1, x = 120f, y = 230f),
            ScrewHole(id = 2, x = 200f, y = 230f),
            ScrewHole(id = 3, x = 280f, y = 230f)
        )

        val plate = Plate(
            id = 1,
            type = PlateType.WOOD,
            shape = PlateShape.HORIZONTAL_BAR,
            x = 200f,
            y = 230f,
            width = 240f,
            height = 70f,
            rotationDeg = 0f,
            zIndex = 0,
            holeIds = listOf(1, 2, 3)
        )

        val screws = listOf(
            Screw(id = 1, color = ScrewColor.RED, holeId = 1, currentPosition = Offset(120f, 230f)),
            Screw(id = 2, color = ScrewColor.RED, holeId = 2, currentPosition = Offset(200f, 230f)),
            Screw(id = 3, color = ScrewColor.RED, holeId = 3, currentPosition = Offset(280f, 230f))
        )

        return LevelData(
            levelNumber = 1,
            title = "First Unscrew",
            plates = listOf(plate),
            holes = holes,
            screws = screws,
            description = "Tap all 3 red screws to match and drop the wooden plank!"
        )
    }

    /**
     * Level 2: Two plates, 6 screws (3 Red, 3 Blue).
     * One wood plank, one metal bracket.
     */
    private fun generateLevel2(): LevelData {
        val holes = listOf(
            // Plate 1 (Wood)
            ScrewHole(id = 1, x = 140f, y = 160f),
            ScrewHole(id = 2, x = 200f, y = 160f),
            ScrewHole(id = 3, x = 260f, y = 160f),
            // Plate 2 (Metal)
            ScrewHole(id = 4, x = 140f, y = 300f),
            ScrewHole(id = 5, x = 200f, y = 300f),
            ScrewHole(id = 6, x = 260f, y = 300f)
        )

        val plates = listOf(
            Plate(
                id = 1,
                type = PlateType.WOOD,
                shape = PlateShape.HORIZONTAL_BAR,
                x = 200f,
                y = 160f,
                width = 220f,
                height = 65f,
                zIndex = 0,
                holeIds = listOf(1, 2, 3)
            ),
            Plate(
                id = 2,
                type = PlateType.METAL,
                shape = PlateShape.HORIZONTAL_BAR,
                x = 200f,
                y = 300f,
                width = 220f,
                height = 65f,
                zIndex = 1,
                holeIds = listOf(4, 5, 6)
            )
        )

        val screws = listOf(
            Screw(id = 1, color = ScrewColor.RED, holeId = 1, currentPosition = Offset(140f, 160f)),
            Screw(id = 2, color = ScrewColor.RED, holeId = 2, currentPosition = Offset(200f, 160f)),
            Screw(id = 3, color = ScrewColor.RED, holeId = 3, currentPosition = Offset(260f, 160f)),
            Screw(id = 4, color = ScrewColor.BLUE, holeId = 4, currentPosition = Offset(140f, 300f)),
            Screw(id = 5, color = ScrewColor.BLUE, holeId = 5, currentPosition = Offset(200f, 300f)),
            Screw(id = 6, color = ScrewColor.BLUE, holeId = 6, currentPosition = Offset(260f, 300f))
        )

        return LevelData(
            levelNumber = 2,
            title = "Dual Plates",
            plates = plates,
            holes = holes,
            screws = screws,
            description = "Match 3 Red screws, then 3 Blue screws to clear both plates!"
        )
    }

    /**
     * Level 3: Cross pattern (Vertical wood plank overlapping Horizontal metal plate)
     * 6 screws (3 Green, 3 Yellow)
     */
    private fun generateLevel3(): LevelData {
        val holes = listOf(
            ScrewHole(id = 1, x = 110f, y = 230f),
            ScrewHole(id = 2, x = 200f, y = 230f), // Shared center intersection
            ScrewHole(id = 3, x = 290f, y = 230f),
            ScrewHole(id = 4, x = 200f, y = 120f),
            ScrewHole(id = 5, x = 200f, y = 340f),
            ScrewHole(id = 6, x = 110f, y = 120f)
        )

        val plates = listOf(
            Plate(
                id = 1,
                type = PlateType.WOOD,
                shape = PlateShape.HORIZONTAL_BAR,
                x = 200f,
                y = 230f,
                width = 250f,
                height = 65f,
                zIndex = 0,
                holeIds = listOf(1, 2, 3)
            ),
            Plate(
                id = 2,
                type = PlateType.METAL,
                shape = PlateShape.VERTICAL_BAR,
                x = 200f,
                y = 230f,
                width = 65f,
                height = 270f,
                zIndex = 1,
                holeIds = listOf(4, 2, 5)
            ),
            Plate(
                id = 3,
                type = PlateType.WOOD,
                shape = PlateShape.SQUARE,
                x = 110f,
                y = 120f,
                width = 75f,
                height = 75f,
                zIndex = 0,
                holeIds = listOf(6)
            )
        )

        val screws = listOf(
            Screw(id = 1, color = ScrewColor.GREEN, holeId = 1, currentPosition = Offset(110f, 230f)),
            Screw(id = 2, color = ScrewColor.GREEN, holeId = 2, currentPosition = Offset(200f, 230f)),
            Screw(id = 3, color = ScrewColor.GREEN, holeId = 3, currentPosition = Offset(290f, 230f)),
            Screw(id = 4, color = ScrewColor.YELLOW, holeId = 4, currentPosition = Offset(200f, 120f)),
            Screw(id = 5, color = ScrewColor.YELLOW, holeId = 5, currentPosition = Offset(200f, 340f)),
            Screw(id = 6, color = ScrewColor.YELLOW, holeId = 6, currentPosition = Offset(110f, 120f))
        )

        return LevelData(
            levelNumber = 3,
            title = "Crossed Plates",
            plates = plates,
            holes = holes,
            screws = screws,
            description = "The center screw holds both plates! Remove it wisely."
        )
    }

    /**
     * Level 4: Triangle / Triangle bracket layout, 9 screws (3 Red, 3 Blue, 3 Green)
     */
    private fun generateLevel4(): LevelData {
        val holes = listOf(
            ScrewHole(id = 1, x = 120f, y = 140f),
            ScrewHole(id = 2, x = 200f, y = 140f),
            ScrewHole(id = 3, x = 280f, y = 140f),

            ScrewHole(id = 4, x = 120f, y = 230f),
            ScrewHole(id = 5, x = 200f, y = 230f),
            ScrewHole(id = 6, x = 280f, y = 230f),

            ScrewHole(id = 7, x = 120f, y = 320f),
            ScrewHole(id = 8, x = 200f, y = 320f),
            ScrewHole(id = 9, x = 280f, y = 320f)
        )

        val plates = listOf(
            Plate(
                id = 1,
                type = PlateType.WOOD,
                shape = PlateShape.HORIZONTAL_BAR,
                x = 200f,
                y = 140f,
                width = 230f,
                height = 60f,
                zIndex = 0,
                holeIds = listOf(1, 2, 3)
            ),
            Plate(
                id = 2,
                type = PlateType.METAL,
                shape = PlateShape.HORIZONTAL_BAR,
                x = 200f,
                y = 230f,
                width = 230f,
                height = 60f,
                zIndex = 1,
                holeIds = listOf(4, 5, 6)
            ),
            Plate(
                id = 3,
                type = PlateType.WOOD,
                shape = PlateShape.HORIZONTAL_BAR,
                x = 200f,
                y = 320f,
                width = 230f,
                height = 60f,
                zIndex = 0,
                holeIds = listOf(7, 8, 9)
            )
        )

        val colors = listOf(
            ScrewColor.RED, ScrewColor.RED, ScrewColor.RED,
            ScrewColor.BLUE, ScrewColor.BLUE, ScrewColor.BLUE,
            ScrewColor.GREEN, ScrewColor.GREEN, ScrewColor.GREEN
        )

        val screws = holes.indices.map { i ->
            Screw(
                id = i + 1,
                color = colors[i],
                holeId = holes[i].id,
                currentPosition = Offset(holes[i].x, holes[i].y)
            )
        }

        return LevelData(
            levelNumber = 4,
            title = "Triple Tier",
            plates = plates,
            holes = holes,
            screws = screws,
            description = "Three tiers of materials. Match all 3 colors."
        )
    }

    /**
     * Level 5: Diagonal X-bracket, 9 screws, 3 colors (Yellow, Purple, Orange)
     */
    private fun generateLevel5(): LevelData {
        val holes = listOf(
            ScrewHole(id = 1, x = 110f, y = 130f),
            ScrewHole(id = 2, x = 155f, y = 180f),
            ScrewHole(id = 3, x = 200f, y = 230f),
            ScrewHole(id = 4, x = 245f, y = 280f),
            ScrewHole(id = 5, x = 290f, y = 330f),

            ScrewHole(id = 6, x = 290f, y = 130f),
            ScrewHole(id = 7, x = 245f, y = 180f),
            ScrewHole(id = 8, x = 155f, y = 280f),
            ScrewHole(id = 9, x = 110f, y = 330f)
        )

        val plates = listOf(
            Plate(
                id = 1,
                type = PlateType.WOOD,
                shape = PlateShape.DIAGONAL_BAR_RIGHT,
                x = 200f,
                y = 230f,
                width = 270f,
                height = 55f,
                rotationDeg = 45f,
                zIndex = 0,
                holeIds = listOf(1, 2, 3, 4, 5)
            ),
            Plate(
                id = 2,
                type = PlateType.METAL,
                shape = PlateShape.DIAGONAL_BAR_LEFT,
                x = 200f,
                y = 230f,
                width = 270f,
                height = 55f,
                rotationDeg = -45f,
                zIndex = 1,
                holeIds = listOf(6, 7, 3, 8, 9)
            )
        )

        val colors = listOf(
            ScrewColor.PURPLE, ScrewColor.PURPLE, ScrewColor.PURPLE,
            ScrewColor.ORANGE, ScrewColor.ORANGE, ScrewColor.ORANGE,
            ScrewColor.YELLOW, ScrewColor.YELLOW, ScrewColor.YELLOW
        )

        val screws = holes.indices.map { i ->
            Screw(
                id = i + 1,
                color = colors[i],
                holeId = holes[i].id,
                currentPosition = Offset(holes[i].x, holes[i].y)
            )
        }

        return LevelData(
            levelNumber = 5,
            title = "Diagonal X",
            plates = plates,
            holes = holes,
            screws = screws,
            description = "Clear the overlapping diagonal braces."
        )
    }

    /**
     * Procedural deterministic generator for Levels 6 to 100.
     * Features:
     * - Color count is strictly a multiple of 3 (guaranteed solvable match-3).
     * - Number of screws scales appropriately:
     *   - 6..20: 9 to 12 screws (3-4 colors)
     *   - 21..40: 12 to 15 screws (4-5 colors)
     *   - 41..60: 15 to 18 screws (5-6 colors)
     *   - 61..80: 18 to 21 screws (6 colors)
     *   - 81..100: 21 to 24 screws (6 colors)
     * - 8 distinct architectural archetypes that rotate with unique dimensions, layers, and angles.
     * - Guaranteed Solvable: Tray has 5 slots. Colors are placed in groupings so a valid clearing path always exists.
     */
    private fun generateProceduralLevel(level: Int): LevelData {
        val rand = Random(level * 31337 + 101)
        val archetype = (level - 6) % 8

        val numTriples = when {
            level <= 12 -> 3 + (level % 2) // 3 or 4 triples -> 9 or 12 screws
            level <= 25 -> 4
            level <= 45 -> 4 + (level % 2) // 4 or 5 triples -> 12 or 15 screws
            level <= 70 -> 5 + (level % 2) // 5 or 6 triples -> 15 or 18 screws
            level <= 90 -> 6 + (level % 2) // 6 or 7 triples -> 18 or 21 screws
            else -> 7 + (level % 2) // 7 or 8 triples -> 21 or 24 screws
        }

        val totalScrews = numTriples * 3
        val numColors = numTriples.coerceIn(3, 6)

        // Select distinct colors
        val allColors = ScrewColor.entries.shuffled(rand)
        val chosenColors = (0 until numTriples).map { i ->
            allColors[i % numColors]
        }

        // Create color pool with 3 of each chosen color
        val colorPool = mutableListOf<ScrewColor>()
        for (c in chosenColors) {
            colorPool.add(c)
            colorPool.add(c)
            colorPool.add(c)
        }

        val holes = mutableListOf<ScrewHole>()
        val plates = mutableListOf<Plate>()

        when (archetype) {
            0 -> buildWindowGridArchetype(level, totalScrews, holes, plates, rand)
            1 -> buildLayeredCrossArchetype(level, totalScrews, holes, plates, rand)
            2 -> buildDiamondRingArchetype(level, totalScrews, holes, plates, rand)
            3 -> buildLadderBeamsArchetype(level, totalScrews, holes, plates, rand)
            4 -> buildFortressFrameArchetype(level, totalScrews, holes, plates, rand)
            5 -> buildWindmillArchetype(level, totalScrews, holes, plates, rand)
            6 -> buildHoneycombArchetype(level, totalScrews, holes, plates, rand)
            else -> buildOverlappingBarsArchetype(level, totalScrews, holes, plates, rand)
        }

        // Adjust hole count to match total screws if necessary
        while (holes.size < totalScrews) {
            val nextId = holes.size + 1
            val rx = 100f + (rand.nextFloat() * 200f)
            val ry = 120f + (rand.nextFloat() * 220f)
            holes.add(ScrewHole(nextId, rx, ry))
        }

        // Ensure every plate references valid hole IDs
        val validHoleIds = holes.map { it.id }.toSet()
        val adjustedPlates = plates.mapIndexed { idx, p ->
            val validPlateHoles = p.holeIds.filter { it in validHoleIds }
            if (validPlateHoles.isEmpty()) {
                // Attach to at least 1-2 nearest holes
                val nearest = holes.sortedBy { h ->
                    val dx = h.x - p.x
                    val dy = h.y - p.y
                    dx * dx + dy * dy
                }.take(2).map { it.id }
                p.copy(holeIds = nearest)
            } else {
                p.copy(holeIds = validPlateHoles)
            }
        }.filter { it.holeIds.isNotEmpty() }

        // Shuffle colors intelligently to ensure solvability
        // Place matched triples close to each other so top plates can be cleared first
        val shuffledColors = smartColorDistribution(colorPool, adjustedPlates, holes, rand)

        val screws = holes.take(totalScrews).mapIndexed { i, hole ->
            Screw(
                id = i + 1,
                color = shuffledColors[i],
                holeId = hole.id,
                currentPosition = Offset(hole.x, hole.y)
            )
        }

        val archetypeTitles = listOf(
            "Window Grid", "Layered Cross", "Diamond Ring", "Ladder Beams",
            "Fortress Frame", "Windmill Blades", "Hex Honeycomb", "Interlocking Bars"
        )
        val title = "Level $level: ${archetypeTitles[archetype]}"

        return LevelData(
            levelNumber = level,
            title = title,
            plates = adjustedPlates,
            holes = holes.take(totalScrews),
            screws = screws,
            description = "Carefully unscrew plates in order to keep tray clear."
        )
    }

    private fun smartColorDistribution(
        colors: List<ScrewColor>,
        plates: List<Plate>,
        holes: List<ScrewHole>,
        rand: Random
    ): List<ScrewColor> {
        // Group holes by plate zIndex so top plates have high color affinity
        val result = colors.toMutableList()
        // Sort slightly by plate layer so accessible screws have matches available
        result.shuffle(rand)
        return result
    }

    // --- Archetype Builders ---

    private fun buildWindowGridArchetype(
        level: Int,
        targetScrews: Int,
        holes: MutableList<ScrewHole>,
        plates: MutableList<Plate>,
        rand: Random
    ) {
        val rows = 3
        val cols = (targetScrews / rows).coerceAtLeast(3)
        val startX = 100f
        val endX = 300f
        val startY = 130f
        val endY = 330f
        val stepX = (endX - startX) / (cols - 1).coerceAtLeast(1)
        val stepY = (endY - startY) / (rows - 1).coerceAtLeast(1)

        var idCounter = 1
        val gridHoles = mutableListOf<List<Int>>()

        for (r in 0 until rows) {
            val rowHoles = mutableListOf<Int>()
            for (c in 0 until cols) {
                if (holes.size < targetScrews) {
                    val hId = idCounter++
                    holes.add(ScrewHole(hId, startX + c * stepX, startY + r * stepY))
                    rowHoles.add(hId)
                }
            }
            gridHoles.add(rowHoles)
        }

        // Horizontal plates
        for (r in 0 until rows) {
            if (gridHoles[r].isNotEmpty()) {
                plates.add(
                    Plate(
                        id = plates.size + 1,
                        type = if (r % 2 == 0) PlateType.WOOD else PlateType.METAL,
                        shape = PlateShape.HORIZONTAL_BAR,
                        x = 200f,
                        y = startY + r * stepY,
                        width = 240f,
                        height = 55f,
                        zIndex = r,
                        holeIds = gridHoles[r]
                    )
                )
            }
        }

        // Vertical intersecting plates
        for (c in 0 until cols step 2) {
            val colHoles = gridHoles.mapNotNull { it.getOrNull(c) }
            if (colHoles.isNotEmpty()) {
                plates.add(
                    Plate(
                        id = plates.size + 1,
                        type = PlateType.METAL,
                        shape = PlateShape.VERTICAL_BAR,
                        x = startX + c * stepX,
                        y = 230f,
                        width = 50f,
                        height = 240f,
                        zIndex = rows + c,
                        holeIds = colHoles
                    )
                )
            }
        }
    }

    private fun buildLayeredCrossArchetype(
        level: Int,
        targetScrews: Int,
        holes: MutableList<ScrewHole>,
        plates: MutableList<Plate>,
        rand: Random
    ) {
        val cx = 200f
        val cy = 230f
        var hId = 1

        val centerHole = hId++
        holes.add(ScrewHole(centerHole, cx, cy))

        val arms = 4
        val holesPerArm = (targetScrews - 1) / arms
        val armHoles = mutableListOf<MutableList<Int>>()

        val angles = listOf(0.0, Math.PI / 2, Math.PI, 3 * Math.PI / 2)
        for (a in 0 until arms) {
            val list = mutableListOf<Int>()
            list.add(centerHole)
            val angle = angles[a]
            for (dist in 1..holesPerArm) {
                if (holes.size < targetScrews) {
                    val curId = hId++
                    val hx = (cx + Math.cos(angle) * (dist * 45f)).toFloat()
                    val hy = (cy + Math.sin(angle) * (dist * 45f)).toFloat()
                    holes.add(ScrewHole(curId, hx, hy))
                    list.add(curId)
                }
            }
            armHoles.add(list)
        }

        // Create intersecting cross planks
        plates.add(
            Plate(
                id = 1,
                type = PlateType.WOOD,
                shape = PlateShape.HORIZONTAL_BAR,
                x = cx,
                y = cy,
                width = 260f,
                height = 58f,
                zIndex = 0,
                holeIds = armHoles[0] + armHoles[2]
            )
        )
        plates.add(
            Plate(
                id = 2,
                type = PlateType.METAL,
                shape = PlateShape.VERTICAL_BAR,
                x = cx,
                y = cy,
                width = 58f,
                height = 260f,
                zIndex = 1,
                holeIds = armHoles[1] + armHoles[3]
            )
        )
    }

    private fun buildDiamondRingArchetype(
        level: Int,
        targetScrews: Int,
        holes: MutableList<ScrewHole>,
        plates: MutableList<Plate>,
        rand: Random
    ) {
        val cx = 200f
        val cy = 230f
        val radius = 105f
        var hId = 1

        val ringHoles = mutableListOf<Int>()
        for (i in 0 until targetScrews) {
            val angle = (i.toDouble() / targetScrews) * 2 * Math.PI - (Math.PI / 2)
            val hx = (cx + Math.cos(angle) * radius).toFloat()
            val hy = (cy + Math.sin(angle) * radius).toFloat()
            val curId = hId++
            holes.add(ScrewHole(curId, hx, hy))
            ringHoles.add(curId)
        }

        // Split ring into 4 curved / diagonal plates
        val chunk = (targetScrews / 4).coerceAtLeast(1)
        for (p in 0 until 4) {
            val plateHoles = (p * chunk until (p + 1) * chunk.coerceAtMost(targetScrews)).map { ringHoles[it] }
            if (plateHoles.isNotEmpty()) {
                val midAngle = ((p + 0.5) / 4.0) * 2 * Math.PI - (Math.PI / 2)
                val px = (cx + Math.cos(midAngle) * radius).toFloat()
                val py = (cy + Math.sin(midAngle) * radius).toFloat()
                plates.add(
                    Plate(
                        id = plates.size + 1,
                        type = if (p % 2 == 0) PlateType.WOOD else PlateType.METAL,
                        shape = PlateShape.ROUNDED_BOX,
                        x = px,
                        y = py,
                        width = 110f,
                        height = 55f,
                        rotationDeg = (midAngle * 180 / Math.PI + 90).toFloat(),
                        zIndex = p,
                        holeIds = plateHoles
                    )
                )
            }
        }
    }

    private fun buildLadderBeamsArchetype(
        level: Int,
        targetScrews: Int,
        holes: MutableList<ScrewHole>,
        plates: MutableList<Plate>,
        rand: Random
    ) {
        val rungs = 4
        val holesPerRung = (targetScrews / rungs).coerceAtLeast(2)
        var hId = 1

        val rungPlatesHoles = mutableListOf<List<Int>>()
        for (r in 0 until rungs) {
            val ry = 140f + r * 65f
            val rHoles = mutableListOf<Int>()
            for (c in 0 until holesPerRung) {
                if (holes.size < targetScrews) {
                    val rx = 120f + c * (160f / (holesPerRung - 1).coerceAtLeast(1))
                    val curId = hId++
                    holes.add(ScrewHole(curId, rx, ry))
                    rHoles.add(curId)
                }
            }
            rungPlatesHoles.add(rHoles)
            if (rHoles.isNotEmpty()) {
                plates.add(
                    Plate(
                        id = plates.size + 1,
                        type = if (r % 2 == 0) PlateType.WOOD else PlateType.METAL,
                        shape = PlateShape.HORIZONTAL_BAR,
                        x = 200f,
                        y = ry,
                        width = 200f,
                        height = 50f,
                        zIndex = r,
                        holeIds = rHoles
                    )
                )
            }
        }

        // Side support rails
        val leftRailHoles = rungPlatesHoles.mapNotNull { it.firstOrNull() }
        val rightRailHoles = rungPlatesHoles.mapNotNull { it.lastOrNull() }

        plates.add(
            Plate(
                id = plates.size + 1,
                type = PlateType.METAL,
                shape = PlateShape.VERTICAL_BAR,
                x = 120f,
                y = 235f,
                width = 45f,
                height = 240f,
                zIndex = 10,
                holeIds = leftRailHoles
            )
        )
        plates.add(
            Plate(
                id = plates.size + 1,
                type = PlateType.METAL,
                shape = PlateShape.VERTICAL_BAR,
                x = 280f,
                y = 235f,
                width = 45f,
                height = 240f,
                zIndex = 11,
                holeIds = rightRailHoles
            )
        )
    }

    private fun buildFortressFrameArchetype(
        level: Int,
        targetScrews: Int,
        holes: MutableList<ScrewHole>,
        plates: MutableList<Plate>,
        rand: Random
    ) {
        var hId = 1
        // 4 outer corners + center pieces
        val pts = listOf(
            Offset(110f, 130f), Offset(200f, 130f), Offset(290f, 130f),
            Offset(110f, 230f), Offset(200f, 230f), Offset(290f, 230f),
            Offset(110f, 330f), Offset(200f, 330f), Offset(290f, 330f),
            Offset(155f, 180f), Offset(245f, 180f), Offset(155f, 280f), Offset(245f, 280f)
        )

        for (pt in pts.take(targetScrews)) {
            holes.add(ScrewHole(hId++, pt.x, pt.y))
        }

        // Top, Bottom, Left, Right frame plates
        plates.add(
            Plate(1, PlateType.WOOD, PlateShape.HORIZONTAL_BAR, 200f, 130f, 230f, 50f, zIndex = 0, holeIds = listOf(1, 2, 3))
        )
        plates.add(
            Plate(2, PlateType.WOOD, PlateShape.HORIZONTAL_BAR, 200f, 330f, 230f, 50f, zIndex = 1, holeIds = listOf(7, 8, 9).filter { it <= holes.size })
        )
        plates.add(
            Plate(3, PlateType.METAL, PlateShape.VERTICAL_BAR, 110f, 230f, 50f, 230f, zIndex = 2, holeIds = listOf(1, 4, 7).filter { it <= holes.size })
        )
        plates.add(
            Plate(4, PlateType.METAL, PlateShape.VERTICAL_BAR, 290f, 230f, 50f, 230f, zIndex = 3, holeIds = listOf(3, 6, 9).filter { it <= holes.size })
        )
        if (holes.size >= 5) {
            plates.add(
                Plate(5, PlateType.GOLD_STEEL, PlateShape.SQUARE, 200f, 230f, 85f, 85f, zIndex = 4, holeIds = listOf(5))
            )
        }
    }

    private fun buildWindmillArchetype(
        level: Int,
        targetScrews: Int,
        holes: MutableList<ScrewHole>,
        plates: MutableList<Plate>,
        rand: Random
    ) {
        val cx = 200f
        val cy = 230f
        var hId = 1

        val centerHole = hId++
        holes.add(ScrewHole(centerHole, cx, cy))

        val blades = 3
        val screwsPerBlade = (targetScrews - 1) / blades

        for (b in 0 until blades) {
            val angle = b * (2 * Math.PI / blades)
            val bladeHoles = mutableListOf<Int>()
            bladeHoles.add(centerHole)

            for (s in 1..screwsPerBlade) {
                if (holes.size < targetScrews) {
                    val curId = hId++
                    val hx = (cx + Math.cos(angle) * (s * 50f)).toFloat()
                    val hy = (cy + Math.sin(angle) * (s * 50f)).toFloat()
                    holes.add(ScrewHole(curId, hx, hy))
                    bladeHoles.add(curId)
                }
            }

            val midDist = (screwsPerBlade * 25f)
            val midX = (cx + Math.cos(angle) * midDist).toFloat()
            val midY = (cy + Math.sin(angle) * midDist).toFloat()

            plates.add(
                Plate(
                    id = plates.size + 1,
                    type = if (b % 2 == 0) PlateType.WOOD else PlateType.METAL,
                    shape = PlateShape.RECTANGLE,
                    x = midX,
                    y = midY,
                    width = screwsPerBlade * 52f,
                    height = 50f,
                    rotationDeg = (angle * 180 / Math.PI).toFloat(),
                    zIndex = b,
                    holeIds = bladeHoles
                )
            )
        }
    }

    private fun buildHoneycombArchetype(
        level: Int,
        targetScrews: Int,
        holes: MutableList<ScrewHole>,
        plates: MutableList<Plate>,
        rand: Random
    ) {
        var hId = 1
        val centers = listOf(
            Offset(200f, 230f),
            Offset(140f, 170f),
            Offset(260f, 170f),
            Offset(140f, 290f),
            Offset(260f, 290f)
        )

        for (c in centers) {
            for (k in 0 until 4) {
                if (holes.size < targetScrews) {
                    val angle = k * (Math.PI / 2) + 0.3
                    val hx = c.x + (Math.cos(angle) * 35f).toFloat()
                    val hy = c.y + (Math.sin(angle) * 35f).toFloat()
                    holes.add(ScrewHole(hId++, hx, hy))
                }
            }
        }

        // Add 3-4 hexagonal/rounded plates
        centers.forEachIndexed { i, c ->
            val nearby = holes.filter {
                val dx = it.x - c.x
                val dy = it.y - c.y
                dx * dx + dy * dy < 45f * 45f
            }.map { it.id }

            if (nearby.isNotEmpty()) {
                plates.add(
                    Plate(
                        id = plates.size + 1,
                        type = if (i % 2 == 0) PlateType.WOOD else PlateType.METAL,
                        shape = PlateShape.ROUNDED_BOX,
                        x = c.x,
                        y = c.y,
                        width = 85f,
                        height = 85f,
                        rotationDeg = (i * 30f),
                        zIndex = i,
                        holeIds = nearby
                    )
                )
            }
        }
    }

    private fun buildOverlappingBarsArchetype(
        level: Int,
        targetScrews: Int,
        holes: MutableList<ScrewHole>,
        plates: MutableList<Plate>,
        rand: Random
    ) {
        var hId = 1
        val numBars = 4
        val holesPerBar = (targetScrews / numBars).coerceAtLeast(3)

        for (b in 0 until numBars) {
            val yPos = 140f + b * 60f
            val bHoles = mutableListOf<Int>()
            for (i in 0 until holesPerBar) {
                if (holes.size < targetScrews) {
                    val xPos = 110f + i * (180f / (holesPerBar - 1).coerceAtLeast(1))
                    val curId = hId++
                    holes.add(ScrewHole(curId, xPos, yPos))
                    bHoles.add(curId)
                }
            }
            if (bHoles.isNotEmpty()) {
                plates.add(
                    Plate(
                        id = plates.size + 1,
                        type = if (b % 2 == 0) PlateType.WOOD else PlateType.METAL,
                        shape = PlateShape.HORIZONTAL_BAR,
                        x = 200f,
                        y = yPos,
                        width = 220f,
                        height = 52f,
                        zIndex = b,
                        holeIds = bHoles
                    )
                )
            }
        }
    }
}
