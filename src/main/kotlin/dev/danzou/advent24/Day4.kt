package dev.danzou.advent24

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.times
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day4 : AdventTestRunner24("") {

  fun isXmas(mat: Matrix<Char>, start: Pos, dir: Compass): Boolean {
    return "XMAS"
        .mapIndexed { i, c -> mat.getOrNull(start + dir.dir * i) == c }
        .all { it }
  }

  override fun part1(input: String): Number {
    val mat = input.asMatrix<Char>()

    val xs = mat.indices2D.filter { mat[it] == 'X' }
    return xs.sumOf { xPos -> Compass.ALL.count { isXmas(mat, xPos, it) } }
  }

  fun isXmas2(mat: Matrix<Char>, start: Pos, dir: Compass): Boolean {
    when (dir) {
      Compass.EAST ->
          return mat.getOrNull(start + Compass.NORTHEAST.dir) == 'M' &&
              mat.getOrNull(start + Compass.SOUTHEAST.dir) == 'M' &&
              mat.getOrNull(start + Compass.NORTHWEST.dir) == 'S' &&
              mat.getOrNull(start + Compass.SOUTHWEST.dir) == 'S'
      Compass.WEST ->
          return mat.getOrNull(start + Compass.NORTHEAST.dir) == 'S' &&
              mat.getOrNull(start + Compass.SOUTHEAST.dir) == 'S' &&
              mat.getOrNull(start + Compass.NORTHWEST.dir) == 'M' &&
              mat.getOrNull(start + Compass.SOUTHWEST.dir) == 'M'
      Compass.SOUTH ->
          return mat.getOrNull(start + Compass.NORTHEAST.dir) == 'S' &&
              mat.getOrNull(start + Compass.SOUTHEAST.dir) == 'M' &&
              mat.getOrNull(start + Compass.NORTHWEST.dir) == 'S' &&
              mat.getOrNull(start + Compass.SOUTHWEST.dir) == 'M'
      Compass.NORTH ->
          return mat.getOrNull(start + Compass.NORTHEAST.dir) == 'M' &&
              mat.getOrNull(start + Compass.SOUTHEAST.dir) == 'S' &&
              mat.getOrNull(start + Compass.NORTHWEST.dir) == 'M' &&
              mat.getOrNull(start + Compass.SOUTHWEST.dir) == 'S'
      else -> return false
    }
  }

  override fun part2(input: String): Number {
    val mat = input.asMatrix<Char>()
    val centers = mat.indices2D.filter { mat[it] == 'A' }
    return centers.count { xPos -> Compass.ALL.any { isXmas2(mat, xPos, it) } }
  }

  @Test
  fun testExample() {
    """
      MMMSXXMASM
      MSAMXMSMSA
      AMXSXMAAMM
      MSAMASMSMX
      XMASAMXAMM
      XXAMMXXAMA
      SMSMSASXSS
      SAXAMASAAA
      MAMMMXMMMM
      MXMXAXMASX
    """
        .trimIndent()
        .let { input ->
          //          assertEquals(null, part1(input))
          assertEquals(9, part2(input))
        }
  }
}
