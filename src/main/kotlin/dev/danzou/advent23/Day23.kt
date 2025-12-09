package dev.danzou.advent23

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.minus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

internal class Day23 : AdventTestRunner23("A Long Walk") {

    override val timeout = Duration.ofSeconds(30)

    override fun part1(input: String): Int {
        val matrix = input.asMatrix<Char>()
        val start = 1 to 0
        val target = matrix[0].size - 2 to matrix.size - 1

        val paths = findPathsBetween(start, target) { cur ->
            matrix.neighboringPos(cur)
                .filter { pos ->
                    when (matrix[pos]) {
                        '#' -> false
                        else -> true
                    }
                }
                .filter { next ->
                    when (matrix[cur]) {
                        '>' -> next - cur == Compass.EAST.dir
                        'v' -> next - cur == Compass.SOUTH.dir
                        '^' -> next - cur == Compass.NORTH.dir
                        '<' -> next - cur == Compass.WEST.dir
                        else -> true
                    }
                }
                .toSet()
        }

        return paths.maxOf { it.size - 1 }
    }

    override fun part2(input: String): Int {
        val matrix = input.asMatrix<Char>()
        val start = 1 to 0
        val target = matrix[0].size - 2 to matrix.size - 1
        val junctions = (matrix.indices2D.filter { pos ->
            matrix.neighboringPos(pos).filter { matrix[it] != '#' }.size > 2
        } + start + target).toSet()

        val neighbors: (Pos) -> Set<Pos> = { cur ->
            matrix.neighboringPos(cur)
                .filter { pos ->
                    when (matrix[pos]) {
                        '#' -> false
                        else -> true
                    }
                }
                .toSet()
        }
        val nextJunctions: (Pos) -> Map<Pos, Int> = { from ->
            bfsWithDistance(from) { cur ->
                if (cur in junctions && cur != from) return@bfsWithDistance emptySet()
                neighbors(cur)
            }.filter { (next, _) ->
                next in junctions && next != from
            }
        }

        // To make the resulting graph faster to work with, contract the maze into just its
        // junctions. Discover the edges between junctions and their costs with bfs
        val junctionMap = mutableMapOf<Pos, Set<Pos>>()
        val junctionCosts = mutableMapOf<Pair<Pos, Pos>, Int>()
        bfs(start) { cur ->
            nextJunctions(cur).also {
                junctionMap[cur] = it.keys
                it.entries.onEach {
                    junctionCosts[cur to it.key] = it.value
                }
            }.keys
        }

        // Using the contracted map, find the longest path with dfs
        val paths = findPathsBetween(start, target) { cur ->
            junctionMap[cur]!!
        }
        return paths.maxOf {
            it.windowed(2).sumOf { (prev, cur) ->
                junctionCosts[prev to cur]!!
            }
        }
    }

    @Test
    fun testExample() {
        val input = """
            #.#####################
            #.......#########...###
            #######.#########.#.###
            ###.....#.>.>.###.#.###
            ###v#####.#v#.###.#.###
            ###.>...#.#.#.....#...#
            ###v###.#.#.#########.#
            ###...#.#.#.......#...#
            #####.#.#.#######.#.###
            #.....#.#.#.......#...#
            #.#####.#.#.#########v#
            #.#...#...#...###...>.#
            #.#.#v#######v###.###v#
            #...#.>.#...>.>.#.###.#
            #####v#.#.###v#.#.###.#
            #.....#...#...#.#.#...#
            #.#########.###.#.#.###
            #...###...#...#...#.###
            ###.###.#.###v#####v###
            #...#...#.#.>.>.#.>.###
            #.###.###.#.###.#.#v###
            #.....###...###...#...#
            #####################.#
        """.trimIndent()

        assertEquals(94, part1(input))
        assertEquals(154, part2(input))
    }
}