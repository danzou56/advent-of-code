package dev.danzou.advent21

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Pos

internal class Day15 : AdventTestRunner21() {

    private fun lowestPathCost(grid: Matrix<Int>): Int {
        val neighborFunction: NeighborFunction<Pos> = { grid.neighboringPos(it).toSet() }
        val costFunction: CostFunction<Pos> = { _, dst -> grid[dst] }
        val path = doDijkstras(
          init = Pos(0, 0),
          target = { it == Pos(grid.size - 1, grid.first().size - 1) },
          neighborFunction,
          costFunction
        )
        return path.windowed(2).sumOf { costFunction(it[0], it[1]) }
    }

    override fun part1(input: String): Int {
        val grid = input.split("\n").map {
            it.map { it.digitToInt() }
        }
        return lowestPathCost(grid)
    }

    override fun part2(input: String): Int {
        val tile = 5
        val grid = input.split("\n").map {
            it.map { it.digitToInt() }
        }.map { grid ->
            List(grid.size * tile) { index ->
                grid[index % grid.size] + index / grid.size
            }.map { (it - 1) % 9 + 1 }
        }.let { row ->
            List(row.size * tile) { index ->
                row[index % row.size]
                    .map { it + index / row.size }
                    .map { (it - 1) % 9 + 1 }
            }
        }
        return lowestPathCost(grid)
    }
}