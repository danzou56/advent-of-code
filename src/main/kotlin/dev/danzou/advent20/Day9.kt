package dev.danzou.advent20

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day9 : AdventTestRunner20("Encoding Error") {

  override fun part1(input: String): Long = part1(input, 25)

  fun part1(input: String, preambleSize: Int): Long {
    return input
        .lines()
        .map(String::toLong)
        .windowed(preambleSize + 1, 1)
        .find { lst ->
          (lst.take(preambleSize).sorted() to lst.last()).let { (prev, cur) ->
            if (cur > prev.takeLast(2).sum()) return@let true
            if (cur < prev.take(2).sum()) return@let true
            var i = 0
            var j = prev.size - 1

            while (i in prev.indices && j in prev.indices) {
              if (prev[i] + prev[j] == cur) return@let false
              else if (prev[i] + prev[j] > cur) j-- else if (prev[i] + prev[j] < cur) i++
            }
            return@let true
          }
        }!!
        .last()
  }

  override fun part2(input: String): Long {
    return part2(input, part1(input))
  }

  fun part2(input: String, target: Long): Long {
    val nums = input.lines().map(String::toLong)

    var i = 0
    var j = 0
    var sum = nums[i]
    while (i in nums.indices && j in nums.indices) {
      if (i == j) {
        j++
        sum += nums[j]
      }

      if (sum == target) {
        return nums.slice(i..j).run { min() + max() }
      } else if (sum > target) {
        sum -= nums[i]
        i++
      } else { // Always sum < target
        j++
        sum += nums[j]
      }
    }
    throw IllegalArgumentException()
  }

  @Test
  fun testExample() {
    val input =
        """
          35
          20
          15
          25
          47
          40
          62
          55
          65
          95
          102
          117
          150
          182
          127
          219
          299
          277
          309
          576
        """
            .trimIndent()

    assertEquals(127, part1(input, 5))
    assertEquals(62, part2(input, part1(input, 5)))
  }
}
