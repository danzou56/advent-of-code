package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException

internal class Day21 : AdventTestRunner() {

    sealed class Monkey(val name: String) {
        class LeafMonkey(name: String, val data: Long) : Monkey(name) {}
        class InnerMonkey(name: String, val op: (Long, Long) -> Long, val left: String, val right: String) : Monkey(name) {}
    }

    fun getMonkeys(input: String) : List<Monkey> =
        input.split("\n")
            .map { it.split(": ") }
            .map { (name, rest) ->
                try {
                    Monkey.LeafMonkey(name, rest.toLong())
                } catch (e: NumberFormatException) {
                    val (left, textOp, right) = rest.split(" ")
                    val op: (Long, Long) -> Long = when (textOp) {
                        "+" -> Long::plus
                        "-" -> Long::minus
                        "*" -> Long::times
                        "/" -> Long::div
                        else -> throw IllegalArgumentException()
                    }
                    Monkey.InnerMonkey(name, op, left, right)
                }
            }

    override fun part1(input: String): Any {
        val monkeyMap = getMonkeys(input).associateBy { it.name }
        fun run(cur: Monkey): Long {
            return when (cur) {
                is Monkey.LeafMonkey -> cur.data
                is Monkey.InnerMonkey -> cur.op(run(monkeyMap[cur.left]!!), run(monkeyMap[cur.right]!!))
            }
        }

        return run(monkeyMap["root"]!!)
    }

    override fun part2(input: String): Any {
        TODO("Not yet implemented")
    }

    @Test
    fun testExample() {
        val input = """
            root: pppw + sjmn
            dbpl: 5
            cczh: sllz + lgvd
            zczc: 2
            ptdq: humn - dvpt
            dvpt: 3
            lfqf: 4
            humn: 5
            ljgn: 2
            sjmn: drzm * dbpl
            sllz: 4
            pppw: cczh / lfqf
            lgvd: ljgn * ptdq
            drzm: hmdt - zczc
            hmdt: 32
        """.trimIndent()

        assertEquals(152L, part1(input))
    }
}