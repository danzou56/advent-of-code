package dev.danzou.advent20

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day15 : AdventTestRunner20("Rambunctious Recitation") {
  override fun part1(input: String): Long {
    return part1(input.split(",").map(String::toLong), 2020L)
  }

  fun part1(start: List<Long>, halt: Long): Long {
    val seen =
        start
            // last seen, last diff
            .mapIndexed { i, n -> n to Pair<Long, Long?>(i + 1L, null) }
            .toMap()
            .toMutableMap()

    tailrec fun step(turn: Long, prev: Long): Long {
      if (turn == halt) return prev
      val next = seen.getOrPut(prev, { Pair(turn + 1, null) }).second ?: 0
      val nextDiff = if (next in seen) turn + 1 - seen[next]!!.first else null
      seen[next] = Pair(turn + 1, nextDiff)
      return step(turn + 1, next)
    }

    return step(start.size.toLong(), start.last().toLong())
  }

  override fun part2(input: String): Long {
    return part1(input.split(",").map(String::toLong), 30_000_000L)
  }

  @Test
  fun testExample() {
    """
      0,3,6
    """
        .trimIndent()
        .let { input ->
          val start = input.split(",").map(String::toLong)
          assertEquals(0, part1(start, 4))
          assertEquals(3, part1(start, 5))
          assertEquals(3, part1(start, 6))
          assertEquals(1, part1(start, 7))
          assertEquals(175594, part2(input))
        }
  }
}
