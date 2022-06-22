package lexer

import token.Token
import token.statement.Block

/**
 * Parses created tokens into AST
 *
 * Changes: advance changed to ignore \n
 */
class Parser() {
    lateinit var lexer: Lexer

    constructor(text: String) : this() {
        lexer = Lexer(text)
    }

    constructor(lexer: Lexer) : this() {
        this.lexer = lexer
    }

    fun expression(rbp: Int): Token {
        var t = lexer.next()
        var left = t.nud?.let { it(t, this) } ?: throw PositionalException(
            "Expected variable or prefix operator",
            position = lexer.position, length = 1
        )
        while (rbp < lexer.peek().bindingPower) {
            t = lexer.next()
            left = t.led?.let { it(t, this, left) } ?: throw PositionalException(
                "Expected infix or suffix operator",
                position = lexer.position, length = 1
            )
        }
        return left
    }

    fun advance(symbol: String): Token {
        // to make possible writing comments on same line after statements
        if (symbol == "\n" && lexer.hasCommentAhead())
            return Token()
        var token = lexer.next()
        // program can end without line break
        if (symbol == "\n" && token.symbol == "(EOF)")
            return token
//        // TODO added these lines to ignore \n
        while (token.value == "\n" && symbol != "\n")
            token = lexer.next()
        if (token.symbol != symbol)
            throw PositionalException(
                "Expected ${if (symbol != "\n") symbol else "line break"}",
                position = lexer.position,
                length = 1
            )
        return token
    }

    fun statements(): List<Token> {
        val statements = mutableListOf<Token>()
        var next = lexer.peek()
        while (next.symbol != "(EOF)" && next.symbol != "}") {
            statements.add(statement())
            next = lexer.peek()
        }
        return statements.filter { it.symbol != "\n" }
    }

    fun statement(): Token {
        var token = lexer.peek()
        if (token.std != null) {
            token = lexer.next()
            return token.std?.let { it(token, this) } ?: throw PositionalException(
                "expected statement",
                position = lexer.position,
                length = 1
            )
        }
        if (token.symbol == "\n") {
            lexer.next()
            return token
        }
        token = expression(0)
        val peeked = lexer.peek()
        if (peeked.symbol == "\n" || peeked.symbol == "(EOF)")
            advance("\n")
        else if (peeked.symbol != "}")
            throw PositionalException("Expected block end or line break", peeked)
        return token
    }

    fun block(canBeSingleStatement: Boolean = false): Token {
        var token = lexer.next()
        if (token.symbol == "\n")
            token = lexer.next()
        if (token.symbol != "{") {
            if (canBeSingleStatement) {
                lexer.prev()
                val res = Block(Pair(token.position.first - 1, token.position.second))
                res.children.add(statement())
                return res
            }
            throw PositionalException("Expected a block start '{'", position = token.position)
        }
        return token.std?.let { it(token, this) } ?: throw PositionalException(
            "Expected statement",
            position = lexer.position
        )
    }
}