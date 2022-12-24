package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException

internal class Day21 : AdventTestRunner() {
    val ROOT = "root"
    val HUMN = "humn"

    sealed class Monkey(val name: String) {
        class LeafMonkey(name: String, val data: Long) : Monkey(name) {}
        class InnerMonkey(name: String, val op: Op, val left: String, val right: String) : Monkey(name) {}
    }

    enum class Op(val op: (Long, Long) -> Long) {
        PLUS(Long::plus),
        MINUS(Long::minus),
        TIMES(Long::times),
        DIV(Long::div);

        override fun toString(): String {
            return this.name
        }
    }

    fun getMonkeys(input: String) : List<Monkey> =
        input.split("\n")
            .map { it.split(": ") }
            .map { (name, rest) ->
                try {
                    Monkey.LeafMonkey(name, rest.toLong())
                } catch (e: NumberFormatException) {
                    val (left, textOp, right) = rest.split(" ")
                    val op = when (textOp) {
                        "+" -> Op.PLUS
                        "-" -> Op.MINUS
                        "*" -> Op.TIMES
                        "/" -> Op.DIV
                        else -> throw IllegalArgumentException()
                    }
                    Monkey.InnerMonkey(name, op, left, right)
                }
            }

    fun calculate(init: String, monkeyMap: Map<String, Monkey>): Long {
        fun run(cur: String): Long {
            val monkey = monkeyMap[cur]!!
            return when (monkey) {
                is Monkey.LeafMonkey -> monkey.data
                is Monkey.InnerMonkey -> monkey.op.op(run(monkey.left), run(monkey.right))
            }
        }

        return run(init)
    }

    override fun part1(input: String): Any {
        val monkeyMap = getMonkeys(input).associateBy { it.name }
        return calculate(ROOT, monkeyMap)
    }

    override fun part2(input: String): Any {
        val monkeys = getMonkeys(input)
        val monkeyMap = monkeys.associateBy { it.name }
        val parents = monkeys.filter { it is Monkey.InnerMonkey }
            .map { it as Monkey.InnerMonkey }
            .flatMap { sequenceOf(it.left to it.name, it.right to it.name) }
            .toMap()

        fun runInverted(prevNode: Monkey, cur: Monkey.InnerMonkey): Long {
            val prevOnLeft = prevNode == monkeyMap[cur.left]!!
            val otherChildRun =
                if (prevOnLeft) calculate(cur.right, monkeyMap)
                else calculate(cur.left, monkeyMap)

            if (cur.name == ROOT) return otherChildRun
            val parentValue = runInverted(cur, monkeyMap[parents[cur.name]!!]!! as Monkey.InnerMonkey)

            return when (cur.op) {
                Op.PLUS ->
                    parentValue - otherChildRun
                Op.MINUS ->
                    if (prevOnLeft) parentValue + otherChildRun
                    else otherChildRun - parentValue
                Op.TIMES ->
                    parentValue / otherChildRun
                Op.DIV ->
                    if (prevOnLeft) parentValue * otherChildRun
                    else otherChildRun / parentValue
            }
        }

        return runInverted(monkeyMap[HUMN]!!, monkeyMap[parents[HUMN]!!]!! as Monkey.InnerMonkey)
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
        assertEquals(301L, part2(input))
    }
}