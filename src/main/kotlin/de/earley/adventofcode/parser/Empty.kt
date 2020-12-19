package de.earley.adventofcode.parser

/**
 * Represents a failed parser
 */
//TODO remove data class to use hashCode in equals? e.g. seen
class Empty<I>(val error : ParseResult.Error<I>) : Parser<I, Nothing> {
    override fun derive(i: I)  = this
    override fun deriveNull() = error
    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, Nothing> = this
    override fun size(seen: MutableSet<Parser<*, *>>): Int = 1
}