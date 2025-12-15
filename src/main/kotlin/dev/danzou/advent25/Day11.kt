package dev.danzou.advent25

import dev.danzou.advent.utils.*
import java.time.Duration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day11 : AdventTestRunner25("Reactor") {

  override val timeout: Duration
    get() = Duration.ofMinutes(30)

  fun parseGraph(input: String): Map<String, Set<String>> =
      input
          .lines()
          .map { it.split(": ") }
          .associate { (label, outs) -> label to (outs.split(" ").toSet()) }

  override fun part1(input: String): Long {
    val graph = parseGraph(input)

    return countPathsBetween("you", "out") { graph[it]!! }
  }

  override fun part2(input: String): Long {
    val graph = parseGraph(input) + mapOf("out" to emptySet())

    val svrDac = countPathsBetween("svr", "dac") { graph[it]!!.filter { it != "fft" } }
    val DacFft = countPathsBetween("dac", "fft") { graph[it]!! }
    val FftOut = countPathsBetween("fft", "out") { graph[it]!!.filter { it != "dac" } }

    val svrFft = countPathsBetween("svr", "fft") { graph[it]!!.filter { it != "dac" } }
    val FftDac = countPathsBetween("fft", "dac") { graph[it]!! }
    val DacOut = countPathsBetween("dac", "out") { graph[it]!!.filter { it != "fft" } }

    return svrDac * DacFft * FftOut + svrFft * FftDac * DacOut
  }

  @Test
  fun testExample() {
    """
    aaa: you hhh
    you: bbb ccc
    bbb: ddd eee
    ccc: ddd eee fff
    ddd: ggg
    eee: out
    fff: out
    ggg: out
    hhh: ccc fff iii
    iii: out
    """
        .trimIndent()
        .let { input -> assertEquals(5, part1(input)) }

    """
    svr: aaa bbb
    aaa: fft
    fft: ccc
    bbb: tty
    tty: ccc
    ccc: ddd eee
    ddd: hub
    hub: fff
    eee: dac
    dac: fff
    fff: ggg hhh
    ggg: out
    hhh: out
    """
        .trimIndent()
        .let { input -> assertEquals(2, part2(input)) }
  }
}
