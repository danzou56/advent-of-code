package dev.danzou.advent20

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.x
import dev.danzou.advent.utils.geometry.y
import dev.danzou.advent.utils.geometry3.plus
import dev.danzou.advent.utils.geometry3.x
import dev.danzou.advent.utils.geometry3.y
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private typealias Cube = Triple<Int, Int, Int>

internal class Day17 : AdventTestRunner20("") {

  fun getActive(input: String): List<Pos> =
      input.asMatrix<Char>().flatMapIndexed { y, row ->
        row.mapIndexedNotNull { x, c -> if (c == '#') x to y else null }
      }

  fun <T> evolve(active: Set<T>, neighbors: Collection<T>, isActive: Boolean): Boolean =
      when (isActive) {
        true -> neighbors.count { it in active } in 2..3
        false -> neighbors.count { it in active } == 3
      }

  override fun part1(input: String): Int {
    val offsets =
        (-1..1).flatMap { x ->
          (-1..1).flatMap { y -> (-1..1).map { z -> Triple(x, y, z) } }
        } - Triple(0, 0, 0)

    tailrec fun step(steps: Int, active: Set<Cube>): Set<Cube> {
      if (steps == 0) return active
      val next =
          active
              .flatMap { a -> offsets.map { it + a } }
              .toSet()
              .filter { cur -> evolve(active, offsets.map { cur + it }, cur in active) }
              .toSet()
      return step(steps - 1, next)
    }

    val initialActive = getActive(input).map { Cube(it.x, it.y, 0) }
    return step(6, initialActive.toSet()).size
  }

  data class Quad(val x: Int, val y: Int, val z: Int, val w: Int) {
    infix operator fun plus(other: Quad): Quad =
        Quad(
            x = this.x + other.x,
            y = this.y + other.y,
            z = this.z + other.z,
            w = this.w + other.w,
        )
  }

  override fun part2(input: String): Int {
    val offsets =
        (-1..1).flatMap { x ->
          (-1..1).flatMap { y ->
            (-1..1).flatMap { z -> (-1..1).map { w -> Quad(x, y, z, w) } }
          }
        } - Quad(0, 0, 0, 0)

    tailrec fun step(steps: Int, active: Set<Quad>): Set<Quad> {
      if (steps == 0) return active
      val next =
          active
              .flatMap { a -> offsets.map { it + a } }
              .toSet()
              .filter { cur -> evolve(active, offsets.map { cur + it }, cur in active) }
              .toSet()
      return step(steps - 1, next)
    }

    val initialActive = getActive(input).map { Quad(it.x, it.y, 0, 0) }
    return step(6, initialActive.toSet()).size
  }

  @Test
  fun testExample() {
    """
      .#.
      ..#
      ###
    """
        .trimIndent()
        .let { input ->
          assertEquals(112, part1(input))
          assertEquals(848, part2(input))
        }
  }
}
