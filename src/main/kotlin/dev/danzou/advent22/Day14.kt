package dev.danzou.advent22

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.toPair
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.test.assertEquals

typealias Sand = SparseMatrix<Boolean>
typealias Wall = SparseMatrix<Boolean>
typealias Line = Pair<Point, Point>

internal class Day14 : AdventTestRunner22() {

    data class Cave(val bounds: IntRange, val sand: Sand, val wall: Wall) {

        // TODO get rid of immutable map cuz it so slow
        tailrec fun addSandAt(p: Point): Cave? {
            if (sand.getValue(p)) return null
            if (p.y !in bounds) return null
            val nextMoves = listOf(0, -1, 1).map { p + Pair(it, 1) }
            val nextMove = nextMoves.firstOrNull { move -> !sand.getValue(move) && !wall.getValue(move) }
            return if (nextMove == null) this.copy(sand = (sand + mapOf(p to true)).withDefault { false })
            else addSandAt(nextMove)
        }

        companion object {
            fun parseFrom(input: String, part: Int = 1): Cave {
                val wallLines: List<List<Line>> = input.split("\n")
                    .map { it.split(" -> ", ",").map(String::toInt).chunked(2).map(List<Int>::toPoint) }
                    .map { it.windowed(2).map(List<Point>::toPair) }

                val bounds = wallLines.flatten().let { walls -> walls.flatMap { listOf(it.first.y, it.second.y) }.let { yCoords ->
                    min(0, yCoords.min()).. yCoords.max() + 2 * (part - 1)
                } }
                val sand = emptyMap<Pos, Boolean>().withDefault { false }
                val walls = wallLines.fold<List<Line>, Map<Pos, Boolean>>(emptyMap()) { map, lines ->
                    map + lines.fold(map) { map, wall ->
                        map + when {
                            wall.first.x - wall.second.x == 0 ->
                                (min(wall.first.y, wall.second.y)..max(wall.first.y, wall.second.y)).map {
                                    Point(wall.first.x, it)
                                }
                            wall.first.y - wall.second.y == 0 ->
                                (min(wall.first.x, wall.second.x)..max(wall.first.x, wall.second.x)).map {
                                    Point(it, wall.first.y)
                                }
                            else -> throw IllegalArgumentException()
                        }.associateWith { true }
                    }
                }.withDefault(when (part) {
                    1 -> { _: Pos -> false }
                    2 -> { (_, y): Pos -> when (y) {
                        bounds.last -> true
                        else -> false
                    } }
                    else -> throw IllegalArgumentException()
                })
                return Cave(bounds, sand, walls)
            }
        }
    }

    fun run(cave: Cave): Int = run(0, cave)

    tailrec fun run(iter: Int, cave: Cave): Int {
        val newCave = cave.addSandAt(Pos(500, 0))
        if (newCave == null) return iter
        else return run(iter + 1, newCave)
    }

    override fun part1(input: String): Any =
        run(Cave.parseFrom(input))

    override fun part2(input: String): Any =
        run(Cave.parseFrom(input, part = 2))

    @Test
    fun testExample() {
        val input = """
            498,4 -> 498,6 -> 496,6
            503,4 -> 502,4 -> 502,9 -> 494,9
        """.trimIndent()

        assertEquals(24, part1(input))
        assertEquals(93, part2(input))
    }
}