package dev.danzou.advent21

import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.plus

internal class Day2 : AdventTestRunner21() {
    override fun part1(input: String): Int =
        input.split("\n")
            .map { it.split(" ") }
            .map { (dir, offset) -> Pair(dir, offset.toInt()) }
            .map { (dir, offset) ->
                when (dir) {
                    "forward" -> Pos(offset, 0)
                    "down" -> Pos(0, offset)
                    "up" -> Pos(0, -offset)
                    else -> throw IllegalArgumentException()
                }
            }
            .reduce(Pos::plus)
            .toList()
            .reduce(Int::times)

    data class Pose(val hor: Long = 0, val depth: Long = 0, val aim: Long = 0) {
        infix operator fun plus(other: Pose): Pose =
            Pose(this.hor + other.hor, this.depth + other.depth, this.aim + other.aim)
    }

    override fun part2(input: String): Long =
        input.split("\n")
            .map { it.split(" ") }
            .map { (dir, offset) -> Pair(dir, offset.toLong()) }
            .fold(Pose()) { pose, (dir, offset) ->
                when (dir) {
                    "forward" -> pose + Pose(hor = offset, depth = pose.aim * offset)
                    "down" -> pose + Pose(aim = offset)
                    "up" -> pose + Pose(aim = -offset)
                    else -> throw IllegalArgumentException()
                }
            }
            .let { (hor, depth, _) ->
                hor * depth
            }
}