package dev.danzou.advent23

import dev.danzou.advent.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day19 : AdventTestRunner23("Aplenty") {

    companion object {
        val xmasToIndex = "xmas".toList().map(Char::toString).zip(0..<4).toMap()
        val MIN_PART = 1
        val MAX_PART = 4000
        val ACCEPT = "A"
        val REJECT = "R"
    }

    enum class Operator(val op: (Int, Int) -> Boolean) {
        GT({ a, b -> a > b }) {
            override fun not() = LTE
            override fun makeGate(literal: Int) = literal + 1..MAX_PART
        },
        LT({ a, b -> a < b }) {
            override fun not() = GTE
            override fun makeGate(literal: Int) = MIN_PART..<literal
        },
        GTE({ a, b -> a >= b }) {
            override fun not() = LT
            override fun makeGate(literal: Int) = literal..MAX_PART
        },
        LTE({ a, b -> a <= b }) {
            override fun not() = GT
            override fun makeGate(literal: Int) = MIN_PART..literal
        };

        operator fun invoke(a: Int, b: Int): Boolean = op(a, b)

        abstract operator fun not(): Operator
        abstract fun makeGate(literal: Int): IntRange
    }

    sealed class Rule(val sink: String) {
        class Immediate(sink: String) : Rule(sink)
        class Compare(val operand: String, val operator: Operator, val literal: Int, sink: String) :
            Rule(sink)

        companion object {
            fun workflowsFromString(input: String): Map<String, List<Rule>> {
                return input.split("\n\n").first()
                    .split("\n")
                    .map { it.split('{') }
                    .map { (label, rest) -> label to rest.dropLast(1) }
                    .associate { (label, rules) ->
                        label to rules.split(",").map(Companion::fromString)
                    }
            }

            fun fromString(input: String): Rule {
                val operand = input.takeWhile { it.isLetter() }
                if (operand.length == input.length) return Immediate(operand)
                val operator = input[operand.length]
                val (literal, target) = input.drop(operand.length + 1).split(":")
                return Compare(
                    operand,
                    when (operator) {
                        '>' -> Operator.GT
                        '<' -> Operator.LT
                        else -> throw IllegalArgumentException("Invalid operator $operator")
                    },
                    literal.toInt(),
                    target
                )
            }
        }
    }

    tailrec fun traverse(workflow: List<Rule>, xmas: Map<String, Int>): String =
        when (val rule = workflow.first()) {
            is Rule.Immediate -> rule.sink
            is Rule.Compare -> if (rule.operator(xmas[rule.operand]!!, rule.literal)) rule.sink
            else traverse(workflow.drop(1), xmas)
        }

    fun accept(workflows: Map<String, List<Rule>>, xmas: Map<String, Int>): Boolean {
        tailrec fun run(workflowLabel: String): Boolean {
            val workflow = workflows[workflowLabel]!!
            val next = traverse(workflow, xmas)
            if (next == REJECT) return false
            if (next == ACCEPT) return true
            return run(next)
        }

        return run("in")
    }

    override fun part1(input: String): Int {
        val workflows = Rule.workflowsFromString(input)
        val commands = input.split("\n\n")
            .last()
            .split("\n")
            .map { it.drop(1).dropLast(1) }
            .map { it.split(',', '=') }
            .map { it.windowed(2, step = 2).associate { it.first() to it.last().toInt() } }

        // Quirk: the xmas part numbers are represented differently in part 1 and part 2. Here,
        // they're represented as a map of string to their value. Since rules store the operand as
        // a string, we directly access the part's value with the rule operand.
        return commands.filter { xmas ->
            accept(workflows, xmas)
        }.sumOf { it.values.sum() }
    }

    override fun part2(input: String): Long {
        val workflows = Rule.workflowsFromString(input)

        // Quirk: the xmas part numbers are represented differently in part 1 and part 2. Here,
        // they're represented as list of int ranges. The list is always ordered such that each
        // part's gate is at the same index. Since the rule operand is stored as a string, we use
        // the xmasToIndex map to find the correct index to update.
        //
        // Given a workflow label and input gates, determine the next workflow labels to traverse
        // and their respective gates
        fun successors(label: String, xmasGates: List<IntRange>): Set<Pair<String, List<IntRange>>> {
            if (label == REJECT || label == ACCEPT) return emptySet()

            return workflows[label]!!.fold(
                xmasGates to emptyList<Pair<String, List<IntRange>>>()
            ) { (gateAcc, nexts), rule ->
                when (rule) {
                    // Technically `emptyList()` should be `List(4) { IntRange.EMPTY }`, but
                    // Immediate is always the last in the list and so the value is never used
                    is Rule.Immediate -> emptyList<IntRange>() to nexts + (rule.sink to gateAcc)
                    is Rule.Compare -> gateAcc.mapIndexed { index, xmasGate ->
                        when (index) {
                            // Determine the gate for this sink and the inversion of the gate to be
                            // applied to subsequent rules in this workflow
                            xmasToIndex[rule.operand] -> Pair(
                                xmasGate.intersect(rule.operator.not().makeGate(rule.literal)),
                                xmasGate.intersect(rule.operator.makeGate(rule.literal))
                            )
                            // Don't modify the gate at this index if it isn't the one being
                            // checked by the rule
                            else -> xmasGate to xmasGate
                        }
                    }.unzip().let { (rejects, accepts) ->
                        rejects to nexts + (rule.sink to accepts)
                    }
                }
            }.second.toSet()
        }

        // The possible ways to end up in an accept/reject state along with the input ranges we used
        // to get there can be modeled as a tree. Perform a search on the tree to discover all of
        // its leaf nodes (that is, whether the ranges were accepted or rejected).
        val discovered = bfs("in" to List(4) { MIN_PART..MAX_PART }) { (label, gates) ->
            successors(label, gates)
        }

        // The total count of all terminals is just the total number of all possibilities
        val terminals = discovered.filter { (label, _) -> label == ACCEPT || label == REJECT }
        require(terminals.sumOf { (_, gates) ->
            gates
                .map { it.last - it.first + 1 }
                .map(Int::toLong)
                .reduce(Long::times)
        } == (MAX_PART - MIN_PART + 1).toLong().pow(4))

        return terminals.filter { (label, _) ->
            label == ACCEPT
        }.sumOf { (_, gates) ->
            gates
                .map { it.last - it.first + 1 }
                .map(Int::toLong)
                .reduce(Long::times)
        }
    }

    @Test
    fun testExample() {
        val input = """
            px{a<2006:qkq,m>2090:A,rfg}
            pv{a>1716:R,A}
            lnx{m>1548:A,A}
            rfg{s<537:gd,x>2440:R,A}
            qs{s>3448:A,lnx}
            qkq{x<1416:A,crn}
            crn{x>2662:A,R}
            in{s<1351:px,qqz}
            qqz{s>2770:qs,m<1801:hdj,R}
            gd{a>3333:R,R}
            hdj{m>838:A,pv}

            {x=787,m=2655,a=1222,s=2876}
            {x=1679,m=44,a=2067,s=496}
            {x=2036,m=264,a=79,s=2244}
            {x=2461,m=1339,a=466,s=291}
            {x=2127,m=1623,a=2188,s=1013}
        """.trimIndent()

        assertEquals(19114, part1(input))
        assertEquals(167409079868000L, part2(input))
    }
}