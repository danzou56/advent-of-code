package dev.danzou.advent20

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day2 : AdventTestRunner20("") {

  override fun part1(input: String): Int {
    val lines = input.lines()
    return lines.count { line ->
      val (policy, password) = line.split(": ")
      val (min, max, letter) = policy.split("-", " ")
      password.count { it.toString() == letter } in min.toInt()..max.toInt()
    }
  }

  override fun part2(input: String): Int {
    val lines = input.lines()
    return lines.count { line ->
      val (policy, password) = line.split(": ")
      val (min, max, letter) = policy.split("-", " ")
      (password[min.toInt() - 1].toString() == letter) xor
          (password[max.toInt() - 1].toString() == letter)
    }
  }

  @Test
  fun testExample() {
    val input =
        """
            1-3 a: abcde
            1-3 b: cdefg
            2-9 c: ccccccccc
        """
            .trimIndent()

    assertEquals(2, part1(input))
    //        assertEquals(null, part2(input))
  }
}
