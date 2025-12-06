package dev.danzou.advent25

import dev.danzou.advent.utils.*
import kotlin.collections.map
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private typealias CProblem = Pair<List<Long>, (Long, Long) -> Long>

internal class Day6 : AdventTestRunner25("Trash Compactor") {

  fun getReducer(c: Char): (Long, Long) -> Long =
      when (c) {
        '+' -> Long::plus
        '*' -> Long::times
        else -> throw IllegalArgumentException()
      }

  fun solve(problems: List<CProblem>): Long =
      problems.sumOf { (operands, operator) -> operands.reduce(operator) }

  fun parseProblemsPart1(input: String): List<CProblem> =
      input
          .lines()
          .map {
            it.replace(Regex("\\s+"), " ")
                .dropWhile { it == ' ' }
                .dropLastWhile { it == ' ' }
                .split(" ")
          }
          .let { lines ->
            val operands = lines.dropLast(1)
            val operators = lines.last().map(String::single).map(::getReducer)
            operands.map { it.map(String::toLong) }.transpose().zip(operators)
          }

  fun parseProblemsPart2(input: String): List<CProblem> {
    val problemInput = input.asMatrix<Char>()
    val columns = problemInput[0].size

    /**
     * Determine the operands that are at and to the right of the specified column and
     * also the next column index to start searching for the next operands
     */
    tailrec fun parseProblemOperands(
        column: Int,
        operands: List<Long> = emptyList(),
    ): Pair<List<Long>, Int> {
      // Coerce out of bounds to empty column
      val curCol = problemInput.map { it.getOrNull(column) ?: ' ' }
      // If the current column is empty, we've reached the end of this set of operands.
      // Return the
      // current operands and the next column to indicate where the next problem starts
      if (curCol.all { it == ' ' }) return operands to column + 1

      // Determine the number represented by the current column
      val curOperand =
          curCol.dropLast(1).fold(0L) { operand, digit ->
            when (digit) {
              ' ' -> operand
              else -> operand * 10L + digit.digitToInt().toLong()
            }
          }
      return parseProblemOperands(column + 1, operands + curOperand)
    }

    fun parseAllOperands(column: Int = 0): List<List<Long>> {
      if (column >= columns) return emptyList()

      val (operands, nextColumn) = parseProblemOperands(column)
      return listOf(operands) + parseAllOperands(nextColumn)
    }

    val operators = problemInput.last().filter { it != ' ' }.map(::getReducer)
    return parseAllOperands().zip(operators)
  }

  override fun part1(input: String): Long = solve(parseProblemsPart1(input))

  override fun part2(input: String): Long = solve(parseProblemsPart2(input))

  @Test
  fun testExample() {
    """
      123 328  51 64 
       45 64  387 23 
        6 98  215 314
      *   +   *   +  
    """
        .trimIndent()
        .let { input ->
          assertEquals(4277556L, part1(input))
          assertEquals(3263827L, part2(input))
        }
  }
}
