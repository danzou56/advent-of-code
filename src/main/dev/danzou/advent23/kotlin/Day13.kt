package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.asMatrix
import dev.danzou.advent.utils.indices2D
import dev.danzou.advent.utils.map2D
import dev.danzou.advent.utils.mapIndexed2D
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.min

internal class Day13 : AdventTestRunner23() {
    override fun part1(input: String): Any {
        val matrices = input.split("\n\n").map { it.asMatrix<Char>() }

        return matrices.map { matrix ->
            val colIndices = (1..<matrix[0].size).filter { reflIndex ->
//                println(reflIndex)
                val left = matrix.map { it.slice(0..<reflIndex) }
                    .map { it.reversed() }
                val right = matrix.map { it.slice(reflIndex..<matrix[0].size) }
                val size = min(left[0].size, right[0].size)
                right.map { it.slice(0..<size) } == left.map { it.slice(0..<size) }
            }

            val rowIndices = (1..<matrix.size).filter { reflIndex ->
                val top = matrix.take(reflIndex).reversed()
                val bottom = matrix.drop(reflIndex)
                val size = min(top.size, bottom.size)
                top.take(size) == bottom.take(size)
            }

            colIndices.sum() + rowIndices.sumOf { it * 100 }
        }.sum()
    }

    override fun part2(input: String): Any {
        val matrices = input.split("\n\n").map { it.asMatrix<Char>() }

        return matrices.map { original ->
            val (origSum, origCol, origRow) = original.let { matrix ->
                val colIndices = (1..<matrix[0].size).filter { reflIndex ->
//                println(reflIndex)
                    val left = matrix.map { it.slice(0..<reflIndex) }
                        .map { it.reversed() }
                    val right = matrix.map { it.slice(reflIndex..<matrix[0].size) }
                    val size = min(left[0].size, right[0].size)
                    right.map { it.slice(0..<size) } == left.map { it.slice(0..<size) }
                }

                val rowIndices = (1..<matrix.size).filter { reflIndex ->
                    val top = matrix.take(reflIndex).reversed()
                    val bottom = matrix.drop(reflIndex)
                    val size = min(top.size, bottom.size)
                    top.take(size) == bottom.take(size)
                }

                Triple(colIndices.sum() + rowIndices.sumOf { it * 100 }, colIndices, rowIndices)
            }
            val res = original.indices2D.map { target ->
                original.mapIndexed2D { pos, c ->
                    when (pos) {
                        target -> if (c == '.') '#' else '.'
                        else -> c
                    }
                }
            }.map { matrix ->
                val colIndices = (1..<matrix[0].size).filter { reflIndex ->
                    val left = matrix.map { it.slice(0..<reflIndex) }
                        .map { it.reversed() }
                    val right = matrix.map { it.slice(reflIndex..<matrix[0].size) }
                    val size = min(left[0].size, right[0].size)
                    right.map { it.slice(0..<size) } == left.map { it.slice(0..<size) }
                }.filter { it !in origCol }

                val rowIndices = (1..<matrix.size).filter { reflIndex ->
                    val top = matrix.take(reflIndex).reversed()
                    val bottom = matrix.drop(reflIndex)
                    val size = min(top.size, bottom.size)
                    top.take(size) == bottom.take(size)
                }.filter { it !in origRow }

                (colIndices.sum() + rowIndices.sumOf { it * 100 })
            }.toSet() - origSum - 0
            res.single()
        }.sum()
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