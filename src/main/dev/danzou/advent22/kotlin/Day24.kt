package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Direction
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day24 : AdventTestRunner22() {
    class BlizzardBasin(private val walls: SparseMatrix<Boolean>, private val blizzards: SparseMatrix<Direction>) {
        val width = walls.maxOf { (k, _) -> k.x } - 1
        val height = walls.maxOf { (k, _) -> k.y } - 1
        val entrance = Pos((0 .. width + 1).first { Pos(it, 0) !in walls }, 0)
        val exit = Pos((0 .. width + 1).first { Pos(it, height + 1) !in walls }, height + 1)

        fun isOccupied(pos: Pos, time: Int): Boolean {
            if (pos in walls || pos.x !in 0..width + 1 || pos.y !in 0..height + 1) return true
            val isOccupiedHorizontally = (0 until width).mapNotNull { x ->
                when (blizzards[Pos(x, pos.y - 1)]) {
                    Direction.RIGHT -> (x + time) % width
                    Direction.LEFT -> ((x - time) % width + width) % width
                    else -> null
                }
            }.any { it == pos.x - 1 }
            val isOccupiedVertically = (0 until height).mapNotNull { y ->
                when (blizzards[Pos(pos.x - 1, y)]) {
                    Direction.DOWN -> (y + time) % height
                    Direction.UP -> ((y - time) % height + height) % height
                    else -> null
                }
            }.any { it == pos.y - 1 }
            return isOccupiedHorizontally || isOccupiedVertically
        }

        companion object {
            fun fromString(input: String): BlizzardBasin {
                val walls = mutableMapOf<Pos, Boolean>()
                val blizzards = mutableMapOf<Pos, Direction>()
                input.split("\n").forEachIndexed { j, row ->
                    row.forEachIndexed { i, c -> when (c) {
                        '#' -> walls[Pos(i, j)] = true
                        '>' -> blizzards[Pos(i - 1, j - 1)] = Direction.RIGHT
                        '<' -> blizzards[Pos(i - 1, j - 1)] = Direction.LEFT
                        '^' -> blizzards[Pos(i - 1, j - 1)] = Direction.UP
                        'v' -> blizzards[Pos(i - 1, j - 1)] = Direction.DOWN
                    } }
                }
                return BlizzardBasin(
                    walls,
                    blizzards
                )
            }
        }
    }

    override fun part1(input: String): Any {
        val basin = BlizzardBasin.fromString(input)
        val init = Pair(basin.entrance, 0)
        val path = doDijkstras(
            init,
            { (p, _) -> basin.exit == p },
            { (p, t) ->
                cardinalDirections
                    .plus(Pos(0, 0))
                    .map { delta -> Pair(p + delta, t + 1) }
                    .filter { (p, t) -> !basin.isOccupied(p, t) }
                    .toSet()
            }
        )
        return path.size - 1
    }

    override fun part2(input: String): Any {
        val basin = BlizzardBasin.fromString(input)
        val exitTarget: (Pair<Pos, Int>) -> Boolean = { (p, _) -> basin.exit == p }
        val entranceTarget: (Pair<Pos, Int>) -> Boolean = { (p, _) -> basin.entrance == p }
        val getNeighbors: NeighborFunction<Pair<Pos, Int>> = { (p, t) ->
            cardinalDirections
                .plus(Pos(0, 0))
                .map { delta -> Pair(p + delta, t + 1) }
                .filter { (p, t) -> !basin.isOccupied(p, t) }
                .toSet()
        }
        val first = doDijkstras(
            Pair(basin.entrance, 0),
            exitTarget,
            getNeighbors
        )
        val second = doDijkstras(
            Pair(basin.exit, first.size - 1),
            entranceTarget,
            getNeighbors
        )
        val third = doDijkstras(
            Pair(basin.entrance, first.size + second.size - 2),
            exitTarget,
            getNeighbors
        )
        return first.size + second.size + third.size - 3
    }

    @Test
    fun testBlizzardPhysics() {
        val input = """
            #.#####
            #.....#
            #>....#
            #.....#
            #...v.#
            #.....#
            #####.#
        """.trimIndent()

        val basin = BlizzardBasin.fromString(input)

        // test fields
        assertEquals(5, basin.width)
        assertEquals(5, basin.height)
        assertEquals(Pos(1, 0), basin.entrance)
        assertEquals(Pos(5, 6), basin.exit)

        // test walls
        assertTrue(basin.isOccupied(Pos(0, 0), 0))
        assertTrue(basin.isOccupied(Pos(0, 0), 1))
        assertTrue(basin.isOccupied(Pos(6, 3), 15))
        assertTrue(basin.isOccupied(Pos(6, 6), 15))
        assertTrue(basin.isOccupied(Pos(1, -1), 0))
        assertTrue(basin.isOccupied(Pos(5, 7), 0))

        // test exit
        assertFalse(basin.isOccupied(Pos(1, 0), 0))

        // test initial blizzards
        assertFalse(basin.isOccupied(Pos(1, 1), 0))
        assertTrue(basin.isOccupied(Pos(1, 2), 0))

        // test moved blizzards
        assertTrue(basin.isOccupied(Pos(1, 2), 0))
        assertTrue(basin.isOccupied(Pos(2, 2), 1))
        assertTrue(basin.isOccupied(Pos(2, 2), 6))
    }

    @Test
    fun testExample() {
        val input = """
            #.######
            #>>.<^<#
            #.<..<<#
            #>v.><>#
            #<^v^^>#
            ######.#
        """.trimIndent()

        assertEquals(18, part1(input))
        assertEquals(54, part2(input))
    }

    @Test
    fun testPath() {
        val input = """
            #.######
            #>>.<^<#
            #.<..<<#
            #>v.><>#
            #<^v^^>#
            ######.#
        """.trimIndent()

        val basin = BlizzardBasin.fromString(input)
        val path = listOf(
            Pos(1, 0),
            Pos(1, 1),
            Pos(1, 2),
            Pos(1, 2),
            Pos(1, 1),
            Pos(2, 1),
            Pos(3, 1),
            Pos(3, 2),
            Pos(2, 2),
            Pos(2, 1),
            Pos(3, 1),
            Pos(3, 1),
            Pos(3, 2),
            Pos(3, 3),
            Pos(4, 3),
            Pos(5, 3),
            Pos(6, 3),
            Pos(6, 4),
            Pos(6, 5)
        ).withIndex()

        path.forEach { (t, p) -> assertFalse(basin.isOccupied(p, t)) }
    }
}