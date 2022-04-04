/**
 * AST building algorithm was taken and rewritten from:
 * https://www.cristiandima.com/top-down-operator-precedence-parsing-in-go
 */
package lexer

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
            position = lexer.position,length = 1
        )
        while (rbp < lexer.peek().bindingPower) {
            t = lexer.next()
            left = t.led?.let { it(t, this, left) } ?: throw PositionalException(
                "expected infix or suffix operator",
                position = lexer.position,length = 1
            )
        }
        return left
    }

    fun advance(symbol: String): Token {
        val token = lexer.next()
        // program can end without line break
        if (symbol == "\n" && token.symbol == "(EOF)")
            return token
        if (token.symbol != symbol)
            throw PositionalException("expected ${if (symbol != "\n") symbol else "line break"}", position = lexer.position,length = 1)
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
            return token.std?.let { it(token, this) } ?: throw PositionalException("expected statement", position = lexer.position,length = 1)
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
        val token = lexer.next()
        if (token.symbol != "{")
            throw PositionalException("expected a block start '{'", position = lexer.position)
        return token.std?.let { it(token, this) } ?: throw PositionalException("expected statement", position = lexer.position)
    }
}