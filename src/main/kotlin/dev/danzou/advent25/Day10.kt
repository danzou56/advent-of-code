package dev.danzou.advent25

import dev.danzou.advent.utils.E
import dev.danzou.advent.utils.doDijkstras
import dev.danzou.advent.utils.intersect
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private typealias Button = Set<Int>

internal class Day10 : AdventTestRunner25("Factory") {

  data class Machine(
      val lights: List<Boolean>,
      val buttons: Set<Button>,
      val joltages: List<Int>,
  ) {

    fun buttonMatrix(): RealMatrix {
      val rows = joltages.size
      val A =
          MatrixUtils.createRealMatrix(
              buttons
                  .map { button ->
                    (0..<rows).map { if (it in button) 1.0 else 0.0 }.toDoubleArray()
                  }
                  .toTypedArray()
          )

      return A.transpose()
    }

    fun joltagesVector(): RealVector {
      val b = MatrixUtils.createRealVector(joltages.map { it.toDouble() }.toDoubleArray())
      return b
    }

    fun buttonJoltageSystem(): RealMatrix {
      val A = buttonMatrix()
      val b = joltagesVector()

      return MatrixUtils.createRealMatrix(A.rowDimension, A.columnDimension + 1).apply {
        this.setSubMatrix(A.data, 0, 0)
        this.setColumn(A.columnDimension, b.toArray())
      }
    }

    companion object {
      private val lightsRegex = Regex("""(?<=\[)[.#]+(?=\])""")
      private val buttonsRegex = Regex("""(?<=\()\d+(,\d+)*(?=\))""")
      private val joltagesRegex = Regex("""(?<=\{)\d+(,\d+)*(?=\})""")

      fun fromString(line: String): Machine {
        val lights = lightsRegex.find(line)!!.value.map { it == '#' }
        val buttons =
            buttonsRegex
                .findAll(line)
                .map { it.value }
                .map { it.split(",").map { it.toInt() }.toSet() }
                .toSet()
        val joltages = joltagesRegex.find(line)!!.value.split(",").map { it.toInt() }
        return Machine(
            lights = lights,
            buttons = buttons,
            joltages = joltages,
        )
      }
    }
  }

  override fun part1(input: String): Int {
    val machines = input.lines().map(Machine::fromString)
    return machines.sumOf { machine ->
      doDijkstras(
              machine.lights.map { false },
              { it == machine.lights },
              { curLights ->
                machine.buttons.map { button ->
                  curLights.mapIndexed { i, b -> if (i in button) !b else b }
                }
              },
          )
          .size - 1
    }
  }

  fun createWolframCode(machine: Machine): String {
    val size = machine.joltages.size
    val buttons = machine.buttons.joinToString(",") { "{" + it.joinToString(",") + "}" }
    val variables = (1..machine.buttons.size).joinToString(",") { "x$it" }
    val joltages = machine.joltages.joinToString(",")
    val wolframCode =
        """
          Solve[
            Transpose[
              Total[UnitVector[$size, # + 1] &/@ #] &/@ {$buttons}
            ].Transpose[
              {{$variables}}
            ] == Transpose[
              {{$joltages}}
            ],
            {$variables},
            NonNegativeIntegers
          ]
        """
            .trimIndent()
    return wolframCode
  }

  fun applyTestToSystem(reduced: RealMatrix, test: Map<Int, Int>): List<Int>? {
    return (0..<reduced.rowDimension).map { i ->
      (0..<reduced.columnDimension - 1)
          .fold(reduced.getEntry(i, reduced.columnDimension - 1)) { s, j ->
            s - reduced.getEntry(i, j) * (test[j] ?: 0)
          }
          .let { s ->
            val rounded = round(s)
            if (abs(s - rounded) > E) return null
            rounded.toInt()
          }
    }
  }

  fun guessBounds(row: DoubleArray): Map<Int, IntRange> {
    // sketchy double comparisons abound!
    val pivot = row.indexOfFirst { it == 1.0 }
    if (pivot < 0) return emptyMap()
    if (row.slice(pivot..<row.size).any { it < 0.0 }) return emptyMap()
    if (row.slice(pivot + 1..<row.size - 1).all { it == 0.0 }) return emptyMap()
    val max = row.last()
    return (pivot + 1..<row.size)
        .mapNotNull { i ->
          val coeff = row[i]
          if (abs(coeff) < E) null else i to 0..floor(max / coeff).toInt()
        }
        .toMap()
  }

  fun getTestSetsFromRanges(ranges: Map<Int, IntRange>): List<Map<Int, Int>> {
    val xs = ranges.keys.toList()

    tailrec fun step(testSet: List<Map<Int, Int>>, keyIndex: Int): List<Map<Int, Int>> {
      if (keyIndex >= xs.size) return testSet
      val thisMap = ranges[xs[keyIndex]]!!.map { i -> mapOf(xs[keyIndex] to i) }
      return if (testSet.isEmpty()) {
        step(thisMap, keyIndex + 1)
      } else {
        step(
            thisMap.flatMap { newTestCase -> testSet.map { it + newTestCase } },
            keyIndex + 1,
        )
      }
    }

    return step(emptyList(), 0)
  }

  fun getTestSetFromMatrix(reduced: RealMatrix): List<Map<Int, Int>> {
    val testRanges = mutableMapOf<Int, IntRange>()
    for (i in 0..<reduced.rowDimension) {
      val bounds = guessBounds(reduced.getRow(i))
      for ((x, bound) in bounds) {
        testRanges[x] = testRanges.getOrDefault(x, bound).intersect(bound)
      }
    }

    return getTestSetsFromRanges(testRanges)
  }

