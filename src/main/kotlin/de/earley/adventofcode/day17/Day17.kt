package de.earley.adventofcode.day17

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()

    val state = parseInput(input)

    println("Part one: ${partOne(state)}")
    println("Part two: ${partTwo(extend(state))}")
}

typealias Vec3 = Triple<Int, Int, Int>
data class Vec4(
    val x : Int,
    val y : Int,
    val z : Int,
    val w : Int,
)

// active elements
typealias State<V> = Set<V>

private fun extend(s : State<Vec3>) : State<Vec4> =
    s.map { pos -> Vec4(pos.first, pos.second, pos.third, 0) }.toSet()

private fun parseInput(input : List<String>) : State<Vec3> =
    input.flatMapIndexed { y, line ->
    line.mapIndexedNotNull { x, c -> when (c) {
        '#' -> Triple(x, y, 0)
        else -> null
    } }
}.toSet()

@OptIn(ExperimentalStdlibApi::class)
private fun neighbourPositions3(p : Vec3) : List<Vec3> = buildList {
    for (dx in -1 .. 1) {
        for (dy in -1 .. 1) {
            for (dz in -1 .. 1) {
                if (dx == 0 && dy == 0 && dz == 0) continue
                add(Triple(p.first + dx, p.second + dy, p.third + dz))
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun neighbourPositions4(p : Vec4) : List<Vec4> = buildList {
    for (dx in -1 .. 1) {
        for (dy in -1 .. 1) {
            for (dz in -1 .. 1) {
                for (dw in -1 .. 1) {
                    if (dx == 0 && dy == 0 && dz == 0 && dw == 0) continue
                    add(Vec4(p.x + dx, p.y + dy, p.z + dz, p.w + dw))
                }
            }
        }
    }
}

private fun <V> State<V>.step(neighbourPositions: (V) -> List<V>): State<V> {
    val newActive = this.toMutableSet()

    fun activeNeighbours(pos: V): Int = neighbourPositions(pos).count {
        this.contains(it)
    }

    // If a cube is active and exactly 2 or 3 of its neighbors are also active, the cube remains active. Otherwise, the cube becomes inactive.
    newActive.removeIf { pos ->
        when (activeNeighbours(pos)) {
            2, 3 -> false
            else -> true // remove
        }
    }

    //the edges are all inactive, so go through them too
    // the region to consider is any empty space next to an active cell
    val edges: Set<V> = this.flatMap { pos ->
        neighbourPositions(pos).filterNot { new -> this.contains(new) }
    }.toSet()

    // If a cube is inactive but exactly 3 of its neighbors are active, the cube becomes active. Otherwise, the cube remains inactive.
    newActive.addAll(
        edges.filter { pos ->
            activeNeighbours(pos) == 3
        }
    )

    return newActive
}

private fun partOne(init : State<Vec3>) : Int {
    var state = init
    repeat(6) {
        state = state.step(::neighbourPositions3)
    }
    return state.count()
}

private fun partTwo(init : State<Vec4>) : Int {
    var state = init
    repeat(6) {
        state = state.step(::neighbourPositions4)
    }
    return state.count()
}