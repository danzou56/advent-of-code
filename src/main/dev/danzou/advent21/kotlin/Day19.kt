package dev.danzou.advent21.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent.utils.geometry3.Pos3
import dev.danzou.advent.utils.geometry3.manhattanDistanceTo
import dev.danzou.advent.utils.geometry3.squaredDistanceTo
import dev.danzou.advent.utils.geometry3.toTriple
import dev.danzou.advent21.AdventTestRunner21
import org.apache.commons.math3.linear.LUDecomposition
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.linear.RealMatrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.round

internal class Day19 : AdventTestRunner21("Beacon Scanner") {
    val MIN_OVERLAP = 12

    data class Scanner(val id: Int)
    data class Beacon(val pos: Pos3, val scanner: Scanner)
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

    /**
     * Determine the affine transformation matrix that maps beacons in scanner 1's context into
     * scanner 0's context.
     *
     * The transformation defining the mapping between two scanner's contexts is known as an affine
     * transformation. We are told that the transformation involves a rotation and a translation.
     * Transformations only involving a rotation would be given by a dxd matrix with determinant 1;
     * however, translations are not linear. In order to make the transformation linear, we use a
     *
     */
    fun resolveTransformation(
        distances: Map<Scanner, Map<Beacon, Map<Beacon, Int>>>,
        differences: Map<Scanner, Map<Int, BeaconEdge>>,
        imageScanner: Scanner,
        preImageScanner: Scanner
    ): RealMatrix {
        val overlappingDists: Set<Int> = differences[imageScanner]!!.keys.intersect(differences[preImageScanner]!!.keys)
        val pairings = resolveBeaconPairings(
            distances[imageScanner]!!
                .mapValues { (_, map) -> map.filter { (_, dist) -> dist in overlappingDists } }
                .filter { (_, map) -> map.isNotEmpty() },
            distances[preImageScanner]!!
                .mapValues { (_, map) -> map.filter { (_, dist) -> dist in overlappingDists } }
                .filter { (_, map) -> map.isNotEmpty() }
        )
        // image
        val b_cols = pairings
            .map { it.beacon1.pos }
            .map { (x, y, z) ->
                doubleArrayOf(
                    x.toDouble(),
                    y.toDouble(),
                    z.toDouble(),
                    1.0
                )
            } // 1.0 not needed but helps
            .toTypedArray()
        val B = MatrixUtils.createRealMatrix(b_cols)
        assert(B.columnDimension == 4)
        assert(B.rowDimension >= 12)
        // pre-image
        val x_cols = pairings
            .map { it.beacon2.pos }
            .map { (x, y, z) -> doubleArrayOf(x.toDouble(), y.toDouble(), z.toDouble(), 1.0) }
            .toTypedArray()
        val X = MatrixUtils.createRealMatrix(x_cols)
        assert(X.columnDimension == 4)
        assert(X.rowDimension >= 12)
        val A = QRDecomposition(X).solver.solve(B)
        assert(A.rowDimension == 4)
        assert(A.columnDimension == 4)
        require(A.data.all { it.all { d -> abs(round(d) - d) < E } })
//        require(A.getRow(3).zip(doubleArrayOf(0.0, 0.0, 0.0, 1.0)).all { (actual, expected) -> actual - expected in -E..E })
        val roundedA = MatrixUtils.createRealMatrix(A.data.map { it.map(::round).toDoubleArray() }.toTypedArray())
        require(LUDecomposition(roundedA.getSubMatrix(0, 2, 0, 2)).determinant in 1 - E..1 + E)

        return roundedA
    }

    fun resolveBeaconPairings(
        imageDistances: Map<Beacon, Map<Beacon, Int>>,
        preImageDistances: Map<Beacon, Map<Beacon, Int>>,
    ): List<BeaconEdge> {
        val initBeacon = imageDistances.keys.first()
        val initBeaconInPreImage = preImageDistances.entries.single { (_, map) ->
            imageDistances[initBeacon]!!.values.toSet() == map.values.toSet()
        }.key
        val initPairing = listOf(
            BeaconEdge(
                initBeacon,
                initBeaconInPreImage
            )
        )
        return (initPairing + imageDistances[initBeacon]!!.map { (beacon, distance) ->
            BeaconEdge(
                beacon,
                preImageDistances[initBeaconInPreImage]!!
                    .entries
                    .single { it.value == distance }
                    .key
            )
        }).also { assert(it.size in 12..17) }
    }

