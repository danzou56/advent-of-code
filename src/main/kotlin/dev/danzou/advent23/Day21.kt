package dev.danzou.advent23

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass.Companion.CARDINAL_DIRECTIONS
import dev.danzou.advent.utils.geometry.plus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day21 : AdventTestRunner23("Step Counter") {

    private fun searchWithParity(matrix: Matrix<Char>, start: Pos, max: Int): Set<Pos> =
        bfsWithDistance(start) { cur  ->
            CARDINAL_DIRECTIONS.map { delta ->
                cur + delta
            }.filter { p ->
                p.manhattanDistanceTo(start) <= max
            }.filter { p ->
                val c = matrix.getOrNull(p)
                c == '.' || c == 'S'
            }.toSet()
        }.filter { (_, distance) ->
            distance <= max && distance % 2 == max % 2
        }.keys

    override fun part1(input: String): Int = getStepsIn(input, 64)

    override fun part2(input: String): Long = getALotOfStepsIn(input, 26501365)

    fun getStepsIn(input: String, steps: Int): Int {
        val matrix = input.asMatrix<Char>()
        val start = matrix.indices2D.single { matrix[it] == 'S' }

        return searchWithParity(matrix, start, steps).size
    }

    fun getALotOfStepsIn(input: String, steps: Int): Long {
        val matrix = input.asMatrix<Char>()
        val size = matrix.size
        val start = matrix.indices2D.single { matrix[it] == 'S' }
        require(steps >= start.manhattanDistanceTo(0 to 0))

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
        val evenTotal = evenTilesCount * searchWithParity(matrix, start, size).size
        val oddTilesCount = (2 * (reach / 2)).toLong().pow(2)
        val oddTotal = oddTilesCount * searchWithParity(matrix, start, size - 1).size

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
            searchWithParity(matrix, start.x to size - 1, steps - innerStraightStart),
            searchWithParity(matrix, size - 1 to start.y, steps - innerStraightStart),
            searchWithParity(matrix, start.x to 0, steps - innerStraightStart),
            searchWithParity(matrix, 0 to start.y, steps - innerStraightStart),
            // outer straight - second partial tile directly straight from the center (if any)
            searchWithParity(matrix, start.x to size - 1, steps - outerStraightStart),
            searchWithParity(matrix, size - 1 to start.y, steps - outerStraightStart),
            searchWithParity(matrix, start.x to 0, steps - outerStraightStart),
            searchWithParity(matrix, 0 to start.y, steps - outerStraightStart),
            // inner diagonal - partial diagonals co-linear with inner straight tiles
            searchWithParity(matrix, 0 to 0, steps - diagonalStart),
            searchWithParity(matrix, size - 1 to 0, steps - diagonalStart),
            searchWithParity(matrix, size - 1 to size - 1, steps - diagonalStart),
            searchWithParity(matrix, 0 to size - 1, steps - diagonalStart),
            // offset diagonal - partial diagonals co-linear with outer straight tiles
            searchWithParity(matrix, 0 to 0, steps - offsetDiagonalStart),
            searchWithParity(matrix, size - 1 to 0, steps - offsetDiagonalStart),
            searchWithParity(matrix, size - 1 to size - 1, steps - offsetDiagonalStart),
            searchWithParity(matrix, 0 to size - 1, steps - offsetDiagonalStart),
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

        assertEquals(16, getStepsIn(input, 6))
        // Either 1. the arithmetic in getALotOfStepsIn is wrong, or 2. the structural assumptions
        // made on the test input in getALotOfStepsIn are invalid for the example input (that is,
        // the relative sparseness of obstacles and direct lines from S to the border) so
        // getALotOfStepsIn is known broken on the example cases.
    }
}