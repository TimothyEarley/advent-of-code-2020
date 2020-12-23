package de.earley.adventofcode.day23

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    val numbers = input.first().split("").filterNot(String::isBlank).map { it.toInt() }

    println("Part one: ${partOne(numbers)}")
    println("Part two: ${partTwo(numbers)}")
}

private fun partOne(numbers : List<Int>) : String {
    val init = Array(numbers.size) {
        val num = it + 1
        val numIndex = numbers.indexOf(num)
        if (numIndex + 1 == numbers.size) numbers.first() - 1
        else numbers[numIndex + 1] - 1
    }

    val state = playGame(numbers.first() - 1, init, 100)

    var start = state[0]
    return buildString {
        repeat(numbers.size - 1) {
            append(start + 1)
            start = state[start]
        }
    }
}

private fun partTwo(numbers : List<Int>) : Long {
    val totalCups = 1000000
    val max = numbers.maxOrNull()!!

    val init = Array(totalCups) {
        val num = it + 1
        if (numbers.contains(num)) {
            val numIndex = numbers.indexOf(num)
            if (numIndex + 1 == numbers.size) max
            else numbers[numIndex + 1] - 1
        } else {
            // just the next number (or start)
            if (num == totalCups) {
                numbers.first() - 1
            } else {
                num // num + 1 - 1
            }
        }

    }

    val state = playGame(numbers.first() - 1, init, 10000000)

    val afterCupOne = state[0]
    val afterThat = state[afterCupOne]

    return (afterCupOne + 1).toLong() * (afterThat + 1)
}

// since array index from 0, and the game indexes from 1, a lot is shifted here
// state[i] is the number that comes after i (with the -1 offset)
private fun playGame(start: Int, state: Array<Int>, rounds: Int): Array<Int> {
    var currentNumber = start
    repeat(rounds) {
        val moving1 = state[currentNumber]
        val moving2 = state[moving1]
        val moving3 = state[moving2]

        state[currentNumber] = state[moving3]

        val destination = findDestination(state, currentNumber, setOf(moving1, moving2, moving3))

        // put the moving after the destination
        val destinationNext = state[destination]
        state[destination] = moving1
        state[moving3] = destinationNext

        currentNumber = state[currentNumber]
        // debug(state, currentNumber)

    }
    return state
}

fun findDestination(state: Array<Int>, current: Int, moving : Set<Int>): Int {
    val target = when (current) {
        0 -> state.size - 1
        else -> current - 1
    }

    return if (moving.contains(target)) {
        // not this one
        findDestination(state, target, moving)
    } else {
        target
    }

}