package dev.danzou.advent24

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.minus
import dev.danzou.advent.utils.geometry.plus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day15 : AdventTestRunner24("Warehouse Woes") {
  val BOX = 'O'
  val WIDE_BOX_L = '['
  val WIDE_BOX_R = ']'
  val WALL = '#'
  val EMPTY = '.'
  val START = '@'

  private fun sumBoxCoordinates(warehouse: Matrix<Char>): Int {
    return warehouse
        .mapIndexed2D { x, y, c ->
          when (c) {
            BOX -> y * 100 + x
            WIDE_BOX_L -> y * 100 + x
            else -> 0
          }
        }
        .flatten()
        .sum()
  }

  override fun part1(input: String): Int {
    val parsed = input.split("\n\n").first().asMatrix<Char>()
    val instructions = input.split("\n\n").last().lines().joinToString("")

    val start = parsed.indices2D.single { parsed[it] == START }
    val mat = parsed.map { it.toMutableList() }.toMutableList()
    mat[start] = EMPTY

    fun moveBox(mat: MutableMatrix<Char>, box: Pos, dir: Pos): Boolean {
      val first = box

      tailrec fun step(box: Pos): Boolean {
        if (mat[box + dir] == EMPTY) {
          mat[box + dir] = BOX
          mat[first] = EMPTY
          return true
        } else if (mat[box + dir] == WALL) {
          return false
        } else {
          return step(box + dir)
        }
      }

      return step(first)
    }

    tailrec fun move(cur: Pos, instr: String) {
      if (instr.isEmpty()) return
      val dir =
          when (instr.first()) {
            '<' -> Compass.WEST.dir
            '>' -> Compass.EAST.dir
            '^' -> Compass.NORTH.dir
            'v' -> Compass.SOUTH.dir
            else -> throw IllegalArgumentException()
          }

      val next = cur + dir

      val can =
          when (mat[next]) {
            WALL -> false
            BOX -> moveBox(mat, next, dir)
            EMPTY -> true
            else -> throw IllegalArgumentException()
          }

      return move(if (can) next else cur, instr.drop(1))
    }

    move(start, instructions)

    return sumBoxCoordinates(mat)
  }

  override fun part2(input: String): Int {
    val parsed =
        input.split("\n\n").first().asMatrix<Char>().map { row ->
          row.flatMap { c ->
            when (c) {
              WALL -> listOf(WALL, WALL)
              EMPTY -> listOf(EMPTY, EMPTY)
              BOX -> listOf(WIDE_BOX_L, WIDE_BOX_R)
              START -> listOf(START, EMPTY)
              else -> throw IllegalArgumentException()
            }
          }
        }
    val instructions = input.split("\n\n").last().lines().joinToString("")

    val start = parsed.indices2D.single { parsed[it] == START }
    val mat = parsed.map { it.toMutableList() }.toMutableList()

    fun moveBoxLR(mat: MutableMatrix<Char>, box: Pos, dir: Pos): Boolean {

      tailrec fun canMove(box: Pos): Boolean {
        if (mat[box + dir] == EMPTY) return true
        if (mat[box + dir] == WALL) return false
        return canMove(box + dir)
      }

      fun move(box: Pos) {

        if (mat[box + dir] != EMPTY) move(box + dir + dir)

        mat[box + dir] = mat[box]
        mat[box] = mat[box - dir]
        mat[box - dir] = EMPTY
      }

      return canMove(box).also { canMove ->
        if (canMove) move(if (dir == Compass.WEST.dir) box else box + dir)
      }
    }

    fun moveBoxUD(mat: MutableMatrix<Char>, box: Pos, dir: Pos): Boolean {

      fun canMove(box: Pos): Boolean {
        val boxR = box + Compass.EAST.dir
        if (mat[box + dir] == WALL || mat[boxR + dir] == WALL) return false

        if (mat[box + dir] == WIDE_BOX_L) return canMove(box + dir)

        // At this point we know we're pushing an offset box or empty
        val canLeftMove =
            mat[box + dir] == EMPTY ||
                (mat[box + dir] == WIDE_BOX_R && canMove(box + dir + Compass.WEST.dir))
        val canRightMove =
            mat[boxR + dir] == EMPTY ||
                (mat[boxR + dir] == WIDE_BOX_L && canMove(boxR + dir))
        return canLeftMove && canRightMove
      }

      fun move(box: Pos) {
        val boxR = box + Compass.EAST.dir
        if (mat[box + dir] == WIDE_BOX_L) move(box + dir)
        else {
          if (mat[box + dir] == WIDE_BOX_R) move(box + dir + Compass.WEST.dir)
          if (mat[boxR + dir] == WIDE_BOX_L) move(boxR + dir)
        }

        mat[box + dir] = WIDE_BOX_L
        mat[boxR + dir] = WIDE_BOX_R
        mat[box] = EMPTY
        mat[boxR] = EMPTY
        return
      }

      return canMove(box).also { canMove -> if (canMove) move(box) }
    }

    tailrec fun move(cur: Pos, instr: String) {
      if (instr.isEmpty()) return

      val dir =
          when (instr.first()) {
            '<' -> Compass.WEST.dir
            '>' -> Compass.EAST.dir
            '^' -> Compass.NORTH.dir
            'v' -> Compass.SOUTH.dir
            else -> throw IllegalArgumentException()
          }
      val next = cur + dir

      val can =
          when (mat[next]) {
            WALL -> false
            WIDE_BOX_R ->
                if (dir.y == 0) {
                  assert(dir == Compass.WEST.dir)
                  moveBoxLR(mat, next + Compass.WEST.dir, dir)
                } else moveBoxUD(mat, next + Compass.WEST.dir, dir)
            WIDE_BOX_L ->
                if (dir.y == 0) {
                  assert(dir == Compass.EAST.dir)
                  moveBoxLR(mat, next, dir)
                } else moveBoxUD(mat, next, dir)
            EMPTY -> true
            else -> throw IllegalArgumentException()
          }

      return move(
          if (can) {
            mat[cur] = EMPTY
            mat[next] = START
            next
          } else cur,
          instr.drop(1),
      )
    }

    move(start, instructions)

    return sumBoxCoordinates(mat)
  }

  @Test
  fun testSumBoxCoordinates() {
    """
      ##########
      ##...[]...
      ##........
    """
        .trimIndent()
        .let { input -> assertEquals(105, sumBoxCoordinates(input.asMatrix<Char>())) }
  }

  @Test
  fun testExample() {
    """
      ##########
      #..O..O.O#
      #......O.#
      #.OO..O.O#
      #..O@..O.#
      #O#..O...#
      #O..O..O.#
      #.OO.O.OO#
      #....O...#
      ##########

      <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
      vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
      ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
      <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
      ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
      ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
      >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
      <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
      ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
      v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
    """
        .trimIndent()
        .let { input ->
          assertEquals(10092, part1(input))
          assertEquals(9021, part2(input))
        }

    """
      ########
      #..O.O.#
      ##@.O..#
      #...O..#
      #.#.O..#
      #...O..#
      #......#
      ########
      
      <^^>>>vv<v>>v<<
    """
        .trimIndent()
        .let { input -> assertEquals(2028, part1(input)) }
  }
}
