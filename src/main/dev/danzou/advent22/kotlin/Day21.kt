package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException

internal class Day21 : AdventTestRunner() {

    val HUMN = "humn"

    sealed class Monkey(val name: String) {
        class LeafMonkey(name: String, val data: Long) : Monkey(name) {}
        class InnerMonkey(name: String, val op: Op, val left: String, val right: String) : Monkey(name) {}
    }

    sealed class MonkeyNode(val name: String, val parent: String?) {
        class LeafMonkey(name: String, val data: Long, parent: String?) : MonkeyNode(name, parent) {
            override fun toString(): String {
                return "Leaf($name, $data, ${parent})"
            }
        }
        class InnerMonkey(name: String, val op: Op, val left: String, val right: String, parent: String?) : MonkeyNode(name, parent) {
            override fun toString(): String {
                return "Inner($name, $op, $left, $right, $parent)"
            }
        }
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

    override fun part1(input: String): Any {
        val monkeyMap = getMonkeys(input).associateBy { it.name }
        fun run(cur: Monkey): Long {
            return when (cur) {
                is Monkey.LeafMonkey -> cur.data
                is Monkey.InnerMonkey -> cur.op.op(run(monkeyMap[cur.left]!!), run(monkeyMap[cur.right]!!))
            }
        }

        return run(monkeyMap["root"]!!)
    }

    override fun part2(input: String): Any {
        val monkeys = getMonkeys(input)
        val monkeyMap = monkeys.associateBy { it.name }
        val monkeyNodeMap = mutableMapOf<String, MonkeyNode>()

        fun buildGraph(cur: Monkey, parent: String?): MonkeyNode {
            val node = when (cur) {
                is Monkey.LeafMonkey ->
                    MonkeyNode.LeafMonkey(cur.name, cur.data, parent)
                is Monkey.InnerMonkey -> {
                    val node = MonkeyNode.InnerMonkey(cur.name, cur.op, cur.left, cur.right, parent)
                    buildGraph(monkeyMap[cur.left]!!, cur.name)
                    buildGraph(monkeyMap[cur.right]!!, cur.name)
                    node
                }
            }
            monkeyNodeMap[node.name] = node
            return node
        }

        val root = buildGraph(monkeyMap["root"]!!, null) as MonkeyNode.InnerMonkey
        val humn = monkeyNodeMap[HUMN]!! as MonkeyNode.LeafMonkey

        fun findHumn(cur: MonkeyNode): Boolean {
            if (cur.name == HUMN) return true
            return when (cur) {
                is MonkeyNode.LeafMonkey -> false
                is MonkeyNode.InnerMonkey -> findHumn(monkeyNodeMap[cur.left]!!) || findHumn(monkeyNodeMap[cur.right]!!)
            }
        }

        val humnSide = if (findHumn(monkeyNodeMap[root.left]!!)) monkeyNodeMap[root.left]!! else monkeyNodeMap[root.right]!!
        val otherSide = if (humnSide == monkeyNodeMap[root.right]!!) monkeyNodeMap[root.left]!! else monkeyNodeMap[root.right]!!

        fun run(cur: MonkeyNode): Long {
            return when (cur) {
                is MonkeyNode.LeafMonkey -> cur.data
                is MonkeyNode.InnerMonkey -> cur.op.op(run(monkeyNodeMap[cur.left]!!), run(monkeyNodeMap[cur.right]!!))
            }
        }

        val otherSideVal = run(otherSide)

        fun runInverted(prevNode: MonkeyNode, cur: MonkeyNode.InnerMonkey): Long {
            if (cur.name == "root") return run(otherSide)
            val prevOnLeft = prevNode == monkeyNodeMap[cur.left]!!
            val parentValue = runInverted(cur, monkeyNodeMap[cur.parent]!! as MonkeyNode.InnerMonkey)
            val otherChildRun =
                if (prevOnLeft) run(monkeyNodeMap[cur.right]!!)
                else run(monkeyNodeMap[cur.left]!!)

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

        val ans = runInverted(humn, monkeyNodeMap[humn.parent]!! as MonkeyNode.InnerMonkey)

        tailrec fun rebuild(prevName: String, curName: String) {
            val node = monkeyNodeMap[curName]!! as MonkeyNode.InnerMonkey
            val prevOnLeft = prevName == node.left
            val otherChild = if (prevOnLeft) node.right else node.left

            if (node.parent == null) {
                monkeyNodeMap[node.name] = MonkeyNode.LeafMonkey(
                    node.name,
                    otherSideVal,
                    prevName
                )
                return
            }

            val (op, left, right) = when (node.op) {
                Op.PLUS ->
                    Triple(Op.MINUS, node.parent, otherChild)
                Op.MINUS ->
                    if (prevOnLeft) Triple(Op.PLUS, node.parent, otherChild)
                    else Triple(Op.MINUS, otherChild, node.parent)
                Op.TIMES ->
                    Triple(Op.DIV, node.parent, otherChild)
                Op.DIV ->
                    if (prevOnLeft) Triple(Op.TIMES, node.parent, otherChild)
                    else Triple(Op.DIV, otherChild, node.parent)
            }

            monkeyNodeMap[node.name] = MonkeyNode.InnerMonkey(
                node.name,
                op,
                left,
                right,
                if (prevName == HUMN) null else prevName
            )
            rebuild(node.name, node.parent)
        }


        rebuild(HUMN, humn.parent!!)
        return run(monkeyNodeMap[humn.parent]!!)
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