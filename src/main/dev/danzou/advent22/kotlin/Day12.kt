package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day12 : AdventTestRunner() {
    override fun part1(input: String): Any {
        val matrix: Matrix<Char> = input.split("\n").map { it.split("").filter { it.isNotEmpty() }.map { it.first() } }
        val start: Pos = matrix.indexOfFirst { it.contains('S') }.let { i -> Pair(i, matrix[i].indexOfFirst { it == 'S' }) }
        val end: Pos = matrix.indexOfFirst { it.contains('E') }.let { i -> Pair(i, matrix[i].indexOfFirst { it == 'E' }) }

        val mapper = { c: Char -> when (c) {
            'S' -> 'a'
            'E' -> 'z'
            else -> c
        } }

        val path = doDijkstras(
            start,
            end,
            { cur ->
                matrix.getNeighboringPos(cur.first, cur.second).filter { next ->
                    mapper(matrix[next]) <= mapper(matrix[cur]) + 1
                }.toSet()
            },
            { _, _ -> 1 }
        )

        return path.size - 1
    }

    override fun part2(input: String): Any {
        val matrix: Matrix<Char> = input.split("\n").map { it.split("").filter { it.isNotEmpty() }.map { it.first() } }
        val end: Pos = matrix.indexOfFirst { it.contains('E') }.let { i -> Pair(i, matrix[i].indexOfFirst { it == 'E' }) }

        val mapper = { c: Char -> when (c) {
            'S' -> 'a'
            'E' -> 'z'
            else -> c
        } }

        val paths = matrix.indices2D.filter { p -> matrix[p] == 'a' || matrix[p] == 'S' }.map { start -> doDijkstras(
            start,
            end,
            { cur ->
                matrix.getNeighboringPos(cur.first, cur.second).filter { next ->
                    mapper(matrix[next]) <= mapper(matrix[cur]) + 1
                }.toSet()
            },
            { _, _ -> 1 }
        ) }

        val res = paths.filter { it.isNotEmpty() }.minBy { it.size }.size - 1
        return res
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