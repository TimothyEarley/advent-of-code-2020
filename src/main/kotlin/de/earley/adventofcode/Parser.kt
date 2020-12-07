package de.earley.adventofcode

// a very simply parser implementation

sealed class ParseResult<out T> {
    data class Ok<T>(val value : T, val state : ParserState) : ParseResult<T>()
    data class Error(val msg : String) : ParseResult<Nothing>()
}
typealias Parser<T> = ParserState.() -> ParseResult<T>

data class ParserState(
    private val input: String,
    private val position: Int
) {
    fun isDone(): Boolean = position == input.length
    val currentChar: Char
        get() = input[position]

    fun next() = ParserState(input, position + 1)
    fun rest() : String = input.substring(position)
}

fun <T> Parser<T>.parse(string: String): T =
   when (val result = ParserState(string, 0).this()) {
       is ParseResult.Ok -> if (result.state.isDone()) result.value else throw Exception("Parsing did not finish: Value is ${result.value}, rest is \"${result.state.rest()}\"")
       is ParseResult.Error -> throw Exception(result.msg)
   }

fun empty(): Parser<Unit> = { ParseResult.Ok(Unit, this) }
fun char(c: Char): Parser<Char> = {
    when {
        isDone() -> ParseResult.Error("Expected char '$c', but input is done.")
        currentChar == c -> ParseResult.Ok(c, next())
        else -> ParseResult.Error("Expected char '$c', but got '$currentChar' at $this")
    }
}

fun space() : Parser<Unit> = char(' ').map { }
fun <T> Parser<T>.void(): Parser<Unit> = map { }

operator fun <A, B> Parser<A>.plus(other: Parser<B>): Parser<Pair<A, B>> = {
    when (val result = this.this@plus()) {
        is ParseResult.Ok -> when (val result2 = result.state.other()) {
            is ParseResult.Ok -> ParseResult.Ok(result.value to result2.value, result2.state)
            is ParseResult.Error -> result2
        }
        is ParseResult.Error -> result
    }
}
@JvmName("unitPlus")
operator fun <A> Parser<A>.plus(other: Parser<Unit>): Parser<A> = {
    when (val result = this.this@plus()) {
        is ParseResult.Ok -> when (val result2 = result.state.other()) {
            is ParseResult.Ok -> ParseResult.Ok(result.value, result2.state)
            is ParseResult.Error -> result2
        }
        is ParseResult.Error -> result
    }
}
@JvmName("plusUnit")
operator fun <B> Parser<Unit>.plus(other: Parser<B>): Parser<B> = {
    when (val result = this.this@plus()) {
        is ParseResult.Ok -> result.state.other()
        is ParseResult.Error -> result
    }
}

inline fun <A, B> Parser<A>.map(crossinline f: (A) -> B): Parser<B> = {
    when (val result = this.this@map()) {
        is ParseResult.Ok -> ParseResult.Ok(f(result.value), result.state)
        is ParseResult.Error -> result
    }
}

fun string(s: String): Parser<String> = s
    .map { char(it) }
    .fold<Parser<Any?>, Parser<Any?>>(empty()) { acc, b -> acc + b }
    .map { s }

fun oneOf(vararg s: String): Parser<String> = s.map { string((it)) }.reduce { a, b -> a or b }

fun anyWord(): Parser<String> = {
    var word: String = ""
    var state = this
    while (!state.isDone() && state.currentChar.isLetter()) {
        word += state.currentChar
        state = state.next()
    }
    ParseResult.Ok(word, state)
}

infix fun <A> Parser<A>.or(other: Parser<A>): Parser<A> = {
    when (val result = this.this@or()) {
      is ParseResult.Ok -> result
      is ParseResult.Error ->  this.other()
    }
}

fun <T> maybe(p: Parser<T>): Parser<T?> = {
    when (val result = p()) {
        is ParseResult.Ok -> result
        is ParseResult.Error -> ParseResult.Ok(null, this)
    }
}

fun <T> lazy(p: () -> Parser<T>): Parser<T> = { p()() }

fun <T> many(p: Parser<T>, delimiter: Parser<Any?> = empty()): Parser<List<T>> = lazy {
    (p + maybe(delimiter + many(p, delimiter))).map { (t, rest) ->
        if (rest == null) {
            listOf(t)
        } else {
            rest.second + t
        }
    }
} or empty().map { emptyList() }

fun number(): Parser<Int> = {
    var numString = ""
    var state = this
    while (!state.isDone() && state.currentChar.isDigit()) {
        numString += state.currentChar
        state = state.next()
    }
    when {
        state.isDone() -> ParseResult.Error("Expected a digit but input is done")
        numString.isEmpty() -> ParseResult.Error("Expected a digit but found ${state.currentChar} at $state")
        else -> ParseResult.Ok(numString.toInt(), state)
    }
}