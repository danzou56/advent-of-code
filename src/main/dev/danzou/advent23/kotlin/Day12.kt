package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day12 : AdventTestRunner23() {
    val UNKNOWN = '?'
    val BROKEN = '#'
    val OPERATIONAL = '.'

    fun arrangementsOf(spring: String, sizes: List<Int>): Long {
        val cache = mutableMapOf<Pair<String, List<Int>>, Long>()

        fun calculate(spring: String, sizes: List<Int>): Long =
            if (spring to sizes in cache) cache[spring to sizes]!!
            else when (spring.firstOrNull()) {
                OPERATIONAL -> calculate(spring.dropWhile { it == OPERATIONAL }, sizes)
                BROKEN -> when {
                    sizes.isEmpty() -> 0
                    sizes.first() > spring.takeWhile { it != OPERATIONAL }.length -> 0
                    spring.drop(sizes.first()).firstOrNull() == BROKEN -> 0
                    else -> calculate(spring.drop(sizes.first() + 1), sizes.drop(1))
                }
                UNKNOWN -> calculate(OPERATIONAL + spring.drop(1), sizes) +
                        calculate(BROKEN + spring.drop(1), sizes)
                null -> if (sizes.isEmpty()) 1 else 0
                else -> throw IllegalArgumentException(spring.first().toString())
            }.also {
                cache[spring to sizes] = it
            }

        return calculate(spring, sizes)
    }

    override fun part1(input: String): Long =
        input.split("\n")
            .map { it.split(" ") }
            .map { (spring, sizes) ->
                spring to sizes.split(",").map { it.toInt() }
            }
            .sumOf { (spring, sizes) ->
                arrangementsOf(spring, sizes)
            }

    override fun part2(input: String): Long =
        input.split("\n")
            .map { it.split(" ") }
            .map { (spring, sizes) ->
                Pair(
                    (0..<5).joinToString(UNKNOWN.toString()) { spring },
                    (0..<5).flatMap {
                        sizes.split(",").map { it.toInt() }
                    }
                )
            }
            .sumOf { (spring, sizes) ->
                arrangementsOf(spring, sizes)
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