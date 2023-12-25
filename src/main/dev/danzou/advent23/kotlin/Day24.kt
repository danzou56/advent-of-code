package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry3.*
import dev.danzou.advent23.AdventTestRunner23
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.linear.LUDecomposition
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.round

internal class Day24 : AdventTestRunner23("Never Tell Me The Odds") {

    data class Hailstone(val pos: Pos3L, val vel: Pos3L) {
        fun move(time: Double): Triple<Double, Double, Double> = Triple(
            pos.x + vel.x * time,
            pos.y + vel.y * time,
            pos.z + vel.z * time
        )

        companion object {
            fun fromString(input: String): Hailstone =
                input.split(" @ ").let { (pos, vel) ->
                    Hailstone(
                        pos.split(", ").map { it.toLong() }.toTriple(),
                        vel.split(", ").map { it.toLong() }.toTriple()
                    )
                }
        }
    }

    override fun part1(input: String): Int =
        part1(input, 200000000000000.0..400000000000000.0)

    fun part1(input: String, testArea: ClosedRange<Double>): Int {
        val hailstones = input.split("\n").map(Hailstone::fromString)

        return hailstones.pairs().count { (h1, h2) ->

            val A = MatrixUtils.createRealMatrix(
                arrayOf(
                    doubleArrayOf(h1.vel.x.toDouble(), -h2.vel.x.toDouble()),
                    doubleArrayOf(h1.vel.y.toDouble(), -h2.vel.y.toDouble())
                )
            )
            val b = MatrixUtils.createColumnRealMatrix(
                listOf(
                    h2.pos.x - h1.pos.x,
                    h2.pos.y - h1.pos.y,
                ).map { it.toDouble() }.toDoubleArray()
            )

            val decomposition = LUDecomposition(A)
            // When matrix is singular, hailstones are parallel and never intersect
            if (decomposition.determinant in -E..E) return@count false

            // Find time parameters that solve for intersection (but not necessarily collision)
            val t = decomposition.solver.solve(b)

            // If any time parameter is negative, intersection occurs "before" hailstones start
            // moving
            if (t.getColumn(0).any { it < 0 }) return@count false

            val p1 = h1.move(t.getEntry(0, 0))
            val p2 = h2.move(t.getEntry(1, 0))
            // IEEE double precision is about 15-17 digits. Since these results are about 1e14,
            // the lower digits frequently don't match
            require(p1.x - p2.x in -10.0..10.0)
            require(p1.y - p2.y in -100.0..100.0)
            p1.x in testArea && p1.y in testArea
        }
    }

    override fun part2(input: String): Long =
        part2Alt(input)

    /**
     * The solution I used to submit my first correct answer to AOC. I eventually gave up on
     * trying to transform my system of non-linear equations into a system of linear equations, so
     * I just gave it to Mathematica to solve.
     *
     * Make sure you trust this code before running it - it runs wolframscript via the shell!
     */
    fun part2WithWolfram(input: String): Long {
        val hailstones = input.split("\n").map(Hailstone::fromString).take(3)

        val eqs = hailstones.flatMapIndexed { i, h ->
            "xyz".toList().zip(listOf<(Pos3L) -> Long>(Pos3L::x, Pos3L::y, Pos3L::z))
                .map { (name, accessor) ->
                    "${accessor(h.vel)} Subscript[t, $i] + ${accessor(h.pos)} == " +
                            "Subscript[v, $name] Subscript[t, $i] + Subscript[b, $name]"
                }
        }
        val wolframCode = """
            { Subscript[b, x] + Subscript[b, y] + Subscript[b, z] } /. Solve[
                ${eqs.joinToString(" &&\n")}, 
                {
                    Subscript[t, 0],
                    Subscript[t, 1],
                    Subscript[t, 2],
                    Subscript[v, x],
                    Subscript[v, y],
                    Subscript[v, z],
                    Subscript[b, x],
                    Subscript[b, y],
                    Subscript[b, z]
                }
            ]
        """.trimIndent()

        val process = Runtime.getRuntime().exec(
            arrayOf(
                "wolframscript",
                "-code",
                wolframCode
            )
        )
        val output = process.inputStream.bufferedReader().lineSequence().joinToString("\n")
        val exit = process.waitFor()
        assert(exit == 0) { "wolframscript exited with non-zero value $exit" }
        require(output.startsWith("{{") && output.endsWith("}}"))

        return output.drop(2).dropLast(2).toLong()
    }

