package dev.danzou.advent20

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day13 : AdventTestRunner20("Shuttle Search") {
  override fun part1(input: String): Int {
    val (earliest, buses) =
        input.lines().let { (first, second) ->
          val earliest = first.toInt()
          val buses =
              second.split(",").mapNotNull { it.takeIf { it != "x" } }.map(String::toInt)
          earliest to buses
        }

    return buses
        .minBy { interval -> -earliest.mod(interval) + interval }
        .let { minBusId -> minBusId * (-earliest.mod(minBusId) + minBusId) }
  }

  override fun part2(input: String): Long {
    val buses =
        input.lines().last().split(",").mapIndexedNotNull { i, el ->
          if (el != "x") i.toLong() to el.toLong() else null
        }

    tailrec fun solve(offset1: Long, mod1: Long, offset2: Long, mod2: Long): Long {
      /*
       * Solves the system { x + offset1 === 0 (mod mod1);
       *                     x + offset2 === 0 (mod mod2) } for x
       * All arguments should be positive
       */
      return if ((offset1 + offset2).mod(mod2) == 0L) offset1
      else solve(offset1 + mod1, mod1, offset2, mod2)
    }

    return buses
        .drop(1)
        .fold(0L to buses.first().second) { (prevOffset, prevMod), (curOffset, curMod) ->
          val nextMod = prevMod * curMod
          Pair(solve(prevOffset, prevMod, curOffset, curMod).mod(nextMod), nextMod)
        }
        .let { (finalOffset, finalMod) -> finalOffset.mod(finalMod) }
  }

  @Test
  fun testExample() {
    val input =
        """
          939
          7,13,x,x,59,x,31,19
        """
            .trimIndent()

    assertEquals(295, part1(input))
    assertEquals(1068781, part2(input))
  }
}
