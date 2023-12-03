package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.Matrix
import dev.danzou.advent.utils.get
import dev.danzou.advent.utils.geometry.Compass.Companion.ALL_DIRECTIONS
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day3 : AdventTestRunner23() {
    override fun part1(input: String): Any {
        val mat: Matrix<Char> = input.split("\n").map { it.toList() }
        return input.split("\n")
            .map { Regex("\\d+").findAll(it) }
            .mapIndexed { y, it ->
                it.mapNotNull { matchResult ->
                    val xs = matchResult.range
                    val num = matchResult.value.toInt()
                    if (ALL_DIRECTIONS.any { dir ->
                            xs.any { x ->
                                try {
                                    val dirToLook = Pair(x, y) + dir
                                    val c = mat[dirToLook.second][dirToLook.first]
                                    !c.isDigit() && c != '.'
                                } catch (e: IndexOutOfBoundsException) {
                                    false
                                }
                            }
                        }) num
                    else null
                }.sum()
            }.sum()
    }

    override fun part2(input: String): Any {
        val mat: Matrix<Char> = input.split("\n").map { it.toList() }

        return input.split("\n")
            .map { Regex("\\*").findAll(it) }
            .mapIndexed { y, it ->
                it.mapNotNull { matchResult ->
                    val xs = matchResult.range.first
                    val surroundingNums = ALL_DIRECTIONS.mapNotNull all@{ dir ->
                        val dirToLook = Pair(xs, y) + dir
                        val c = mat[dirToLook.second][dirToLook.first]
                        if (c.isDigit()) {
                            val matches = Regex("\\d+").findAll(mat[dirToLook.second].joinToString(""))
                            val match = matches.firstOrNull { dirToLook.first in it.range } ?: return@all null
                            Pair(match.range.first, match.value.toInt())
                        } else {
                            null
                        }
                    }.toSet()
                    if (surroundingNums.size < 2) null
                    else surroundingNums.map{ it.second }.reduce(Int::times)
                }.sum()
            }.sum()
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