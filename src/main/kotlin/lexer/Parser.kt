package lexer

import node.Node
import node.statement.Block

/**
 * Parses created tokens into AST
 *
 * Changes with Go implementation:
 * 1. [advance] changed
 * 2. [block] supports one statement block without braces
 */
class Parser() {
    lateinit var lexer: Lexer

    constructor(text: String) : this() {
        lexer = Lexer(text)
    }

    fun expression(rbp: Int): Node {
        var t = lexer.next()
        var left = t.nud?.let { it(t, this) }
            ?: throw PositionalException(
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

    fun advance(symbol: String): Node {
        var token = lexer.next()
        if (token.symbol == symbol)
            return token
        throw PositionalException("Expected $symbol", position = token.position, length = 1)
    }

    fun advanceSeparator() {
        lexer.moveAfterSeparator()
    }

    fun statements(): List<Node> {
        val statements = mutableListOf<Node>()
        var next = lexer.peek()
        while (next.symbol != "(EOF)" && next.symbol != "}") {
            statements.add(statement())
            next = lexer.peek()
        }
        return statements.filter { it.symbol != "(SEP)" }
    }

    fun statement(): Node {
        var token = lexer.peek()
        if (token.std != null) {
            token = lexer.next()
            return token.std?.let { it(token, this) }
                ?: throw PositionalException("Expected statement", position = token.position, length = 1)
        }
        token = expression(0)
        val peeked = lexer.peek()
        if (peeked.symbol != "}")
            advanceSeparator()
        return token
    }

    fun block(canBeSingleStatement: Boolean = false): Node {
        val token = lexer.next()
        if (token.symbol != "{") {
            if (canBeSingleStatement) {
                lexer.prev()
                val res = Block(Pair(token.position.first - 1, token.position.second))
                res.children.add(statement())
                return res
            }
            throw PositionalException("Expected a block start '{'", position = token.position)
        }
        return token.std?.let { it(token, this) }
            ?: throw PositionalException("Expected statement", position = token.position)
    }
}
