package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*

internal class Day8 : AdventTestRunner() {
    fun getMatrix(input: String): Matrix<Int> =
        input.split("\n").map { it.map { it.toString().toInt() } }

    fun isVisibleInDir(matrix: Matrix<Int>, pos: Pair<Int, Int>, dir: Pair<Int, Int>): Boolean {
        val height = matrix[pos]
        fun isVisibleInDir(pos: Pair<Int, Int>): Boolean {
            try {
                val nextPos = pos + dir
                val nextHeight = matrix[nextPos]
                if (nextHeight >= height) return false
                return isVisibleInDir(nextPos)
            } catch (e: IndexOutOfBoundsException) {
                return true
            }
        }
        return isVisibleInDir(pos)
    }

    override fun part1(input: String): Any {
        val matrix = getMatrix(input)
        return matrix.mapIndexed { i, it ->
            it.mapIndexed { j, _ ->
                cardinalDirections
                    .map { isVisibleInDir(matrix, Pair(i, j), it) }
                    .reduce(Boolean::or)
            }
        }.sumOf { it.count { it } }
    }

    fun getVisibleDistance(matrix: Matrix<Int>, pos: Pair<Int, Int>, dir: Pair<Int, Int>): Int {
        val height = matrix[pos]
        fun getVisibleDistance(pos: Pair<Int, Int>): Int {
            try {
                val nextPos = pos + dir
                val nextHeight = matrix[nextPos]
                if (nextHeight >= height) return 1
                return 1 + getVisibleDistance(nextPos)
            } catch (e: IndexOutOfBoundsException) {
                return 0
            }
        }
        return getVisibleDistance(pos)
    }

    fun getScenicScore(matrix: Matrix<Int>, pos: Pair<Int, Int>): Int =
        cardinalDirections
            .map { getVisibleDistance(matrix, pos, it) }
            .reduce(Int::times)

    override fun part2(input: String): Any {
        val matrix = getMatrix(input)
        return matrix.mapIndexed { i, it ->
            it.mapIndexed { j, _ ->
                getScenicScore(matrix, Pair(i, j))
            }.max()
        }.max()
    }

}