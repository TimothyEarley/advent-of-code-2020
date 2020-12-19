package de.earley.adventofcode.parser

interface Parser<Input, out Output> {
    fun derive(i : Input) : Parser<Input, Output>
    fun deriveNull() : ParseResult<Input, Output>

    /**
     * Attempt to reduce the number of nodes in the parse graph.
     * If [disregardErrors] is true the reduction might discard error information
     */
    fun compact(seen : MutableSet<Parser<*, *>>, disregardErrors: Boolean) : Parser<Input, Output>
    fun size(seen: MutableSet<Parser<*, *>>) : Int
}

fun <T> Parser<*, *>.ifNotSeen(seen : MutableSet<Parser<*, *>>, default : T, block : () -> T) : T =
    if (! seen.contains(this)) {
        seen.add(this)
        block()
    } else default


fun <O> Parser<Char, O>.deriveAll(input : String) = deriveAll(input.toList().asSequence())

fun <I, O> Parser<I, O>.deriveAll(inputs : Sequence<I>) =
    inputs.fold(this) { parser, i ->
        parser.derive(i).compact(mutableSetOf(), false)
    }.deriveNull()