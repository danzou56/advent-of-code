package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.geometry.toPair
import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day14 : AdventTestRunner21() {
    fun getRules(input: String): Map<List<Char>, Char> =
        input.split("\n")
            .drop(2)
            .map { it.split(" -> ") }
            .map { it.toPair() }
            .associateBy({ it.first.toList() }, { it.second.single() })

    fun simulate(
        initial: String,
        rules: Map<List<Char>, Char>,
        steps: Int
    ): Map<List<Char>, Long> {
        fun step(cur: Map<List<Char>, Long>): Map<List<Char>, Long> =
            rules.entries.fold(emptyMap()) { acc, (src, dst) ->
                val srcCount = cur[src] ?: 0
                acc + mapOf(
                    listOf(dst, src[1]) to srcCount + (acc[listOf(dst, src[1])] ?: 0),
                    listOf(src[0], dst) to srcCount + (acc[listOf(src[0], dst)] ?: 0)
                )
            }

        return (0 until steps).fold(
            initial.toList()
                .windowed(2, 1)
                .groupingBy { it }
                .eachCount()
                .mapValues { it.value.toLong() }
        ) { acc, _ -> step(acc) }
    }

    fun getScore(state: Map<List<Char>, Long>, first: Char, last: Char): Long =
        state.entries.fold(emptyMap<Char, Long>()) { acc, (e, count) ->
            acc + when {
                e[0] == e[1] -> mapOf(e[0] to (acc[e[0]] ?: 0) + 2 * count)
                e[0] != e[1] -> mapOf(
                    e[0] to (acc[e[0]] ?: 0) + count,
                    e[1] to (acc[e[1]] ?: 0) + count,
                )

                else -> emptyMap()
            }
        }.mapValues { ((it.value) + if (it.key in listOf(first, last)) 1 else 0) / 2 }
            .let { counts ->
                counts.values.maxOf { it } - counts.values.minOf { it }
            }

    override fun part1(input: String): Any {
        val init = input.split("\n").first()
        val state = simulate(
            init,
            getRules(input),
            10
        )
        return getScore(state, init.first(), init.last())
    }

    override fun part2(input: String): Any {
        val init = input.split("\n").first()
        val state = simulate(
            init,
            getRules(input),
            40
        )
        return getScore(state, init.first(), init.last())
    }

    @Test
    fun testExample() {
        val input = """
            NNCB

            CH -> B
            HH -> N
            CB -> H
            NH -> C
            HB -> C
            HC -> B
            HN -> C
            NN -> C
            BH -> H
            NC -> B
            NB -> B
            BN -> B
            BB -> N
            BC -> B
            CC -> N
            CN -> C
        """.trimIndent()

        assertEquals(1588L, part1(input))
    }
}