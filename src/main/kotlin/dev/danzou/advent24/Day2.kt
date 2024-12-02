package dev.danzou.advent24

import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day2 : AdventTestRunner24("Red-Nosed Reports") {
  fun isSafe(nums: List<Int>): Boolean {
    return nums.windowed(2, 1).let { windowed ->
      windowed.all { it[1] - it[0] > 0 && abs(it[1] - it[0]) in 1..3 } ||
          windowed.all { it[1] - it[0] < 0 && abs(it[1] - it[0]) in 1..3 }
    }
  }

  override fun part1(input: String): Int {
    return input
        .lines()
        .map { it.split(" ").map(String::toInt) }
        .count(::isSafe)
  }

  override fun part2(input: String): Int {
    return input
        .lines()
        .map { it.split(" ").map(String::toInt) }
        .filter { !isSafe(it) }
        .count { nums ->
          nums.indices.any { i ->
            // Kotlin List type doesn't have removeAt index
            // Do stupid toMutable conversion first
            val mut = nums.toMutableList()
            mut.removeAt(i)
            isSafe(mut)
          }
        } + part1(input)
  }

  @Test
  fun testExample() {
    """
      7 6 4 2 1
      1 2 7 8 9
      9 7 6 2 1
      1 3 2 4 5
      8 6 4 4 1
      1 3 6 7 9
    """
        .trimIndent()
        .let { input ->
          assertEquals(2, part1(input))
          assertEquals(4, part2(input))
        }
  }
}
