package dev.danzou.advent.utils

import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.Direction
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.toPair
import kotlin.math.absoluteValue

val cardinalDirections = Direction.values().map { it.dir }.toSet()

typealias RaggedMatrix<T> = List<List<T>>
typealias Matrix<T> = List<List<T>>
typealias MutableMatrix<T> = MutableList<MutableList<T>>
typealias Pos = Pair<Int, Int>
typealias Point = Pair<Int, Int>
typealias SparseMatrix<T> = Map<Pos, T>
typealias MutableSparseMatrix<T> = MutableMap<Pos, T>

val Pos.x: Int
    get() = this.first

val Pos.y: Int
    get() = this.second

fun List<Int>.toPos(): Pos = this.toPair()
fun List<Int>.toPoint(): Point = this.toPair()

fun Pos.manhattanDistanceTo(other: Pos): Int =
    (this.first - other.first).absoluteValue + (this.second - other.second).absoluteValue

fun <T> Matrix<T>.neighboring(p: Pos): List<T> =
    this.neighboringPos(p).map { this[it] }

fun <T> Matrix<T>.neighboring(i: Int, j: Int): List<T> =
    this.neighboring(Pair(i, j))

fun <T> Matrix<T>.neighboringPos(p: Pos, dirs: Collection<Pos> = Compass.CARDINAL_DIRECTIONS): List<Pos> =
    dirs.map { it + p }.filter { this.containsPos(it) }

fun <T> Matrix<T>.neighboringPos(i: Int, j: Int): List<Pos> =
    this.neighboringPos(Pair(i, j))

fun <T> Matrix<T>.transpose(): Matrix<T> {
    // Make sure matrix isn't ragged
    assert(this.map { it.size }.toSet().size == 1)
    return this[0].indices.map { i ->
        this.indices.map { j ->
            this[j][i]
        }
    }
}

fun <T, R> Matrix<T>.map2D(transform: (T) -> R): Matrix<R> =
    this.map { it.map { transform(it) } }

fun <T, R> Matrix<T>.mapIndexed2D(transform: (Pos, T) -> R): Matrix<R> =
    this.mapIndexed { j, it -> it.mapIndexed { i, it -> transform(Pair(i, j), it) } }

fun <T, R> Matrix<T>.mapIndexed2D(transform: (Int, Int, T) -> R): Matrix<R> =
    this.mapIndexed { j, it -> it.mapIndexed { i, it -> transform(i, j, it) } }

fun <T> Matrix<T>.slice(indices: Iterable<Pos>): List<T> =
    indices.map { this[it] }

fun <T> Matrix<T>.row(i: Int): List<T> = this[i]

fun <T> Matrix<T>.col(j: Int): List<T> = this.map { it[j] }

val <T> Matrix<T>.indices2D: List<Pos>
    get() = this.mapIndexed { j, it -> it.indices.map { i -> Pair(i, j) } }.flatten()

fun <T> Matrix<T>.containsPos(p: Pos) = p.second in this.indices && p.first in this[p.second].indices

fun <T> RaggedMatrix<T>.padRowEnds(defaultValue: (Int, Int) -> T): Matrix<T> {
    val maxRowLen = this.maxOf { it.size }
    return this.mapIndexed { i, it ->
        it + (it.size until maxRowLen).map { j -> defaultValue(i, j) }
    }
}

operator fun <T> Matrix<T>.get(p: Pos): T = this[p.second][p.first]
operator fun <T> MutableMatrix<T>.set(p: Pos, value: T) { this[p.second][p.first] = value }