package dev.danzou.advent25

import dev.danzou.advent.utils.Matrix
import dev.danzou.advent.utils.geometry.PosL
import dev.danzou.advent.utils.geometry.minus
import dev.danzou.advent.utils.geometry.toPair
import dev.danzou.advent.utils.geometry.x
import dev.danzou.advent.utils.geometry.y
import kotlin.collections.map
import kotlin.collections.windowed
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.sign

internal class Day9 : AdventTestRunner25("") {

  fun getTiles(input: String): List<PosL> =
      input.lines().map { line -> line.split(",").map { it.toLong() }.toPair() }

  fun getAreas(tiles: List<PosL>): Map<Pair<PosL, PosL>, Long> {
    val tilePairs =
        (0..<tiles.size - 1).flatMap { i ->
          val t1 = tiles[i]
          tiles.subList(i + 1, tiles.size).map { t2 -> t1 to t2 }
        }
    val tileAreas =
        tilePairs.associateWith { (t1, t2) ->
          (t2 - t1).let { (x, y) -> (abs(x) + 1) * (abs(y) + 1) }
        }
    return tileAreas
  }

  override fun part1(input: String): Long {
    val tiles = getTiles(input)
    val tileAreas = getAreas(tiles)
    return tileAreas.maxBy { it.value }.value
  }

  fun findCrossingLines(
      line: Pair<PosL, PosL>,
      //      points: List<PosL>,
      sortedByX: List<PosL>,
      sortedByY: List<PosL>,
  ): List<Pair<PosL, PosL>> {
    //    val lineToIndex = points.zip(points.indices).toMap()

    // vertical line - find horizontal lines
    return if (line.first.x == line.second.x) {
      val minY = min(line.first.y, line.second.y)
      val maxY = max(line.first.y, line.second.y)
      val lowY =
          sortedByY
              .binarySearch { it.y.compareTo(minY) }
              .let { if (it < 0) ((-it) - 1) else it }
      val highY =
          sortedByY
              .binarySearch { it.y.compareTo(maxY) }
              .let { if (it < 0) ((-it) - 1) - 1 else it }
      sortedByY
          .subList(lowY, highY + 1)
          //          .filter { it.x != line.first.x }
          .filter { it.y != line.first.y && it.y != line.second.y }
          .groupBy { it.y }
          .flatMap { (y, points) ->
            points.sortedBy { it.x }.windowed(2, 2).map { it.toPair() }
          }
          .filter { (p1, p2) -> line.first.x in p1.x..p2.x }
    } else {
      val minX = min(line.first.x, line.second.x)
      val maxX = max(line.first.x, line.second.x)
      val lowX =
          sortedByX
              .binarySearch { it.x.compareTo(minX) }
              .let { if (it < 0) ((-it) - 1) else it }
      val highX =
          sortedByX
              .binarySearch { it.x.compareTo(maxX) }
              .let { if (it < 0) ((-it) - 1) - 1 else it }
      sortedByX
          .subList(lowX, highX + 1)
          //          .filter { it.y != line.first.y }
          .filter { it.x != line.first.x && it.x != line.second.x }
          .groupBy { it.x }
          .flatMap { (x, points) ->
            points.sortedBy { it.y }.windowed(2, 2).map { it.toPair() }
          }
          .filter { (p1, p2) -> line.first.y in p1.y..p2.y }
    }
  }

  fun calculateArea(points: List<PosL>): Long {
    fun det2(mat: Matrix<Long>): Long {
      require(mat.size == 2)
      require(mat.all { it.size == 2 })
      return mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0]
    }

