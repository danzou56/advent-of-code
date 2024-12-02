package dev.danzou.advent22

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day25 : AdventTestRunner22() {
    
    val snafuDigitMap = mapOf(
        '2' to 2,
        '1' to 1,
        '0' to 0,
        '-' to -1,
        '=' to -2,
    )
    val digitSnafuMap = mapOf(
        2 to '2',
        1 to '1',
        0 to '0',
        -1 to '-',
        -2 to '=',
    )
    
    fun snafuToLong(snafu: String): Long {
        tailrec fun convert(cur: String, value: Long): Long {
            if (cur == "") return value
            return convert(
                cur.drop(1),
                value * 5 + snafuDigitMap.getValue(cur.first())
            )
        }
        return convert(snafu, 0L)
    }

    fun longToSnafu(num: Long): String {
        assert(num > 0)
        fun convert(cur: Long, value: String): String {
            if (cur == 0L) return value
            return convert(cur / 5, (cur % 5L).toString() + value)
        }

        val base5 = convert(num, "")

        fun incLastDigit(cur: String): String {
            if (cur == "") return "1"
            val lastDigit = snafuDigitMap.getValue(cur.last())
            return if (lastDigit + 1 in digitSnafuMap)
                cur.dropLast(1) + digitSnafuMap[lastDigit + 1]
            else
                incLastDigit(cur.dropLast(1)) + '='
        }
        return base5.fold("") { snafu, digit ->
            if (digit.digitToInt() > 2) {
                incLastDigit(snafu) + if (digit == '3') '=' else '-'
            } else {
                snafu + digit
            }
        }
    }
    
    override fun part1(input: String): String =
        input.split("\n").map { snafuToLong(it) }
            .sum().let { sum -> longToSnafu(sum) }

    override fun part2(input: String): String = "Congratulations!"

    @Test
    fun testExample() {
        val input = """
            1=-0-2
            12111
            2=0=
            21
            2=01
            111
            20012
            112
            1=-1=
            1-12
            12
            1=
            122
        """.trimIndent()

        assertEquals(
            4890,
            input.split("\n").map { snafuToLong(it) }.sum()
        )

        assertEquals(
            input.split("\n"),
            input.split("\n").map { snafuToLong(it) }.map { longToSnafu(it) }
        )
    }
}