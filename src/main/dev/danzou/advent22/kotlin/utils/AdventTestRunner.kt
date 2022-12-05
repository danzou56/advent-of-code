package dev.danzou.advent22.kotlin.utils

import dev.danzou.utils.Utils.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

abstract class AdventTestRunner {
    private val inputLines: List<String> = readInputLines()
    private val inputString: String = readInputString()
    private val expected: List<String?> = readOutputLines()

    abstract fun part1(
        input: String = this.inputString,
    ): Any

    abstract fun part2(
        input: String = this.inputString,
    ): Any

    @Test
    fun testPart1() {
        val part1 = part1()
        println(part1)
        assertEquals(expected.component1(), part1.toString())
    }

    @Test
    fun testPart2() {
        val part2 = part2()
        println(part2)
        assertEquals(expected.component2(), part2.toString())
    }
}