package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.gaussianSum
import dev.danzou.advent21.AdventTestRunner21
import kotlin.math.*

internal class Day17 : AdventTestRunner21("Trick Shot") {
    val E = 1e-5

    fun getBounds(input: String): List<Int> =
        Regex("""target area: x=(\d+)\.\.(\d+), y=(-\d+)\.\.(-\d+)""")
            .find(input)!!
            .destructured
            .toList()
            .map(String::toInt)

    fun quadSolve(a: Double, b: Double, c: Double): List<Double> {
        val discriminant = b * b - 4 * a * c
        return when {
            discriminant < -E -> emptyList()
            discriminant in -E..E -> listOf(-b / (2 * a))
            else -> listOf(-b + sqrt(discriminant), -b - sqrt(discriminant)).map { it / (2 * a) }
        }
    }

    fun simulateUntilPeak(v_x0: Int, v_y0: Int): Int {
        var s_x = 0
        var s_y = 0
        var v_x = v_x0
        var v_y = v_y0
        while (true) {
            s_x += v_x
            s_y += v_y
            v_x = when {
                v_x > 0 -> v_x - 1
                v_x < 0 -> v_x + 1
                else -> 0
            }
            v_y -= 1
            if (v_y == 0) return s_y
        }
    }

    /**
     * Returns all integer solutions for t of
     *
     * target_m     = -1/2 t^2 + v_x0 t
     * target_m + 1 = -1/2 t^2 + v_x0 t
     * ...
     * target_M     = -1/2 t^2 + v_x0 t
     *
     * additionally constrained such that v_x(t) >= 0
     */
    fun getTimesForInitialXVelocity(v_x0: Int, target: ClosedRange<Int>): ClosedRange<Int> {
        val t_min = ceil(
            quadSolve(
                a = 1.0,
                b = -2.0 * v_x0 - 1.0,
                c = 2.0 * target.start
            ).minOrNull() ?: return IntRange.EMPTY // projectile can't reach target
        ).toInt()
        val t_max = min(
            floor(
                quadSolve(
                    a = 1.0,
                    b = -2.0 * v_x0 - 1.0,
                    c = 2.0 * target.endInclusive
                ).minOrNull() ?: v_x0.toDouble() // projectile can't reach far end of target
            ), v_x0.toDouble()
        ).toInt()
        return t_min..t_max
    }

    /**
     * Returns all integer solutions for t of
     *
     * target_m     = -1/2 t^2 + v_y0 t
     * target_m + 1 = -1/2 t^2 + v_y0 t
     * ...
     * target_M     = -1/2 t^2 + v_y0 t
     *
     * additionally constrained such that t >= 0
     */
    fun getTimesForInitialYVelocity(v_y0: Int, target: ClosedRange<Int>): ClosedRange<Int> {
        assert(target.endInclusive <= 0)
        // Due to the exact kinematic nature of this problem, we can guarantee
        // that there is a unique solution, allowing us to use .single()
        val t_min = ceil(
            quadSolve(
                a = 1.0,
                b = -2.0 * v_y0 - 1.0,
                c = 2.0 * target.endInclusive
            ).filter { t -> t >= 0 }.single()
        ).toInt()
        val t_max = floor(
            quadSolve(
                a = 1.0,
                b = -2.0 * v_y0 - 1.0,
                c = 2.0 * target.start
            ).filter { t -> t >= 0 }.single()
        ).toInt()
        return t_min..t_max
    }

    override fun part1(input: String): Any {
        val (_, _, y_min, _) = getBounds(input)
        return simulateUntilPeak(0, -y_min - 1)
    }

    override fun part2(input: String): Any {
        val (x_min, x_max, y_min, y_max) = getBounds(input)
        val targetXRange = x_min..x_max
        val targetYRange = y_min..y_max
        val `max{v_y0}` = -y_min - 1

        // min{v_x0} = 1 (more precisely, whatever solves `min{v_x0}`.gaussianSum() == x_min)
        // max{v_x0} = x_max (otherwise we overshoot on first step)
        val timeRangesFromX = (1..x_max).associateWith { v_x0 ->
            getTimesForInitialXVelocity(v_x0, targetXRange).let { times ->
                when {
                    times.isEmpty() -> IntRange.EMPTY
                    // When time is exactly v_x0, then v_x becomes 0 within the target range, and
                    // the projectile could be inside of target for any time [v_x0, Inf).
                    // We can stop the range at the Gaussian sum max{v_y0} as that is the amount
                    // of time it takes the projectile to travel in the full arc
                    times.endInclusive == v_x0 ->  times.start..`max{v_y0}`.gaussianSum()
                    else -> times
                }
            }
        }
        // min{v_y0} = y_min (otherwise we overshoot on first step)
        // max{v_y0} = -y_min - 1 (we return to y=0, then overshoot on next step)
        val timeRangesFromY = (y_min..`max{v_y0}`).associateWith { v_y0 ->
            getTimesForInitialYVelocity(v_y0, targetYRange)
        }

        val initialVelocities = timeRangesFromX.flatMap { (v_x0, tRangeFromX) ->
            timeRangesFromY.filter { (_, tRangeFromY) ->
                max(tRangeFromX.start, tRangeFromY.start) <= min(tRangeFromX.endInclusive, tRangeFromY.endInclusive)
            }.map { v_y0 -> Pair(v_x0, v_y0) }
        }

        return initialVelocities.size
    }
}