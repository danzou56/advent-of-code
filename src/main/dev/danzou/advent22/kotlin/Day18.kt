package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.bfs
import dev.danzou.advent.utils.geometry3.*
import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Day18 : AdventTestRunner22() {
    enum class Direction3(val dir: Pos3) {
        UP(Triple(0, 1, 0)),
        DOWN(Triple(0, -1, 0)),
        LEFT(Triple(-1, 0, 0)),
        RIGHT(Triple(1, 0, 0)),
        IN(Triple(0, 0, 1)),
        OUT(Triple(0, 0, -1));

        fun opposite(): Direction3 = when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
            IN -> OUT
            OUT -> IN
        }

        companion object {
            val diagonals = listOf(
                listOf(UP, LEFT),
                listOf(UP, RIGHT),
                listOf(UP, IN),
                listOf(UP, OUT),
                listOf(DOWN, LEFT),
                listOf(DOWN, RIGHT),
                listOf(DOWN, IN),
                listOf(DOWN, OUT),
                listOf(LEFT, IN),
                listOf(LEFT, OUT),
                listOf(RIGHT, IN),
                listOf(RIGHT, OUT)
            ).map { it.map { it.dir }.reduce { t1, t2 -> t1 + t2 } }
        }
    }

    data class UnitCube(val pos: Pos3) {
        constructor(x: Int, y: Int, z: Int) : this(Pos3(x, y, z))

        companion object {
            fun fromString(str: String, delimiter: String = ","): UnitCube {
                val (x, y, z) = str.split(Regex("${delimiter}\\s*")).map { it.toInt() }
                return UnitCube(x, y, z)
            }
        }
    }

    override fun part1(input: String): Any {
        val cubes = input.split("\n").map { UnitCube.fromString(it) }
        val cubeMap = cubes.map { it.pos to it }.toMap()
        return cubes.sumOf { cube -> Direction3.values().count { dir ->
            !cubeMap.containsKey(cube.pos + dir.dir)
        } }
    }

    override fun part2(input: String): Any {
        val cubes = input.split("\n").map { UnitCube.fromString(it) }
        val cubeMap = cubes.map { it.pos to it }.toMap()

        fun getNeighboringAir(pos: Pos3): Set<Pos3> =
            Direction3.values().map { pos + it.dir }
                .filter { it !in cubeMap }.toSet()

        val discovered = bfs(
            cubes.maxBy { it.pos.y }.pos + Direction3.UP.dir
        ) { pos ->
            val directNeighbors = getNeighboringAir(pos)
            val diagonalNeighbors = Direction3.diagonals.map { pos + it }
                .filter { it !in cubeMap && directNeighbors.intersect(getNeighboringAir(it)).size == 1 }
            directNeighbors.union(diagonalNeighbors).filter { getNeighboringAir(it).size < 6 }.toSet()
        }
        return discovered.sumOf { pos -> Direction3.values().count { dir ->
            cubeMap.containsKey(pos + dir.dir)
        } }
    }

    @Test
    fun testExample() {
        val input = """
            2,2,2
            1,2,2
            3,2,2
            2,1,2
            2,3,2
            2,2,1
            2,2,3
            2,2,4
            2,2,6
            1,2,5
            3,2,5
            2,1,5
            2,3,5
        """.trimIndent()

        assertEquals(64, part1(input))
        assertEquals(58, part2(input))
    }
}