package dev.danzou.advent24

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day3 : AdventTestRunner24("") {

  override fun part1(input: String): Any {
    return Regex("mul\\((\\d+),(\\d+)\\)")
        .findAll(input)
        .map(MatchResult::destructured)
        .map { (l1, l2) -> l1.toLong() * l2.toLong() }
        .sum()
  }

  override fun part2(input: String): Any {
    val mulRegex = Regex("mul\\((\\d+),(\\d+)\\)")
    val doRegex = Regex("do\\(\\)")
    val dontRegex = Regex("don't\\(\\)")

    tailrec fun process(s: String, sum: Long): Long {
      val mulI = mulRegex.find(s)?.range?.first ?: Int.MAX_VALUE
      val doI = doRegex.find(s)?.range?.first ?: Int.MAX_VALUE
      val dontI = dontRegex.find(s)?.range?.first ?: Int.MAX_VALUE

      return when (minOf(mulI, dontI, doI)) {
        Int.MAX_VALUE -> sum
        mulI -> {
          val (l1, l2) =
              mulRegex.find(s)!!.destructured.let { (s1, s2) ->
                s1.toLong() to s2.toLong()
              }
          process(
              s.drop(mulI + "mul($l1,$l2)".length),
              sum + l1 * l2,
          )
        }
        dontI ->
            when (val nextDoI = doRegex.find(s)?.range?.first) {
              // No do() following this don't() - we can ignore the rest of the string
              null -> sum
              // Skip everything until the next do()
              else -> process(s.drop(nextDoI + "do()".length), sum)
            }
        doI -> process(s.drop(doI + "do()".length), sum)
        else -> throw IllegalArgumentException()
      }
    }

    return process(input, 0)
  }

  @Test
  fun testExample() {
    """
      xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))
    """
        .trimIndent()
        .let { input -> assertEquals(48L, part2(input)) }
  }
}
