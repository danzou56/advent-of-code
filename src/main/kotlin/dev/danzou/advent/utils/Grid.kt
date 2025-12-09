package dev.danzou.advent.utils

import dev.danzou.advent.utils.geometry.Compass
import dev.danzou.advent.utils.geometry.Direction
import dev.danzou.advent.utils.geometry.Point
import dev.danzou.advent.utils.geometry.Pos
import dev.danzou.advent.utils.geometry.plus
import dev.danzou.advent.utils.geometry.toPair
import dev.danzou.advent.utils.geometry.x
import dev.danzou.advent.utils.geometry.y
import kotlin.math.absoluteValue
import kotlin.math.max

val cardinalDirections = Direction.entries.map { it.dir }.toSet()

typealias RaggedMatrix<T> = List<List<T>>
typealias Matrix<T> = List<List<T>>
typealias MutableMatrix<T> = MutableList<MutableList<T>>
typealias SparseMatrix<T> = Map<Pos, T>
typealias MutableSparseMatrix<T> = MutableMap<Pos, T>

inline fun <reified T> String.asMatrix(): Matrix<T> {
    return when (T::class) {
        Int::class -> this.asMatrix(Char::digitToInt)
        Char::class -> this.asMatrix { it }
        else -> throw IllegalArgumentException("Class ${T::class.simpleName} not supported for asMatrix conversion without transformer")
    } as Matrix<T>
}

fun <T> String.asMatrix(transformer: (Char) -> T): Matrix<T> =
    this.split("\n").map { it.map(transformer) }

fun List<Int>.toPos(): Pos = this.toPair()
fun List<Int>.toPoint(): Point = this.toPair()

infix fun Pos.manhattanDistanceTo(other: Pos): Int =
    (this.first - other.first).absoluteValue + (this.second - other.second).absoluteValue

fun Pos.chessDistanceTo(other: Pos): Int = this.chebyshevDistanceTo(other)

fun Pos.chebyshevDistanceTo(other: Pos): Int =
    max((this.first - other.first).absoluteValue, (this.second - other.second).absoluteValue)

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
    get() = this.flatMapIndexed { j, it -> it.indices.map { i -> Pair(i, j) } }

fun <T> Matrix<T>.containsPos(p: Pos) = p.second in this.indices && p.first in this[p.second].indices

fun <T> RaggedMatrix<T>.padRowEnds(defaultValue: (Int, Int) -> T): Matrix<T> {
    val maxRowLen = this.maxOf { it.size }
    return this.mapIndexed { i, it ->
        it + (it.size until maxRowLen).map { j -> defaultValue(i, j) }
    }
}

operator fun <T> Matrix<T>.get(p: Pos): T = this[p.second][p.first]
fun <T> Matrix<T>.getOrNull(p: Pos): T? = this.getOrNull(p.second)?.getOrNull(p.first)
fun <T> Matrix<T>.getOrElse(p: Pos, defaultValue: (Pos) -> T): T = this.getOrNull(p) ?: defaultValue(p)
fun <T> Matrix<T>.getOrElse(p: Pos, defaultValue: (Int, Int) -> T): T = this.getOrNull(p) ?: defaultValue(p.x, p.y)
operator fun <T> MutableMatrix<T>.set(p: Pos, value: T) { this[p.second][p.first] = value }