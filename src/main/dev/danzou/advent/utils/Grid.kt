package dev.danzou.advent.utils

val cardinalDirections = setOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))

typealias RaggedMatrix<T> = List<List<T>>
typealias Matrix<T> = List<List<T>>

fun <T> Matrix<T>.getCellNeighbors(p: Pair<Int, Int>) =
    this.getCellNeighbors(p.first, p.second)

fun <T> Matrix<T>.getCellNeighbors(i: Int, j: Int): List<T> = listOfNotNull(
    this.getOrNull(i - 1)?.get(j),
    this.getOrNull(i + 1)?.get(j),
    this.get(i).getOrNull(j - 1),
    this.get(i).getOrNull(j + 1),
)

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

fun <T, R> Matrix<T>.mapIndexed2D(transform: (Pair<Int, Int>, T) -> R): Matrix<R> =
    this.mapIndexed { i, it -> it.mapIndexed { j, it -> transform(Pair(i, j), it) } }

fun <T, R> Matrix<T>.mapIndexed2D(transform: (Int, Int, T) -> R): Matrix<R> =
    this.mapIndexed { i, it -> it.mapIndexed { j, it -> transform(i, j, it) } }

val <T> Matrix<T>.indices2D: List<Pair<Int, Int>>
    get() = this.mapIndexed { i, it -> it.indices.map { j -> Pair(i, j) } }.flatten()

fun <T> RaggedMatrix<T>.padRowEnds(defaultValue: (Int, Int) -> T): Matrix<T> {
    val maxRowLen = this.maxOf { it.size }
    return this.mapIndexed { i, it ->
        it + (it.size until maxRowLen).map { j -> defaultValue(i, j) }
    }
}

operator fun <T> Matrix<T>.get(p: Pair<Int, Int>): T = this[p.first][p.second]