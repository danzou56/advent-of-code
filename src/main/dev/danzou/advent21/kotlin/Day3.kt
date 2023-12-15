package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.transpose
import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day3 : AdventTestRunner21() {
    private fun bitsToInt(bits: List<Number>): Int =
        bits.fold(0) { num, bit -> (num shl 1) + bit.toInt() }

    override fun part1(input: String): Int =
        input.split("\n")
            .map(String::toList)
            .transpose()
            .map {
                it.map {
                    when (it) {
                        '1' -> 1
                        '0' -> -1
                        else -> throw IllegalArgumentException()
                    }
                }
            }
            .map(List<Int>::sum)
            .map {
                when {
                    it > 0 -> listOf(1, 0)
                    it < 0 -> listOf(0, 1)
                    else -> throw IllegalArgumentException()
                }
            }
            .transpose()
            .map(::bitsToInt)
            .reduce(Int::times)

    override fun part2(input: String): Int {
        return input.split("\n")
            .map { it.map { it.digitToInt() } }
            .let {
                bitsToInt(oxygenRating(it)) * bitsToInt(co2ScrubberRating(it))
            }

    }

    private fun mostCommonAt(bitStrings: List<List<Int>>, index: Int): List<List<Int>> {
        val counts = bitStrings.map { it[index] }.groupingBy { it }.eachCount()
        val mostCommonBit = when {
            counts.getOrDefault(0, 0) == counts.getOrDefault(1, 0) -> 1
            else -> counts.maxBy { it.value }.key
        }
        return bitStrings.filter { it[index] == mostCommonBit }
    }

    private fun leastCommonAt(bitStrings: List<List<Int>>, index: Int): List<List<Int>> {
        val counts = bitStrings.map { it[index] }.groupingBy { it }.eachCount()
        val leastCommonBit = when {
            counts.getOrDefault(0, 0) == counts.getOrDefault(1, 0) -> 0
            else -> counts.minBy { it.value }.key
        }
        return bitStrings.filter { it[index] == leastCommonBit }
    }

    tailrec fun oxygenRating(bitStrings: List<List<Int>>, index: Int = 0): List<Int> {
        if (bitStrings.size == 1)
            return bitStrings.first()

        return oxygenRating(mostCommonAt(bitStrings, index), index + 1)
    }

    tailrec fun co2ScrubberRating(bitStrings: List<List<Int>>, index: Int = 0): List<Int> {
        if (bitStrings.size == 1)
            return bitStrings.first()

        return co2ScrubberRating(leastCommonAt(bitStrings, index), index + 1)
    }

    @Test
    fun testExample() {
        val input = """
            00100
            11110
            10110
            10111
            10101
            01111
            00111
            11100
            10000
            11001
            00010
            01010
        """.trimIndent()

        assertEquals(230, part2(input))
    }

}