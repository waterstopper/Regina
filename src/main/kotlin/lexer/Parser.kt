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
        var left = t.nud?.let { it(t, this) } ?:
        throw PositionalException(
            "Expected variable or prefix operator", position = t.position, length = 1
        )
        while (rbp < lexer.peek().bindingPower) {
            t = lexer.next()
            left = t.led?.let { it(t, this, left) } ?: throw PositionalException(
                "Expected infix or suffix operator", position = t.position, length = 1
            )
        }
        return left
    }

    fun advance(symbol: String): Token {
        var token = lexer.next()
        // ignore separators
        while (token.symbol == "(SEP)" && symbol != "(SEP)" && symbol != "(EOF)")
            token = lexer.next()
        if (token.symbol == symbol)
            return token
        if (symbol == "(SEP)" && token.symbol == "(EOF)")
            return token
        throw PositionalException("Expected ${if(symbol=="(SEP)") "statement separator" else symbol}",
            position = token.position, length = 1)
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
        var token = lexer.peek()
        if (token.std != null) {
            token = lexer.next()
            return token.std?.let { it(token, this) } ?: throw PositionalException(
                "Expected statement", position = token.position, length = 1
            )
        }
        if (token.symbol == "(SEP)") {
            lexer.next()
            return token
        }
        token = expression(0)
        val peeked = lexer.peek()
        if (peeked.symbol != "}")
            advance("(SEP)")
//        if (peeked.symbol == "\n" || peeked.symbol == "(EOF)")
//            advance("\n")
//        else if (peeked.symbol != "}")
//            throw PositionalException("Expected block end or line break", peeked)
        return token
    }

    fun block(canBeSingleStatement: Boolean = false): Token {
        var token = lexer.next()
        while (token.symbol == "(SEP)")
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
            "Expected statement", position = token.position
        )
    }
}
