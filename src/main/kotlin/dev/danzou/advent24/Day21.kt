package dev.danzou.advent24

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day21 : AdventTestRunner24("Keypad Conundrum") {
  companion object {
    val compasses: Map<Compass, Char> =
        mapOf(
            Compass.EAST to '>',
            Compass.WEST to '<',
            Compass.SOUTH to 'v',
            Compass.NORTH to '^',
            //              Compass.CENTER to 'A',
        )
    val dirs: Map<Pos, Char> = compasses.mapKeys { (k, _) -> k.dir }
    val arrows: Map<Char, Compass> = compasses.entries.associate { (f, s) -> s to f }
  }

  open class KeypadPresser(
      val keypad: Matrix<Char>,
  ) {
    val buttons: Map<Char, Pos> =
        this.keypad
            .mapIndexed2D { p, c -> c to p }
            .flatten()
            .filter { (c, p) -> c != ' ' }
            .toMap()
    val keypadIndices: Set<Pos> = buttons.values.toSet()

    protected fun dijkstras(
        start: Char,
        end: Char,
        costFunction: (Pair<Pos, Char>, Pair<Pos, Char>) -> Int
    ): List<Pair<Pos, Char>> {
      val endPos = buttons[end]!!
      return doDijkstras(
          buttons[start]!! to 'A',
          { (p, _) -> p == endPos },
          { (p, _) ->
            dirs
                .map { (dir, button) -> p + dir to button }
                .filter { (p, _) -> p in keypadIndices }
          },
          costFunction)
    }
  }

  class NumericRobot()

 /* open class DirectionalRobot :
      KeypadPresser(
          """
             ^A
            <v>
          """
              .trimIndent()
              .asMatrix<Char>(),
      )

  class RadiationBlastedRobot() : DirectionalRobot()

  class FrozenRobot() : DirectionalRobot() {
  }

  class NotARobot() : DirectionalRobot() {

    fun buttonPressesFor(cur: Pos, presses: String): String {
      assert(cur == buttons['A']!!)


    }
  }
*/
  class NumericKeypad {
    companion object {
      val keypad =
          """
            789
            456
            123
             0A
          """
              .trimIndent()
              .asMatrix<Char>()
      val buttons =
          keypad
              .mapIndexed2D { p, c -> c to p }
              .flatten()
              .filter { (c, p) -> c != ' ' }
              .toMap()
      val keypadIndices = buttons.values.toSet()

      fun process(directions: String): String {
        tailrec fun step(cur: Pos, remaining: String, pressed: StringBuilder): String {
          assert(cur != 0 to 3)
          if (remaining.isEmpty()) return pressed.toString()
          val (next, pressed) =
              when (remaining.first()) {
                '<' -> Pair(cur + Compass.WEST.dir, pressed)
                '>' -> Pair(cur + Compass.EAST.dir, pressed)
                'v' -> Pair(cur + Compass.SOUTH.dir, pressed)
                '^' -> Pair(cur + Compass.NORTH.dir, pressed)
                'A' -> Pair(cur, pressed.append(keypad[cur]))
                else -> throw IllegalArgumentException()
              }
          return step(next, remaining.substring(1), pressed)
        }

        return step(buttons['A']!!, directions, StringBuilder())
      }

      fun route(start: Char, end: Char): String {
        val endPos = buttons[end]!!

        val path =
            doDijkstras(
                buttons[start]!! to 'A',
                { (p, _) -> p == endPos },
                { (p, _) ->
                  dirs
                      .map { (dir, button) -> p + dir to button }
                      .filter { (p, _) -> p in keypadIndices }
                },
                { (_, prevButton), (_, nextButton) ->
                  DirectionalKeypad.route(prevButton, nextButton, 'A', true).length
                })
        return path.map { it.second }.drop(1).joinToString("") + 'A'
      }

      fun route(numbers: String): String {
        tailrec fun step(cur: Char, remaining: String, pressed: StringBuilder): String {
          if (remaining.isEmpty()) return pressed.toString()

          val route = route(cur, remaining.first())
          pressed.append(route)
          return step(remaining.first(), remaining.substring(1), pressed)
        }

        return step('A', numbers, StringBuilder("A")).substring(1)
      }
    }
  }

  class DirectionalKeypad {
    companion object {
      val keypad =
          """
             ^A
            <v>
          """
              .trimIndent()
              .asMatrix<Char>()
      val buttons =
          keypad
              .mapIndexed2D { p, c -> c to p }
              .flatten()
              .filter { (c, p) -> c != ' ' }
              .toMap()
      val keypadIndices = buttons.values.toSet()

      fun process(directions: String): String {
        tailrec fun step(cur: Pos, remaining: String, pressed: StringBuilder): String {
          assert(cur != 0 to 0)
          if (remaining.isEmpty()) return pressed.toString()
          val (next, pressed) =
              when (remaining.first()) {
                'A' -> Pair(cur, pressed.append(keypad[cur]))
                else -> Pair(cur + arrows.getValue(remaining.first()).dir, pressed)
              }
          return step(next, remaining.substring(1), pressed)
        }

        return step(buttons['A']!!, directions, StringBuilder())
      }

      fun route(start: Char, end: Char, lastDir: Char, recurse: Boolean = false): String {
        val endPos = buttons[end]!!
        val path =
            doDijkstras(
                buttons[start]!! to lastDir,
                { (p, _) -> p == endPos },
                { (p, _) ->
                  dirs
                      .map { (dir, button) -> p + dir to button }
                      .filter { (p, _) -> p in keypadIndices }
                },
                { (_, prevButton), (_, nextButton) ->
                  if (recurse) route(prevButton, nextButton, 'A', false).length
                  else buttons[prevButton]!!.manhattanDistanceTo(buttons[nextButton]!!)
                })
        return path.map { it.second }.drop(1).joinToString("") + 'A'
      }

      fun route(directions: String): String {
        tailrec fun step(cur: Char, remaining: String, pressed: StringBuilder): String {
          if (remaining.isEmpty()) return pressed.toString()

          val route = route(cur, remaining.first(), pressed.last())
          pressed.append(route)
          return step(remaining.first(), remaining.substring(1), pressed)
        }

        return step('A', directions, StringBuilder("A")).substring(1)
      }
    }
  }

  fun calculateButtonPresses(code: String): String {
    val firstRobotPresses = NumericKeypad.route(code)
    val secondRobotPresses = DirectionalKeypad.route(firstRobotPresses)
    val thirdRobotPresses = DirectionalKeypad.route(secondRobotPresses)

    assert(
        NumericKeypad.process(
            DirectionalKeypad.process(DirectionalKeypad.process(thirdRobotPresses))) ==
            code)

    return thirdRobotPresses
  }

  override fun part1(input: String): Long {
    val doorCodes = input.lines()

    // not 212830
    // smaller than 208326 probably
    return doorCodes.sumOf { code ->
      val numericCode = code.dropLast(1).toLong()
      val presses = calculateButtonPresses(code)

      numericCode * presses.length
    }
  }

  override fun part2(input: String): Any {
    TODO()
  }

  @Test
  fun testExample() {
    """
      029A
      980A
      179A
      456A
      379A
    """
        .trimIndent()
        .let { input ->
          assertEquals(126384, part1(input))
          // assertEquals(null, part2(input))
        }
  }

  @Test
  fun testRouteOnNumericKeypad() {
    val doorCode = "029A"

    assertEquals("<A", NumericKeypad.route('A', '0'))
    assertEquals("^A", NumericKeypad.route('0', '2'))
    assertEquals(">^^A", NumericKeypad.route('2', '9'))
    assertEquals("vvvA", NumericKeypad.route('9', 'A'))

    val actualRoute = NumericKeypad.route(doorCode)
    assertTrue("<A^A^^>AvvvA" == actualRoute || "<A^A>^^AvvvA" == actualRoute)
  }

  @Test
  fun testRouteOnDirectionalKeypad() {
    val keypadSequence = "<A^A>^^AvvvA"
    val actualRoute = DirectionalKeypad.route(keypadSequence)
    assertEquals("v<<A>>^A<A>AvA<^AA>A<vAAA>^A".length, actualRoute.length)
    assertEquals(keypadSequence, DirectionalKeypad.process(actualRoute))
  }

  @Test
  fun testPresses() {
    val input =
        """
          029A: <vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
          980A: <v<A>>^AAAvA^A<vA<AA>>^AvAA<^A>A<v<A>A>^AAAvA<^A>A<vA>^A<A>A
          179A: <v<A>>^A<vA<A>>^AAvAA<^A>A<v<A>>^AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
          456A: <v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A
          379A: <v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
        """
            .trimIndent()

    val codes = input.lines().map { it.split(": ").first() }
    val presses = input.lines().map { it.split(": ").last() }
    for ((code, press) in codes.zip(presses)) {
      println(code)
      println(press)
      val buttonPresses = calculateButtonPresses(code)
      println(buttonPresses)
      assertEquals(
          code,
          NumericKeypad.process(
              DirectionalKeypad.process(DirectionalKeypad.process(buttonPresses)))
          //              .also { println(it) },
          )
      assertEquals(press.length, calculateButtonPresses(code).length)
    }
  }
}
