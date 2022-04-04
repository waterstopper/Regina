/**
 * AST building algorithm was taken and rewritten from:
 * https://www.cristiandima.com/top-down-operator-precedence-parsing-in-go
 */
package lexer

class Token(
    var symbol: String = "",
    var value: String = "",
    val position: Pair<Int, Int> = Pair(0, 0),
    val bindingPower: Int = 0,
    var nud: ((token: Token, parser: Parser) -> Token)? = null,
    var led: ((token: Token, parser: Parser, token2: Token) -> Token)? = null,
    var std: ((token: Token, parser: Parser) -> Token)? = null,
    val children: MutableList<Token> = mutableListOf()
) {
//    fun nud(token: Token, parser: Parser): Token {
//        return this
//    }
//
//    fun led(token: Token, parser: Parser, token2: Token): Token {
//        return this
//    }
//
//    fun std(token: Token, parser: Parser): Token {
//        return this
//    }

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

    fun findAndRemove(symbol: String) {
        val inChildren = children.find { it.value == symbol }
        if (inChildren != null)
            children.remove(inChildren)
        else
            for (t in children)
                t.findAndRemove(symbol)
    }

    override fun toString(): String = if (symbol == value) symbol else "$symbol:$value"

//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as Token
//
//        if (symbol != other.symbol) return false
//        if (value != other.value) return false
//        if (bindingPower != other.bindingPower) return false
//        if (nud != other.nud) return false
//        if (led != other.led) return false
//        if (std != other.std) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = symbol.hashCode()
//        result = 31 * result + value.hashCode()
//        result = 31 * result + bindingPower
//        result = 31 * result + (nud?.hashCode() ?: 0)
//        result = 31 * result + (led?.hashCode() ?: 0)
//        result = 31 * result + (std?.hashCode() ?: 0)
//        return result
//    }
}