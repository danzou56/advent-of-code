package dev.danzou.advent24

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day11 : AdventTestRunner24("Plutonian Pebbles") {

  data class Stone(val id: Long) {
    fun blink(): List<Stone> =
        id.toString().let { sId ->
          when {
            sId == "0" -> listOf(Stone(1))
            sId.length % 2 != 0 -> listOf(Stone(id * 2024))
            else -> {
              listOf(
                  Stone(sId.take(sId.length / 2).toLong()),
                  Stone(sId.drop(sId.length / 2).toLong()),
              )
            }
          }
        }
  }

  tailrec fun blink(stones: Map<Stone, Long>, times: Int): Map<Stone, Long> {
    if (times == 0) return stones

    val next = mutableMapOf<Stone, Long>()
    stones
        .flatMap { (stone, count) -> stone.blink().map { s -> s to count } }
        .forEach { (stone, count) -> next[stone] = (next[stone] ?: 0) + count }
    return blink(next, times - 1)
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
