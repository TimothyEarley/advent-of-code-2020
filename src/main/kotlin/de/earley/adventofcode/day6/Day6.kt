package de.earley.adventofcode.day6

import de.earley.adventofcode.split

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    val partOne = input
        .split(String::isBlank)
        .map { it.joinToString("") }
        .map(String::toSet)
        .map(Set<Char>::size)
        .sum()

    println("Part one: $partOne")

    val partTwo = input
        .split(String::isBlank)
        .map { group ->
            group
                .map(String::toSet)
                .reduce { acc, s ->
                    acc intersect s
                }
                .size
        }
        .sum()

    println("Part two: $partTwo")
}