    override fun part1(input: String): Any {
        val beaconsByScanners: Map<Scanner, List<Beacon>> =
            input.split("\n\n").associate {
                val lines = it.split("\n")
                val scanner = Regex("\\d+")
                    .find(lines.first())!!
                    .value
                    .toInt()
                    .let(::Scanner)
                val beacons = lines
                    .drop(1)
                    .map { line ->
                        line.split(",")
                            .map(String::toInt)
                            .let { (x, y, z) -> Pos3(x, y, z) }
                    }.map { p3 -> Beacon(p3, scanner) }
                scanner to beacons
            }

        // essentially undirected edge map
        val distances: Map<Scanner, Map<Beacon, Map<Beacon, Int>>> =
            beaconsByScanners.mapValues { (_, beacons) ->
                (beacons.toSet() choose 2)
                    .map { it.toList() }
                    .map { (b1, b2) -> Pair(b1, b2) to b1.pos.squaredDistanceTo(b2.pos) }
                    .flatMap { (pair, dist) ->
                        listOf(
                            pair to dist, pair.reversed() to dist
                        )
                    }
                    .groupBy { (pair, _) -> pair.first }
                    .mapValues { (_, v) -> v.associate { (pair, dist) -> pair.second to dist } }
            }
        val differences: Map<Scanner, Map<Int, BeaconEdge>> =
            beaconsByScanners.mapValues { (_, beacons) ->
                (beacons.toSet() choose 2)
                    .map { it.toList() }
                    .associate { (b1, b2) -> b1.pos.squaredDistanceTo(b2.pos) to BeaconEdge(b1, b2) }
            }

        val minOverlap = MIN_OVERLAP choose 2
        require(minOverlap == 66)
        val overlappingScanners: Map<Scanner, Set<Scanner>> =
            (beaconsByScanners.keys choose 2)
                .map { it.toList() }
                .filter { scanners ->
                    scanners.map(differences::getValue)
                        .map { it.keys }
                        .reduce(Set<Int>::intersect)
                        .size >= minOverlap
                }
                .flatMap { (f, s) -> listOf(f to s, s to f) }
                .groupBy { it.first }
                .mapValues { (_, pairs) -> pairs.map(Pair<Scanner, Scanner>::second).toSet() }

        val predecessors = mutableMapOf<Int, Pair<Int, RealMatrix?>>(
            0 to (0 to null)
        )
        bfs(Scanner(0)) { s ->
            val scanners = overlappingScanners.getValue(s)
            scanners.forEach { d ->
                predecessors.computeIfAbsent(d.id) { _ ->
                    s.id to resolveTransformation(distances, differences, s, d)
                }
            }
            scanners
        }

        val beacons = beaconsByScanners.values.flatten()
            .map { (pos, scanner) ->
                var context = scanner.id
//                var beacon = pos.toList().map { it.toDouble() }.toDoubleArray()
                var beacon = MatrixUtils.createRealMatrix(
                    arrayOf((pos.toList().map { it.toDouble() } + 1.0).toDoubleArray())
                )
                assert(beacon.rowDimension == 1)
                assert(beacon.columnDimension == 4)
                while (predecessors[context]!!.second != null) {
                    beacon = predecessors[context]!!.second!!.preMultiply(
                        beacon
                    )
                    context = predecessors[context]!!.first
                    assert(beacon.rowDimension == 1)
                    assert(beacon.columnDimension == 4)
                }
                beacon.data[0].map(::round).map(Double::toInt).toList()
            }

        return beacons.toSet().size
    }

