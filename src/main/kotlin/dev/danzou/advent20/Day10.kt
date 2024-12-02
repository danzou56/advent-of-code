package dev.danzou.advent20

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day10 : AdventTestRunner20("Adapter Array") {
  override fun part1(input: String): Int {
    val adapters = input.lines().map(String::toInt).sorted()
    val builtInJoltage = adapters.max() + 3
    return (listOf(0) + adapters + builtInJoltage)
        .windowed(2, 1)
        .map { (i1, i2) -> i2 - i1 }
        .onEach { assert(it <= 3) }
        .groupBy { it }
        .mapValues { it.value.size }
        .let { m -> m[1]!! * m[3]!! }
  }

  override fun part2(input: String): Long {
    val adapters = input.lines().map(String::toInt).sorted()

    val builtInJoltage = adapters.max() + 3
    val cache = mutableMapOf<List<Int>, Long>(listOf(builtInJoltage) to 1)

    fun arrangements(list: List<Int>): Long {
      if (list in cache) return cache[list]!!

      var total = arrangements(list.drop(1))
      if (list.size >= 3 && list[2] - list[0] <= 3) total += arrangements(list.drop(2))
      if (list.size >= 4 && list[3] - list[0] <= 3) total += arrangements(list.drop(3))
      return total.also { cache[list] = total }
    }

    return arrangements(listOf(0) + adapters + builtInJoltage)
  }

  @Test
  fun testSmallExample() {
    val input =
        """
          16
          10
          15
          5
          1
          11
          7
          19
          6
          12
          4
        """
            .trimIndent()

    assertEquals(8, part2(input))
  }

  @Test
  fun testLargeExample() {
    val input =
        """
          28
          33
          18
          42
          31
          14
          46
          20
          48
          47
          24
          23
          49
          45
          19
          38
          39
          11
          1
          32
          25
          35
          8
          17
          7
          9
          4
          2
          34
          10
          3
        """
            .trimIndent()

    assertEquals(220, part1(input))
    assertEquals(19208, part2(input))
  }
}
