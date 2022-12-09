package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import dev.danzou.advent.utils.minus
import dev.danzou.advent.utils.plus
import dev.danzou.advent.utils.toPair
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue

internal class Day9 : AdventTestRunner() {

    fun simulate(rope: Rope, dir: Pair<Int, Int>): Rope {
        val newHead = rope.head + dir
        val oldTail = rope.tail
        val diff = newHead - oldTail

        val offset = when {
            (diff.toList().map { it.absoluteValue }.max() > 1) -> (newHead - oldTail).toList()
                .map {
                    if (it == 0) it
                    else it.absoluteValue / it
                }.toPair()
            else -> Pair(0, 0)
        }
        return Rope(newHead, oldTail + offset)
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
                    simulate(rope, dir).let { newRope ->
                        Pair(newRope, visited + newRope.tail)
                    }
                }.also { (rope, _) -> /*println(rope.head)*/ }
            }
        return res.second.size
    }

    fun simulate(newHead: Pair<Int, Int>, oldTail: Pair<Int, Int>): Rope {
        return Rope(newHead, oldTail + (newHead - oldTail).toList()
            .map {
                if (it == 0) it
                else it.absoluteValue / it
            }.toPair())
    }

    override fun part2(input: String): Any {
        val res = parse(input)
            .fold(Pair(
                List(9) { Rope() },
                emptySet<Pair<Int, Int>>()
            )) { (ropes, visited), (dir, amt) ->
                (0 until amt).fold(Pair(ropes, visited)) { (ropes, visited), _ ->
                    ropes.foldIndexed(
                        Pair(emptyList<Rope>(), dir)
                    ) { i, (ropesAcc, dir), rope ->
                        when (dir) {
                            Pair(0, 0) -> Pair(ropesAcc + rope, rope.tail - ropes.getOrElse(i + 1) { ropes[i] }.head)
                            else -> simulate(rope, dir).let { newRope ->
                                Pair(ropesAcc + newRope, newRope.tail - ropes.getOrElse(i + 1, { ropes[i] }).head)
                            }
                        }
                    }.let { (newRopes, _) ->
                        Pair(newRopes, visited + newRopes.last().tail).also {
//                            println(it.first)
                        }
                    }
/*                    ropes.drop(1).fold(
                        listOf(simulate(ropes.first(), dir))
                    ) { ropes, rope ->
                        (rope.head - ropes.last().tail).let { diff ->
                            when (diff) {
                                Pair(0, 0) -> ropes + rope
                                else -> ropes + simulate(rope, diff)
                            }
                        }
                    }.let { newRopes ->
                        Pair(newRopes, visited + newRopes.last().tail)
                    }*/
                }.also {
//                    println(it.first)
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


