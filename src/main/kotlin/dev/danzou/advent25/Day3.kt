package dev.danzou.advent25

import dev.danzou.advent.utils.pow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day3 : AdventTestRunner25("Lobby") {
  fun maxJoltage(bank: List<Int>, batteries: Int): Long {
    if (batteries == 1) return bank.max().toLong()

    val max = bank.dropLast(batteries - 1).max()
    val indexOfMax = bank.indexOf(max)
    return max * (10L.pow(batteries - 1)) +
        maxJoltage(bank.drop(indexOfMax + 1), batteries - 1)
  }

  override fun part1(input: String): Long =
      input
          .lines()
          .map { it.map(Char::digitToInt) }
          .sumOf { bank -> maxJoltage(bank, 2) }

  override fun part2(input: String): Long =
      input
          .lines()
          .map { it.map(Char::digitToInt) }
          .sumOf { bank -> maxJoltage(bank, 12) }

  @Test
  fun testExample() {
    """
      987654321111111
      811111111111119
      234234234234278
      818181911112111
    """
        .trimIndent()
        .let { input ->
          assertEquals(357L, part1(input))
          assertEquals(3121910778619L, part2(input))
        }
  }
}
