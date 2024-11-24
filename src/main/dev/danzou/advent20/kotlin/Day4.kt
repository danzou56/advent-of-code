package dev.danzou.advent20.kotlin

import dev.danzou.advent20.AdventTestRunner20

internal class Day4 : AdventTestRunner20("") {

  enum class Field(val validate: (String) -> Boolean) {
    // Apparently for number fields, we can always assume that they're valid numbers?
    // That is, NumberFormatException is never thrown!
    byr({ s -> s.toInt() in 1920..2002 }), // (Birth Year)
    iyr({ s -> s.toInt() in 2010..2020 }), // (Issue Year)
    eyr({ s -> s.toInt() in 2020..2030 }), // (Expiration Year)
    hgt({ s ->
      when (s.takeLast(2)) {
        "cm" -> s.dropLast(2).toInt() in 150..193
        "in" -> s.dropLast(2).toInt() in 59..76
        else -> false
      }
    }), // (Height)
    hcl({ s -> s.matches(Regex("#[0-9a-f]{6}")) }), // (Hair Color)
    ecl({ s -> s in "amb blu brn gry grn hzl oth".split(" ") }), // (Eye Color)
    pid({ s -> s.matches(Regex("\\d{9}")) }), // (Passport ID)
    cid({ true }); // (Country ID)

    companion object {
      val required =
          setOf(
              byr, // (Birth Year)
              iyr, // (Issue Year)
              eyr, // (Expiration Year)
              hgt, // (Height)
              hcl, // (Hair Color)
              ecl, // (Eye Color)
              pid, // (Passport ID)
          )
    }
  }

  fun parsePassports(input: String): List<Map<Field, String>> =
    input
      .split("\n\n")
      .map { it.split(" ", "\n") }
      .map { passport ->
        passport
          .map { it.split(":") }
          .associate { (key, value) -> Field.valueOf(key) to value }
      }

  override fun part1(input: String): Any {
    val passports = parsePassports(input)
    return passports.count { m -> m.keys.containsAll(Field.required) }
  }

  override fun part2(input: String): Any {
    val passports = parsePassports(input)
    return passports.count { m ->
      m.keys.containsAll(Field.required) && m.all { (field, value) -> field.validate(value) }
    }
  }
}
