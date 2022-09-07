package lexer

import token.Token
import token.TokenBlock

/**
 * Parses created tokens into AST
 *
 * Changes with Go implementation:
 * 1. [advance] changed
 * 2. [block] supports one statement block without braces
 */
class Parser(text: String, val filePath: String) {
    var lexer: Lexer

    init {
        lexer = Lexer(text, filePath)
    }

    fun expression(rbp: Int): Token {
        var t = lexer.next()
        var left = t.nud?.let { it(t, this) }
            ?: throw PositionalException(
                "Expected variable or prefix operator",
                position = t.position,
                length = t.value.length,
                fileName = filePath
            )
        while (rbp < lexer.peek().bindingPower) {
            t = lexer.next()
            left = t.led?.let { it(t, this, left) } ?: throw PositionalException(
                "Expected infix or suffix operator", position = t.position, length = t.value.length, fileName = filePath
            )
        }
        return left
    }

    fun advance(symbol: String): Token {
        val token = lexer.next()
        if (token.symbol == symbol)
            return token
        throw PositionalException(
            "Expected $symbol",
            position = token.position,
            length = token.symbol.length,
            fileName = filePath
        )
    }

    fun advanceSeparator() {
        lexer.moveAfterSeparator()
    }

    fun statements(): List<Token> {
        val statements = mutableListOf<Token>()
        var next = lexer.peek()
        while (next.symbol != "(EOF)" && next.symbol != "}") {
            statements.add(statement())
            next = lexer.peek()
        }
        return statements.filter { it.symbol != "(SEP)" }
    }

    fun statement(): Token {
        if (lexer.peek().symbol == "(SEP)")
            return lexer.next()
        var token = lexer.peek()
        if (token.std != null) {
            token = lexer.next()
            return token.std?.let { it(token, this) }
                ?: throw PositionalException(
                    "Expected statement",
                    position = token.position,
                    length = token.value.length,
                    fileName = filePath
                )
        }
        token = expression(0)
        val peeked = lexer.peek()
        if (peeked.symbol != "}")
            advanceSeparator()
        return token
    }

    fun block(canBeSingleStatement: Boolean = false): Token {
        val token = lexer.next()
        if (token.symbol != "{") {
            if (canBeSingleStatement) {
                lexer.prev()
                val res = TokenBlock(Pair(token.position.first - 1, token.position.second))
                res.children.add(statement())
                if(res.children.first().symbol=="(SEP)")
                    res.children.clear()
                return res
            }
            throw PositionalException(
                "Expected a block start '{'",
                position = token.position,
                length = token.value.length,
                fileName = filePath
            )
        }
        return token.std?.let { it(token, this) }
            ?: throw PositionalException(
                "Expected statement",
                position = token.position,
                length = token.value.length,
                fileName = filePath
            )
    }
}
