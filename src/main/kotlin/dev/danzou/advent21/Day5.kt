package dev.danzou.advent21

import dev.danzou.advent.utils.geometry.Point
import dev.danzou.advent.utils.geometry.x
import dev.danzou.advent.utils.geometry.y
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

internal class Day5 : AdventTestRunner21() {
    data class Line(val p1: Point, val p2: Point) {
        fun getPoints(): List<Point> = when {
            p1.x == p2.x || p1.y == p2.y -> ((min(p1.x, p2.x)..max(p1.x, p2.x)).map { Point(it, p1.y) } +
                    (min(p1.y, p2.y)..max(p1.y, p2.y)).map { Point(p1.x, it) })
                .distinct()

            abs(p1.x - p2.x) == abs(p1.y - p2.y) -> (0..abs(p1.x - p2.x))
                .map { Point(p1.x + it * (p2.x - p1.x).sign, p1.y + it * (p2.y - p1.y).sign) }

            else -> emptyList()
        }

        fun isDiagonal(): Boolean =
            !(p1.x == p2.x || p1.y == p2.y)
    }

    private fun getLines(input: String): List<Line> =
        input.split("\n").map {
            Regex("(\\d+),(\\d+) -> (\\d+),(\\d+)")
                .matchEntire(it)!!
                .destructured
                .toList()
                .map { s -> s.toInt() }
        }
            .map { Line(Point(it[0], it[1]), Point(it[2], it[3])) }

    override fun part1(input: String): Int =
        getLines(input)
            .filter { !it.isDiagonal() }
            .map { it.getPoints() }
            .flatten()
            .groupingBy { it }
            .eachCount()
            .filter { it.value >= 2 }
            .count()


    override fun part2(input: String): Int =
        getLines(input)
            .map { it.getPoints() }
            .flatten()
            .groupingBy { it }
            .eachCount()
            .filter { it.value >= 2 }
            .count()

}