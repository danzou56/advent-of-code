package dev.danzou.advent25

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.toPair
import kotlin.math.max
import kotlin.math.min
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day5 : AdventTestRunner25("Cafeteria") {

  fun parseIngredients(input: String): Pair<List<LongRange>, List<Long>> {
    val (rangeLines, availableLines) = input.split("\n\n").toPair()
    return rangeLines
        .lines()
        .map { line -> line.split("-").map(String::toLong) }
        .map { (f, s) -> f..s } to availableLines.lines().map(String::toLong)
  }

  override fun part1(input: String): Int {
    val (fresh, available) = parseIngredients(input)
    return available.count { ingredient -> fresh.any { it.contains(ingredient) } }
  }

  tailrec fun merge(ranges: List<LongRange>, newRange: LongRange): List<LongRange> {
    val index =
        ranges.binarySearch {
          when {
            it.intersects(newRange) -> 0
            it.last < newRange.first -> 1
            else -> -1
          }
        }
    if (index < 0) {
      val insertionIndex = -index - 1
      return ranges.take(insertionIndex) + listOf(newRange) + ranges.drop(insertionIndex)
    } else {
      val target = ranges[index]
      val unioned = min(target.first, newRange.first)..max(target.last, newRange.last)
      return merge(ranges.take(index) + ranges.drop(index + 1), unioned)
    }
  }

  override fun part2(input: String): Long =
      parseIngredients(input)
          .first
          .fold(listOf<LongRange>()) { ranges, el -> merge(ranges, el) }
          .sumOf { it.last - it.first + 1 }

  @Test
  fun testExample() {
    """
      3-5
      10-14
      16-20
      12-18

      1
      5
      8
      11
      17
      32
    """
        .trimIndent()
        .let { input ->
          assertEquals(3, part1(input))
          assertEquals(14L, part2(input))
        }
  }
}
