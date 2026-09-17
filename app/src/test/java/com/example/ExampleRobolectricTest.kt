package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GamePreferences
import com.example.data.LevelGenerator
import com.example.model.ScrewColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NutBolt Master 3D", appName)
  }

  @Test
  fun `test 100 levels validity and solvability constraints`() {
    for (lvl in 1..100) {
      val levelData = LevelGenerator.generateLevel(lvl)
      assertEquals(lvl, levelData.levelNumber)
      assertTrue("Level $lvl has plates", levelData.plates.isNotEmpty())
      assertTrue("Level $lvl has holes", levelData.holes.isNotEmpty())
      assertTrue("Level $lvl has screws", levelData.screws.isNotEmpty())

      // Solvability requirement: Screws must be multiples of 3 for each color
      val colorCounts = levelData.screws.groupBy { it.color }
      for ((color, list) in colorCounts) {
        assertEquals(
          "Level $lvl color $color count must be multiple of 3",
          0,
          list.size % 3
        )
      }

      // Every plate must have at least one hole assigned
      for (plate in levelData.plates) {
        assertTrue("Plate ${plate.id} in level $lvl has holes", plate.holeIds.isNotEmpty())
      }
    }
  }

  @Test
  fun `test game preferences save and unlock system`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = GamePreferences(context)
    prefs.resetAllProgress()

    assertEquals(1, prefs.currentLevel)
    assertEquals(1, prefs.highestUnlockedLevel)
    assertEquals(100, prefs.totalCoins)

    prefs.saveStarsForLevel(1, 3)
    assertEquals(3, prefs.getStarsForLevel(1))

    prefs.unlockNextLevel(1)
    assertEquals(2, prefs.highestUnlockedLevel)
    assertEquals(2, prefs.currentLevel)

    prefs.addCoins(50)
    assertEquals(150, prefs.totalCoins)
  }
}

