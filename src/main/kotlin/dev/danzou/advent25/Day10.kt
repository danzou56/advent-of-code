package dev.danzou.advent25

import dev.danzou.advent.utils.E
import dev.danzou.advent.utils.doDijkstras
import dev.danzou.advent.utils.intersect
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor
import org.apache.commons.math3.linear.MatrixUtils.createRealIdentityMatrix
import org.apache.commons.math3.linear.MatrixUtils.createRealMatrix
import org.apache.commons.math3.linear.MatrixUtils.createRealVector
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

    fun buttonMatrix(): RealMatrix =
        createRealMatrix(joltages.size, buttons.size).apply {
          buttons.forEachIndexed { j, button ->
            button.forEach { i -> setEntry(i, j, 1.0) }
          }
        }

    fun joltagesVector(): RealVector =
        createRealVector(joltages.map { it.toDouble() }.toDoubleArray())

    fun buttonJoltageSystem(): RealMatrix {
      val A = buttonMatrix()
      val b = joltagesVector()

      return createRealMatrix(A.rowDimension, A.columnDimension + 1).apply {
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
    var h = 0
    var k = 0

    val m = reduced.rowDimension
    val n = reduced.columnDimension - 1
    val applied = (0..<reduced.columnDimension - 1).map { i -> test[i] }.toMutableList()
    while (h < m && k < n) {
      if (k in test) {
        k += 1
        continue
      }

      applied[k] =
          (0..<n)
              .fold(reduced.getEntry(h, n)) { s, j ->
                s - reduced.getEntry(h, j) * (test[j] ?: 0)
              }
              .let { s ->
                val rounded = round(s)
                if (abs(s - rounded) > E) return null
                rounded.toInt()
              }
      h += 1
      k += 1
    }

    if (applied.any { it == null }) require(applied.all { it != null })
    return applied as List<Int>?
  }

  tailrec fun applyTestToSystemPartial(
      reduced: RealMatrix,
      test: DoubleArray,
  ): RealMatrix {
    require(reduced.columnDimension - 1 == test.size)
    val n = reduced.rowDimension
    val m = reduced.columnDimension - 1
    val applicantIndex = test.indexOfFirst { it.isFinite() }
    require(applicantIndex in 0..<m)
    val applicant = test[applicantIndex]
    val partiallyApplied =
        createRealMatrix(n, m).apply {
          setSubMatrix(
              reduced.getSubMatrix(0, n - 1, 0, applicantIndex - 1).data,
              0,
              0,
          )
          if (applicantIndex != m - 1) {
            setSubMatrix(
                reduced.getSubMatrix(0, n - 1, applicantIndex + 1, m - 1).data,
                0,
                applicantIndex,
            )
          }
          setColumnVector(
              m - 1,
              reduced
                  .getColumnVector(m)
                  .subtract(
                      reduced.getColumnVector(applicantIndex).mapMultiplyToSelf(applicant)
                  ),
          )
        }
    if (test.indexOfLast { it.isFinite() } != applicantIndex) {
      return applyTestToSystemPartial(
          partiallyApplied,
          test.sliceArray(test.indices.filter { it != applicantIndex }),
      )
    } else {
      return partiallyApplied
    }
  }

  fun freeVariables(reducedSystem: RealMatrix): Set<Int> {
    val m = reducedSystem.rowDimension
    val n = reducedSystem.columnDimension - 1
    return (0..<m)
        .fold(0 to (0..<n).toSet()) { (k, frees), h ->
          (k..<n)
              .firstOrNull { reducedSystem.getEntry(h, it) == 1.0 }
              ?.let { k -> (k + 1) to (frees - k) } ?: (k to frees)
        }
        .second
  }

  fun guessBounds(row: DoubleArray): Map<Int, IntRange> {
    // sketchy double comparisons abound!
    val pivot = row.indexOfFirst { it == 1.0 }
    if (pivot < 0) return emptyMap()
    if (row.slice(pivot..<row.size).any { it < 0.0 }) return emptyMap()
    if (row.slice(pivot + 1..<row.size - 1).all { it == 0.0 }) return emptyMap()
    val max = row.last()
    return (pivot + 1..<row.size - 1)
        .mapNotNull { i ->
          val coeff = row[i]
          if (abs(coeff) < E) null else i to 0..floor(max / coeff).toInt()
        }
        .toMap()
  }

  fun guessBoundsByReduction(system: RealMatrix, target: Int): Map<Int, IntRange> {
    val n = system.rowDimension
    val m = system.columnDimension - 1
    val rowPairs = (0..<n - 1).flatMap { i -> (i + 1..<n).map { j -> i to j } }
    val reduciblePairs =
        rowPairs.filter { (i1, i2) ->
          system.getEntry(i1, target) == -system.getEntry(i2, target)
        }
    val reduced =
        reduciblePairs
            .map { (i1, i2) -> system.getRowVector(i1).add(system.getRowVector(i2)) }
            .filter { r ->
              r.getEntry(target) == 0.0 &&
                  !(0..<m).all { r.getEntry(it) == 0.0 } &&
                  (0..<m).any { r.getEntry(it) > 0 }
            }
    val boundsList =
        reduced.map { r ->
          r.setEntry(0, 1.0)
          guessBounds(r.toArray())
        }
    return mergeBounds(boundsList)
  }

  fun mergeBounds(boundsList: List<Map<Int, IntRange>>): Map<Int, IntRange> {
    val boundsMap = mutableMapOf<Int, IntRange>()

    for (bounds in boundsList) {
      for ((x, bound) in bounds) {
        boundsMap[x] = boundsMap.getOrDefault(x, bound).intersect(bound)
      }
    }

    return boundsMap
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

  fun getTestRangesFromMatrix(reduced: RealMatrix): Map<Int, IntRange> {
    if (
        (0..<reduced.rowDimension).all { i ->
          val row = reduced.getRow(i).slice(0..<reduced.columnDimension - 1)
          row.all { it == 0.0 } || row.count { it != 0.0 } == 1
        }
    ) {
      val bIndex = reduced.columnDimension - 1
      return (0..<reduced.rowDimension).associateWith { i ->
        val b =
            round(reduced.getEntry(i, bIndex).also { require(abs(it - round(it)) < E) })
                .toInt()
        b..b
      }
    }

    val testRanges =
        mergeBounds((0..<reduced.rowDimension).map { guessBounds(reduced.getRow(it)) })
            .toMutableMap()

    if (testRanges.isEmpty()) {
      val removePivots = createRealMatrix(reduced.data)
      for (h in (0..<reduced.rowDimension)) {
        for (k in (h..<reduced.columnDimension - 1)) {
          if (removePivots.getEntry(h, k) == 1.0) {
            removePivots.setEntry(h, k, 0.0)
            break
          }
        }
      }

      testRanges.putAll(
          mergeBounds(
              freeVariables(reduced).fold(emptyList()) { boundsList, freeVariable ->
                boundsList + guessBoundsByReduction(removePivots, freeVariable)
              }
          )
      )
    }

    if (testRanges.keys.toSet() != freeVariables(reduced)) {
      val oldToNewIndices =
          (0..<reduced.columnDimension - 1)
              .fold(0 to emptyMap<Int, Int?>()) { (k, indexMap), i ->
                if (i !in testRanges) (k + 1) to (indexMap + (i to k))
                else (k) to (indexMap + (i to null))
              }
              .second
      val newToOldIndices =
          oldToNewIndices.mapNotNull { (k, v) -> (v to k).takeIf { v != null } }.toMap()

      val draftTestSets = getTestSetsFromRanges(testRanges)
      val testSetsFromPartials =
          draftTestSets
              .map { testMap ->
                val testArray =
                    DoubleArray(reduced.columnDimension - 1) { i ->
                      if (i in testMap) testMap[i]!!.toDouble() else Double.NaN
                    }
                val partiallyApplied = applyTestToSystemPartial(reduced, testArray)
                getTestRangesFromMatrix(partiallyApplied)
              }
              .map { newTestRanges ->
                newTestRanges.mapKeys { (testI, _) -> newToOldIndices[testI]!! }
              }
      for (testSet in testSetsFromPartials) {
        for ((x, bound) in testSet) {
          testRanges[x] =
              min(testRanges[x]?.first ?: Int.MAX_VALUE, bound.first)..max(
                      testRanges[x]?.last ?: 0,
                      bound.last,
                  )
        }
      }

      // if we can't figure out a way to guess the ranges for all of the vars,
      // just guess zero 🤷‍♀️
      if (testRanges.keys.toSet() != freeVariables(reduced)) {
        testRanges.putAll(
            (freeVariables(reduced) - testRanges.keys).associateWith { 0..0 }
        )
      }
    }

    return testRanges
  }

  fun getTestSetFromMatrix(reduced: RealMatrix): List<Map<Int, Int>> =
      getTestSetsFromRanges(getTestRangesFromMatrix(reduced))

  fun reduceToEchelon(A: RealMatrix): RealMatrix {
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
        continue
      }
      // Swap rows so k-th pivot is in row h
      val hRow = A.getRow(h)
      val iMaxRow = A.getRow(iMax)
      A.setRow(iMax, hRow)
      A.setRow(h, iMaxRow)

      // Scale the current row so the pivot is 1
      val pivotScale = A.getEntry(h, k)
      if (pivotScale != 1.0)
        A.setRowVector(
            h,
            A.getRowVector(h).mapDivideToSelf(pivotScale),
        )

      // For all rows not the pivot, make sure the pivot column is zero-ed out
      for (i in 0..<m) {
        if (i == h) continue
        val f = A.getEntry(i, k) / A.getEntry(h, k)
        A.setRowVector(
            i,
            A.getRowVector(i).subtract(A.getRowVector(h).mapMultiplyToSelf(f)),
        )
        A.setEntry(i, k, 0.0)
      }
      h += 1
      k += 1
    }

    // Round entries that look like integers to the nearest int
    A.walkInOptimizedOrder(
        object : DefaultRealMatrixChangingVisitor() {
          override fun visit(row: Int, column: Int, value: Double): Double =
              when {
                value == -0.0 -> 0.0
                abs(value - round(value)) < E -> round(value)
                else -> value
              }
        }
    )

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

  fun resolveMinimumSolution(wolframOutputString: String): List<Int> {
    return wolframOutputString
        .split("}, {")
        .map {
          it.trim('{', '}')
              .replace(" ", "")
              .split(",")
              .map { it.split("->").let { (f, s) -> f to s.toInt() } }
              .toMap()
        }
        .minBy { it.values.sum() }
        .entries
        .sortedBy { it.key }
        .map { it.value }
        .toList()
  }

  fun resolveSolutions(wolframOutputString: String): List<List<Int>> {
    return wolframOutputString.split("}, {").map {
      it.trim('{', '}').replace(" ", "").split(",").map {
        it.split("->").let { (f, s) -> s.toInt() }
      }
    }
  }

  override fun part2(input: String): Int {
    val machines = input.lines().map(Machine::fromString)

    //    val realSols = solveSystemsWithWolfram(machines).map {
    // resolveMinimumSolution(it) }
    val linAlg =
        machines.map { m ->
          val reduced = reduceToEchelon(m.buttonJoltageSystem())
          val tests = getTestSetFromMatrix(reduced)
          tests.minOf { test ->
            applyTestToSystem(reduced, test)?.takeIf {
              it.all { it >= 0 }
            }?.sum() ?: Int.MAX_VALUE
          }
        }
    return linAlg.sum()
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
  fun testGetTestSetsFromRanges() {
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
  fun testGetTestRangesFromMatrix() {
    assertEquals(
        mapOf(3 to 0..3),
        getTestRangesFromMatrix(
            createRealMatrix(
                arrayOf(
                    doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, 4.0),
                    doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 3.0),
                    doubleArrayOf(0.0, 0.0, 1.0, 1.0, 0.0, 3.0),
                    doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0),
                )
            )
        ),
    )

    assertEquals(
        mapOf(3 to 0..4, 5 to 0..3),
        getTestRangesFromMatrix(
            createRealMatrix(
                arrayOf(
                    doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, -1.0, 2.0),
                    doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 5.0),
                    doubleArrayOf(0.0, 0.0, 1.0, 1.0, 0.0, -1.0, 1.0),
                    doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 3.0),
                )
            )
        ),
    )
  }

  @Test
  fun testFreeVariables() {
    assertEquals(
        setOf(1),
        freeVariables(
            createRealMatrix(
                arrayOf(
                    doubleArrayOf(1.0, 0.0, 0.0, 3.0),
                    doubleArrayOf(0.0, 0.0, 1.0, 5.0),
                    doubleArrayOf(0.0, 0.0, 0.0, 0.0),
                )
            )
        ),
    )
  }

  @Test
  fun testApplyPartial() {
    assertEquals(
        createRealMatrix(
            arrayOf(
                doubleArrayOf(1.0, 0.0, 0.0, 0.0, -1.0, -1.0),
                doubleArrayOf(0.0, 1.0, 0.0, 0.0, 1.0, 5.0),
                doubleArrayOf(0.0, 0.0, 1.0, 0.0, -1.0, -2.0),
                doubleArrayOf(0.0, 0.0, 0.0, 1.0, 1.0, 3.0),
            )
        ),
        applyTestToSystemPartial(
            createRealMatrix(
                arrayOf(
                    doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, -1.0, 2.0),
                    doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 5.0),
                    doubleArrayOf(0.0, 0.0, 1.0, 1.0, 0.0, -1.0, 1.0),
                    doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 3.0),
                )
            ),
            doubleArrayOf(
                Double.NaN,
                Double.NaN,
                Double.NaN,
                3.0,
                Double.NaN,
                Double.NaN,
            ),
        ),
    )

    assertEquals(
        createRealMatrix(
            arrayOf(
                doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, 4.0),
                doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 3.0),
                doubleArrayOf(0.0, 0.0, 1.0, 1.0, 0.0, 3.0),
                doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0),
            )
        ),
        applyTestToSystemPartial(
            createRealMatrix(
                arrayOf(
                    doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, -1.0, 2.0),
                    doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 5.0),
                    doubleArrayOf(0.0, 0.0, 1.0, 1.0, 0.0, -1.0, 1.0),
                    doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 3.0),
                )
            ),
            doubleArrayOf(
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                2.0,
            ),
        ),
    )

    assertEquals(
        createRealMatrix(
            arrayOf(
                doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0),
                doubleArrayOf(0.0, 1.0, 0.0, 0.0, 3.0),
                doubleArrayOf(0.0, 0.0, 1.0, 0.0, 0.0),
                doubleArrayOf(0.0, 0.0, 0.0, 1.0, 1.0),
            )
        ),
        applyTestToSystemPartial(
            createRealMatrix(
                arrayOf(
                    doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, -1.0, 2.0),
                    doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 5.0),
                    doubleArrayOf(0.0, 0.0, 1.0, 1.0, 0.0, -1.0, 1.0),
                    doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 3.0),
                )
            ),
            doubleArrayOf(
                Double.NaN,
                Double.NaN,
                Double.NaN,
                3.0,
                Double.NaN,
                2.0,
            ),
        ),
    )
  }

  @Test
  fun testRowReduction() {
    val machine = Machine.fromString("[.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}")
    assertEquals(
        createRealMatrix(
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
        createRealMatrix(
            arrayOf(
                doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, -1.0),
                doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 1.0),
                doubleArrayOf(0.0, 0.0, 1.0, 1.0, 0.0, -1.0),
                doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0),
            )
        ),
        reduceToEchelon(machine.buttonMatrix()),
    )

    assertEquals(
        createRealIdentityMatrix(2),
        reduceToEchelon(
            createRealMatrix(
                arrayOf(
                    doubleArrayOf(5.0, 0.0),
                    doubleArrayOf(0.0, 3.0),
                )
            )
        ),
    )
  }
}
