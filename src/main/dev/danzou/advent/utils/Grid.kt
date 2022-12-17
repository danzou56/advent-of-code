package dev.danzou.advent.utils

import kotlin.math.absoluteValue

val cardinalDirections = setOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))

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
    (this - other).let { (diffX, diffY) -> diffX.absoluteValue + diffY.absoluteValue }

fun <T> Matrix<T>.getNeighboring(p: Pos) =
    this.getNeighboringPos(p).map { this[p] }

fun <T> Matrix<T>.getNeighboring(i: Int, j: Int): List<T> =
    this.getNeighboring(Pair(i, j))

fun <T> Matrix<T>.getNeighboringPos(p: Pos): List<Pos> =
    cardinalDirections.map { it + p }.filter { this.containsPos(it) }

fun <T> Matrix<T>.getNeighboringPos(i: Int, j: Int): List<Pos> =
    this.getNeighboringPos(Pair(i, j))

fun <T> Matrix<T>.transpose(): Matrix<T> {
    // Make sure matrix isn't ragged
    assert(this.map { it.size }.toSet().size == 1)
    return this[0].indices.map { j ->
        this.indices.map { i ->
            this[i][j]
        }
    }
}

fun <T, R> Matrix<T>.map2D(transform: (T) -> R): Matrix<R> =
    this.map { it.map { transform(it) } }

fun <T, R> Matrix<T>.mapIndexed2D(transform: (Pos, T) -> R): Matrix<R> =
    this.mapIndexed { i, it -> it.mapIndexed { j, it -> transform(Pair(i, j), it) } }

fun <T, R> Matrix<T>.mapIndexed2D(transform: (Int, Int, T) -> R): Matrix<R> =
    this.mapIndexed { i, it -> it.mapIndexed { j, it -> transform(i, j, it) } }

fun <T> Matrix<T>.slice(indices: Iterable<Pos>): List<T> =
    indices.map { this[it] }

fun <T> Matrix<T>.row(i: Int): List<T> = this[i]

fun <T> Matrix<T>.col(j: Int): List<T> = this.map { it[j] }

val <T> Matrix<T>.indices2D: List<Pos>
    get() = this.mapIndexed { i, it -> it.indices.map { j -> Pair(i, j) } }.flatten()

fun <T> Matrix<T>.containsPos(p: Pos) = p.first in this.indices && p.second in this[p.first].indices

fun <T> RaggedMatrix<T>.padRowEnds(defaultValue: (Int, Int) -> T): Matrix<T> {
    val maxRowLen = this.maxOf { it.size }
    return this.mapIndexed { i, it ->
        it + (it.size until maxRowLen).map { j -> defaultValue(i, j) }
    }
}

operator fun <T> Matrix<T>.get(p: Pos): T = this[p.first][p.second]
operator fun <T> MutableMatrix<T>.set(p: Pos, value: T) { this[p.first][p.second] = value }