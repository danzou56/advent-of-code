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

    fun arrangements(spring: String, sizes: List<Int>): Long {
        val cache = mutableMapOf<Pair<String, List<Int>>, Long>()

        fun genArrangement(spring: String, sizes: List<Int>): Long {
            if (spring to sizes in cache) return cache[spring to sizes]!!


            return (if (spring.length < sizes.size) 0
            else if (sizes.isEmpty() && spring.isNotEmpty()) if (spring.all { it != BROKEN }) 1 else 0
            else if (spring.isEmpty() && sizes.isEmpty()) 1
            else if (spring.isEmpty() && sizes.isNotEmpty()) 0
            else if (spring.first() == OPERATIONAL) {
                genArrangement(spring.drop(1), sizes)
            } else if (spring.first() == BROKEN) {
                if (sizes.isEmpty()) return 0
                val nextBrokenOrUnknown = spring.takeWhile { it == BROKEN || it == UNKNOWN }
                if (sizes.first() > nextBrokenOrUnknown.length) return 0

                val remaining = spring.drop(sizes.first())
                if (remaining.isEmpty() && sizes.size == 1) return 1
                if (remaining.isEmpty()) return 0
                if (remaining.first() == BROKEN) return 0
                return genArrangement(
                    spring.drop(sizes.first() + 1),
                    sizes.drop(1)
                )
            } else if (spring.first() == UNKNOWN) {
                return genArrangement(
                    OPERATIONAL + spring.drop(1),
                    sizes,
                ) + genArrangement(
                    BROKEN + spring.drop(1),
                    sizes,
                )
            } else {
                throw IllegalArgumentException(spring.first().toString())
            }).also {
                if (spring to sizes !in cache) cache.put(spring to sizes, it)
            }
        }

        return genArrangement(spring, sizes)
    }

    override fun part1(input: String): Any {
        val (springStrs, sizes) = input.split("\n")
            .map { it.split(" ") }
            .map { (spring, sizes) ->
                spring to sizes.split(",").map { it.toInt() }
            }
            .unzip()

        return springStrs.zip(sizes).map { (spring, sizes) ->
            val size2 = arrangements(spring, sizes)
            size2
        }.sumOf { it }
    }

    override fun part2(input: String): Any {
        val (springStrs, sizes) = input.split("\n")
            .map { it.split(" ") }
            .map { (spring, sizes) ->
                (0..<5).map { spring }.joinToString(UNKNOWN.toString()) to (0..<5).flatMap {
                    sizes.split(",").map { it.toInt() }
                }
            }
            .unzip()

        return springStrs.zip(sizes).map { (spring, sizes) ->
//            println(spring)
            arrangements(spring, sizes).also {
//                println("${it}")
            }
        }.sumOf { it }
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

        assertEquals(21L, part1(input))
        assertEquals(525152L, part2(input))
    }
}