package dev.danzou.advent25

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass.Companion.ALL_DIRECTIONS
import dev.danzou.advent.utils.geometry.Pos
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day4 : AdventTestRunner25("Printing Department") {
  private val PAPER = '@'
  private val EMPTY = '.'

  fun getAccessible(map: Matrix<Char>): Collection<Pos> =
      map.indices2D
          .filter { map[it] == PAPER }
          .filter { pos ->
            map.neighboringPos(pos, ALL_DIRECTIONS).count { map[it] == PAPER } < 4
          }

  override fun part1(input: String): Int = getAccessible(input.asMatrix<Char>()).size

  override fun part2(input: String): Int {

    fun step(map: Matrix<Char>): Int {
      val accessible = getAccessible(map)
      if (accessible.isEmpty()) return 0
      return accessible.size +
          step(map.mapIndexed2D { pos, el -> if (pos in accessible) EMPTY else el })
    }

    return step(input.asMatrix<Char>())
  }

  @Test
  fun testExample() {
    """
      ..@@.@@@@.
      @@@.@.@.@@
      @@@@@.@.@@
      @.@@@@..@.
      @@.@@@@.@@
      .@@@@@@@.@
      .@.@.@.@@@
      @.@@@.@@@@
      .@@@@@@@@.
      @.@.@@@.@.
    """
        .trimIndent()
        .let { input ->
          assertEquals(13, part1(input))
          assertEquals(43, part2(input))
        }
  }
}
