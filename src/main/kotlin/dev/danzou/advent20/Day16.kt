package dev.danzou.advent20

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day16 : AdventTestRunner20("Ticket Translation") {
  fun parseFields(input: String): Map<String, List<IntRange>> =
      input
          .lines()
          .takeWhile { it != "" }
          .associate {
            Pair(
                it.takeWhile { it != ':' },
                it.split(" or ")
                    .map { Regex(".*?(\\d+)-(\\d+).*?").matchEntire(it)!!.destructured }
                    .map { (l, h) -> l.toInt()..h.toInt() },
            )
          }

  fun parseMyTicket(input: String): List<Int> =
      input
          .lines()
          .dropWhile { it != "your ticket:" }
          .drop(1)
          .first()
          .split(",")
          .map(String::toInt)

  fun parseNearbyTickets(input: String): List<List<Int>> =
      input
          .lines()
          .dropWhile { it != "nearby tickets:" }
          .drop(1)
          .map { it.split(",").map(String::toInt) }

  override fun part1(input: String): Int {
    val ranges = parseFields(input).values.flatten()
    return parseNearbyTickets(input)
        .flatten()
        .filter { field -> ranges.all { range -> field !in range } }
        .sum()
  }

  override fun part2(input: String): Any {
    val fields = parseFields(input)
    val validRanges = fields.values.flatten()
    val validTickets =
        parseNearbyTickets(input).filter { ticket ->
          ticket.all { field -> validRanges.any { range -> field in range } }
        }

    val fieldMapping = resolveFieldMappings(fields, validTickets)

    val myTicket = parseMyTicket(input)
    return fieldMapping.entries
        .filter { (_, field) -> field.startsWith("departure") }
        .map { (index, _) -> myTicket[index].toLong() }
        .reduce(Long::times)
  }

  fun resolveFieldMappings(
      fields: Map<String, List<IntRange>>,
      validTickets: List<List<Int>>,
  ): Map<Int, String> {
    fun step(
        knownMappings: Map<Int, String>,
        unknownIndices: Set<Int>,
        unknownFields: Set<String>,
    ): Map<Int, String> {
      if (unknownIndices.isEmpty()) return knownMappings

      val next =
          unknownIndices.firstNotNullOf { workingIndex ->
            val candidates =
                unknownFields.filter { field ->
                  val ranges = fields[field]!!
                  validTickets.all { ticket ->
                    ranges.any { range -> ticket[workingIndex] in range }
                  }
                }
            if (candidates.size == 1) workingIndex to candidates.first() else null
          }

      return step(
          knownMappings + (next.first to next.second),
          unknownIndices - next.first,
          unknownFields - next.second,
      )
    }

    return step(
        emptyMap(),
        (0..<fields.count()).toSet(),
        fields.keys,
    )
  }

  @Test
  fun testExample() {
    """
      class: 1-3 or 5-7
      row: 6-11 or 33-44
      seat: 13-40 or 45-50

      your ticket:
      7,1,14

      nearby tickets:
      7,3,47
      40,4,50
      55,2,20
      38,6,12
    """
        .trimIndent()
        .let { input -> assertEquals(71, part1(input)) }

    """
      class: 0-1 or 4-19
      row: 0-5 or 8-19
      seat: 0-13 or 16-19
      
      your ticket:
      11,12,13
      
      nearby tickets:
      3,9,18
      15,1,5
      5,14,9
    """
        .trimIndent()
        .let { input ->
          val fields = parseFields(input)
          val nearbyTickets = parseNearbyTickets(input)
          assertEquals(
              mapOf(0 to "row", 1 to "class", 2 to "seat"),
              resolveFieldMappings(fields, nearbyTickets),
          )
        }
  }
}
