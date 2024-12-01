package dev.danzou.advent24.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent24.AdventTestRunner24
import java.lang.Math.abs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day1 : AdventTestRunner24("") {
  override fun part1(input: String): Any {
    val (l1, l2) =
        input
            .lines()
            .map { it.split("\\s+".toRegex()) }
            .map { (f, s) -> f.toInt() to s.toInt() }
            .unzip()
    return (l1.sorted() to l2.sorted())
        .let { (l1, l2) -> l1.zip(l2).map { (i1, i2) -> abs(i1 - i2) } }
        .sum()
  }

  override fun part2(input: String): Any {
    val (l1, l2) =
        input
            .lines()
            .map { it.split("\\s+".toRegex()) }
            .map { (f, s) -> f.toInt() to s.toInt() }
            .unzip()
    val counts = l2.frequencyMap()
    return l1.map { it * (counts[it] ?: 0) }.sum()
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
