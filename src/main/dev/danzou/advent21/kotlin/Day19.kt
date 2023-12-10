package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry3.Pos3
import dev.danzou.advent.utils.geometry3.manhattanDistanceTo
import dev.danzou.advent.utils.geometry3.squaredDistanceTo
import dev.danzou.advent.utils.geometry3.toTriple
import dev.danzou.advent21.AdventTestRunner21
import org.apache.commons.math3.linear.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.round

internal class Day19 : AdventTestRunner21("Beacon Scanner") {
    data class Scanner(val id: Int)
    data class Beacon(val pos: Pos3, val scanner: Scanner) {
        fun asExtendedRealVector(): RealVector =
            (pos.toList() + 1)
                .map(Int::toDouble)
                .toDoubleArray()
                .let { MatrixUtils.createRealVector(it) }
    }

    class BeaconEdge(val beacon1: Beacon, val beacon2: Beacon) : Data() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as BeaconEdge

            return when {
                beacon1 == other.beacon1 && beacon2 == other.beacon2 -> true
                beacon1 == other.beacon2 && beacon2 == other.beacon1 -> true
                else -> false
            }
        }

        override fun hashCode(): Int {
            return beacon1.hashCode() + beacon2.hashCode()
        }
    }

    class Scan(val beacons: Map<Scanner, List<Beacon>>) {
        val scanners = beacons.keys

        private val distances: Map<Scanner, Map<Beacon, Map<Beacon, Int>>> =
            beacons.mapValues { (_, beacons) ->
                beacons.pairs()
                    .map { (b1, b2) -> Pair(b1, b2) to b1.pos.squaredDistanceTo(b2.pos) }
                    .flatMap { (pair, dist) ->
                        listOf(
                            pair to dist, pair.reversed() to dist
                        )
                    }
                    .groupingBy { (bs, _) -> bs.first }
                    .aggregate { _, map: Map<Beacon, Int>?, (bs: Pair<Beacon, Beacon>, dist: Int), _ ->
                        (map ?: emptyMap()) + (bs.second to dist)
                    }
            }

        private val differences: Map<Scanner, Map<Int, BeaconEdge>> =
            beacons.mapValues { (_, beacons) ->
                beacons.pairs()
                    .associate { (b1, b2) -> b1.pos.squaredDistanceTo(b2.pos) to BeaconEdge(b1, b2) }
            }

        val transforms: Map<Int, Pair<Int, RealMatrix>> =
            mutableMapOf<Int, Pair<Int, RealMatrix>>().apply {
                val minOverlappingSetSize = MIN_OVERLAP choose 2
                val overlappingScanners: Map<Scanner, Set<Scanner>> = beacons.keys.pairs()
                    .filter { scanners ->
                        scanners.map(differences::getValue)
                            .map { it.keys }
                            .reduce(Set<Int>::intersect)
                            .size >= minOverlappingSetSize
                    }
                    .map { it.toList() }
                    .flatMap { (f, s) -> listOf(f to s, s to f) }
                    .groupingBy { it.first }
                    .aggregate { _, scanners, (_, scanner), _ ->
                        (scanners ?: emptySet()) + scanner
                    }


                // Use BFS to discover all the scanners and their predecessor back to scanner 0
                bfs(Scanner(0)) { cur ->
                    overlappingScanners.getValue(cur).onEach { next ->
                        computeIfAbsent(next.id) { _ ->
                            cur.id to solve(
                                pairings(
                                    from = next,
                                    to = cur
                                )
                            )
                        }
                    }
                }
            }

        /**
         * Given paired points, return the matrix Aᵀ₁₀ such that A₁₀B₁ = B₀
         */
        private fun solve(pairings: List<Pair<Beacon, Beacon>>): RealMatrix {
            // Split list of pair of beacons (triples) into two matrices
            val (matrixB1, matrixB0) = pairings.unzip().toList().map { beacons ->
                MatrixUtils.createRealMatrix(beacons.size, 4).apply {
                    beacons.forEachIndexed { i, beacon ->
                        // Add an extra dimension to each vector
                        // Technically, we don't need to add an extra dimension to toB; in this
                        // case the affine transformation matrix A would be given by [R t] where
                        // R is the rotation matrix and t is the translation vector. Adding the
                        // extra dimension makes the affine transformation matrix [R t; 0 0 0 1],
                        // keeping the image of the A in R4
                        setRowVector(i, beacon.asExtendedRealVector())
                    }
                }
            }

            // Solve B₁ᵀA₁₀ᵀ = B₀ᵀ for A₁₀ᵀ
            // Decomposition solver solve for the right multiplicand in AX = Y with A, Y known. To
            // solve for the left multiplicand, rearrange to XᵀAᵀ = Yᵀ. Solving for unknown right
            // multiplicand Aᵀ is now possible given known Xᵀ, Yᵀ.
            return QRDecomposition(matrixB1).solver.solve(matrixB0)
                .apply {
                    // Check that this is a square 4x4 matrix
                    assert(rowDimension == 4 && columnDimension == 4)

                    // Round all entries after checking that they are "integers"
                    object : DefaultRealMatrixChangingVisitor() {
                        override fun visit(row: Int, column: Int, value: Double): Double {
                            val newValue = round(value)
                            require(newValue - value in -E..E)
                            return newValue
                        }
                    }.let(::walkInRowOrder)

                    // Check that this is a valid affine transformation matrix with no scaling
                    require(
                        getColumn(3)
                            .zip(doubleArrayOf(0.0, 0.0, 0.0, 1.0))
                            .all { (actual, expected) -> actual - expected in -E..E }
                    )
                    require(
                        1 - LUDecomposition(
                            getSubMatrix(0, 2, 0, 2)
                        ).determinant in -E..E
                    )
                }
        }

        /**
         * Given known matched scanners, return a list of paired points
         */
        private fun pairings(
            from: Scanner,
            to: Scanner
        ): List<Pair<Beacon, Beacon>> {
            // Find sub-maps of the distances map with matching beacon distances
            val overlap = differences[to]!!.keys.intersect(differences[from]!!.keys)
            val fromDistances = distances[from]!!
                .mapValues { (_, map) -> map.filter { (_, dist) -> dist in overlap } }
                .filter { (_, map) -> map.isNotEmpty() }
            val toDistances = distances[to]!!
                .mapValues { (_, map) -> map.filter { (_, dist) -> dist in overlap } }
                .filter { (_, map) -> map.isNotEmpty() }

            // Arbitrarily choose an initial starting beacon and find its matching beacon in the
            // target context by finding the beacon whose distance map has identical values
            val toBeacon = toDistances.keys.first()
            val fromBeacon = fromDistances.entries.single { (_, map) ->
                toDistances[toBeacon]!!.values.toSet() == map.values.toSet()
            }.key

            // The rest of the beacons can be matched by exclusively examining the sub-map of the
            // initial beacons and matching the unknown beacons by their distances
            return toDistances[toBeacon]!!.map { (toBeacon, distance) ->
                fromDistances[fromBeacon]!!
                    .entries
                    .single { it.value == distance }
                    .key to toBeacon

            } + (fromBeacon to toBeacon)
        }

        /**
         * Transform data in an arbitrary context into the context 0
         */
        fun transformTo0(data: RealMatrix, from: Scanner): RealMatrix {
            var context = from.id
            var transformed = data
            while (context != 0) {
                require(transformed.columnDimension == 4)
                val (newContext, transform) = transforms[context]!!
                transformed = transform.preMultiply(transformed)
                context = newContext
            }
            return transformed
        }

        companion object {
            val MIN_OVERLAP = 12

            fun fromString(input: String): Scan {
                val beacons = input.split("\n\n").associate {
                    val lines = it.split("\n")
                    val scanner = lines.first()
                        .getValue<Int>()
                        .let(::Scanner)
                    val beacons = lines
                        .drop(1)
                        .map { line -> line.getValues<Int>().toTriple() }
                        .map { p3 -> Beacon(p3, scanner) }
                    scanner to beacons
                }
                return Scan(beacons)
            }
        }
    }

    override fun part1(input: String): Int {
        val scan = Scan.fromString(input)
        return scan.beacons.entries
            .map { (scanner, beacons) ->
                scanner to MatrixUtils.createRealMatrix(beacons.size, 4).apply {
                    beacons.forEachIndexed { i, beacon ->
                        setRowVector(i, beacon.asExtendedRealVector())
                    }
                }
            }
            .map { (scanner, matrix) ->
                scan.transformTo0(matrix, from = scanner)
            }
            .flatMap { it.data.toList() }
            .map { it.toList().map(::round).map(Double::toInt) }
            .toSet()
            .size
    }

    override fun part2(input: String): Int {
        val scan = Scan.fromString(input)
        return scan.scanners
            .map { scanner ->
                scan.transformTo0(
                    MatrixUtils.createRowRealMatrix(doubleArrayOf(0.0, 0.0, 0.0, 1.0)),
                    from = scanner
                )
            }
            .map { it.getRow(0) }
            .map { it.toList().map(::round).map(Double::toInt).take(3) }
            .choose(2)
            .maxOf { (s1, s2) -> s1.toTriple().manhattanDistanceTo(s2.toTriple()) }
    }

    @Test
    fun testExample() {
        val input = readFileString("$baseInputPath/day$day.ex.in")

        assertEquals(79, part1(input))
    }
}