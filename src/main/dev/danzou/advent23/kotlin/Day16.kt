package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day16 : AdventTestRunner23("The Floor Will Be Lava") {
    fun getEnergizedCells(cavern: Matrix<Char>, start: Pos, dir: Compass): Set<Pos> {
        val energized = mutableSetOf<Pos>()
        val visited = mutableSetOf<Pair<Pos, Pos>>()

        // Even though not all recursive calls are in tail position, using tailrec can
        // optimize the tail calls to not create an extra stack frame
        fun move(cur: Pos, vel: Compass) {
            if (cur to vel.dir in visited) return
            visited.add(cur to vel.dir)

            // To keep calls in tail position, add to energized _before_ making
            // the recursive call
            val next = cur + vel.dir
            when (cavern.getOrNull(next)?.also { energized.add(next) }) {
                null -> return
                '.' -> move(next, vel)
                '-' -> when (vel) {
                    Compass.WEST, Compass.EAST -> move(next, vel)
                    else -> {
                        move(next, Compass.EAST)
                        move(next, Compass.WEST)
                    }
                }
                '|' -> when (vel) {
                    Compass.NORTH, Compass.SOUTH -> move(next, vel)
                    else -> {
                        move(next, Compass.NORTH)
                        move(next, Compass.SOUTH)
                    }
                }
                '/' -> when (vel) {
                    Compass.NORTH -> move(next, Compass.EAST)
                    Compass.EAST -> move(next, Compass.NORTH)
                    Compass.SOUTH -> move(next, Compass.WEST)
                    Compass.WEST -> move(next, Compass.SOUTH)
                    else -> throw IllegalArgumentException()
                }
                '\\' -> when (vel) {
                    Compass.NORTH -> move(next, Compass.WEST)
                    Compass.EAST -> move(next, Compass.SOUTH)
                    Compass.SOUTH -> move(next, Compass.EAST)
                    Compass.WEST -> move(next, Compass.NORTH)
                    else -> throw IllegalArgumentException()
                }
                else -> throw IllegalArgumentException()
            }
        }

        move(start, dir)
        return energized
    }

    override fun part1(input: String): Int {
        val cavern = input.asMatrix<Char>()
        return getEnergizedCells(cavern, -1 to 0, Compass.EAST).size
    }

    override fun part2(input: String): Int {
        val cavern = input.asMatrix<Char>()
        val starts = cavern[0].indices.map { x -> (x to -1) to Compass.SOUTH } +
                cavern[0].indices.map { x -> (x to cavern.size) to Compass.NORTH } +
                cavern.indices.map { y -> (-1 to y) to Compass.EAST } +
                cavern.indices.map { y -> (cavern[0].size to y) to Compass.WEST }

        return starts.maxOf { (initPos, initVel) ->
            getEnergizedCells(cavern, initPos, initVel).size
        }
    }

    @Test
    fun testExample() {
        val input = """
            .|...\....
            |.-.\.....
            .....|-...
            ........|.
            ..........
            .........\
            ..../.\\..
            .-.-/..|..
            .|....-|.\
            ..//.|....
        """.trimIndent()

        assertEquals(46, part1(input))
        assertEquals(51, part2(input))
    }
}