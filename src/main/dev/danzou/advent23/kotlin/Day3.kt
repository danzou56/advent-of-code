package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass.Companion.ALL_DIRECTIONS
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day3 : AdventTestRunner23("Gear Ratios") {

    private fun getPartMatchesByRow(input: String): Map<Int, Sequence<MatchResult>> =
        input.split("\n")
            .mapIndexed { y, line -> y to Regex("\\d+").findAll(line) }
            .toMap()

    override fun part1(input: String): Int {
        val schematic = input.split("\n").map { it.toList() }
        val partMatchesByRow = getPartMatchesByRow(input)
        return partMatchesByRow.flatMap { (y, matches) ->
            matches.map { y to it }
        }.sumOf { (y, partMatch) ->
            val surrounding = partMatch.range.flatMap { x ->
                ALL_DIRECTIONS.map { dir ->
                    Pos(x, y) + dir
                }
            }.toSet()

            if (surrounding.any {
                    when (schematic.getOrNull(it)) {
                        in '0'..'9' -> false
                        '.' -> false
                        null -> false // invalid coordinate
                        else -> true
                    }
                }) partMatch.value.toInt()
            else 0
        }
    }

    override fun part2(input: String): Int {
        val gearMatchesWithIndex = input.split("\n")
            .map { line -> Regex("\\*").findAll(line).toList() }
            .mapIndexed { y, matches -> matches.map { it to y } }
            .flatten()
        val partMatchesByRow = getPartMatchesByRow(input)
        val offsetGroups = (-1..1).map { y -> y to (-1..1) }
        // Given a list of gear match results (and their row index),
        // 1. Find all part match results in adjacent rows
        // 2. Keep only those that are also in an adjacent column
        // 3. Keep only match results when there are at least two of them
        // 4. Sum together the gear ratios
        return gearMatchesWithIndex.mapNotNull { (gearMatch, y) ->
            offsetGroups.flatMap { (yOffset, xOffsets) ->
                partMatchesByRow[y + yOffset]?.filter { partMatch ->
                    xOffsets.any { xOffset ->
                        gearMatch.range.first + xOffset in partMatch.range
                    }
                } ?: emptySequence()
            }.takeIf { it.size >= 2 }
        }.sumOf { partMatches ->
            partMatches.map { it.value.toInt() }.reduce(Int::times)
        }
    }

    @Test
    fun testExample() {
        val input = """
            467..114..
            ...*......
            ..35..633.
            ......#...
            617*......
            .....+.58.
            ..592.....
            ......755.
            ...${'$'}.*....
            .664.598..
        """.trimIndent()

        assertEquals(4361, part1(input))
        assertEquals(467835, part2(input))
    }
}