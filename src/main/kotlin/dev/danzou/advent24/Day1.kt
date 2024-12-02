package dev.danzou.advent24

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class Day1 : AdventTestRunner24("Historian Hysteria") {
  override fun part1(input: String): Int {
    val (l1, l2) =
        input
            .lines()
            .map { it.split("\\s+".toRegex()) }
            .map { (f, s) -> f.toInt() to s.toInt() }
            .unzip()
    return (l1.sorted() to l2.sorted())
        .let { (l1, l2) -> l1.zip(l2) }
        .sumOf { (i1, i2) -> abs(i1 - i2) }
  }

  override fun part2(input: String): Int {
    val (l1, l2) =
        input
            .lines()
            .map { it.split("\\s+".toRegex()) }
            .map { (f, s) -> f.toInt() to s.toInt() }
            .unzip()
    val counts = l2.frequencyMap()
    return l1.sumOf { it * (counts[it] ?: 0) }
  }

  @Test
  fun testExample() {
    """
      3   4
      4   3
      2   5
      1   3
      3   9
      3   3
    """
        .trimIndent()
        .let { input ->
          assertEquals(11, part1(input))
          assertEquals(31, part2(input))
        }
  }
}
