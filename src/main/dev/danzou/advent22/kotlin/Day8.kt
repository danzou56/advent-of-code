package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*

internal class Day8 : AdventTestRunner() {
    fun getMatrix(input: String): Matrix<Int> =
        input.split("\n").map { it.map { it.toString().toInt() } }

    fun isVisibleInDir(matrix: Matrix<Int>, pos: Pair<Int, Int>, dir: Pair<Int, Int>): Boolean {
        val height = matrix[pos]
        tailrec fun isVisibleInDir(pos: Pair<Int, Int>): Boolean {
            val nextPos = pos + dir
            if (!matrix.containsPos(nextPos)) return true
            if (matrix[nextPos] >= height) return false
            return isVisibleInDir(nextPos)
        }
        return isVisibleInDir(pos)
    }

    override fun part1(input: String): Any {
        val matrix = getMatrix(input)
        return matrix.mapIndexed2D { p, _ ->
            cardinalDirections
                .map { isVisibleInDir(matrix, p, it) }
                .reduce(Boolean::or)
        }.flatten().count { it }
    }

    fun getVisibleDistance(matrix: Matrix<Int>, pos: Pair<Int, Int>, dir: Pair<Int, Int>): Int {
        val height = matrix[pos]
        fun getVisibleDistance(pos: Pair<Int, Int>): Int {
            val nextPos = pos + dir
            if (!matrix.containsPos(nextPos)) return 0
            if (matrix[nextPos] >= height) return 1
            return 1 + getVisibleDistance(nextPos)
        }
        return getVisibleDistance(pos)
    }

    fun getScenicScore(matrix: Matrix<Int>, pos: Pair<Int, Int>): Int =
        cardinalDirections
            .map { getVisibleDistance(matrix, pos, it) }
            .reduce(Int::times)

    override fun part2(input: String): Any {
        val matrix = getMatrix(input)
        return matrix
            .mapIndexed2D { p, _ -> getScenicScore(matrix, p) }
            .flatten()
            .max()
    }

}