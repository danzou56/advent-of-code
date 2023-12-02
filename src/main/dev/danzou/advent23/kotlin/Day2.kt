package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23

internal class Day2 : AdventTestRunner23() {
    enum class Cube {
        Blue, Red, Green
    }

    fun getGames(input: String): List<Pair<String, List<Map<Cube, Int>>>> =
        input.split("\n")
            .map { it.replace(" ", "") }
            .map { it.drop("Game ".length - 1) }
            .map {
                Pair(it.takeWhile { it.isDigit() }, it.split(":").drop(1).single())
            }
            .map { (id, rest) ->
                id to rest.split(";").map {
                    it.split(",").map {
                        val color = it.dropWhile { it.isDigit() }
                        val num = it.dropLast(color.length).toInt()
                        when (color) {
                            "blue" -> Cube.Blue
                            "red" -> Cube.Red
                            "green" -> Cube.Green
                            else -> throw IllegalArgumentException("Invalid color")
                        } to num
                    }.toMap()
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
            }.sumOf { (id, _) -> id.toInt() }
    }

    override fun part2(input: String): Int =
        getGames(input).sumOf { (_, maps: List<Map<Cube, Int>>) ->
            Cube.entries.map { maps.maxOf { map -> map[it] ?: 0 } }.reduce(Int::times)
        }

}