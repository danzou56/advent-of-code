package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23

internal class Day2 : AdventTestRunner23() {
    sealed class Cube {
        data object Blue : Cube()
        data object Red : Cube()
        data object Green : Cube()
    }

    override fun part1(input: String): Any {
        val res = input.split("\n")
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
                            else -> throw RuntimeException()
                        } to num
                    }.toMap()
                }
            }
            .filter { (_, maps: List<Map<Cube, Int>>) ->
                maps.all { map ->
                    ((map[Cube.Red] ?: 0) <= 12
                            &&
                            (map[Cube.Green] ?: 0) <= 13 && (map[Cube.Blue] ?: 0) <= 14)
                }
            }.sumOf { (id, _) -> id.toInt() }
        return res
    }

    override fun part2(input: String): Any {
        val res = input.split("\n")
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
                            else -> throw RuntimeException()
                        } to num
                    }.toMap()
                }
            }
            .map { (_, maps: List<Map<Cube, Int>>) ->
                val reds = maps.maxOf { map -> map[Cube.Red] ?: 0 }
                val blues = maps.maxOf { map -> map[Cube.Blue] ?: 0 }
                val greens = maps.maxOf { map -> map[Cube.Green] ?: 0 }
                reds.toLong() * blues * greens
            }.sum()
        return res
    }
}