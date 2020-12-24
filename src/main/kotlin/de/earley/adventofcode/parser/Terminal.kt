package de.earley.adventofcode.parser

class Terminal<I>(val name: String, val check: (I) -> Boolean) : Parser<I, I> {
    override fun derive(i: I) : Parser<I, I> =
        if (check(i))
            epsilon(i)
        else
            Empty(ParseResult.Error(ErrorData.ExpectedName<I>(name, i)))

    override fun deriveNull(): ParseResult<I, I> = ParseResult.Error(ErrorData.ExpectedName(name, null))

    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, I> = this

    override fun toString(): String = "<$name>"
    override fun size(seen: MutableSet<Parser<*, *>>): Int = 1
}

fun char(c : Char) : Parser<Char, Char> = Terminal(c.toString()) { it == c }

//TODO CLEANUP
fun string(s : String) : Parser<Char, String> =
    (s.drop(1).fold(
        char(s.first()).map { listOf(it) }
    ) { a, b ->
        (a + char(b)).map { x, y -> x + y }
    }).map { it.joinToString("") }

val letter : Parser<Char, Char> = Terminal("letter") { it.isLetter() }
val digit : Parser<Char, Char> = Terminal("digit") { it.isDigit() }
val number : Parser<Char, Int> = many(digit).filter("non empty") { it.isNotEmpty() }.map {
    it.joinToString("").toInt()
}
val ws : Parser<Char, VOID> = char(' ').void() + many(char(' ')).void()


//TODO test