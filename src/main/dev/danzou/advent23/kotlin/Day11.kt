package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.math.min

internal class Day11 : AdventTestRunner23() {
    override fun part1(input: String): Int {
        val universe = input.asMatrix<Char>()
        val emptyRows = universe.mapIndexedNotNull { y, row -> if (row.all { it == '.' }) y else null }
        val emptyCols = universe.transpose().mapIndexedNotNull { x, col -> if (col.all { it == '.' }) x else null }
        val expanded = universe.flatMapIndexed { y, row ->
            if (y in emptyRows) listOf(row, row)
            else listOf(row)
        }.transpose().flatMapIndexed { x, col ->
            if (x in emptyCols) listOf(col, col)
            else listOf(col)
        }

        return  expanded.indices2D.filter {
            expanded.get(it) == '#'
        }.choose(2).map { (p1, p2) ->
            p1.manhattanDistanceTo(p2)
        }.sum()
    }

    override fun part2(input: String): Long {
        val universe = input.asMatrix<Char>()
        val emptyRows = universe.mapIndexedNotNull { y, row -> if (row.all { it == '.' }) y else null }
        val emptyCols = universe.transpose().mapIndexedNotNull { x, col -> if (col.all { it == '.' }) x else null }
        val expandBy = 1_000_000L

        return  universe.indices2D.filter {
            universe.get(it) == '#'
        }.choose(2).map { (p1, p2) ->
            val preempt = p1.manhattanDistanceTo(p2).toLong()
            val xRange = min(p1.x, p2.x)..max(p1.x, p2.x)
            val yRange = min(p1.y, p2.y)..max(p1.y, p2.y)
            preempt + (expandBy - 1) * emptyRows.count { it in yRange }.toLong() + (expandBy - 1) * emptyCols.count { it in xRange }
        }.sum()
    }
}