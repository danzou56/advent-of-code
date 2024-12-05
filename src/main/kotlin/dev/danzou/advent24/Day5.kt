package dev.danzou.advent24

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day5 : AdventTestRunner24("Print Queue") {
  fun parseRules(input: String): Map<Pair<Int, Int>, Int> =
      input
          .lines()
          .takeWhile { it != "" }
          .map { it.split("|").map(String::toInt) }
          .flatMap { (i1, i2) ->
            listOf(
                (i1 to i2) to -1,
                (i2 to i1) to 1,
            )
          }
          .toMap()

  fun parseUpdates(input: String): List<List<Int>> =
      input
          .lines()
          .dropWhile { it != "" }
          .drop(1)
          .map { it.split(",").map(String::toInt) }

  val ruleSort: (Map<Pair<Int, Int>, Int>) -> ((Int, Int) -> Int) = { rules ->
    { i1, i2 -> rules[i1 to i2]!! }
  }

  override fun part1(input: String): Any {
    val orderingRules = parseRules(input)
    val updates = parseUpdates(input)
    return updates
        .filter { it.sortedWith(ruleSort(orderingRules)) == it }
        .sumOf { it[it.size / 2] }
  }

  override fun part2(input: String): Any {
    val orderingRules = parseRules(input)
    val updates = parseUpdates(input)
    return updates
        .mapNotNull {
          it.sortedWith(ruleSort(orderingRules)).takeIf { sorted -> sorted != it }
        }
        .sumOf { it[it.size / 2] }
  }

  @Test
  fun testExample() {
    """
      47|53
      97|13
      97|61
      97|47
      75|29
      61|13
      75|53
      29|13
      97|29
      53|29
      61|53
      97|53
      61|29
      47|13
      75|47
      97|75
      47|61
      75|61
      47|29
      75|13
      53|13

      75,47,61,53,29
      97,61,53,29,13
      75,29,13
      75,97,47,61,53
      61,13,29
      97,13,75,29,47
    """
        .trimIndent()
        .let { input ->
          assertEquals(143, part1(input))
          assertEquals(123, part2(input))
        }
  }
}
