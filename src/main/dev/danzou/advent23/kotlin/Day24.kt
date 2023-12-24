package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry3.toTriple
import dev.danzou.advent23.AdventTestRunner23
import org.apache.commons.math3.linear.LUDecomposition
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.SingularValueDecomposition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day24 : AdventTestRunner23() {

    fun getPos(
        hailstone: Pair<Triple<Long, Long, Long>, Triple<Long, Long, Long>>,
        time: Double
    ): Triple<Double, Double, Double> {
        return Triple(
            hailstone.first.first + hailstone.second.first * time,
            hailstone.first.second + hailstone.second.second * time,
            hailstone.first.third + hailstone.second.third * time
        )
    }

    override fun part1(input: String): Int {
        val hailstones = input.split("\n").map {
            it.split(" @ ")
        }.map { (pos, vel) ->
            Pair(
                pos.split(", ").map { it.toLong() }.toTriple(),
                vel.split(", ").map { it.toLong() }.toTriple()
            )
        }

//        val testArea = 7.0..27.0
        val testArea = 200000000000000.0..400000000000000.0

        return hailstones.pairs().count { (h1, h2) ->
            val A = MatrixUtils.createRealMatrix(
                arrayOf(
                    doubleArrayOf(h1.second.first.toDouble(), -h2.second.first.toDouble()),
                    doubleArrayOf(h1.second.second.toDouble(), -h2.second.second.toDouble())
                )
            )
            val b = MatrixUtils.createColumnRealMatrix(
                listOf(
                    h2.first.first - h1.first.first,
                    h2.first.second - h1.first.second,
                ).map { it.toDouble() }.toDoubleArray()
            )

            try {
                val decomposition = LUDecomposition(A)
                if (decomposition.determinant in -E..E) return@count false
                val x = decomposition.solver.solve(b)
                if (x.getEntry(0, 0) < 0 || x.getEntry(1, 0) < 0) return@count false
                val p1 = getPos(h1, x.getEntry(0, 0))
                val p2 = getPos(h2, x.getEntry(1, 0))
                p1.first in testArea && p1.second in testArea
            } catch (e: Exception) {
                false
            }
        }

    }

    override fun part2(input: String): Any {
        return 0L
    }

    @Test
    fun testExample() {
        val input = """
            19, 13, 30 @ -2, 1, -2
            18, 19, 22 @ -1, -1, -2
            20, 25, 34 @ -2, -2, -4
            12, 31, 28 @ -1, -2, -1
            20, 19, 15 @ 1, -5, -3
        """.trimIndent()

//        assertEquals(2, part1(input))
        assertEquals(null, part2(input))
    }
}