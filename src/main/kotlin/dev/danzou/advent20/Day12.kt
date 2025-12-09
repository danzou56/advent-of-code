package dev.danzou.advent20

import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.times
import dev.danzou.advent.utils.manhattanDistanceTo
import dev.danzou.advent.utils.geometry.x
import dev.danzou.advent.utils.geometry.y
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day12 : AdventTestRunner20("") {

  override fun part1(input: String): Int {

    val compassDirs =
        listOf(
            Compass.NORTH,
            Compass.EAST,
            Compass.SOUTH,
            Compass.WEST,
        )

    tailrec fun step(pos: Pos, dir: Int, instr: List<String>): Pos {
      if (instr.isEmpty()) return pos
      val (action, amount) = instr.first().let { it.take(1) to it.drop(1).toInt() }
      val rest = instr.drop(1)
      return when (action) {
        "N" -> step(pos + Compass.NORTH.dir * amount, dir, rest)
        "S" -> step(pos + Compass.SOUTH.dir * amount, dir, rest)
        "E" -> step(pos + Compass.EAST.dir * amount, dir, rest)
        "W" -> step(pos + Compass.WEST.dir * amount, dir, rest)
        "L" -> step(pos, (dir - (amount / 90) + 4) % 4, rest)
        "R" -> step(pos, (dir + (amount / 90)) % 4, rest)
        "F" -> step(pos + compassDirs[dir].dir * amount, dir, rest)
        else -> throw IllegalArgumentException()
      }
    }

    return step(
            0 to 0,
            compassDirs.indexOf(Compass.EAST),
            input.lines(),
        ) manhattanDistanceTo (0 to 0)
  }

  tailrec fun cos(deg: Int): Int =
      when (deg) {
        0 -> 1
        90 -> 0
        180 -> -1
        270 -> 0
        else -> if (deg >= 360) cos(deg - 360) else cos(deg + 360)
      }

  tailrec fun sin(deg: Int): Int =
      when (deg) {
        0 -> 0
        90 -> 1
        180 -> 0
        270 -> -1
        else -> if (deg >= 360) sin(deg - 360) else sin(deg + 360)
      }

  fun rot(pos: Pos, angle: Int): Pos =
      pos.x * cos(angle) - pos.y * sin(angle) to pos.x * sin(angle) + pos.y * cos(angle)

  override fun part2(input: String): Int {
    tailrec fun step(ship: Pos, waypoint: Pos, instr: List<String>): Pos {
      if (instr.isEmpty()) return ship
      val (action, amount) = instr.first().let { it.take(1) to it.drop(1).toInt() }
      val rest = instr.drop(1)
      return when (action) {
        "N" -> step(ship, waypoint + Compass.NORTH.dir * amount, rest)
        "S" -> step(ship, waypoint + Compass.SOUTH.dir * amount, rest)
        "E" -> step(ship, waypoint + Compass.EAST.dir * amount, rest)
        "W" -> step(ship, waypoint + Compass.WEST.dir * amount, rest)
        "L" -> step(ship, rot(waypoint, -amount), rest)
        "R" -> step(ship, rot(waypoint, amount), rest)
        "F" -> step(ship + waypoint * amount, waypoint, rest)
        else -> throw IllegalArgumentException()
      }
    }

    return step(
        0 to 0,
        Compass.EAST.dir * 10 + Compass.NORTH.dir * 1,
        input.lines(),
    ) manhattanDistanceTo (0 to 0)
  }

  @Test
  fun testExample() {
    val input =
        """
          F10
          N3
          F7
          R90
          F11
        """
            .trimIndent()

    assertEquals(286, part2(input))
  }
}
