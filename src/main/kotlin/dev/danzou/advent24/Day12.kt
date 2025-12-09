package dev.danzou.advent24

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass.Companion.CARDINAL_DIRECTIONS
import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.x
import dev.danzou.advent.utils.geometry.y
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day12 : AdventTestRunner24("Garden Groups") {

  tailrec fun divideRegions(
      garden: Matrix<Char>,
      remaining: Set<Pos>,
      discovered: List<Set<Pos>> = emptyList(),
  ): List<Set<Pos>> {
    if (remaining.isEmpty()) return discovered
    val start = remaining.first()
    val target = garden[start]
    val curPlot =
        dfs(start) { pos ->
          garden.neighboringPos(pos).filter { adj -> garden[adj] == target }.toSet()
        }
    val nextRemaining = remaining - curPlot
    val nextDiscovered = discovered + listOf(curPlot)
    return divideRegions(garden, nextRemaining, nextDiscovered)
  }

  override fun part1(input: String): Any {
    val garden = input.asMatrix<Char>()
    val plots =
        garden.indices2D
            .groupBy { garden[it] }
            .flatMap { (plotChar, plotPos) ->
              divideRegions(garden, plotPos.toSet()).map { plotChar to it }
            }

    return plots.sumOf { (plotChar, posList) ->
      val area = posList.size
      val perimeter =
          posList.sumOf { pos ->
            CARDINAL_DIRECTIONS.count { dir ->
              val adj = pos + dir
              !garden.containsPos(adj) || garden[adj] != plotChar
            }
          }
      area * perimeter
    }
  }

  tailrec fun countSides(garden: Matrix<Char>, remaining: Set<Pos>, count: Int = 0): Int {
    if (remaining.isEmpty()) return count
    val start = remaining.first()
    val curSide =
        dfs(start) { garden.neighboringPos(it).filter(remaining::contains).toSet() }
    val nextRemaining = remaining - curSide
    return countSides(garden, nextRemaining, count + 1)
  }

  override fun part2(input: String): Any {
    val garden = input.asMatrix<Char>()
    val plots =
        garden.indices2D
            .groupBy { garden[it] }
            .flatMap { (plotChar, posList) ->
              divideRegions(garden, posList.toSet()).map { plotChar to it }
            }

    return plots.sumOf { (plotChar, posList) ->
      val area = posList.size
      val sides =
          posList
              // Find all perimeter positions - since we need to find "sides", we also
              // need to keep
              // the direction in order to later count and disambiguate disjoint sides
              .flatMap { pos ->
                CARDINAL_DIRECTIONS.filter { dir ->
                      val adj = pos + dir
                      !garden.containsPos(adj) || garden[adj] != plotChar
                    }
                    .map { dir -> pos to dir }
              }
              // Group by side (sort of) - pos/dir pairs on the same side share the same y
              // coordinate for north/south sides and share the same x coordinate for
              // east/west
              // sides
              .groupBy { (pos, dir) -> if (dir.x == 0) pos.y to dir else pos.x to dir }
              .values
              .sumOf { posDirList: List<Pair<Pos, Pos>> ->
                // At this point, the direction the side faces is no longer relevant and
                // can be
                // thrown away
                val posSet = posDirList.map { (pos, _) -> pos }.toSet()
                // Grouping by pos.y/x,dir will produce scenarios where disjointed sides
                // are in the
                // same group so use a search to disambiguate the sides
                countSides(garden, posSet)
              }
      area * sides
    }
  }

  @Test
  fun testExample() {
    """
      RRRRIICCFF
      RRRRIICCCF
      VVRRRCCFFF
      VVRCCCJFFF
      VVVVCJJCFE
      VVIVCCJJEE
      VVIIICJJEE
      MIIIIIJJEE
      MIIISIJEEE
      MMMISSJEEE
    """
        .trimIndent()
        .let { input ->
          assertEquals(1930, part1(input))
          assertEquals(1206, part2(input))
        }
  }
}
