package dev.danzou.advent20

import dev.danzou.advent.utils.pairs

internal class Day1 : AdventTestRunner20("Report Repair") {

  override fun part1(input: String): Any {
    return input
        .split("\n")
        .map(String::toInt)
        .pairs()
        .find { (a1, a2) -> a1 + a2 == 2020 }!!
        .reduce(Int::times)
  }

  override fun part2(input: String): Long {
    val nums = input.split("\n").map { it.toLong() }
    val pairs = nums.pairs()

    return pairs
        .filter { it.sum() < 2020 }
        .firstNotNullOf { (i1, i2) ->
          val n = nums.find { n -> i1 + i2 + n == 2020L }
          when (n) {
            null -> null
            else -> listOf(i1, i2, n)
          }
        }
        .reduce(Long::times)
  }
}