    override fun part2(input: String): Any {
        val beaconsByScanners: Map<Scanner, List<Beacon>> =
            input.split("\n\n").associate {
                val lines = it.split("\n")
                val scanner = Regex("\\d+")
                    .find(lines.first())!!
                    .value
                    .toInt()
                    .let(::Scanner)
                val beacons = lines
                    .drop(1)
                    .map { line ->
                        line.split(",")
                            .map(String::toInt)
                            .let { (x, y, z) -> Pos3(x, y, z) }
                    }.map { p3 -> Beacon(p3, scanner) }
                scanner to beacons
            }

        // essentially undirected edge map
        val distances: Map<Scanner, Map<Beacon, Map<Beacon, Int>>> =
            beaconsByScanners.mapValues { (_, beacons) ->
                (beacons.toSet() choose 2)
                    .map { it.toList() }
                    .map { (b1, b2) -> Pair(b1, b2) to b1.pos.squaredDistanceTo(b2.pos) }
                    .flatMap { (pair, dist) ->
                        listOf(
                            pair to dist, pair.reversed() to dist
                        )
                    }
                    .groupBy { (pair, _) -> pair.first }
                    .mapValues { (_, v) -> v.associate { (pair, dist) -> pair.second to dist } }
            }
        val differences: Map<Scanner, Map<Int, BeaconEdge>> =
            beaconsByScanners.mapValues { (_, beacons) ->
                (beacons.toSet() choose 2)
                    .map { it.toList() }
                    .associate { (b1, b2) -> b1.pos.squaredDistanceTo(b2.pos) to BeaconEdge(b1, b2) }
            }

        val minOverlap = MIN_OVERLAP choose 2
        require(minOverlap == 66)
        val overlappingScanners: Map<Scanner, Set<Scanner>> =
            (beaconsByScanners.keys choose 2)
                .map { it.toList() }
                .filter { scanners ->
                    scanners.map(differences::getValue)
                        .map { it.keys }
                        .reduce(Set<Int>::intersect)
                        .size >= minOverlap
                }
                .flatMap { (f, s) -> listOf(f to s, s to f) }
                .groupBy { it.first }
                .mapValues { (_, pairs) -> pairs.map(Pair<Scanner, Scanner>::second).toSet() }

        val predecessors = mutableMapOf<Int, Pair<Int, RealMatrix?>>(
            0 to (0 to null)
        )
        bfs(Scanner(0)) { s ->
            val scanners = overlappingScanners.getValue(s)
            scanners.forEach { d ->
                predecessors.computeIfAbsent(d.id) { _ ->
                    s.id to resolveTransformation(distances, differences, s, d)
                }
            }
            scanners
        }

        val beacons = beaconsByScanners.keys
            .map { (id) ->
                var context = id
//                var beacon = pos.toList().map { it.toDouble() }.toDoubleArray()
                var beacon = MatrixUtils.createRealMatrix(
                    arrayOf(doubleArrayOf(0.0, 0.0, 0.0, 1.0))
                )
                assert(beacon.rowDimension == 1)
                assert(beacon.columnDimension == 4)
                while (predecessors[context]!!.second != null) {
                    beacon = predecessors[context]!!.second!!.preMultiply(
                        beacon
                    )
                    context = predecessors[context]!!.first
                    assert(beacon.rowDimension == 1)
                    assert(beacon.columnDimension == 4)
                }
                beacon.data[0].map(::round).map(Double::toInt).toList()
            }

        return (beacons.toSet() choose 2).map { it.toList().map { it.take(3) } }.maxOf { (b1, b2) -> b1.toTriple().manhattanDistanceTo(b2.toTriple()) }
    }

    @Test
    fun testExample() {
        val input = """
            --- scanner 0 ---
            404,-588,-901
            528,-643,409
            -838,591,734
            390,-675,-793
            -537,-823,-458
            -485,-357,347
            -345,-311,381
            -661,-816,-575
            -876,649,763
            -618,-824,-621
            553,345,-567
            474,580,667
            -447,-329,318
            -584,868,-557
            544,-627,-890
            564,392,-477
            455,729,728
            -892,524,684
            -689,845,-530
            423,-701,434
            7,-33,-71
            630,319,-379
            443,580,662
            -789,900,-551
            459,-707,401

            --- scanner 1 ---
            686,422,578
            605,423,415
            515,917,-361
            -336,658,858
            95,138,22
            -476,619,847
            -340,-569,-846
            567,-361,727
            -460,603,-452
            669,-402,600
            729,430,532
            -500,-761,534
            -322,571,750
            -466,-666,-811
            -429,-592,574
            -355,545,-477
            703,-491,-529
            -328,-685,520
            413,935,-424
            -391,539,-444
            586,-435,557
            -364,-763,-893
            807,-499,-711
            755,-354,-619
            553,889,-390

            --- scanner 2 ---
            649,640,665
            682,-795,504
            -784,533,-524
            -644,584,-595
            -588,-843,648
            -30,6,44
            -674,560,763
            500,723,-460
            609,671,-379
            -555,-800,653
            -675,-892,-343
            697,-426,-610
            578,704,681
            493,664,-388
            -671,-858,530
            -667,343,800
            571,-461,-707
            -138,-166,112
            -889,563,-600
            646,-828,498
            640,759,510
            -630,509,768
            -681,-892,-333
            673,-379,-804
            -742,-814,-386
            577,-820,562

            --- scanner 3 ---
            -589,542,597
            605,-692,669
            -500,565,-823
            -660,373,557
            -458,-679,-417
            -488,449,543
            -626,468,-788
            338,-750,-386
            528,-832,-391
            562,-778,733
            -938,-730,414
            543,643,-506
            -524,371,-870
            407,773,750
            -104,29,83
            378,-903,-323
            -778,-728,485
            426,699,580
            -438,-605,-362
            -469,-447,-387
            509,732,623
            647,635,-688
            -868,-804,481
            614,-800,639
            595,780,-596

            --- scanner 4 ---
            727,592,562
            -293,-554,779
            441,611,-461
            -714,465,-776
            -743,427,-804
            -660,-479,-426
            832,-632,460
            927,-485,-438
            408,393,-506
            466,436,-512
            110,16,151
            -258,-428,682
            -393,719,612
            -211,-452,876
            808,-476,-593
            -575,615,604
            -485,667,467
            -680,325,-822
            -627,-443,-432
            872,-547,-609
            833,512,582
            807,604,487
            839,-516,451
            891,-625,532
            -652,-548,-490
            30,-46,-14
        """.trimIndent()

        assertEquals(79, part1(input))
    }
}