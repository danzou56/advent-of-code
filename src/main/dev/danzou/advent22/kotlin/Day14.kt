package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.test.assertEquals

typealias Sand = SparseMatrix<Boolean>
typealias Wall = SparseMatrix<Boolean>
typealias Line = Pair<Point, Point>

internal class Day14 : AdventTestRunner() {

    data class Cave(val bounds: Rectangle, val sand: Sand, val wall: Wall) {

        fun withSand(sand: Sand): Cave = Cave(bounds, sand, wall)

        tailrec fun addSandAt(p: Point): Cave? {
            if (!bounds.contains(p)) return null
            val nextMoves = listOf(0, -1, 1).map { p + Pair(it, 1) }
            val nextMove = nextMoves.firstOrNull { move -> move !in sand && move !in wall }
            return if (nextMove == null) this.withSand(sand + mapOf(p to true))
            else addSandAt(nextMove)
        }

        companion object {
            fun parseFrom(input: String): Cave =
                input.split("\n")
                    .map { it.split(" -> ").map { it.split(",").map { it.toInt() }.toPair() } }
                    .map { it.fold(listOf(Line(it.component1(), it.component2()))) { lines, point ->
                        lines + Line(lines.last().second, point)
                    } }.let { protoWalls -> Cave(
                        protoWalls.flatten().let { walls ->
                            Rectangle(
                                Point(walls.minOf { min(it.first.x, it.second.x) }, walls.minOf { listOf(0, it.first.y, it.second.y).min() }),
                                Point(walls.maxOf { max(it.first.x, it.second.x) }, walls.maxOf { max(it.first.y, it.second.y) })
                            )
                        },
                        emptyMap<Pos, Boolean>().withDefault { false },
                        protoWalls.fold<List<Line>, Map<Pos, Boolean>>(emptyMap()) { map, lines -> map + lines.fold(map) { map, wall ->
                            map + when {
                                wall.first.x - wall.second.x == 0 ->
                                    (min(wall.first.y, wall.second.y)..max(wall.first.y, wall.second.y)).map {
                                        Point(
                                            wall.first.x,
                                            it
                                        )
                                    }

                                wall.first.y - wall.second.y == 0 ->
                                    (min(wall.first.x, wall.second.x)..max(wall.first.x, wall.second.x)).map {
                                        Point(
                                            it,
                                            wall.first.y
                                        )
                                    }
                                else -> throw IllegalStateException()
                            }.associateWith { true }
                        } }.withDefault { false }
                    ) }
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

    override fun part2(input: String): Any {
        TODO("Not yet implemented")
    }

    @Test
    fun testExample() {
        val input = """
            498,4 -> 498,6 -> 496,6
            503,4 -> 502,4 -> 502,9 -> 494,9
        """.trimIndent()

        assertEquals(24, part1(input))
    }
}