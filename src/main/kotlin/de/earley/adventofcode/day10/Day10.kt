package de.earley.adventofcode.day10

import de.earley.adventofcode.sumByLong


fun main() {
    val voltages = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()
        .map(String::toInt)

    val sorted = voltages.sorted()
    val chain = (listOf(0) + sorted + (sorted.last() + 3)) // 0 for the outlet, +3 for device

    println("Part one: ${partOne(chain)}")
    println("Part two: ${partTwo(chain)}")

}

private fun partOne(chain: List<Int>): Int {
    val diff = chain.zipWithNext().map { (a, b) ->
        val d = b - a
        require(d <= 3)
        d
    }.groupingBy { it }
        .eachCount()

    return diff.getOrDefault(1, 0) * diff.getOrDefault(3, 0)
}

private fun partTwo(chain: List<Int>): Long {
    // to find the number of possible paths, calculate the number of paths that can reach each node
    // then the result is just the last nodes number

    // cache the result so we only visit every item once
    val cache: MutableMap<Int, Long> = mutableMapOf()
    fun pathCount(toIndex: Int): Long {
        if (cache.containsKey(toIndex)) {
            return cache[toIndex]!!
        }

        // start
        if (toIndex == 0) return 1
        // out of bounds
        if (toIndex < 0) return 0

        val sum = (1..3).filter {
            toIndex - it >= 0
        }.filter {
            chain[toIndex] - chain[toIndex - it] <= 3
        }.sumByLong {
            pathCount(toIndex - it)
        }

        cache[toIndex] = sum
        return sum
    }

    return pathCount(chain.lastIndex)
}

