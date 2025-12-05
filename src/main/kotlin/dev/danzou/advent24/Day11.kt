package dev.danzou.advent24

import dev.danzou.advent.utils.frequencyMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day11 : AdventTestRunner24("Plutonian Pebbles") {

  data class Stone(val id: Long) {
    fun blink(): List<Stone> {
      return if (id == 0L) OneStone
      else if (id.toString().length % 2 == 0) {
        val digits = id.toString()
        listOf(
            Stone(digits.take(digits.length / 2).toLong()),
            Stone(digits.drop(digits.length / 2).toLong()))
      } else listOf(Stone(id * 2024))
    }

    companion object {
      val OneStone = listOf(Stone(1))
    }
  }

  fun blink(stones: Map<Stone, Long>, times: Int): Map<Stone, Long> {

    val mut = stones.toMutableMap()

    tailrec fun step(stones: Map<Stone, Long>, times: Int): Map<Stone, Long> {
      if (times == 0) return stones

      val next =
          stones
              .map { (stone, count) ->
                stone.blink().frequencyMap().mapValues { (_, v) -> v * count }
              }
              .fold(emptyMap<Stone, Long>()) { acc, cur ->
                (acc.keys + cur.keys).associateWith { (acc[it] ?: 0) + (cur[it] ?: 0) }
              }
      return step(next, times - 1)
    }

    return step(stones, times)
  }

  override fun part1(input: String): Long {
    val stones = input.split(" ").map { it.toLong() }.associate { Stone(it) to 1L }
    return blink(stones, 25).values.sum()
  }

  override fun part2(input: String): Long {
    val stones = input.split(" ").map { it.toLong() }.associate { Stone(it) to 1L }
    return blink(stones, 75).values.sum()
  }

  @Test
  fun testExample() {
    """
      125 17
    """
        .trimIndent()
        .let { input -> assertEquals(55312L, part1(input)) }
  }
}
