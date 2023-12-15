package dev.danzou.advent21.kotlin

import dev.danzou.advent21.AdventTestRunner21
import kotlin.math.abs

internal class Day7 : AdventTestRunner21() {
    override fun part1(input: String): Any {
        val crabs = input.split(",").map { it.toInt() }

        return (crabs.min()..crabs.max()).minOf { target ->
            crabs.sumOf { crab -> abs(target - crab) }
        }
    }

    override fun part2(input: String): Any {
        val crabs = input.split(",").map { it.toInt() }

        return (crabs.min()..crabs.max()).minOf { target ->
            crabs.sumOf { crab -> abs(target - crab).let { dist ->
                dist * (dist + 1) / 2
            } }
        }
    }
}