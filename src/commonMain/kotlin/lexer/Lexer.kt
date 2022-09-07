package lexer

import Logger
import TokenNumber
import lexer.RegistryFactory.getRegistry
import token.MetaToken
import token.Token

/**
 * Lexer creates tokens from text
 *
 * Important changes:
 * 1. ";" is replaced by \n
 * 2. ternary operator and functions follow kotlin style
 */
class Lexer(val source: String = "", val filePath: String) {

    private val operators = "!@$%^*-+=?.,:;\"&|/(){}[]><\n\r"
    private val escapes = mutableMapOf('"' to '\"', '\\' to '\\', 'b' to '\b', 'n' to '\n', 'r' to '\r', 't' to '\t')
    private var registry: Registry = getRegistry(filePath)
    private var index: Int = 0
    var position: Pair<Int, Int> = Pair(0, 0)
    private val nodes = mutableListOf<Token>()
    private var tokenIndex = -1

    init {
        addTokens()
    }

    private fun addTokens() {
        nodes.add(Token("fictive")) // this is added to prevent tokens.last()
        // check throwing exception if first token is comment
        nodes.add(createNextToken())
        while (index < source.length)
            addToken(createNextToken())
        if (nodes.last().symbol != "(EOF)")
            nodes.add(Token("(EOF)", "(EOF)", position))
        if (nodes.size >= 100000)
            Logger.addWarning(nodes.last(), "File too large")
        nodes.removeAt(0)
    }

    fun next(): Token {
        // in current implementation if is good too, but it is safer with while if there are some changes
        while (nodes[++tokenIndex].value == "//") {
        }
        return nodes[tokenIndex]
    }

    fun prev(): Token = nodes[--tokenIndex]

    fun moveAfterSeparator() {
        tokenIndex++
        if (nodes[tokenIndex].symbol != "(SEP)" && nodes[tokenIndex].symbol != "(EOF)")
            throw SyntaxException("Expected separator", filePath, nodes[tokenIndex])
    }

    fun peek(): Token {
        var offset = 1
        if (tokenIndex + offset > nodes.lastIndex)
            return nodes.last()
        while (nodes[tokenIndex + offset].value == "//")
            offset++
        return nodes[tokenIndex + offset]
    }

    fun peekSeparator(): Boolean = nodes[tokenIndex + 1].symbol == "(SEP)"

    /**
     * Once is fictive now, because two separators in a row are merged in one. This behavior might change
     */
    fun moveAfterTokenLineSeparator(once: Boolean = true) {
        if (peek().value.contains(Regex("[\n\r]")))
            next()
        if (!once) {
            while (peek().value.contains(Regex("[\n\r]")))
                next()
        }
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
                throw PositionalException(
                    "Expected new line after \\",
                    position = position,
                    length = 1,
                    fileName = filePath
                )
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
        else if (source[index] == '#')
            nextMeta()
        else throw PositionalException("Invalid character", position = position, length = 1, fileName = filePath)
    }

    private fun nextMeta(): Token {
        val res = StringBuilder()
        res.append(source[index])
        move()
        while (index < source.length && isIdentChar(source[index]))
            moveAndAppend(res)
        if (registry.defined(res.toString())) {
            return MetaToken(
                res.toString(),
                res.toString(),
                Pair(position.first - res.length, position.second)
            )
        }
        throw PositionalException(
            "No such meta token",
            position = Pair(position.first - res.length, position.second),
            length = res.length, fileName = filePath
        )
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
                throw PositionalException(
                    "Unterminated string at ${position.second}:${position.first}",
                    position = Pair(position.first - res.length, position.second),
                    length = res.length, fileName = filePath
                )
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
                if (operator.symbol == "(SEP)"
                    && (operator.value.contains(Regex("[\n\r]")))
                ) {
                    toNextLine()
                } else for (i in index..ind)
                    move()
                return operator
            }
        }
        throw PositionalException("Invalid operator", position = position, fileName = filePath)
    }

    private fun consumeWhitespaceAndComments(): Boolean {
        var iter = 0
        var (whitespace, comments) = listOf(true, true)
        while (whitespace || comments) {
            whitespace = consumeWhitespace()
            comments = consumeComments()
            if (comments) // TODO here (COMMENT)
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
            val startingPosition = position
            move(2)
            if (index + 1 >= source.length)
                throw PositionalException(
                    "Unterminated comment",
                    position = startingPosition,
                    length = 2,
                    fileName = filePath
                )
            while (!(source[index] == '*' && source[index + 1] == '/')) {
                if (isLineSeparator())
                    toNextLine()
                else move()
                if (index >= source.lastIndex)
                    throw PositionalException(
                        "Unterminated comment",
                        position = startingPosition,
                        length = 2,
                        fileName = filePath
                    )
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
    private fun addToken(newNode: Token) {
        if (nodes.last().symbol != "(SEP)" || newNode.symbol != "(SEP)")
            nodes.add(newNode)
    }

    private fun move(step: Int = 1) {
        index += step
        position = Pair(position.first + step, position.second)
    }

    private fun moveAndAppend(sb: StringBuilder) {
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