  fun rowReduce(A: RealMatrix): RealMatrix {
    var h = 0 // pivot row
    var k = 0 // pivot column

    val m = A.rowDimension
    val n = A.columnDimension
    while (h < m && k < n) {
      // Find k-th pivot
      val iMax = (h..<m).maxBy { i -> abs(A.getEntry(i, k)) }
      if (abs(A.getEntry(iMax, k)) < E) {
        // No pivot in this column
        k += 1
      } else {
        // Swap rows so k-th pivot is in row h
        val hRow = A.getRow(h)
        val iMaxRow = A.getRow(iMax)
        A.setRow(iMax, hRow)
        A.setRow(h, iMaxRow)

        // Scale the current row so the pivot is 1
        val pivotScale = A.getEntry(h, k)
        for (j in k..<n) {
          A.setEntry(h, j, A.getEntry(h, j) / pivotScale)
        }

        // For all rows not the pivot, make sure the pivot column is zero-ed out
        for (i in 0..<m) {
          if (i == h) continue
          val f = A.getEntry(i, k) / A.getEntry(h, k)
          A.setEntry(i, k, 0.0)
          for (j in k + 1..<n) {
            A.setEntry(
                i,
                j,
                (A.getEntry(i, j) - A.getEntry(h, j) * f),
            )
          }
        }
        h += 1
        k += 1
      }
    }

    for (i in 0..<m) {
      for (j in 0..<n) {
        val a = A.getEntry(i, j)
        if (abs(a - round(a)) < E) {
          A.setEntry(i, j, round(a))
        }
      }
    }

    return A
  }

  fun solveSystemsWithWolfram(machines: List<Machine>): List<String> {
    val codes = machines.map { createWolframCode(it) }
    val fullCode = "{${codes.joinToString(",")}}"
    val process =
        Runtime.getRuntime()
            .exec(
                arrayOf(
                    "wolframscript",
                    "-code",
                    "Row[Riffle[$fullCode,\"\\n\"]]",
                )
            )
    val output = process.inputStream.bufferedReader().lineSequence().toList()
    val exit = process.waitFor()
    return output
  }

  fun resolveMinimumSolution(wolframOutputString: String): Int {
    return wolframOutputString
        .split("}, {")
        .map {
          it.trim('{', '}')
              .replace(" ", "")
              .split(",")
              .map { it.split("->").let { (f, s) -> f to s.toInt() } }
              .toMap()
        }
        .minOf { it.values.sum() }
  }

  fun resolveSolutions(wolframOutputString: String): List<List<Int>> {
    return wolframOutputString.split("}, {").map {
      it.trim('{', '}').replace(" ", "").split(",").map {
        it.split("->").let { (f, s) -> s.toInt() }
      }
      //          .toMap()
    }
  }

  override fun part2(input: String): Int {
    val machines = input.lines().map(Machine::fromString)

    machines
        .mapIndexed { i, m ->
          val reduced = rowReduce(m.buttonJoltageSystem())
          val tests = getTestSetFromMatrix(reduced)
          val testResults =
              tests.mapNotNull { test ->
                applyTestToSystem(reduced, test)?.takeIf { it.all { it >= 0 } }?.sum()
              }
          if (testResults.isEmpty()) {
            val emptyTest = applyTestToSystem(reduced, emptyMap())
            if (emptyTest == null || emptyTest.any { it < 0}) {
              println("null")
              0
            } else {
              emptyTest!!.sum()
            }
          } else {
            testResults.min()
            //        testResults.minOf { it }
          }
          //      tests.mapNotNull { test -> applyTestToSystem(reduced, test) }.minOf {
          // it.sum() }
          //      println(reduced)
          //      println("reduced")
          //      0.0
        }
        .sum()
        .also { println(it) }
    return solveSystemsWithWolfram(machines).sumOf { resolveMinimumSolution(it) }
  }

  @Test
  fun testExample() {
    """
    [.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
    [...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
    [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
    """
        .trimIndent()
        .let { input ->
          val machines = input.lines().map { Machine.fromString(it) }
          val lastMachine = machines.last()
          assertEquals(
              listOf(false, true, true, true, false, true),
              lastMachine.lights,
          )

          assertEquals(7, part1(input))
          assertEquals(33, part2(input))
        }
  }

  @Test
  fun testCartesian() {
    assertEquals(
        setOf(
            mapOf(1 to 1, 2 to 1),
            mapOf(1 to 1, 2 to 2),
            mapOf(1 to 2, 2 to 1),
            mapOf(1 to 2, 2 to 2),
        ),
        getTestSetsFromRanges(mapOf(1 to 1..2, 2 to 1..2)).toSet(),
    )
  }

  @Test
  fun testRowReduction() {
    val machine = Machine.fromString("[.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}")
    val systemMatrix = machine.buttonJoltageSystem()
    assertEquals(
        MatrixUtils.createRealMatrix(
            arrayOf(
                doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 3.0),
                doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 5.0),
                doubleArrayOf(0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 4.0),
                doubleArrayOf(1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 7.0),
            )
        ),
        machine.buttonJoltageSystem(),
    )

    assertEquals(
        MatrixUtils.createRealMatrix(
            arrayOf(
                doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, -1.0),
                doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 1.0),
                doubleArrayOf(0.0, 0.0, 1.0, 1.0, 0.0, -1.0),
                doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0),
            )
        ),
        rowReduce(machine.buttonMatrix()),
    )
  }
}
