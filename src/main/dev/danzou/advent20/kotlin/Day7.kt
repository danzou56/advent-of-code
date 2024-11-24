package dev.danzou.advent20.kotlin

import dev.danzou.advent20.AdventTestRunner20
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day7 : AdventTestRunner20("Handy Haversacks") {
  override fun part1(input: String): Int {
    val bagRules = parseBagRules(input)
    tailrec fun step(insideBags: Set<String>): Set<String> {
      val containingBags =
          insideBags
              .flatMap { inside ->
                bagRules.filterValues { contents -> inside in contents }.keys
              }
              .toSet()
      if (containingBags + insideBags == insideBags) return insideBags

      return step(containingBags + insideBags)
    }

    return (step(setOf("shiny gold")) - "shiny gold").size
  }

  override fun part2(input: String): Long {
    val bagRules = parseBagRules(input)

    tailrec fun step(
        curBags: Map<String, Long>,
        outerBags: Map<String, Long>
    ): Map<String, Long> {
      if (curBags.isEmpty()) return outerBags
      val insideBags =
          curBags
              .map { (key, multiplier) ->
                bagRules[key]!!.mapValues { (_, count) -> count * multiplier }
              }
              .map(Map<String, Long>::entries)
              .reduce { m1, m2 -> m1 + m2 }
              .groupBy({ it.key }, { it.value })
              .mapValues { (_, counts) -> counts.sum() }
      return step(
          insideBags,
          (outerBags.entries + curBags.entries)
              .groupBy({ it.key }, { it.value })
              .mapValues { (_, counts) -> counts.sum() },
      )
    }

    return (step(mapOf("shiny gold" to 1), emptyMap()) - "shiny gold").values.sum()
  }

  fun parseBagRules(input: String): Map<String, Map<String, Int>> {
    return input
        .lines()
        .map { it.split(" contain ") }
        .associate { (outer, inner) ->
          outer.dropLast(" bags".length) to
              when (inner) {
                "no other bags." -> emptyMap()
                else ->
                    inner.split(", ").associate {
                      val countStr = it.takeWhile(Char::isDigit)
                      it.removePrefix(countStr)
                          .removeSuffix(".")
                          .removeSuffix("s")
                          .removeSuffix("bag")
                          .trim() to countStr.toInt()
                    }
              }
        }
  }

  @Test
  fun testExample() {
    val input =
        """
          light red bags contain 1 bright white bag, 2 muted yellow bags.
          dark orange bags contain 3 bright white bags, 4 muted yellow bags.
          bright white bags contain 1 shiny gold bag.
          muted yellow bags contain 2 shiny gold bags, 9 faded blue bags.
          shiny gold bags contain 1 dark olive bag, 2 vibrant plum bags.
          dark olive bags contain 3 faded blue bags, 4 dotted black bags.
          vibrant plum bags contain 5 faded blue bags, 6 dotted black bags.
          faded blue bags contain no other bags.
          dotted black bags contain no other bags.
        """
            .trimIndent()

    assertEquals(4, part1(input))
    assertEquals(32, part2(input))

    val input2 =
        """
        shiny gold bags contain 2 dark red bags.
        dark red bags contain 2 dark orange bags.
        dark orange bags contain 2 dark yellow bags.
        dark yellow bags contain 2 dark green bags.
        dark green bags contain 2 dark blue bags.
        dark blue bags contain 2 dark violet bags.
        dark violet bags contain no other bags.
      """
            .trimIndent()

    assertEquals(126, part2(input2))
  }
}
