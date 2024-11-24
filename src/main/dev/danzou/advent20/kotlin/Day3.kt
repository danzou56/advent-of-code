package dev.danzou.advent20.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent20.AdventTestRunner20

internal class Day3 : AdventTestRunner20("") {

  override fun part1(input: String): Int {
    val mat = input.asMatrix<Char>()
    val diff = 3 to 1
    return traverse(mat, diff)
  }

  override fun part2(input: String): Any {
    return listOf(
            1 to 1,
            3 to 1,
            5 to 1,
            7 to 1,
            1 to 2,
        )
        .map { traverse(input.asMatrix<Char>(), it) }
        .map(Int::toLong)
        .reduce(Long::times)
  }

  private fun traverse(mat: Matrix<Char>, diff: Pos): Int {
    tailrec fun step(pos: Pos, trees: Int): Int {
      if (!mat.containsPos(pos)) return trees
      val next = (pos.x + diff.x).mod(mat[0].size) to (pos.second + diff.y)

      return if (mat[pos] == '#') step(next, trees + 1) else step(next, trees)
    }

    return step(0 to 0, 0)
  }
}
