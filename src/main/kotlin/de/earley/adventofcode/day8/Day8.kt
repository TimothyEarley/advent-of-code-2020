package de.earley.adventofcode.day8

fun main() {
    val code = object {}.javaClass.getResourceAsStream("input.txt").bufferedReader()
        .readLines()
        .map(::parseInstruction)

    println("Part one: ${run(code)}")

    val fixed = fixCode(code)
    println("Part two: ${run(fixed)}")
}

private typealias PC = Int
private data class State(val accumulator : Int, val pc : PC) {
    fun jump(offset : Int) = State(accumulator, pc + offset)
}

private sealed class Instruction {

    abstract fun step(state : State) : State

    data class Nop(val value: Int) : Instruction() {
        override fun step(state: State): State = state.jump(1)
    }

    data class Acc(val value : Int) : Instruction() {
        override fun step(state: State): State = state.copy(accumulator =  state.accumulator + value, pc = state.pc + 1)
    }

    data class Jmp(val value: Int) : Instruction() {
        override fun step(state: State): State = state.jump(value)
    }
}

private fun Instruction.jumpSize() : Int = when (this) {
    is Instruction.Nop -> 1
    is Instruction.Acc -> 1
    is Instruction.Jmp -> value
}

private fun parseInstruction(line : String) : Instruction {
    val mnemonic = line.substring(0 .. 2)
    val value = line.substring(4).toInt()
    return when (mnemonic) {
        "nop" -> Instruction.Nop(value)
        "acc" -> Instruction.Acc(value)
        "jmp" -> Instruction.Jmp(value)
        else -> throw Exception("Unknown instruction $line")
    }
}

/**
 * Run until end or loop
 * if the boolean is true there was no loop
 */
private fun run(code : List<Instruction>) : Pair<State, Boolean> {

    var state = State(0, 0)
    val visitedPcs = mutableSetOf<Int>()

    while (code.indices.contains(state.pc)) {
        val instruction = code[state.pc]
        state = instruction.step(state)
        if ( ! visitedPcs.add(state.pc) ) {
            // element was already in list
            return state to false
        }
    }

    // code has finished!
    return state to true
}

/**
 * Fix so that the program terminates by changing one nop to a jmp or vice versa
 */
private fun fixCode(code : List<Instruction>) : List<Instruction> {
    // convert the jumps to a lookup table
    // reverseLookup[n] is all the instructions that jump to n
    val reverseLookup : Map<PC, Set<PC>> = code.mapIndexed { i, ins ->
        (i + ins.jumpSize()) to i
    }.fold(emptyMap()) { acc, (to, from) ->
        acc.toMutableMap().apply {
            compute(to) { _, entry -> (entry ?: emptySet()) + from  }
        }
    }

    // build a list of instruction that reach the end
    val reverse = reverseReachable(code.size, reverseLookup)

    // build a list of instructions reachable from the start
    val forward = reachable(0, code)

    // we know there is a single swap
    // to find it we must bridge the gap between forward and reverse,
    // that is find something in forward that when flipped points to something in reverse

    val swapAt = forward.find { pc ->
        when (val ins = code[pc]) {
            is Instruction.Nop -> reverse.contains(pc + ins.value) // test a change to jump
            is Instruction.Acc -> false // cannot change
            is Instruction.Jmp -> reverse.contains(pc + 1) // test a change to nop
        }
    }

    return code.mapIndexed { i, ins ->
        if (i == swapAt) {
            when (ins) {
                is Instruction.Nop -> Instruction.Jmp(ins.value)
                is Instruction.Acc -> throw Exception("Impossible since we filtered that case out above")
                is Instruction.Jmp -> Instruction.Nop(ins.value)
            }
        } else {
            ins
        }
    }
}

private fun reverseReachable(from: PC, lookup: Map<PC, Set<PC>>): Set<PC> {
    val reaches = lookup[from] ?: return setOf(from)
    return reaches.flatMap { reverseReachable(it, lookup) }.toSet()
}

private fun reachable(from: PC, code: List<Instruction>): Set<PC> {

    tailrec fun reach(todo : List<PC>, acc : Set<PC>) : Set<PC> {
        if (todo.isEmpty()) return acc

        val current = todo.first()
        return if (acc.contains(current) || !code.indices.contains(current)) acc
        else {
            val next = current + code[current].jumpSize()
            reach(todo.drop(1) + next, acc + current)
        }
    }

    return reach(listOf(from), emptySet())
}