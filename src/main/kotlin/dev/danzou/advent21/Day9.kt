package dev.danzou.advent21

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class Day9 : AdventTestRunner21() {

    fun getHeightMap(input: String): Matrix<Int> =
        input.split("\n").map {
            it.split("")
                .drop(1).dropLast(1)
                .map { it.toInt() }
        }

    val Matrix<Int>.basins: List<Pos>
        get() = this.indices2D.filter { cur ->
            this.neighboring(cur).all { this[cur] < it }
        }

    override fun part1(input: String): Int =
        getHeightMap(input).let { heightMap ->
            heightMap.basins.map { heightMap[it] }
        }.sumOf { it + 1 }

    override fun part2(input: String): Int =
        getHeightMap(input).run {
            this.basins.map { init ->
              dfs(init) { cur ->
                this.neighboringPos(cur).filter { neighbor ->
                  this[neighbor] > this[cur] && this[neighbor] != 9
                }.toSet()
              }
            }
        }
            .map { it.size }
            .sortedDescending()
            .take(3)
            .reduce(Int::times)

    @Test
    fun testExample() {
        val input = """
            2199943210
            3987894921
            9856789892
            8767896789
            9899965678
        """.trimIndent()

      Assertions.assertEquals(15, part1(input))
      Assertions.assertEquals(1134, part2(input))
    }
}