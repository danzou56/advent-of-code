package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day12 : AdventTestRunner22() {

    fun parse(input: String): Triple<Matrix<Char>, Pos, Pos> =
        input.split("\n").map { it.toList() }.let { matrix ->
            Triple(
                matrix.map2D { c ->
                    when (c) {
                        'S' -> 'a'
                        'E' -> 'z'
                        else -> c
                    }
                },
                matrix.indexOfFirst { it.contains('S') }.let { i -> Pos(i, matrix[i].indexOf('S')) },
                matrix.indexOfFirst { it.contains('E') }.let { i -> Pos(i, matrix[i].indexOf('E')) },
            )
        }

    override fun part1(input: String): Any {
        val (matrix, start, end) = parse(input)

        val path = doDijkstras(
            start,
            { it == end },
            { cur ->
                matrix.neighboringPos(cur).filter { next ->
                    matrix[next] <= matrix[cur] + 1
                }.toSet()
            }
        )

        return path.size - 1
    }

    override fun part2(input: String): Any {
        val (matrix, _, end) = parse(input)

        val paths = matrix.indices2D.filter { p -> matrix[p] == 'a' }.map { start ->
            doDijkstras(
                start,
                { it == end },
                { cur ->
                    matrix.neighboringPos(cur).filter { next ->
                        matrix[next] <= matrix[cur] + 1
                    }.toSet()
                }
            )
        }

        return paths.filter { it.isNotEmpty() }.minBy { it.size }.size - 1
    }

    @Test
    fun testExample() {
        val input = """
            Sabqponm
            abcryxxl
            accszExk
            acctuvwj
            abdefghi
        """.trimIndent()

        assertEquals(31, part1(input))
        assertEquals(29, part2(input))
    }
}