package de.earley.adventofcode.parser

private class Lazy<I, O>(
    val f : () -> Parser<I, O>
) : Parser<I, O> {

    val p : Parser<I, O> by lazy {
        f()
    }

    override fun derive(i: I): Parser<I, O> = p.derive(i)
    override fun deriveNull(): ParseResult<I, O> = p.deriveNull()
    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, O> = p.compact(seen, disregardErrors)

    override fun size(seen: MutableSet<Parser<*, *>>): Int = 0 // because p has not been evaluated yet
}

fun <I, O> lazyParser(f : () -> Parser<I, O>) : Parser<I, O> = Lazy(f)