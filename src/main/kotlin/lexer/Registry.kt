/**
 * AST building algorithm was taken and rewritten from:
 * https://www.cristiandima.com/top-down-operator-precedence-parsing-in-go
 */
package lexer

class Registry {
    val table = mutableMapOf<String, Token>()

    fun register(
        symbol: String, bp: Int,
        nud: ((token: Token, parser: Parser) -> Token)?,
        led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
        std: ((token: Token, parser: Parser) -> Token)?,
    ) {
        if (table[symbol] != null) {
            val value = table[symbol]!!
            if (nud != null && value.nud == null)
                value.nud = nud
            if (led != null && value.led == null)
                value.led = led
            if (std != null && value.std == null)
                value.std = std

        } else
            table[symbol] = Token(bindingPower = bp, nud = nud, led = led, std = std)
    }

    fun prefix(symbol: String) {
        register(symbol, 0, { t: Token, p: Parser ->
            t.children.add(p.expression(100))
            t
        }, null, null)
    }

    fun infix(symbol: String, bp: Int) {
        register(symbol, bp, null, { t: Token, p: Parser, left: Token ->
            t.children.add(left)
            t.children.add(p.expression(t.bindingPower))
            t
        }, null)
    }

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
        register(symbol, 0, { t: Token, p: Parser -> t }, null, null)
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

}