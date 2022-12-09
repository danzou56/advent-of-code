package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import dev.danzou.advent.utils.minus
import dev.danzou.advent.utils.plus
import dev.danzou.advent.utils.toPair
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue

internal class Day9 : AdventTestRunner() {

    fun simulate(newHead: Pair<Int, Int>, oldTail: Pair<Int, Int>): Rope =
        (newHead - oldTail).toList().let { diffAsList ->
            Rope(newHead, oldTail + when {
                diffAsList.maxOf { it.absoluteValue } > 1 ->
                    diffAsList.map {
                        if (it == 0) it
                        else it.absoluteValue / it
                    }.toPair()
                else -> Pair(0, 0)
            })
    }

    data class Rope(val head: Pair<Int, Int> = Pair(0, 0), val tail: Pair<Int, Int> = Pair(0, 0))

    fun parse(input: String) = input.split("\n")
        .map { it.split(" ").toPair() }
        .map { (dir, amt) -> Pair(
            when (dir) {
                "R" -> Pair(1, 0)
                "L" -> Pair(-1, 0)
                "U" -> Pair(0, 1)
                "D" -> Pair(0, -1)
                else -> throw IllegalStateException()
            },
            amt.toInt()
        ) }

    override fun part1(input: String): Any {
        val res = parse(input)
            .fold(Pair(Rope(), emptySet<Pair<Int, Int>>())) { (rope, visited), (dir, amt) ->
                (0 until amt).fold(Pair(rope, visited)) { (rope, visited), _ ->
                    simulate(rope.head + dir, rope.tail).let { newRope ->
                        Pair(newRope, visited + newRope.tail)
                    }
                }
            }
        return res.second.size
    }

    override fun part2(input: String): Any {
        val res = parse(input)
            .fold(Pair(
                List(9) { Rope() },
                emptySet<Pair<Int, Int>>()
            )) { (ropes, visited), (dir, amt) ->
                (0 until amt).fold(Pair(ropes, visited)) { (ropes, visited), _ ->
                    ropes.drop(1).fold(
                        listOf(simulate(ropes.first().head + dir, ropes.first().tail))
                    ) { ropes, rope ->
                        ropes + simulate(ropes.last().tail, rope.tail)
                    }.let { newRopes ->
                        Pair(newRopes, visited + newRopes.last().tail)
                    }
                }
            }
        return res.second.size
    }

    @Test
    fun testSmallExample() {
        val input = """
            R 4
            U 4
            L 3
            D 1
            R 4
            D 1
            L 5
            R 2
        """.trimIndent()

        assertEquals(13, part1(input))
        assertEquals(1, part2(input))
    }

    @Test
    fun testBigExample() {
        val input = """
            R 5
            U 8
            L 8
            D 3
            R 17
            D 10
            L 25
            U 20
        """.trimIndent()

        assertEquals(36, part2(input))
    }
}


