package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.times
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class Day18 : AdventTestRunner23("Lavaduct Lagoon") {

    fun det2(mat: Matrix<Long>): Long {
        require(mat.size == 2)
        require(mat.all { it.size == 2 })
        return mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0]
    }

    /**
     * Determine area from dig plan using the shoelace formula
     */
    fun areaFromDigPlan(plan: List<Pair<Compass, Int>>): Long =
        plan.fold((0 to 0) to listOf(0 to 0)) { (cur, border), (dir, count) ->
            val next = cur + dir.dir * count
            next to border + next
        }.let { (_, border) ->
            require(border.first() == border.last())
            require(border.size % 2 == 1)
            val area = abs(
                border.windowed(2)
                    .map { it.map { it.toList().map(Int::toLong) } }
                    .sumOf { det2(it) } / 2
            )
            val (_, borderSize) = border.drop(1).fold(border.first() to 0) { (cur, size), pos ->
                pos to size + cur.manhattanDistanceTo(pos)
            }
            // The integer points inside the polygon and on its boundary can be found by
            // 1. Finding the "traditional" area via the shoelace formula (for why this isn't our
            //    area, consider the unit square versus the "area" of (0, 0), (0, 1), (1, 1), (1, 0))
            // 2. Determining the number of interior points via Pick's Theorem - A = i + b / 2 - 1
            // 3. Calculating our area by summing the number of interior points and boundary points
            area + borderSize / 2 + 1
        }

    override fun part1(input: String): Long {
        val instrs = input.split("\n")
            .map { it.split(" ") }
            .map { (dir, count, _) ->
                when (dir) {
                    "R" -> Compass.EAST
                    "D" -> Compass.SOUTH
                    "L" -> Compass.WEST
                    "U" -> Compass.NORTH
                    else -> throw IllegalStateException()
                } to count.toInt()
            }

        return areaFromDigPlan(instrs)

    }

    override fun part2(input: String): Long {
        val instrs = input.split("\n")
            .map { it.split(" ").last() }
            .map { code -> code.slice(2..<code.length - 1) }
            .map { code ->
                when (code.last()) {
                    '0' -> Compass.EAST
                    '1' -> Compass.SOUTH
                    '2' -> Compass.WEST
                    '3' -> Compass.NORTH
                    else -> throw IllegalArgumentException()
                } to code.slice(0..<5).toInt(radix = 16)
            }

        return areaFromDigPlan(instrs)
    }

    @Test
    fun testExample() {
        val input = """
            R 6 (#70c710)
            D 5 (#0dc571)
            L 2 (#5713f0)
            D 2 (#d2c081)
            R 2 (#59c680)
            D 2 (#411b91)
            L 5 (#8ceee2)
            U 2 (#caa173)
            L 1 (#1b58a2)
            U 2 (#caa171)
            R 2 (#7807d2)
            U 3 (#a77fa3)
            L 2 (#015232)
            U 2 (#7a21e3)
        """.trimIndent()

        assertEquals(62L, part1(input))
        assertEquals(952408144115L, part2(input))
    }
}