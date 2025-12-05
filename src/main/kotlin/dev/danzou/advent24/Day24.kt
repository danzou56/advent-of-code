package dev.danzou.advent24

import dev.danzou.advent24.Day24.Wire.End
import dev.danzou.advent24.Day24.Wire.Junction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day24 : AdventTestRunner24("Crossed Wires") {

  data class Inputs(val i1: String, val i2: String, val gate: Gate)

  enum class Gate(val f: (Boolean, Boolean) -> Boolean) {
    AND(Boolean::and),
    OR(Boolean::or),
    XOR(Boolean::xor)
  }

  data class Circuit(val parents: Map<String, Wire>, val wires: Map<String, Wire>) {
    companion object {
      fun fromMap(wireMap: Map<String, Inputs>): Circuit {
        val junctions = mutableMapOf<String, Wire>()
        val parents = mutableMapOf<String, Wire>()

        fun build(node: String): Wire {
          return junctions.getOrPut(node) {
            if (node !in wireMap) {
              assert(node.startsWith('x') || node.startsWith('y'))
              return@getOrPut End(node)
            }

            val inputs = wireMap[node]!!
            return@getOrPut Junction(
              node,
              build(inputs.i1),
              build(inputs.i2),
              inputs.gate,
            ).also {
              parents[it.left.name] = it
              parents[it.right.name] = it
            }
          }
        }

        val zs = wireMap.keys.filter { it.startsWith('z') }
        zs.sorted().reversed().onEach { junctionName ->
          build(junctionName)
        }

        return Circuit(junctions, parents)
      }
    }
  }

  sealed interface Wire {
    val name: String
    data class Junction(
        override val name: String,
        val left: Wire,
        val right: Wire,
        val operator: Gate
    ) : Wire

    data class End(override val name: String) : Wire
  }

  fun getInits(input: String): Map<String, Boolean> {
    return input.split("\n\n").first().lines().associate { line ->
      val (input, value) = line.split(": ")
      input to
          when (value) {
            "1" -> true
            "0" -> false
            else -> throw IllegalArgumentException("Invalid input line '$line'")
          }
    }
  }

  fun buildWires(input: String): Map<String, Inputs> {
    return input.split("\n\n").last().lines().associate { line ->
      val (inputs, output) = line.split(" -> ")
      val (i1, gate, i2) = inputs.split(" ")
      output to Inputs(i1, i2, Gate.valueOf(gate.uppercase()))
    }
  }

  override fun part1(input: String): Long {
    val wires = buildWires(input)
    val wireStates = getInits(input).toMutableMap()

    fun getWireState(wire: String): Boolean {
      return wireStates.getOrPut(wire) {
        val (i1, i2, gate) = wires[wire]!!
        gate.f(getWireState(i1), getWireState(i2))
      }
    }

    val zs = wires.keys.filter { it.startsWith('z') }
    return zs.sorted()
        .reversed()
        .map(::getWireState)
        .map { if (it) 1 else 0 }
        .joinToString("")
        .toLong(radix = 2)
  }

  override fun part2(input: String): String {
    val wires = buildWires(input)
    val circuit = Circuit.fromMap(wires)

    wires
        .map { (k, v) -> "  $k [label = \"$k\\n${v.gate}\"]\n  $k -> {${v.i1} ${v.i2}};" }
        .joinToString("\n")
        .let { "strict digraph {\n$it\n}" }
        .also { println(it) }

    // sorted z00..z99
    // z00 is LSB
    // z33 is MSB
    val zs = wires.keys.filter { it.startsWith('z') }.sorted()

    val dependencies = getInits(input).mapValues { emptySet<String>() }.toMutableMap()
    fun getWireDependencies(wire: String): Set<String> {
      return dependencies.getOrPut(wire) {
        val (i1, i2, _) = wires[wire]!!
        setOf(i1, i2) + getWireDependencies(i1) + getWireDependencies(i2)
      }
    }

    wires.keys.onEach(::getWireDependencies)

    val broken =
        zs.filter { z ->
          val bitPosition = z.drop(1).toInt()
          val required =
              (0..bitPosition)
                  .map { i -> i.toString().padStart(2, '0') }
                  .flatMap { i -> listOf("x$i", "y$i") }
          !dependencies[z]!!.containsAll(required)
        }

    //    val forwards =
    //    val badForwards = wires.filter {
    //
    //    }

    return ""
  }

  @Test
  fun testExample() {
    """
      x00: 1
      x01: 0
      x02: 1
      x03: 1
      x04: 0
      y00: 1
      y01: 1
      y02: 1
      y03: 1
      y04: 1
      
      ntg XOR fgs -> mjb
      y02 OR x01 -> tnw
      kwq OR kpj -> z05
      x00 OR x03 -> fst
      tgd XOR rvg -> z01
      vdt OR tnw -> bfw
      bfw AND frj -> z10
      ffh OR nrd -> bqk
      y00 AND y03 -> djm
      y03 OR y00 -> psh
      bqk OR frj -> z08
      tnw OR fst -> frj
      gnj AND tgd -> z11
      bfw XOR mjb -> z00
      x03 OR x00 -> vdt
      gnj AND wpb -> z02
      x04 AND y00 -> kjc
      djm OR pbm -> qhw
      nrd AND vdt -> hwm
      kjc AND fst -> rvg
      y04 OR y02 -> fgs
      y01 AND x02 -> pbm
      ntg OR kjc -> kwq
      psh XOR fgs -> tgd
      qhw XOR tgd -> z09
      pbm OR djm -> kpj
      x03 XOR y03 -> ffh
      x00 XOR y04 -> ntg
      bfw OR bqk -> z06
      nrd XOR fgs -> wpb
      frj XOR qhw -> z04
      bqk OR frj -> z07
      y03 OR x01 -> nrd
      hwm AND bqk -> z03
      tgd XOR rvg -> z12
      tnw OR pbm -> gnj
    """
        .trimIndent()
        .let { input ->
          assertEquals(2024, part1(input))
          // assertEquals(null, part2(input))
        }
  }
}

// private fun String.not() {
//  TODO("Not yet implemented")
// }
