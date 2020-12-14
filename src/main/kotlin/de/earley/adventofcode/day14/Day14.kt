@file:OptIn(ExperimentalUnsignedTypes::class)

package de.earley.adventofcode.day14

import de.earley.adventofcode.sumByLong
import kotlin.math.pow
import kotlin.math.roundToInt


private const val SIZE = 36

fun main() {
    val input = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()
        .map(::parseInstruction)

    println("Part one: ${partOne(input)}")
    println("Part two: ${partTwo(input)}")
}

private enum class MaskEntry {
    ONE, ZERO, X
}
private typealias Mask = List<MaskEntry>

private data class State(
    val mask : Mask,
    val memory : Map<Int, ULong>
) {
    fun run(code : List<Instruction>) : State = code.fold(this) { state, i ->
        i.step(state)
    }
}

private sealed class Instruction {
    abstract fun step(state : State) : State
}
private data class SetMask(val mask : Mask) : Instruction() {
    override fun step(state: State): State = state.copy(mask = mask)
}

private data class SetMem(val memAddr : Int, val value : Int) : Instruction() {
    override fun step(state: State): State {
        val combined = value.toString(2).padStart(SIZE, '0').toList().zip(state.mask).map { (bit, m) ->
            when (m) {
                MaskEntry.X -> bit
                MaskEntry.ONE -> '1'
                MaskEntry.ZERO -> '0'
            }
        }
        val actualValue = combined.joinToString("").toULong(2)

        return state.copy(memory = state.memory + (memAddr to actualValue))
    }
}

private fun parseInstruction(line : String) : Instruction = if (line.startsWith("mask =")) {
    SetMask(line.substringAfterLast(' ').map { when (it) {
        '0' -> MaskEntry.ZERO
        '1' -> MaskEntry.ONE
        'X' -> MaskEntry.X
        else -> throw Exception("Could not parse $line, unknown char $it")
    } })
} else {
    val addr = line.substringAfter('[').substringBefore(']').toInt()
    val value = line.substringAfterLast(' ').toInt()
    SetMem(addr, value)
}

private fun partOne(code: List<Instruction>): ULong {
    val init = State(List(SIZE) { MaskEntry.X }, emptyMap())
    val end = init.run(code)
    return end.memory.values.sum()
}

/**
 * Each address can now be a range of locations, since an 'X' is replace with both '1' and '0'.
 * Instead of expanding all the possibilities we adapt the masks to remove overlap.
 * I.e. if we have the mask X0XX and 10X1, then they overlap but we can fix it:
 * Start with the first 'X' and replace it with a '0', this removes the overlap.
 * Alternatively change to '1' (to avoid overlapping with self) and continue: The next '0' cannot be replaced, the next 'X' is covered
 * by the 'X' in the other mask. The final 'X' can be changed to a '0'. Result:
 * - 00XX
 * - 10X0
 * - 10X1
 * are all distinct
 *
 */
private fun partTwo(code: List<Instruction>): Long {
    var mask = List(SIZE) { MaskEntry.X }
    val memory : MutableMap<Mask, Int> = mutableMapOf()

    for (i in code) {
        when (i) {
            is SetMask -> mask = i.mask
            is SetMem -> {
                // get the combined mask
                val combined = i.memAddr.toString(2).padStart(SIZE, '0').toList().zip(mask).map { (bit, m) ->
                    when (m) {
                        MaskEntry.ONE -> MaskEntry.ONE
                        MaskEntry.ZERO -> when (bit) {
                            '0' -> MaskEntry.ZERO
                            '1' -> MaskEntry.ONE
                            else -> throw Exception("Not possible")
                        }
                        MaskEntry.X -> MaskEntry.X
                    }
                }

                // see if there is any overlap with existing stored masks
                val problems = memory.keys.filter {
                    // there is no problem if they can be distinguished by at least one concrete
                    // 0/1 pair

                    ! it.zip(combined).any { (a, b) ->
                        (a == MaskEntry.ONE && b == MaskEntry.ZERO) ||
                        (b == MaskEntry.ONE && a == MaskEntry.ZERO)
                    }

                }

                problems.forEach { problemMask ->
                    // this is a mask that will get overridden
                    // split it into smaller masks that are distinct from the new mask

                    // first remove it from memory (keeping the value)
                    val oldValue = memory.remove(problemMask)!! // cannot be null since we know it exists

                    var current = problemMask

                    for (pos in current.indices) {
                        val new = combined[pos]
                        val old = current[pos]

                        if (old == MaskEntry.X) {
                            // candidate for splitting
                            when (new) {
                                MaskEntry.ONE -> {
                                    // if we change to a one we are good
                                    memory[ current.changeAt(pos, MaskEntry.ZERO) ] = oldValue
                                    // we can also continue, but need to change to ONE to avoid
                                    // colliding with self
                                    current = current.changeAt(pos, MaskEntry.ONE)
                                }
                                MaskEntry.ZERO -> {
                                    // same as above, but flipped
                                    memory[ current.changeAt(pos, MaskEntry.ONE) ] = oldValue
                                    current = current.changeAt(pos, MaskEntry.ZERO)
                                }
                                MaskEntry.X -> {} // no wiggle room
                            }
                        }
                    }
                }

                memory[combined] = i.value
            }
        }
    }

    // for each entry in memory, it occurs * 2^(number of Xs) times
    return memory.entries.sumByLong { (mask, value) ->
        value.toLong() * 2f.pow(mask.count { it == MaskEntry.X }).toLong()
    }
}

private fun <E> List<E>.changeAt(pos: Int, e: E): List<E> = toMutableList().apply {
    this[pos] = e
}
