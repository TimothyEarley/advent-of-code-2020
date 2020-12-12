package de.earley.adventofcode.day12

import kotlin.math.abs

fun main() {
    val lines = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()
        .parseActions()

    println("Part one ${partOne(lines)}")
    println("Part one ${partTwo(lines)}")
}

private enum class Compass {
    NORTH, SOUTH, EAST, WEST
}
private enum class Direction {
    NORTH, SOUTH, EAST, WEST, LEFT, RIGHT, FORWARD
}
private data class Action(val direction : Direction, val amount : Int)

private fun List<String>.parseActions(): List<Action> =
    map {
        val d = when (it[0]) {
            'N' -> Direction.NORTH
            'S' -> Direction.SOUTH
            'E' -> Direction.EAST
            'W' -> Direction.WEST
            'L' -> Direction.LEFT
            'R' -> Direction.RIGHT
            'F' -> Direction.FORWARD
            else -> throw Exception("Could not parse $it, unknown direction ${it[0]}")
        }
        val a = it.substring(1).toInt()
        Action(d, a)
    }

private data class State(
    val x : Int, // to the east
    val y : Int, // to the north
    val facing : Compass
)

private fun Compass.rotate(angle : Int) : Compass = when {
    angle <= -90 -> when (this) {
        Compass.NORTH -> Compass.WEST.rotate(angle + 90)
        Compass.SOUTH -> Compass.EAST.rotate(angle + 90)
        Compass.EAST -> Compass.NORTH.rotate(angle + 90)
        Compass.WEST -> Compass.SOUTH.rotate(angle + 90)
    }
    angle == 0 -> this
    angle >= 90 -> when (this) {
        Compass.NORTH -> Compass.EAST.rotate(angle - 90)
        Compass.SOUTH -> Compass.WEST.rotate(angle - 90)
        Compass.EAST -> Compass.SOUTH.rotate(angle - 90)
        Compass.WEST -> Compass.NORTH.rotate(angle - 90)
    }
    else -> throw Exception("Unsupported angle $angle")
}

private fun State.step(action : Action) : State = when (action.direction) {
    Direction.NORTH -> copy(y = y + action.amount)
    Direction.SOUTH -> copy(y = y - action.amount)
    Direction.EAST -> copy(x = x + action.amount)
    Direction.WEST -> copy(x = x - action.amount)
    Direction.LEFT -> copy(facing = facing.rotate(- action.amount))
    Direction.RIGHT -> copy(facing = facing.rotate(action.amount))
    Direction.FORWARD -> when (facing) {
        Compass.NORTH -> copy(y = y + action.amount)
        Compass.SOUTH -> copy(y = y - action.amount)
        Compass.EAST -> copy(x = x + action.amount)
        Compass.WEST -> copy(x = x - action.amount)
    }
}

private fun partOne(actions : List<Action>) : Int {

    val end = actions.fold(State(0, 0, Compass.EAST)) { state, action ->
        state.step(action)
    }

    return abs(end.x) + abs(end.y)
}

private data class State2(
    val x : Int,
    val y : Int,
    val waypointX : Int,
    val waypointY: Int
)

private tailrec fun State2.rotate(angle : Int) : State2 = when {
    angle <= -90 -> copy(waypointX = - waypointY, waypointY = waypointX).rotate(angle + 90)
    angle == 0 -> this
    angle >= 90 -> copy(waypointX = waypointY, waypointY = - waypointX).rotate(angle - 90)
    else -> throw Exception("Unsupported angle $angle")
}

private fun State2.step(action : Action) : State2 = when (action.direction) {
    Direction.NORTH -> copy(waypointY = waypointY + action.amount)
    Direction.SOUTH -> copy(waypointY = waypointY - action.amount)
    Direction.EAST -> copy(waypointX = waypointX + action.amount)
    Direction.WEST -> copy(waypointX = waypointX - action.amount)
    Direction.LEFT -> rotate( - action.amount )
    Direction.RIGHT -> rotate( action.amount )
    Direction.FORWARD -> (1..action.amount).fold(this) { state, _ ->
        state.copy(x = state.x + waypointX, y = state.y + waypointY)
    }
}

private fun partTwo(actions: List<Action>): Int {

    val end = actions.fold(State2(0, 0, 10, 1)) { state, action ->
        state.step(action)
    }

    return abs(end.x) + abs(end.y)

}