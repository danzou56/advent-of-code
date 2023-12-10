package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day9 : AdventTestRunner23() {

    fun getExtrapolatedHistories(input: String): List<List<Long>> {
        tailrec fun buildDifferenceTriangle(history: List<List<Long>>): List<List<Long>> =
            if (history.last().all { it == 0L }) history
            else buildDifferenceTriangle(
                history + listOf(history.last().windowed(2).map { (l1, l2) -> l2 - l1 })
            )

        fun extrapolateHistory(triangle: List<List<Long>>): List<List<Long>> =
            if (triangle.first().all { it == 0L })
                listOf(listOf(0L) + triangle.first() + 0L)
            else
                extrapolateHistory(triangle.drop(1)).let { extrapolation ->
                    val top = listOf(triangle.first().first() - extrapolation.first().first()) +
                            triangle.first() +
                            listOf(triangle.first().last() + extrapolation.first().last())
                    listOf(top) + extrapolation
                }

        return input.split("\n")
            .map { it.split(" ").map { it.toLong() } }
            .map(::listOf)
            .map(::buildDifferenceTriangle)
            .map(::extrapolateHistory)
            .map { it.first()}
    }

    override fun part1(input: String): Long =
        getExtrapolatedHistories(input).sumOf { it.last() }

    override fun part2(input: String): Long =
        getExtrapolatedHistories(input).sumOf { it.first() }

    @Test
    fun testExample() {
        val input = """
            0 3 6 9 12 15
            1 3 6 10 15 21
            10 13 16 21 30 45
        """.trimIndent()

        assertEquals(114L, part1(input))
    }
}