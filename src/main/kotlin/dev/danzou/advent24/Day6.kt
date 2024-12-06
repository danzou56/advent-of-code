package dev.danzou.advent24

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.plus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class Day6 : AdventTestRunner24("") {

  fun next(dir: Compass): Compass =
      when (dir) {
        Compass.NORTH -> Compass.EAST
        Compass.EAST -> Compass.SOUTH
        Compass.SOUTH -> Compass.WEST
        Compass.WEST -> Compass.NORTH
        else -> throw IllegalArgumentException("Unknown dir: $dir")
      }

  fun visit(mat: Matrix<Char>, start: Pos): Set<Pos> {
    val visited: MutableSet<Pos> = mutableSetOf()

    tailrec fun step(cur: Pos, dir: Compass) {
      visited += cur

      val next = cur + dir.dir
      return when (mat.getOrNull(next)) {
        null -> Unit
        '#' -> step(cur, next(dir))
        else -> step(next, dir)
      }
    }

    step(start, Compass.NORTH)
    return visited
  }

  fun isLoop(mat: Matrix<Char>, start: Pos): Boolean {
    val visited: MutableSet<Pair<Pos, Compass>> = mutableSetOf()

    tailrec fun step(cur: Pos, dir: Compass): Boolean {
      if (cur to dir in visited) return true
      visited += cur to dir

      val next = cur + dir.dir
      return when (mat.getOrNull(next)) {
        null -> false
        '#' -> step(cur, next(dir))
        else -> step(next, dir)
      }
    }

    return step(start, Compass.NORTH)
  }

  override fun part1(input: String): Any {
    val mat = input.asMatrix<Char>()
    val start = mat.indices2D.first { mat[it] == '^' }
    return visit(mat, start).size
  }

  override fun part2(input: String): Any {
    val mat = input.asMatrix<Char>()
    val start = mat.indices2D.first { mat[it] == '^' }

    val possible = visit(mat, start)

    return possible
        .filter { p ->
          val (i, j) = p
          when (mat[p]) {
            '.' -> isLoop(mat.update(j, mat[j].update(i, '#')), start)
            else -> false
          }
        }
        .size
  }

  @Test
  fun testExample() {
    """
      ....#.....
      .........#
      ..........
      ..#.......
      .......#..
      ..........
      .#..^.....
      ........#.
      #.........
      ......#...
    """
        .trimIndent()
        .let { input ->
          assertEquals(41, part1(input))
          assertEquals(6, part2(input))
        }

    """
      ....#.....
      ....+---+#
      ....|...|.
      ..#.|...|.
      ....|..#|.
      ....|...|.
      .#.#^---+.
      ........#.
      #.........
      ......#...
    """
        .trimIndent()
        .let { input ->
          val mat = input.asMatrix<Char>()
          val start = mat.indices2D.first { mat[it] == '^' }

          assertTrue(isLoop(mat, start))
        }

    """
      ....#.....
      ....+---+#
      ....|...|.
      ..#.|...|.
      ..+-+-+#|.
      ..|.|.|.|.
      .#+-^-+-+.
      ......#.#.
      #.........
      ......#...
    """
        .trimIndent()
        .let { input ->
          val mat = input.asMatrix<Char>()
          val start = mat.indices2D.first { mat[it] == '^' }

          assertTrue(isLoop(mat, start))
        }
  }
}
