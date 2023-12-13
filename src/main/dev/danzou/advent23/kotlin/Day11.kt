package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent23.AdventTestRunner23
import kotlin.math.max
import kotlin.math.min

internal class Day11 : AdventTestRunner23("Cosmic Expansion") {
    val SPACE = '.'
    val GALAXY = '#'

    fun part1(input: String, expandBy: Long): Long {
        val universe = input.asMatrix<Char>()
        val emptyRows = universe.mapIndexedNotNull { y, row -> y.takeIf { row.all { it == SPACE }} }
        val emptyCols = universe.transpose().mapIndexedNotNull { x, col -> x.takeIf { col.all { it == SPACE } } }

        return universe.indices2D.filter {
            universe[it] == GALAXY
        }.pairs().sumOf { (p1, p2) ->
            val distance = p1.manhattanDistanceTo(p2).toLong()
            val xs = min(p1.x, p2.x)..max(p1.x, p2.x)
            val ys = min(p1.y, p2.y)..max(p1.y, p2.y)
            distance +
                    (expandBy - 1) * emptyCols.count { it in xs } +
                    (expandBy - 1) * emptyRows.count { it in ys }
        }
    }

    override fun part1(input: String): Long = part1(input, 2L)

    override fun part2(input: String): Long = part1(input, 1_000_000L)
}