    // Use the shoelace formula to calculate the interior area of polygon defined by
    // coordinates
    return (points.toList() + points.first())
        .windowed(2)
        .map { it.map { it.toList() } }
        .map { det2(it) }
        .sum() / 2
  }

  // 4525501422 too high
  // 4616852656 also going to be too high
  // 223926930 too low? not sure
  // 124688070 too low
  // not tried yet - 4520170681?
  override fun part2(input: String): Long {
    // tiles ordered clockwise
    val tiles = getTiles(input)
    val tilesToIndex = tiles.zip(tiles.indices).toMap()

    val sortedTilesByX = tiles.sortedBy { it.x }
    val sortedTilesByY = tiles.sortedBy { it.y }

    return getAreas(tiles)
        .entries
        .sortedByDescending { it.value }
        .first { (k, v) ->
          val p1 = k.first
          val p2 = k.second

          val minX = min(p1.x, p2.x)
          val maxX = max(p1.x, p2.x)

          val minY = min(p1.y, p2.y)
          val maxY = max(p1.y, p2.y)

          val horizontals =
              listOf(
                  PosL(minX, minY) to PosL(maxX, minY),
                  PosL(minX, maxY) to PosL(maxX, maxY),
              )
          val verticals =
              listOf(
                  PosL(minX, minY) to PosL(minX, maxY),
                  PosL(maxX, minY) to PosL(maxX, maxY),
              )
          val incursions =
              horizontals.flatMap { h ->
                val crossings = findCrossingLines(h, sortedTilesByX, sortedTilesByY)
                crossings.filter { (p1, p2) ->
                  (min(p1.y, p2.y) == minY && max(p1.y, p2.y) == maxY) ||
                  (p1.y in minY + 1..<maxY - 1 || p2.y in minY + 1..maxY - 1)
                }
              } +
                  verticals.flatMap { v ->
                    val crossings = findCrossingLines(v, sortedTilesByX, sortedTilesByY)
                    crossings.filter { (p1, p2) ->
                      (min(p1.x, p2.x) == minX && max(p1.x, p2.x) == maxX) ||
                      (p1.x in minX + 1..maxX - 1 || p2.x in minX + 1..maxX - 1)
                    }
                  }


          if (incursions.isNotEmpty()) {
            return@first false
          }

          val i1 = min(tilesToIndex[p1]!!, tilesToIndex[p2]!!)
          val i2 = max(tilesToIndex[p1]!!, tilesToIndex[p2]!!)
          val area = calculateArea(tiles.subList(i1, i2 + 1))
          val area2 = calculateArea(tiles.subList(i2, tiles.size) + tiles.subList(0, i1 + 1))
          return@first sign(area.toDouble()) == sign(area2.toDouble())
          //
          //          if (incursions.isNotEmpty()) {
          //            assert(true)
          //          } else {
          //            assert(true)
          //          }
          //          incursions.isEmpty()
        }
        .also {
          println(it.key)
          require((it.key.second - it.key.first).let { (x, y) -> (abs(x) + 1) * (abs(y) + 1) } == it.value)
        }
        .value
  }

  @Test
  fun testfindCrossingLines() {
    listOf(PosL(0, 0), PosL(5, 0), PosL(1, 3), PosL(1, -3)).let { points ->
      val crossingLines =
          findCrossingLines(
              points.take(2).toPair(),
              points.sortedBy { it.x },
              points.sortedBy { it.y },
          )
      assertTrue(crossingLines.isNotEmpty())
      assertEquals(1, crossingLines.size)
    }

    listOf(PosL(0, 5), PosL(0, -2), PosL(3, 0), PosL(-1, 0)).let { points ->
      val crossingLines =
          findCrossingLines(
              points.take(2).toPair(),
              points.sortedBy { it.x },
              points.sortedBy { it.y },
          )
      assertTrue(crossingLines.isNotEmpty())
      assertEquals(1, crossingLines.size)
    }

    listOf(PosL(0, 0), PosL(5, 0), PosL(3, 0), PosL(3, -1)).let { points ->
      val crossingLines =
          findCrossingLines(
              points.take(2).toPair(),
              points.sortedBy { it.x },
              points.sortedBy { it.y },
          )
      assertTrue(crossingLines.isNotEmpty())
      assertEquals(1, crossingLines.size)
    }

    listOf(PosL(0, 5), PosL(0, -2), PosL(-3, 0), PosL(0, 0)).let { points ->
      val crossingLines =
          findCrossingLines(
              points.take(2).toPair(),
              points.sortedBy { it.x },
              points.sortedBy { it.y },
          )
      assertTrue(crossingLines.isNotEmpty())
      assertEquals(1, crossingLines.size)
    }
  }

  @Test
  fun testExample() {
    """
    7,1
    11,1
    11,7
    9,7
    9,5
    2,5
    2,3
    7,3
    """
        .trimIndent()
        .let { input ->
          assertEquals(50L, part1(input))
          assertEquals(24L, part2(input))
        }
  }
}
