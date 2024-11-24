package dev.danzou.advent20.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent20.AdventTestRunner20
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day6 : AdventTestRunner20("") {
  override fun part1(input: String): Any {
    return input.split("\n\n").sumOf { group: String ->
      group.toSet().count { it.isLetter() }
    }
  }

  override fun part2(input: String): Any {
    return input.split("\n\n")
      .sumOf {
        it.split("\n").map { it.toSet() }.reduce(Set<Char>::intersect).size
      }
  }
  
  @Test
  fun testExample() {
    val input = 
        """
          abc

          a
          b
          c

          ab
          ac

          a
          a
          a
          a

          b
        """
            .trimIndent()

    assertEquals(11, part1(input))
    assertEquals(6, part2(input))
  }
}