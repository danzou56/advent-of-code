package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass.Companion.CARDINAL_DIRECTIONS
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class Day21 : AdventTestRunner23() {

    /**
     * Custom bfs for day 21 that also keeps track of shortest distance to the node, then removes
     */
    private fun <T> bfs(init: T, threshold: Int, getNeighbors: NeighborFunction<T>): Set<T> {
        if (threshold < 0) return emptySet()
        if (threshold == 0) return setOf(init)
        val queue: Queue<T> = LinkedList()
        val discovered = mutableMapOf(init to 0)
        queue.add(init)
        while (queue.isNotEmpty()) {
            val cur = queue.poll()!!
            if (discovered[cur]!! > threshold) continue
            for (adjacent in getNeighbors(cur)) {
                if (adjacent !in discovered) {
                    discovered.put(adjacent, discovered[cur]!! + 1)
                    queue.add(adjacent)
                }
            }
        }

        return discovered.filter { (_, value) -> value % 2 == threshold % 2 }.keys
    }

    override fun part1(input: String): Int = getStepsIn(input, 64)

    override fun part2(input: String): Long = getALotOfStepsIn(input, 26501365)

    fun getStepsIn(input: String, steps: Int): Int {
        val matrix = input.asMatrix<Char>()
        val size = matrix.size
        val start = matrix.indices2D.single { matrix[it] == 'S' }
        println(start)

        val getNeighbors = { cur: Pos ->
            CARDINAL_DIRECTIONS.map { delta ->
                cur + delta
            }.filter { (x, y) ->
                val c = matrix.getOrNull(
                    Pair(
                        (x % size + size) % size,
                        (y % size + size) % size
                    )
                )
                c == '.' || c == 'S'
            }.toSet()
        }

        return bfs(start, steps, getNeighbors).toSet().size
    }

    fun getALotOfStepsIn(input: String, steps: Int): Long {
        val matrix = input.asMatrix<Char>()
        val size = matrix.size
        val start = matrix.indices2D.single { matrix[it] == 'S' }

        require(steps >= start.manhattanDistanceTo(0 to 0))
        val getNeighbors = { cur: Pos ->
            CARDINAL_DIRECTIONS.map { delta ->
                cur + delta
            }.filter { p ->
                val c = matrix.getOrNull(p)
                c == '.' || c == 'S'
            }.toSet()
        }

        // The arithmetic here quite possibly has off-by-one errors or other arithmetic errors as
        // the test input is an edge case that may not be fully exercising the code
        // * the sample input doesn't produce correct results (although this might have more to do
        //   with how many obstacles it has versus the test input)
        // * the test input is a special case where "outside straight" tiles don't exist (for
        //   certain values of steps it's possible to have more than one incomplete tile directly
        //   straight from the center per direction)
        // * while the actual number of garden plots in each cut-off/added-on corner are different,
        //   their dimensions are the same in the test input

        // Including the center, the number of tiles that are completely discovered in one direction
        val reach = (steps + 1) / size

        // The tile size is odd which causes the number of tiles accessible for complete covered
        // tiles to form a checkerboard pattern
        require(size % 2 != 0)
        val evenTilesCount = (2 * ((reach - 1) / 2) + 1).toLong().pow(2)
        val evenTotal = evenTilesCount * bfs(start, size, getNeighbors).size
        val oddTilesCount = (2 * (reach / 2)).toLong().pow(2)
        val oddTotal = oddTilesCount * bfs(start, size - 1, getNeighbors).size

        // We can make the assumption that we enter a given tile from a fixed point. For partially
        // covered tiles, calculate the distance already covered for each type
        val innerStraightStart = start.x + (reach - 1) * size + 1
        val outerStraightStart = start.x + reach * size + 1
        val diagonalStart = (2 * size) * ((steps - (size + 1)) / (2 * size)) + size + 1
        val offsetDiagonalStart = (2 * size) * ((steps - 1) / (2 * size)) + 1

        // Subtract the already covered distance from the total steps desired to determine the
        // threshold to search to within the tile. The number of garden plots reachable depends on
        // where we start from, so we calculate 4 cases for each of the 4 different partial
        // coverage types.
        val edgeDiscovered = listOf(
            // inner straight - first partial tile directly straight from the center
            bfs(start.x to size - 1, steps - innerStraightStart, getNeighbors),
            bfs(size - 1 to start.y, steps - innerStraightStart, getNeighbors),
            bfs(start.x to 0, steps - innerStraightStart, getNeighbors),
            bfs(0 to start.y, steps - innerStraightStart, getNeighbors),
            // outer straight - second partial tile directly straight from the center (if any)
            bfs(start.x to size - 1, steps - outerStraightStart, getNeighbors),
            bfs(size - 1 to start.y, steps - outerStraightStart, getNeighbors),
            bfs(start.x to 0, steps - outerStraightStart, getNeighbors),
            bfs(0 to start.y, steps - outerStraightStart, getNeighbors),
            // inner diagonal - partial diagonals co-linear with inner straight tiles
            bfs(0 to 0, steps - diagonalStart, getNeighbors),
            bfs(size - 1 to 0, steps - diagonalStart, getNeighbors),
            bfs(size - 1 to size - 1, steps - diagonalStart, getNeighbors),
            bfs(0 to size - 1, steps - diagonalStart, getNeighbors),
            // offset diagonal - partial diagonals co-linear with outer striahg tiles
            bfs(0 to 0, steps - offsetDiagonalStart, getNeighbors),
            bfs(size - 1 to 0, steps - offsetDiagonalStart, getNeighbors),
            bfs(size - 1 to size - 1, steps - offsetDiagonalStart, getNeighbors),
            bfs(0 to size - 1, steps - offsetDiagonalStart, getNeighbors),
        ).map { it.size }.map(Int::toLong)
        val cornerTotal =
            edgeDiscovered.zip(
                // The number of times to duplicate each part of the search
                listOf(1, 1, reach - 1, reach).flatMap { factor -> List(4) { factor } }
            ).sumOf { (size, factor) ->
                size * factor
            }
        return cornerTotal + evenTotal + oddTotal
    }

    @Test
    fun testExample() {
        val input = """
            ...........
            .....###.#.
            .###.##..#.
            ..#.#...#..
            ....#.#....
            .##..S####.
            .##..#...#.
            .......##..
            .##.#.####.
            .##..##.##.
            ...........
        """.trimIndent()

        val matrix = input.asMatrix<Char>()
        val indices = matrix.indices2D
        val start = indices.single { matrix[it] == 'S' }
        indices.filter {
            it.manhattanDistanceTo(start) % 2 == 0
        }.filter {
            matrix[it] == '.' || matrix[it] == 'S'
        }.also {
            println(it.size)
        }

        assertEquals(16, getStepsIn(input, 6))
        assertEquals(1594, getStepsIn(input, 50))
        assertEquals(167004, getStepsIn(input, 500))
    }
}