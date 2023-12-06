package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.quadSolve
import dev.danzou.advent23.AdventTestRunner23
import kotlin.math.ceil
import kotlin.math.floor

internal class Day6 : AdventTestRunner23() {
    // completely overbuilt for the problem because we can actually just brute force try all values
    fun waysToWin(time: Long, distance: Long): Long {
        fun f(t: Double): Double = t * (time - t)
        // d := distance, t := button time, T := race duration
        // solve quadratic t * (T - t) > d for unknown t
        val solutions = quadSolve(-1.0, time.toDouble(), -distance.toDouble())
        require(solutions.size == 2)

        return (1 + floor(solutions.max()) - ceil(solutions.min())).toLong()
    }

    override fun part1(input: String): Long {
        val (times, distances) = input.split("\n")
            .map { Regex("\\d+").findAll(it).map { it.value.toInt() }.toList() }
        return times.zip(distances).map { (time, distance) -> waysToWin(time.toLong(), distance.toLong()) }
            .reduce(Long::times)
    }

    override fun part2(input: String): Long {
        val (time, distance) = input.split("\n")
            .map { Regex("\\d+").findAll(it).map { it.value }.reduce(String::plus).toLong() }
        return waysToWin(time, distance)
    }
}