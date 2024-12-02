package dev.danzou.advent20

internal class Day5 : AdventTestRunner20("") {

  fun parseRow(fb: String, range: IntRange = 0..127): Int {
    if (fb.isEmpty()) {
      assert(range.last - range.first == 0)
      return range.first
    }

    return when (fb.take(1)) {
      "F" ->
          parseRow(
              fb.drop(1),
              range.first..(range.first + (range.last - range.first) / 2),
          )

      "B" ->
          parseRow(
              fb.drop(1),
              (range.first + (range.last - range.first + 1) / 2)..range.last,
          )

      else -> throw IllegalArgumentException()
    }
  }

  fun parseSeat(lr: String, range: IntRange = 0..7): Int {
    if (lr.isEmpty()) {
      assert(range.last - range.first == 0)
      return range.first
    }

    return when (lr.take(1)) {
      "L" ->
          parseSeat(
              lr.drop(1),
              range.first..(range.first + (range.last - range.first) / 2),
          )

      "R" ->
          parseSeat(
              lr.drop(1),
              (range.first + (range.last - range.first + 1) / 2)..range.last,
          )

      else -> throw IllegalArgumentException()
    }
  }

  fun seatId(row: Int, seat: Int): Int = row * 8 + seat

  override fun part1(input: String): Any {
    return input.lines().maxOf {
      seatId(
          parseRow(it.take(7)),
          parseSeat(it.takeLast(3)),
      )
    }
  }

  override fun part2(input: String): Any {
    val seatIds =
        input.lines().map {
          seatId(
              parseRow(it.take(7)),
              parseSeat(it.takeLast(3)),
          )
        }
    return seatIds.sorted().windowed(2).find { (id1, id2) -> id2 - id1 == 2 }!!.first() + 1
  }
}
