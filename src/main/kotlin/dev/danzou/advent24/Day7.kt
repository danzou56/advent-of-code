package dev.danzou.advent24

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day7 : AdventTestRunner24("") {

  fun getEquations(input: String): List<Pair<Long, List<Long>>> =
      input
          .lines()
          .map { it.split(": ") }
          .map { (test, ops) -> test.toLong() to (ops.split(" ").map(String::toLong)) }

  fun isSolvable(
      target: Long,
      nums: List<Long>,
      ops: List<(Long, Long) -> Long>
  ): Boolean {
    fun step(cur: Long, rest: List<Long>): Boolean {
      if (rest.isEmpty()) return cur == target
      if (cur > target) return false

      val next = rest.subList(1, rest.size)
      return ops.any { step(it(cur, rest.first()), next) }
    }

    return step(nums.first(), nums.subList(1, nums.size))
  }

  override fun part1(input: String): Long {
    val parsed = getEquations(input)
    val ops: List<(Long, Long) -> Long> = listOf(Long::plus, Long::times)

    return parsed
        .filter { (target, nums) -> isSolvable(target, nums, ops) }
        .sumOf { (target, _) -> target }
  }

  override fun part2(input: String): Long {
    val parsed = getEquations(input)
    val ops: List<(Long, Long) -> Long> =
        listOf(Long::plus, Long::times, { l1, l2 -> "$l1$l2".toLong() })

    return parsed
        .filter { (target, nums) -> isSolvable(target, nums, ops) }
        .sumOf { (target, _) -> target }
  }

  @Test
  fun testExample() {
    """
      190: 10 19
      3267: 81 40 27
      83: 17 5
      156: 15 6
      7290: 6 8 6 15
      161011: 16 10 13
      192: 17 8 14
      21037: 9 7 18 13
      292: 11 6 16 20
    """
        .trimIndent()
        .let { input ->
          assertEquals(3749, part1(input))
          assertEquals(11387, part2(input))
        }
  }
}
