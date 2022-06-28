package lexer

import token.Identifier
import token.Token
import token.TokenFactory
import token.variable.TokenNumber
import token.variable.TokenString

/**
 * Registers common token groups, such as:
 * * [Statement][stmt] usually a token that can be on one line (assignment, declaration, block)
 * * [Infix][stmt] parses left and right expressions into its children. Examples: +, /, *, is, ==.
 * [Right infix][infixRight] is similar, but is right associative (like assignment).
 * * [Prefix][prefix] makes expression on the right its child. Examples: unary minus, logic not '!'
 *
 * @property table dictionary of registered token symbols mapped to created tokens
 */
class Registry {
    private val table = mutableMapOf<String, Token>()

    private fun register(
        symbol: String,
        bp: Int,
        nud: ((token: Token, parser: Parser) -> Token)?,
        led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
        std: ((token: Token, parser: Parser) -> Token)?,
    ) {
        if (table[symbol] != null) {
            // It is debatable whether these should be overridden on `register` call or not
            // This is initial (not my) design
            val value = table[symbol]!!
            if (nud != null && value.nud == null)
                value.nud = nud
            if (led != null && value.led == null)
                value.led = led
            if (std != null && value.std == null)
                value.std = std
        } else table[symbol] = Token(bindingPower = bp, nud = nud, led = led, std = std)
    }

    fun prefix(symbol: String) {
        register(symbol, 0, { t: Token, p: Parser ->
            t.children.add(p.expression(100))
            t
        }, null, null)
    }

    /**
     * This function exists solely because abs(Int.MAX_VALUE) < abs(Int.MIN_VALUE).
     * Therefore, standard evaluation won't work (it will produce NumberFormatException)
     */
    fun unaryMinus(symbol: String) {
        register(symbol, 0, { t: Token, p: Parser ->
            t.children.add(p.expression(100))
            if (t.left is TokenNumber) {
                t.left.value = "-${t.left.value}"
                t.left
            } else t
        }, null, null)
    }

    fun infix(symbol: String, bp: Int) {
        register(symbol, bp, null, { t: Token, p: Parser, left: Token ->
            t.children.add(left)
            t.children.add(p.expression(t.bindingPower))
            t
        }, null)
    }

    fun operator(symbol: String, value: String, position: Pair<Int, Int>): Token {
        return TokenFactory.createOperator(
            symbol,
            value,
            position,
            table[symbol]!!.bindingPower,
            table[symbol]!!.nud,
            table[symbol]!!.led,
            table[symbol]!!.std
        )
    }

    fun string(symbol: String, value: String, position: Pair<Int, Int>): TokenString =
        TokenString(
            symbol,
            value,
            position,
            table[symbol]!!.bindingPower,
            table[symbol]!!.nud,
            table[symbol]!!.led,
            table[symbol]!!.std
        )

    fun token(symbol: String, value: String, position: Pair<Int, Int>): Token =
        Token(
            symbol,
            value,
            position,
            table[symbol]!!.bindingPower,
            table[symbol]!!.nud,
            table[symbol]!!.led,
            table[symbol]!!.std
        )

    fun symbol(symbol: String) {
        register(symbol, 0, { t: Token, _: Parser -> t }, null, null)
    }

    fun consumable(symbol: String) {
        register(symbol, 0, null, null, null)
    }

    fun infixLed(symbol: String, bp: Int, led: ((token: Token, parser: Parser, token2: Token) -> Token)?) {
        register(symbol, bp, null, led, null)
    }

    fun infixRight(symbol: String, bp: Int) {
        register(symbol, bp, null, { t: Token, p: Parser, left: Token ->
            t.children.add(left)
            t.children.add(p.expression(t.bindingPower - 1))
            t
        }, null)
    }

    fun prefixNud(symbol: String, nud: (token: Token, parser: Parser) -> Token) {
        register(symbol, 0, nud, null, null)
    }

    fun infixRightLed(symbol: String, bp: Int, led: ((token: Token, parser: Parser, token2: Token) -> Token)) {
        register(symbol, bp, null, led, null)
    }

    fun stmt(symbol: String, std: (token: Token, parser: Parser) -> Token) {
        register(symbol, 0, null, null, std)
    }

    fun defined(symbol: String): Boolean = table[symbol] != null

    fun definedIdentifier(symbol: String, value: String, position: Pair<Int, Int>): Token {
        return TokenFactory.createWordToken(
            symbol, value, position,
            table[symbol]!!.bindingPower,
            table[symbol]!!.nud,
            table[symbol]!!.led,
            table[symbol]!!.std
        )
    }

    fun identifier(symbol: String, value: String, position: Pair<Int, Int>) = Identifier(
        symbol,
        value,
        position,
        table[symbol]!!.bindingPower,
        table[symbol]!!.nud,
        table[symbol]!!.led,
        table[symbol]!!.std
    )
}
