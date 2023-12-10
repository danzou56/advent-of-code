package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class Day10 : AdventTestRunner23() {
    /**
     * | is a vertical pipe connecting north and south.
     * - is a horizontal pipe connecting east and west.
     * L is a 90-degree bend connecting north and east.
     * J is a 90-degree bend connecting north and west.
     * 7 is a 90-degree bend connecting south and west.
     * F is a 90-degree bend connecting south and east.
     * . is ground; there is no pipe in this tile.
     * S is the starting position of the animal; there is a pipe on this tile, but your sketch doesn't show what shape the pipe has.
     */

    // @formatter:off
    enum class Maze(vararg val dirs: Compass) {
        VERTICAL(Compass.NORTH, Compass.SOUTH),
        HORIZONTAL(Compass.EAST, Compass.WEST),
        EL(Compass.NORTH, Compass.EAST),
        JAY(Compass.NORTH, Compass.WEST),
        SEVEN(Compass.SOUTH, Compass.WEST),
        EFF(Compass.SOUTH, Compass.EAST),
        GROUND(),
        START(Compass.NORTH, Compass.SOUTH, Compass.EAST, Compass.WEST);
    }
    // @formatter:on

    fun getEmbiggened(mazePiece: Maze): String = when (mazePiece) {
        Maze.VERTICAL -> """
                .|.
                .|.
                .|.
            """.trimIndent()

        Maze.HORIZONTAL -> """
                ...
                ---
                ...
            """.trimIndent()

        Maze.EL -> """
                .|.
                .L-
                ...
            """.trimIndent()

        Maze.JAY -> """
                .|.
                -J.
                ...
            """.trimIndent()

        Maze.SEVEN -> """
                ...
                -7.
                .|.
            """.trimIndent()

        Maze.EFF -> """
                ...
                .F-
                .|.
            """.trimIndent()

        Maze.GROUND -> """
                ...
                ...
                ...
            """.trimIndent()

        Maze.START -> """
                .|.
                -S-
                .|.
            """.trimIndent()
    }

    fun getMaze(input: String): Matrix<Maze> = input.asMatrix {
        when (it) {
            '|' -> Maze.VERTICAL
            '-' -> Maze.HORIZONTAL
            'L' -> Maze.EL
            'J' -> Maze.JAY
            '7' -> Maze.SEVEN
            'F' -> Maze.EFF
            '.' -> Maze.GROUND
            'S' -> Maze.START
            else -> throw IllegalArgumentException(it.toString())
        }
    }

    override fun part1(input: String): Any {
        val maze = getMaze(input)
        val startPosY = maze.indexOfFirst { row -> row.indexOf(Maze.START) >= 0 }
        val startPosX = maze[startPosY].indexOf(Maze.START)

        val indices = maze.indices2D
        val paths = bfs(startPosX to startPosY) { cur ->
            maze.get(cur).dirs.map { cur + it.dir }.filter { it in indices }
                .filter { next -> maze.get(next).dirs.any { (next + it.dir) == cur } }.toSet()
        }
        return paths.maxOf { it.size } - 1
    }

    override fun part2(input: String): Any {
        val maze = getMaze(input)
        val startPosY = maze.indexOfFirst { row -> row.indexOf(Maze.START) >= 0 }
        val startPosX = maze[startPosY].indexOf(Maze.START)

        val indices = maze.indices2D

        val paths = bfs(startPosX to startPosY) { cur ->
            maze.get(cur).dirs.map { cur + it.dir }.filter { it in indices }
                .filter { next -> maze.get(next).dirs.any { (next + it.dir) == cur } }.toSet().also {
                    it
                }
        }
        val loop = paths.flatMap { it }.toSet()

        val newMaze = maze.mapIndexed { y, row ->
            row.mapIndexed { x, cell -> if ((x to y) in loop) cell else Maze.GROUND }
        }
        val embiggenedMaze = getMaze(newMaze.mapIndexed { y, row ->
            row.mapIndexed { x, cell ->
                getEmbiggened(cell).split("\n")
            }.fold(listOf("", "", "")) { (top, middle, bottom), (curTop, curMiddle, curBottom) ->
                listOf(top + curTop, middle + curMiddle, bottom + curBottom)
            }
        }.flatten().joinToString("\n"))


        val bigStartPosY = embiggenedMaze.indexOfFirst { row -> row.indexOf(Maze.START) >= 0 }
        val bigStartPosX = embiggenedMaze[bigStartPosY].indexOf(Maze.START)
        val bigIndices = embiggenedMaze.indices2D
//        val bigLoop = em
//        val bigLoop = bfs(bigStartPosX to bigStartPosY) { cur ->
//            embiggenedMaze.get(cur).dirs
//                .map { cur + it.dir }
//                .filter { it in bigIndices }
//                .filter { next -> embiggenedMaze.get(next).dirs.any { (next + it.dir) == cur } }.toSet()
//        }.flatMap { it }.toSet()

        val border = bigIndices.filter {
            it.x == 0 || it.x == embiggenedMaze[0].size - 1 || it.y == 0 || it.y == embiggenedMaze.size - 1
        }.filter {
            embiggenedMaze[it] == Maze.GROUND
        }

        val outsideBigCoords = customBfs(border.toSet()) {
            embiggenedMaze.neighboringPos(it).filter { embiggenedMaze[it] == Maze.GROUND }.toSet()
        }.toSet()
        val outside = outsideBigCoords.map { it.x / 3 to it.y / 3 }.filter {
            newMaze[it] == Maze.GROUND
        }.toSet()

        return outside.let { outside ->
            indices.size - loop.size - outside.size
        }.also {
            assertNotEquals(713, it)
            //5945
        }

    }

    fun <T> customBfs(initSet: Set<T>, getNeighbors: NeighborFunction<T>): Set<T> {
        val queue: Queue<T> = LinkedList()
        val discovered = initSet.toMutableSet()
        queue.addAll(initSet)
        while (queue.isNotEmpty()) {
            val cur = queue.poll()!!
//            discovered.add(cur)
            for (adjacent in getNeighbors(cur)) {
                if (adjacent !in discovered) {
                    discovered.add(adjacent)
                    queue.add(adjacent)
                }
            }
        }
        return discovered
    }

    @Test
    fun testExample() {
        run {
            val input = """
                ...........
                .S-------7.
                .|F-----7|.
                .||.....||.
                .||.....||.
                .|L-7.F-J|.
                .|..|.|..|.
                .L--J.L--J.
                ...........
            """.trimIndent()

            assertEquals(4, part2(input))
        }


        run {
            val input = """
                ..........
                .S------7.
                .|F----7|.
                .||....||.
                .||....||.
                .|L-7F-J|.
                .|..||..|.
                .L--JL--J.
                ..........
            """.trimIndent()

            assertEquals(4, part2(input))
        }
    }

    @Test
    fun testLargeExample() {
        run {
            val input = """
                .F----7F7F7F7F-7....
                .|F--7||||||||FJ....
                .||.FJ||||||||L7....
                FJL7L7LJLJ||LJ.L-7..
                L--J.L7...LJS7F-7L7.
                ....F-J..F7FJ|L7L7L7
                ....L7.F7||L7|.L7L7|
                .....|FJLJ|FJ|F7|.LJ
                ....FJL-7.||.||||...
                ....L---J.LJ.LJLJ...
            """.trimIndent()

            assertEquals(8, part2(input))
        }

        run {
            val input = """
                FF7FSF7F7F7F7F7F---7
                L|LJ||||||||||||F--J
                FL-7LJLJ||||||LJL-77
                F--JF--7||LJLJ7F7FJ-
                L---JF-JLJ.||-FJLJJ7
                |F|F-JF---7F7-L7L|7|
                |FFJF7L7F-JF7|JL---7
                7-L-JL7||F7|L7F-7F7|
                L.L7LFJ|||||FJL7||LJ
                L7JLJL-JLJLJL--JLJ.L
            """.trimIndent()

            assertEquals(10, part2(input))
        }
    }
}