package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import dev.danzou.advent.utils.toPair
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day11 : AdventTestRunner() {

    data class Monkey(
        val items: List<Item>,
        val operation: (Long) -> Long,
        val divisor: Long,
        val test: (Boolean) -> Int,
    ) {
        fun withItems(items: List<Item>) : Monkey =
            Monkey(items, operation, divisor, test)

        override fun toString(): String {
            return "Monkey(items=$items)"
        }
    }

    data class Item(
        val worryLevel: Long
    )

    fun parse(input: String): List<Monkey> =
        input.split("\n\n").mapIndexed { i, it -> it.split("\n").let { data ->
            Monkey(
                data[1].drop("  Starting items: ".length).split(", ").map { Item(it.toLong()) },
                data[2].drop("  Operation: new = old ".length).split(" ").toPair().let { (operator, operand) ->
                    // The previous solution's better and clearer, but I
                    // couldn't resist currying
                    val opMap = mapOf<String, (Long) -> ((Long) -> Long)>(
                        "+" to { x -> { y -> x + y } },
                        "*" to { x -> { y -> x * y } }
                    )
                    when (operand) {
                        "old" -> { old -> opMap[operator]!!(old)(old) }
                        else -> opMap[operator]!!(operand.toLong())
                    }
                },
                data[3].drop("  Test: divisible by ".length).toLong(),
                Pair(
                    data[4].drop("    If true: throw to monkey ".length).toInt(),
                    data[5].drop("    If false: throw to monkey ".length).toInt()
                ).let { (trueTarget, falseTarget) -> { isDivisible ->
                        if (isDivisible) trueTarget
                        else falseTarget
                } }
            )
        } }

    override fun part1(input: String): Any {
        val rounds = 20
        val monkeys = parse(input)
        val counters = List(10) { 0L }.toMutableList()
        (1..rounds).fold(monkeys) { monkeys, _ ->
            // Essentially, we need to fold over the monkeys while also being
            // able to modify the underlying list (which mapIndexed normally
            // wouldn't let us do). Instead, update the entire list at the end
            // and pass that on to the next iteration of fold. Now this truly
            // is a loop with extra steps and no more elegant either.
            monkeys.indices.fold(monkeys) { monkeys, i -> monkeys[i].let { monkey ->
                // Go through all the items the monkey has and update the
                // monkeys list accordingly
                monkey.items.fold(monkeys) { monkeys, item ->
                    counters[i] += 1L
                    val worryLevel = monkey.operation(item.worryLevel) / 3L
                    val targetMonkey = monkey.test(worryLevel % monkey.divisor == 0L)
                    monkeys.mapIndexed { curI, monkey -> when (curI) {
                        targetMonkey -> monkey.withItems(monkey.items + Item(worryLevel))
                        i -> monkey.withItems(monkey.items.drop(1))
                        else -> monkey
                    } }
                } }
            }
        }
        return counters.sorted().takeLast(2).reduce(Long::times)
    }

    override fun part2(input: String): Any {
        val rounds = 10000
        val monkeys = parse(input)
        val modOperand = monkeys.map { it.divisor }.reduce(Long::times)
        val counters = List(10) { 0L }.toMutableList()
        (1..rounds).fold(monkeys) { monkeys, _ ->
            monkeys.indices.fold(monkeys) { monkeys, i -> monkeys[i].let { monkey ->
                monkey.items.fold(monkeys) { monkeys, item ->
                    counters[i] += 1L
                    // This is the only difference between part 1 and 2:
                    val worryLevel = monkey.operation(item.worryLevel) % modOperand
                    val targetMonkey = monkey.test(worryLevel % monkey.divisor == 0L)
                    monkeys.mapIndexed { curI, monkey -> when (curI) {
                        targetMonkey -> monkey.withItems(monkey.items + Item(worryLevel))
                        i -> monkey.withItems(monkey.items.drop(1))
                        else -> monkey
                    } }
                } }
            }
        }
        return counters.sorted().takeLast(2).reduce(Long::times)
    }

    @Test
    fun testExample() {
        val input = """
            Monkey 0:
              Starting items: 79, 98
              Operation: new = old * 19
              Test: divisible by 23
                If true: throw to monkey 2
                If false: throw to monkey 3
            
            Monkey 1:
              Starting items: 54, 65, 75, 74
              Operation: new = old + 6
              Test: divisible by 19
                If true: throw to monkey 2
                If false: throw to monkey 0
            
            Monkey 2:
              Starting items: 79, 60, 97
              Operation: new = old * old
              Test: divisible by 13
                If true: throw to monkey 1
                If false: throw to monkey 3
            
            Monkey 3:
              Starting items: 74
              Operation: new = old + 3
              Test: divisible by 17
                If true: throw to monkey 0
                If false: throw to monkey 1
        """.trimIndent()

        assertEquals(10605L, part1(input))
        assertEquals(2713310158L, part2(input))
    }
}