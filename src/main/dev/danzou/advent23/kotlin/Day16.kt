package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day16 : AdventTestRunner23() {

    override fun part1(input: String): Any {
        val room = input.asMatrix<Char>()
        val valid = room.indices2D
        val energized = mutableSetOf<Pos>()
        val movedThrough = mutableSetOf<Pair<Pos, Pos>>()

        fun move(cur: Pos, vel: Pos) {
            val next = cur + vel
            if (next !in valid) return
            energized.add(next)
            if (cur to vel in movedThrough) return
            movedThrough.add(Pair(cur, vel))
            when (room[next]) {
                '.' -> move(next, vel)
                '-' -> if (vel.x != 0) {
                    require(vel == Compass.WEST.dir || vel == Compass.EAST.dir)
                    move(next, vel)
                } else {
                    move(next, Compass.EAST.dir)
                    move(next, Compass.WEST.dir)
                }

                '|' -> if (vel.y != 0) {
                    require(vel == Compass.NORTH.dir || vel == Compass.SOUTH.dir)
                    move(next, vel)
                } else {
                    require(vel == Compass.EAST.dir || vel == Compass.WEST.dir)
                    move(next, Compass.NORTH.dir)
                    move(next, Compass.SOUTH.dir)
                }
                '/' -> when (vel) {
                    Compass.NORTH.dir -> move(next, Compass.EAST.dir)
                    Compass.EAST.dir -> move(next, Compass.NORTH.dir)
                    Compass.SOUTH.dir -> move(next, Compass.WEST.dir)
                    Compass.WEST.dir -> move(next, Compass.SOUTH.dir)
                }

                '\\' -> when (vel) {
                    Compass.NORTH.dir -> move(next, Compass.WEST.dir)
                    Compass.EAST.dir -> move(next, Compass.SOUTH.dir)
                    Compass.SOUTH.dir -> move(next, Compass.EAST.dir)
                    Compass.WEST.dir -> move(next, Compass.NORTH.dir)
                }
                else -> throw IllegalArgumentException()
            }
        }

        move(-1 to 0, Compass.EAST.dir)

        return energized.size
    }

    override fun part2(input: String): Any {
        val room = input.asMatrix<Char>()
        val valid = room.indices2D

        tailrec fun move(cur: Pos, vel: Pos, energized: MutableSet<Pos>, movedThrough: MutableSet<Pair<Pos, Pos>>) {
            val next = cur + vel
            if (next !in valid) return
            energized.add(next)
            if (cur to vel in movedThrough) return
            movedThrough.add(Pair(cur, vel))
            when (room[next]) {
                '.' -> move(next, vel, energized, movedThrough)
                '-' -> if (vel.x != 0) {
                    require(vel == Compass.WEST.dir || vel == Compass.EAST.dir)
                    move(next, vel, energized, movedThrough)
                } else {
                    move(next, Compass.EAST.dir, energized, movedThrough)
                    move(next, Compass.WEST.dir, energized, movedThrough)
                }

                '|' -> if (vel.y != 0) {
                    require(vel == Compass.NORTH.dir || vel == Compass.SOUTH.dir)
                    move(next, vel, energized, movedThrough)
                } else {
                    require(vel == Compass.EAST.dir || vel == Compass.WEST.dir)
                    move(next, Compass.NORTH.dir, energized, movedThrough)
                    move(next, Compass.SOUTH.dir, energized, movedThrough)
                }
                '/' -> when (vel) {
                    Compass.NORTH.dir -> move(next, Compass.EAST.dir, energized, movedThrough)
                    Compass.EAST.dir -> move(next, Compass.NORTH.dir, energized, movedThrough)
                    Compass.SOUTH.dir -> move(next, Compass.WEST.dir, energized, movedThrough)
                    Compass.WEST.dir -> move(next, Compass.SOUTH.dir, energized, movedThrough)
                }

                '\\' -> when (vel) {
                    Compass.NORTH.dir -> move(next, Compass.WEST.dir, energized, movedThrough)
                    Compass.EAST.dir -> move(next, Compass.SOUTH.dir, energized, movedThrough)
                    Compass.SOUTH.dir -> move(next, Compass.EAST.dir, energized, movedThrough)
                    Compass.WEST.dir -> move(next, Compass.NORTH.dir, energized, movedThrough)
                }
                else -> throw IllegalArgumentException()
            }
        }

        val starts = (0..<room[0].size).map { x -> (x to -1) to Compass.SOUTH.dir } +
                (0..<room[0].size).map { x -> (x to room.size) to Compass.NORTH.dir } +
                (0..<room.size).map { y -> (-1 to y) to Compass.EAST.dir } +
                (0..<room.size).map { y -> (room[0].size to y) to Compass.WEST.dir }

        return starts.maxOf { (initPos, initVel) ->
            val energized = mutableSetOf<Pos>()
            val movedThrough = mutableSetOf<Pair<Pos, Pos>>()

            move(initPos, initVel, energized, movedThrough)
            energized.size
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