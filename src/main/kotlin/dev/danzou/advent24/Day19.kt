package dev.danzou.advent24

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day19 : AdventTestRunner24("Linen Layout") {

  fun getPatterns(input: String): List<String> = input.lines().first().split(", ")

  fun getDesigns(input: String): List<String> = input.lines().drop(2)

  override fun part1(input: String): Int {
    val patterns = getPatterns(input)
    val designs = getDesigns(input)

    fun isPossible(
        design: String,
        patterns: List<String>,
        cache: MutableMap<String, Boolean>
    ): Boolean {
      if (design == "") return true
      if (design in cache) return cache[design]!!

      return patterns
          .filter { design.startsWith(it) }
          .any { prefix -> isPossible(design.substring(prefix.length), patterns, cache) }
          .also { cache[design] = it }
    }

    val cache = mutableMapOf<String, Boolean>()
    return designs.count { isPossible(it, patterns, cache) && it != "" }
  }

  override fun part2(input: String): Long {
    val patterns = getPatterns(input)
    val designs = getDesigns(input)

    fun countArrangements(
        design: String,
        patterns: List<String>,
        cache: MutableMap<String, Long>
    ): Long {
      if (design == "") return 1
      if (design in cache) return cache[design]!!

      return patterns
          .filter { design.startsWith(it) }
          .sumOf { prefix ->
            countArrangements(design.substring(prefix.length), patterns, cache)
          }
          .also { cache[design] = it }
    }

    val cache = mutableMapOf<String, Long>()
    return designs.sumOf { countArrangements(it, patterns, cache).toLong() }
  }

  @Test
  fun testExample() {
    """
      r, wr, b, g, bwu, rb, gb, br

      brwrr
      bggr
      gbbr
      rrbgbr
      ubwu
      bwurrg
      brgr
      bbrgwb
    """
        .trimIndent()
        .let { input ->
          assertEquals(6, part1(input))
          assertEquals(16L, part2(input))
        }
  }
}
