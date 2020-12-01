package de.earley.adventofcode.day1

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
 * The list needs to be sorted.
 * Starts at the first (smallest) and last (highest) element.
 * If that sum is too low it can only be increased by increasing the smaller index.
 * If it is too high decrement the high index.
 */
private fun List<Int>.findSumToOrNull(sum: Int): Pair<Int, Int>? {
    var low = 0
    var high = lastIndex

    while (low < high) {
        // try this sum
        val l = get(low)
        val h = get(high)
        val thisSum = l + h
        when {
            thisSum < sum -> {
                // need to be bigger
                low++
            }
            thisSum == sum -> return l to h
            thisSum > sum -> {
                // need to be smaller
                high--
            }
        }
    }

    // we have not found any match
    return null
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