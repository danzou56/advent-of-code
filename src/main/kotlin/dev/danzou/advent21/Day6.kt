package dev.danzou.advent21

internal class Day6: AdventTestRunner21() {
    fun getFish(input: String): Map<Int, Long> =
        input.split(",")
            .map { it.toInt() }.groupingBy { it }
            .eachCount()
            .mapValues { (_, count) -> count.toLong() }

    tailrec fun simulateFish(fish: Map<Int, Long>, days: Int): Map<Int, Long> = when (days) {
        0 -> fish
        else -> simulateFish(ageFish(fish), days - 1)
    }

    fun ageFish(fish: Map<Int, Long>): Map<Int, Long> =
        fish.mapKeys { (age, _) -> ((age - 1) % 9)
            when {
                age > 0 -> age - 1
                else -> 8
            }
        } + mapOf(6 to (fish[7] ?: 0) + (fish[0] ?: 0))

    override fun part1(input: String): Long = simulateFish(getFish(input), 80).values.sum()

    override fun part2(input: String): Long = simulateFish(getFish(input), 256).values.sum()
}