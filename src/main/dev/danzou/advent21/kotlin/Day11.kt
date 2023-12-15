package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.Matrix
import dev.danzou.advent.utils.mapIndexed2D
import dev.danzou.advent21.AdventTestRunner21
import kotlin.math.abs
import kotlin.math.max

typealias OctopusMatrix = Matrix<Int>

internal class Day11 : AdventTestRunner21() {

    fun getOctopi(input: String): OctopusMatrix =
        input.split("\n").map { it.map { it.digitToInt() } }

    fun OctopusMatrix.step(): OctopusMatrix =
        this.step1().step2()

    fun OctopusMatrix.step1(): OctopusMatrix = this.map { row -> row.map { cell -> cell + 1 } }
    fun OctopusMatrix.step2(): OctopusMatrix = this.step2(emptySet())
    tailrec fun OctopusMatrix.step2(flashed: Set<Pair<Int, Int>>): OctopusMatrix {
        val toFlash = this.mapIndexed2D { (i, j), v ->
            if (v > 9 && Pair(i, j) !in flashed) Pair(i, j)
            else null
        }.flatten().filterNotNull()
        if (toFlash.isEmpty()) return this

        return this.mapIndexed2D { (i, j), v ->
            if (Pair(i, j) !in flashed) v + toFlash.count { p -> p % Pair(i, j) <= 1 }
            else v
        }.mapIndexed2D { (i, j), v ->
            if (Pair(i, j) in toFlash) 0
            else v
        }.step2(flashed + toFlash.toSet())
    }

    operator fun Pair<Int, Int>.rem(other: Pair<Int, Int>): Int =
        max(abs(this.first - other.first), abs(this.second - other.second))

    override fun part1(input: String): Int =
        (0 until 100).fold(Pair(getOctopi(input), 0)) { (octopi, sum), _ ->
            octopi.step().let { res -> Pair(res, sum + res.flatten().count { it == 0 }) }
        }.second

    override fun part2(input: String): Int {
        fun run(mat: OctopusMatrix, i: Int): Int =
            if (mat.flatten().sum() == 0) i
            else run(mat.step(), i + 1)

        return run(getOctopi(input), 0)
    }
}