package dev.danzou.kotlin

import dev.danzou.kotlin.utils.AdventTestRunner

class Day4 : AdventTestRunner() {
    override fun part1(input: String): Number =
        input.split("\n").map {
            it.split(",", "-").map(String::toInt)
        }.map {
            Pair(it[0]..it[1], it[2]..it[3])
        }.count { (range1, range2) ->
            range1.first <= range2.first && range2.last <= range1.last ||
                    range2.first <= range1.first && range1.last <= range2.last
        }

    override fun part2(input: String): Number =
        input.split("\n").map {
            it.split(",", "-").map(String::toInt)
        }.map {
            Pair(it[0]..it[1], it[2]..it[3])
        }.count {
            (it.first intersect it.second).isNotEmpty()
        }
}