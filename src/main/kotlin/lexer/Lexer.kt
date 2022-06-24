package lexer

import Logger
import lexer.RegistryFactory.getRegistry
import token.Token
import token.variable.TokenNumber

/**
 * Lexer creates tokens from text
 *
 * Important changes:
 * 1. ";" is replaced by \n
 * 2. ternary operator and functions follow kotlin style
 */
class Lexer() {

    private var source: String = ""
    private val operators = "!@#$%^*-+=?.,:;\"&|/(){}[]><\n\r"
    private val escapes = mutableMapOf('"' to '\"', '\\' to '\\', 'b' to '\b', 'n' to '\n', 'r' to '\r', 't' to '\t')
    private var registry: Registry = getRegistry()
    private var index: Int = 0
    var position: Pair<Int, Int> = Pair(0, 0)
    private val tokens = mutableListOf<Token>()
    private var tokenIndex = -1

    constructor(source: String = "") : this() {
        this.source = source
        addTokens()
    }

    private fun addTokens() {
        tokens.add(createNextToken())
        while (index < source.length)
            addToken(createNextToken())
        if (tokens.last().symbol != "(EOF)")
            tokens.add(Token("(EOF)", "(EOF)", position))
        if (tokens.size >= 100000)
            Logger.addWarning(tokens.last(), "File too large")
    }

    fun next(): Token = tokens[++tokenIndex]
    fun prev(): Token = tokens[--tokenIndex]
    fun moveAfterSeparator() {
        tokenIndex++
        if (tokens[tokenIndex].symbol != "(SEP)")
            throw PositionalException("Expected separator", tokens[tokenIndex])
    }

    fun peek(offset: Int = 1): Token {
        if (tokenIndex + offset >= tokens.size)
            return tokens.last()
        return tokens[tokenIndex + offset]
    }

    private fun createNextToken(): Token {
        consumeWhitespaceAndComments()
        if (index == source.length)
            return registry.token("(EOF)", "(EOF)", position)
        // go to next line symbol - to separate long expressions
        if (source[index] == '\\') {
            index++
            consumeWhitespaceAndComments()
            if (!isLineSeparator())
                throw PositionalException("Expected new line after \\", position = position, length = 1)
            else index = moveAfterLineSeparator()
            consumeWhitespaceAndComments()
            position = Pair(0, position.second + 1)
        }
        return if (source[index] == '"')
            nextString()
        else if (isFirstIdentChar(source[index]))
            nextIdent()
        else if (source[index].isDigit())
            nextNumber()
        else if (isOperatorChar(source[index]))
            nextOperator()
        else throw PositionalException("Invalid character", position = position, length = 1)
    }

    private fun nextString(): Token {
        val res = StringBuilder()
        moveAndAppend(res)
        while (source[index] != '"') {
            // escaped symbols in string
            if (source[index] == '\\' && source.length > index + 1) {
                move()
                res.append(escapes[source[index]])
                move()
            } else if (isLineSeparator() || index == source.lastIndex)
                throw Exception("Unterminated string at ${position.second}:${position.first}")
            else moveAndAppend(res)
        }
        moveAndAppend(res)
        return registry.string(
            "(STRING)",
            res.toString().substring(1, res.toString().length - 1),
            Pair(position.first - res.toString().length, position.second)
        )
    }

    private fun nextIdent(): Token {
        val res = StringBuilder()
        while (index < source.length && isIdentChar(source[index]))
            moveAndAppend(res)
        if (registry.defined(res.toString()))
            return registry.definedIdentifier(
                res.toString(), res.toString(),
                Pair(position.first - res.toString().length, position.second)
            )
        return registry.identifier(
            "(IDENT)",
            res.toString(),
            Pair(position.first - res.toString().length, position.second)
        )
    }

