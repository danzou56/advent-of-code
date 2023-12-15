package dev.danzou.advent21.kotlin

import dev.danzou.advent21.AdventTestRunner21
import java.lang.IllegalStateException

internal class Day4: AdventTestRunner21() {

    data class Board(private val board: List<List<Cell>>) {

        fun contains(cell: String): Boolean = contains(Cell(cell))

        fun contains(cell: Cell): Boolean = board.fold(false) { a, b -> a || b.contains(cell) }

        fun getPos(cell: Cell): Pair<Int, Int> {
            return board.foldIndexed(Pair(-1, -1)) { cur, a, b ->
                val i = b.indexOf(cell)
                if (i != -1) Pair(cur, i) else a
            }
        }

        fun isBingo(cell:String): Boolean = isBingo(Cell(cell))

        fun isBingo(cell: Cell): Boolean {
            if (!this.contains(cell)) return false
            val pos = getPos(cell);
            assert(pos.first != -1 && pos.second != -1)
            return board.all { it[pos.second].marked } || board[pos.first].all { it.marked }
        }

        fun mark(cell: String): Boolean = mark(Cell(cell))

        fun mark(cell: Cell): Boolean {
            if (!this.contains(cell)) return false
            val pos = getPos(cell)
            board[pos.first][pos.second].marked = true
            return true
        }

        fun prescore(): Int = board.sumOf { it -> it.filter { !it.marked }.sumOf { it.id.toInt() } }

        data class Cell(val id: String) {
            var marked: Boolean = false
        }
    }

    private fun getDraws(lines: List<String>): List<String> =
        lines.first().split(",")

    private fun getBoards(lines: List<String>): List<Board> =
        lines.slice(2 until lines.size)
            .filter { it != "" }
            .map { it -> it.split(Regex("\\s+")).filter { it != "" } }
            .chunked(5)
            .map { board -> Board(board.map { it -> it.map { Board.Cell(it) } }) }

    override fun part1(input: String): Int {
        val lines = input.split("\n")
        val draws = getDraws(lines)
        val boards = getBoards(lines)

        var res = 0
        for (draw in draws) {
            for (board in boards) {
                if (board.mark(draw) && board.isBingo(draw)) {
                    res = board.prescore() * draw.toInt()
                    return res
                }
            }
        }
        throw IllegalStateException()
    }

    override fun part2(input: String): Int {
        val lines = input.split("\n")
        val draws = getDraws(lines)
        val boards = getBoards(lines)

        var res = 0
        val removed = MutableList(boards.size) { false }
        for (draw in draws) {
            for ((index, board) in boards.withIndex()) {
                if (!removed[index] && board.mark(draw) && board.isBingo(draw)) {
                    res = board.prescore() * draw.toInt()
                    removed[index] = true
                }
            }
        }
        return res
    }
}