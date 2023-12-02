package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23

internal class Day2 : AdventTestRunner23("Cube Conundrum") {
    enum class Cube { Blue, Red, Green }

    fun getGames(input: String): Map<Int, List<Map<Cube, Int>>> =
        input.split("\n")
            .map { it.drop("Game ".length) }
            .map { it.split(": ") }
            .associate { (id, rolls) ->
                id.toInt() to rolls.split("; ").map { roll ->
                    roll.split(", ").associate {
                        val (num, color) = it.split(" ")
                        when (color) {
                            "blue" -> Cube.Blue
                            "red" -> Cube.Red
                            "green" -> Cube.Green
                            else -> throw IllegalArgumentException("Invalid color")
                        } to num.toInt()
                    }
                }
            }

    override fun part1(input: String): Int {
        val maxCubes = mapOf(
            Cube.Red to 12,
            Cube.Green to 13,
            Cube.Blue to 14
        )
        return getGames(input)
            .filter { (_, maps: List<Map<Cube, Int>>) ->
                maps.all { map -> maxCubes.all { (color, max) -> (map[color] ?: 0) <= max } }
            }.keys.sum()
    }

    override fun part2(input: String): Int =
        getGames(input).entries.sumOf { (_, rolls: List<Map<Cube, Int>>) ->
            Cube.entries
                .map { color -> rolls.maxOf { roll -> roll[color] ?: 0 } }
                .reduce(Int::times)
        }

}