    /**
     * An alternative solution that solves a system of linear equations resulting from the original
     * system. It's been too long since I've done vector algebra with cross products so this didn't
     * come to me without finding the solution on Reddit.
     *
     * Source: https://www.reddit.com/r/adventofcode/comments/18pnycy/2023_day_24_solutions/kepu26z/
     *
     * Rearrange the vector equality
     *                      pᵣ + vᵣtᵢ = pᵢ + vᵢtᵢ
     *                        pᵣ - pᵢ = (vᵢ + vᵣ)tᵢ    (rearrange)
     *          (pᵣ - pᵢ) × (vᵢ + vᵣ) = 0              (cross product of parallel vectors is 0)
     *                   (expand LHS) = 0
     * Taking i = j, k, l (i.e. any three hailstones), use the common pᵣ×vᵣ term to cancel terms
     * and reduce the three vector equations to two equations with linear terms pᵣ, vᵣ. This gives
     * 6 linear equations with 6 unknowns.
     *
     * For this implementation, we choose i = 1, 2, 3 and use the second equation with i = 2 to
     * cancel out the common term such that the two equations have pairings i = 1, 2 and i = 3, 2.
     */
    fun part2Alt(input: String): Long {
        val hailstones = input.split("\n").map(Hailstone::fromString).take(3)

        // x, y, z ->                       y, z, x             z, x, y
        val crossProductIndexOrder = listOf(1, 2, 0).zip(listOf(2, 0, 1))

        /**
         * Computes the matrix block corresponding to v×(h₁-h₂) where v is unknown and is presumed
         * to be a part of the right hand multiplicand column vector and h₁, h₂ are known vectors.
         */
        fun crossProductMatrixBlock(h1: DoubleArray, h2: DoubleArray): RealMatrix {
            return MatrixUtils.createRealMatrix(3, 3).apply {
                crossProductIndexOrder.mapIndexed { i, (i1, i2) ->
                    assert(getEntry(i, i1) == 0.0)
                    setEntry(i, i1, h1[i2] - h2[i2])
                    assert(getEntry(i, i2) == 0.0)
                    setEntry(i, i2, -h1[i1] - -h2[i1])
                }
            }
        }

        val (h1P, h2P, h3P) = hailstones.map {
            it.pos.toList().map(Long::toDouble).toDoubleArray()
        }
        val (h1V, h2V, h3V) = hailstones.map {
            it.vel.toList().map(Long::toDouble).toDoubleArray()
        }

        val A = MatrixUtils.createRealMatrix(6, 6).apply {
            setSubMatrix(crossProductMatrixBlock(h1P, h2P).data, 0, 0)
            setSubMatrix(crossProductMatrixBlock(h3P, h2P).data, 3, 0)

            setSubMatrix(crossProductMatrixBlock(h1V, h2V).scalarMultiply(-1.0).data, 0, 3)
            setSubMatrix(crossProductMatrixBlock(h3V, h2V).scalarMultiply(-1.0).data, 3, 3)
        }

        val cross1 = Vector3D(h1P).crossProduct(Vector3D(h1V))
        val cross2 = Vector3D(h2P).crossProduct(Vector3D(h2V))
        val cross3 = Vector3D(h3P).crossProduct(Vector3D(h3V))
        // Cross product is a part of a different package with apache-commons-math so we have to
        // do this awkward transition between the two
        val b = MatrixUtils.createColumnRealMatrix(
            cross2.subtract(cross1).toArray() + cross2.subtract(cross3).toArray()
        )

        val r = LUDecomposition(A).solver.solve(b)

        // I thought that the way the above is all set up, the positions should be in the first 3
        // entries, but for some reason they end up in the last 3 entries
        return r.getColumn(0)
            .takeLast(3)
            .map(::round)  // Precision isn't great - rounding suffices
            .map(Double::toLong)
            .sum()
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

        assertEquals(2, part1(input, 7.0..27.0))
        assertEquals(47, part2(input))
    }
}