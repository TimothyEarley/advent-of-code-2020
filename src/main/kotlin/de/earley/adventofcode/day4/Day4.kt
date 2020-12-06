package de.earley.adventofcode.day4

import de.earley.adventofcode.split

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    val passports = parseInput(input)

    println("Part one: ${passports.count(::partOneValid)}")
    println("Part two: ${passports.count(::partTwoValid)}")
}

private typealias Passport = Map<String, String>

private val spaceRegex = Regex("\\s+")
private fun parseInput(lines : List<String>) : List<Passport> =
   lines.split(String::isBlank)
        .map { it.joinToString(" ") }
        .map(::parsePassport)


private fun parsePassport(data : String) : Passport =
    data.split(spaceRegex).map { it.split(":", limit = 2) }.associate { it[0] to it[1] }

private val requiredFields = listOf("byr", "iyr", "eyr", "hgt", "hcl", "ecl", "pid")
private fun partOneValid(passport: Passport) : Boolean =
    requiredFields.all { passport.containsKey(it) }

typealias Requirement = (String) -> Boolean
private infix fun Requirement.and(other : Requirement) : Requirement = {
    this(it) && other(it)
}
private infix fun Requirement.or(other : Requirement) : Requirement = {
    this(it) || other(it)
}

private fun requireRegex(regex: Regex) : Requirement = { it.matches(regex) }
private fun digitCount(count : Int) : Requirement = requireRegex(Regex("\\d{$count}"))
private fun between(min : Int, max : Int) : Requirement = { it.toIntOrNull() in min..max }
private fun stringFollowedBy(string: String, requirement: Requirement) : Requirement = {
    it.startsWith(string) && requirement(it.removePrefix(string))
}
private fun stringPrecededBy(string: String, requirement: Requirement) : Requirement = {
    it.endsWith(string) && requirement(it.removeSuffix(string))
}
private fun oneOf(vararg strings: String) : Requirement = {
    strings.contains(it)
}

private val requirements = mapOf(
    "byr" to (digitCount(4) and between(1920, 2002)),
    "iyr" to (digitCount(4) and between(2010, 2020)),
    "eyr" to (digitCount(4) and between(2020, 2030)),
    "hgt" to ((stringPrecededBy("cm", between(150, 193)) or stringPrecededBy("in", between(59, 76)))),
    "hcl" to (stringFollowedBy("#", requireRegex(Regex("[0-9a-f]{6}")))),
    "ecl" to oneOf("amb", "blu", "brn", "gry", "grn", "hzl", "oth"),
    "pid" to digitCount(9)
)
private fun partTwoValid(passport: Passport) : Boolean =
    requirements.all {
        val field = passport[it.key]
        if (field != null) it.value(field)
        else false
    }
