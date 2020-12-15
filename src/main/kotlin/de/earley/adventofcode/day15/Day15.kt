package de.earley.adventofcode.day15


fun main() {
    val numbers = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()
        .single()
        .split(',')
        .map(String::toInt)

    println("Part one: ${solve(numbers, 2020)}")
    println("Part two: ${solve(numbers, 30000000)}")
}

private fun solve(numbers: List<Int>, target: Int) : Int {
    // we store both current and previous occurrence
    // could probably get away with only storing previous
    data class Data(
        val current : Int,
        val previous : Int?
    )

    // we need to store the last time a number was said
    val store : MutableMap<Int, Data> = mutableMapOf()

    // assume the initial numbers are all different
    numbers.forEachIndexed { index, i ->
        store[i] = Data(index, null)
    }

    var last = numbers.last()
    for (current in numbers.size until target) {
        val lastOccurrence = store[last]!! // that number should have been stored
        val next = if (lastOccurrence.previous == null) {
            // was first occurrence
            0
        } else {
            lastOccurrence.current - lastOccurrence.previous
        }
        // update store
        store.compute(next) { _, data ->
            if (data == null) {
                Data(current, null)
            } else {
                Data(current, data.current)
            }
        }

        last = next
    }

    return last
}