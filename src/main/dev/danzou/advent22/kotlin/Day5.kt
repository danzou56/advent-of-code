package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.*


internal class Day5 : AdventTestRunner() {
    data class Instruction(val quantity: Int, val source: Int, val target: Int)

    fun String.toInstruction(): Instruction =
        this.split(" ").let {
            Instruction(it[1].toInt(), it[3].toInt() - 1, it[5].toInt() - 1)
        }

    fun getCrates(input: String): Matrix<Char> {
        val (crateInput, _) = input.split("\n\n").toPair()
        val crateMatrix: RaggedMatrix<Char> =
            crateInput.split("\n")
                .dropLast(1)
                .map { it.chunked(4).map { it[1] } }
                .reversed()
        return crateMatrix.padRowEnds { _, _ -> ' ' }
            .transpose()
            .map { it.filter { it != ' ' } }
    }

    fun getInstructions(input: String): List<Instruction> {
        val (_, procedureInput) = input.split("\n\n").toPair()
        return procedureInput.split("\n")
            .map { it.toInstruction() }
    }

    override fun part1(input: String): String =
        getInstructions(input)
            .fold(getCrates(input)) { crateStacks, (quantity, source, target) ->
                crateStacks.mapIndexed { i, stack ->
                    when (i) {
                        source -> stack.dropLast(quantity)
                        target -> stack + crateStacks[source].takeLast(quantity).reversed()
                        else -> stack
                    }
                }
            }.map { it.last() }.joinToString("")

    override fun part2(input: String): String =
        getInstructions(input)
            .fold(getCrates(input)) { crateStacks, (quantity, source, target) ->
                crateStacks.mapIndexed { i, stack ->
                    when (i) {
                        source -> stack.dropLast(quantity)
                        target -> stack + crateStacks[source].takeLast(quantity)
                        else -> stack
                    }
                }
            }.map { it.last() }.joinToString("")
}