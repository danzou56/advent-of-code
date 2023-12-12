package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day12 : AdventTestRunner23() {
    val UNKNOWN = '?'
    val BROKEN = '#'
    val OPERATIONAL = '.'

    enum class Condition {
        UNKNOWN, BROKEN, OPERATIONAL
    }

    fun isValidArrangement(spring: String, sizes: List<Int>): Boolean {
        val brokenSprings = Regex("$BROKEN+").findAll(spring).toList()
        if (brokenSprings.size != sizes.size) return false
        return brokenSprings.zip(sizes).all { (broken, size) -> broken.value.length == size }
    }

    fun arrangements(spring: String, sizes: List<Int>): List<String> {
        val cache = mutableMapOf<Pair<String, List<Int>>, List<String>>()

        fun genArrangement(spring: String, sizes: List<Int>, cur: String): List<String> {
//            if (spring.length < sizes.size) return emptyList()
            if (spring.isEmpty() && sizes.isEmpty()) return listOf(cur)
            if (spring.isEmpty() && sizes.isNotEmpty()) return emptyList()

            if (spring.first() == OPERATIONAL) return genArrangement(spring.drop(1), sizes, cur + OPERATIONAL)
            if (spring.first() == BROKEN) {
                if (sizes.isEmpty()) return emptyList()
                val nextBrokenOrUnknown = spring.takeWhile { it == BROKEN || it == UNKNOWN }
                if (sizes.first() > nextBrokenOrUnknown.length) return emptyList()

                val remaining = spring.drop(sizes.first())
                if (remaining.isEmpty() && sizes.size == 1) return listOf(cur + "$BROKEN".repeat(sizes.first()))
                if (remaining.isEmpty()) return emptyList()
                if (remaining.first() == BROKEN) return emptyList()
                return genArrangement(
                    spring.drop(sizes.first() + 1),
                    sizes.drop(1),
                    cur + "$BROKEN".repeat(sizes.first()) + OPERATIONAL
                )
            }
            if (spring.first() == UNKNOWN) {
                return genArrangement(
                    OPERATIONAL + spring.drop(1),
                    sizes,
                    cur
                ) + genArrangement(
                    BROKEN + spring.drop(1),
                    sizes,
                    cur
                )
            }
            throw IllegalArgumentException(spring.first().toString())
        }

        return genArrangement(spring, sizes, "")
    }

    override fun part1(input: String): Any {
        val (springStrs, sizes) = input.split("\n")
            .map { it.split(" ") }
            .map { (spring, sizes) ->
                spring to sizes.split(",").map { it.toInt() }
            }
            .unzip()

        return springStrs.zip(sizes).map { (spring, sizes) ->
            arrangements(spring, sizes).also {
//                println("${it.size}: $it")
            }
        }.sumOf { it.size }
    }

    override fun part2(input: String): Any {
        val (springStrs, sizes) = input.split("\n")
            .map { it.split(" ") }
            .map { (spring, sizes) ->
                (0..<5).map { spring }.joinToString(UNKNOWN.toString()) to (0..<5).flatMap {
                    sizes.split(",").map { it.toInt() } }
            }
            .unzip()

        return springStrs.zip(sizes).map { (spring, sizes) ->
            arrangements(spring, sizes).also {
                println("${it.size}")
            }
        }.sumOf { it.size.toLong() }
    }

    @Test
    fun testExample() {
        val input = """
            ???.### 1,1,3
            .??..??...?##. 1,1,3
            ?#?#?#?#?#?#?#? 1,3,1,6
            ????.#...#... 4,1,1
            ????.######..#####. 1,6,5
            ?###???????? 3,2,1
        """.trimIndent()

//        assertEquals(10, arrangements("?###????????", listOf(3, 2, 1)).size)
//        assertEquals(21, part1(input))
        assertEquals(525152L, part2(input))
    }
}