package dev.danzou.advent25

import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day2 : AdventTestRunner25("Gift Shop") {
  private fun parse(input: String): List<LongRange> =
      input
          .split(",")
          .map { it.split("-").map(String::toLong) }
          .map { it.first()..it.last() }

  fun isRepeatedTwice(id: String): Boolean =
      id.length.mod(2) == 0 && id.take(id.length / 2) == id.drop(id.length / 2)

  fun isRepeatedAtLeastTwice(id: String): Boolean {
    tailrec fun isRepeated(sub: String, offset: Int = 0): Boolean =
      if (offset == id.length) true
      else
        id.length.mod(sub.length) == 0 &&
            id.substring(offset).startsWith(sub) &&
            isRepeated(sub, offset + sub.length)

    return (1..(id.length / 2)).any { chars -> isRepeated(id.take(chars)) }
  }

  override fun part1(input: String): Long =
    parse(input).flatMap { it.filter { isRepeatedTwice(it.toString()) } }.sum()

  override fun part2(input: String): Long =
      parse(input).flatMap { it.filter { isRepeatedAtLeastTwice(it.toString()) } }.sum()

  @Test
  fun testExample() {
    """
      11-22,95-115,998-1012,1188511880-1188511890,222220-222224,1698522-1698528,446443-446449,38593856-38593862,565653-565659,824824821-824824827,2121212118-2121212124
    """
        .trimIndent()
        .let { input ->
          assertEquals(1227775554L, part1(input))
          assertEquals(4174379265L, part2(input))
        }
  }

  @Test
  fun testIsRepeatedTwice() {
    assertTrue(isRepeatedTwice(""))
    assertFalse(isRepeatedTwice("f"))
    assertTrue(isRepeatedTwice("fdfd"))
  }

  @Test
  fun testIsRepeatedAtLeastTwice() {
    assertFalse(isRepeatedAtLeastTwice("1121212"))
    assertTrue(isRepeatedAtLeastTwice("12121212"))
  }
}
