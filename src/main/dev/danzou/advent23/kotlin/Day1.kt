package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day1 : AdventTestRunner23() {
    override fun part1(input: String): Any {
        return input.split("\n")
            .sumOf { (it.find { it.isDigit() }.toString() + it.findLast { it.isDigit() }).toInt()}
    }

    val nums = """
        one, two, three, four, five, six, seven, eight, nine
    """.trimIndent().split(", ").mapIndexed { i, s -> s to i + 1 }.toMap()

    override fun part2(input: String): Any {
        return input.split("\n")
            .sumOf { line ->
                val firstIndex = (line.indexOfAny(
                nums.keys + nums.values.map { it.toString() }
                ))
                val firstFound: Int = (nums.keys + nums.values.map { it.toString() }).first { it ->
                    try {line.substring(firstIndex, firstIndex + it.length) == it }
                    catch (e: StringIndexOutOfBoundsException) { false }
                }.let {
                    if (it in nums.keys) nums[it]!!
                    else it.toInt()
                }
                val lastIndex = (line.lastIndexOfAny(
                    nums.keys + nums.values.map { it.toString() }
                ))
                val lastFound: Int = (nums.keys + nums.values.map { it.toString() }).first { it ->
                    try {line.substring(lastIndex, lastIndex + it.length) == it }
                    catch (e: StringIndexOutOfBoundsException) { false }
                }.let {
                    if (it in nums.keys) nums[it]!!
                    else it.toInt()
                }
                (firstFound.toString() + lastFound.toString()).toInt()
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