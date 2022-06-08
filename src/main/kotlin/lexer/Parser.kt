package lexer

import token.Token

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
            "expected variable or prefix operator",
            position = lexer.position, length = 1
        )
        while (rbp < lexer.peek().bindingPower) {
            t = lexer.next()
            left = t.led?.let { it(t, this, left) } ?: throw PositionalException(
                "expected infix or suffix operator",
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
                "expected ${if (symbol != "\n") symbol else "line break"}",
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
        advance("\n")
        return token
    }

    fun block(): Token {
        var token = lexer.next()
        if(token.symbol == "\n")
            token = lexer.next()
        if (token.symbol != "{")
            throw PositionalException("expected a block start '{'", position = lexer.position)
        return token.std?.let { it(token, this) } ?: throw PositionalException(
            "expected statement",
            position = lexer.position
        )
    }
}