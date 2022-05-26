/**
 * AST building algorithm was taken and rewritten from:
 * https://www.cristiandima.com/top-down-operator-precedence-parsing-in-go
 */
package token

import lexer.Parser
import lexer.PositionalException
import properties.Type
import table.SymbolTable
import token.operator.TokenTernary
import token.statement.Assignment

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
        get() = if(children.isEmpty()) {
            println()
            Token("")
        } else children[0]
    val right: Token
        get() = children[1]

//    open fun copy(): Token =
//        Token(symbol, value, position, bindingPower, nud, led, std, children.map { it.copy() }.toMutableList())

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

//    fun findAndReplace(symbol: String,newValue:String):Token?{
//        for(t in children){
//            //if(t.symbol==symbol)
//        }
//    }

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
        throw PositionalException("Not implemented", this)
    }

    /**
     * BFS all tokens. If  returned Token(LEAVE) - do not visit children
     */
    fun traverseUntil(condition: (token: Token) -> Token?): Token? {
        val forThis = condition(this)
        if (forThis != null && forThis.symbol != "(LEAVE)")
            return forThis
        if (forThis == null)
            for (i in children) {
                val childRes = i.traverseUntil(condition)
                if (childRes != null && childRes.symbol != "(LEAVE)")
                    return childRes
            }
        return condition(this)
    }

    fun traverseUnresolved(symbolTable: SymbolTable, parent: Type): Assignment? {
        val res = traverseUntil {
            when (it) {
                is TokenTernary -> {
                    it.left.traverseUnresolved(symbolTable, parent)
                        ?: if (it.evaluateCondition(symbolTable) != 0)
                            it.right.traverseUnresolved(symbolTable, parent) ?: Token("(LEAVE)")
                        else it.children[2].traverseUnresolved(symbolTable, parent) ?: Token("(LEAVE)")
                }
                is Assignable -> it.getFirstUnassigned(parent, symbolTable) ?: Token("(LEAVE)")
                else -> null
            }
        }
        if (res is Token && res.symbol == "(LEAVE)")
            return null
        return res as Assignment?
    }


    override fun equals(other: Any?): Boolean {
        if (other !is Token)
            return false
        if (children.size != other.children.size)
            return false
        if (this.value != other.value)
            return false
        var areEqual = true
        for (i in children.indices)
            areEqual = children[i].equals(other.children[i])
        return areEqual
    }

    override fun hashCode(): Int {
        var hash = value.hashCode()
        for ((i, c) in children.withIndex())
            hash += c.hashCode() * (i + 1)
        return hash
    }
}