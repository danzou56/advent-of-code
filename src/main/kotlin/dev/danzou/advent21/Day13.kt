package dev.danzou.advent21

import dev.danzou.advent.utils.AsciiArt

class Day13 : AdventTestRunner21() {

    private fun getDots(lines: List<String>): List<Pair<Int, Int>> =
        lines
            .take(lines.indexOf(""))
            .map { it.split(",") }
            .map { (p1, p2) -> Pair(p1.toInt(), p2.toInt()) }

    private fun getFolds(lines: List<String>): List<Pair<String, Int>> =
        lines.drop(lines.indexOf("") + 1)
            .map { Regex("""fold along ([xy])=(\d+)""").matchEntire(it)!! }
            .map { it.destructured }
            .map { Pair(it.component1(), it.component2().toInt()) }

    private fun fold(dots: List<Pair<Int, Int>>, axis: Pair<String, Int>): List<Pair<Int, Int>> =
        dots.map { (x, y) ->
            when {
                axis.first == "x" && x > axis.second -> Pair(2 * axis.second - x, y)
                axis.first == "y" && y > axis.second -> Pair(x, 2 * axis.second - y)
                else -> Pair(x, y)
            }
        }.distinct()

    override fun part1(input: String): Any {
        val dots = getDots(input.split("\n"))
        val folds = getFolds(input.split("\n"))

        return fold(dots, folds.first()).size
    }

    override fun part2(input: String): Any {
        val dots = getDots(input.split("\n"))
        val folds = getFolds(input.split("\n"))

        val finalDots = folds.fold(dots) { dots, axis -> fold(dots, axis) }
        return AsciiArt.fromOccupied(finalDots)
    }
}