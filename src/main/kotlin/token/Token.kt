/**
 * AST building algorithm was taken and rewritten from:
 * https://www.cristiandima.com/top-down-operator-precedence-parsing-in-go
 */
package token

import lexer.Parser
import SymbolTable
import lexer.PositionalException

open class Token(
    var symbol: String = "",
    var value: String = "",
    val position: Pair<Int, Int> = Pair(0, 0),
    val bindingPower: Int = 0, // precedence priority
    var nud: ((token: Token, parser: Parser) -> Token)? = null, // null denotation: values, prefix operators
    var led: ((token: Token, parser: Parser, token2: Token) -> Token)? = null, // left denotation: infix and suffix operators
    var std: ((token: Token, parser: Parser) -> Token)? = null, // statement denotation
    val children: MutableList<Token> = mutableListOf()
) {
    val left: Token
        get() = children[0]
    val right: Token
        get() = children[1]

    fun copy(): Token =
        Token(symbol, value, position, bindingPower, nud, led, std, children.map { it.copy() }.toMutableList())

    fun toTreeString(indentation: Int = 0): String {
        val res = StringBuilder()
        for (i in 0 until indentation)
            res.append(' ')
        res.append(this)
        if (children.size > 0)
            for (i in children)
                res.append('\n' + i.toTreeString(indentation + 2))

        return res.toString()
    }

    fun find(symbol: String): Token? {
        if (this.symbol == symbol)
            return this
        for (t in children) {
            val inChild = t.find(symbol)
            if (inChild != null)
                return inChild
        }
        return null
    }

    private fun findAndRemove(symbol: String) {
        val inChildren = children.find { it.value == symbol }
        if (inChildren != null)
            children.remove(inChildren)
        else
            for (t in children)
                t.findAndRemove(symbol)
    }

    override fun toString(): String = if (symbol == value) symbol else "$symbol:$value"

    open fun evaluate(symbolTable: SymbolTable): Any {
        throw PositionalException("not implemented", this)
    }
}