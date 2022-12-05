package dev.danzou.advent22.kotlin

import dev.danzou.advent22.kotlin.utils.AdventTestRunner
import scala.collection.mutable.Stack
import toPair
import java.util.*
import kotlin.collections.ArrayList


internal class Day5 : AdventTestRunner() {
    data class Instruction(val quantity: Int, val source: Int, val target: Int)

    fun String.toInstruction(): Instruction =
        this.split(" ").let {
            Instruction(it[1].toInt(), it[3].toInt() - 1, it[5].toInt() - 1)
        }

    override fun part1(input: String): Number {
        val (crateInput, procedureInput) = input.split("\n\n").toPair()
        val procedures = procedureInput.split("\n")
            .map { it.toInstruction() }

        val crateMatrix: List<List<Char>> = crateInput.split("\n")
            .dropLast(1)
//            .map { it + " " }
            .map { it.chunked(4).map { it[1] } }
            .reversed()


        val crateStacks: List<MutableList<Char>> = crateMatrix.get(0).indices
            .map { colI -> crateMatrix.indices.map { rowI ->
                crateMatrix[rowI].getOrElse(colI) { ' ' }
            }
                .filter { it != ' ' }
                .toMutableList() }
//            .map { it: List<Char> -> it }

        println(crateStacks)

        for (procedure in procedures) {
            for (i in 0 until procedure.quantity) {
                crateStacks[procedure.target].add(crateStacks[procedure.source].removeLast())
            }
        }

        print(crateStacks.map { it[it.size - 1] })

        return 0
    }

    override fun part2(input: String): Number {
        val (crateInput, procedureInput) = input.split("\n\n").toPair()
        val procedures = procedureInput.split("\n")
            .map { it.toInstruction() }

        val crateMatrix: List<List<Char>> = crateInput.split("\n")
            .dropLast(1)
//            .map { it + " " }
            .map { it.chunked(4).map { it[1] } }
            .reversed()


        val crateStacks: List<MutableList<Char>> = crateMatrix.get(0).indices
            .map { colI -> crateMatrix.indices.map { rowI ->
                crateMatrix[rowI].getOrElse(colI) { ' ' }
            }
                .filter { it != ' ' }
                .toMutableList() }
//            .map { it: List<Char> -> it }

        println(crateStacks)

        for (procedure in procedures) {
//            crateStacks[procedure.target].
            val dropped = crateStacks[procedure.source].subList(crateStacks[procedure.source].size - procedure
                .quantity,
                crateStacks[procedure.source].size).toList()
            for (i in 0 until procedure.quantity) {
                crateStacks[procedure.source].removeLast()
            }
            for (droppedEl in dropped)
                crateStacks[procedure.target].add(droppedEl)
//            crateStacks[procedure.target].addAll(dropped)
        }

        print(crateStacks.map { it[it.size - 1] })

        return 0
    }
}