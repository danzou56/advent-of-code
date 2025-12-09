package dev.danzou.advent24

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.minus
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.unaryMinus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day8 : AdventTestRunner24("Resonant Collinearity") {

  fun getAntinodes(p1: Pos, p2: Pos): List<Pos> {
    val diff = (p2 - p1)
    val antinode1 = p1 - diff
    val antinode2 = p2 + diff
    return listOf(antinode1, antinode2)
  }

  override fun part1(input: String): Any {
    val map = input.asMatrix<Char>()
    val antennaMap =
        map.indices2D.filter { map[it].isLetterOrDigit() }.groupBy { map[it] }

    return antennaMap
        .flatMap { (_, posList) ->
          posList.pairs().flatMap { (p1, p2) ->
            getAntinodes(p1, p2).filter { map.containsPos(it) }
          }
        }
        .toSet()
        .size
  }

  fun getManyAntinodes(map: Matrix<Char>, p1: Pos, p2: Pos): List<Pos> {
    tailrec fun generate(cur: Pos, diff: Pos, antinodes: List<Pos>): List<Pos> {
      if (!map.containsPos(cur)) return antinodes
      val next = antinodes + cur
      return generate(cur + diff, diff, next)
    }

    val diff = (p2 - p1)
    return generate(p1, -diff, emptyList()) + generate(p2, diff, emptyList())
  }

  override fun part2(input: String): Any {
    val map = input.asMatrix<Char>()
    val antennaMap =
        map.indices2D.filter { map[it].isLetterOrDigit() }.groupBy { map[it] }

    return antennaMap
        .flatMap { (_, posList) ->
          posList.pairs().flatMap { (p1, p2) -> getManyAntinodes(map, p1, p2) }
        }
        .toSet()
        .size
  }

  @Test
  fun testExample() {
    """
      ............
      ........0...
      .....0......
      .......0....
      ....0.......
      ......A.....
      ............
      ............
      ........A...
      .........A..
      ............
      ............
    """
        .trimIndent()
        .let { input ->
          assertEquals(14, part1(input))
          assertEquals(34, part2(input))
        }
  }
}