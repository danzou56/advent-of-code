package dev.danzou.advent24

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass.Companion.CARDINAL_DIRECTIONS
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.toPair
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day18 : AdventTestRunner24("RAM Run") {

  override fun part1(input: String): Int {
    return part1(input, 70, 1024)
  }

  fun part1(input: String, gridSize: Int, simulate: Int): Int {
    val bytes =
        input
            .lines()
            .map { it.split(",").map { it.toInt() }.toPair() }
            .take(simulate)
            .toSet()
    return part1(bytes, gridSize)
  }

  fun part1(bytes: Set<Pos>, gridSize: Int): Int {
    val path =
        doDijkstras(
            0 to 0,
            { it == gridSize to gridSize },
            { cur ->
              CARDINAL_DIRECTIONS.map { cur + it }
                  .filter { it.x in 0..gridSize && it.y in 0..gridSize }
                  .filter { it !in bytes }
            },
        )
    return path.size - 1
  }

  override fun part2(input: String): String {
    return part2(input, 70, 1024).let { (x, y) -> "$x,$y" }
  }

  fun part2(input: String, gridSize: Int, start: Int): Pos {
    val bytes = input.lines().map { it.split(",").map { it.toInt() }.toPair() }

    // lazy, but works
    val firstBlocked =
        (start..bytes.size).first { i ->
          part1(bytes.subList(0, i).toSet(), gridSize) == -1
        }

    return bytes[firstBlocked - 1]
  }

  @Test
  fun testExample() {
    """
      5,4
      4,2
      4,5
      3,0
      2,1
      6,3
      2,4
      1,5
      0,6
      3,3
      2,6
      5,1
      1,2
      5,5
      2,5
      6,5
      1,4
      0,4
      6,4
      1,1
      6,1
      1,0
      0,5
      1,6
      2,0
    """
        .trimIndent()
        .let { input ->
          assertEquals(22, part1(input, 6, 12))
          assertEquals(6 to 1, part2(input, 6, 12))
        }
  }
}
