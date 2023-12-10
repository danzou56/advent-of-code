package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day10 : AdventTestRunner23("Pipe Maze") {
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
    enum class Maze(val char: Char, vararg val dirs: Compass) {
        // @formatter:off
        GROUND    ('.'),
        START     ('S', Compass.NORTH, Compass.SOUTH, Compass.EAST, Compass.WEST),
        VERTICAL  ('|', Compass.NORTH, Compass.SOUTH),
        HORIZONTAL('-', Compass.EAST,  Compass.WEST),
        EL        ('L', Compass.NORTH, Compass.EAST),
        JAY       ('J', Compass.NORTH, Compass.WEST),
        SEVEN     ('7', Compass.SOUTH, Compass.WEST),
        EFF       ('F', Compass.SOUTH, Compass.EAST);
        // @formatter:on

        fun embiggened(): String {
            val middlePos = (1 to 1)
            val occupied = this.dirs.associate {
                (middlePos + it.dir) to when (it) {
                    Compass.NORTH, Compass.SOUTH -> '|'
                    Compass.EAST, Compass.WEST -> '-'
                    else -> throw IllegalArgumentException("Invalid direction")
                }
            } + mapOf(middlePos to this.char)

            return (0..<EXPLOSION_FACTOR).joinToString("\n") { y ->
                (0..<EXPLOSION_FACTOR).joinToString("") { x ->
                    occupied[x to y]?.toString() ?: "."
                }
            }
        }

        companion object {
            val EXPLOSION_FACTOR = 3

            private val map = entries.associateBy(Maze::char)
            fun fromChar(char: Char): Maze = map.getValue(char)

            fun fromString(input: String): Matrix<Maze> = input.asMatrix(Maze::fromChar)
        }
    }

    fun getLoop(maze: Matrix<Maze>): Set<Pos> {
        val indices = maze.indices2D
        val start = indices.single { maze[it] == Maze.START }

        val paths = bfs(start) { cur ->
            maze[cur].dirs
                .map { cur + it.dir }
                .filter { next ->
                    maze.getOrElse(next) { _ -> Maze.GROUND }
                        .dirs
                        .any { (next + it.dir) == cur }
                }
                .toSet()
        }
        // Because of the exact geometry of the problem, the number of accessible cells is
        // necessarily even
        return paths.also { assert(it.size % 2 == 0) }
    }

    override fun part1(input: String): Int {
        val maze = Maze.fromString(input)
        val loopPos = getLoop(maze)
        // Given the even-sized loop, the furthest we can get away is half of the number of
        // accessible cells
        return loopPos.size / 2
    }

    override fun part2(input: String): Int {
        val maze = Maze.fromString(input)
        val loop = getLoop(maze)

        val cleanedMaze = maze.mapIndexed2D { pos, cell -> if (pos in loop) cell else Maze.GROUND }
        // To allow us to go between pipes, explode all of the cells into a 3x3 - now each pipe is
        // guaranteed to be at least one empty cell away from any other given pipe
        val explodedMaze = Maze.fromString(
            cleanedMaze.joinToString("\n") { row ->
                row.map(Maze::embiggened)
                    .fold(List(Maze.EXPLOSION_FACTOR) { "" }) { accs, curs ->
                        accs.zip(curs.split("\n")).map { (acc, cur) -> acc + cur }
                    }
                    .joinToString("\n")
            }
        )

        // Find those cells accessible from the border
        val outside = bfs(0 to 0) {
            explodedMaze.neighboringPos(it)
                .filter { explodedMaze[it] == Maze.GROUND }
                .toSet()
        }
            // Map all of them back into the context of the un-exploded maze
            .map { it.x / Maze.EXPLOSION_FACTOR to it.y / Maze.EXPLOSION_FACTOR }
            .filter { cleanedMaze[it] == Maze.GROUND }
            .toSet()

        // Number of cells inside is the total size of the maze, minus the loop & outside
        return maze.indices2D.size - loop.size - outside.size
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