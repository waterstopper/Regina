package token

import lexer.Parser
import lexer.PositionalException
import properties.primitive.Primitive
import SymbolTable
import properties.Variable
import properties.primitive.PArray

class TokenIndexing(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?, children: List<Token> = listOf()
) : Token(symbol, value, position, bindingPower, nud, led, std) {
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
            println(array)
            return when (array) {
                is MutableList<*> -> if (index < array.size) array[index]!!
                else throw PositionalException("index $index out of bounds for array of size ${array.size}", this)
                is String -> if (index < array.length) array[index].toString()
                else throw PositionalException("index $index out of bounds for string of length ${array.length}", this)
                else -> throw PositionalException("array or string expected", this)
            }
        } else throw PositionalException("expected Int as index", this)
    }

    /**
     * TODO make method better
     */
    fun getIndexedVariable(symbolTable: SymbolTable): Variable {
        val number = right.evaluate(symbolTable)
        if (left is TokenIdentifier && number is Int) {
            val variable = symbolTable.getVariable(left)
            if (variable is PArray)
                return variable.getByIndex(this, number)
            throw PositionalException("expected array", left)
        }
        throw PositionalException("expected array as left and int as right", this)
    }

    fun getArrayAndIndex(symbolTable: SymbolTable): Pair<PArray, Int> {
        val array = symbolTable.getVariable(left)
        val number = right.evaluate(symbolTable)
        if (array is PArray && number is Int)
            return Pair(array, number)
        throw PositionalException("expected array and number", this)
    }
}