package dev.danzou.advent24

import dev.danzou.advent.utils.geometry.PosL
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.toPair
import kotlin.math.abs
import kotlin.math.roundToLong
import org.apache.commons.math3.linear.LUDecomposition
import org.apache.commons.math3.linear.MatrixUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day13 : AdventTestRunner24("Claw Contraption") {

  fun getMachines(input: String): List<PrizeMachine> =
      input
          .split("\n\n")
          .map { it.lines() }
          .map { (a, b, prize) ->
            PrizeMachine(
                Regex("\\d+").findAll(a).toList().map { it.value.toLong() }.toPair(),
                Regex("\\d+").findAll(b).toList().map { it.value.toLong() }.toPair(),
                Regex("\\d+").findAll(prize).toList().map { it.value.toLong() }.toPair(),
            )
          }

  data class PrizeMachine(val a: PosL, val b: PosL, val prize: PosL) {
    /** Solve the system */
    fun solve(): PosL? {
      val M =
          MatrixUtils.createRealMatrix(
              arrayOf(
                  doubleArrayOf(a.first.toDouble(), b.first.toDouble()),
                  doubleArrayOf(a.second.toDouble(), b.second.toDouble()),
              )
          )
      val b =
          MatrixUtils.createColumnRealMatrix(
              doubleArrayOf(prize.first.toDouble(), prize.second.toDouble())
          )

      val presses = LUDecomposition(M).solver.solve(b)
      return presses
          .getColumn(0)
          .map {
            if (abs(it.roundToLong().toDouble() - it) < 1E-4) it.roundToLong()
            else return null
          }
          .toPair()
    }

    companion object {
      fun tokens(a: Long, b: Long): Long = a * 3 + b
    }
  }

  override fun part1(input: String): Long {
    val prizeMachines = getMachines(input)
    return prizeMachines
        .mapNotNull { it.solve() }
        .sumOf { (a, b) -> PrizeMachine.tokens(a, b) }
  }

  override fun part2(input: String): Long {
    val prizeMachines =
        getMachines(input).map {
          it.copy(prize = it.prize + (10000000000000 to 10000000000000))
        }

    return prizeMachines
        .mapNotNull { it.solve() }
        .sumOf { (a, b) -> PrizeMachine.tokens(a, b) }
  }

  @Test
  fun testExample() {
    """
      Button A: X+94, Y+34
      Button B: X+22, Y+67
      Prize: X=8400, Y=5400
      
      Button A: X+26, Y+66
      Button B: X+67, Y+21
      Prize: X=12748, Y=12176
      
      Button A: X+17, Y+86
      Button B: X+84, Y+37
      Prize: X=7870, Y=6450
      
      Button A: X+69, Y+23
      Button B: X+27, Y+71
      Prize: X=18641, Y=10279
    """
        .trimIndent()
        .let { input ->
          assertEquals(480L, part1(input))
          //          assertEquals(null, part2(input))
        }
  }
}
