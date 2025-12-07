package dev.danzou.advent20

import dev.danzou.advent20.Day18.Token.LParen
import dev.danzou.advent20.Day18.Token.Number
import dev.danzou.advent20.Day18.Token.Operator
import dev.danzou.advent20.Day18.Token.RParen
import java.util.Stack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day18 : AdventTestRunner20("Operation Order") {

  sealed class Token {
    class LParen : Token() { override fun toString() = "(" }
    class RParen : Token() { override fun toString() = ")" }
    class Number(val n: Long) : Token() { override fun toString() = "$n" }
    class Operator(val op: (Long, Long) -> Long, val underlying: Char) : Token() {
      override fun toString() = "$underlying"
    }

    companion object {
      fun fromChar(c: Char): Token? =
          when (c) {
            '(' -> LParen()
            ')' -> RParen()
            '+' -> Operator(Long::plus, c)
            '*' -> Operator(Long::times, c)
            c if c.isDigit() -> Number(c.digitToInt().toLong())
            else -> null
          }
    }
  }

  fun evaluate1(expression: List<Token>): Long {
    val stack = Stack<Token>()

    tailrec fun reduce(expression: List<Token>): Long =
        when (val el = expression.firstOrNull()) {
          null -> (stack.pop() as Number).n
          is RParen -> {
            val operand2 = stack.pop() as Number
            stack.pop() as LParen
            reduce(listOf(operand2) + expression.drop(1))
          }
          is Number if stack.lastOrNull() is Operator -> {
            val operator = stack.pop() as Operator
            val operand1 = stack.pop() as Number
            stack.push(Number(operator.op(operand1.n, el.n)))
            reduce(expression.drop(1))
          }
          is Number,
          is Operator,
          is LParen -> {
            stack.push(el)
            reduce(expression.drop(1))
          }
        }

    return reduce(expression)
  }

  override fun part1(input: String): Long {
    val expressions = input.lines().map { line -> line.mapNotNull(Token::fromChar) }
    return expressions.sumOf { evaluate1(it) }
  }

  fun evaluate2(expression: List<Token>): Long {
    val stack = Stack<Token>()

    tailrec fun reduce(expression: List<Token>): Long {
      return when (val el = expression.firstOrNull()) {
        null -> (stack.pop() as Number).n
        is RParen -> {
          val operand2 = stack.pop() as Number
          if (stack.peek() is LParen) {
            stack.pop() as LParen
            reduce(listOf(operand2) + expression.drop(1))
          } else {
            val operator = stack.pop() as Operator
            val operand1 = stack.pop() as Number
            stack.push(Number(operator.op(operand1.n, operand2.n)))
            reduce(expression)
          }
        }
        is Number -> {
          val maybeOperator = stack.lastOrNull()
          if (maybeOperator is Operator && maybeOperator.underlying == '+') {
            val operator = stack.pop() as Operator
            val operand1 = stack.pop() as Number
            stack.push(Number(operator.op(operand1.n, el.n)))
          } else {
            stack.push(el)
          }
          reduce(expression.drop(1))
        }
        is Operator,
        is LParen -> {
          stack.push(el)
          reduce(expression.drop(1))
        }
      }
    }

    stack.push(LParen())
    return reduce(expression + RParen())
  }

  override fun part2(input: String): Any {
    val expressions = input.lines().map { line -> line.mapNotNull(Token::fromChar) }
    return expressions.sumOf { evaluate2(it) }
  }

  @Test
  fun testExamplePart1() {
    assertEquals(71L, part1("1 + 2 * 3 + 4 * 5 + 6"))
    assertEquals(51L, part1("1 + (2 * 3) + (4 * (5 + 6))"))
    assertEquals(26L, part1("2 * 3 + (4 * 5)"))
    assertEquals(437L, part1("5 + (8 * 3 + 9 + 3 * 4 * 3)"))
    assertEquals(12240L, part1("5 * 9 * (7 * 3 * 3 + 9 * 3 + (8 + 6 * 4))"))
    assertEquals(13632L, part1("((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2"))
  }

  @Test
  fun testExamplePart2() {
    assertEquals(231L, part2("1 + 2 * 3 + 4 * 5 + 6"))
    assertEquals(51L, part2("1 + (2 * 3) + (4 * (5 + 6))"))
    assertEquals(46L, part2("2 * 3 + (4 * 5)"))
    assertEquals(1445L, part2("5 + (8 * 3 + 9 + 3 * 4 * 3)"))
    assertEquals(669060L, part2("5 * 9 * (7 * 3 * 3 + 9 * 3 + (8 + 6 * 4))"))
    assertEquals(23340L, part2("((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2"))
  }
}
