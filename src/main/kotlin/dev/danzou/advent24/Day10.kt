package dev.danzou.advent24

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day10 : AdventTestRunner24("Hoof It") {
  override fun part1(input: String): Int {
    val map = input.asMatrix<Int>()
    val starts = map.indices2D.filter { map[it] == 0 }

    return starts
        .map { start ->
          dfs(start) { pos ->
            map.neighboringPos(pos).filter { map[it] == map[pos] + 1 }.toSet()
          }
        }
        .sumOf { it.count { map[it] == 9 } }
  }

  override fun part2(input: String): Int {
    val map = input.asMatrix<Int>()
    val starts = map.indices2D.filter { map[it] == 0 }

    return starts
        // Get all start, end pairs
        .flatMap { start ->
          dfs(start) { pos ->
                map.neighboringPos(pos).filter { map[it] == map[pos] + 1 }.toSet()
              }
              .filter { map[it] == 9 }
              .map { start to it }
        }
        // Find paths between each start, end pair
        .flatMap { (start, end) ->
          findPathsBetween(start, end) { pos ->
            map.neighboringPos(pos).filter { map[it] == map[pos] + 1 }.toSet()
          }
        }
        // How many of those were there?
        .size
  }

  @Test
  fun testExample() {
    """
      89010123
      78121874
      87430965
      96549874
      45678903
      32019012
      01329801
      10456732
    """
        .trimIndent()
        .let { input ->
          assertEquals(36, part1(input))
          // assertEquals(null, part2(input))
        }
  }
}
