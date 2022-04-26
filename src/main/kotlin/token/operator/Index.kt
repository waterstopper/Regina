package token.operator

import lexer.Parser
import lexer.PositionalException
import properties.Type
import properties.primitive.PArray
import properties.primitive.Primitive
import table.SymbolTable
import token.Assignable
import token.Token
import token.statement.Assignment

class Index(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?, children: List<Token> = listOf()
) : Token(symbol, value, position, bindingPower, nud, led, std), Assignable {
    constructor(token: Token) : this(
        token.symbol,
        token.value,
        token.position,
        token.bindingPower,
        token.nud,
        token.led,
        token.std,
        token.children
    )

    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        val res = evaluateIndex(symbolTable)
        if (res is Primitive)
            return res.value
        return res
    }

    private fun evaluateIndex(symbolTable: SymbolTable): Any {
        val array = left.evaluate(symbolTable)
        val index = right.evaluate(symbolTable)
        if (index is Int) {
            return when (array) {
                is MutableList<*> -> if (index < array.size) array[index]!!
                else throw PositionalException("index $index out of bounds for array of size ${array.size}", this)
                is String -> if (index < array.length) array[index].toString()
                else throw PositionalException("index $index out of bounds for string of length ${array.length}", this)
                else -> throw PositionalException("array or string expected", this)
            }
        } else throw PositionalException("expected Int as index", this)
    }

    fun getArrayAndIndex(symbolTable: SymbolTable): Pair<PArray, Int> {
        val array = symbolTable.getVariable(left)
        val number = right.evaluate(symbolTable)
        if (array is PArray && number is Int)
            return Pair(array, number)
        throw PositionalException("expected array and number", this)
    }

    override fun assign(parent: Type) {
        TODO("Not yet implemented")
    }

    override fun getAssignment(parent: Type): Assignment {
        TODO("Not yet implemented")
    }

    override fun getFirstUnassigned(parent: Type): Assignment? {
        if (left is Assignable) {
            val fromAnother = (left as Assignable).getFirstUnassigned(parent)
            if (fromAnother != null) return fromAnother
        }
        if (parent.getAssignment(this) != null)
            return parent.getAssignment(this)
        return null
    }
}