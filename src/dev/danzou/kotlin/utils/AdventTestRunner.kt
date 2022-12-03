package dev.danzou.kotlin.utils

import dev.danzou.utils.Utils.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

abstract class AdventTestRunner {
    private val inputLines: List<String> = readInputLines()
    private val inputString: String = readInputString()
    private val expected: List<Long?> = readOutputLines()

    abstract fun part1(
        input: String = this.inputString,
    ): Number

    abstract fun part2(
        input: String = this.inputString,
    ): Number

    @Test
    fun testPart1() {
        val part1 = part1().toLongAndWarn()
        println(part1)
        assertEquals(expected.component1(), part1)
    }

    @Test
    fun testPart2() {
        val part2 = part2().toLongAndWarn()
        println(part2)
        assertEquals(expected.component2(), part2)
    }

    fun Number.toLongAndWarn(): Long {
        if (this !is Long) {
            println("Your implementation didn't use Long! Are you sure you didn't over/underflow?")
        }
        return this.toLong()
    }
}