    private fun nextNumber(): Token {
        val res = StringBuilder()
        moveAndAppend(res)
        while (index < source.length && source[index].isDigit())
            moveAndAppend(res)
        if (index + 1 < source.length && source[index] == '.' && source[index + 1].isDigit()) {
            moveAndAppend(res)
            while (index < source.length && source[index].isDigit())
                moveAndAppend(res)
        }
        return TokenNumber(res.toString(), Pair(position.first - res.toString().length, position.second))
    }

    private fun nextOperator(): Token {
        for (ind in index + 2 downTo index) {
            if (ind < source.length && registry.defined(source.substring(index..ind))) {
                val value = source.substring(index..ind)
                val operator = registry.operator(value, value, Pair(position.first - value.length, position.second))
                if (operator.symbol == "(SEP)" && operator.value != ";") {
                    toNextLine()
                } else for (i in index..ind)
                    move()
                return operator
            }
        }
        throw PositionalException("Invalid operator", position = position)
    }

    private fun consumeWhitespaceAndComments(): Boolean {
        var iter = 0
        var (whitespace, comments) = listOf(true, true)
        while (whitespace || comments) {
            whitespace = consumeWhitespace()
            comments = consumeComments()
            if (comments)
                addToken(registry.token("(SEP)", "//", position))
            iter++
        }
        return iter > 1
    }

    private fun consumeComments(): Boolean {
        if (index < source.length && source[index] == '/' && index < source.lastIndex && source[index + 1] == '/') {
            while (!isLineSeparator()) {
                move()
                if (index == source.length)
                    return true
            }
            return true
        } else if (index < source.lastIndex && source[index] == '/' && source[index + 1] == '*') {
            move(2)
            if (index + 1 >= source.length)
                throw PositionalException("Unterminated comment", position = position)
            while (!(source[index] == '*' && source[index + 1] == '/')) {
                if (isLineSeparator())
                    toNextLine()
                else move()
                if (index >= source.lastIndex)
                    throw PositionalException("Unterminated comment", position = position)
            }
            move(2)
            return true
        }
        return false
    }

    private fun consumeWhitespace(): Boolean {
        // '\t', '\v', '\f', '\r', ' ', U+0085 (NEL), U+00A0 (NBSP).
        val res = index < source.length && !isLineSeparator() && source[index].isWhitespace()
        while (index < source.length && !isLineSeparator() && source[index].isWhitespace())
            move()
        return res
    }

    fun hasCommentAhead(): Boolean = consumeWhitespaceAndComments()

    private fun isLineSeparator(): Boolean {
        if (index == source.lastIndex)
            return source[index] == '\r' || source[index] == '\n'
        if (source[index] == '\r' && source[index + 1] == '\n')
            return true
        return source[index] == '\r' || source[index] == '\n'
    }

    private fun moveAfterLineSeparator(): Int {
        if (index == source.lastIndex)
            return if (source[index] == '\r' || source[index] == '\n') index + 1 else index
        if (source[index] == '\r' && source[index + 1] == '\n')
            return index + 2
        return if (source[index] == '\r' || source[index] == '\n') index + 1 else index
    }

    /**
     * Add (SEP) token if last added token is not (SEP)
     *
     * (SEP) tokens' purpose is separating statements. Therefore, there is no need to have more than one (SEP) in a row.
     * Additionally, if-else statement denotation function in [RegistryFactory] relies on one (SEP) in a row.
     */
    private fun addToken(newToken: Token) {
        if (tokens.last().symbol != "(SEP)" || newToken.symbol != "(SEP)")
            tokens.add(newToken)
    }

    private fun move(step: Int = 1) {
        index += step
        position = Pair(position.first + step, position.second)
    }

    private fun moveAndAppend(sb: java.lang.StringBuilder) {
        sb.append(source[index])
        move()
    }

    private fun toNextLine() {
        index = moveAfterLineSeparator()
        position = Pair(0, position.second + 1)
    }

    private fun isFirstIdentChar(c: Char): Boolean = c.isLetter() || c == '_'
    private fun isIdentChar(c: Char): Boolean = c.isLetterOrDigit() || c == '_'
    private fun isOperatorChar(c: Char): Boolean = operators.toCharArray().any { it == c }
}
