package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.minus
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.times
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.math.max

internal class Day17 : AdventTestRunner23("Clumsy Crucible") {
    override val timeout: Duration = Duration.ofSeconds(30)

    override fun part1(input: String): Int {
        val matrix = input.asMatrix<Int>()

        val path = doDijkstras(
            listOf<Pos?>(null, null, null, 0 to 0),
            { curs -> curs.last()!! == matrix[0].size - 1 to matrix.size - 1 },
            { curs ->
                val cur = curs.last()
                require(cur != null)
                val disallowedDir = curs.let { (last3, last2, last1, cur) ->
                    if (last3 != null && last2 != null && last1 != null && cur != null) {
                        val diff3 = last2.minus(last3)
                        val diff2 = last1.minus(last2)
                        val diff1 = cur.minus(last1)
                        if (diff3 == diff2 && diff2 == diff1)
                            setOf(diff1)
                        else
                            emptySet()
                    } else emptySet()
                } + setOfNotNull(curs[2]?.minus(cur))

                matrix.neighboringPos(cur).filter {
                    (it - cur) !in disallowedDir
                }.map {
                    (curs + it).takeLast(4)
                }.toSet()
            },
            { _, curs -> matrix[curs.last()!!] }
        )
        return path.drop(1).map { it.last()!! }.toSet().sumOf { matrix[it] }
    }

    override fun part2(input: String): Any {
        val matrix = input.asMatrix<Int>()
        val indices = matrix.indices2D.toSet()

        val path = doDijkstras(
            listOf(0 to 0),
            { curs -> curs.last() == matrix[0].size - 1 to matrix.size - 1 },
            { curs ->
                val cur = curs.last()
                val disallowedDir = curs.windowed(2).map { (last, cur) ->
                    cur - last
                }.let { diffs ->
                    if (diffs.size == 10 && diffs.all { it == diffs.last() })
                        setOf(diffs.last())
                    else
                        emptySet()
                } + setOfNotNull(curs.getOrNull(curs.size - 2)?.minus(cur))

                val lastDiff = curs.getOrNull(curs.size - 2)?.let { cur - it }
                matrix.neighboringPos(cur).filter {
                    (it - cur) !in disallowedDir
                }.mapNotNull { next ->
                    val nextDiff = next - cur
                    if (nextDiff != lastDiff) {
                        val nexts = (1..4).map { cur + (nextDiff * it) }
                        // If we land outside the matrix, we can't actually go in this direction
                        if (nexts.last() !in indices) return@mapNotNull null
                        curs + nexts
                    } else {
                        curs + next
                    }
                }.map {
                    it.takeLast(11)
                }.toSet()
            },
            { last, curs ->
                curs.takeLastWhile { it != last.last() }.sumOf { matrix[it] }
            }
        )
        return path.flatten().toSet().sumOf { matrix[it] } - matrix[0 to 0]
    }

    @Test
    fun testExample() {
        val input = """
            2413432311323
            3215453535623
            3255245654254
            3446585845452
            4546657867536
            1438598798454
            4457876987766
            3637877979653
            4654967986887
            4564679986453
            1224686865563
            2546548887735
            4322674655533
        """.trimIndent()

        assertEquals(102, part1(input))
        assertEquals(94, part2(input))
    }

    @Test
    fun testToyExample() {
        val input = """
            111111111111
            999999999991
            999999999991
            999999999991
            999999999991
        """.trimIndent()

        assertEquals(71, part2(input))
    }
}