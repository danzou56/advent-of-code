package dev.danzou.advent23

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass.Companion.CARDINAL_DIRECTIONS
import dev.danzou.advent.utils.geometry.minus
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.times
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day17 : AdventTestRunner23("Clumsy Crucible") {

    fun findCruciblePath(city: Matrix<Int>, minDistance: Int, maxDistance: Int): List<Pos> {
        val target = city[0].size - 1 to city.size - 1

        val path = doDijkstras(
            listOf(0 to 0),
            { curs -> curs.last() == target },
            { curs ->
                val cur = curs.last()
                val prevDiff = curs.getOrNull(curs.size - 2)?.let { cur - it }
                val disallowedDiff = setOfNotNull(
                    curs.getOrNull(maxDistance)?.minus(curs[maxDistance - 1]),
                    prevDiff?.times(-1)
                )

                CARDINAL_DIRECTIONS.filter { diff ->
                    diff !in disallowedDiff
                }.map { diff ->
                    when (diff) {
                        prevDiff -> curs.takeLast(maxDistance) + (cur + diff)
                        else -> (0..minDistance).map { cur + (diff * it) }
                    }
                }.filter { nexts ->
                    city.containsPos(nexts.last())
                }.toSet()
            },
            { last, curs ->
                curs.takeLastWhile { it != last.last() }.sumOf { city[it] }
            },
        )

        return path.flatten().toSet().sortedBy { pos -> path.indexOfFirst { state -> pos in state } }
    }

    override fun part1(input: String): Int {
        val city = input.asMatrix<Int>()

        val path = findCruciblePath(city, 1, 3)
        return path.sumOf { city[it] } - city[0 to 0]
    }

    override fun part2(input: String): Int {
        val city = input.asMatrix<Int>()

        val path = findCruciblePath(city, 4, 10)
        return path.sumOf { city[it] } - city[0 to 0]
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