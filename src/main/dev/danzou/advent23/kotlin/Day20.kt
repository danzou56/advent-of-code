package dev.danzou.advent23.kotlin

import dev.danzou.advent.utils.lcm
import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class Day20 : AdventTestRunner23() {

    enum class Pulse {
        HIGH { override fun not() = LOW },
        LOW { override fun not() = HIGH };

        abstract operator fun not(): Pulse
    }

    sealed class Module(val name: String, val targets: List<String>) {
        class Broadcaster(targets: List<String>) : Module("broadcaster", targets) {
            override fun pulse(pulse: Pulse, from: String): Pair<Pulse, List<String>> =
                pulse to targets
        }

        class FlipFlop(name: String, targets: List<String>) : Module(name, targets) {
            var state = Pulse.LOW
            override fun pulse(pulse: Pulse, from: String): Pair<Pulse, List<String>> =
                when (pulse) {
                    Pulse.HIGH -> pulse to emptyList()
                    Pulse.LOW -> {
                        state = !state
                        state to targets
                    }
                }
        }

        class Conjunction(name: String, inputs: List<String>, targets: List<String>) : Module(name, targets) {
            var lasts = inputs.associateWith { Pulse.LOW }.toMutableMap()
            override fun pulse(pulse: Pulse, from: String): Pair<Pulse, List<String>> {
                lasts[from] = pulse
                return if (lasts.values.all { it == Pulse.HIGH }) Pulse.LOW to targets
                else Pulse.HIGH to targets
            }
        }

        abstract fun pulse(pulse: Pulse, from: String): Pair<Pulse, List<String>>

        companion object {
            fun fromString(input: String): Map<String, Module> {
                val conjunctions = input.split("\n")
                    .filter { it.startsWith("&") }
                    .map { it.drop(1).takeWhile { it.isLetter() } }
                    .toSet()

                val conjunctionInputs = mutableMapOf<String, List<String>>()
                input.split("\n")
                    .map { it.split(" -> ") }
                    .map { (source, targets) ->
                        targets.split(", ")
                            .onEach { target ->
                                if (target in conjunctions)
                                    conjunctionInputs[target] = (conjunctionInputs[target]
                                        ?: emptyList()) + source.dropWhile { it == '&' || it == '%' }
                            }
                    }

                val modules: Map<String, Module> = input.split("\n")
                    .map { it.split(" -> ") }
                    .map { (source, targets) -> source to targets.split(", ") }
                    .map { (source, targets) ->
                        val name = source.drop(1)
                        when (source.first()) {
                            '%' -> FlipFlop(name, targets)
                            '&' -> Conjunction(name, conjunctionInputs[name]!!, targets)
                            else -> Broadcaster(targets)
                        }
                    }
                    .associateBy { it.name }

                return modules
            }
        }
    }

    override fun part1(input: String): Long {
        val modules = Module.fromString(input)

        var lows = 0L
        var highs = 0L
        val queue: Queue<Triple<Pulse, String, String>> = LinkedList()
        val times = 1000
        var i = 0
        while (i < times) {
            queue.add(Triple(Pulse.LOW, "broadcaster", "broadcaster"))
            while (queue.isNotEmpty()) {
                val (pulse, source, from) = queue.poll()!!
                when (pulse) {
                    Pulse.LOW -> lows++
                    Pulse.HIGH -> highs++
                }
                val (nextPulse, targets) = modules[source]?.pulse(pulse, from) ?: continue
                queue.addAll(
                    targets.map { target -> Triple(nextPulse, target, source) }
                )
            }
            i++
        }

        return lows * highs
    }

    override fun part2(input: String): Long {
        val modules = Module.fromString(input)

        // Under some assumptions about the overall structure of the graph created by the modules,
        // * rx is preceded by exactly one module
        // * said predecessor module is a conjunction
        // * the predecessor module is preceded by at least one conjunction
        // * those modules preceding the predecessor module are also conjunctions, are only
        //   preceded by one module, and only target the predecessor module
        // establish a mutable map that will contain the first index at which each of
        // pre-predecessors send a high pulse to the predecessor module
        require(modules.entries.count { (_, module) -> "rx" in module.targets } == 1)
        val rxPrevEntry = modules.entries.single { (_, module) -> "rx" in module.targets }
        val rxPrev = rxPrevEntry.key
        require(rxPrevEntry.value is Module.Conjunction)
        val rxPrevModule = (rxPrevEntry.value as Module.Conjunction)
        require(
            rxPrevModule.lasts.keys.map { modules[it]!! }.all {
                it is Module.Conjunction && it.lasts.size == 1 && it.targets == listOf(rxPrev)
            }
        )
        val rxPrevLastIndices = mutableMapOf<String, Int>()

        val queue: Queue<Triple<Pulse, String, String>> = LinkedList()
        var i = 0
        while (rxPrevLastIndices.size < rxPrevModule.lasts.size) {
            queue.add(Triple(Pulse.LOW, "broadcaster", "broadcaster"))
            i++
            while (queue.isNotEmpty()) {
                val (pulse, source, from) = queue.poll()!!
                if (source == rxPrev && rxPrevModule.lasts.values.any { it == Pulse.HIGH }) {
                    rxPrevModule.lasts.entries.single { (_, last) -> last == Pulse.HIGH }.let { (label, _) ->
                        rxPrevLastIndices.putIfAbsent(label, i)
                    }
                }
                val (nextPulse, targets) = modules[source]?.pulse(pulse, from) ?: continue
                queue.addAll(
                    targets.map { target -> Triple(nextPulse, target, source) }
                )
            }
        }

        // The first index at which the predecessor module will send a LOW pulse to rx is when all
        // of the pre-predecessors have sent a HIGH to it. Return the lcm of the first indices at
        // which the predecessor received a HIGH from each of the pre-predecessors.
        return rxPrevLastIndices.values.map(Int::toLong).reduce(::lcm)
    }

    @Test
    fun testExample() {
        val input = """
            broadcaster -> a, b, c
            %a -> b
            %b -> c
            %c -> inv
            &inv -> a
        """.trimIndent()

        assertEquals(32000000L, part1(input))
//        assertEquals(null, part2(input))
    }
}