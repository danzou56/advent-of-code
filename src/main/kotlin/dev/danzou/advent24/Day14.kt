package dev.danzou.advent24

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day14 : AdventTestRunner24("Restroom Redoubt") {
  // Mutability is used in part2 to improve speed
  data class Robot(var pX: Int, var pY: Int, var dX: Int, var dY: Int)

  override fun part1(input: String): Int {
    return part1(input, 101, 103, 100)
  }

  fun part1(input: String, width: Int, height: Int, iters: Int): Int {
    val robots =
        input
            .lines()
            .map {
              Regex("-?\\d+")
                  .findAll(it)
                  .toList()
                  .map { it.value.toInt() }
                  .also { assert(it.size == 4) }
            }
            .map { (pX, pY, dX, dY) -> Robot(pX, pY, dX, dY) }
    val finalPos =
        robots.map { (pX, pY, dX, dY) ->
          ((pX + dX * iters).mod(width) + width).mod(width) to
              ((pY + dY * iters + height).mod(height) + height).mod(height)
        }

    val q1 = finalPos.count { (pX, pY) -> pX < width / 2 && pY < height / 2 }
    val q2 = finalPos.count { (pX, pY) -> pX > width / 2 && pY < height / 2 }
    val q3 = finalPos.count { (pX, pY) -> pX < width / 2 && pY > height / 2 }
    val q4 = finalPos.count { (pX, pY) -> pX > width / 2 && pY > height / 2 }
    return q1 * q2 * q3 * q4
  }

  override fun part2(input: String): Any {
    return part2(input, 101, 103)
  }

  fun part2(input: String, width: Int, height: Int): Int {
    val robots =
        input
            .lines()
            .map { Regex("-?\\d+").findAll(it).toList().map { it.value.toInt() } }
            .map { (pX, pY, dX, dY) -> Robot(pX, pY, dX, dY) }

    tailrec fun step(robots: List<Robot>, iters: Int): Int {
      robots.forEach {
        it.run {
          pX = ((pX + dX).mod(width) + width).mod(width)
          pY = ((pY + dY).mod(height) + height).mod(height)
        }
      }
      if (robots.map { (pX, pY) -> pX to pY }.toSet().size == robots.size) return iters + 1
      return step(robots, iters + 1)
    }

    return step(robots, 0)
  }

  @Test
  fun testExample() {
    """
      p=0,4 v=3,-3
      p=6,3 v=-1,-3
      p=10,3 v=-1,2
      p=2,0 v=2,-1
      p=0,0 v=1,3
      p=3,0 v=-2,-2
      p=7,6 v=-1,-3
      p=3,0 v=-1,-2
      p=9,3 v=2,3
      p=7,3 v=-1,2
      p=2,4 v=2,-3
      p=9,5 v=-3,-3
    """
        .trimIndent()
        .let { input ->
          assertEquals(12, part1(input, 11, 7, 100))
          // assertEquals(null, part2(input))
        }
  }
}
