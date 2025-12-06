package dev.danzou.advent25

import dev.danzou.advent.utils.*
import kotlin.text.split
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day1 : AdventTestRunner25("Secret Entrance") {
  val size = 100
  val start = 50

  private fun parse(input: String): List<Int> =
      input.split("\n").map {
        when (it.take(1)) {
          "L" -> -1
          else -> 1
        } * it.drop(1).toInt()
      }

  override fun part1(input: String): Int =
      parse(input)
          .fold(Pair(start, 0)) { (pos, pass), rot ->
            val next = (pos + rot).mod(size)
            if (next == 0) Pair(next, pass + 1) else Pair(next, pass)
          }
          .second

  override fun part2(input: String): Int =
      parse(input)
          .fold(Pair(start, 0)) { (pos, pass), rot ->
            val zeroes =
                if (rot > 0) {
                  (pos + rot) / size
                } else {
                  (size - pos - rot) / size + if (pos == 0) -1 else 0
                }

            val next = (pos + rot).mod(size)
            Pair(next, pass + zeroes)
          }
          .second

  @Test
  fun testExample() {
    """
      L68
      L30
      R48
      L5
      R60
      L55
      L1
      L99
      R14
      L82
    """
        .trimIndent()
        .let { input ->
          assertEquals(3, part1(input))
          assertEquals(6, part2(input))
        }
  }
}
