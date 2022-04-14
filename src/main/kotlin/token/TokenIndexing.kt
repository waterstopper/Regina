package token

import evaluation.ValueEvaluation
import lexer.Parser
import lexer.PositionalException
import properties.primitive.Primitive
import SymbolTable

class TokenIndexing(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?
) : Token(symbol, value, position, bindingPower, nud, led, std) {
    // get array
    fun getParent(): Any {
        return ""
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        val res = evaluateIndex(symbolTable)
        if (res is Primitive)
            return res.value
        return res
    }

    private fun evaluateIndex(symbolTable: SymbolTable): Any {
        val array = ValueEvaluation.evaluateValue(left, symbolTable)
        val index = ValueEvaluation.evaluateValue(right, symbolTable)
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
}