package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import dev.danzou.advent.utils.geometry.minus
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.toPair
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue

typealias Knot = Pair<Int, Int>
typealias Rope = List<Knot>

internal class Day9 : AdventTestRunner() {

    /**
     * Returns new tail knot given new head knot
     */
    fun moveKnot(newHead: Knot, oldTail: Knot): Knot =
        (newHead - oldTail).toList().let { diffAsList ->
            oldTail + when {
                diffAsList.maxOf { it.absoluteValue } > 1 ->
                    diffAsList.map {
                        if (it == 0) it
                        else it.absoluteValue / it
                    }.toPair()
                else -> Pair(0, 0)
            }
        }

    fun parse(input: String) = input.split("\n")
        .map { it.split(" ").toPair() }
        .map { (dir, amt) -> Pair(
            when (dir) {
                "R" -> Pair(1, 0)
                "L" -> Pair(-1, 0)
                "U" -> Pair(0, 1)
                "D" -> Pair(0, -1)
                else -> throw IllegalArgumentException()
            },
            amt.toInt()
        ) }

    // You could actually do this with the code from part 2 and different rope
    // length but this more closely resembels the original solution written for
    // part 1
    override fun part1(input: String): Any {
        val res = parse(input).fold(Pair(
            Pair(Knot(0, 0), Knot(0, 0)),
            emptySet<Knot>())
        ) { (rope, visited), (dir, amt) ->
            (0 until amt).fold(Pair(rope, visited)) { (rope, visited), _ ->
                Pair(
                    rope.first + dir,
                    moveKnot(rope.first + dir, rope.second)
                ).let { rope ->
                    Pair(rope, visited + rope.second)
                }
            }
        }
        return res.second.size
    }

    override fun part2(input: String): Any {
        val ropeLength = 10
        // get visited set by folding over each instruction; also keep track of
        // rope state while folding
        val res = parse(input).fold(Pair(
            List(ropeLength) { Knot(0, 0) },
            emptySet<Knot>()
        )) { (rope, visited), (dir, amt) ->
            // get rope state after movement by folding over the number of moves
            // that needs to be made
            (0 until amt).fold(Pair(rope, visited)) { (rope, visited), _ ->
                // construct new rope by folding over each knot in the rope
                rope.drop(1).fold(
                    listOf(rope.first() + dir)
                ) { rope, knot ->
                    rope + moveKnot(rope.last(), knot)
                }.let { rope ->
                    Pair(rope, visited + rope.last())
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
