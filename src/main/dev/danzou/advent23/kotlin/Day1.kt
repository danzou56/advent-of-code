package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day1 : AdventTestRunner23("Trebuchet?!") {
    override fun part1(input: String): Any = input.split("\n")
        .sumOf { "${it.find(Char::isDigit)}${it.findLast(Char::isDigit)}".toInt() }

    override fun part2(input: String): Any {
        // there's spaces here cuz i literally directly copy pasted it from aoc
        val nums = """
            one, two, three, four, five, six, seven, eight, nine
        """.trimIndent()
            .split(", ")
            .mapIndexed { i, v -> v to i + 1 }
            .toMap() + (1..9).associateBy { it.toString() }

        return input.split("\n")
            .sumOf { line ->
                val first = line.indexOfAny(nums.keys).let { index ->
                    nums.keys.first { line.drop(index).startsWith(it) }
                }.let { nums[it]!! }
                val last = line.lastIndexOfAny(nums.keys).let { index ->
                    nums.keys.first { line.drop(index).startsWith(it) }
                }.let { nums[it]!! }
                "$first$last".toInt()
            }
    }

    @Test
    fun testExample() {
        val input = """
            two1nine
            eightwothree
            abcone2threexyz
            xtwone3four
            4nineeightseven2
            zoneight234
            7pqrstsixteen
        """.trimIndent()

        assertEquals(281, part2(input))
    }


}