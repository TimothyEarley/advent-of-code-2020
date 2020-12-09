package de.earley.adventofcode.day1

import de.earley.adventofcode.findSumToOrNull

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()

    input.useLines { line ->
        val sorted = line.map { it.toInt() }.toList().sorted()
        println("Part One: ${solvePartOne(sorted)}")
        println("Part Two: ${solvePartTwo(sorted)}")
    }
}

/**
 * Given a sorted list of numbers find two numbers that sum to 2020
 * and return their product.
 */
private fun solvePartOne(sorted: List<Int>): Int {
    val (a, b) = sorted.findSumToOrNull(2020) ?: throw Exception("No solution!")
    return a * b
}

/**
 * Given a sorted list of numbers find **three** numbers that sum to 2020
 * and return their product.
 */
private fun solvePartTwo(numbers : List<Int>) : Int {
    // Idea: go through every number and reduce the problem to 2 numbers
    for (picked in numbers) {
        val targetSum = 2020 - picked
        if (targetSum < 0) continue

        val (a, b) = numbers.findSumToOrNull(targetSum) ?: continue

        return picked * a * b
    }

    throw Exception("No solution!")
}