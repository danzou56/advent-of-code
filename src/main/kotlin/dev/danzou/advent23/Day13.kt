package dev.danzou.advent23

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.min

internal class Day13 : AdventTestRunner23("Point of Incidence") {

    private fun reflectionIndices(matrix: Matrix<Boolean>): Pair<Collection<Int>, Collection<Int>> {
        // It's much clearer what's going on when this logic is written as list slices, but kotlin
        // list slices aren't particularly fast since new lists are constantly being created(?). We
        // get a 5x speed improvement just by switching colIndices calculation from list slices to
        // list accesses by index (no substantial speedup is observed from making the refactor for
        // rowIndices)
        val colIndices = (1..<matrix[0].size).filter { reflIndex ->
            val size = min(reflIndex, matrix[0].size - reflIndex)
            (reflIndex - 1 downTo reflIndex - size)
                .zip(reflIndex..<reflIndex + size)
                .all { (left, right) ->
                    matrix.all { it[left] == it[right] }
                }
        }
        val rowIndices = (1..<matrix.size).filter { reflIndex ->
            val size = min(reflIndex, matrix.size - reflIndex)
            (reflIndex - 1 downTo reflIndex - size)
                .zip(reflIndex..<reflIndex + size)
                .all { (top, bottom) ->
                    matrix[top] == matrix[bottom]
                }
        }
        return colIndices to rowIndices
    }

    private fun summarize(colIndex: Int, rowIndex: Int): Int = colIndex + 100 * rowIndex

    override fun part1(input: String): Int {
        val matrices = input.split("\n\n").map { it.asMatrix { it == '#' } }

        return matrices.sumOf { matrix ->
            val (colIndex, rowIndex) = reflectionIndices(matrix).toList().map { it.singleOrNull() ?: 0 }
            summarize(colIndex, rowIndex)
        }
    }

    override fun part2(input: String): Int {
        val matrices = input.split("\n\n").map { it.asMatrix { it == '#' } }

        return matrices.sumOf { original ->
            val originalIndices = reflectionIndices(original).toList().map { it.singleOrNull() ?: 0 }
            original.indices2D.asSequence()
                .map { target ->
                    original.mapIndexed2D { pos, b ->
                        if (pos == target) !b
                        else b
                    }
                }.firstNotNullOf { matrix ->
                    val (colIndex, rowIndex) = reflectionIndices(matrix).toList().zip(originalIndices)
                        .map { (new, original) ->
                            (new - original).also { require(it.size <= 1) }.singleOrNull() ?: 0
                        }
                    summarize(colIndex, rowIndex).takeIf { it != 0 }
                }
        }
    }

    @Test
    fun testExample() {
        val input = """
            #.##..##.
            ..#.##.#.
            ##......#
            ##......#
            ..#.##.#.
            ..##..##.
            #.#.##.#.

            #...##..#
            #....#..#
            ..##..###
            #####.##.
            #####.##.
            ..##..###
            #....#..#
        """.trimIndent()

        assertEquals(405, part1(input))
        assertEquals(400, part2(input))
    }
}