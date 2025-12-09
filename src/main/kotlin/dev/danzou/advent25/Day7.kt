package dev.danzou.advent25

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.y
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day7 : AdventTestRunner25("Laboratories") {

  fun getSplitters(input: String): List<Pos> {
    val mat = input.asMatrix<Char>()
    return mat.indices2D.filter { mat[it] == '^' }
  }

  fun getStart(input: String): Pos {
    val mat = input.asMatrix<Char>()
    return mat.indices2D.single { mat[it] == 'S' }
  }

  fun getHeight(input: String): Int {
    val mat = input.asMatrix<Char>()
    return mat.size
  }

  fun getDiscoverableSplitters(input: String): Collection<Pos> {
    val start = getStart(input)
    val splitters = getSplitters(input).toSet()
    val maxHeight = getHeight(input)

    tailrec fun findNextSplitter(cur: Pos): Pos? {
      if (cur.y > maxHeight) return null
      if (cur in splitters) return cur
      return findNextSplitter(cur + (0 to 2))
    }

    return dfs(start) { cur ->
      val left = findNextSplitter(cur + (-1 to 0))
      val right = findNextSplitter(cur + (1 to 0))
      listOfNotNull(left, right)
    }
  }

  override fun part1(input: String): Int {
    return getDiscoverableSplitters(input).size
  }

  override fun part2(input: String): Any {
    val start = getStart(input)
    val splitters = getSplitters(input)
    val maxHeight = getHeight(input)

    val pathCounts = mutableMapOf<Pos, Long>()

    tailrec fun findNextSplitter(cur: Pos): Pos? {
      if (cur.y > maxHeight) return null
      if (cur in splitters) return cur
      return findNextSplitter(cur + (0 to 2))
    }

    fun countPaths(cur: Pos): Long {
      if (cur in pathCounts) return pathCounts[cur]!!

      val left = findNextSplitter(cur + (-1 to 0))
      val right = findNextSplitter(cur + (1 to 0))
      return listOf(left, right)
          .sumOf {
            when (it) {
              null -> 1
              else -> countPaths(it)
            }
          }
          .also { pathCounts[cur] = it }
    }

    return countPaths(start + (0 to 2))
  }

  @Test
  fun testExample() {
    """
      .......S.......
      ...............
      .......^.......
      ...............
      ......^.^......
      ...............
      .....^.^.^.....
      ...............
      ....^.^...^....
      ...............
      ...^.^...^.^...
      ...............
      ..^...^.....^..
      ...............
      .^.^.^.^.^...^.
      ...............
    """
        .trimIndent()
        .let { input ->
          assertEquals(21, part1(input))
          assertEquals(40L, part2(input))
        }
  }
}
