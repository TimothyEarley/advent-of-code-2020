package de.earley.adventofcode.day2

import kotlin.math.max

fun main() {

    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .lineSequence().map(::parseLine).toList()

    val answerOne = input.count { (policy, password) -> policy.checkOne(password) }
    val answerTwo = input.count { (policy, password) -> policy.checkTwo(password) }

    println("Part One: $answerOne, Part Two: $answerTwo")
}

typealias Password = String

private val policyRegex = Regex("(\\d+)-(\\d+)\\s(\\w)")
private fun parseLine(line : String) : Pair<Policy, Password> {
    val (policyPart, password) = line.split(": ", limit = 2)

    val match = policyRegex.matchEntire(policyPart)
    requireNotNull(match) { "Failed to parse $line" }
    val (min, max, letter) = match.destructured

    return Policy(min.toInt(), max.toInt(), letter.single()) to password
}

private data class Policy(
    val a : Int,
    val b : Int,
    val letter : Char
)

private fun Policy.checkOne(password : Password) : Boolean {
    val occurrences = password.toCharArray().count { it == letter }
    return occurrences in a..b
}

private fun Policy.checkTwo(password : Password) : Boolean {
    val atA = password.getOrNull(a - 1) == letter
    val atB = password.getOrNull(b - 1) == letter
    return atA != atB
}