package dev.danzou.advent22.kotlin

import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

internal class Day19 : AdventTestRunner22() {

    sealed class Material private constructor() {
        object Ore : Material()
        object Clay : Material()
        object Obsidian : Material()
        object Geode : Material()
        override fun toString(): String {
            return this.javaClass.simpleName
        }
    }

    sealed class Robot private constructor(
        val produces: Material,
        val ore: Int = 0,
        val clay: Int = 0,
        val obsidian: Int = 0
    ) {
        class OreRobot(ore: Int) : Robot(Material.Ore, ore = ore)
        class ClayRobot(ore: Int) : Robot(Material.Clay, ore = ore)
        class ObsidianRobot(ore: Int, clay: Int) : Robot(Material.Obsidian, ore = ore, clay = clay)
        class GeodeRobot(ore: Int, obsidian: Int) : Robot(Material.Geode, ore = ore, obsidian = obsidian)

        override fun equals(other: Any?): Boolean =
            when (other) {
                is Robot -> this.produces == other.produces
                        && this.ore == other.ore
                        && this.clay == other.clay
                        && this.obsidian == other.obsidian
                else -> false
            }

        override fun hashCode(): Int {
            var result = produces.hashCode()
            result = 31 * result + ore
            result = 31 * result + clay
            result = 31 * result + obsidian
            return result
        }
    }

    data class Blueprint(
        val oreRobot: Robot.OreRobot,
        val clayRobot: Robot.ClayRobot,
        val obsidianRobot: Robot.ObsidianRobot,
        val geodeRobot: Robot.GeodeRobot
    ) {

        val combinations: Set<Set<Robot>> = run {
            fun <T> generateCombinations(elements: Collection<T>, current: Set<T>, num: Int): Set<Set<T>> {
                require(num <= elements.size)
                if (elements.isEmpty() || num == 0) return setOf(current)
                return elements.flatMap { generateCombinations(elements - it, current + it, num - 1) }.toSet()
            }

            val robots = listOf(oreRobot, clayRobot, obsidianRobot, geodeRobot)
            (0..1).flatMap { generateCombinations(robots, emptySet(), it) }.toSet()
        }

        companion object {
            fun fromString(line: String): Blueprint {
                val matches = Regex("""\d+""").findAll(line).map { it.value.toInt() }.toList()
                return Blueprint(
                    Robot.OreRobot(matches[1]),
                    Robot.ClayRobot(matches[2]),
                    Robot.ObsidianRobot(matches[3], matches[4]),
                    Robot.GeodeRobot(matches[5], matches[6])
                )
            }
        }
    }

    data class Factory(
        private val blueprint: Blueprint,
        private var ore: Int = 0,
        private var oreRobots: Int = 1,
        private var clay: Int = 0,
        private var clayRobots: Int = 0,
        private var obsidian: Int = 0,
        private var obsidianRobots: Int = 0,
        private var _geodes: Int = 0,
        private var geodeRobots: Int = 0,
    ) {
        val geodes: Int
            get() = _geodes
        private val oreThreshold = 8 // double the max,,,

        fun collect(): Factory = copy(
            ore = ore + oreRobots,
            clay = clay + clayRobots,
            obsidian = obsidian + obsidianRobots,
            _geodes = _geodes + geodeRobots
        )

        fun getNextFactories(): Set<Factory> =
            blueprint.combinations
                .filter { this.canCollect(it) }
                .map {
                        this.collect().run { this.copy(
                            ore = this.ore - it.sumOf { it.ore },
                            oreRobots = this.oreRobots + it.contains(blueprint.oreRobot).compareTo(false),
                            clay = this.clay - it.sumOf { it.clay },
                            clayRobots = this.clayRobots + it.contains(blueprint.clayRobot).compareTo(false),
                            obsidian = this.obsidian - it.sumOf { it.obsidian },
                            obsidianRobots = this.obsidianRobots + it.contains(blueprint.obsidianRobot).compareTo(false),
                            geodeRobots = this.geodeRobots + it.contains(blueprint.geodeRobot).compareTo(false),
                        ) }
                    }.toSet()

        fun canCollect(robots: Set<Robot>): Boolean =
            robots.fold(Triple(0, 0, 0)) { (ore, clay, obsidian), robot ->
                Triple(ore + robot.ore, clay + robot.clay, obsidian + robot.obsidian)
            }.let { (ore, clay, obsidian) -> this.ore >= ore && this.clay >= clay && this.obsidian >= obsidian }

        override fun toString(): String =
            "Factory(ore=$ore, clay=$clay, obsidian=$obsidian, geode=$_geodes)"

        companion object {
            fun runForMinutes(factory: Factory, time: Int): Set<Factory> =
                (0 until time).fold(setOf(factory)) { factories, i ->
//                    print("${i + 1}: ")
//                    println(factories.size)
                    factories
                        .flatMap { it.getNextFactories() }
                        .let { next ->
                            val max = next.maxOf { it.geodes }
                            next.filter { it.geodes == max }
                        }.toSet().also {
//                            println(it)
                        }
                }
        }
    }

