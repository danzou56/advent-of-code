package dev.danzou.advent20

import dev.danzou.advent.utils.geometry.toPair
import kotlin.math.min
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day19 : AdventTestRunner20("") {

  private fun interface Node {
    fun re(): String

    class Concat(val nodes: List<Node>) : Node {
      override fun re() = nodes.joinToString("", transform = Node::re)
    }

    class Union(val left: Node, val right: Node) : Node {
      override fun re() = "(${left.re()}|${right.re()})"
    }

    class Constant(val value: Char) : Node {
      override fun re(): String = "$value"
    }
  }

  private fun parseRules(
      rules: List<String>,
      rootName: String,
      seedNodes: Map<String, Node> = emptyMap(),
  ): Map<String, Node> {
    val ruleStrings = rules.associate { it.split(": ").toPair() }
    val ruleDependencies =
        ruleStrings.mapValues { (_, v) ->
          Regex("\\d+").findAll(v).map { it.value }.toSet()
        }

    val nodes =
        seedNodes.toMutableMap().apply {
          // Add leaves to seed map
          putAll(
              ruleDependencies
                  .filter { (_, dependencies) -> dependencies.isEmpty() }
                  .mapValues { (name, _) ->
                    Node.Constant(ruleStrings[name]!!.trim('"').single())
                  }
          )
        }

    fun makeNode(name: String): Node {
      if (nodes.containsKey(name)) return nodes[name]!!
      ruleDependencies[name]!!.forEach { nodeName ->
        if (nodeName !in nodes) nodes[nodeName] = makeNode(nodeName)
      }

      val ruleString = ruleStrings[name]!!
      return if (ruleString.contains("|")) {
        val (left, right) = ruleString.split(" | ")
        Node.Union(
            Node.Concat(left.split(" ").map { nodes[it]!! }),
            Node.Concat(right.split(" ").map { nodes[it]!! }),
        )
      } else {
        Node.Concat(ruleString.split(" ").map { nodes[it]!! })
      }
    }

    nodes[rootName] = makeNode(rootName)
    return nodes
  }

  override fun part1(input: String): Int {
    val (rules, messages) = input.split("\n\n").map(String::lines)

    val rootName = "0"
    val map = parseRules(rules, rootName)

    val rootRegex = Regex("^${map[rootName]!!.re()}$")
    return messages.count { rootRegex.matches(it) }
  }

  override fun part2(input: String): Int {
    val (rules, messages) = input.split("\n\n").map(String::lines)

    val seedNodes =
        buildMap<String, Node> {
          putAll(parseRules(rules, "42"))
          val rule42 = get("42")!!
          putAll(parseRules(rules, "31"))
          val rule31 = get("31")!!


          /*
          Regex is less expressive than formal grammars (which is what we're processing),
          but because of the subset of functionality we need, we can make a regex that's close enough.
          To make sure the number of instances of rule42 and rule31 match, we can just make a regex of
          the form /(<42>{1}<31>{1})|(<42>{2}<31>{2})|.../. To know how long we need to extend this
          regex, we simply need to know the minimum length of rules 42 & 31 and divide the longest
          string by that length and then 2 to obtain the maximum number of times the rules could
          appear
          */

          fun minLength(node: Node): Int =
            when (node) {
              is Node.Constant -> 1
              is Node.Union -> min(minLength(node.left), minLength(node.right))
              is Node.Concat -> node.nodes.sumOf { minLength(it) }
              else -> throw IllegalArgumentException()
            }

          val maxMessageLength = messages.maxOf { it.length }
          val minMatchLength = min(minLength(rule42), minLength(rule31))
          val maxCount = maxMessageLength / minMatchLength / 2 + 1

          val re42 = "(" + rule42.re() + ")"
          val re31 = "(" + rule31.re() + ")"
          val innerRe =
              (1..maxCount)
                  .map { times -> "$re42{$times}$re31{$times}" }
                  .joinToString("|")

          put("8") { "$re42+" }
          put("11") { "($innerRe)" }
        }

    val rootName = "0"
    val map = parseRules(rules, rootName, seedNodes)

    val rootRegex = Regex("^${map[rootName]!!.re()}$")
    return messages.count { rootRegex.matches(it) }
  }

  @Test
  fun testExample() {
    """
      0: 4 1 5
      1: 2 3 | 3 2
      2: 4 4 | 5 5
      3: 4 5 | 5 4
      4: "a"
      5: "b"

      ababbb
      bababa
      abbbab
      aaabbb
      aaaabbb
    """
        .trimIndent()
        .let { input -> assertEquals(2, part1(input)) }

    """
      42: 9 14 | 10 1
      9: 14 27 | 1 26
      10: 23 14 | 28 1
      1: "a"
      11: 42 31
      5: 1 14 | 15 1
      19: 14 1 | 14 14
      12: 24 14 | 19 1
      16: 15 1 | 14 14
      31: 14 17 | 1 13
      6: 14 14 | 1 14
      2: 1 24 | 14 4
      0: 8 11
      13: 14 3 | 1 12
      15: 1 | 14
      17: 14 2 | 1 7
      23: 25 1 | 22 14
      28: 16 1
      4: 1 1
      20: 14 14 | 1 15
      3: 5 14 | 16 1
      27: 1 6 | 14 18
      14: "b"
      21: 14 1 | 1 14
      25: 1 1 | 1 14
      22: 14 14
      8: 42
      26: 14 22 | 1 20
      18: 15 15
      7: 14 5 | 1 21
      24: 14 1

      abbbbbabbbaaaababbaabbbbabababbbabbbbbbabaaaa
      bbabbbbaabaabba
      babbbbaabbbbbabbbbbbaabaaabaaa
      aaabbbbbbaaaabaababaabababbabaaabbababababaaa
      bbbbbbbaaaabbbbaaabbabaaa
      bbbababbbbaaaaaaaabbababaaababaabab
      ababaaaaaabaaab
      ababaaaaabbbaba
      baabbaaaabbaaaababbaababb
      abbbbabbbbaaaababbbbbbaaaababb
      aaaaabbaabaaaaababaa
      aaaabbaaaabbaaa
      aaaabbaabbaaaaaaabbbabbbaaabbaabaaa
      babaaabbbaaabaababbaabababaaab
      aabbbbbaabbbaaaaaabbbbbababaaaaabbaaabba
    """
        .trimIndent()
        .let { input ->
          assertEquals(3, part1(input))
          assertEquals(12, part2(input))
        }
  }
}
