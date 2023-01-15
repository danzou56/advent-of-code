package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.geometry.toPair
import dev.danzou.advent.utils.update
import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day11 : AdventTestRunner22() {

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
        val worry: Long
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

    fun play(monkeys: List<Monkey>, rounds: Int): List<Long> =
        (1..rounds).fold(Pair(monkeys, List(monkeys.size) { 0L })) { (monkeys: List<Monkey>, count: List<Long>), _ ->
            // Essentially, we need to fold over the monkeys while also being
            // able to modify the underlying list (which mapIndexed normally
            // wouldn't let us do). Instead, update the entire list at the end
            // and pass that on to the next iteration of fold. Now this truly
            // is a loop with extra steps and no more elegant either.
            monkeys.indices.fold(Pair(monkeys, count)) { (monkeys, count), srcI -> monkeys[srcI].let { monkey ->
                // Go through all the items the monkey has and update the
                // monkeys list accordingly
                monkey.items.fold(Pair(monkeys, count)) { (monkeys, count), item ->
                    val worry = monkey.operation(item.worry) / 3L
                    val destI = monkey.test(worry % monkey.divisor == 0L)
                    Pair(
                        monkeys.mapIndexed { curI, monkey -> when (curI) {
                            destI -> monkey.withItems(monkey.items + Item(worry))
                            srcI -> monkey.withItems(monkey.items.drop(1))
                            else -> monkey
                        } },
                        count.update(srcI, count[srcI] + 1)
                    )
                } }
            }
        }.second

    override fun part1(input: String): Any =
        play(parse(input), 20).sorted().takeLast(2).reduce(Long::times)

    override fun part2(input: String): Any =
        play(
            parse(input).let { monkeys -> monkeys.map { Monkey(
                it.items,
                { l -> it.operation(l) * 3L % (monkeys.map { it.divisor } + 3L).reduce(Long::times) },
                it.divisor,
                it.test
            ) } },
            10_000
        ).sorted().takeLast(2).reduce(Long::times)

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