    override fun part1(input: String): Any {
        val factories = input.split("\n").map { Factory(Blueprint.fromString(it)) }
        val qualities = factories.mapIndexed { i, factory ->
            println(i)
            (i + 1) * Factory.runForMinutes(factory, 24).maxOf { it.geodes }
        }
        return qualities.sum()
    }

    override fun part2(input: String): Any {
        val factories = input.split("\n").take(3).map { Factory(Blueprint.fromString(it)) }
        val qualities = factories.mapIndexed { i, factory ->
            println(i)
            Factory.runForMinutes(factory, 32).maxOf { it.geodes }
        }
        return qualities.reduce(Int::times)
    }

    @Test
    fun testBlueprint() {
        assertEquals(
            Blueprint(
                Robot.OreRobot(2),
                Robot.ClayRobot(4),
                Robot.ObsidianRobot(4, 20),
                Robot.GeodeRobot(3, 14)
            ),
            Blueprint.fromString("Blueprint 1: Each ore robot costs 2 ore. Each clay robot costs 4 ore. Each obsidian robot costs 4 ore and 20 clay. Each geode robot costs 3 ore and 14 obsidian.\n")
        )
    }

    @Test
    fun testExample() {
        val input = """
            Blueprint 1:
              Each ore robot costs 4 ore.
              Each clay robot costs 2 ore.
              Each obsidian robot costs 3 ore and 14 clay.
              Each geode robot costs 2 ore and 7 obsidian.

            Blueprint 2:
              Each ore robot costs 2 ore.
              Each clay robot costs 3 ore.
              Each obsidian robot costs 3 ore and 8 clay.
              Each geode robot costs 3 ore and 12 obsidian.
        """.trimIndent()
            .split("\n\n")
            .map { it.split("\n").joinToString("") }
            .joinToString("\n")
        val blueprints = input.split("\n").map { Blueprint.fromString(it) }

        val factory = Factory(blueprints[0])

        assertContains(
            Factory.runForMinutes(factory, 5),
            factory.copy(
                ore = 1,
                oreRobots = 1,
                clay = 2,
                clayRobots = 2
            )
        )

        assertContains(
            Factory.runForMinutes(factory, 10),
            factory.copy(
                ore = 4,
                oreRobots = 1,
                clay = 15,
                clayRobots = 3
            )
        )

        assertEquals(9, Factory.runForMinutes(Factory(blueprints[0]), 24).maxOf { it.geodes })
        assertEquals(12, Factory.runForMinutes(Factory(blueprints[1]), 24).maxOf { it.geodes })

        assertEquals(33, part1(input))

        assertEquals(56, Factory.runForMinutes(Factory(blueprints[0]), 32).maxOf { it.geodes })
        assertEquals(62, Factory.runForMinutes(Factory(blueprints[1]), 32).maxOf { it.geodes })
    }
}